-- ========================================
-- 积分抽奖系统数据库表设计 v1.5.0
-- ========================================

-- 1. 抽奖奖品配置表
CREATE TABLE IF NOT EXISTS lottery_prize_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '奖品ID',
    prize_name VARCHAR(50) NOT NULL COMMENT '奖品名称',
    prize_level TINYINT NOT NULL COMMENT '奖品等级：1-特等奖 2-一等奖 3-二等奖 4-三等奖 5-四等奖 6-五等奖 7-六等奖 8-未中奖',
    prize_points INT NOT NULL COMMENT '奖励积分',
    base_probability DECIMAL(10, 8) NOT NULL COMMENT '基础中奖概率（0-1之间）',
    current_probability DECIMAL(10, 8) NOT NULL COMMENT '当前动态概率（0-1之间）',
    target_return_rate DECIMAL(6, 4) DEFAULT 0.0100 COMMENT '目标回报率（如0.0100=1%）',
    max_return_rate DECIMAL(6, 4) DEFAULT 0.0150 COMMENT '最大回报率阈值（如0.0150=1.5%）',
    min_return_rate DECIMAL(6, 4) DEFAULT 0.0050 COMMENT '最小回报率阈值（如0.0050=0.5%）',
    actual_return_rate DECIMAL(6, 4) DEFAULT 0 COMMENT '实际回报率',
    total_draw_count INT DEFAULT 0 COMMENT '总抽奖次数（作为分母）',
    total_win_count INT DEFAULT 0 COMMENT '总中奖次数',
    today_draw_count INT DEFAULT 0 COMMENT '今日抽奖次数',
    today_win_count INT DEFAULT 0 COMMENT '今日中奖次数',
    daily_stock INT DEFAULT -1 COMMENT '每日库存（-1表示无限制）',
    total_stock INT DEFAULT -1 COMMENT '总库存（-1表示无限制）',
    current_stock INT DEFAULT 0 COMMENT '当前剩余库存',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    prize_icon VARCHAR(255) COMMENT '奖品图标',
    prize_desc VARCHAR(500) COMMENT '奖品描述',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    is_suspended TINYINT DEFAULT 0 COMMENT '是否暂停：0-正常 1-暂停（回报率超标）',
    suspend_reason VARCHAR(255) COMMENT '暂停原因',
    suspend_until DATETIME COMMENT '暂停至某时间',
    adjust_strategy VARCHAR(50) DEFAULT 'AUTO' COMMENT '调整策略：AUTO-自动 MANUAL-手动 FIXED-固定',
    last_adjust_time DATETIME COMMENT '最后调整时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_active (is_active),
    INDEX idx_level (prize_level),
    INDEX idx_probability (current_probability DESC),
    INDEX idx_return_rate (actual_return_rate DESC),
    INDEX idx_suspended (is_suspended)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '抽奖奖品配置表';

