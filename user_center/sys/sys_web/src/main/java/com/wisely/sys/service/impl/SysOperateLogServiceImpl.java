package com.wisely.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wisely.framework.entity.Model;
import com.wisely.framework.entity.PageVo;
import com.wisely.framework.helper.DateHelper;
import com.wisely.framework.helper.StringHelper;
import com.wisely.framework.helper.ValidHelper;
import com.wisely.sso.client.SsoConstants;
import com.wisely.sso.client.entity.SsoUser;
import com.wisely.sso.client.helper.UserHelper;
import com.wisely.sys.entity.SysOperateLog;
import com.wisely.sys.mapper.SysOperateLogMapper;
import com.wisely.sys.service.SysOperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;


@Service
@Slf4j
public class SysOperateLogServiceImpl implements SysOperateLogService, SsoConstants {

    @Value("${sys.log_topic:sysLog}")
    String sysLogTopic;

    @Resource
    SysOperateLogMapper sysOperateLogMapper;

    @Override
    public PageInfo findPage(SysOperateLog query, PageVo pageVo) {
        PageHelper.startPage(pageVo.getPageNo(), pageVo.getPageSize());
        query.setIsDeleted(0);
        return new PageInfo(sysOperateLogMapper.selectListBySelective(query));
    }

    @Override
    public Integer add(SysOperateLog sysOperateLog) {
        return sysOperateLogMapper.insertSelective(sysOperateLog);
    }


    @KafkaListener(topics = {"sysLog"})
    public void onMessage(ConsumerRecord<?, ?> consumerRecord) {
        Optional<?> optional = Optional.ofNullable(consumerRecord.value());
        if (optional.isPresent()) {
            Model sysLog = Model.parseObject(optional.get());
            SysOperateLog record = (SysOperateLog) sysLog.convertTo(SysOperateLog.class);
            if (StringHelper.isNotBlank(record.getToken())) {
                try {
                    SsoUser ssoUser = UserHelper.loadByTicket(record.getOsType(), record.getToken());
                    if (ValidHelper.isNotEmpty(ssoUser)) {
                        record.setUserId(ssoUser.getId());
                    }
                } catch (Exception e) {
                    log.error("load user error:{}", e);
                }
            }

            record.setIsDeleted(0);
            record.setCreateBy(1);
            record.setCreateTime(DateHelper.formatNow());
            sysOperateLogMapper.insertSelective(record);
        }
    }
}
