---
artifact: test-report
work_id: WORK-013
work: AI 记忆的自动提取与控制面
status: passed
evidence: required
verified_commit: 1c1a301
verified_at: 2026-10-17
---

# AI 记忆的自动提取与控制面验证报告

## 验证环境

| 项 | 值 |
| --- | --- |
| 机器 | 本机（Windows / Git Bash），JDK 25、Maven、Node 24 |
| 被验提交 | `70e1b7f`（实现）；其后只有 `.agent/` 工件与本报告 |
| 数据库 | 服务器 MySQL 8.0.46，**公网直连 `36.212.5.130:3306`**（2026-10-16 起，见下）；账号 `paideia_test`，只授权 `paideia_test.*` |
| 跑测试的前置 | `source backend/config/remote-db.local.env`（口令只在被忽略的文件里）。**不再需要 SSH 隧道** |
| 上游模型 | 集成测试**从不真打上游**：`MemoryExtractor` 是 `@MockitoBean` 桩；真上游只被 `AssistantLiveUpstreamIntegrationTest` 用到（未配置时按设计跳过） |

## 验证结果

**通过。** 记忆现在会**自己长**：同一会话攒够 6 轮，回答发给用户之后异步整理一次；写进去的每条都带来源（手动／自动）、理由与出自哪段对话；同一件事说第二次是**覆盖**而不是新增；三个开关各自成立（关掉不召回、不整理、手动写入也被明确拒绝）；「清空并关闭」与「只关不删」是两个动作。

三处机制值得单独记一句：

1. **单飞靠一条条件 UPDATE**（`... AND last_memory_extract_message_id <=> ?`），抢到即推进游标 ⇒ 不需要分布式锁，也天然不会重复整理。**反向验证过**：把 `<=>` 改成 `=`，12 条集成测试红 9 条——因为游标第一次是 NULL，而 `= NULL` 永远不成立。
2. **"空内容"按故障处理**：DeepSeek 会把 `max_tokens` 吃在思考上并**静默返回空**，所以解析器把空/非法 JSON 一律判失败、**整次放弃**（一个事务全有或全无），不当成"这几轮没什么可记的"。
3. **提取花的是用户的积分**：走同一套预扣/结算，流水单独记 `MEMORY_EXTRACT`；余额不足**一个上游请求都不发**（有专门用例），失败也只记日志、绝不影响那一轮回答。

**顺带做了用户要求的一件事**：数据库从 SSH 隧道改成公网直连。**只新建了一个 `paideia_test@'%'` 且只授权 `paideia_test.*`；生产库 `paideia` 没有任何可远程登录的账号**（原先只有 `@127.0.0.1`/`@localhost`，一个都没改）——先查清这一点才动的 `bind-address`。风险与回滚写在 `.agent/notes/dev-workflow.md`。

## 验收矩阵

| 检查项 | 命令 | 退出码 | 结果 | 证据 |
| --- | --- | --- | --- | --- |
| AC-001 自动提取真的发生 | `source backend/config/remote-db.local.env && cd backend && mvn -B -pl paideia-app -am test -Dtest=MemoryAutoExtractionIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false` | 0 | passed | `extractionWritesMemoryWithProvenance`：攒 6 轮 + 触发 → 写入一条，`origin=auto`、`reason` 与 `conversationId` 都在 |
| AC-002 幂等（不膨胀） | 同上 | 0 | passed | `updatingDoesNotDuplicate`：两次同一件事，第二次 `UPDATE` → `ai_memories_text` 仍是 **1 行**，内容被覆盖成最新说法 |
| AC-003 纠错可追溯 | 同上 | 0 | passed | `deleteActionRemovesTheRow`：判定"已不成立"→ 行真的被删；`MemoryCandidateParserTest` 覆盖 `DELETE` 解析（缺 `targetId` 判失败）；理由落库、面板可展开看到 |
| AC-004 失败与欠费都不影响本轮 | 同上 | 0 | passed | `extractorFailureIsSwallowed`（模型抛错 → 不写记忆、游标推进不再重跑）+ `insufficientCreditsSkipsExtraction`（余额 0 → `verify(extractor, times(0))`，一个请求都不发） |
| AC-005 总开关真的关掉 | 同上 | 0 | passed | `disabledMemoryStopsExtractionAndManualWrite`：关闸后不整理（0 次调用）；`POST /memories` → **409 + 业务码 40910**；库 0 行 |
| AC-006 两个轴独立 | 同上 | 0 | passed | `historyAxisIsIndependent`：关掉「参考当前对话的历史消息」后，桩收到的 `history()` 为空 |
| AC-007 清空并关闭 ≠ 只关不删 | 同上 | 0 | passed | `clearingIsSeparateFromDisabling`：只关不删 → 2 条仍在；`DELETE /api/v1/ai/memories` → `cleared:2` + `memoryEnabled:false` + 列表空 |
| AC-008 来源可见 + 分页 | 同上 | 0 | passed | `listSupportsPagingAndProvenance`：`limit/offset` 取到不同页；响应含 `origin`/`reason`，`manual` 与 `auto` 分得开 |
| AC-009 不占用户时间 | 同上 | 0 | passed | `extractionDoesNotBlockTheAnswer`：桩**卡在模型调用里**，SSE 回答仍完整返回（含 `done`），整理在另一个线程里等 |
| AC-010 账对得上 | 同上 | 0 | passed | `extractionIsRecordedInLedger`：流水有 `MEMORY_EXTRACT` 一行且 `input_tokens` 为 1000 |
| T-06 并发只跑一次 | 同上 | 0 | passed | `concurrentTriggersRunOnce`：连续两次 `scheduleIfDue` → `extractor` 只被调用 1 次 |
| 后端全量不回归 | `source ... && cd backend && mvn -B test` | 0 | passed | BUILD SUCCESS；`paideia-app` **130 运行 / 0 失败 / 4 跳过**（跳过的是 `AssistantLiveUpstreamIntegrationTest`，它要真上游）；`paideia-ai` 43 条（含新增 `MemoryCandidateParserTest` 11 条） |
| 注销级联含新表 | 同上（`AccountDeletionCascadeIntegrationTest`） | 0 | passed | 用例逐表断言行数为 0；清单与夹具都加了 `ai_user_settings`（不加这条用例就红） |
| e2e：学习者端 | `source backend/config/remote-db.local.env && cd frontend && pnpm --filter @paideia/app test:e2e` | 0 | passed | **33 passed**（2.1 分钟），全程无隧道 |
| e2e：管理端 | `... pnpm --filter @paideia/admin test:e2e` | 0 | passed | **8 passed**（32 秒） |
| 前端类型与单测 | `cd frontend && pnpm typecheck` / `pnpm test` | 0 | passed | typecheck 全绿；单测 ui 93、assistant 23、knowledge 20、core 22、auth 13、platform-desktop 4 |
| 数据库公网直连可用 | 本机 socket 连 `36.212.5.130:3306`；再跑上面那组集成测试（**隧道已停**） | 0 | passed | 拿到 `8.0.46-0ubuntu0` 握手；集成测试 12/12 通过；线上服务健康检查仍 UP |

