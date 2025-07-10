package com.xiaou.bbs.mq;

import com.xiaou.bbs.constants.NotifyConstants;
import com.xiaou.bbs.domain.entity.BbsUserNotify;
import com.xiaou.bbs.mapper.BbsUserNotifyMapper;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.mq.config.RabbitMQConfig;
import com.xiaou.sse.dto.SseMessageDto;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BbsNotifyConsumer {
    @Resource
    private BbsUserNotifyMapper userNotifyMapper;
    @RabbitListener(queues = RabbitMQConfig.BBS_QUEUE)
    public void handle(BbsUserNotifyMq mq) {
        BbsUserNotify convert = MapstructUtils.convert(mq, BbsUserNotify.class);
        userNotifyMapper.insert(convert);
        //发送SSE消息通知
        SseMessageDto sseMessageDto = new SseMessageDto();
        sseMessageDto.setType(mq.getType());
        //发送消息判断
        if (mq.getType()== NotifyConstants.Like){
            sseMessageDto.setMessage("有用户点赞了你的帖子");
        }else if (mq.getType()== NotifyConstants.Comment){
            sseMessageDto.setMessage("有用户评论了你的帖子");
        }else if (mq.getType()== NotifyConstants.Reply){
            sseMessageDto.setMessage("有用户回复了你的评论");
        }
    }
}
