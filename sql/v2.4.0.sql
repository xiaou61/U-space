-- =============================================
-- U-Space 2.4.0 版本数据库升级脚本
-- 活动管理与积分系统
-- =============================================

-- 1. 积分类型表
DROP TABLE IF EXISTS `u_points_type`;
CREATE TABLE `u_points_type` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `type_name` VARCHAR(50) NOT NULL COMMENT '积分类型名称',
  `type_code` VARCHAR(20) NOT NULL COMMENT '积分类型编码',
  `description` VARCHAR(200) COMMENT '积分类型描述',
  `icon_url` VARCHAR(200) COMMENT '积分图标URL',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用(0:禁用 1:启用)',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`type_code`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分类型表';

-- 2. 活动表
DROP TABLE IF EXISTS `u_activity`;
CREATE TABLE `u_activity` (
  `id` BIGINT AUTO_INCREMENT COMMENT '活动ID',
  `title` VARCHAR(100) NOT NULL COMMENT '活动标题',
  `description` TEXT COMMENT '活动描述',
  `cover_image` VARCHAR(200) COMMENT '活动封面图片URL',
  `max_participants` INT NOT NULL DEFAULT 0 COMMENT '最大参与人数',
  `current_participants` INT NOT NULL DEFAULT 0 COMMENT '当前参与人数',
  `points_type_id` BIGINT NOT NULL COMMENT '积分类型ID',
  `points_amount` INT NOT NULL DEFAULT 0 COMMENT '奖励积分数量',
  `activity_type` TINYINT(1) DEFAULT 1 COMMENT '活动类型(1:抢夺型 2:签到型 3:任务型)',
  `status` TINYINT(1) DEFAULT 0 COMMENT '活动状态(0:待开始 1:进行中 2:已结束 3:已取消)',
  `start_time` DATETIME NOT NULL COMMENT '活动开始时间',
  `end_time` DATETIME NOT NULL COMMENT '活动结束时间',
  `rules` TEXT COMMENT '活动规则说明',
  `create_user_id` VARCHAR(64) NOT NULL COMMENT '创建人用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除(0:未删除 1:已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_end_time` (`start_time`, `end_time`),
  KEY `idx_points_type` (`points_type_id`),
  KEY `idx_create_user` (`create_user_id`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_u_activity_points_type` FOREIGN KEY (`points_type_id`) REFERENCES `u_points_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 3. 活动参与记录表
DROP TABLE IF EXISTS `u_activity_participant`;
CREATE TABLE `u_activity_participant` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `participate_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '参与时间',
  `status` TINYINT(1) DEFAULT 0 COMMENT '参与状态(0:参与成功 1:已取消 2:违规取消)',
  `participant_rank` INT COMMENT '参与排名(抢夺活动中的顺序)',
  `remark` VARCHAR(200) COMMENT '备注信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_participate_time` (`participate_time`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_u_participant_activity` FOREIGN KEY (`activity_id`) REFERENCES `u_activity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动参与记录表';

-- 4. 积分发放记录表
DROP TABLE IF EXISTS `u_points_record`;
CREATE TABLE `u_points_record` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `activity_id` BIGINT COMMENT '活动ID',
  `points_type_id` BIGINT NOT NULL COMMENT '积分类型ID',
  `points_amount` INT NOT NULL COMMENT '积分数量',
  `operation_type` TINYINT(1) DEFAULT 1 COMMENT '操作类型(1:获得 2:扣除)',
  `status` TINYINT(1) DEFAULT 0 COMMENT '发放状态(0:待发放 1:已发放 2:发放失败 3:已撤销)',
  `grant_time` DATETIME COMMENT '实际发放时间',
  `fail_reason` VARCHAR(200) COMMENT '失败原因',
  `batch_no` VARCHAR(32) COMMENT '批量发放批次号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `uk_user_activity` (`user_id`, `activity_id`),
  KEY `idx_points_type` (`points_type_id`),
  KEY `idx_status` (`status`),
  KEY `idx_grant_time` (`grant_time`),
  KEY `idx_batch_no` (`batch_no`),
  CONSTRAINT `fk_u_points_activity` FOREIGN KEY (`activity_id`) REFERENCES `u_activity` (`id`),
  CONSTRAINT `fk_u_points_type_id` FOREIGN KEY (`points_type_id`) REFERENCES `u_points_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分发放记录表';

-- 5. 用户积分余额表
DROP TABLE IF EXISTS `u_user_points`;
CREATE TABLE `u_user_points` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `points_type_id` BIGINT NOT NULL COMMENT '积分类型ID',
  `current_balance` INT NOT NULL DEFAULT 0 COMMENT '当前积分余额',
  `total_earned` INT NOT NULL DEFAULT 0 COMMENT '累计获得积分',
  `total_spent` INT NOT NULL DEFAULT 0 COMMENT '累计消费积分',
  `frozen_points` INT NOT NULL DEFAULT 0 COMMENT '冻结积分（待发放的）',
  `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_points_type` (`user_id`, `points_type_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_points_type` (`points_type_id`),
  CONSTRAINT `fk_u_user_points_type` FOREIGN KEY (`points_type_id`) REFERENCES `u_points_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分余额表';

-- 6. 初始化积分类型数据
INSERT INTO `u_points_type` (`type_name`, `type_code`, `description`, `icon_url`, `is_active`, `sort_order`) VALUES
('学习积分', 'STUDY_POINTS', '通过参与学习活动获得的积分，可用于兑换学习资源', '/icons/study.png', 1, 1),
('活动积分', 'ACTIVITY_POINTS', '通过参与校园活动获得的积分，可用于兑换活动奖品', '/icons/activity.png', 1, 2),
('志愿积分', 'VOLUNTEER_POINTS', '通过参与志愿服务获得的积分，体现社会责任感', '/icons/volunteer.png', 1, 3),
('创新积分', 'INNOVATION_POINTS', '通过创新创业项目获得的积分，鼓励创新思维', '/icons/innovation.png', 1, 4),
('通用积分', 'GENERAL_POINTS', '通用积分，可用于各种兑换和消费', '/icons/general.png', 1, 5);

-- 完成提示
SELECT '=== U-Space 2.4.0 数据库升级完成 ===' AS message;
SELECT '表创建完成：u_points_type, u_activity, u_activity_participant, u_points_record, u_user_points' AS tables;
SELECT '初始化数据完成：5种积分类型' AS init_data;


