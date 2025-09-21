-- ============================
-- 版本更新历史表
-- ============================
DROP TABLE IF EXISTS `version_history`;
CREATE TABLE `version_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `version_number` VARCHAR(50) NOT NULL COMMENT '版本号',
    `title` VARCHAR(200) NOT NULL COMMENT '更新标题',
    `update_type` TINYINT NOT NULL DEFAULT 1 COMMENT '更新类型: 1-重大更新, 2-功能更新, 3-修复更新, 4-其他',
    `description` VARCHAR(500) COMMENT '版本更新简要描述',
    `prd_url` VARCHAR(500) COMMENT 'PRD文档链接',
    `release_time` DATETIME NOT NULL COMMENT '发布时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发布, 2-已隐藏',
    `sort_order` INT DEFAULT 0 COMMENT '排序权重(数字越大越靠前)',
    `view_count` INT DEFAULT 0 COMMENT '查看次数',
    `is_featured` TINYINT DEFAULT 0 COMMENT '是否重点推荐: 0-否, 1-是',
    `created_by` BIGINT COMMENT '创建人ID',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人ID',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    
    INDEX `idx_version_number` (`version_number`),
    INDEX `idx_release_time` (`release_time`),
    INDEX `idx_status` (`status`),
    INDEX `idx_update_type` (`update_type`),
    INDEX `idx_sort_order` (`sort_order`),
    INDEX `idx_created_time` (`created_time`),
    UNIQUE KEY `uk_version_number` (`version_number`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='版本更新历史表';
