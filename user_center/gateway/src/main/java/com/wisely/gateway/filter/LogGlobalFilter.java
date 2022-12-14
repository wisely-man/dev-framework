package com.wisely.gateway.filter;

import com.wisely.framework.helper.DateHelper;
import com.wisely.framework.helper.JsonHelper;
import com.wisely.framework.helper.StringHelper;
import com.wisely.framework.helper.ValidHelper;
import com.wisely.gateway.common.GateWayLogProperties;
import com.wisely.gateway.common.helper.AsyncKafkaBean;
import com.wisely.gateway.common.helper.WebHelper;
import com.wisely.gateway.entity.GatewayLog;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * ?????????????????????
 */
@Slf4j
public class LogGlobalFilter implements GlobalFilter, Ordered {

    public LogGlobalFilter(GateWayLogProperties properties, AsyncKafkaBean asyncKafkaBean) {
        this.properties = properties;
        this.asyncKafkaBean = asyncKafkaBean;
    }

    private GateWayLogProperties properties;

    private AsyncKafkaBean asyncKafkaBean;

    private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

    /**
     * ???????????????
     *
     * @return
     */
    public int getOrder() {
        return this.properties.getOrderNo();
    }

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        // ????????????
        String requestPath = request.getPath().pathWithinApplication().value();

        Route route = getGatewayRoute(exchange);
        if (!this.properties.getServerNames().contains(route.getUri().getAuthority())) {
            // ?????????????????????????????????????????????
            return chain.filter(exchange);
        }

        // ????????????
        GatewayLog gatewayLog = new GatewayLog();
        gatewayLog.setSchema(request.getURI().getScheme());
        gatewayLog.setRequestMethod(request.getMethodValue());
        gatewayLog.setToken(request.getHeaders().getFirst("_sgk"));
        gatewayLog.setRequestPath(requestPath);
        gatewayLog.setTargetServer(route.getId());
        gatewayLog.setRequestTime(LocalDateTime.now());
        gatewayLog.setIp(WebHelper.getServerHttpRequestIpAddress(request));

        MediaType mediaType = request.getHeaders().getContentType();
        // ?????????????????????application/json
        if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType) ||
                MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            return writeBodyLog(exchange, chain, gatewayLog);
        }

        return writeBasicLog(exchange, chain, gatewayLog);
    }

    private Mono<Void> writeBasicLog(ServerWebExchange exchange, GatewayFilterChain chain, GatewayLog accessLog) {
        StringBuilder builder = new StringBuilder();
        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            builder.append(entry.getKey()).append("=").append(StringHelper.join(entry.getValue(), ","));
        }
        accessLog.setRequestBody(builder.toString());

        //???????????????
        ServerHttpResponseDecorator decoratedResponse = recordResponseLog(exchange, accessLog);
        return chain.filter(exchange.mutate()
                .response(decoratedResponse).build())
                .then(Mono.fromRunnable(() -> {
                    // ???????????????kafka
                    writeAccessLog(accessLog);
                }));
    }


    private Mono writeBodyLog(ServerWebExchange exchange, GatewayFilterChain chain, GatewayLog gatewayLog) {

        ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);

        Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
                .flatMap(body -> {
                    gatewayLog.setRequestBody(body);
                    return Mono.just(body);
                });

        // ?????? BodyInserter ?????? body(????????????body), ?????? request body ??????????????????
        BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        // the new content type will be computed by bodyInserter
        // and then set in the request decorator
        headers.remove(HttpHeaders.CONTENT_LENGTH);

        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);

        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> {
                    // ??????????????????
                    ServerHttpRequest decoratedRequest = requestDecorate(exchange, headers, outputMessage);

                    // ??????????????????
                    ServerHttpResponseDecorator decoratedResponse = recordResponseLog(exchange, gatewayLog);

                    // ???????????????
                    return chain.filter(exchange.mutate().request(decoratedRequest).response(decoratedResponse).build())
                            .then(Mono.fromRunnable(() -> {
                                // ????????????
                                writeAccessLog(gatewayLog);
                            }));
                }));
    }

    /**
     * ????????????
     *
     * @param gatewayLog ????????????
     * @date 2021/3/24 14:53
     */
    private void writeAccessLog(GatewayLog gatewayLog) {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
        this.asyncKafkaBean.sendMessage(this.properties.getTopic(), JsonHelper.obj2Json(gatewayLog));
//        stopWatch.stop();
//        log.debug("writeAccessLog: {}", stopWatch.prettyPrint());
    }


    private Route getGatewayRoute(ServerWebExchange exchange) {
        return exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    }


    /**
     * ?????????????????????????????? headers
     *
     * @param exchange
     * @param headers
     * @param outputMessage
     * @return
     */
    private ServerHttpRequestDecorator requestDecorate(
            ServerWebExchange exchange,
            HttpHeaders headers,
            CachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    //TODO: this causes a 'HTTP/1.1 411 Length Required'
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }


    /**
     * ??????????????????
     * ?????? DataBufferFactory ????????????????????????????????????
     */
    private ServerHttpResponseDecorator recordResponseLog(ServerWebExchange exchange, GatewayLog gatewayLog) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();

        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    LocalDateTime responseTime = LocalDateTime.now();
                    gatewayLog.setResponseTime(responseTime);
                    // ??????????????????
                    long executeTime =
                            DateHelper.between(gatewayLog.getRequestTime(), gatewayLog.getResponseTime(), ChronoUnit.MILLIS);
                    gatewayLog.setExecuteTime(executeTime);

                    // ?????????????????????????????? json ?????????
                    String originalResponseContentType =
                            exchange.getAttribute(ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);


                    if (ValidHelper.isEquals(this.getStatusCode(), HttpStatus.OK)
                            && StringHelper.isNotBlank(originalResponseContentType)
                            && originalResponseContentType.contains("application/json")) {

                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        return super.writeWith(fluxBody.buffer().map(dataBuffers -> {

                            // ???????????????????????????????????????????????????
                            DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                            DataBuffer join = dataBufferFactory.join(dataBuffers);
                            byte[] content = new byte[join.readableByteCount()];
                            join.read(content);

                            // ???????????????
                            DataBufferUtils.release(join);
                            String responseResult = new String(content, StandardCharsets.UTF_8);


                            gatewayLog.setResponseData(responseResult);

                            return bufferFactory.wrap(content);
                        }));
                    }
                }
                // if body is not a flux. never got there.
                return super.writeWith(body);
            }
        };
    }
}
