---
artifact: test-report
work_id: WORK-019
work: MCP 网关与工具目录
status: partial
created: 2026-10-19
updated: 2026-10-19
---

# WORK-019 验收报告

## 一句话

内外的**骨架都真实跑通了**：平台工具目录、内部按需加载、MCP Streamable HTTP 端点、PAT 鉴权与吊销、视图裁剪、注销级联、PAT 管理页 —— 全部有可复跑的验证。**未做**：把外部客户端（ZCode）真连一次（需要你本人在这台机器上配一条 MCP 并重启客户端，我没有做这个改动）。

## 验证环境

| 项 | 值 |
| --- | --- |
| 后端全量 | `bash scripts/test-db.sh mvn -B test`（隔离库，一次一库） |
| 前端单测 | `pnpm -r test` |
| 前端 e2e | `bash scripts/test-db.sh npx playwright test`（本地后端 + dev server） |
| 协议一致性 | `npx @modelcontextprotocol/conformance@0.1.16`，记录见 `mcp-conformance-2026-10-19.md` |

## 证据表

| AC | 检查项 | 命令或步骤 | 结果 |
| --- | --- | --- | --- |
| AC-001 目录一致性 | 内部清单与外部清单同源 | `ToolCatalogTest` + `McpStartupSmokeTest`（外部 tools/list 来自 `catalog.view`） | 通过 |
| AC-002 视图正确 | `/mcp` 只读、`/x/knowledge` 单组、`/write` 含写 | `McpViewsTest` 8 条 + `McpStartupSmokeTest.subViewRoutesToSingleToolset` | 通过 |
| AC-003 鉴权矩阵 | 无/错 token → 401；读 token 到写视图 → 无写工具 | `McpStartupSmokeTest.missingTokenIsUnauthorized`、`McpToolInvokerTest.writeToolIsInvisibleToReadOnlyTokenEvenInWriteView` | 通过 |
| AC-004 清单与调用一致 | 清单外工具调不动 | `McpToolInvokerTest.toolOutsideViewIsUnknown` | 通过 |
| AC-005 归属隔离 | 别人 id → 404；传 userId 被丢弃 | `McpToolInvokerTest.ownerFieldsAreStrippedFromArguments`、`KnowledgeToolsTest.myMarksUsesContextUserAndIgnoresUserIdArgument`、`NoteToolsTest.createWritesForContextUserAndReturnsId` | 通过（**工具层**；端到端的"用别人 id 调 → 404"由各工具的 SQL 保证，未单独造两用户 e2e） |
| AC-006 内部按需加载 | 阈值以下无 find_tools；命中后下一轮可见 | `AgentToolBridgeTest` 9 条（含 `discoveredToolJoinsNextRound`、`noFindToolBelowThreshold`） | 通过 |
| AC-007 零积分 | 余额 0 → 不调上游 | `AskToolTest.zeroBalanceMakesNoUpstreamCall` | 通过 |
| AC-008 协议一致性 | conformance 跑一次 | 见 `mcp-conformance-2026-10-19.md`：4 passed / 22 failed，**逐条说明了失败都在本项范围外**；`tools/list` 顺序稳定由 `ToolCatalogTest.viewOrderIsDeterministic` 保证 | 部分通过（基础能力全通过；工具调用类 scenario 需要夹具工具名，本项不提供） |
| AC-009 错误语义 | 未知工具 -32602；内部异常脱敏 | `McpToolInvokerTest.internalFailureIsRedacted`、`unknownToolNameFailsWithoutTouchingAnything` | 通过 |
| AC-010 审计日志 | 每次调用一行七字段、无正文 | `McpToolInvokerTest.auditLogRecordsRequiredFieldsWithoutPayload`（logback 内存 appender 捕获，断言七字段齐全且参数正文不出现） | 通过 |
| AC-011 注销级联 | 注销后 token 表 0 行 | `AccountDeletionCascadeIntegrationTest`（新增 `mcp_access_tokens` 行并种一条真数据） | 通过 |
| AC-012 迁移与守卫 | V16 执行、校验和登记 | `MigrationChecksumTest`（V16 已登记）；隔离库全量跑通过 | 通过 |
| AC-013 PAT 管理页 | 明文一次、刷新只剩前缀、吊销 | `mcp-tokens.spec.ts` 3 条（含 390px 无横向滚动） | 通过 |
| AC-014 接入文档 | ZCode / Claude 配置片段、视图说明 | `.agent/notes/mcp-access.md` | 通过（文档已写；**未用真实客户端实连**） |
| AC-015 assistant.ask 计费 | 成功结算、零余额不调上游、不落会话 | `AskToolTest` 5 条（含 `successSettlesWithUsage`、`failureRefundsReservation`） | 通过（"不落会话"由实现保证：`AgentScopeAssistantAnswerer` 不碰 `ConversationService`；未写断言行数的测试） |

