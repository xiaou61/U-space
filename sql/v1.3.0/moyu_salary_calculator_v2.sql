-- 摸鱼工具模块 - 时薪计算器相关表结构 v2

-- 用户薪资配置表
DROP TABLE IF EXISTS `user_salary_config`;
CREATE TABLE `user_salary_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `monthly_salary` decimal(10,2) NOT NULL COMMENT '月薪（元）',
    `work_days_per_month` tinyint NOT NULL DEFAULT 22 COMMENT '每月工作天数',
    `work_hours_per_day` decimal(4,2) NOT NULL DEFAULT 8.00 COMMENT '每日工作小时数',
    `hourly_rate` decimal(10,2) GENERATED ALWAYS AS (`monthly_salary` / (`work_days_per_month` * `work_hours_per_day`)) STORED COMMENT '时薪（自动计算）',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_id` (`user_id`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    CONSTRAINT `fk_user_salary_config_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户薪资配置表';

-- 工作记录表 (增强版，支持暂停功能)
DROP TABLE IF EXISTS `work_record`;
CREATE TABLE `work_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `work_date` date NOT NULL COMMENT '工作日期',
    `work_hours` decimal(4,2) NOT NULL DEFAULT 0.00 COMMENT '工作小时数',
    `daily_earnings` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '当日收入（元）',
    `start_time` datetime NULL COMMENT '开始工作时间',
    `end_time` datetime NULL COMMENT '结束工作时间',
    `pause_start_time` datetime NULL COMMENT '暂停开始时间',
    `total_pause_minutes` int NULL DEFAULT 0 COMMENT '累计暂停时长（分钟）',
    `work_status` tinyint NOT NULL DEFAULT 0 COMMENT '工作状态：0-未开始，1-进行中，2-暂停中，3-已完成',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-无效，1-有效',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_date` (`user_id`, `work_date`) USING BTREE,
    INDEX `idx_work_date` (`work_date`) USING BTREE,
    INDEX `idx_user_id_work_date` (`user_id`, `work_date`) USING BTREE,
    INDEX `idx_work_status` (`work_status`) USING BTREE,
    CONSTRAINT `fk_work_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作记录表';
