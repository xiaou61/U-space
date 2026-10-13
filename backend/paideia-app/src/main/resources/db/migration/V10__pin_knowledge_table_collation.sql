-- 把知识库五张表的字符集与排序规则显式钉死（与 V2 对账号表做的是同一件事）。
--
-- V3 的注释声称"与库一致（utf8mb4 / utf8mb4_0900_ai_ci）"，但建表语句没写 CHARSET/COLLATE，
-- 实际取值取决于目标实例的默认值：默认不是 utf8mb4 时中文标题/正文/分类名会乱码甚至写入失败；
-- 默认排序规则大小写敏感时 slug/name 的唯一性语义会静默漂移且不报错。
--
-- 用新迁移而不是改 V3：V3 已在各环境执行过，改它会触发 Flyway 校验和不匹配。
-- 表都很小，这次重建的成本可以忽略。

ALTER TABLE knowledge_categories CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE knowledge_entries CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE knowledge_entry_relations CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE knowledge_files CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE knowledge_self_assessments CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
