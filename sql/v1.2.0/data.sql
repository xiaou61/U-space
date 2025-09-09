/*
 Navicat Premium Data Transfer

 Source Server         : 本机的
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : code_nest

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 03/09/2025 13:33:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
-- Records of sys_admin_role
-- ----------------------------
INSERT INTO `sys_admin_role` VALUES (1, 1, 1, '2025-08-31 12:11:46', 1);
INSERT INTO `sys_admin_role` VALUES (2, 2, 2, '2025-08-31 12:11:46', 1);


-- ----------------------------
-- Records of sys_admin
-- ----------------------------
INSERT INTO `sys_admin` VALUES (1, 'admin', '$2a$10$w9fmcYcV69fM7.01Ev6N8OwlUeShcb8Zc1B0uDNc6Q4s0z8CzvQVi', '超级管理员', 'admin@code-nest.com', '13800138000', NULL, 1, 0, '2025-09-03 11:17:28', '127.0.0.1', 11, '系统内置超级管理员账户', '2025-08-31 12:11:46', '2025-09-03 11:17:27', 1, NULL);
INSERT INTO `sys_admin` VALUES (2, 'system', '$2a$10$w9fmcYcV69fM7.01Ev6N8OwlUeShcb8Zc1B0uDNc6Q4s0z8CzvQVi', '系统管理员', 'system@code-nest.com', '13800138001', NULL, 1, 0, NULL, NULL, 0, '系统内置系统管理员账户', '2025-08-31 12:11:46', '2025-08-31 12:28:30', 1, NULL);


-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 0, 1, '2025-08-31 12:11:46', '2025-08-31 12:11:46', NULL, NULL);
INSERT INTO `sys_role` VALUES (2, '系统管理员', 'SYSTEM_ADMIN', '系统管理员，负责系统配置和用户管理', 0, 2, '2025-08-31 12:11:46', '2025-08-31 12:11:46', NULL, NULL);
INSERT INTO `sys_role` VALUES (3, '普通管理员', 'ADMIN', '普通管理员，负责日常业务管理', 0, 3, '2025-08-31 12:11:46', '2025-08-31 12:11:46', NULL, NULL);


-- 插入默认本地存储配置
INSERT INTO `storage_config` (`storage_type`, `config_name`, `config_params`, `is_default`, `is_enabled`, `test_status`) VALUES
    ('LOCAL', '本地存储', '{"basePath":"/uploads","urlPrefix":"http://localhost:9999/files"}', 1, 1, 1);

-- 插入默认系统设置
INSERT INTO `file_system_setting` (`setting_key`, `setting_value`, `setting_desc`) VALUES
                                                                                       ('MAX_FILE_SIZE', '104857600', '最大文件大小限制(字节) - 100MB'),
                                                                                       ('ALLOWED_FILE_TYPES', '["jpg","jpeg","png","gif","bmp","webp","pdf","doc","docx","xls","xlsx","ppt","pptx","txt","zip","rar","7z","mp4","avi","mov","mp3","wav"]', '允许上传的文件类型白名单'),
                                                                                       ('AUTO_BACKUP_ENABLED', 'true', '是否自动创建本地备份'),
                                                                                       ('TEMP_LINK_EXPIRE_HOURS', '24', '临时链接有效期(小时)'),
                                                                                       ('STORAGE_QUOTA_PER_MODULE', '10737418240', '每个模块存储配额(字节) - 10GB');



-- 初始化消息模板数据
INSERT INTO notification_template (code, name, title_template, content_template) VALUES
                                                                                     ('WELCOME', '欢迎消息', '欢迎加入{platform}', '亲爱的{username}，欢迎加入我们的平台！'),
                                                                                     ('COMMUNITY_LIKE', '帖子点赞', '您的帖子收到点赞', '您的帖子《{postTitle}》收到了{likerName}的点赞'),
                                                                                     ('COMMUNITY_COMMENT', '帖子评论', '您的帖子收到评论', '您的帖子《{postTitle}》收到了{commenterName}的评论'),
                                                                                     ('INTERVIEW_FAVORITE', '面试题收藏', '收藏提醒', '您收藏的面试题《{questionTitle}》已更新'),
                                                                                     ('SYSTEM_MAINTENANCE', '系统维护', '系统维护通知', '系统将于{maintenanceTime}进行维护，预计耗时{duration}');


SET FOREIGN_KEY_CHECKS = 1;