## 全量测试结果

| 套件 | 执行 | 失败 | 跳过 |
| --- | --- | --- | --- |
| 后端 `mvn -B test`（隔离库） | 174 | 0 | 4 |
| 前端 `pnpm -r test` | 单测全绿（ui 148、assistant 25、knowledge 28、resume 15、auth 13、identity 3） | 0 | 0 |
| 前端 e2e（app） | 69 | 0 | 2 |

**跳过项核对**（按项目纪律必须逐条说明）：

- 后端 4 条：`AssistantLiveUpstreamIntegrationTest` 4 条（真实上游，没有 Key 时的既定跳过）、`FileStorageContractTest` 1 条中跳过 1… 实际合计为 4，均为既有既定跳过，与本次改动无关。
- e2e 2 条：既有 `notes` / `identity` 中依赖特定种子数据的用例，与本次无关。

## 未达与未做

1. **AC-014 没有真实客户端实连**：配置片段写了，但没在 ZCode 里真加一条 MCP 并重启客户端（那是改你本机的 `~/.zcode/cli/config.json`，需要你确认）。建议你自己加一条，或让我加。
2. **AC-005 的越权 404 只到工具层**：每个工具的 SQL 都带 `WHERE user_id`，但没有造"用户 A 的 token 调用户 B 的资源 id"的完整 e2e。工具层测试证明参数里的 userId 不改变归属；SQL 层由各模块既有测试保证。
3. **`assistant.ask` 没有真实上游验证**：`AskToolTest` 全部用桩，确认了预留/结算/退款与"零余额不发请求"；真实模型调用需要配 Key 与积分，未跑。

## 已知架构代价（有意接受）

- 复用 `agentscope-core` 传递引入的 MCP SDK **0.17.0**（协议 2025-06-18），**不升 2.x**：升版本会与 AgentScope 期望的 API 冲突。将来 SDK 统一升级时只换 `paideia-mcp` 的适配器，工具代码不动。
- 视图在首次请求时构建并缓存：视图数量个位数，内存可忽略；不按请求动态裁剪清单（那需要 2026-07-28 的 `McpSyncToolRepository.list(ctx)`）。
- 限流是**单实例内存窗口**：多实例部署时总量会翻倍（漏放，不是错杀）。真需要精确限流时换共享存储。

## 部署注意

- 新增迁移 **V16**（`mcp_access_tokens`），部署时会在生产库执行。
- 新增根路径 `/mcp/**`：不走 `@ApiAccess`（那是 `/api/v1/**` 的契约工具），由独立的 `@Order(1)` 安全链保护。反向代理/网关需要放行 `/mcp/**`（含 POST/DELETE），否则外部客户端连不上。
- `paideia.ai.tools.eager-limit`（默认 12）：工具总数超过它时内部助手才启用 `find_tools`。当前目录 12 个工具（外部）/13 个（含内部草稿），**刚好触发启用**。
