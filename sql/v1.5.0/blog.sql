-- 个人博客模块数据库表结构 v1.5.0
-- 创建时间：2025-10-02
-- 说明：实现个人博客系统，包括博客配置、文章、分类和标签管理

-- 1. 博客配置表
CREATE TABLE IF NOT EXISTS blog_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    blog_name VARCHAR(100) COMMENT '博客名称',
    blog_description VARCHAR(500) COMMENT '博客简介',
    blog_avatar VARCHAR(255) COMMENT '博客头像URL',
    blog_cover VARCHAR(255) COMMENT '博客背景图URL',
    blog_notice VARCHAR(200) COMMENT '博客公告',
    personal_tags VARCHAR(500) COMMENT '个人标签，JSON格式',
    social_links VARCHAR(1000) COMMENT '社交链接，JSON格式',
    is_public TINYINT DEFAULT 1 COMMENT '是否公开：1-公开 0-私密',
    total_articles INT DEFAULT 0 COMMENT '文章总数',
    points_cost INT DEFAULT 50 COMMENT '开通消耗积分',
    status TINYINT DEFAULT 1 COMMENT '博客状态：1-正常 0-封禁',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_total_articles (total_articles DESC),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '博客配置表';

-- 2. 博客文章表
CREATE TABLE IF NOT EXISTS blog_article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
    user_id BIGINT NOT NULL COMMENT '作者用户ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    cover_image VARCHAR(255) COMMENT '文章封面图',
    summary VARCHAR(500) COMMENT '文章摘要',
    content LONGTEXT NOT NULL COMMENT '文章内容（Markdown格式）',
    category_id BIGINT COMMENT '文章分类ID',
    tags VARCHAR(200) COMMENT '文章标签，JSON数组格式',
    status TINYINT DEFAULT 0 COMMENT '文章状态：0-草稿 1-已发布 2-已下架 3-已删除',
    is_original TINYINT DEFAULT 1 COMMENT '是否原创：1-原创 0-转载',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：1-置顶 0-普通',
    top_expire_time DATETIME COMMENT '置顶过期时间',
    points_cost INT DEFAULT 20 COMMENT '发布消耗积分',
    publish_time DATETIME COMMENT '发布时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status_publish_time (status, publish_time DESC),
    INDEX idx_create_time (create_time DESC),
    INDEX idx_is_top (is_top, publish_time DESC),
    FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '博客文章表';

-- 3. 文章分类表
CREATE TABLE IF NOT EXISTS blog_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_icon VARCHAR(255) COMMENT '分类图标URL',
    category_description VARCHAR(200) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    article_count INT DEFAULT 0 COMMENT '文章数量',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_category_name (category_name),
    INDEX idx_sort_order (sort_order),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '文章分类表';

-- 4. 文章标签表
CREATE TABLE IF NOT EXISTS blog_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_tag_name (tag_name),
    INDEX idx_use_count (use_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '文章标签表';

-- 初始化分类数据
INSERT INTO blog_category (category_name, category_icon, category_description, sort_order, status) VALUES
('Java', '☕', 'Java技术相关文章', 1, 1),
('Python', '🐍', 'Python技术相关文章', 2, 1),
('前端开发', '💻', '前端开发技术文章', 3, 1),
('数据库', '🗄️', '数据库相关文章', 4, 1),
('算法', '🧮', '算法与数据结构', 5, 1),
('系统设计', '🏗️', '系统架构设计', 6, 1),
('DevOps', '🚀', '运维和部署相关', 7, 1),
('其他', '📝', '其他技术文章', 99, 1)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 初始化热门标签
INSERT INTO blog_tag (tag_name, use_count) VALUES
('Spring Boot', 0),
('Vue', 0),
('React', 0),
('MySQL', 0),
('Redis', 0),
('Docker', 0),
('Kubernetes', 0),
('微服务', 0),
('分布式', 0),
('高并发', 0)
ON DUPLICATE KEY UPDATE update_time = NOW();

