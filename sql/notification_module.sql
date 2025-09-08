
-- 消息通知主表
CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    type VARCHAR(50) DEFAULT 'PERSONAL' COMMENT '消息类型：PERSONAL(个人消息)/ANNOUNCEMENT(系统公告)/COMMUNITY_INTERACTION(社区互动)/SYSTEM(系统通知)',
    priority VARCHAR(20) DEFAULT 'LOW' COMMENT '优先级：HIGH(高)/MEDIUM(中)/LOW(低)',
    sender_id BIGINT DEFAULT 0 COMMENT '发送者ID（系统消息为0）',
    receiver_id BIGINT COMMENT '接收者ID（为NULL表示全站公告）',
    source_module VARCHAR(50) COMMENT '来源模块：community/interview/system等',
    source_id VARCHAR(100) COMMENT '来源数据ID，如帖子ID、题目ID等',
    status VARCHAR(20) DEFAULT 'UNREAD' COMMENT '状态：UNREAD(未读)/read(已读)/DELETED(已删除)',
    read_time DATETIME COMMENT '阅读时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_receiver_status (receiver_id, status),
    INDEX idx_created_time (created_time),
    INDEX idx_type (type),
    INDEX idx_priority (priority),
    INDEX idx_source (source_module, source_id)
) COMMENT='消息通知表';

-- 消息模板表（可选扩展功能）
CREATE TABLE notification_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    code VARCHAR(50) NOT NULL COMMENT '模板代码',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    title_template VARCHAR(200) NOT NULL COMMENT '标题模板',
    content_template TEXT NOT NULL COMMENT '内容模板',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用：0(禁用)/1(启用)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_code (code),
    INDEX idx_enabled (is_enabled)
) COMMENT='消息模板表';

-- 初始化消息模板数据
INSERT INTO notification_template (code, name, title_template, content_template) VALUES
('WELCOME', '欢迎消息', '欢迎加入{platform}', '亲爱的{username}，欢迎加入我们的平台！'),
('COMMUNITY_LIKE', '帖子点赞', '您的帖子收到点赞', '您的帖子《{postTitle}》收到了{likerName}的点赞'),
('COMMUNITY_COMMENT', '帖子评论', '您的帖子收到评论', '您的帖子《{postTitle}》收到了{commenterName}的评论'),
('INTERVIEW_FAVORITE', '面试题收藏', '收藏提醒', '您收藏的面试题《{questionTitle}》已更新'),
('SYSTEM_MAINTENANCE', '系统维护', '系统维护通知', '系统将于{maintenanceTime}进行维护，预计耗时{duration}');

-- 消息配置表（用户消息接收设置）
CREATE TABLE notification_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '消息类型：PERSONAL/ANNOUNCEMENT/COMMUNITY_INTERACTION/SYSTEM等',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用接收：0(禁用)/1(启用)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_user_type (user_id, type),
    INDEX idx_user_enabled (user_id, is_enabled)
) COMMENT='消息配置表';

-- 创建索引优化查询性能
CREATE INDEX idx_notification_user_unread ON notification (receiver_id, status, created_time DESC);
CREATE INDEX idx_notification_announcement ON notification (receiver_id, type, created_time DESC); 