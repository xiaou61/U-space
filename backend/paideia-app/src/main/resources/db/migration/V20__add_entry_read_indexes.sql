-- 条目读路径的两个索引（性能压测结论，2026-10-19）。
--
-- 背景：5000 条已发布条目下，列表接口只有 108 RPS、详情 245 RPS，而分类树/积分等
-- 近乎零成本的接口有 400–530 RPS；压测期间 MySQL 单进程吃到 175% CPU，是整机瓶颈。
--
-- 1. idx_knowledge_entries_published_order
--    列表的 `ORDER BY published_at DESC, id DESC` 原本只能走 idx_knowledge_entries_status，
--    把 5037 行全读出来再 filesort（实测 16.4ms/次）。换成带排序列的索引后同一查询
--    0.156ms，快 105 倍；端到端 10 并发下列表从 118 → 384 RPS。
--    **必须是升序**：写成 DESC 版本会让上一篇/下一篇改走更差的计划（详情 RPS 减半，已实测）。
--
-- 2. idx_knowledge_entries_category_order
--    上一篇/下一篇按 `(published_at, id)` 定位同分类邻居。原先走 idx_knowledge_entries_category
--    再 filesort。这个索引本身解决不了行值比较（见 EntryMapper 里 OR 改写的说明），
--    但配上改写后能直接做范围扫描：3.14ms → 0.17ms。
--
-- 两个索引都以 status 打头：学习者侧的每条查询都写死 `status = 'published'`，
-- 草稿不进索引（数据量大时这部分能省一半以上的索引体积）。

CREATE INDEX idx_knowledge_entries_published_order
  ON knowledge_entries (status, published_at, id);

CREATE INDEX idx_knowledge_entries_category_order
  ON knowledge_entries (status, category_id, published_at, id);
