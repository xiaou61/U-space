-- 积分系统数据库表结构 v1.4.0
-- 创建时间：2025-09-23
-- 说明：实现积分系统，包括余额管理、明细记录和打卡位图存储

-- 1. 用户积分余额表
CREATE TABLE user_points_balance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_points INT DEFAULT 0 COMMENT '总积分余额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_points_desc (total_points DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '用户积分余额表';

-- 2. 积分明细表
CREATE TABLE user_points_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    points_change INT NOT NULL COMMENT '积分变动数量（正数为增加）',
    points_type TINYINT NOT NULL COMMENT '积分类型：1-后台发放 2-打卡积分',
    description VARCHAR(500) COMMENT '变动描述/原因',
    balance_after INT NOT NULL COMMENT '变动后余额',
    admin_id BIGINT COMMENT '操作管理员ID（后台发放时记录）',
    continuous_days INT COMMENT '连续打卡天数（打卡积分时记录）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time DESC),
    INDEX idx_user_time (user_id, create_time DESC),
    INDEX idx_points_type (points_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '积分明细表';

-- 3. 用户打卡位图表（优化存储方案）
CREATE TABLE user_checkin_bitmap (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    `year_month` VARCHAR(7) NOT NULL COMMENT '年月格式YYYY-MM',
    checkin_bitmap BIGINT DEFAULT 0 COMMENT '打卡位图每位代表当月某天',
    continuous_days INT DEFAULT 0 COMMENT '当前连续打卡天数',
    last_checkin_date DATE COMMENT '最后打卡日期',
    total_checkin_days INT DEFAULT 0 COMMENT '当月总打卡天数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_user_year_month (user_id, `year_month`),
    INDEX idx_user_id (user_id),
    INDEX idx_last_checkin (last_checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户打卡位图表';

-- 老用户数据迁移（可选执行）
-- 为现有老用户创建积分账户，新用户注册时会自动创建
-- INSERT INTO user_points_balance (user_id, total_points) 
-- SELECT id, 0 FROM sys_user WHERE status = 1 AND id NOT IN (SELECT user_id FROM user_points_balance);
