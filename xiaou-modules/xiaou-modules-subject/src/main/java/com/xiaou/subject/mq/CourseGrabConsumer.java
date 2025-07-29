package com.xiaou.subject.mq;

import com.xiaou.mq.config.RabbitMQConfig;
import com.xiaou.subject.domain.mq.CourseGrabMq;
import com.xiaou.subject.service.StudentCourseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 抢课消息队列消费者
 */
@Component
@Slf4j
public class CourseGrabConsumer {

    @Resource
    private StudentCourseService studentCourseService;

    /**
     * 处理抢课/退课异步持久化操作
     */
    @RabbitListener(queues = RabbitMQConfig.COURSE_GRAB_QUEUE)
    public void handleCourseGrabMessage(CourseGrabMq mq) {
        try {
            log.info("接收到抢课消息: studentId={}, courseId={}, operation={}",
                    mq.getStudentId(), mq.getCourseId(), mq.getOperation());

            studentCourseService.asyncProcessCourseOperation(
                    mq.getStudentId(),
                    mq.getCourseId(),
                    mq.getOperation());

            log.info("抢课消息处理完成: studentId={}, courseId={}",
                    mq.getStudentId(), mq.getCourseId());

        } catch (Exception e) {
            log.error("处理抢课消息失败: {}", mq, e);
            // 这里可以考虑重试机制或者将失败消息放入死信队列
        }
    }
}