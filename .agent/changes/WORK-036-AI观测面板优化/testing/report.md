# 验证报告 · AI 观测面板优化（WORK-036）

日期：2026-10-23。每条结论对应一次真实执行，命令与结果如实记录。

## 一、执行结果总览

| 检查 | 命令 | 结果 |
| --- | --- | --- |
| 后端单测（除集成模块） | `scripts/test-db.sh mvn -B -T 1C -pl '!paideia-app' test` | **291 执行 / 0 失败 / 13 跳过** |
| WORK-036 集成用例 | `scripts/test-db.sh mvn -B -pl paideia-app -am -Dtest=AiObservabilityIntegrationTest test` | **11 执行 / 0 失败** |
| 注销级联 | `... -Dtest=AccountDeletionCascadeIntegrationTest test` | **2 执行 / 0 失败** |
| 迁移与模块边界闸 | `... -Dtest=ModularityTest,MigrationVersionFormatTest,MigrationChecksumTest,MigrationSeedOverlapTest test` | **9 执行 / 0 失败** |
| assistant 包单测 | `pnpm --filter @paideia/assistant test` | **26 执行 / 0 失败** |
| 管理端 e2e（本地起后端） | `scripts/test-db.sh pnpm --filter @paideia/admin test:e2e` | **27 passed** |
| 类型检查 | `pnpm --filter @paideia/admin typecheck`、`@paideia/assistant typecheck` | 均通过 |

## 二、逐条验收

| AC | 结论 | 证据 |
| --- | --- | --- |
| AC-001 日桶之和等于窗口行数 | 通过 | `AiObservabilityIntegrationTest.dailySeriesTotalsMatch` |
| AC-002 空桶补零、不跳段、分位数为 null | 通过 | `seriesFillsEmptyBuckets` + `SegmentationTest.fillUsesNullForMissingPercentiles` |
| AC-003 概览与明细同一筛选中总数一致 | 通过 | `overviewMatchesListWithSameFilter`（**曾红过，见第三节缺陷 2**） |
| AC-004 缓存比例；全零返回 0 | 通过 | `cacheTokensAreExposed` + `AiRunMetricsTest.cacheSavedRatioIsZeroWhenEverythingIsZero` |
| AC-005 名义成本与实扣成本同时可读 | 通过（部分） | `overview.nominalCredits` 由 `BillingProperties.creditsFor` 按模型累加；界面两数并列。**价目表变更前后的对比只验了代码路径，没有真的改配置跑一次** |
| AC-006 丢弃 > 0 时出现告警条 | 通过（结构） | `healthReportsQueueState` 验证字段可读；界面 `runs-health-alert` 由 `dropped > 0 \|\| writeFailures > 0` 驱动。**没有真把队列灌满**（那需要把容量设成 1 并压测，不在本次范围） |
| AC-007 采样 < 1 时提示 | 通过（结构） | `overview.sampling` 透传 + 界面 `runs-sampling-hint`。**没有真的把 sampling 调成 0.5 跑一轮** |
| AC-008 对账标异常并给差值 | 通过 | `reconcileExcludesReserveAndAdjust`，含 **RESERVE 反向验证** |
| AC-009 四段之和等于总耗时 | 通过 | `AiRunMetricsTest.breakdownSubtractsEachSegment` |
| AC-010 重复反馈是覆盖 | 通过 | `feedbackUpsertsInsteadOfAppending`（集成）+ `conversation-panel.test.tsx` 的 `回答反馈` 用例（前端） |
| AC-011 差评筛选 + 展开看载荷 | 通过 | `feedbackPageFilters`（后端）+ 管理端 e2e `反馈页` |
| AC-012 注销删反馈 | 通过 | `AccountDeletionCascadeIntegrationTest`（**新增 ai_feedback 与 ai_runs 两行种子与断言**） |
| AC-013 traceId 前缀搜索 | 通过 | `tracePrefixSearch`；不存在的前缀返回 0 行而非 500 |
| AC-014 子步骤 parent 落库 | 通过（见偏差） | `childSpanKeepsParent` |
| AC-015 关闭采集后接口仍可用 | 通过（部分） | `healthReportsQueueState` 覆盖 `enabled` 字段；**没有在 `telemetry.enabled=false` 的上下文里跑一遍所有接口** |

