-- 操作日志表
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operation_id` VARCHAR(64) NOT NULL COMMENT '操作ID',
  `module` VARCHAR(50) DEFAULT '' COMMENT '操作模块',
  `operation_type` VARCHAR(20) DEFAULT 'OTHER' COMMENT '操作类型',
  `description` VARCHAR(500) DEFAULT '' COMMENT '操作描述',
  `method` VARCHAR(200) DEFAULT '' COMMENT '请求方法',
  `request_uri` VARCHAR(500) DEFAULT '' COMMENT '请求URI',
  `request_method` VARCHAR(10) DEFAULT 'GET' COMMENT 'HTTP请求方法',
  `request_params` TEXT COMMENT '请求参数',
  `response_data` TEXT COMMENT '响应数据',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT '' COMMENT '操作人姓名',
  `operator_ip` VARCHAR(128) DEFAULT '' COMMENT '操作IP',
  `operation_location` VARCHAR(255) DEFAULT '' COMMENT '操作地点',
  `browser` VARCHAR(50) DEFAULT '' COMMENT '浏览器类型',
  `os` VARCHAR(50) DEFAULT '' COMMENT '操作系统',
  `status` TINYINT(1) DEFAULT 0 COMMENT '操作状态：0-成功，1-失败',
  `error_msg` VARCHAR(2000) DEFAULT '' COMMENT '错误消息',
  `operation_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `cost_time` BIGINT DEFAULT 0 COMMENT '耗时（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_operation_id` (`operation_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_module` (`module`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operation_time` (`operation_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 插入一些示例数据
INSERT INTO `sys_operation_log` 
(`operation_id`, `module`, `operation_type`, `description`, `method`, `request_uri`, `request_method`, 
 `operator_id`, `operator_name`, `operator_ip`, `operation_location`, `browser`, `os`, `status`, `operation_time`, `cost_time`) 
VALUES 
('OP001', '用户管理', 'UPDATE', '更新个人信息', 'updateProfile', '/auth/profile', 'PUT', 
 1, 'admin', '127.0.0.1', '本地', 'Chrome', 'Windows 10', 0, NOW(), 125),
('OP002', '用户管理', 'UPDATE', '修改密码', 'changePassword', '/auth/password', 'PUT', 
 1, 'admin', '127.0.0.1', '本地', 'Chrome', 'Windows 10', 0, NOW(), 89),
('OP003', '日志管理', 'SELECT', '查询登录日志', 'getLoginLogs', '/auth/login-logs', 'GET', 
 1, 'admin', '127.0.0.1', '本地', 'Chrome', 'Windows 10', 0, NOW(), 45),
('OP004', '日志管理', 'DELETE', '清空登录日志', 'clearLoginLogs', '/auth/login-logs', 'DELETE', 
 1, 'admin', '127.0.0.1', '本地', 'Chrome', 'Windows 10', 0, NOW(), 234); 