-- =============================================
-- 闪卡记忆系统数据库脚本
-- 版本: v1.6.2
-- 创建时间: 2025-12-10
-- =============================================

-- -------------------------------------------
-- 1. 闪卡卡组表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS flashcard_deck (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '卡组ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID (0为系统卡组)',
    name VARCHAR(100) NOT NULL COMMENT '卡组名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '卡组描述',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
    category_id BIGINT DEFAULT NULL COMMENT '关联的面试题分类ID',
    card_count INT DEFAULT 0 COMMENT '闪卡总数',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开: 0-私有 1-公开',
    is_official TINYINT DEFAULT 0 COMMENT '是否官方: 0-用户 1-官方',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_is_public (is_public),
    INDEX idx_is_official (is_official)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪卡卡组表';

-- -------------------------------------------
-- 2. 闪卡表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS flashcard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '闪卡ID',
    user_id BIGINT NOT NULL COMMENT '创建用户ID',
    deck_id BIGINT NOT NULL COMMENT '所属卡组ID',
    question_id BIGINT DEFAULT NULL COMMENT '关联的面试题ID',
    front_content TEXT NOT NULL COMMENT '正面内容(问题)',
    back_content LONGTEXT NOT NULL COMMENT '背面内容(答案)',
    source_type TINYINT DEFAULT 1 COMMENT '来源类型: 1-题库生成 2-用户创建',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_deck_id (deck_id),
    INDEX idx_question_id (question_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪卡表';

-- -------------------------------------------
-- 3. 闪卡学习记录表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS flashcard_study_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_id BIGINT NOT NULL COMMENT '闪卡ID',
    deck_id BIGINT NOT NULL COMMENT '卡组ID',
    ease_factor DECIMAL(4,2) DEFAULT 2.50 COMMENT '难度因子(EF)',
    interval_days INT DEFAULT 0 COMMENT '当前间隔天数',
    repetitions INT DEFAULT 0 COMMENT '连续正确次数',
    quality INT DEFAULT NULL COMMENT '最近一次评分(0-3)',
    next_review_date DATE NOT NULL COMMENT '下次复习日期',
    last_review_time DATETIME DEFAULT NULL COMMENT '最近复习时间',
    total_reviews INT DEFAULT 0 COMMENT '总复习次数',
    correct_count INT DEFAULT 0 COMMENT '正确次数',
    wrong_count INT DEFAULT 0 COMMENT '错误次数',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-新卡 1-学习中 2-已掌握',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_card (user_id, card_id),
    INDEX idx_user_id (user_id),
    INDEX idx_next_review (user_id, next_review_date),
    INDEX idx_deck_id (deck_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪卡学习记录表';

-- -------------------------------------------
-- 4. 闪卡每日学习统计表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS flashcard_daily_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    study_date DATE NOT NULL COMMENT '学习日期',
    new_count INT DEFAULT 0 COMMENT '新学数量',
    review_count INT DEFAULT 0 COMMENT '复习数量',
    correct_count INT DEFAULT 0 COMMENT '正确数量',
    wrong_count INT DEFAULT 0 COMMENT '错误数量',
    study_time_seconds INT DEFAULT 0 COMMENT '学习时长(秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_date (user_id, study_date),
    INDEX idx_user_id (user_id),
    INDEX idx_study_date (study_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪卡每日学习统计表';

-- -------------------------------------------
-- 5. 闪卡复习详情日志表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS flashcard_review_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_id BIGINT NOT NULL COMMENT '闪卡ID',
    quality INT NOT NULL COMMENT '评分(0-3)',
    ease_factor_before DECIMAL(4,2) COMMENT '复习前EF',
    ease_factor_after DECIMAL(4,2) COMMENT '复习后EF',
    interval_before INT COMMENT '复习前间隔',
    interval_after INT COMMENT '复习后间隔',
    review_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '复习时间',
    time_spent_ms INT DEFAULT 0 COMMENT '思考时长(毫秒)',
    INDEX idx_user_id (user_id),
    INDEX idx_card_id (card_id),
    INDEX idx_review_time (review_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪卡复习详情日志表';

ALTER TABLE flashcard_deck
    ADD COLUMN category VARCHAR(50) DEFAULT NULL COMMENT '分类标识(java,spring等)'
        AFTER category_id;