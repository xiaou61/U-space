-- ====================================
-- 在线简历模块数据库脚本 v1.6.0
-- ====================================

-- 1. 简历模板表
CREATE TABLE IF NOT EXISTS resume_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    category VARCHAR(50) COMMENT '模板分类（前端/后端/全栈等）',
    description VARCHAR(500) COMMENT '模板简介',
    cover_url VARCHAR(255) COMMENT '封面地址',
    preview_url VARCHAR(255) COMMENT '预览地址',
    tags VARCHAR(255) COMMENT '标签，逗号分隔',
    tech_stack VARCHAR(255) COMMENT '推荐技术栈',
    experience_level TINYINT DEFAULT 1 COMMENT '经验层级（1-5）',
    rating DECIMAL(3,2) DEFAULT 0 COMMENT '评分',
    rating_count INT DEFAULT 0 COMMENT '评分次数',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下线 1-启用',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '最后修改人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_experience (experience_level),
    FULLTEXT INDEX ft_template_keyword (name, description, tags) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历模板表';

-- 2. 简历主体信息表
CREATE TABLE IF NOT EXISTS resume_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resume_name VARCHAR(120) NOT NULL COMMENT '简历名称',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    summary VARCHAR(500) COMMENT '个人概述',
    status TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-发布',
    visibility TINYINT DEFAULT 0 COMMENT '可见性：0-私密 1-公开',
    version INT DEFAULT 1 COMMENT '版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_template (template_id),
    INDEX idx_update_time (update_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历主体信息表';

-- 3. 简历模块内容表
CREATE TABLE IF NOT EXISTS resume_sections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模块ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    section_type VARCHAR(32) COMMENT '模块类型（PROFILE/WORK等）',
    title VARCHAR(120) NOT NULL COMMENT '模块标题',
    content MEDIUMTEXT COMMENT '模块内容',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_resume_id (resume_id),
    INDEX idx_sort (resume_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历模块内容表';

-- 4. 简历版本快照表
CREATE TABLE IF NOT EXISTS resume_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    version_number INT NOT NULL COMMENT '版本号',
    snapshot LONGTEXT COMMENT '快照数据（JSON）',
    change_log VARCHAR(255) COMMENT '变更说明',
    created_by BIGINT COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_resume_version (resume_id, version_number DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历版本快照表';

-- 5. 简历分享记录表
CREATE TABLE IF NOT EXISTS resume_shares (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分享ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    share_code VARCHAR(64) COMMENT '分享口令',
    share_url VARCHAR(255) COMMENT '分享链接',
    status TINYINT DEFAULT 1 COMMENT '状态：0-关闭 1-启用',
    access_count INT DEFAULT 0 COMMENT '访问次数',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_share_resume (resume_id, status),
    INDEX idx_share_code (share_code),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历分享记录表';

-- 6. 简历访问统计表
CREATE TABLE IF NOT EXISTS resume_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    resume_id BIGINT NOT NULL UNIQUE COMMENT '简历ID',
    view_count BIGINT DEFAULT 0 COMMENT '浏览次数',
    export_count BIGINT DEFAULT 0 COMMENT '导出次数',
    share_count BIGINT DEFAULT 0 COMMENT '分享次数',
    unique_visitors BIGINT DEFAULT 0 COMMENT '访客数',
    last_access_time DATETIME COMMENT '最近访问时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_update_time (update_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历访问统计表';

