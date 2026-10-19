-- 学习记录（WORK-020）：一个用户在一站（知识库条目）上的学习状态。
--
-- 三处刻意的取舍：
-- * 分数存 JSON 而不是一张作答明细表。本项只消费"这一站各块的最高分"；逐次作答历史
--   属于将来的掌握度/错题本，现在建等于先写一张没有读者的表。
-- * 不建到 users 的外键：与 knowledge_self_assessments 同口径，注销级联由
--   AccountDeletionService 的清单负责（那里有集成测试盯着，漏一张就红）。
-- * review_due_at 由应用按 UTC 计算后写入，不用 NOW()。

CREATE TABLE learning_progress (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id         BIGINT UNSIGNED NOT NULL,
  entry_id        BIGINT UNSIGNED NOT NULL,
  -- 最远走读步骤；只是"接着读"的提示，不参与通过判定
  tour_step       INT             NOT NULL DEFAULT 0,
  -- {"<blockId>":{"score":8,"total":10}}；两个块类型各一份
  challenges_json JSON            NOT NULL,
  quizzes_json    JSON            NOT NULL,
  -- in_progress | passed；**只由服务端按分数算**
  status          VARCHAR(16)     NOT NULL DEFAULT 'in_progress',
  passed_at       DATETIME(3)     NULL,
  -- 复习阶梯档位 0..3，对应 1/3/7/21 天
  review_stage    TINYINT         NOT NULL DEFAULT 0,
  review_due_at   DATETIME(3)     NULL,
  created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_learning_progress (user_id, entry_id),
  KEY idx_learning_progress_user_due (user_id, review_due_at),
  KEY idx_learning_progress_entry (entry_id),
  CONSTRAINT fk_learning_progress_entry FOREIGN KEY (entry_id)
    REFERENCES knowledge_entries (id) ON DELETE CASCADE
) ENGINE = InnoDB;
