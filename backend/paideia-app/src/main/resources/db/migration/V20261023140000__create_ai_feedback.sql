-- 用户对助手回答的反馈（WORK-036 C 组）。
--
-- 一条助手回答可能被改判（先踩后赞），所以独立成表而不是给 ai_messages 加列：
-- 加列只能存最后一次，且要改那张热表的写入路径。
--
-- uk_ai_feedback_user_trace 让"同一用户对同一轮的重复提交"变成覆盖（ON DUPLICATE KEY UPDATE），
-- 与 WORK-013 的重复抑制口径一致。**trace_id 为空时唯一键退化成不生效**（MySQL 允许多个 NULL）：
-- 关闭采集时本来就没有轮次可挂，留多条是可以接受的。
--
-- reason 只收四个枚举，不收自由文本：那会把用户输入变成需要审核、需要转义展示的内容。
-- 时间列由应用写入（UTC），不用 DEFAULT CURRENT_TIMESTAMP。

CREATE TABLE ai_feedback (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id         BIGINT UNSIGNED NOT NULL,
  conversation_id BIGINT UNSIGNED NULL,
  message_id      BIGINT UNSIGNED NULL,
  trace_id        VARCHAR(40)     NULL,
  kind            VARCHAR(32)     NULL,
  rating          VARCHAR(8)      NOT NULL,
  reason          VARCHAR(24)     NULL,
  created_at      DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_feedback_user_trace (user_id, trace_id),
  KEY idx_ai_feedback_created (created_at),
  KEY idx_ai_feedback_rating (rating, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
