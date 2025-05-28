package com.xiaou.notify.utils;


import com.xiaou.notify.domain.Notification;
import com.xiaou.notify.enums.NotificationTypeEnum;
import com.xiaou.notify.mapper.NotificationMapper;
import com.xiaou.websocket.holder.WebSocketSessionHolder;
import com.xiaou.websocket.utils.WebSocketUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationUtils {

    private final NotificationMapper notificationMapper;

    /**
     * 发送通知并通过 WebSocket 推送消息（仅在线用户）
     *
     * @param fromUserId 发送者ID
     * @param toUserId   接收者ID
     * @param type       通知类型
     * @param content    消息内容
     */
    public void sendNotification(Long fromUserId, Long toUserId, NotificationTypeEnum type, String content) {
        // 1. 持久化
        if (fromUserId.equals(toUserId)) {
            return;
        }
        Notification notification = new Notification();
        notification.setFromUserId(fromUserId);
        notification.setUserId(toUserId);
        notification.setType(type.getCode());
        notification.setContent(content);
        notificationMapper.insert(notification);

        // 2. 仅当用户在线时，才进行 WebSocket 推送
        if (WebSocketSessionHolder.existSession(toUserId)) {
            WebSocketUtils.sendMessage(toUserId, content);
            notification.setIs_read(true);
        } else {
            //不在线设置为未读
            notification.setIs_read(false);
        }
    }
}
