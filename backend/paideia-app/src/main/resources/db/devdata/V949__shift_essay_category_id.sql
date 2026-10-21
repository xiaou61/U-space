-- 为 seed 数据腾出分类 id 空间（WORK-025 回归的修复）。
--
-- **问题**：V24（生产迁移）用 `INSERT ... SELECT` 给「八股文 · 面经真题」建分类，没指定 id，
-- 于是自增分配。在**全新库**上整张表是空的，它拿到 **id = 1**；紧接着 V951 的种子要插入
-- 硬编码的 `id = 1`（「后端基础」），主键冲突：
--   Duplicate entry '1' for key 'knowledge_categories.PRIMARY'
-- 表现是所有需要数据库的测试在**上下文启动阶段**就全红——不是某个用例失败，是 201 个错误。
--
-- **为什么修在这里而不是改 V24**：V24 已在生产库应用且已推送，改它会让 Flyway 校验失败
-- （`MigrationChecksumTest` 也把已应用迁移钉死）。而本文件在 `db/devdata` 下，
-- **生产迁移目录里没有它**，因此生产环境完全不受影响。
--
-- **为什么编号是 V949 而不是 V956**：必须**早于 V951** 执行——冲突发生在 V951 里。
-- 测试 profile 已开 `out-of-order`，所以这个号在已有数据的开发库上也能补跑。
--
-- 做法：把 essay-java 的分类挪到高位 id 900，同步其名下条目引用。
-- 外键是 NO ACTION（不支持级联），顺序必须是：建新行 → 改子表 → 删旧行 → 正名。
--
-- **幂等**：分类不存在、或 900 已被占用时整段自然空转。
-- 900 远离自增区（生产库最大 id 是 20），但仍显式检查，避免把条目指到别人的分类上。

-- ① 建新行，用临时 slug 避开唯一约束
INSERT INTO knowledge_categories (id, parent_id, name, slug, sort_order, created_at, updated_at)
SELECT 900, NULL, name, CONCAT(slug, '-v949-tmp'), sort_order, created_at, updated_at
FROM knowledge_categories
WHERE slug = 'essay-java'
  AND NOT EXISTS (SELECT 1 FROM knowledge_categories WHERE id = 900);

-- ② 子表改指到 900。
--    用派生表包一层：MySQL 不允许 UPDATE 的子查询直接引用被更新的表（错误 1093）。
--    旧 id 由 slug 查得（生产库 20、全新库 1），不写死。
UPDATE knowledge_entries
SET category_id = 900
WHERE category_id = (
    SELECT id FROM (
        SELECT id FROM knowledge_categories WHERE slug = 'essay-java'
    ) AS old_category
);

-- ③ 删旧行（此时已无子表引用）
DELETE FROM knowledge_categories
WHERE slug = 'essay-java'
  AND EXISTS (SELECT 1 FROM (
      SELECT 1 FROM knowledge_categories WHERE slug = 'essay-java-v949-tmp'
  ) AS shifted) ;

-- ④ 新行改回正式 slug
UPDATE knowledge_categories SET slug = 'essay-java' WHERE slug = 'essay-java-v949-tmp';
