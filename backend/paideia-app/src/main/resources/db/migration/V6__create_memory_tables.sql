-- 助手记忆：结构化 + 全文两路（WORK-006 TASK-004，用户裁决 2：混合）。
--
-- 两路各存各的，不合成一张表：学习状态类（哪个条目懂／不懂、偏好）是**可以直接筛**的事实，
-- 「说过的话」是**只能靠检索**找回来的文本。合成一路会让两种召回退化成同一个含糊的东西。
--
-- 与 V4/V5 一致：**不建跨模块外键**，级联删除由应用层按清单负责（见 design 的注销一节）。
-- 全部属于用户私有数据（用户裁决 3：记忆严格私有），查询一律带 user_id。

CREATE TABLE ai_memories_structured (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL,
  -- 记忆的种类，由写入方给（如理解程度、偏好）。用字符串而不是数据库枚举：
  -- 种类会随功能增加，改枚举要迁移，加一处常量即可。
  kind       VARCHAR(32)     NOT NULL,
  -- 可选的指向对象（例如条目 id）。对不上就当没有，不因此删记忆。
  ref_id     BIGINT UNSIGNED NULL,
  content    VARCHAR(512)    NOT NULL,
  created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ai_memory_structured_user (user_id, created_at)
) ENGINE = InnoDB;

CREATE TABLE ai_memories_text (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL,
  -- 一条一句，便于"逐条查看与删除"（用户裁决 13）。
  content    VARCHAR(1024)   NOT NULL,
  created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ai_memory_text_user (user_id),
  -- 与知识库同一套：默认分词器按空格切词，中文没有空格，必须 ngram。
  -- 代价是单字查询在索引上永远召回为零，所以检索要按 SearchQuery 的判定退回 LIKE。
  FULLTEXT KEY ft_ai_memory_text (content) WITH PARSER ngram
) ENGINE = InnoDB;
