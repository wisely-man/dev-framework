package com.wisely;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GateWayWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GateWayWebApplication.class, args);
    }


}