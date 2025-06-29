-- 创建管理员用户表
CREATE TABLE `admin_user` (
                              `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID（UUID）',
                              `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                              `password` VARCHAR(100) NOT NULL COMMENT '密码'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='管理员用户表（UUID主键）';


CREATE TABLE `user_role` (
                             `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID（UUID）',
                             `role` VARCHAR(50) NOT NULL COMMENT '角色名称'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户角色表（UUID主键）';


CREATE TABLE `u_class` (
                           `id` VARCHAR(32) NOT NULL COMMENT '主键ID（UUID）',
                           `class_name` VARCHAR(100) NOT NULL COMMENT '班级名称',
                           `grade` VARCHAR(50) NOT NULL COMMENT '年级，2025级',
                           `major` VARCHAR(100) DEFAULT NULL COMMENT '所属专业，例如：计算机科学与技术',
                           `class_teacher` VARCHAR(100) DEFAULT NULL COMMENT '班主任姓名',
                           `student_count` INT UNSIGNED DEFAULT 0 COMMENT '学生人数',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';

CREATE TABLE `u_teacher` (
                           `id` VARCHAR(32) NOT NULL COMMENT '主键UUID，32位无连字符',
                           `employee_no` CHAR(8) NOT NULL COMMENT '教职工编号（8位数字字符串）',
                           `name` VARCHAR(100) NOT NULL COMMENT '教师姓名',
                           `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                           `password` VARCHAR(64) NOT NULL COMMENT '密码，默认手机号后6位',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uniq_employee_no` (`employee_no`),
                           UNIQUE KEY `uniq_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

