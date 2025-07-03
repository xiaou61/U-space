package com.xiaou.sse.listener;


import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.mq.config.RabbitMQConfig;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.sse.core.SseEmitterManager;
import com.xiaou.sse.dto.SseMessageDto;
import com.xiaou.sse.dto.UserNotifyMessage;
import com.xiaou.sse.mapper.UserNotifyMessageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NoticeConsumer {
    @Resource
    private SseEmitterManager emitterManager;
    @Resource
    private UserNotifyMessageMapper userNotifyMessageMapper;



    @RabbitListener(queues = RabbitMQConfig.NOTICE_QUEUE)
    public void handleNoticeMessage(SseMessageDto msg) {
        if (msg.getType() == null) {
            log.warn("收到消息type为空，消息内容: {}", msg);
            // 设置默认值
            msg.setType(GlobalConstants.NOKNOWN);
        }

        log.info("接收到MQ消息: " + msg.getMessage()+msg.getType());
        //如果userids为null 那么需要保存所有的
        if (msg.getUserIds() == null){
//            UserNotifyMessage userNotifyMessage = new UserNotifyMessage();
//            userNotifyMessage.setUserId(GlobalConstants.ALL);
//            userNotifyMessage.setContent(msg.getMessage());
//
//            //默认都设置为0
//            userNotifyMessage.setIsPush(GlobalConstants.ZERO);
//            userNotifyMessage.setType(msg.getType());
//            userNotifyMessageMapper.insert(userNotifyMessage);
            //todo ddd
            List<String> cacheList = RedisUtils.getCacheList(GlobalConstants.USER_ONLINE_KEY);
            if (cacheList != null){
                for (String userId : cacheList) {
                    extracted(msg, userId);
                }
            }
        }else {
            // 保存到数据库信息 可能会有多个用户的推送所以要保存多个用户
            for (String userId : msg.getUserIds()){
                extracted(msg, userId);

            }
        }


    }

    private void extracted(SseMessageDto msg, String userId) {
        UserNotifyMessage userNotifyMessage = new UserNotifyMessage();
        userNotifyMessage.setUserId(userId);
        userNotifyMessage.setContent(msg.getMessage());
        //在线的话设置为已推送 在线为1 不在线为0
        userNotifyMessage.setIsPush(emitterManager.isUserOnline(userId)?GlobalConstants.ONE:GlobalConstants.ZERO);
        userNotifyMessage.setType(msg.getType());
        userNotifyMessageMapper.insert(userNotifyMessage);
    }
}
