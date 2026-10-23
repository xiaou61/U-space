-- 复习会话（WORK-034 S2）。
--
-- items_json 含答案与解析，**绝不下发答案**：响应由服务端裁剪成 itemId/题干/选项。
-- 不建到 knowledge_* 的外键，注销由 KnowledgeUserDeletionListener 的表清单负责。
-- 时间列由应用写入（UTC），不用 DEFAULT CURRENT_TIMESTAMP。

CREATE TABLE review_sessions (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id           BIGINT UNSIGNED NOT NULL,
  concept_ids_json  JSON            NOT NULL,
  items_json        JSON            NOT NULL,
  answers_json      JSON            NULL,
  created_at        DATETIME(3)     NOT NULL,
  expires_at        DATETIME(3)     NOT NULL,
  completed_at      DATETIME(3)     NULL,
  PRIMARY KEY (id),
  KEY idx_review_session_user (user_id, completed_at, expires_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
