/**
 * AI 训练营包（WORK-029）。
 *
 * <p>它只做三件事：把后端视图转成前端类型、发请求、渲染薄弱点与每日计划。
 * **没有任何权限判断**：营的归属完全由后端 SQL 校验，前端拿到的永远只是"我的"。
 */

export { createCampApi, type CampApi } from './client'
export { PlanDays } from './plan'
export {
  DIFFICULTY_LABEL,
  KIND_LABEL,
  VERDICT_LABEL,
  type CampAnswer,
  type CampComparison,
  type CampCreated,
  type CampDay,
  type CampForm,
  type CampItem,
  type CampKind,
  type CampPlan,
  type CampResult,
  type CampSummary,
  type CampTask,
  type CampVerdict,
  type ComparedTopic,
  type DifficultyCount,
  type TopicResult,
} from './types'
export { ComparisonTable, TopicTable, WeaknessCards } from './weakness'
