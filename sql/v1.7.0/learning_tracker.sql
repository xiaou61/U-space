-- 学习效果追踪模块表结构
-- 版本: v1.7.0
-- 日期: 2025-12-24

-- 1. 学习掌握度记录表
CREATE TABLE IF NOT EXISTS interview_mastery_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    question_set_id BIGINT NOT NULL COMMENT '题单ID（冗余，方便查询）',
    mastery_level TINYINT NOT NULL DEFAULT 1 COMMENT '掌握度等级 1-不会 2-模糊 3-熟悉 4-已掌握',
    review_count INT NOT NULL DEFAULT 0 COMMENT '复习次数',
    last_review_time DATETIME COMMENT '上次复习时间',
    next_review_time DATETIME COMMENT '下次应复习时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次学习时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_question (user_id, question_id),
    INDEX idx_user_next_review (user_id, next_review_time),
    INDEX idx_user_mastery (user_id, mastery_level),
    INDEX idx_user_set (user_id, question_set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题掌握度记录表';

-- 2. 每日学习统计表
CREATE TABLE IF NOT EXISTS interview_daily_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    learn_count INT NOT NULL DEFAULT 0 COMMENT '新学习题目数',
    review_count INT NOT NULL DEFAULT 0 COMMENT '复习题目数',
    total_count INT NOT NULL DEFAULT 0 COMMENT '总学习题目数（新学习+复习）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_date (user_id, stat_date),
    INDEX idx_user_date_range (user_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题每日学习统计表';

-- 3. 学习掌握度历史记录表（用于分析学习轨迹）
CREATE TABLE IF NOT EXISTS interview_mastery_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    mastery_level TINYINT NOT NULL COMMENT '掌握度等级',
    is_review TINYINT NOT NULL DEFAULT 0 COMMENT '是否复习 0-首次学习 1-复习',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    INDEX idx_user_time (user_id, create_time),
    INDEX idx_question_time (question_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题掌握度变更历史表';
