-- =============================================
-- 社区模块v1.1.0升级SQL
-- 版本：v1.1.0
-- 日期：2025-10-03
-- 说明：新增标签系统、二级评论、AI摘要等功能
-- =============================================

-- =============================================
-- 1. 新增标签表
-- =============================================

-- 标签表
CREATE TABLE IF NOT EXISTS `community_tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '标签描述',
    `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '标签图标',
    `post_count` INT DEFAULT 0 COMMENT '帖子数量',
    `follow_count` INT DEFAULT 0 COMMENT '关注数量',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `status` INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY `uk_name` (`name`),
    INDEX `idx_post_count` (`post_count` DESC),
    INDEX `idx_status` (`status`),
    INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区标签表';

-- 帖子标签关联表
CREATE TABLE IF NOT EXISTS `community_post_tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
    INDEX `idx_post_id` (`post_id`),
    INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';

-- =============================================
-- 2. 帖子表字段调整
-- =============================================

-- 添加AI摘要相关字段
ALTER TABLE `community_post`
ADD COLUMN `ai_summary` TEXT DEFAULT NULL COMMENT 'AI生成的摘要' AFTER `content`,
ADD COLUMN `ai_keywords` VARCHAR(200) DEFAULT NULL COMMENT 'AI提取的关键词，逗号分隔' AFTER `ai_summary`,
ADD COLUMN `ai_generate_time` DATETIME DEFAULT NULL COMMENT 'AI生成时间' AFTER `ai_keywords`;

-- 添加全文索引（用于搜索优化）
-- 注意：MySQL 5.7及以上版本支持InnoDB全文索引
ALTER TABLE `community_post`
ADD FULLTEXT INDEX `ft_title_content` (`title`, `content`) WITH PARSER ngram;

-- 添加性能优化索引
ALTER TABLE `community_post`
ADD INDEX `idx_status_top_time` (`status`, `is_top` DESC, `create_time` DESC),
ADD INDEX `idx_category_status_time` (`category_id`, `status`, `create_time` DESC),
ADD INDEX `idx_author_status_time` (`author_id`, `status`, `create_time` DESC),
ADD INDEX `idx_hot_score` (`status`, `create_time`, `like_count`, `comment_count`);

-- =============================================
-- 3. 评论表字段调整（支持二级评论）
-- =============================================

-- 添加二级评论支持字段
ALTER TABLE `community_comment`
ADD COLUMN `reply_to_id` BIGINT DEFAULT NULL COMMENT '回复的评论ID，NULL表示一级评论' AFTER `parent_id`,
ADD COLUMN `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '回复的用户ID' AFTER `reply_to_id`,
ADD COLUMN `reply_to_user_name` VARCHAR(50) DEFAULT NULL COMMENT '回复的用户名' AFTER `reply_to_user_id`,
ADD COLUMN `reply_count` INT DEFAULT 0 COMMENT '回复数量（仅一级评论有效）' AFTER `like_count`;

-- 添加索引优化查询
ALTER TABLE `community_comment`
ADD INDEX `idx_post_parent_time` (`post_id`, `parent_id`, `create_time` DESC),
ADD INDEX `idx_reply_to_id` (`reply_to_id`),
ADD INDEX `idx_author_time` (`author_id`, `create_time` DESC);

-- =============================================
-- 4. 点赞表索引优化
-- =============================================

ALTER TABLE `community_post_like`
ADD INDEX `idx_user_time` (`user_id`, `create_time` DESC);

-- =============================================
-- 5. 收藏表索引优化
-- =============================================

ALTER TABLE `community_post_collect`
ADD INDEX `idx_user_time` (`user_id`, `create_time` DESC);

-- =============================================
-- 6. 插入初始标签数据（示例）
-- =============================================

INSERT INTO `community_tag` (`name`, `description`, `color`, `post_count`, `sort_order`, `status`) VALUES
('Java', 'Java编程语言相关', '#E53935', 0, 1, 1),
('Spring', 'Spring框架相关', '#43A047', 0, 2, 1),
('MySQL', 'MySQL数据库相关', '#1E88E5', 0, 3, 1),
('Redis', 'Redis缓存相关', '#D32F2F', 0, 4, 1),
('Vue', 'Vue.js前端框架', '#4CAF50', 0, 5, 1),
('算法', '算法与数据结构', '#FF9800', 0, 6, 1),
('面试', '面试经验分享', '#9C27B0', 0, 7, 1),
('架构', '系统架构设计', '#795548', 0, 8, 1),
('微服务', '微服务架构相关', '#00BCD4', 0, 9, 1),
('Docker', 'Docker容器技术', '#2196F3', 0, 10, 1),
('Kubernetes', 'K8s容器编排', '#326CE5', 0, 11, 1),
('前端', '前端开发相关', '#FF5722', 0, 12, 1),
('后端', '后端开发相关', '#3F51B5', 0, 13, 1),
('数据库', '数据库设计与优化', '#607D8B', 0, 14, 1),
('性能优化', '系统性能优化', '#FFC107', 0, 15, 1),
('分布式', '分布式系统', '#673AB7', 0, 16, 1),
('消息队列', 'MQ消息队列', '#009688', 0, 17, 1),
('安全', '系统安全相关', '#F44336', 0, 18, 1),
('运维', '系统运维相关', '#4CAF50', 0, 19, 1),
('职场', '职场经验分享', '#9E9E9E', 0, 20, 1);

-- =============================================
-- 7. 数据迁移说明
-- =============================================

-- 说明：
-- 1. 执行此脚本前请备份数据库
-- 2. 全文索引创建可能需要较长时间，建议在低峰期执行
-- 3. 如果表数据量很大，索引创建可能影响性能，建议分批执行
-- 4. 标签数据为示例数据，可根据实际需求调整

-- =============================================
-- 8. 回滚SQL（如需回滚）
-- =============================================

/*
-- 回滚标签表
DROP TABLE IF EXISTS `community_post_tag`;
DROP TABLE IF EXISTS `community_tag`;

-- 回滚帖子表字段
ALTER TABLE `community_post`
DROP COLUMN `ai_summary`,
DROP COLUMN `ai_keywords`,
DROP COLUMN `ai_generate_time`,
DROP INDEX `ft_title_content`,
DROP INDEX `idx_status_top_time`,
DROP INDEX `idx_category_status_time`,
DROP INDEX `idx_author_status_time`,
DROP INDEX `idx_hot_score`;

-- 回滚评论表字段
ALTER TABLE `community_comment`
DROP COLUMN `reply_to_id`,
DROP COLUMN `reply_to_user_id`,
DROP COLUMN `reply_to_user_name`,
DROP COLUMN `reply_count`,
DROP INDEX `idx_post_parent_time`,
DROP INDEX `idx_reply_to_id`,
DROP INDEX `idx_author_time`;

-- 回滚点赞表索引
ALTER TABLE `community_post_like`
DROP INDEX `idx_user_time`;

-- 回滚收藏表索引
ALTER TABLE `community_post_collect`
DROP INDEX `idx_user_time`;
*/

-- =============================================
-- 升级完成
-- =============================================

