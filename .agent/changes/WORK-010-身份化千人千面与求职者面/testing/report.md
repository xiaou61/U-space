---
artifact: test-report
work_id: WORK-010
work: 身份化千人千面与求职者面
status: partial
evidence: required
verified_commit: 495a07a
verified_at: 2026-10-18
---

# 身份化千人千面与求职者面验证报告

## 验证环境

| 项 | 值 |
| --- | --- |
| 机器 | 本机（Windows / Git Bash），JDK 25、Node 24 |
| 数据库 | 独立库 `paideia_w010_final`（后端全量）、`paideia_e2e_w010`（e2e）；本机 MySQL 8.0 **从零跑 Flyway** |
| 前端 | 学习者端 vite（5173）+ 从当前工作区重打的后端 jar |
| 上游模型 | 集成测试**从不真打上游**（`AssistantConversation` 是桩）；面试页 e2e 的账号余额为 0，走的是"积分不足"分支 |

## 验证结果

**部分通过。** 身份作为数据、harness 三套、自评桥、点评与评估落库、前端三页（设置/面试/首页重排）都已完成并验证。**但有一条验收无法真正走完**：线上/种子账号余额为 0，而本期不做充值——"完整走完面试 → 拿到评分 → 看到扣减"这条要等管理员给验收账号发积分才能验。所以本报告是 `partial`，不是 `passed`。

## 验收矩阵

| 检查项 | 命令 | 退出码 | 结果 | 证据 |
| --- | --- | --- | --- | --- |
| AC-001 身份与角色正交 | `mvn -B -pl paideia-app -am test -Dtest=IdentityAndAssessmentBridgeIntegrationTest` | 0 | passed | 该用例改身份后断言 `role` 仍为 `learner`、管理端端点仍返回 **403** |
| AC-002 切换身份生效 | `pnpm --filter @paideia/app test:e2e`（`identity.spec.ts`） | 0 | passed | 「导航按身份重排」：求职者的面试入口可见且**排在第一位**；首页快捷入口按 `preferredNav` 排序 |
| AC-003 记忆不分区 | 同上（后端 `MemoryAutoExtractionIntegrationTest`） | 0 | passed | 记忆两路都只按 `user_id` 过滤，**没有任何身份列**（V14 未给记忆表加身份维度，与 2026-10-15 的裁决一致） |
| AC-004 记忆与既有数据不丢 | `IdentityAndAssessmentBridgeIntegrationTest` + e2e | 0 | passed | 改画像后笔记/自评/会话表结构未动（本项**没有**给这些表加列）；e2e 刷新后画像仍在且其它页正常 |
| AC-005 harness 选择正确 | `InterviewEvaluationParserTest`（`harnessRegistryIsComplete`）+ `identity.spec.ts` | 0 | passed | 三套提示词两两不同、版本号不同；面试页建会话时带 `harness=interview`，库里有断言 |
| AC-006 场景工具不外泄 | `InterviewEvaluationParserTest` + `AssistantTools.schemas(Harness)` | 0 | passed | `REVIEW → List.of()`；穷尽 switch 无 default（新增 harness 时编译器强制决定工具集） |
| AC-007 点评是独立无工具调用 | `InterviewEvaluationParserTest`（7 条） | 0 | passed | 走独立的 `InterviewReviewer` 端口、非流式、**不经过工具循环**；解析器要求分数 1–5 整数，0/6/3.5/"3" 全部被判失败 |
| AC-008 自评桥覆盖语义 | `mvn ... -Dtest=IdentityAndAssessmentBridgeIntegrationTest` | 0 | passed | 「反复改仍只有一条」：改 4 次后 `ai_memories_structured` **仍 1 行**且内容是最后一次；取消自评后该行消失（按 SQL 数行，不看接口返回） |
| AC-009 面试闭环 | e2e `identity.spec.ts`（前半） | 0 | **未完成** | 只验到"能发起会话 + 积分不足时给明确提示"；**完整走完并拿到评分未验**（见「失败与未验证项」1） |
| AC-010 零积分语义 | 同上 | 0 | passed | 种子账号余额 0 点开始 → 页面显示「积分不足」，不是 500 也不是上游错误 |
| AC-011 注销清零 | `mvn -B -pl paideia-app -am test -Dtest=AccountDeletionCascadeIntegrationTest` | 0 | passed | 清单加了 `learner_profiles` 与 `ai_interview_evaluations`，夹具也建了这两行；逐表断言行数为 0 |
| 后端模块单测 | `mvn -B -pl paideia-ai -am test` | 0 | passed | **50 条**（含新增 `InterviewEvaluationParserTest` 7 条） |
| 前端单测 | `cd frontend && pnpm test` | 0 | passed | **243 条**（identity 3 / ui 148 / knowledge 28 / assistant 25 / core 22 / auth 13 / desktop 4） |
| 前端类型 | `pnpm --filter @paideia/app typecheck` | 0 | passed | 退出码 0 |
| 学习者端 e2e 全量 | `pnpm --filter @paideia/app test:e2e` | 0 | passed | **63 passed / 2 skipped / 0 failed**（1.7 分钟） |
| 构建 | `pnpm --filter @paideia/app build` | 0 | passed | 主包 760.83 kB（gzip 236.94 kB） |

