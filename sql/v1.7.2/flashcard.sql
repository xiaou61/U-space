-- =============================================
-- 闪卡记忆系统 - 数据库表结构
-- 版本: v1.7.2
-- 创建时间: 2026-01-19
-- 基于 SM-2 间隔重复算法
-- =============================================

-- 先删除旧表（注意顺序，先删有外键依赖的表）
DROP TABLE IF EXISTS flashcard_user_deck;
DROP TABLE IF EXISTS flashcard_daily_stats;
DROP TABLE IF EXISTS flashcard_study_record;
DROP TABLE IF EXISTS flashcard;
DROP TABLE IF EXISTS flashcard_deck;

-- 卡组表
CREATE TABLE IF NOT EXISTS flashcard_deck (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '卡组ID',
    user_id BIGINT NOT NULL COMMENT '创建者ID',
    name VARCHAR(100) NOT NULL COMMENT '卡组名称',
    description VARCHAR(500) COMMENT '卡组描述',
    cover_image VARCHAR(500) COMMENT '封面图片',
    tags VARCHAR(255) COMMENT '标签，逗号分隔',
    is_public TINYINT(1) DEFAULT 0 COMMENT '是否公开: 0-私有 1-公开',
    card_count INT DEFAULT 0 COMMENT '卡片数量',
    study_count INT DEFAULT 0 COMMENT '学习人数',
    fork_count INT DEFAULT 0 COMMENT '复制次数',
    source_deck_id BIGINT COMMENT '复制来源ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志: 0-正常 1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_is_public (is_public),
    INDEX idx_del_flag (del_flag),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪卡卡组表';

-- 闪卡表
CREATE TABLE IF NOT EXISTS flashcard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '闪卡ID',
    deck_id BIGINT NOT NULL COMMENT '所属卡组ID',
    front_content TEXT NOT NULL COMMENT '正面内容(问题)',
    back_content TEXT NOT NULL COMMENT '反面内容(答案)',
    content_type TINYINT DEFAULT 1 COMMENT '内容类型: 1-文本 2-Markdown 3-代码',
    tags VARCHAR(255) COMMENT '标签',
    source_question_id BIGINT COMMENT '关联题库题目ID',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志: 0-正常 1-已删除',
    INDEX idx_deck_id (deck_id),
    INDEX idx_source_question_id (source_question_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪卡表';

-- 用户学习记录表 (SM-2 算法核心数据)
CREATE TABLE IF NOT EXISTS flashcard_study_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_id BIGINT NOT NULL COMMENT '闪卡ID',
    deck_id BIGINT NOT NULL COMMENT '卡组ID',
    -- SM-2 算法字段
    repetitions INT DEFAULT 0 COMMENT '连续正确次数',
    ease_factor DECIMAL(4,2) DEFAULT 2.50 COMMENT '难度因子(EF)，范围1.3-2.5+',
    interval_days INT DEFAULT 0 COMMENT '当前间隔天数',
    -- 掌握度
    mastery_level TINYINT DEFAULT 1 COMMENT '掌握度: 1-新卡 2-学习中 3-已掌握',
    -- 复习计划
    last_review_time DATETIME COMMENT '上次复习时间',
    next_review_time DATETIME COMMENT '下次复习时间',
    total_reviews INT DEFAULT 0 COMMENT '总复习次数',
    correct_count INT DEFAULT 0 COMMENT '正确次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_card (user_id, card_id),
    INDEX idx_user_id (user_id),
    INDEX idx_deck_id (deck_id),
    INDEX idx_next_review (user_id, next_review_time),
    INDEX idx_mastery (user_id, mastery_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪卡学习记录表';

-- 每日学习统计表
CREATE TABLE IF NOT EXISTS flashcard_daily_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    new_cards INT DEFAULT 0 COMMENT '新学卡片数',
    review_cards INT DEFAULT 0 COMMENT '复习卡片数',
    correct_cards INT DEFAULT 0 COMMENT '正确卡片数',
    study_duration INT DEFAULT 0 COMMENT '学习时长(秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_date (user_id, stat_date),
    INDEX idx_user_id (user_id),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪卡每日学习统计';

-- 用户卡组关联表 (用于记录用户学习了哪些卡组)
CREATE TABLE IF NOT EXISTS flashcard_user_deck (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    deck_id BIGINT NOT NULL COMMENT '卡组ID',
    is_owner TINYINT(1) DEFAULT 0 COMMENT '是否为创建者: 0-否 1-是',
    last_study_time DATETIME COMMENT '最后学习时间',
    total_cards INT DEFAULT 0 COMMENT '总卡片数',
    learned_cards INT DEFAULT 0 COMMENT '已学习卡片数',
    mastered_cards INT DEFAULT 0 COMMENT '已掌握卡片数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    UNIQUE KEY uk_user_deck (user_id, deck_id),
    INDEX idx_user_id (user_id),
    INDEX idx_deck_id (deck_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户卡组关联表';
