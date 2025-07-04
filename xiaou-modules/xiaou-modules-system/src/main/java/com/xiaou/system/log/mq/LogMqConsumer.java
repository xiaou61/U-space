package com.xiaou.system.log.mq;

import com.xiaou.mq.config.RabbitMQConfig;
import com.xiaou.system.log.domain.bo.SysOperLogBo;
import com.xiaou.system.log.domain.entity.SysOperLog;
import com.xiaou.system.log.mapper.SysOperLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class LogMqConsumer {

    @Resource
    private SysOperLogMapper baseMapper;
    @RabbitListener(queues = RabbitMQConfig.LOG_QUEUE)
    public void handleLogMessage(SysOperLogBo bo) {
        //入库操作
        SysOperLog operLog = new SysOperLog();
        BeanUtils.copyProperties(bo, operLog);
        operLog.setOperTime(new Date());
        baseMapper.insert(operLog);
    }
}
