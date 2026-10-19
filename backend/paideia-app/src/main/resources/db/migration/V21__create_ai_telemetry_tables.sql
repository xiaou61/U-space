-- AI 调用追踪与运营统计（WORK-022）：轮次表与步骤表。
--
-- **编号是 V21 而不是 V20**：V20 已被并发会话的性能索引迁移占用（2026-10-19）。
--
-- 一行一轮（`ai_runs`）+ 一行一步（`ai_run_spans`），不是"一轮一行、步骤塞 JSON"：
-- 工具调用当前是黑盒（`AgentToolBridge` 把异常吞成一段文本），只有步级行能回答
-- "哪个工具失败、哪个慢、一轮调了几次"。塞进 JSON 之后再也聚合不了。
--
-- 三处刻意的取舍：
-- 1. **`kind` / `status` / `error_type` 都是字符串，不用 MySQL 枚举**：加新的 AI 功能是常态，
--    枚举每加一个值都要 DDL；这里的可扩展性比类型严格更值钱。
-- 2. **`attrs` 用 JSON 放冷字段**（历史条数、记忆命中、条目字数、httpTraceId）：
--    加维度不改表结构；真要按它筛时再加生成列。
-- 3. **不存任何正文与提示词**：正文已在 `ai_messages`，靠 `conversation_id` 可还原；
--    prompt 属于 OTel 明确列为 opt-in 的敏感内容。
--
-- **不建跨模块外键**（与 V4/V5/V11 一致），`conversation_id` 只是归属列；
-- 注销级联由 `AiUserDeletionListener` 的清单负责（步骤表没有 user_id，按 trace 子查询删）。
--
-- 时间列全部由应用传值（UTC），不用 DEFAULT CURRENT_TIMESTAMP——项目纪律。

CREATE TABLE ai_runs (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  -- 服务端生成的 32 位十六进制串（与 TraceIdFilter 同一写法）。**不复用客户端传来的
  -- X-Trace-Id**：那个值可被伪造，拿它做关联键等于让客户端决定写哪一行。
  trace_id          VARCHAR(40)     NOT NULL,
  user_id           BIGINT UNSIGNED NOT NULL,
  -- 可选：记忆整理 / 外部问答 / 条目精简这些路径没有对话
  conversation_id   BIGINT UNSIGNED NULL,
  -- chat / memory_extract / interview_review / mcp_ask / resume_import / resume_optimize / entry_summary
  kind              VARCHAR(32)     NOT NULL,
  -- assistant / interview / review / tutor；非对话路径为空
  harness           VARCHAR(32)     NULL,
  model             VARCHAR(64)     NULL,
  prompt_version    VARCHAR(32)     NULL,
  -- running / ok / failed / timeout / cancelled / interrupted
  status            VARCHAR(16)     NOT NULL,
  -- 低基数标识（取自 ErrorCode.name 或归一值），**绝不写异常消息**——那可能含 URL 或响应片段
  error_type        VARCHAR(64)     NULL,
  started_at        DATETIME(3)     NOT NULL,
  ended_at          DATETIME(3)     NULL,
  -- 提交到线程池之前与真正开始之间的等待
  queue_ms          INT             NULL,
  duration_ms       INT             NULL,
  -- 首字延迟：第一个正文增量出现的时刻
  ttft_ms           INT             NULL,
  llm_rounds        SMALLINT        NOT NULL DEFAULT 0,
  tool_calls        SMALLINT        NOT NULL DEFAULT 0,
  input_tokens      INT             NOT NULL DEFAULT 0,
  output_tokens     INT             NOT NULL DEFAULT 0,
  cache_read_tokens INT             NOT NULL DEFAULT 0,
  -- 这一轮实扣多少积分（与积分流水做日级对账）
  credits           INT             NOT NULL DEFAULT 0,
  attrs             JSON            NULL,
  PRIMARY KEY (id),
  -- 唯一键让 `UPDATE ... WHERE trace_id` 与步骤插入互相独立，不需要往返取自增 id
  UNIQUE KEY uk_ai_runs_trace (trace_id),
  KEY idx_ai_runs_user (user_id, started_at),
  KEY idx_ai_runs_conversation (conversation_id, started_at),
  KEY idx_ai_runs_kind (kind, started_at),
  KEY idx_ai_runs_state (status, started_at)
) ENGINE = InnoDB;

CREATE TABLE ai_run_spans (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  -- 用 trace_id 关联而不是自增 run_id：异步写入下拿不到刚插入的 id，
  -- 两次写互相独立，任一次失败不牵连另一次
  run_trace_id      VARCHAR(40)     NOT NULL,
  -- 父步骤的 seq；为空表示顶层
  parent_seq        SMALLINT        NULL,
  seq               SMALLINT        NOT NULL,
  -- llm / tool / retrieval / persist / billing
  kind              VARCHAR(24)     NOT NULL,
  -- 工具名原样，或 `llm#2` 这种轮次标号
  name              VARCHAR(64)     NOT NULL,
  status            VARCHAR(16)     NOT NULL,
  error_type        VARCHAR(64)     NULL,
  started_at        DATETIME(3)     NOT NULL,
  duration_ms       INT             NULL,
  ttft_ms           INT             NULL,
  input_tokens      INT             NOT NULL DEFAULT 0,
  output_tokens     INT             NOT NULL DEFAULT 0,
  cache_read_tokens INT             NOT NULL DEFAULT 0,
  attrs             JSON            NULL,
  PRIMARY KEY (id),
  -- 详情页按 seq 升序取；工具排行按 (kind, name)
  KEY idx_ai_run_spans_run (run_trace_id, seq),
  KEY idx_ai_run_spans_kind (kind, name, started_at)
) ENGINE = InnoDB;
