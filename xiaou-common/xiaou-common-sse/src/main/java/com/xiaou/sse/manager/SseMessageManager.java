package com.xiaou.sse.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.sse.core.SseEmitterManager;
import com.xiaou.sse.dto.SseMessageDto;
import com.xiaou.sse.dto.UserNotifyMessage;
import com.xiaou.sse.mapper.UserNotifyMessageMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SseMessageManager {
    @Resource
    private UserNotifyMessageMapper userNotifyMessageMapper;

    @Resource
    private SseEmitterManager sseEmitterManager;
    @Resource
    private LoginHelper loginHelper;



    /**
     * 拉取未推送消息并立即推送
     */
    public void pullAndPushUnreadMessages(String userId) {
        if("ALL".equals(userId)){
            List<UserNotifyMessage> messageList = userNotifyMessageMapper.selectList(
                    new LambdaQueryWrapper<UserNotifyMessage>()
                            .eq(UserNotifyMessage::getIsPush, GlobalConstants.ZERO)
            );
            extracted(messageList);
        }else {
            List<UserNotifyMessage> messageList = userNotifyMessageMapper.selectList(
                    new LambdaQueryWrapper<UserNotifyMessage>()
                            .eq(UserNotifyMessage::getUserId, userId)
                            .eq(UserNotifyMessage::getIsPush, GlobalConstants.ZERO)
            );
            extracted(messageList);
        }



    }

    private void extracted(List<UserNotifyMessage> messageList) {
        for (UserNotifyMessage message : messageList) {
            // 构造 DTO
            SseMessageDto dto = new SseMessageDto();
            //这里为本人id 因为这个接口一定是本人进行请求的
            dto.setUserIds(List.of(loginHelper.getCurrentAppUserId()));
            dto.setMessage(message.getContent());

            // 推送
            sseEmitterManager.publishMessage(dto);

            // 更新状态为已推送（可批量更新）
            message.setIsPush(GlobalConstants.ONE);
            userNotifyMessageMapper.updateById(message);
        }
    }


}
