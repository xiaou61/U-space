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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class NoticeConsumer {
    @Resource
    private SseEmitterManager emitterManager;
    @Resource
    private UserNotifyMessageMapper userNotifyMessageMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @RabbitListener(queues = RabbitMQConfig.NOTICE_QUEUE)
    public void handleNoticeMessage(SseMessageDto msg) {
        if (msg.getType() == null) {
            log.warn("收到消息type为空，消息内容: {}", msg);
            // 设置默认值
            msg.setType(GlobalConstants.NOKNOWN);
        }

        // 如果userids为null 那么需要保存所有的
        if (msg.getUserIds() == null) {
            // 获取所有有效用户ID（群发消息）
            List<String> allUserIds = getAllValidUserIds();
            log.info("群发消息给 {} 个用户", allUserIds.size());
            for (String userId : allUserIds) {
                extracted(msg, userId);
            }
        } else {
            log.info("定向发送消息给指定用户");
            // 保存到数据库信息 可能会有多个用户的推送所以要保存多个用户
            for (String userId : msg.getUserIds()) {
                extracted(msg, userId);
            }
        }

    }

    /**
     * 获取所有有效用户ID
     * 查询所有已注册且状态正常的学生用户ID
     */
    private List<String> getAllValidUserIds() {
        try {
            // 查询所有状态为1的学生用户ID（只查询ID字段，提高性能）
            String sql = "SELECT id FROM u_student WHERE status = 1 AND id IS NOT NULL";
            List<String> userIds = jdbcTemplate.queryForList(sql, String.class);
            log.info("从数据库获取到 {} 个有效用户", userIds.size());

            // 如果数据库中没有用户，使用Redis作为后备
            if (userIds.isEmpty()) {
                log.warn("数据库中没有有效用户，使用Redis在线用户列表");
                return getRedisOnlineUsers();
            }

            return userIds;
        } catch (Exception e) {
            log.error("数据库查询失败，使用Redis在线用户列表作为后备方案: {}", e.getMessage());
            return getRedisOnlineUsers();
        }
    }

    /**
     * 获取Redis中的在线用户列表（后备方案）
     */
    private List<String> getRedisOnlineUsers() {
        try {
            List<String> cacheList = RedisUtils.getCacheList(GlobalConstants.USER_ONLINE_KEY);
            if (cacheList != null && !cacheList.isEmpty()) {
                log.info("使用Redis在线用户列表，共 {} 个用户", cacheList.size());
                return cacheList;
            } else {
                log.warn("Redis在线用户列表为空");
                return new ArrayList<>();
            }
        } catch (Exception redisException) {
            log.error("Redis在线用户列表读取失败: {}", redisException.getMessage());
            return new ArrayList<>();
        }
    }

    private void extracted(SseMessageDto msg, String userId) {
        UserNotifyMessage userNotifyMessage = new UserNotifyMessage();
        userNotifyMessage.setUserId(userId);
        userNotifyMessage.setContent(msg.getMessage());
        // 在线的话设置为已推送 在线为1 不在线为0
        userNotifyMessage.setIsPush(emitterManager.isUserOnline(userId) ? GlobalConstants.ONE : GlobalConstants.ZERO);
        userNotifyMessage.setType(msg.getType());
        userNotifyMessageMapper.insert(userNotifyMessage);
    }
}
