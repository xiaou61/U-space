package com.xiaou.bbs.mq;

import com.xiaou.bbs.constants.NotifyConstants;
import com.xiaou.bbs.domain.entity.BbsUserNotify;
import com.xiaou.bbs.mapper.BbsUserNotifyMapper;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.mq.config.RabbitMQConfig;
import com.xiaou.sse.dto.SseMessageDto;
import com.xiaou.sse.utils.SseMessageUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BbsNotifyConsumer {
    private static final Logger log = LoggerFactory.getLogger(BbsNotifyConsumer.class);
    @Resource
    private BbsUserNotifyMapper userNotifyMapper;
    @RabbitListener(queues = RabbitMQConfig.BBS_QUEUE)
    public void handle(BbsUserNotifyMq mq) {
        BbsUserNotify convert = MapstructUtils.convert(mq, BbsUserNotify.class);
        userNotifyMapper.insert(convert);
        log.info("用户{}收到消息{}",mq.getReceiverId(),mq.getType());
        //发送SSE消息通知
        SseMessageDto sseMessageDto = new SseMessageDto();
        sseMessageDto.setType(mq.getType());
        //发送消息判断
        if (mq.getType().equals(NotifyConstants.Like)){
            log.info("用户点赞了帖子");
            sseMessageDto.setMessage("有用户点赞了你的帖子");
        }else if (mq.getType().equals(NotifyConstants.Comment)){
            sseMessageDto.setMessage("有用户评论了你的帖子");
        }else if (mq.getType().equals(NotifyConstants.Reply)){
            sseMessageDto.setMessage("有用户回复了你的评论");
        }
        sseMessageDto.setUserIds(List.of(mq.getReceiverId()));
        //发送消息
        SseMessageUtils.publishMessage(sseMessageDto);
    }
}
