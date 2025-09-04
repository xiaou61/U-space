-- ===========================
-- 帖子分类管理表
-- ===========================
DROP TABLE IF EXISTS `community_category`;
CREATE TABLE `community_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类描述',
  `sort_order` int DEFAULT 0 COMMENT '排序顺序，数字越小越靠前',
  `status` tinyint DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `post_count` int DEFAULT 0 COMMENT '该分类下的帖子数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creator_id` bigint DEFAULT NULL COMMENT '创建者ID（管理员）',
  `creator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建者名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`) USING BTREE,
  KEY `idx_status_sort` (`status`,`sort_order`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子分类表' ROW_FORMAT=Dynamic;

-- 插入默认分类数据
INSERT INTO `community_category` (`name`, `description`, `sort_order`, `status`, `creator_id`, `creator_name`) VALUES
('技术分享', '分享技术心得、学习笔记、项目经验', 1, 1, 1, 'admin'),
('问题求助', '遇到问题寻求帮助，技术难题讨论', 2, 1, 1, 'admin'),
('项目展示', '展示个人项目、作品集、开源项目', 3, 1, 1, 'admin'),
('学习心得', '学习过程中的感悟、总结、经验分享', 4, 1, 1, 'admin'),
('面试经验', '面试题目、面试技巧、求职心得', 5, 1, 1, 'admin'),
('行业动态', '技术趋势、行业新闻、工具推荐', 6, 1, 1, 'admin'),
('其他讨论', '其他技术相关话题讨论', 99, 1, 1, 'admin'); 