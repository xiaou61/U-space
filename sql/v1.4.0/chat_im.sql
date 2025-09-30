-- IM聊天室模块数据库表结构
-- v1.4.0

-- 1. 聊天室表
CREATE TABLE chat_rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '聊天室ID',
    room_name VARCHAR(100) NOT NULL COMMENT '聊天室名称',
    room_type TINYINT DEFAULT 1 COMMENT '类型：1官方群组',
    description VARCHAR(500) COMMENT '聊天室描述',
    max_users INT DEFAULT 0 COMMENT '最大人数限制，0表示不限制',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室表';

-- 2. 聊天消息表
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    room_id BIGINT NOT NULL COMMENT '聊天室ID',
    user_id BIGINT NOT NULL COMMENT '发送者用户ID',
    message_type TINYINT NOT NULL COMMENT '消息类型：1文本 2图片 3系统消息',
    content TEXT NOT NULL COMMENT '消息内容',
    image_url VARCHAR(500) COMMENT '图片URL（消息类型为图片时）',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否 1是',
    ip_address VARCHAR(50) COMMENT '发送者IP地址',
    device_info VARCHAR(200) COMMENT '设备信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_room_id (room_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time DESC),
    INDEX idx_room_time (room_id, create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 3. 用户禁言表
CREATE TABLE chat_user_bans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '禁言记录ID',
    user_id BIGINT NOT NULL COMMENT '被禁言用户ID',
    room_id BIGINT NOT NULL COMMENT '聊天室ID',
    ban_reason VARCHAR(500) COMMENT '禁言原因',
    ban_start_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '禁言开始时间',
    ban_end_time DATETIME COMMENT '禁言结束时间，NULL表示永久',
    operator_id BIGINT COMMENT '操作人ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1生效中 0已解除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_room_id (room_id),
    INDEX idx_status (status),
    INDEX idx_end_time (ban_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户禁言表';

-- 4. 在线用户表
CREATE TABLE chat_online_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT NOT NULL COMMENT '聊天室ID',
    session_id VARCHAR(100) NOT NULL COMMENT 'WebSocket会话ID',
    ip_address VARCHAR(50) COMMENT '用户IP地址',
    device_info VARCHAR(200) COMMENT '设备信息',
    connect_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '连接时间',
    last_heartbeat_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后心跳时间',
    
    UNIQUE KEY uk_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_room_id (room_id),
    INDEX idx_heartbeat (last_heartbeat_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在线用户表';

-- 初始化官方聊天室
INSERT INTO chat_rooms (room_name, room_type, description, status) 
VALUES ('Code-Nest官方群组', 1, '欢迎来到Code-Nest官方聊天室，大家可以在这里自由交流！', 1);