## 反向验证（"这条要是坏了，哪个断言会红"）

| 被验行为 | 反着改一处 | 结果 |
| --- | --- | --- |
| 身份不参与权限 | 在 `LearnerProfileController` 里按身份加一条角色判定 | AC-001 那条"管理端仍 403"会红 |
| 自评桥是覆盖不是追加 | 把 `upsertStructuredByRef` 改成普通 INSERT | AC-008"反复改仍只有一条"会红（唯一键会直接报错或被插成多行） |
| 点评不带工具 | 给 `Harness.REVIEW` 返回通用工具集 | `REVIEW → List.of()` 那条单测会红 |
| 首页排序不丢入口 | 让源码解读卡与知识库卡共用排序 key | **实测过**：首页入口消失，`source-reading.spec.ts` 的「首页有源码解读入口」直接红 |

## 失败与未验证项

1. **AC-009 的完整闭环未验**：需要管理员给验收账号发积分后，真人走一遍"开场 → 追问 → 结束 → 点评 → 看到评分与扣减"。本期不做充值，所以这一条**只能由有权限的人手动完成**。这也是本报告为 `partial` 的原因。
2. **"勾选后才带笔记"目前只有界面明示**：面试页写了"你的笔记默认不参与"，但**一期还没有勾选控件**，也没有接笔记批量读接口。当前的事实是"一期根本不送笔记"——比设计更保守，但**不是设计所说的"用户显式勾选后可以带"**。登记为缺口，不当作已完成。
3. **全量后端测试有一组已知不稳定**：`KnowledgeReadIntegrationTest` / `KnowledgeSelfAssessmentIntegrationTest` / `KnowledgeAdminIntegrationTest` 在全量一起跑时报"读不到种子条目 id=1"，**每个类单独跑都全绿**。已排查七种可能（详见 `.agent/notes/dev-workflow.md`），未定位完；本报告的后端证据均来自**单类/单组**运行，没有把全量绿当成已达标。
4. **多身份切换无法端到端验收**：一期只有 `job_seeker` 可选（`selectable()` 为 false 的其余三个身份写不进接口）。这是设计里登记的已知缺口（design §10），不是漏测。
5. **`identityDefault` 恒为 assistant**：因此"不同身份的默认行为不同"在界面上**不可观察**。同样是登记过的缺口。
6. **面试题源未验**：设计说题源是"知识库已发布条目"，但那是由**提示词**引导模型去检索的（工具集里有 `search_entries`），没有断言"它一定用了哪些条目"——那属模型行为，不适合断言。

## 剩余风险

- **面试成本**：多轮带工具 + 一次点评，单次会话的 token 是普通对话的数倍。本期不加轮次上限（按设计：先看真实消耗再定），所以真跑起来可能比预期贵。
- **画像字段是"求职者专用"**：加第二个真身份时字段归属要重审（继续可空堆下去，还是按身份分表）。
- **点评的对话截断**：超过 12000 字时保留**最近**部分并在开头注明"面试开头已省略"。聊很久的面试，前半场不参与评分。
