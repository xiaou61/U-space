-- =============================================
-- 个人计划打卡模块 SQL v1.6.1
-- =============================================

-- 1. 用户计划表
CREATE TABLE IF NOT EXISTS user_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    plan_name VARCHAR(100) NOT NULL COMMENT '计划名称',
    plan_desc VARCHAR(500) COMMENT '计划描述',
    plan_type TINYINT NOT NULL COMMENT '计划类型：1-刷题 2-学习 3-阅读 4-运动 5-自定义',
    target_type TINYINT NOT NULL DEFAULT 1 COMMENT '目标类型：1-数量 2-时长 3-次数',
    target_value INT NOT NULL DEFAULT 1 COMMENT '目标值',
    target_unit VARCHAR(20) DEFAULT '次' COMMENT '目标单位（道/小时/次）',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE COMMENT '结束日期（NULL表示长期）',
    daily_start_time TIME COMMENT '每日开始时间',
    daily_end_time TIME COMMENT '每日截止时间',
    repeat_type TINYINT NOT NULL DEFAULT 1 COMMENT '重复类型：1-每日 2-工作日 3-周末 4-自定义',
    repeat_days VARCHAR(20) COMMENT '自定义重复日（如：1,2,3,4,5 表示周一到周五）',
    remind_before INT DEFAULT 30 COMMENT '提前提醒分钟数',
    remind_deadline INT DEFAULT 10 COMMENT '截止提醒分钟数',
    remind_enabled TINYINT DEFAULT 1 COMMENT '是否启用提醒：0-否 1-是',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除 1-进行中 2-已暂停 3-已完成 4-已过期',
    total_checkin_days INT DEFAULT 0 COMMENT '累计打卡天数',
    current_streak INT DEFAULT 0 COMMENT '当前连续打卡天数',
    max_streak INT DEFAULT 0 COMMENT '最长连续打卡天数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_user_status (user_id, status),
    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户计划表';

-- 2. 计划打卡记录表
CREATE TABLE IF NOT EXISTS plan_checkin_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    checkin_date DATE NOT NULL COMMENT '打卡日期',
    checkin_time DATETIME NOT NULL COMMENT '打卡时间',
    complete_value INT COMMENT '完成数量',
    complete_content VARCHAR(500) COMMENT '完成内容描述',
    remark VARCHAR(500) COMMENT '心得备注',
    is_supplement TINYINT DEFAULT 0 COMMENT '是否补卡：0-否 1-是',
    points_earned INT DEFAULT 0 COMMENT '获得积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_plan_date (plan_id, checkin_date),
    INDEX idx_user_id (user_id),
    INDEX idx_plan_id (plan_id),
    INDEX idx_checkin_date (checkin_date),
    INDEX idx_user_date (user_id, checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划打卡记录表';

-- 3. 计划提醒任务表
CREATE TABLE IF NOT EXISTS plan_remind_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    remind_type TINYINT NOT NULL COMMENT '提醒类型：1-开始提醒 2-截止提醒',
    remind_date DATE NOT NULL COMMENT '提醒日期',
    remind_time DATETIME NOT NULL COMMENT '提醒时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待发送 1-已发送 2-已取消',
    send_time DATETIME COMMENT '实际发送时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_remind_time (remind_time),
    INDEX idx_status (status),
    INDEX idx_plan_id (plan_id),
    INDEX idx_remind_date_status (remind_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划提醒任务表';