## 反向验证（"这条要是坏了，哪个断言会红"）

| 被验行为 | 反着改一处 | 结果 |
| --- | --- | --- |
| 单飞的 `<=>`（游标首次为 NULL） | `last_memory_extract_message_id <=> #{expectedCursor}` 改成 `=` | **12 条集成测试红 9 条**，报"等记忆写入超时"——`= NULL` 永远不成立，第一次整理永远抢不到。改回后 12/12 绿 |
| 解析器的"失败即放弃" | 单测里直接喂空串 / 非 JSON / 缺 `targetId` / id 非正整数 | 每一条都抛业务异常（`MemoryCandidateParserTest` 11 条覆盖）；若改成"尽力而为"，写半条记忆的路径就没了守卫 |
| 开关的短路真的生效 | 关掉总开关后再触发 | `verify(extractor, times(0))` 与 409/40910 两条断言；短路失效必红 |
| 清的确实是库不是内存 | `DELETE` 之后按 SQL 数行 | `SELECT COUNT(*) FROM ai_memories_text` 为 0（不是只看接口返回） |

## 失败与未验证项

1. **前端单测有一次"与 e2e 并发跑"时失败**：`pnpm test` 与 e2e 同时跑时，`packages/ui` 的 `markdown.test.tsx` transform 失败、73 条被跳过（其余 20 条通过）。**单独复跑 93/93 通过**（6 文件）。判断为并发资源问题，不是代码问题；已如实记下，未改测试。
2. **提炼质量没有断言**：模型"该记的没记、记了一堆废话"这类问题，本报告给不出证据——它由提示词与真实使用决定（`memory-extract-v1`）。桩只能证明"我们这边"的行为正确。**这一条需要真实使用几轮之后看**。
3. **`kind` 白名单的边界只验了"越界落全文路"**：`weak_topic`/`goal`/`preference` 谁是"事实"谁是"偏好"由模型判断，没有用例（也不该有——那是提示词的活）。
4. **面板的"来源筛选"只过了前端单测**（"已加载页内筛选"），没有 e2e；`assistant.spec.ts` 里与记忆面板相关的断言（"可逐条删除"、空态文案）都保留并通过。
5. **删除的"动作历史"没做**：`DELETE` 的理由只进日志，库里不留痕（那一行已经没了）。用户看到的是"这条不见了"，看不到"什么时候因为什么被删"。设计里就是这么定的（非目标），记在这里以免被当成缺陷。

## 剩余风险

- **公网 3306 是长期风险面**：扫描器会持续戳它。当前能连的只有那个只碰得到 `paideia_test` 的账号，但"测试库被写坏"是可能的。回滚/收窄办法写在 `.agent/notes/dev-workflow.md`。
- **失败不重跑**：抢到游标即推进 ⇒ 一次提取失败，那一段就不再整理（设计里登记的取舍：重跑要引入重试状态机与"失败要不要计费"一串问题）。若实测"记忆老是漏"，第一个该改的是这里。
- **判重只看最近 40 条**：更早的记忆不参与判重，可能出现跨时间重复。`ponytail:` 天花板已标在代码里。
- **提示词成本**：默认 N=6（摊到每轮 +约 13%）。若用户嫌"整理太频繁花钱"，把 `paideia.ai.memory.rounds-per-extraction` 调大即可；调成 0/负数 = 关掉自动整理（留的后门，不需要改代码）。
- **与 WORK-010 的边界**：本项只做"对话里的自动提取"；"自评 → 结构化记忆"那座桥仍属 WORK-010，谁后实施谁负责对齐覆盖式更新的语义。

## 2026-10-17 重新核对（结算）

`MemoryAutoExtractionIntegrationTest` 12/0、`MemoryCandidateParserTest` 11/0、`ConversationWindowTest` 5/0、`ConversationUsageAccumulationTest` 4/0；`@paideia/assistant` 24/0（含记忆面板 7 条）。锚点之后只有测试文件变化（`assistant.spec.ts`、`knowledge.spec.ts`、新增 `conversation-panel.test.tsx`），记忆提取与控制面的实现未被改动。
