package com.xiaou.auth.user.listener;

import com.xiaou.auth.user.domain.mq.StudentMsgMQ;
import com.xiaou.mail.utils.MailUtils;
import com.xiaou.mq.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailConsumer {

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailMessage(StudentMsgMQ msg) {
        log.info("接收到邮件消息: " + msg);
        // TODO: 调用邮件发送服务发送邮件 发送到指定导员那里 如果没有指定导员，那么默认发送到管理员邮箱
        MailUtils.sendText("3153566913@qq.com", "又有新的学生注册申请请你去处理", "该学生为" + msg.getName());
    }
}
