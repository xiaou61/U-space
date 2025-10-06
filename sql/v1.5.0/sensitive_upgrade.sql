-- ========================================
-- 敏感词过滤系统 v2.0.0 升级脚本
-- ========================================

-- 1. 新增白名单表
CREATE TABLE IF NOT EXISTS `sensitive_whitelist` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `word` VARCHAR(255) NOT NULL COMMENT '白名单词汇',
    `category` VARCHAR(50) COMMENT '分类（专业术语/成语/人名/品牌等）',
    `reason` VARCHAR(500) COMMENT '加入白名单的原因',
    `scope` VARCHAR(50) DEFAULT 'global' COMMENT '作用范围（global-全局/module-模块级）',
    `module_name` VARCHAR(50) COMMENT '模块名称（scope=module时有效）',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `creator_id` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_word_scope` (`word`, `scope`, `module_name`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词白名单表';

-- 2. 新增策略配置表
CREATE TABLE IF NOT EXISTS `sensitive_strategy` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `strategy_name` VARCHAR(100) NOT NULL COMMENT '策略名称',
    `module` VARCHAR(50) NOT NULL COMMENT '业务模块（community/interview/moment等）',
    `level` TINYINT NOT NULL COMMENT '风险等级 1-低 2-中 3-高',
    `action` VARCHAR(20) NOT NULL COMMENT '处理动作（replace/reject/warn）',
    `notify_admin` TINYINT DEFAULT 0 COMMENT '是否通知管理员 0-否 1-是',
    `limit_user` TINYINT DEFAULT 0 COMMENT '是否限制用户 0-否 1-是',
    `limit_duration` INT COMMENT '限制时长（分钟）',
    `description` VARCHAR(500) COMMENT '策略描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_module_level` (`module`, `level`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词处理策略表';

-- 3. 新增词库来源表
CREATE TABLE IF NOT EXISTS `sensitive_source` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `source_name` VARCHAR(100) NOT NULL COMMENT '词库来源名称',
    `source_type` VARCHAR(20) NOT NULL COMMENT '来源类型（local/api/github）',
    `api_url` VARCHAR(500) COMMENT 'API地址',
    `api_key` VARCHAR(255) COMMENT 'API密钥',
    `sync_interval` INT DEFAULT 24 COMMENT '同步间隔（小时）',
    `last_sync_time` DATETIME COMMENT '最后同步时间',
    `sync_status` TINYINT COMMENT '同步状态 0-失败 1-成功',
    `word_count` INT DEFAULT 0 COMMENT '词汇数量',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词来源管理表';

-- 4. 新增版本历史表
CREATE TABLE IF NOT EXISTS `sensitive_version` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `version_no` VARCHAR(50) NOT NULL COMMENT '版本号（如v1.0.1）',
    `change_type` VARCHAR(20) COMMENT '变更类型（add/update/delete/import）',
    `change_count` INT COMMENT '变更数量',
    `change_detail` JSON COMMENT '变更详情',
    `operator_id` BIGINT COMMENT '操作人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词版本历史表';

-- 5. 新增同音字映射表
CREATE TABLE IF NOT EXISTS `sensitive_homophone` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `original_char` VARCHAR(10) NOT NULL COMMENT '原始字符',
    `homophone_chars` VARCHAR(100) NOT NULL COMMENT '同音字（逗号分隔）',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_original` (`original_char`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同音字映射表';

-- 6. 新增形似字映射表
CREATE TABLE IF NOT EXISTS `sensitive_similar_char` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `original_char` VARCHAR(10) NOT NULL COMMENT '原始字符',
    `similar_chars` VARCHAR(100) NOT NULL COMMENT '形似字（逗号分隔）',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_original` (`original_char`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='形似字映射表';

-- 7. 新增命中统计表
CREATE TABLE IF NOT EXISTS `sensitive_hit_statistics` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `word` VARCHAR(255) NOT NULL COMMENT '敏感词',
    `category_id` INT COMMENT '分类ID',
    `hit_count` INT DEFAULT 0 COMMENT '命中次数',
    `module` VARCHAR(50) COMMENT '业务模块',
    UNIQUE KEY `uk_date_word_module` (`stat_date`, `word`, `module`),
    INDEX `idx_date` (`stat_date`),
    INDEX `idx_hit_count` (`hit_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词命中统计表';

-- 8. 新增用户违规统计表
CREATE TABLE IF NOT EXISTS `sensitive_user_violation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `violation_count` INT DEFAULT 0 COMMENT '违规次数',
    `last_violation_time` DATETIME COMMENT '最后违规时间',
    `is_restricted` TINYINT DEFAULT 0 COMMENT '是否被限制 0-否 1-是',
    `restrict_end_time` DATETIME COMMENT '限制结束时间',
    UNIQUE KEY `uk_user_date` (`user_id`, `stat_date`),
    INDEX `idx_violation_count` (`violation_count` DESC),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户违规统计表';

-- 9. 修改现有敏感词表结构
ALTER TABLE `sensitive_word`
ADD COLUMN `word_type` TINYINT DEFAULT 1 COMMENT '词类型 1-普通词 2-正则表达式' AFTER `word`,
ADD COLUMN `pinyin` VARCHAR(500) COMMENT '拼音（用于拼音检测）' AFTER `word_type`,
ADD COLUMN `enable_variant_check` TINYINT DEFAULT 1 COMMENT '启用变形词检测 0-否 1-是' AFTER `action`,
ADD COLUMN `remark` VARCHAR(500) COMMENT '备注' AFTER `creator_id`,
ADD INDEX `idx_word_type` (`word_type`),
ADD INDEX `idx_pinyin` (`pinyin`(100));

-- 10. 初始化默认策略
INSERT INTO `sensitive_strategy` (`strategy_name`, `module`, `level`, `action`, `notify_admin`, `limit_user`, `limit_duration`, `description`) VALUES
('社区低风险', 'community', 1, 'replace', 0, 0, NULL, '社区低风险敏感词-替换处理'),
('社区中风险', 'community', 2, 'replace', 1, 0, NULL, '社区中风险敏感词-替换处理并通知管理员'),
('社区高风险', 'community', 3, 'reject', 1, 1, 60, '社区高风险敏感词-拒绝发布并禁言1小时'),
('面试低风险', 'interview', 1, 'replace', 0, 0, NULL, '面试低风险敏感词-替换处理'),
('面试中风险', 'interview', 2, 'replace', 1, 0, NULL, '面试中风险敏感词-替换处理并通知管理员'),
('面试高风险', 'interview', 3, 'reject', 1, 1, 1440, '面试高风险敏感词-拒绝发布并禁言24小时'),
('朋友圈低风险', 'moment', 1, 'replace', 0, 0, NULL, '朋友圈低风险敏感词-替换处理'),
('朋友圈中风险', 'moment', 2, 'replace', 1, 0, NULL, '朋友圈中风险敏感词-替换处理并通知管理员'),
('朋友圈高风险', 'moment', 3, 'reject', 1, 1, 60, '朋友圈高风险敏感词-拒绝发布并禁言1小时');

-- 11. 初始化同音字映射（示例数据）
INSERT INTO `sensitive_homophone` (`original_char`, `homophone_chars`) VALUES
('傻', '沙,煞,啥'),
('逼', '比,币,鄙'),
('操', '草,曹,槽'),
('妈', '马,码'),
('你', '泥,拟');

-- 12. 初始化形似字映射（示例数据）
INSERT INTO `sensitive_similar_char` (`original_char`, `similar_chars`) VALUES
('草', '艹,屮'),
('日', '曰,囗'),
('毒', '毐'),
('爆', '暴');

-- ========================================
-- 升级完成
-- ========================================
