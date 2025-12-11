-- =============================================
-- 添加 category 字段到 flashcard_deck 表
-- 版本: v1.6.3
-- 创建时间: 2025-12-11
-- =============================================

-- 添加 category 字段（分类标识，如 java, spring 等）
ALTER TABLE flashcard_deck 
ADD COLUMN category VARCHAR(50) DEFAULT NULL COMMENT '分类标识(java,spring等)' 
AFTER category_id;
