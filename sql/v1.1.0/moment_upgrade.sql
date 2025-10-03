-- ============================================
-- 朋友圈模块 v1.1.0 数据库升级脚本
-- 版本：v1.1.0
-- 日期：2025-10-03
-- 说明：新增浏览数、收藏数字段，创建收藏表和浏览记录表
-- ============================================

-- 1. moments表增加字段
ALTER TABLE moments 
ADD COLUMN view_count INT DEFAULT 0 COMMENT '浏览数' AFTER comment_count,
ADD COLUMN favorite_count INT DEFAULT 0 COMMENT '收藏数' AFTER view_count;

-- 2. moments表增加索引（用于热门动态查询）
ALTER TABLE moments 
ADD INDEX idx_hot_score (status, create_time, like_count, comment_count);

-- 3. 创建动态收藏表
CREATE TABLE IF NOT EXISTS moment_favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    moment_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    
    UNIQUE KEY uk_moment_user (moment_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_moment_id (moment_id),
    INDEX idx_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态收藏表';

-- 4. 创建动态浏览记录表（可选，用于详细统计分析）
CREATE TABLE IF NOT EXISTS moment_views (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '浏览记录ID',
    moment_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT COMMENT '用户ID（未登录为NULL）',
    ip VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    
    INDEX idx_moment_id (moment_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态浏览记录表';

-- 5. 初始化现有数据的view_count和favorite_count为0（如果有历史数据）
UPDATE moments SET view_count = 0 WHERE view_count IS NULL;
UPDATE moments SET favorite_count = 0 WHERE favorite_count IS NULL;

