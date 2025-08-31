-- ===========================
-- xiaou-user 模块数据表设计
-- ===========================

USE `code_nest`;

-- 普通用户表
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密后）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-正常，1-禁用，2-删除',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_register_time` (`register_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 初始化测试用户数据
INSERT INTO `user_info` (`id`, `username`, `password`, `nickname`, `real_name`, `email`, `phone`, `gender`, `status`, `remark`, `create_by`) VALUES
(1, 'testuser', '$2a$10$7JB720yubVSIOUQEb8tlCe5bwvJQpJk5E3KtZLOhJQBFgF8LDfN.6', '测试用户', '张三', 'test@example.com', '13912345678', 1, 0, '系统测试用户', 1),
(2, 'user001', '$2a$10$7JB720yubVSIOUQEb8tlCe5bwvJQpJk5E3KtZLOhJQBFgF8LDfN.6', '用户001', '李四', 'user001@example.com', '13912345679', 2, 0, '普通用户账户', 1); 