-- 2. 抽奖记录表
CREATE TABLE IF NOT EXISTS lottery_draw_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    prize_id BIGINT NOT NULL COMMENT '奖品ID',
    prize_level TINYINT NOT NULL COMMENT '奖品等级',
    prize_points INT NOT NULL COMMENT '获得积分',
    cost_points INT NOT NULL COMMENT '消耗积分',
    actual_probability DECIMAL(10, 8) COMMENT '实际中奖概率',
    draw_strategy VARCHAR(50) COMMENT '使用的抽奖策略',
    draw_ip VARCHAR(50) COMMENT '抽奖IP',
    draw_device VARCHAR(100) COMMENT '抽奖设备',
    status TINYINT DEFAULT 1 COMMENT '状态：1-成功 2-失败 3-已补偿',
    cost_detail_id BIGINT COMMENT '积分明细ID（扣减）',
    reward_detail_id BIGINT COMMENT '积分明细ID（奖励）',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '抽奖时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time DESC),
    INDEX idx_user_time (user_id, create_time DESC),
    INDEX idx_prize_level (prize_level),
    INDEX idx_status (status),
    INDEX idx_prize_id (prize_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '抽奖记录表';

-- 3. 抽奖统计表（每日汇总）
CREATE TABLE IF NOT EXISTS lottery_statistics_daily (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_draw_count INT DEFAULT 0 COMMENT '总抽奖次数',
    total_cost_points BIGINT DEFAULT 0 COMMENT '总消耗积分',
    total_reward_points BIGINT DEFAULT 0 COMMENT '总奖励积分',
    actual_return_rate DECIMAL(5, 2) COMMENT '实际回报率（%）',
    profit_points BIGINT DEFAULT 0 COMMENT '平台利润积分',
    unique_user_count INT DEFAULT 0 COMMENT '参与用户数',
    special_prize_count INT DEFAULT 0 COMMENT '特等奖中奖次数',
    first_prize_count INT DEFAULT 0 COMMENT '一等奖中奖次数',
    second_prize_count INT DEFAULT 0 COMMENT '二等奖中奖次数',
    third_prize_count INT DEFAULT 0 COMMENT '三等奖中奖次数',
    fourth_prize_count INT DEFAULT 0 COMMENT '四等奖中奖次数',
    fifth_prize_count INT DEFAULT 0 COMMENT '五等奖中奖次数',
    sixth_prize_count INT DEFAULT 0 COMMENT '六等奖中奖次数',
    no_prize_count INT DEFAULT 0 COMMENT '未中奖次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_stat_date (stat_date),
    INDEX idx_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '抽奖每日统计表';

-- 4. 用户抽奖限制表
CREATE TABLE IF NOT EXISTS user_lottery_limit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    today_draw_count INT DEFAULT 0 COMMENT '今日抽奖次数',
    week_draw_count INT DEFAULT 0 COMMENT '本周抽奖次数',
    month_draw_count INT DEFAULT 0 COMMENT '本月抽奖次数',
    total_draw_count INT DEFAULT 0 COMMENT '总抽奖次数',
    today_win_count INT DEFAULT 0 COMMENT '今日中奖次数',
    total_win_count INT DEFAULT 0 COMMENT '总中奖次数',
    max_continuous_no_win INT DEFAULT 0 COMMENT '最大连续未中奖次数',
    current_continuous_no_win INT DEFAULT 0 COMMENT '当前连续未中奖次数',
    last_draw_time DATETIME COMMENT '最后抽奖时间',
    last_win_time DATETIME COMMENT '最后中奖时间',
    is_blacklist TINYINT DEFAULT 0 COMMENT '是否黑名单：0-否 1-是',
    risk_level TINYINT DEFAULT 0 COMMENT '风险等级：0-正常 1-低风险 2-中风险 3-高风险',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_blacklist (is_blacklist),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '用户抽奖限制表';

-- 5. 概率调整历史表（新增）
CREATE TABLE IF NOT EXISTS lottery_adjust_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    prize_id BIGINT NOT NULL COMMENT '奖品ID',
    adjust_type VARCHAR(20) NOT NULL COMMENT '调整类型：AUTO-自动 MANUAL-手动',
    old_probability DECIMAL(10, 8) NOT NULL COMMENT '调整前概率',
    new_probability DECIMAL(10, 8) NOT NULL COMMENT '调整后概率',
    old_return_rate DECIMAL(6, 4) COMMENT '调整前回报率',
    new_return_rate DECIMAL(6, 4) COMMENT '调整后回报率',
    adjust_reason VARCHAR(500) COMMENT '调整原因',
    operator VARCHAR(50) COMMENT '操作人（SYSTEM或管理员名称）',
    operator_id BIGINT COMMENT '操作人ID（管理员ID）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '调整时间',
    
    INDEX idx_prize_id (prize_id),
    INDEX idx_create_time (create_time DESC),
    INDEX idx_adjust_type (adjust_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '概率调整历史表';

-- ========================================
-- 初始化数据 - 8个奖项配置
-- ========================================
INSERT INTO lottery_prize_config (prize_name, prize_level, prize_points, base_probability, current_probability, 
    target_return_rate, max_return_rate, min_return_rate, display_order, prize_desc, is_active) VALUES
('特等奖', 1, 1000, 0.00100000, 0.00100000, 0.0100, 0.0150, 0.0050, 1, '超级大奖！1000积分', 1),
('一等奖', 2, 500, 0.00500000, 0.00500000, 0.0250, 0.0300, 0.0200, 2, '幸运大奖！500积分', 1),
('二等奖', 3, 200, 0.02000000, 0.02000000, 0.0400, 0.0500, 0.0300, 3, '恭喜中奖！200积分', 1),
('三等奖', 4, 150, 0.05000000, 0.05000000, 0.0750, 0.0850, 0.0650, 4, '不错的奖品！150积分', 1),
('四等奖', 5, 100, 0.10000000, 0.10000000, 0.1000, 0.1100, 0.0900, 5, '保本奖！100积分', 1),
('五等奖', 6, 50, 0.30000000, 0.30000000, 0.1500, 0.1600, 0.1400, 6, '小奖励！50积分', 1),
('六等奖', 7, 20, 0.40000000, 0.40000000, 0.0800, 0.0900, 0.0700, 7, '安慰奖！20积分', 1),
('未中奖', 8, 0, 0.12400000, 0.12400000, 0.0000, 0.0000, 0.0000, 8, '很遗憾，再试一次吧！', 1);

-- ========================================
-- 扩展积分类型枚举（需要在代码中添加）
-- ========================================
-- 新增积分类型：
-- 3: LOTTERY_COST - 抽奖消耗
-- 4: LOTTERY_REWARD - 抽奖奖励

