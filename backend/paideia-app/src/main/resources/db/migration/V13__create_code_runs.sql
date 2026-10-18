-- 代码执行沙箱的审计表（WORK-015）。
--
-- **刻意不存源码**：只存 SHA-256 与长度。审计要能回答"谁、何时、什么语言、结果如何、
-- 用了多少资源"，但不能还原用户写的东西——它不是内容存储，是运行记录。
--
-- 为什么需要这张表而不只是日志：
-- ① 配额按天计数要查它（`user_id + created_at` 索引）；
-- ② 出问题时能回答"这个人今天跑了多少次、是不是都在超时"；
-- ③ 注销账号时要能一并删掉（隐私口径与其它用户数据一致）。
--
-- 不用库的 CURRENT_TIMESTAMP：库会话时区是 +08:00，应用按 UTC 算（V6 起的约定），
-- 应用每次写自己带值。

CREATE TABLE code_runs (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id      BIGINT UNSIGNED NOT NULL,
  -- 知识库条目 id。**不建外键**：与本项目其它跨模块引用一致，由应用层保证
  entry_id     BIGINT UNSIGNED NULL,
  case_id      VARCHAR(64)     NULL,
  language     VARCHAR(16)     NOT NULL,
  -- ok / compile_error / runtime_error / timeout / out_of_memory / rejected / disabled
  status       VARCHAR(24)     NOT NULL,
  exit_code    INT             NULL,
  duration_ms  INT             NOT NULL DEFAULT 0,
  output_bytes INT             NOT NULL DEFAULT 0,
  truncated    TINYINT(1)      NOT NULL DEFAULT 0,
  -- 源码指纹：能判断"是不是同一份代码反复跑"，但不可还原
  code_hash    CHAR(64)        NOT NULL,
  code_length  INT             NOT NULL,
  created_at   DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_code_runs_user_created (user_id, created_at),
  KEY idx_code_runs_case (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
