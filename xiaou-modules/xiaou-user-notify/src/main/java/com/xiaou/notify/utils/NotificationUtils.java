package com.xiaou.notify.utils;


import com.xiaou.notify.domain.Notification;
import com.xiaou.notify.enums.NotificationTypeEnum;
import com.xiaou.notify.mapper.NotificationMapper;
import com.xiaou.websocket.utils.WebSocketUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationUtils {

    private final NotificationMapper notificationMapper;

    /**
     * 发送通知并通过 WebSocket 推送消息
     * 
     * @param fromUserId 发送者ID
     * @param toUserId   接收者ID
     * @param type       通知类型
     * @param content    消息内容
     */
    public void sendNotification(Long fromUserId, Long toUserId, NotificationTypeEnum type, String content) {
        if (fromUserId.equals(toUserId)) {
            return; // 不发送给自己
        }

        Notification notification = new Notification();
        notification.setFromUserId(fromUserId);
        notification.setUserId(toUserId);
        notification.setType(type.getCode());
        notification.setContent(content);
        notificationMapper.insert(notification);

        WebSocketUtils.sendMessage(toUserId, content);
    }
}
