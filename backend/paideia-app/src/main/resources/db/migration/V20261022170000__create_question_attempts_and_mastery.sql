-- 题目知识点关联、作答留痕与掌握状态（WORK-033）。
--
-- **不建题目表**：题目仍只存在于条目正文的 quiz 围栏里，题干/选项/答案是唯一权威。
-- 这里只存「身份 + 证据 + 状态」：身份由 entry_id + block_key + question_key + fingerprint 组成，
-- fingerprint 随题干/选项/答案变化，内容一改旧统计就不会套到新题上。
--
-- **question_key 是 blockKey#index**，与正文解析一致；不带题干，避免两份真相。
--
-- **掌握状态落库而题目难度也落库**：BKT 是顺序更新，必须存状态；Elo 需要跨会话累积。
-- 两者都以「最后一次写入」为准，并发作答的简化写在 design.md。
--
-- 只建结构、不插业务数据（MigrationSeedOverlapTest 守着）。

CREATE TABLE question_attempts (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id       BIGINT UNSIGNED NOT NULL,
  entry_id      BIGINT UNSIGNED NOT NULL,
  block_key     VARCHAR(64)     NOT NULL,
  question_key  VARCHAR(128)    NOT NULL,
  -- 题干 + 选项 + 答案的 SHA-256；内容变了就不再指向同一道题
  fingerprint   CHAR(64)        NOT NULL,
  -- 选了第几个选项；填空/排序题暂不落这一列
  choice_index  INT             NULL,
  correct       TINYINT(1)      NOT NULL,
  latency_ms    INT             NULL,
  -- server = 服务端按正文判定；client = 客户端判定后上报（代码挑战的 run 题型）
  judged_by     VARCHAR(16)     NOT NULL,
  created_at    DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_attempt_user_time (user_id, created_at),
  KEY idx_attempt_fingerprint (fingerprint)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE question_concepts (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  entry_id      BIGINT UNSIGNED NOT NULL,
  block_key     VARCHAR(64)     NOT NULL,
  question_key  VARCHAR(128)    NOT NULL,
  concept_id    BIGINT UNSIGNED NOT NULL,
  -- 一题多概念时的证据权重；一期都用 1.00
  weight        DECIMAL(3, 2)   NOT NULL DEFAULT 1.00,
  -- author 人工确认 / ai 模型候选 / inferred 由行为推断
  source        VARCHAR(16)     NOT NULL,
  confidence    DECIMAL(3, 2)   NULL,
  created_at    DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_question_concept (entry_id, block_key, question_key, concept_id),
  KEY idx_question_concept_concept (concept_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE concept_mastery_state (
  user_id       BIGINT UNSIGNED NOT NULL,
  concept_id    BIGINT UNSIGNED NOT NULL,
  -- BKT 的后验掌握概率
  mastery       DECIMAL(5, 4)   NOT NULL,
  attempts      INT             NOT NULL,
  correct       INT             NOT NULL,
  updated_at    DATETIME(3)     NOT NULL,
  PRIMARY KEY (user_id, concept_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE question_difficulty (
  -- 主键就是题目身份；内容改了自然产生新行
  fingerprint   CHAR(64)        NOT NULL,
  entry_id      BIGINT UNSIGNED NOT NULL,
  block_key     VARCHAR(64)     NOT NULL,
  question_key  VARCHAR(128)    NOT NULL,
  -- Elo 评分，初始 1200
  rating        DECIMAL(6, 2)   NOT NULL,
  attempts      INT             NOT NULL,
  correct       INT             NOT NULL,
  updated_at    DATETIME(3)     NOT NULL,
  PRIMARY KEY (fingerprint),
  KEY idx_difficulty_question (entry_id, block_key, question_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
