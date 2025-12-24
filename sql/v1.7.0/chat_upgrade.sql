-- 聊天室模块升级SQL
-- v1.7.0 - 消息引用/回复功能支持

-- 1. 为消息表添加回复相关字段
ALTER TABLE chat_messages 
ADD COLUMN reply_to_id BIGINT COMMENT '回复的消息ID' AFTER image_url,
ADD COLUMN reply_to_content VARCHAR(200) COMMENT '被回复消息内容摘要' AFTER reply_to_id,
ADD COLUMN reply_to_user VARCHAR(50) COMMENT '被回复者昵称' AFTER reply_to_content,
ADD COLUMN mentions VARCHAR(500) COMMENT '@提及的用户ID,逗号分隔' AFTER reply_to_user;

-- 2. 为回复消息添加索引
ALTER TABLE chat_messages ADD INDEX idx_reply_to_id (reply_to_id);

-- 3. 创建敏感词表
CREATE TABLE IF NOT EXISTS chat_sensitive_word (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '敏感词ID',
    word VARCHAR(100) NOT NULL COMMENT '敏感词',
    category VARCHAR(50) DEFAULT 'default' COMMENT '分类',
    match_type TINYINT DEFAULT 1 COMMENT '匹配类型:1精确,2模糊',
    status TINYINT DEFAULT 1 COMMENT '状态:0禁用,1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_word (word),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- 4. 插入一些默认敏感词示例（可根据需要调整）
INSERT INTO chat_sensitive_word (word, category, match_type, status) VALUES
('广告', 'spam', 1, 1),
('代理', 'spam', 1, 1),
('赌博', 'illegal', 1, 1);


