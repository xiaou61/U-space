-- 一轮交互的完整输入与输出（WORK-030 阶段 B）。
--
-- 为什么单开一张表而不是塞进 `ai_run_spans.attrs`：一次对话的 prompt 含历史消息、召回的记忆、
-- 条目正文，几十 KB 起步；`attrs` 是 JSON 列，那表又是写入最频繁的一张，塞进去会拖慢它。
-- 分开之后两件事各有自己的保留期：spans 留 90 天（账目级），payload 留 30 天（诊断级临时证据）。
--
-- 只记 **LLM 步**：工具步的入参与返回已经在 `ai_run_spans.attrs` 里（截断后的），
-- 而工具步没有"发给模型的一整包消息"这回事。
--
-- **敏感内容不入库**：API Key / Authorization 头在 HTTP 头里、不在 body，本表只存 body 部分。
-- 但工具返回可能包含用户私有笔记片段——这是用户明确要的（U-16），不是遗漏。
CREATE TABLE ai_run_payloads (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  -- 与 ai_run_spans 同一关联键：异步写入下拿不到刚插入的自增 id
  run_trace_id   VARCHAR(40)     NOT NULL,
  -- 对应 ai_run_spans.seq（第几轮），点开某一步时按它对上
  seq            SMALLINT        NOT NULL,
  -- 实际发给上游的 messages / tools / 采样参数（JSON）。超上限时为 NULL，见 truncated
  request_body   MEDIUMTEXT      NULL,
  -- 模型返回的完整正文与工具调用（JSON）
  response_body  MEDIUMTEXT      NULL,
  -- 原始字节数：即使正文被占位符替代，也留得下"原本多大"
  request_bytes  INT             NOT NULL DEFAULT 0,
  response_bytes INT             NOT NULL DEFAULT 0,
  -- 1 = 内容超上限（>1MB），正文被占位记录替代。**不静默丢**：
  -- 静默丢会让"这一轮为什么没有请求体"变成一个新的谜
  truncated      TINYINT(1)      NOT NULL DEFAULT 0,
  created_at     DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  -- 一次运行里每一步至多一条；重试或重复投递不会产生第二行
  UNIQUE KEY uk_ai_run_payloads (run_trace_id, seq),
  -- 清理按创建时间切片删
  KEY idx_ai_run_payloads_created (created_at)
) ENGINE = InnoDB;
