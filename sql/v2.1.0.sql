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
