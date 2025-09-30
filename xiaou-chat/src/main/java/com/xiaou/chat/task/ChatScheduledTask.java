package com.xiaou.chat.task;

import com.xiaou.chat.service.ChatOnlineUserService;
import com.xiaou.chat.service.ChatUserBanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 聊天室定时任务
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatScheduledTask {
    
    private final ChatUserBanService chatUserBanService;
    private final ChatOnlineUserService chatOnlineUserService;
    
    /**
     * 自动解除过期禁言
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void autoUnbanExpired() {
        try {
            chatUserBanService.autoUnbanExpired();
        } catch (Exception e) {
            log.error("自动解除过期禁言任务执行失败", e);
        }
    }
    
    /**
     * 清理超时的在线用户
     * 每30秒执行一次
     */
    @Scheduled(cron = "*/30 * * * * ?")
    public void cleanTimeoutUsers() {
        try {
            chatOnlineUserService.cleanTimeoutUsers();
        } catch (Exception e) {
            log.error("清理超时用户任务执行失败", e);
        }
    }
}
