-- AI 记忆的自动提取与控制面（WORK-013 T-01）。三处改动，每处都有一个明确用途：
--
-- ① 两路记忆表加"来源"与"出处"：面板要能回答"它为什么在这里"（REQ-010）。自动整理写的那条
--    万一错了，得能回看它出自哪段对话——这是"用可撤销换自动"的一部分。
--  `source` 默认 'manual'：存量行因此**不需要回填**，自然归为"用户手动让它记的"。
-- ② 新增 ai_user_settings：三个开关（总开关 + 两个独立轴），一人一行。
-- ③ 会话加游标：自动整理是**增量**的——"已经整理到哪条消息为止"靠它，也是并发单飞的判据。
--
-- 与 V4/V5/V6 一致：**不建跨模块外键**，注销级联由应用层的表清单负责（见 AiUserDeletionListener）。
-- `updated_at` 不用库的 CURRENT_TIMESTAMP：库会话时区是 +08:00，而应用按 UTC 算（V6 的约定），
-- 默认值会让同一列出现两种时区的时间。应用每次写自己带值。

ALTER TABLE ai_memories_structured
  ADD COLUMN source          VARCHAR(16)     NOT NULL DEFAULT 'manual',
  ADD COLUMN conversation_id BIGINT UNSIGNED NULL,
  ADD COLUMN ref_message_id  BIGINT UNSIGNED NULL,
  ADD COLUMN reason          VARCHAR(255)    NULL;

ALTER TABLE ai_memories_text
  ADD COLUMN source          VARCHAR(16)     NOT NULL DEFAULT 'manual',
  ADD COLUMN conversation_id BIGINT UNSIGNED NULL,
  ADD COLUMN ref_message_id  BIGINT UNSIGNED NULL,
  ADD COLUMN reason          VARCHAR(255)    NULL;

CREATE TABLE ai_user_settings (
  user_id                  BIGINT UNSIGNED NOT NULL,
  -- 总开关：关掉后不召回、不自动整理，手动写入也被拒（REQ-007）。
  memory_enabled           TINYINT(1)      NOT NULL DEFAULT 1,
  -- 独立轴①：是否使用已保存的记忆（只管"读"，不管"写"）。
  reference_saved_memories TINYINT(1)      NOT NULL DEFAULT 1,
  -- 独立轴②：是否把当前对话的历史消息作为参考（关掉 = 只回答本轮这一句）。
  reference_chat_history   TINYINT(1)      NOT NULL DEFAULT 1,
  updated_at               DATETIME(3)     NOT NULL,
  PRIMARY KEY (user_id)
) ENGINE = InnoDB;

-- 自动整理"已经整理到哪条消息为止"。放会话上而不是用户上：增量天然是"某个会话里从此处往后"
-- 的一段，放用户上会让多个会话互相抢游标。
ALTER TABLE ai_conversations
  ADD COLUMN last_memory_extract_message_id BIGINT UNSIGNED NULL;
