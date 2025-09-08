package com.xiaou.notification.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.domain.Notification;
import com.xiaou.common.domain.NotificationTemplate;
import com.xiaou.common.enums.NotificationTypeEnum;
import com.xiaou.common.mapper.NotificationMapper;
import com.xiaou.common.mapper.NotificationTemplateMapper;
import com.xiaou.common.service.NotificationService;
import com.xiaou.common.utils.NotificationUtil;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.notification.dto.BatchSendRequest;
import com.xiaou.notification.dto.NotificationQueryRequest;
import com.xiaou.notification.dto.NotificationStatistics;
import com.xiaou.notification.dto.StatisticsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端消息通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAdminService {
    
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final NotificationTemplateMapper notificationTemplateMapper;
    
    /**
     * 获取消息统计信息
     */
    public NotificationStatistics getStatistics(StatisticsRequest request) {
        NotificationStatistics statistics = new NotificationStatistics();
        
        try {
            // 如果有时间范围参数，使用时间范围统计
            if (request.getStartTime() != null && request.getEndTime() != null) {
                // 按时间范围统计各类型消息
                statistics.setAnnouncementCount(
                    notificationMapper.countByTimeRangeAndType(request.getStartTime(), request.getEndTime(), NotificationTypeEnum.ANNOUNCEMENT.getCode()));
                statistics.setPersonalCount(
                    notificationMapper.countByTimeRangeAndType(request.getStartTime(), request.getEndTime(), NotificationTypeEnum.PERSONAL.getCode()));
                statistics.setCommunityCount(
                    notificationMapper.countByTimeRangeAndType(request.getStartTime(), request.getEndTime(), NotificationTypeEnum.COMMUNITY_INTERACTION.getCode()));
                statistics.setSystemCount(
                    notificationMapper.countByTimeRangeAndType(request.getStartTime(), request.getEndTime(), NotificationTypeEnum.SYSTEM.getCode()));
                
                // 时间范围查询时，todayTotal和monthTotal置为0，因为失去了原有含义
                statistics.setTodayTotal(0L);
                statistics.setMonthTotal(0L);
            } else {
                // 默认统计
                // 今日发送总数
                Long todayTotal = notificationMapper.countTodayMessages();
                statistics.setTodayTotal(todayTotal != null ? todayTotal : 0L);
                
                // 本月发送总数  
                Long monthTotal = notificationMapper.countMonthMessages();
                statistics.setMonthTotal(monthTotal != null ? monthTotal : 0L);
                
                // 按类型统计
                Long announcementCount = notificationMapper.countByType(NotificationTypeEnum.ANNOUNCEMENT.getCode());
                statistics.setAnnouncementCount(announcementCount != null ? announcementCount : 0L);
                
                Long personalCount = notificationMapper.countByType(NotificationTypeEnum.PERSONAL.getCode());
                statistics.setPersonalCount(personalCount != null ? personalCount : 0L);
                
                Long communityCount = notificationMapper.countByType(NotificationTypeEnum.COMMUNITY_INTERACTION.getCode());
                statistics.setCommunityCount(communityCount != null ? communityCount : 0L);
                
                Long systemCount = notificationMapper.countByType(NotificationTypeEnum.SYSTEM.getCode());
                statistics.setSystemCount(systemCount != null ? systemCount : 0L);
            }
            
            // 全部未读消息数量（不受时间范围限制）
            Long unreadTotal = notificationMapper.countAllUnreadMessages();
            statistics.setUnreadTotal(unreadTotal != null ? unreadTotal : 0L);
            
            log.info("获取消息统计信息: {}, 查询参数: {}", statistics, request);
        } catch (Exception e) {
            log.error("获取消息统计信息失败", e);
            // 异常时返回空统计对象，避免前端报错
            statistics = new NotificationStatistics();
        }
        
        return statistics;
    }
    
    /**
     * 管理端获取所有消息列表
     */
    public PageResult<Notification> getAllMessageList(NotificationQueryRequest request) {
        // 使用PageHelper分页插件
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            // 管理端查询所有用户的消息，不限制用户ID（传null）
            return notificationMapper.selectByUserId(
                null, request.getStatus(), request.getType()
            );
        });
    }
    
    /**
     * 批量发送个人消息
     */
    public boolean batchSendMessage(BatchSendRequest request) {
        try {
            NotificationUtil.sendBatchMessage(
                request.getReceiverIds(), 
                request.getTitle(), 
                request.getContent()
            );
            log.info("管理员批量发送消息成功，接收者数量: {}", request.getReceiverIds().size());
            return true;
        } catch (Exception e) {
            log.error("管理员批量发送消息失败", e);
            return false;
        }
    }
    
    /**
     * 删除消息（管理员权限）
     */
    public boolean deleteMessage(Long messageId) {
        try {
            // 管理员可以删除任何消息，这里需要直接操作数据库
            // 由于NotificationService中的deleteMessage方法需要用户ID验证
            // 我们需要直接操作Mapper
            Notification notification = notificationMapper.selectById(messageId);
            if (notification != null) {
                // 执行硬删除或软删除
                int result = notificationMapper.deleteMessage(messageId, notification.getReceiverId());
                return result > 0;
            }
            return false;
        } catch (Exception e) {
            log.error("管理员删除消息失败, messageId: {}", messageId, e);
            return false;
        }
    }
    
    /**
     * 获取所有模板列表
     */
    public List<NotificationTemplate> getAllTemplates() {
        try {
            return notificationTemplateMapper.selectEnabledTemplates();
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return List.of();
        }
    }
    
    /**
     * 创建消息模板
     */
    public boolean createTemplate(NotificationTemplate template) {
        try {
            int result = notificationTemplateMapper.insert(template);
            log.info("创建消息模板成功: {}", template);
            return result > 0;
        } catch (Exception e) {
            log.error("创建消息模板失败: {}", template, e);
            return false;
        }
    }
    
    /**
     * 更新消息模板
     */
    public boolean updateTemplate(NotificationTemplate template) {
        try {
            int result = notificationTemplateMapper.update(template);
            log.info("更新消息模板成功: {}", template);
            return result > 0;
        } catch (Exception e) {
            log.error("更新消息模板失败: {}", template, e);
            return false;
        }
    }
    
    /**
     * 删除消息模板
     */
    public boolean deleteTemplate(Long templateId) {
        try {
            int result = notificationTemplateMapper.deleteById(templateId);
            log.info("删除消息模板成功, templateId: {}", templateId);
            return result > 0;
        } catch (Exception e) {
            log.error("删除消息模板失败, templateId: {}", templateId, e);
            return false;
        }
    }
} 