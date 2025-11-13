-- ====================================
-- 代码共享器模块数据库脚本 v1.6.0
-- ====================================

-- 1. 代码作品表
CREATE TABLE code_pen (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '作品ID',
    user_id BIGINT NOT NULL COMMENT '作者用户ID',
    title VARCHAR(200) NOT NULL COMMENT '作品标题',
    description VARCHAR(1000) COMMENT '作品描述',
    html_code LONGTEXT COMMENT 'HTML代码',
    css_code LONGTEXT COMMENT 'CSS代码',
    js_code LONGTEXT COMMENT 'JavaScript代码',
    preview_image VARCHAR(255) COMMENT '预览图URL',
    external_css TEXT COMMENT '外部CSS库链接，JSON数组格式',
    external_js TEXT COMMENT '外部JS库链接，JSON数组格式',
    preprocessor_config TEXT COMMENT '预处理器配置，JSON格式',
    tags VARCHAR(500) COMMENT '标签，JSON数组格式',
    category VARCHAR(50) COMMENT '分类（动画、组件、游戏、工具等）',
    is_public TINYINT DEFAULT 1 COMMENT '可见性：1-公开 0-私密',
    is_free TINYINT DEFAULT 1 COMMENT '是否免费：1-免费 0-付费',
    fork_price INT DEFAULT 0 COMMENT 'Fork价格（积分），0表示免费，1-1000表示付费',
    is_template TINYINT DEFAULT 0 COMMENT '是否系统模板：1-是 0-否',
    status TINYINT DEFAULT 1 COMMENT '状态：0-草稿 1-已发布 2-已下架 3-已删除',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推荐：1-推荐 0-普通',
    recommend_expire_time DATETIME COMMENT '推荐过期时间',
    forked_from BIGINT COMMENT 'Fork来源作品ID',
    fork_count INT DEFAULT 0 COMMENT 'Fork次数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    collect_count INT DEFAULT 0 COMMENT '收藏数',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    points_reward INT DEFAULT 10 COMMENT '创建奖励积分',
    total_income INT DEFAULT 0 COMMENT '累计收益积分（通过付费Fork获得）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    publish_time DATETIME COMMENT '发布时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_status_publish_time (status, publish_time DESC),
    INDEX idx_is_recommend (is_recommend, publish_time DESC),
    INDEX idx_category (category),
    INDEX idx_like_count (like_count DESC),
    INDEX idx_view_count (view_count DESC),
    INDEX idx_is_free (is_free),
    INDEX idx_fork_price (fork_price),
    FULLTEXT INDEX ft_title_description (title, description) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '代码作品表';

-- 2. 作品点赞表
CREATE TABLE code_pen_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
    pen_id BIGINT NOT NULL COMMENT '作品ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    
    UNIQUE KEY uk_pen_user (pen_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_pen_id (pen_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '作品点赞表';

-- 3. 作品收藏表
CREATE TABLE code_pen_collect (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    pen_id BIGINT NOT NULL COMMENT '作品ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    folder_id BIGINT COMMENT '收藏夹ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    
    UNIQUE KEY uk_pen_user (pen_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_pen_id (pen_id),
    INDEX idx_folder_id (folder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '作品收藏表';

-- 4. 收藏夹表
CREATE TABLE code_pen_folder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏夹ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    folder_name VARCHAR(100) NOT NULL COMMENT '收藏夹名称',
    folder_description VARCHAR(500) COMMENT '收藏夹描述',
    collect_count INT DEFAULT 0 COMMENT '收藏数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '收藏夹表';

-- 5. 作品评论表
CREATE TABLE code_pen_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    pen_id BIGINT NOT NULL COMMENT '作品ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT COMMENT '父评论ID（回复）',
    reply_to_user_id BIGINT COMMENT '回复目标用户ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-已隐藏 3-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_pen_id (pen_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '作品评论表';

-- 6. 作品标签表
CREATE TABLE code_pen_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_description VARCHAR(200) COMMENT '标签描述',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_tag_name (tag_name),
    INDEX idx_use_count (use_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '作品标签表';

-- 7. Fork交易记录表
CREATE TABLE code_pen_fork_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易ID',
    original_pen_id BIGINT NOT NULL COMMENT '原作品ID',
    forked_pen_id BIGINT NOT NULL COMMENT 'Fork后的作品ID',
    original_author_id BIGINT NOT NULL COMMENT '原作者ID',
    fork_user_id BIGINT NOT NULL COMMENT 'Fork用户ID',
    fork_price INT DEFAULT 0 COMMENT 'Fork价格（积分），0表示免费',
    transaction_type TINYINT DEFAULT 0 COMMENT '交易类型：0-免费Fork 1-付费Fork',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    
    INDEX idx_original_pen_id (original_pen_id),
    INDEX idx_forked_pen_id (forked_pen_id),
    INDEX idx_original_author_id (original_author_id),
    INDEX idx_fork_user_id (fork_user_id),
    INDEX idx_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Fork交易记录表';

