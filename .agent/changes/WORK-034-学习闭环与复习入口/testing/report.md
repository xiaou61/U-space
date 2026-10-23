# WORK-034 验证报告

- 日期：2026-10-23
- 分支：`work-034-learning-loop`（已合并 master）

## S1（不依赖 033）

| 验收 | 结果 | 证据 |
| --- | --- | --- |
| AC-001 复习页可达、空态两个去处 | 通过 | `review.spec.ts` 4 passed（含 375px 不溢出） |
| AC-002 到期列表按到期日升序、带概念名 | 通过 | `LearningReviewIntegrationTest` 2 条 |
| AC-006 首页排序 | 通过 | `home-ranking.test.ts` 3 条；知识包 51 passed |
| AC-007 零数据三步 | 通过（结构） | `HomeFirstRun` + 关闭键；e2e 未单独覆盖关闭 |
| AC-008 训练营入口 | 通过 | 首页固定卡片 `home-entry-camp` |

## S2（复习会话）

| 验收 | 结果 | 证据 |
| --- | --- | --- |
| AC-003 会话创建/交卷主路径 | 通过 | `submitAdvancesStageAndIsIdempotent`（服务端判定、排期前进一档） |
| AC-005 重复交卷 409 / 过期拒绝 / 他人会话 404 | 通过 | `expiredAndForeignSessionRejected`、重复交卷断言 |
| AC-009 上游失败降级 | 通过 | `startFallsBackWhenGenerationFails`（端口打桩返回空 → fallback 列表） |
| AC-004 真连上游出题质量 | **未验证** | 金标用录制样本跑结构校验（`ReviewGoldenTest` 20 条全过）；真连结果未记录 |
| AC-010 会话页完成一轮 | **部分** | e2e 只断言「进入会话或降级」；未做整轮完成（需要确定性出题桩） |
| AC-011 注销清理 | 通过 | `AccountDeletionCascadeIntegrationTest` 追加 `review_sessions` 并真种一行 |

## 后端全量

`scripts/test-db.sh mvn -B -T 1C test` → **283 运行 / 0 失败 / 4 跳过**。
