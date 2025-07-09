CREATE TABLE `u_bbs_category` (
                                `id` VARCHAR(32) PRIMARY KEY COMMENT '分类ID，主键 UUID',
                                `name` VARCHAR(64) NOT NULL COMMENT '分类名称，如 学习、生活、娱乐等',
                                `status` TINYINT DEFAULT 1 COMMENT '分类状态，1启用 0禁用',
                                `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标志，0正常 1已删除',
                                `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='帖子分类表';
CREATE TABLE `u_bbs_post` (
                              `id` VARCHAR(32) PRIMARY KEY COMMENT '帖子ID，UUID',
                              `user_id` VARCHAR(32) NOT NULL COMMENT '发布人ID，关联用户表',
                              `category_id` VARCHAR(32) NOT NULL COMMENT '帖子分类ID，关联 u_bbs_category 表',
                              `title` VARCHAR(255) NOT NULL COMMENT '帖子标题',
                              `content` TEXT NOT NULL COMMENT '帖子内容',
                              `images` JSON COMMENT '帖子图片URL数组，JSON格式',
                              `view_count` INT DEFAULT 0 COMMENT '浏览数',
                              `like_count` INT DEFAULT 0 COMMENT '点赞数',
                              `comment_count` INT DEFAULT 0 COMMENT '评论数',
                              `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0否 1是',
                              `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐：0否 1是（由后台定时任务更新）',
                              `status` TINYINT DEFAULT 1 COMMENT '帖子状态：1正常，0禁用',
                              `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常，1已删除',
                              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              FOREIGN KEY (`category_id`) REFERENCES `u_bbs_category`(`id`)
) COMMENT='帖子表：记录校园BBS的帖子内容，关联帖子分类';


CREATE TABLE `u_bbs_post_like` (
                                   `id` VARCHAR(32) PRIMARY KEY COMMENT '点赞记录ID，UUID',
                                   `post_id` VARCHAR(32) NOT NULL COMMENT '帖子ID，关联 u_bbs_post 表',
                                   `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID，点赞人，关联用户表',
                                   `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
                                   `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   UNIQUE KEY `uniq_post_user` (`post_id`, `user_id`),
                                   FOREIGN KEY (`post_id`) REFERENCES `u_bbs_post`(`id`) ON DELETE CASCADE,
                                   INDEX `idx_user_id` (`user_id`)
) COMMENT='帖子点赞表：记录用户对帖子的点赞/取消点赞行为';



CREATE TABLE `u_bbs_comment` (
                                 `id` VARCHAR(32) PRIMARY KEY COMMENT '评论ID，UUID',
                                 `post_id` VARCHAR(32) NOT NULL COMMENT '所属帖子ID',
                                 `user_id` VARCHAR(32) NOT NULL COMMENT '评论用户ID',
                                 `content` TEXT NOT NULL COMMENT '评论内容',
                                 `like_count` INT DEFAULT 0 COMMENT '点赞数',
                                 `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常，1删除',
                                 `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 INDEX (`post_id`)
) COMMENT='帖子一级评论表';

CREATE TABLE `u_bbs_comment_reply` (
                                       `id` VARCHAR(32) PRIMARY KEY COMMENT '回复ID，UUID',
                                       `comment_id` VARCHAR(32) NOT NULL COMMENT '所属评论ID',
                                       `post_id` VARCHAR(32) NOT NULL COMMENT '所属帖子ID，便于反查',
                                       `user_id` VARCHAR(32) NOT NULL COMMENT '回复人ID',
                                       `reply_user_id` VARCHAR(32) DEFAULT NULL COMMENT '被回复的用户ID（@）',
                                       `content` TEXT NOT NULL COMMENT '回复内容',
                                       `like_count` INT DEFAULT 0 COMMENT '点赞数',
                                       `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常，1删除',
                                       `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       INDEX (`comment_id`),
                                       INDEX (`post_id`)
) COMMENT='评论回复表（二级评论）';