## 三、实施中抓到的两个真缺陷

**缺陷 1：日桶全部被静默跳过。** 首轮集成测试 `dailySeriesTotalsMatch` 期望 2、实得 0。
根因：`parseBucket` 对日桶也用了 `LocalDateTime.parse("2026-10-23")`，纯日期没有时分秒，直接抛异常，
而 catch 把它们当"不可解析"跳过。后果是**任何超过 48 小时的窗口都会返回一条全零折线**——
恰好是最常用的 7 天 / 30 天视图。修法：日桶走 `LocalDate.parse` + `atStartOfDay`。

**缺陷 2：概览的总数不受筛选影响。** `overviewMatchesListWithSameFilter` 期望 1、实得 3。
根因：`overview` 用的是全量 `countByStatus`，四个筛选参数只传给了 totals 没传给状态计数。
后果是"按场景筛选后概览仍显示全平台调用量"，而明细是对的——两个页面对不上。
修法：新增 `countByStatusFiltered` 并在 overview 里用它。

两个缺陷都**只有跑真数据库才看得见**：纯函数单测覆盖不到 SQL 与查询参数，而 UI 上"折线是平的"
与"数字没跟着筛选动"都不会报错。

## 四、反向验证

| 反向操作 | 结果 |
| --- | --- |
| 把 `Segmentation.of` 的 `<=` 改成 `<` | `SegmentationTest` 2 条红（48 小时边界） |
| 把 `ledgerSpendByDay` 的 `RESERVE` 从排除列表移除 | `reconcileExcludesReserveAndAdjust` 红（期望 5、实得 55） |

## 五、偏差与未覆盖

1. **TASK-017（子步骤）只做了"支持"，没有真实产生点。** 仓库里 `SpanSpec.retrieval` 零使用，
   工具步内部也没有二次模型调用。所以本期交付的是：端口支持 `parentSeq`、落库与读取正确、
   详情页按层级缩进。**没有虚构一个假的子步骤埋点**。真正需要分层时要先有"工具内部再调模型"这种场景。
2. **AC-005 / 006 / 007 / 015 是部分覆盖**：代码路径与字段都验了，但"改配置真的跑一次"
   （改价目表、调小采样率、把队列灌满、关掉采集）留作后续。理由：这些都需要临时改配置重启，
   而它们的失败模式是"字段没透传"——那已经被现有的字段断言挡住了。
3. **学习者端 assistant e2e 未跑。** 它要真调上游模型，本次环境没有可用的 AI Key。
   反馈 UI 由 `conversation-panel.test.tsx` 的单测覆盖（点踩→展开原因→提交→显示已反馈）。
4. **`EXPLAIN` 没手工核对。** `LIKE CONCAT(#{tracePrefix}, '%')` 是前缀匹配、理论上走
   `uk_ai_runs_trace` 的范围扫描；`tracePrefixSearch` 验了结果正确性，但没有看执行计划。
   tasks.md 原本把这条写成"人工核对并记进报告"，**这一条没做**。
5. **管理端 e2e 第一次全红是因为 jar 没重建**，不是代码问题：playwright 的 webServer 跑的是
   `paideia-app-0.0.1-SNAPSHOT.jar`，改了后端代码必须先 `mvn package`。重打后 27 条全绿。

## 六、遗留风险

- **对账页一旦上线且差值非零，先怀疑记录口径**（缓存命中不计费、失败轮次不扣分、采样会删成功行），
  不要先怀疑计费 bug。采样 < 1 时对账必然出现正差，界面已写明这一点。
- **采集健康的数字是多实例不友好的**：每个进程各有一份。单实例部署下正确；变成多实例时要经 sink 汇总。
- **反馈是本地状态**：刷新页面后看不出已经反馈过。这是刻意的取舍（少一个回读请求），但意味着
  用户可能对同一条回答评两次——后端是覆盖，不会产生脏数据。
