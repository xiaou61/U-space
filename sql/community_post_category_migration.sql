-- ===========================
-- community_post表分类字段优化
-- 将category字段(varchar)改为category_id字段(bigint)关联community_category表
-- ===========================

-- 步骤1: 备份现有数据中的category字段值
CREATE TABLE IF NOT EXISTS `community_post_category_backup` AS
SELECT id, category FROM community_post WHERE category IS NOT NULL;

-- 步骤2: 添加新的category_id字段
ALTER TABLE `community_post` ADD COLUMN `category_id` bigint DEFAULT NULL COMMENT '分类ID' AFTER `content`;

-- 步骤3: 数据迁移 - 根据category名称找到对应的category_id
UPDATE community_post cp 
LEFT JOIN community_category cc ON cp.category = cc.name 
SET cp.category_id = cc.id 
WHERE cp.category IS NOT NULL AND cc.id IS NOT NULL;

-- 步骤4: 处理没有匹配到分类的数据，设置为"其他讨论"
UPDATE community_post cp 
SET cp.category_id = (SELECT id FROM community_category WHERE name = '其他讨论' LIMIT 1)
WHERE cp.category IS NOT NULL AND cp.category_id IS NULL;

-- 步骤5: 删除旧的category字段
ALTER TABLE `community_post` DROP COLUMN `category`;

-- 步骤6: 添加外键约束
ALTER TABLE `community_post` ADD CONSTRAINT `fk_post_category` 
FOREIGN KEY (`category_id`) REFERENCES `community_category` (`id`) 
ON DELETE SET NULL ON UPDATE RESTRICT;

-- 步骤7: 添加索引
ALTER TABLE `community_post` ADD INDEX `idx_category_id` (`category_id`); 