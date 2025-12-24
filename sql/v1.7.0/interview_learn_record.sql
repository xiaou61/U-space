-- 面试题学习记录表
-- v1.7.0

CREATE TABLE IF NOT EXISTS `interview_learn_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_set_id` BIGINT NOT NULL COMMENT '题单ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '学习时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    KEY `idx_user_set` (`user_id`, `question_set_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题学习记录';
