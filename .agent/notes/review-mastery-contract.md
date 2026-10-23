# 复习会话消费 WORK-033 掌握度的契约（WORK-034 TASK-005）

- 日期：2026-10-23
- 依据：WORK-033 合并后的 `internal/question` 包。

## 可用接口（冻结）

```java
// io.github.xiaou61.knowledge.internal.question.QuestionAttemptService
public void recordMastery(long userId, long conceptId, boolean correct, int optionCount)
```

- 语义：按 BKT 更新该用户该概念的掌握概率；`optionCount` 只用来算猜对率（四选一 = 0.25）。
- 失败：概念不存在也会写一行（mastery 先验），不抛异常；调用方不需要回滚。
- 副作用：只写 `concept_mastery_state`，不写 `question_attempts`（复习题没有条目 + 块 + 题号身份）。

## 本项自己负责的部分

- 排期写 `learning_progress`（`review_stage` / `review_due_at`），通过
  `LearningReviewMapper.updateSchedule`；掌握度不反过来影响排期。
- 会话与作答存 `review_sessions`，不建题库表。

## 回退方案

若 033 的签名将来变化：本项只依赖上面一个方法，替换成本是一个调用点
（`LearningReviewService.submit`）。没有第二处耦合。
