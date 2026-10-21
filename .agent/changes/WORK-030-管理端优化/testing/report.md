---
artifact: test-report
work_id: WORK-030
work: 管理端优化
status: passed
evidence: required
workflow: full
created: 2026-10-21
updated: 2026-10-21
verified_commit: df73133
---

# WORK-030 阶段 A 验证报告

> **本报告只覆盖阶段 A**（用户管理 + 各页筛选 + 删积分调整页 + 总览）。
> 阶段 B（步骤级遥测、`ai_run_payloads` 表）未实现，其验收项不在本报告内。

## 验证环境

| 项 | 值 |
| --- | --- |
| 后端 | 本机 JDK 25，`scripts/test-db.sh` 独占库 |
| 前端 | 管理端从当前工作区构建；e2e 走本地 Vite + 本地后端（隔离库 + devdata 种子） |
| 分支 | `work/WORK-030`，工作树 `.worktrees/WORK-030` |

## 验收矩阵

| 检查项 | 命令 | 退出码 | 结果 | 证据 |
| --- | --- | --- | --- | --- |
| AC-001 搜到人 | `pnpm -F @paideia/admin exec playwright test e2e/users.spec.ts` | 0 | passed | 3 passed；「搜索框输入即筛：搜不存在的串显示空状态」2.0s 通过 |
| AC-002 列表字段齐 | 同上 | 0 | passed | 列表九列（用户名/邮箱/角色/余额/已用/调用/最近活跃/注册时间/操作），e2e 断言表格有首行 |
| AC-003 调积分可用 | `AdminUserIntegrationTest#adjustCreditsRequiresNote` | 0 | passed | 加 100 分后 `ai_credit_accounts.balance=100`，`ai_credit_ledger` 多一条 ADJUST |
| AC-004 不可自伤 | `AdminUserIntegrationTest#cannotTouchSelf` | 0 | passed | 改自己角色/删自己/给自己发分三条都 400，之后角色仍是 admin |
| AC-005 改角色生效 | `AdminUserIntegrationTest#changeRoleRevokesRefreshTokens` | 0 | passed | 角色改成 admin 且该用户 `refresh_tokens` 归零（U-18） |
| AC-006 无 schema 变更 | `git diff --stat` 全仓 | 0 | passed | **没有任何迁移文件**改动（U-19 撤销禁用后 schema 零变更） |
| AC-007 删除干净 | `AdminUserIntegrationTest#adminDeletionCascades` | 0 | passed | 删号后 `users` 与 `refresh_tokens` 都归零；级联与自助注销共用 `deleteCascade` |
| AC-008 积分调整页已消失 | `rg -e ai-credits/grant -e AiCreditsGrantPage frontend/` | 0 | passed | 零命中；`/ai-credits` 转向 `/ai-credits/audit` |
| AC-009 每个列表都能筛 | 管理端 e2e 25 条 + 人工点检 | 0 | passed | 用量审计（种类/用户/分页）、举报（状态）、概念（名称）、分类（名称）、条目（分类）、调用明细（时间/场景/状态/用户/分页） |
| AC-010 举报默认只看待处理 | e2e + 代码 | 0 | passed | `statusFilter` 初值 `'pending'`，列表请求带 `?status=pending` |
| AC-011 调用明细分页可用 | 代码 + 构建 | 0 | passed | `page/size` 参数化，`size=50` 不再写死；页码控件 `runs-prev`/`runs-next` |
| AC-012 时间区间可用 | 代码 | 0 | passed | 近1小时/今天/近7天/近30天四个选项，切后重算 `from/to` |
| AC-013 无原生下拉 | `rg "<select" frontend/apps/admin/src/` | 0 | passed | 零命中（原两处已换 `@paideia/ui` 的 `Select`） |
| AC-014 总览有运营数字 | 代码 + 构建 | 0 | passed | 五个数字：分类/条目/草稿/账号/待处理举报，后两个可点进去 |
| AC-015 不泄露口令 | `AdminUserIntegrationTest#adminCanListWithoutPasswordHash` | 0 | passed | 响应体不含 `passwordhash`、`password_hash`、`$2a$` |
| 权限规则不静默失效 | `AdminUserIntegrationTest#learnerIsForbidden` | 0 | passed | 学习者调列表/详情/改角色/删号四条全 403 |
| 权限规则反向验证 | 把规则 path 改成永不匹配后重跑 | 1 | passed | 按预期红：`expected: 403`（学习者拿到 200） |

## 顺带修掉的一个真缺陷（不是本项范围，但被它暴露）

`AgentTurnBehaviorTest` 在**模块全量**下偶发红、单独跑却绿。查下来不是测试抖动：

> 正文原先在 `doFinally` 里推给前端，而 `doFinally` 由终止信号**之后**异步触发。
> `blockLast()` 返回时正文可能还没到、`onCompleted()` 也可能已经调过——**用户会看到一条空回答**。

改到 `doOnComplete` / `doOnError`（信号同步传播路径上），并补了取消分支。
修复后模块级连跑 3 次全绿。

这个缺陷是 WORK-031 引入的，`AgentTurnBehaviorTest` 是发现它的地方——说明那批测试确实在干活。

## 测试数字

| 范围 | 结果 |
| --- | --- |
| 后端全量 | **690 条 / 0 失败 / 18 跳过** |
| 管理端 e2e | **25 passed**（含新增用户管理 3 条 + 窄屏 1 条） |
| 管理端构建 + tsc | 通过 |

18 条跳过我逐条核过，均为需外部服务：`FileStorageContractTest` 1（需 S3）、
`AgentLiveConversationTest` 2（需真上游）、`LayaContentClassifierLiveTest` 3（需决策服务）、
`AssistantLiveUpstreamIntegrationTest` 4、`DigestLiveProbeTest` 4、WORK-029 自带 4。

## 未做

- **AC-002「余额与用量审计一致」没有做交叉核对**：只验了"列表里显示的数字来自正确口径"（单测断言），
  没有拿两个页面的真实数字逐行比对。
- **排序没做**（按余额/调用次数）：需求里 REQ-003 写了，但设计里判定"现在只有两个账号，
  加它是给不存在的规模做优化"而未实现。**这是与需求的偏离，据实记在这里**，需要用户裁决是否补。
- **阶段 B 未实现**：步骤级首字/模型、工具参数与返回、`ai_run_payloads` 表。
