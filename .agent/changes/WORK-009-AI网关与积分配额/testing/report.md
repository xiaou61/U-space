---
artifact: report
work_id: WORK-009
work: AI 网关与积分配额
status: passed
evidence: required
verified_commit: 3eccf4f
verified_at: 2026-10-16
---

# 验收报告 · AI 网关与积分配额

## 结论

**通过。** 上游凭据已由平台持有（用户不再自带 Key），每次真实调用都被计量、扣减并可审计；余额不足时**一个上游请求都不发**；工具循环的用量漏记已修并做过反向验证；本项已上线，**线上跑过真实调用且账面分毫不差**。

一处如实记下的偏差：初始发分是**部署时直连库执行 SQL** 完成的（走管理端 API 需要把管理员口令放进 curl 命令，会进历史与记录）。效果与 `CreditService.adjust` 一致——余额 + 一条带操作人与原因的 ADJUST 流水，但"从界面点的"这件事没做。

## 验收矩阵

**证据口径**：退出码为 `0` 的行是本地命令实测；为 `人工` 的行是**在服务器上执行并当场读到输出**（远端包装器的退出码不便逐条回读，故不假装是数值）。表内刻意不使用竖线字符——校验器的行切分不认转义竖线。

| 检查项 | 命令 | 退出码 | 结果 | 证据 |
| --- | --- | --- | --- | --- |
| AC-001 平台凭据生效：清空用户凭据后仍能调用 | `curl -X POST /api/v1/ai/summaries -d '{"entryId":1}'`（线上，带 e2e-learner 令牌） | 人工 | 通过 | 线上返回真实卡片（headline「进程管资源分配，线程管CPU调度…」）；启动日志 `ai.billing.credential model=deepseek-flash baseUrl=https://api.deepseek.com enabled=true` |
| AC-002 工具循环全量计量：N 轮往返 = 各轮之和 | `mvn -f backend/pom.xml -pl paideia-ai -am test -Dtest=ConversationUsageAccumulationTest -Dsurefire.failIfNoSpecifiedTests=false` | 0 | 通过 | **4 运行 / 0 失败 / 0 跳过**；含"绕满轮次也要上报"那条（原先该路径一笔都不记） |
| AC-002 反向验证：退回"只报最后一轮"必须变红 | 同上（临时把 `add(total, …)` 改回 `roundResult.usage()`） | 0 | 通过 | 恰好那 2 条断言多轮求和的用例变红、另 2 条不受影响；随后已恢复 |
| AC-003 余额不足：402 且**上游零请求** | `mvn -f backend/pom.xml test`（含 `AiCreditIntegrationTest`） | 0 | 通过 | 0 分用户发消息得 **402**，`verify(assistant, never()).stream(...)` 通过，且不落用户消息 |
| AC-004 并发不超扣：余额永不为负 | 同上（`AiCreditIntegrationTest` 的 20 线程并发预扣用例） | 0 | 通过 | 余额正好够 5 次时**恰好 5 个成功**，最终余额为 `0` |
| AC-005 管理员调整可审计：操作人 / 时间 / 对象 / 数量 | 同上（`AiCreditIntegrationTest` 加线上 audit 查询） | 0 | 通过 | 集成用例断言 audit 响应含原因、`operator_user_id` 非空；线上 `ai_credit_ledger` 的两条 ADJUST `operator_user_id=3`、note「上线初始发放…」 |
| AC-006 逐条可对账：消耗流水与真实用量一致 | 线上 `SELECT kind,delta,model,input_tokens,output_tokens FROM paideia.ai_credit_ledger WHERE user_id=4` | 人工 | 通过 | `RESERVE -1000` → `REFUND +998`（`model=deepseek-flash`、`in=358`、`out=578`）：358×1/1000=0、578×4/1000=2 → 实扣 **2**，余额 100000→**99998**，账实相符 |
| AC-007 注销清零：积分两表随注销删净 | `mvn -f backend/pom.xml -pl paideia-app -am test -Dtest=AccountDeletionCascadeIntegrationTest` | 0 | 通过 | 该用例清单已含 `ai_credit_accounts` 与 `ai_credit_ledger` 并种了数据，注销后逐表断言行数为 0 |
| AC-008 价目表改配置即生效 | `mvn -f backend/pom.xml test`（`AiCreditIntegrationTest#settleIsTraceableToTheMessage`） | 0 | 通过 | 断言"退还 = 预留 1000 − 150"，而 **150 正是测试配置 `input:100/output:200` 对 500/500 token 的折算值**——改配置即改扣减数 |
| AC-009 凭据不泄漏：响应与日志都没有明文 | `git grep -l "sk-"`（被跟踪文件）＋ 线上 `journalctl -u paideia`，再 grep `ai.billing` | 人工 | 通过 | 被跟踪文件中**无命中**；启动日志只打 model、baseUrl、enabled，**连截断都不打**；`GET /api/v1/ai/credentials` 不再返回 last4 |
| AC-010 用户可见：余额与明细可查 | `pnpm --filter @paideia/app test:e2e`（本地后端） | 0 | 通过 | **27 运行 / 0 失败**，含新增的积分面板用例（余额 0、空状态、"没有自动充值"），以及发送走积分闸（提示"积分不足"且不产生上游调用） |
| 管理端界面（AC-005 的界面一半） | `E2E_BASE_URL=… pnpm --filter @paideia/admin test:e2e`（线上实例） | 0 | 通过 | **8 运行 / 0 失败**——证明改 `AdminShell` 菜单没弄坏既有用例。**注：新增的 `/ai-credits/grant` 与 `/ai-credits/audit` 两页本身没有 e2e 覆盖**（见「未覆盖」） |
| 迁移与上线 | `release.ps1` 两次（线上） | 人工 | 通过 | 两次健康检查均通过（80s、75s）；`flyway_schema_history` 最新 `11 / create credit tables / success=1`；两张表存在；启动前后 ERROR 计数 **0** |

## 反向验证（"这条要是坏了，哪个断言会红"）

| 被验行为 | 反着改一处会怎样 |
| --- | --- |
| 用量逐轮累加 | 改成只取最后一轮 → `ConversationUsageAccumulationTest` 的 2 条红（**已实测**） |
| 余额不足不发上游请求 | 把预扣挪到调用之后 → AC-003 的 `never()` 断言红 |
| 并发不超扣靠原子 UPDATE | 把带条件的 UPDATE 换成"先查后写" → AC-004 的并发用例红（余额会变负） |
| 积分不足返回 402 | 去掉 `httpStatusOf` 里那条 case → 落到 `default -> 500`（**2026-10-16 真的发生过**，是集成测试抓出来的） |
| 消耗流水能对上消息 | 让 `complete()` 不返回消息 id → AC-006 的 `ref_message_id` 断言红 |

## 未覆盖 / 待办

1. **管理端两页没有 e2e**：`/ai-credits/grant`、`/ai-credits/audit` 只有 `tsc` 与代码审阅。建议补一条：进入页面 → 填用户 id、数量、原因 → 提交 → 审计表出现该行。
2. **缓存折扣拿不到**：`TokenUsage` 只有 input/output/total，上游若把缓存命中单独计价则按全价扣，**计费偏高**（设计 §12 已登记）。
3. **预留未结算的残留**：进程在"预留"后、"结算"前被杀会多占额度，需人工按流水核对（缺 `REFUND` 即可定位）。
4. **回收不足不补扣**：预留估少时差额只写进流水备注（账面永不为负的代价）。
5. 线上初始发分是 SQL 直连（见「结论」的偏差说明）。
