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

SET FOREIGN_KEY_CHECKS = 1;
