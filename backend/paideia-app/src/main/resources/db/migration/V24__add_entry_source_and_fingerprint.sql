-- 八股文专区（WORK-025）：给条目加「来源」与「指纹」两列。
--
-- **为什么不新建表**：抓来的内容仍然是 knowledge_entries——markdown 渲染、quiz/code-challenge
-- 块、全文检索、自评与复习排期全部现成，另建一套等于把四件事各写一遍。这里只补两列，
-- 让"这条是从外面抓来的"这件事可表达。
--
-- 两列的用途：
--   * source_url  —— 「面经真题」分类下展示"回到原帖"。平台自产内容（平台精讲）为 NULL。
--   * fingerprint —— 题目指纹（规范化后的题目文本哈希）。**去重靠它，不靠标题**：
--                    同一道题会在不同面经里反复出现（"HashMap 为什么线程不安全"每天都有），
--                    按帖子去重会天天新增一条重复内容。
--
-- 可空 + 不加唯一约束的理由：
--   * 存量条目（管理端手抄、课程导入）两列都是 NULL，行为零变化。
--   * 唯一约束建在可空列上时，MySQL 允许多个 NULL，但那会让"指纹为空"的重复检测语义
--     依赖数据库而不是代码；这里选择在写入路径上判（先查后插），并且**同一分类内**才判重——
--     将来若有别的来源也复用指纹，不会互相挡住。

ALTER TABLE knowledge_entries
  ADD COLUMN source_url VARCHAR(512) NULL AFTER body;

ALTER TABLE knowledge_entries
  ADD COLUMN fingerprint VARCHAR(64) NULL AFTER source_url;

-- 判重查询按 (category_id, fingerprint) 走，这个索引就是给它用的。
CREATE INDEX idx_knowledge_entries_fingerprint
  ON knowledge_entries (category_id, fingerprint);

-- 「八股文专区」的分类（WORK-025）。
--
-- 抓取任务按 **slug** 定位分类（配置项 `paideia.ai.essay.category-slug`），不按名字——
-- 名字可能被管理员改，slug 是稳定标识。
--
-- **幂等**：用 INSERT ... SELECT ... WHERE NOT EXISTS 而不是 INSERT IGNORE。
-- 前者在分类已存在时插入 0 行，语义明确；后者会吞掉所有错误（包括"名字超长"这类真问题）。
--
-- 两个分类的关系（D-05）：本项一期只建「面经真题」一个。原本计划再建「平台精讲」（平台按大纲
-- 自产），但那部分没有任何内容来源，先不建空分类占位——空分类出现在导航里比没有更糟。
INSERT INTO knowledge_categories (name, slug, sort_order)
SELECT '八股文 · 面经真题', 'essay-java', 50
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM knowledge_categories WHERE slug = 'essay-java');
