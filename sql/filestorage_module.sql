-- 文件存储模块数据库表结构
-- 版本: v1.0.0
-- 创建时间: 2024-12-19

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `stored_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT '文件大小(字节)',
  `content_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  `md5_hash` varchar(32) NOT NULL COMMENT '文件MD5校验值',
  `module_name` varchar(50) NOT NULL COMMENT '所属模块名称',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `upload_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `access_url` varchar(500) DEFAULT NULL COMMENT '访问URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '文件状态: 0=已删除, 1=正常',
  `is_public` tinyint NOT NULL DEFAULT '0' COMMENT '是否公开访问: 0=私有, 1=公开',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_md5` (`md5_hash`),
  KEY `idx_module_business` (`module_name`, `business_type`),
  KEY `idx_upload_time` (`upload_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- 存储配置表
DROP TABLE IF EXISTS `storage_config`;
CREATE TABLE `storage_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `storage_type` varchar(20) NOT NULL COMMENT '存储类型: LOCAL,OSS,COS,KODO,OBS',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `config_params` text NOT NULL COMMENT '配置参数JSON',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认存储: 0=否, 1=是',
  `is_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用: 0=禁用, 1=启用',
  `test_status` tinyint DEFAULT NULL COMMENT '测试状态: 0=失败, 1=成功, NULL=未测试',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_name` (`config_name`),
  KEY `idx_storage_type` (`storage_type`),
  KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储配置表';

-- 文件存储记录表
DROP TABLE IF EXISTS `file_storage`;
CREATE TABLE `file_storage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '存储记录ID',
  `file_id` bigint NOT NULL COMMENT '文件ID',
  `storage_config_id` bigint NOT NULL COMMENT '存储配置ID',
  `storage_path` varchar(500) NOT NULL COMMENT '存储路径',
  `is_primary` tinyint NOT NULL DEFAULT '1' COMMENT '是否主存储: 0=备份, 1=主存储',
  `sync_status` tinyint NOT NULL DEFAULT '1' COMMENT '同步状态: 0=同步中, 1=已同步, 2=同步失败',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_storage` (`file_id`, `storage_config_id`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_storage_config` (`storage_config_id`),
  KEY `idx_primary` (`is_primary`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件存储记录表';

-- 迁移任务表
DROP TABLE IF EXISTS `file_migration`;
CREATE TABLE `file_migration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '迁移任务ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `source_storage_id` bigint NOT NULL COMMENT '源存储配置ID',
  `target_storage_id` bigint NOT NULL COMMENT '目标存储配置ID',
  `migration_type` varchar(20) NOT NULL COMMENT '迁移类型: FULL,INCREMENTAL,TIME_RANGE,FILE_TYPE',
  `filter_params` text DEFAULT NULL COMMENT '筛选参数JSON',
  `total_files` int NOT NULL DEFAULT '0' COMMENT '总文件数',
  `success_count` int NOT NULL DEFAULT '0' COMMENT '成功数量',
  `fail_count` int NOT NULL DEFAULT '0' COMMENT '失败数量',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态: PENDING,RUNNING,COMPLETED,FAILED,STOPPED',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `error_message` text DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_target` (`source_storage_id`, `target_storage_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件迁移任务表';

-- 文件访问记录表
DROP TABLE IF EXISTS `file_access`;
CREATE TABLE `file_access` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问记录ID',
  `file_id` bigint NOT NULL COMMENT '文件ID',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  `access_ip` varchar(50) DEFAULT NULL COMMENT '访问IP',
  `module_name` varchar(50) NOT NULL COMMENT '访问模块',
  `user_id` bigint DEFAULT NULL COMMENT '访问用户ID',
  `access_type` varchar(20) DEFAULT 'VIEW' COMMENT '访问类型: VIEW,DOWNLOAD',
  PRIMARY KEY (`id`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_access_time` (`access_time`),
  KEY `idx_module` (`module_name`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件访问记录表';

-- 系统设置表
DROP TABLE IF EXISTS `file_system_setting`;
CREATE TABLE `file_system_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `setting_key` varchar(100) NOT NULL COMMENT '设置键',
  `setting_value` text NOT NULL COMMENT '设置值',
  `setting_desc` varchar(255) DEFAULT NULL COMMENT '设置描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件系统设置表';
