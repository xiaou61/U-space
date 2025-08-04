DROP TABLE IF EXISTS `u_class_course`;
CREATE TABLE `u_class_course`  (
                                   `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'id',
                                   `class_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联的班级id',
                                   `course_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联的课程id',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;


CREATE TABLE `u_word` (
                        `id` VARCHAR(32) PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                        `word` VARCHAR(100) NOT NULL COMMENT '单词',
                        `phonetic` VARCHAR(100) DEFAULT NULL COMMENT '音标',
                        `part_of_speech` VARCHAR(50) DEFAULT NULL COMMENT '词性，例如 n. v. adj.',
                        `definition` TEXT COMMENT '中文释义',
                        `definition_en` TEXT COMMENT '英文释义',
                        `example_sentence` TEXT COMMENT '例句',
                        `source` VARCHAR(100) DEFAULT NULL COMMENT '词库来源，如 CET-4、GRE',
                        `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔，如 常考,高频',
                        `difficulty` TINYINT DEFAULT 1 COMMENT '难度等级：1-5',
                        `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
                        `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        UNIQUE KEY `uk_word_source` (`word`, `source`)  -- 避免重复
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词表';


CREATE TABLE `u_user_word_record` (
                                    `id` varchar(32) PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                                    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
                                    `word_id` varchar(32) NOT NULL COMMENT '单词ID',
                                    `know_count` INT DEFAULT 0 COMMENT '认识次数',
                                    `not_know_count` INT DEFAULT 0 COMMENT '不认识次数',
                                    `last_study_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后学习时间',
                                    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户单词学习记录表';
