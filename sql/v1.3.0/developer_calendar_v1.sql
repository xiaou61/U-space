-- 程序员日历模块数据库表结构 v1

-- 程序员日历事件表
DROP TABLE IF EXISTS `developer_calendar_event`;
CREATE TABLE `developer_calendar_event` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID',
    `event_name` varchar(100) NOT NULL COMMENT '事件名称',
    `event_date` varchar(10) NOT NULL COMMENT '事件日期(MM-dd格式，用于每年循环)',
    `event_type` tinyint NOT NULL DEFAULT 1 COMMENT '事件类型：1-程序员节日，2-技术纪念日，3-开源节日',
    `description` text COMMENT '事件描述',
    `icon` varchar(50) DEFAULT NULL COMMENT '图标标识',
    `color` varchar(20) DEFAULT '#1890ff' COMMENT '标记颜色',
    `is_major` tinyint NOT NULL DEFAULT 0 COMMENT '是否重要节日：0-普通，1-重要',
    `blessing_text` text COMMENT '节日祝福语',
    `related_links` json COMMENT '相关链接JSON格式',
    `sort_order` int DEFAULT 0 COMMENT '排序值',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_event_date` (`event_date`) USING BTREE,
    INDEX `idx_event_type` (`event_type`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    INDEX `idx_is_major` (`is_major`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='程序员日历事件表';

-- 每日内容表
DROP TABLE IF EXISTS `daily_content`;
CREATE TABLE `daily_content` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '内容ID',
    `content_type` tinyint NOT NULL COMMENT '内容类型：1-编程格言，2-技术小贴士，3-代码片段，4-历史上的今天',
    `title` varchar(200) NOT NULL COMMENT '内容标题',
    `content` text NOT NULL COMMENT '内容正文',
    `author` varchar(100) DEFAULT NULL COMMENT '作者',
    `programming_language` varchar(50) DEFAULT NULL COMMENT '相关编程语言',
    `tags` json COMMENT '标签数组JSON格式',
    `difficulty_level` tinyint DEFAULT NULL COMMENT '难度等级：1-初级，2-中级，3-高级',
    `source_url` varchar(500) DEFAULT NULL COMMENT '来源链接',
    `view_count` int NOT NULL DEFAULT 0 COMMENT '查看次数',
    `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_content_type` (`content_type`) USING BTREE,
    INDEX `idx_programming_language` (`programming_language`) USING BTREE,
    INDEX `idx_difficulty_level` (`difficulty_level`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日内容表';

-- 用户日历偏好设置表
DROP TABLE IF EXISTS `user_calendar_preference`;
CREATE TABLE `user_calendar_preference` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '偏好ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `event_reminder` tinyint NOT NULL DEFAULT 1 COMMENT '事件提醒：0-关闭，1-开启',
    `daily_content_push` tinyint NOT NULL DEFAULT 1 COMMENT '每日内容推送：0-关闭，1-开启',
    `preferred_languages` json COMMENT '偏好编程语言数组JSON格式',
    `preferred_content_types` json COMMENT '偏好内容类型数组JSON格式',
    `difficulty_preference` tinyint DEFAULT 2 COMMENT '难度偏好：1-初级，2-中级，3-高级',
    `notification_time` time DEFAULT '09:00:00' COMMENT '通知时间',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_id` (`user_id`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    CONSTRAINT `fk_user_calendar_preference_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户日历偏好设置表';

-- 用户日历收藏表
DROP TABLE IF EXISTS `user_calendar_collection`;
CREATE TABLE `user_calendar_collection` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `collection_type` tinyint NOT NULL COMMENT '收藏类型：1-事件，2-内容',
    `target_id` bigint NOT NULL COMMENT '目标ID（事件ID或内容ID）',
    `collection_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-取消收藏，1-已收藏',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_type_target` (`user_id`, `collection_type`, `target_id`) USING BTREE,
    INDEX `idx_user_id` (`user_id`) USING BTREE,
    INDEX `idx_collection_type` (`collection_type`) USING BTREE,
    INDEX `idx_target_id` (`target_id`) USING BTREE,
    CONSTRAINT `fk_user_calendar_collection_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户日历收藏表';

-- 插入一些初始数据

-- 程序员节日事件数据
INSERT INTO `developer_calendar_event` (`event_name`, `event_date`, `event_type`, `description`, `icon`, `color`, `is_major`, `blessing_text`) VALUES
('程序员节', '10-24', 1, '1024程序员节，因为1024是2的十次方，是一个特殊的数字，在信息技术中有特殊含义', 'code', '#722ED1', 1, '1024程序员节快乐！愿代码无Bug，愿人生多惊喜！'),
('世界编程日', '09-13', 1, '庆祝编程技术发展，激励更多人投身编程事业', 'terminal', '#13C2C2', 1, '世界编程日快乐！用代码改变世界，用技术创造未来！'),
('Java诞生日', '05-23', 2, '1995年5月23日，Java编程语言正式发布', 'coffee', '#F5A623', 1, '感谢Java为编程世界带来的贡献！Write Once, Run Anywhere!'),
('Python诞生日', '02-20', 2, '1991年2月20日，Python首个版本发布', 'python', '#4CAF50', 1, 'Python让编程变得简单而优雅！人生苦短，我用Python！'),
('GitHub上线日', '04-10', 3, '2008年4月10日，GitHub正式上线，改变了开源协作方式', 'github', '#24292E', 1, '感谢GitHub让全世界开发者能够协作共建！'),
('Linux诞生日', '08-25', 3, '1991年8月25日，Linus首次发布Linux内核', 'linux', '#FD7E14', 1, '致敬Linux！开源改变世界，自由万岁！'),
('JavaScript诞生日', '05-05', 2, '1995年5月，JavaScript在Netscape浏览器中首次亮相', 'javascript', '#F7DF1E', 0, 'JavaScript让Web变得生动有趣！'),
('C语言诞生日', '01-01', 2, '1972年，C语言在贝尔实验室诞生，影响深远的编程语言', 'c', '#00599C', 1, 'C语言是现代编程的基石！'),
('Git诞生日', '04-07', 3, '2005年4月7日，Linus创建了Git版本控制系统', 'git', '#F05032', 0, 'Git让代码版本管理变得简单高效！'),
('Stack Overflow上线日', '09-15', 3, '2008年9月15日，程序员问答社区Stack Overflow上线', 'stackoverflow', '#F47F24', 0, '感谢Stack Overflow帮助无数程序员解决问题！');

-- 每日内容数据
INSERT INTO `daily_content` (`content_type`, `title`, `content`, `author`, `programming_language`, `tags`, `difficulty_level`) VALUES
(1, '简洁胜过复杂', '简单胜过复杂，复杂胜过混乱。', 'Tim Peters', 'Python', '["设计原则", "Python之禅"]', 1),
(1, '代码如诗', '代码应该写得像诗一样优美，让人读起来赏心悦目。', '佚名', NULL, '["代码质量", "编程哲学"]', 1),
(2, '使用有意义的变量名', '变量名应该清楚地表达其用途，避免使用a、b、c这样的无意义名称。', NULL, NULL, '["代码规范", "可读性"]', 1),
(2, '遵循单一职责原则', '一个函数或类应该只有一个引起变化的原因，专注于做好一件事。', NULL, NULL, '["设计模式", "SOLID原则"]', 2),
(3, 'Python列表推导式', '# 使用列表推导式生成平方数\nsquares = [x**2 for x in range(10)]\nprint(squares)  # [0, 1, 4, 9, 16, 25, 36, 49, 64, 81]', NULL, 'Python', '["Python", "列表推导式"]', 1),
(3, 'JavaScript箭头函数', '// ES6箭头函数简化代码\nconst add = (a, b) => a + b;\nconst multiply = (a, b) => a * b;\nconsole.log(add(2, 3)); // 5', NULL, 'JavaScript', '["JavaScript", "ES6", "箭头函数"]', 1),
(4, '第一台电子计算机诞生', '1946年2月14日，世界上第一台通用电子计算机ENIAC在美国宾夕法尼亚大学诞生。', NULL, NULL, '["计算机历史", "ENIAC"]', 1),
(4, '万维网诞生', '1989年3月12日，蒂姆·伯纳斯-李提出了万维网(World Wide Web)的构想。', 'Tim Berners-Lee', NULL, '["互联网历史", "WWW"]', 1);
