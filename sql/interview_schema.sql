-- 面试题模块数据库表结构
-- 1. 面试题分类表
DROP TABLE IF EXISTS `interview_category`;
CREATE TABLE `interview_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `question_set_count` INT DEFAULT 0 COMMENT '题单数量',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0-禁用 1-启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题分类表';

-- 2. 面试题单表
DROP TABLE IF EXISTS `interview_question_set`;
CREATE TABLE `interview_question_set` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题单ID',
    `title` VARCHAR(200) NOT NULL COMMENT '题单标题',
    `description` TEXT COMMENT '题单描述',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `type` TINYINT DEFAULT 2 COMMENT '类型 (1-官方 2-用户创建)',
    `visibility` TINYINT DEFAULT 1 COMMENT '可见性 (1-公开 2-私有) 仅用户创建题单有效',
    `question_count` INT DEFAULT 0 COMMENT '题目数量',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `favorite_count` INT DEFAULT 0 COMMENT '收藏次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0-草稿 1-发布 2-下线)',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `creator_name` VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_type_status` (`type`, `status`),
    KEY `idx_visibility_status` (`visibility`, `status`),
    KEY `idx_create_time` (`create_time`),
    FULLTEXT KEY `ft_title_description` (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题单表';

-- 3. 面试题目表
DROP TABLE IF EXISTS `interview_question`;
CREATE TABLE `interview_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `question_set_id` BIGINT NOT NULL COMMENT '所属题单ID',
    `title` VARCHAR(500) NOT NULL COMMENT '题目标题',
    `content` TEXT NOT NULL COMMENT '题目内容 (Markdown格式)',
    `answer` TEXT COMMENT '参考答案 (Markdown格式)',
    `sort_order` INT DEFAULT 0 COMMENT '题单内排序',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `favorite_count` INT DEFAULT 0 COMMENT '收藏次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_question_set_id` (`question_set_id`),
    KEY `idx_sort_order` (`question_set_id`, `sort_order`),
    KEY `idx_create_time` (`create_time`),
    FULLTEXT KEY `ft_title_content_answer` (`title`, `content`, `answer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题目表';

-- 4. 用户收藏表
DROP TABLE IF EXISTS `interview_favorite`;
CREATE TABLE `interview_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` TINYINT NOT NULL COMMENT '收藏类型 (1-题单 2-题目)',
    `target_id` BIGINT NOT NULL COMMENT '目标ID（题单ID或题目ID）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_user_type` (`user_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 添加外键约束
ALTER TABLE `interview_question_set` ADD CONSTRAINT `fk_question_set_category` 
    FOREIGN KEY (`category_id`) REFERENCES `interview_category` (`id`) ON DELETE RESTRICT;

ALTER TABLE `interview_question` ADD CONSTRAINT `fk_question_question_set` 
    FOREIGN KEY (`question_set_id`) REFERENCES `interview_question_set` (`id`) ON DELETE CASCADE;
