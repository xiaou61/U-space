/**
 * 训练营的前端类型（WORK-029）。
 *
 * <p>与后端视图一一对应。**`CampItem` 里没有答案与解析**——后端那个类型本来就没有，
 * 所以前端也拿不到；答案只能从结果页的比较里间接体现（对错计数）。
 */

export type CampForm = 'diagnostic' | 'retest'

export type CampKind = 'read' | 'do' | 'gap' | 'review'

export type CampVerdict = 'IMPROVED' | 'SAME' | 'WORSE' | 'INCONCLUSIVE'

/** 营卡片上的状态。 */
export interface CampSummary {
  id: number
  days: number
  itemCount: number
  droppedCount: number
  resumeTitle: string
  createdAt: string
  diagnosticSubmitted: boolean
  retestSubmitted: boolean
  hasPlan: boolean
}

/** 开营/复测的返回。`shortOfTarget` 为真时界面要写明实际题数。 */
export interface CampCreated {
  id: number
  days: number
  itemCount: number
  droppedCount: number
  shortOfTarget: boolean
}

/** 一道题。**没有 answerIndex、没有 explanation**。 */
export interface CampItem {
  id: number
  seq: number
  topic: string
  difficulty: 'easy' | 'medium' | 'hard'
  stem: string
  options: string[]
}

export interface DifficultyCount {
  correct: number
  total: number
}

export interface TopicResult {
  topic: string
  correct: number
  total: number
  byDifficulty: Record<string, DifficultyCount>
}

/** 一个主题的诊断/复测并排。`reason` 在 `INCONCLUSIVE` 时说明原因。 */
export interface ComparedTopic {
  topic: string
  diagnostic: DifficultyCount
  retest: DifficultyCount
  verdict: CampVerdict
  reason: string | null
}

export interface CampComparison {
  topics: ComparedTopic[]
  onlyInDiagnostic: string[]
  onlyInRetest: string[]
  mismatched: string[]
}

export interface CampResult {
  topics: TopicResult[]
  weak: string[]
  strong: string[]
  /** 只有复测结果才非空。 */
  previous: CampComparison | null
  verdict: CampVerdict | null
}

export interface CampTask {
  id: number
  day: number
  kind: CampKind
  topic: string
  title: string
  minutes: number
  why: string
  entryId: number | null
  done: boolean
}

export interface CampDay {
  day: number
  tasks: CampTask[]
}

export interface CampPlan {
  id: number
  days: number
  budgetMinutes: number
  dayPlans: CampDay[]
  unscheduled: CampTask[]
  weak: string[]
}

export interface CampAnswer {
  itemId: number
  choice: number
}

/** 难度档的中文名。**不写「通过」「达标」这类词**——本项刻意不打分。 */
export const DIFFICULTY_LABEL: Record<string, string> = {
  easy: '易',
  medium: '中',
  hard: '难',
}

/** 任务类型的中文名。 */
export const KIND_LABEL: Record<CampKind, string> = {
  read: '读',
  do: '做',
  gap: '缺',
  review: '复习',
}

/** 对比结论的中文名。 */
export const VERDICT_LABEL: Record<CampVerdict, string> = {
  IMPROVED: '进步',
  SAME: '持平',
  WORSE: '退步',
  INCONCLUSIVE: '仅供参考',
}
