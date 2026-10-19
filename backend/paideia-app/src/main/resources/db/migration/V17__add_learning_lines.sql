-- 学习线（WORK-020）：让「一条有序的线」成为知识库的一个投影，而不是第二套内容系统。
--
-- 两条列都可空，**存量行为零变化**：
-- * knowledge_categories.line_kind 非空 = 这个分类是一条学习线。线名、slug、顺序沿用分类自己的
--   name / slug / sort_order，所以不另建 learning_lines 表（多一张表就多一处要同步的真相）。
-- * knowledge_entries.sort_order 声明站的位置；为 NULL 时调用方退回既有的 (published_at, id)。
--
-- source-reading 已由 WORK-014 落地，它本来就是一条线（有 learning-path、有进度），
-- 这里只给它打标记。分类不存在时这条 UPDATE 影响 0 行，安全。

ALTER TABLE knowledge_categories
  ADD COLUMN line_kind VARCHAR(16) NULL AFTER sort_order;

CREATE INDEX idx_knowledge_categories_line ON knowledge_categories (line_kind);

ALTER TABLE knowledge_entries
  ADD COLUMN sort_order INT NULL AFTER category_id;

CREATE INDEX idx_knowledge_entries_line_order ON knowledge_entries (category_id, sort_order);

UPDATE knowledge_categories SET line_kind = 'reading' WHERE slug = 'source-reading';
