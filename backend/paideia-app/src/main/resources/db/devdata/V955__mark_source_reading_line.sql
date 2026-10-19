-- 给「源码解读」分类补上学习线标记（WORK-020 的 V17 在空库上跑时这条分类还不存在）。
--
-- V17 里有一句 `UPDATE knowledge_categories SET line_kind = 'reading' WHERE slug = 'source-reading'`。
-- 在生产库上它是对的（分类早已由历史迁移建好），但**在全新的空库上顺序是反的**：
-- V17 < V953，而 `source-reading` 这个分类是 V953 才 INSERT 的 ⇒ 那句 UPDATE 影响 0 行。
-- 于是任何"一人一库"的隔离测试库里，「源码解读」都不是一条线，`linesComeFromCategoryMarks`
-- 与走查的 `/api/v1/learning/lines` 契约快照都会红（2026-10-19 实测）。
--
-- 修法选择：**不动 V17**（它已在线上与本地库应用过，改它会撞校验和守卫，且线上态是对的），
-- 而是在种子里补一条幂等的收尾。生产迁移目录没有本文件，所以线上不受影响。
--
-- 用户裁决 1（2026-09-30）允许 MySQL 专有语法，`UPDATE ... WHERE slug = ?` 天然幂等。

UPDATE knowledge_categories SET line_kind = 'reading' WHERE slug = 'source-reading';
