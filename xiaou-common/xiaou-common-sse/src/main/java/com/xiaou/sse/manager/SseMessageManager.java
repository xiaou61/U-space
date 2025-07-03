package com.xiaou.sse.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.sse.core.SseEmitterManager;
import com.xiaou.sse.dto.SseMessageDto;
import com.xiaou.sse.dto.UserNotifyMessage;
import com.xiaou.sse.mapper.UserNotifyMessageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SseMessageManager {

    @Resource
    private UserNotifyMessageMapper userNotifyMessageMapper;

    @Resource
    private SseEmitterManager sseEmitterManager;

    /**
     * 拉取当前用户的未推送消息并立即推送
     */
    public void pullAndPushUnreadMessages(String userId) {
        List<UserNotifyMessage> messageList = userNotifyMessageMapper.selectList(
                new LambdaQueryWrapper<UserNotifyMessage>()
                        .eq(UserNotifyMessage::getUserId, userId)
                        .eq(UserNotifyMessage::getIsPush, GlobalConstants.ZERO)
        );

        if (!messageList.isEmpty()) {
            for (UserNotifyMessage message : messageList) {
                // 构造 SSE 消息 DTO
                SseMessageDto dto = new SseMessageDto();
                dto.setUserIds(List.of(userId)); // 精准推送给自己
                dto.setMessage(message.getContent());
                dto.setType(message.getType());

                // 推送消息
                sseEmitterManager.publishMessage(dto);

                // 更新为已推送
                message.setIsPush(GlobalConstants.ONE);
                userNotifyMessageMapper.updateById(message);
            }
        }
    }
}
