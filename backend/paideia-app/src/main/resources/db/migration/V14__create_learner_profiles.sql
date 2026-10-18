-- 学习者身份与求职者画像（WORK-010 T-01）。
--
-- 三件事，各自都有明确的"为什么不是另一种做法"：
--
-- ① **身份只有一列**（不是一张身份表、也不是 `is_current` 标记）。一期只有 `job_seeker` 一个真值，
--    "单选即当前"，多一个布尔列就多一个"两行都标了 current"的非法状态。
-- ② **画像字段全部可空**：它们都是"求职者专用"的，别的身份用不到；加第二个身份时字段归属要重审
--    （登记在 design 的已知缺口，不在这里假装通用）。
-- ③ **不给任何既有表加身份列**（REQ-004/AC-003/AC-004）：记忆、笔记、自评、精简缓存、会话
--    全都**不按身份分区**——"记忆一份全属于你"是 2026-10-15 的裁决。
--
-- 与 V1（users）、V6（记忆）一致：**不建跨模块外键**，注销级联由应用层的表清单负责。
-- `updated_at` 由应用按 UTC 写，不用库的 `ON UPDATE CURRENT_TIMESTAMP`：库会话时区是 +08:00，
-- 默认值会让同一列出现两种时区（2026-10-10 踩过）。

CREATE TABLE learner_profiles (
  user_id     BIGINT UNSIGNED NOT NULL,
  -- 当前身份。一期只有 job_seeker；其余取值只在代码枚举里留位，不做界面。
  identity    VARCHAR(32)     NOT NULL DEFAULT 'job_seeker',
  -- 以下四列是**求职者专用**画像（全部可空）。
  target_role VARCHAR(64)     NULL,
  target_city VARCHAR(64)     NULL,
  -- autumn | spring（秋招 / 春招）
  timeline    VARCHAR(16)     NULL,
  -- applying | written_test | interview
  stage       VARCHAR(16)     NULL,
  updated_at  DATETIME(3)     NOT NULL,
  PRIMARY KEY (user_id)
) ENGINE = InnoDB;

-- 会话的 harness（WORK-010 T-07）：**本项唯一动到的既有表**。
--
-- 为什么必须记：会话在恢复时要保持同一行为。不记的话，用户从通用入口继续一段面试对话，
-- 会**中途换人格**——同一个人突然不按面试官说话了。
--
-- 可空且 NULL = 通用助手：对既有会话就是"行为不变"，不需要任何回填。
ALTER TABLE ai_conversations ADD COLUMN harness VARCHAR(32) NULL;

-- 自评桥的唯一键（WORK-010 T-08）：让"自评 → 结构化记忆"是**覆盖**而不是追加。
--
-- 语义边界（别误读成"改了既有记忆语义"）：
--  * 只对 kind='entry_understanding' 这一条来源有意义；用户点「记住这条」写的是**全文**那一路
--    （另一张表），仍然是追加，不受影响。
--  * MySQL 唯一键里 NULL 不参与去重，所以"没有 ref_id 的结构化记忆"不会被这条约束吞掉。
--
-- 加键前已核过两个库：有 ref_id 的结构化记忆均为 0 行，无冲突（否则要先清理再迁移）。
ALTER TABLE ai_memories_structured
  ADD UNIQUE KEY uk_ai_memory_structured_ref (user_id, kind, ref_id);

-- 面试评估（WORK-010 T-11）。
--
-- 为什么不塞进消息文本："进度与弱项"要能查询，塞进文本等于不可查。
-- 这是"学习行为数据"的第一块真砖（比自评可信）；但本项**不做**基于它的掌握度推断与路径编排。
CREATE TABLE ai_interview_evaluations (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id         BIGINT UNSIGNED NOT NULL,
  conversation_id BIGINT UNSIGNED NOT NULL,
  score_json      JSON            NOT NULL,
  created_at      DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_interview_eval_user (user_id, created_at)
) ENGINE = InnoDB;
