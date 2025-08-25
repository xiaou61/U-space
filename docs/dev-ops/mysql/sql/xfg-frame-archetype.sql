/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 50639
 Source Host           : localhost:3306
 Source Schema         : road-map

 Target Server Type    : MySQL
 Target Server Version : 50639
 File Encoding         : 65001

 Date: 15/07/2023 09:26:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE database if NOT EXISTS `xfg_frame_archetype` default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use `xfg_frame_archetype`;

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `employee_number` varchar(16) NOT NULL DEFAULT '' COMMENT '雇员ID',
  `employee_name` varchar(32) NOT NULL DEFAULT '' COMMENT '雇员姓名',
  `employee_level` varchar(8) NOT NULL DEFAULT '' COMMENT '雇员级别',
  `employee_title` varchar(16) NOT NULL DEFAULT '' COMMENT '雇员岗位Title',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_employee_number` (`employee_number`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of employee
-- ----------------------------
BEGIN;
INSERT INTO `employee` VALUES (1, '10000001', 'sXvfDpsWnJdLsCVk64tJgw==', 'T-3', '中级工程师', '2023-07-14 15:26:26', '2023-07-14 15:26:26');
INSERT INTO `employee` VALUES (2, '10000010', 'sXvfDpsWnJdLsCVk64tJgw==', 'T2', '见习工程师', '2023-07-14 15:34:40', '2023-07-14 15:34:40');
INSERT INTO `employee` VALUES (3, '10000011', 'sXvfDpsWnJdLsCVk64tJgw==', 'T2', '见习工程师', '2023-07-14 15:34:40', '2023-07-14 15:34:40');
INSERT INTO `employee` VALUES (4, '10000012', 'sXvfDpsWnJdLsCVk64tJgw==', 'T2', '见习工程师', '2023-07-14 15:34:40', '2023-07-14 15:34:40');
INSERT INTO `employee` VALUES (5, '10000013', 'sXvfDpsWnJdLsCVk64tJgw==', 'T2', '见习工程师', '2023-07-14 15:34:40', '2023-07-14 15:34:40');
INSERT INTO `employee` VALUES (6, '10000014', 'sXvfDpsWnJdLsCVk64tJgw==', 'T2', '见习工程师', '2023-07-14 15:34:40', '2023-07-14 15:34:40');
INSERT INTO `employee` VALUES (9, '10000002', 'sXvfDpsWnJdLsCVk64tJgw==', 'T2', '见习工程师', '2023-07-15 07:42:52', '2023-07-15 07:42:52');
INSERT INTO `employee` VALUES (22, '10000015', 'hMCgLG6WV3CsNBQ1UD6PEQ==', 'T2', '见习工程师', '2023-07-15 08:02:31', '2023-07-15 08:02:31');
INSERT INTO `employee` VALUES (23, '10000016', 'hMCgLG6WV3CsNBQ1UD6PEQ==', 'T2', '见习工程师', '2023-07-15 08:02:31', '2023-07-15 08:02:31');
INSERT INTO `employee` VALUES (24, '10000017', 'hMCgLG6WV3CsNBQ1UD6PEQ==', 'T2', '见习工程师', '2023-07-15 08:02:31', '2023-07-15 08:02:31');
INSERT INTO `employee` VALUES (39, '10000022', 'GyG+V0r6mBCNsdusuKl03g==', 'T1', '实习工程师', '2023-07-15 09:17:49', '2023-07-15 09:17:49');
COMMIT;

-- ----------------------------
-- Table structure for employee_salary
-- ----------------------------
DROP TABLE IF EXISTS `employee_salary`;
CREATE TABLE `employee_salary` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `employee_number` varchar(16) NOT NULL DEFAULT '' COMMENT '雇员编号',
  `salary_total_amount` decimal(8,2) NOT NULL COMMENT '薪资总额',
  `salary_merit_amount` decimal(8,2) NOT NULL COMMENT '绩效工资',
  `salary_base_amount` decimal(8,2) NOT NULL COMMENT '基础工资',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_number` (`employee_number`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of employee_salary
-- ----------------------------
BEGIN;
INSERT INTO `employee_salary` VALUES (1, '10000001', 5100.00, 1020.00, 4080.00, '2023-07-14 16:09:06', '2023-07-14 16:09:06');
INSERT INTO `employee_salary` VALUES (2, '10000010', 5000.00, 1000.00, 4000.00, '2023-07-14 16:17:10', '2023-07-14 16:17:10');
INSERT INTO `employee_salary` VALUES (3, '10000011', 5000.00, 1000.00, 4000.00, '2023-07-14 16:17:10', '2023-07-14 16:17:10');
INSERT INTO `employee_salary` VALUES (4, '10000012', 5000.00, 1000.00, 4000.00, '2023-07-14 16:17:10', '2023-07-14 16:17:10');
INSERT INTO `employee_salary` VALUES (5, '10000013', 5000.00, 1000.00, 4000.00, '2023-07-14 16:17:10', '2023-07-14 16:17:10');
INSERT INTO `employee_salary` VALUES (6, '10000014', 5000.00, 1000.00, 4000.00, '2023-07-14 16:17:10', '2023-07-14 16:17:10');
INSERT INTO `employee_salary` VALUES (8, '10000022', 100.00, 10.00, 90.00, '2023-07-15 09:17:49', '2023-07-15 09:17:49');
COMMIT;

-- ----------------------------
-- Table structure for employee_salary_adjust
-- ----------------------------
DROP TABLE IF EXISTS `employee_salary_adjust`;
CREATE TABLE `employee_salary_adjust` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `employee_number` varchar(16) NOT NULL DEFAULT '' COMMENT '雇员编号',
  `adjust_order_id` varchar(32) NOT NULL DEFAULT '' COMMENT '调薪单号',
  `adjust_total_amount` decimal(8,2) NOT NULL COMMENT '总额调薪',
  `adjust_base_amount` decimal(8,2) NOT NULL COMMENT '基础调薪',
  `adjust_merit_amount` decimal(8,2) NOT NULL COMMENT '绩效调薪',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_id` (`adjust_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of employee_salary_adjust
-- ----------------------------
BEGIN;
INSERT INTO `employee_salary_adjust` VALUES (1, '10000001', '109089990198888811', 1000.00, 800.00, 200.00, '2023-07-14 16:55:53', '2023-07-14 16:55:53');
INSERT INTO `employee_salary_adjust` VALUES (2, '10000001', '100908977676001', 100.00, 20.00, 80.00, '2023-07-14 21:57:39', '2023-07-14 21:57:39');
COMMIT;