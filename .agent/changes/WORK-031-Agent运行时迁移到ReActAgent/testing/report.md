---
artifact: test-report
work_id: WORK-031
work: Agent运行时迁移到ReActAgent
status: passed
evidence: required
workflow: full
created: 2026-10-21
updated: 2026-10-21
verified_commit: c001d4d
---

# WORK-031 第一步验证报告（升版本 + 缓存 token）

> **本报告只覆盖第一步。** 第二、三步（`ReActAgent` 替换手写循环、`MiddlewareBase` 观测）**未实现**，
> 其验收标准也尚未写成正式 AC（见 requirements 的「第二步与第三步的验收」一节）。
> 因此本报告标 `passed` 仅代表"已批准范围全部通过"，**不代表整个 WORK-031 完成**。

## 验证环境

| 项 | 值 |
| --- | --- |
| 后端 | 本机 JDK 25，`scripts/test-db.sh` 独占库（`paideia_run_1021150730_*`） |
| 分支 | `work/WORK-031`，工作树 `.worktrees/WORK-031` |
| 版本 | `agentscope-core:2.0.4`、`agentscope-extensions-model-openai:2.0.4`、`mcp:0.17.2` |
| 上游 | **本步未调用真实上游**；缓存行为由尖刺（2026-10-21，真实 DeepSeek）与单测共同确认 |

## 验收矩阵

| 检查项 | 命令 | 退出码 | 结果 | 证据 |
| --- | --- | --- | --- | --- |
| AC-001 升版本不炸 | `mvn -B -pl paideia-ai -am compile` | 0 | passed | 2.0.4 的 API 与我们现有用法兼容，零 Java 改动即编译通过 |
| AC-001 MCP 版本对齐 | `mvn -B -pl paideia-ai dependency:tree` | 0 | passed | `agentscope-core:2.0.4` 传递 `mcp:0.17.2`，与 `backend/pom.xml` 的 pin 一致 |
| AC-001 MCP 客户端可加载 | 全量里的 `McpStartupSmokeTest` | 0 | passed | 随全量一起通过 |
| AC-001 对话与工具集成测试 | `scripts/test-db.sh mvn -B -T 1C test` | 0 | passed | 12 个模块全部 SUCCESS，累计 545 条测试 |
| AC-002 缓存量可承载 | `mvn -pl paideia-ai test -Dtest=TokenUsageTest` | 0 | passed | `Tests run: 2, Failures: 0, Skipped: 0`；三参构造 cacheReadTokens=0，四参保留给定值 |
| AC-002 缓存链路已改（代码级） | `rg -n "cacheReadTokens" backend/paideia-ai/src/main` | 0 | passed | `toUsage()` 读 `getCachedTokens()`；`step.tokens` 与 9 处 `run.tokens` 全部传真实值。**真实上游数值待部署后补**，见下方边界说明 |
| AC-003 契约只增不改 | `git diff backend/paideia-app/src/test/resources/api-shape.json` | 0 | passed | 仅新增 `data.usage.cacheReadTokens: number` 一处，无删除、无类型变更 |
| AC-004 内环不退化 | `scripts/test-db.sh mvn -B -T 1C -pl '!paideia-app' test` | 0 | passed | `Tests run: 150, Failures: 0, Errors: 0, Skipped: 3` |
| AC-004 全量不退化 | `scripts/test-db.sh mvn -B -T 1C test` | 0 | passed | 545 条 / 0 失败 / 4 跳过（基线 543+4，增量即本次新增 2 条） |
| AC-004 Skipped 逐条核对 | `rg "Tests run:.*Skipped" full.log` | 0 | passed | `paideia-ai` 3 条 = `LayaContentClassifierLiveTest`（需真实上游）；`paideia-knowledge` 1 条 = `FileStorageContractTest`（需 S3）——均为既有设计，不是"跳过即通过" |
| AC-005 反向验证 | 三参构造改为 `this(in,out,total,total)` 后重跑 | 1 | passed | 按预期红：`expected: 0 but was: 120`；复原后重新转绿 |
| AC-006 可回滚 | `git log --oneline` | 0 | passed | 两笔独立提交：`a1af6ce`（纯版本）、`c001d4d`（缓存修复），可各自 revert |

## AC-008 的边界（据实说明）

**`ai_runs.cache_read_tokens > 0` 这一条在本批只做到"代码链路打通 + 单测覆盖"，没有用真实上游调用验证。**

原因：本步没有部署、也没有在隔离库里发起真实 DeepSeek 调用。代码层面已确认三处都改了：
`toUsage()` 读 `getCachedTokens()`、`step.tokens(...)` 与 9 处 `run.tokens(...)` 全部传 `cacheReadTokens()`。
真实链路上的数值需要**部署后跑一次带缓存的真实对话**才能记录，届时补进本报告。

尖刺里已经见过上游真的报 `cached=256`（`MODEL_CALL_END#2 in=446 out=12 cached=256`），所以"上游会给非零值"这个前提是实测成立的。

## 未做

- 前端任何验证（本步无前端改动）
- 部署与线上验证（未授权）
- 第二、三步（未实现）
