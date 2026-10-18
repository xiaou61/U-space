-- MCP 外部接入的长期凭据（WORK-019）
--
-- 为什么不是 JWT：access token 15 分钟、refresh 单次轮换，MCP 客户端把凭据写在配置里，
-- 两个都放不住。PAT 是这类客户端唯一能用的东西。
--
-- 只存哈希与展示前缀：明文只在创建响应里出现一次。查表用 token_hash 的唯一索引，
-- 校验用恒定时间比较——即便库里被读到，也拿不到可用的令牌。

CREATE TABLE mcp_access_tokens (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id      BIGINT UNSIGNED NOT NULL,
  name         VARCHAR(64)     NOT NULL,
  -- 展示用（如 pd_mcp_3f9a2c1b），**不是**凭据的一部分，泄露它不能登录
  token_prefix VARCHAR(16)     NOT NULL,
  -- sha256(完整 token) 的十六进制；唯一索引兼作校验查找
  token_hash   CHAR(64)        NOT NULL,
  -- "read" / "read,write"，与 ToolScope 的 key 一致
  scopes       VARCHAR(64)     NOT NULL,
  -- NULL = 永不过期（创建时可选）
  expires_at   DATETIME(3)     NULL,
  last_used_at DATETIME(3)     NULL,
  revoked_at   DATETIME(3)     NULL,
  created_at   DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mcp_token_hash (token_hash),
  KEY idx_mcp_token_user (user_id, created_at)
) ENGINE = InnoDB;
