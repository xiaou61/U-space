-- 凭据级工具组白名单（2026-10-22）
--
-- 起因：用户说「MCP 接入这里可以控制的权限太少了，应该要全部都」。
-- 实情是权限只按 read/write 两档切，工具组（note / knowledge / assessment /
-- learning / account / assistant）只能靠改 URL 路径挑，界面上没有任何入口。
--
-- 用可空列而不是新表：判空即可兼容旧凭据——
--   NULL   = 不限制工具组（与本次改动前的语义完全一致，现有凭据不受影响）
--   非空   = 逗号分隔的工具组白名单
-- 所以这里**不需要回填**，也不必停服。
ALTER TABLE mcp_access_tokens
  ADD COLUMN toolsets VARCHAR(255) NULL COMMENT '工具组白名单，逗号分隔；NULL = 不限制' AFTER scopes;
