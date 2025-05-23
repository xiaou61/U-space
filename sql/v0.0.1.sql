-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `u-space` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 使用该数据库
USE `u-space`;

-- 创建管理员用户表
CREATE TABLE `admin_user` (
                              `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                              `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                              `password` VARCHAR(100) NOT NULL COMMENT '密码'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户表';


DROP TABLE IF EXISTS u_college;

CREATE TABLE u_college (
                           college_id INT NOT NULL AUTO_INCREMENT COMMENT '学院编号（主键）',
                           name VARCHAR(255) NOT NULL COMMENT '学院名称',
                           dean VARCHAR(100) DEFAULT NULL COMMENT '院长姓名',
                           phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
                           email VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
                           campus_name VARCHAR(255) NOT NULL COMMENT '所属校区名称',
                           created_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
                           created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           updated_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
                           updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (college_id),
                           KEY idx_name (name),
                           KEY idx_campus_name (campus_name)
) COMMENT='学院信息表';
