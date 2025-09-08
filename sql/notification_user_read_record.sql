-- 用户通知阅读记录表
-- 解决系统公告已读状态问题：为每个用户记录对每条公告的阅读状态
CREATE TABLE notification_user_read_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    notification_id BIGINT NOT NULL COMMENT '通知ID',
    read_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_user_notification (user_id, notification_id),
    INDEX idx_user_id (user_id),
    INDEX idx_notification_id (notification_id),
    INDEX idx_read_time (read_time)
) COMMENT='用户通知阅读记录表';

-- 创建获取用户未读消息的视图（优化查询性能）
CREATE VIEW v_user_unread_notifications AS
SELECT 
    n.id,
    n.title,
    n.content,
    n.type,
    n.priority,
    n.sender_id,
    n.receiver_id,
    n.source_module,
    n.source_id,
    n.status,
    n.read_time,
    n.created_time,
    n.updated_time
FROM notification n
LEFT JOIN notification_user_read_record r ON n.id = r.notification_id
WHERE n.status != 'DELETED'
  AND (
    -- 个人消息：直接检查status
    (n.receiver_id IS NOT NULL AND n.status = 'UNREAD')
    OR
    -- 系统公告：检查用户是否有阅读记录
    (n.receiver_id IS NULL AND r.id IS NULL)
  ); 