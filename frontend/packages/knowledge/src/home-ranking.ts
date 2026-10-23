export interface RankedEntry {
  entryId: number
  reason: 'due' | 'weak' | 'in-progress' | 'preferred' | 'latest'
}

export interface HomeCandidate {
  entryId: number
  title: string
  publishedAt: string
  categoryId: number | null
}

export interface HomeSignals {
  /** 今天到期该复习的条目。 */
  dueEntryIds: number[]
  /** 条目 → 它挂了哪些概念。用来把"薄弱概念"映射回条目。 */
  conceptByEntryId: Record<number, number[]>
  /** 掌握度低于阈值的概念 id。 */
  weakConceptIds: number[]
  /** 已经开始但没走完的条目。 */
  inProgressEntryIds: number[]
  /** 按身份偏好的分类。 */
  preferredCategoryIds: number[]
}


/** 信号强弱：到期 > 薄弱 > 进行中 > 身份偏好。最新的只作为平局次序，不加分。 */
const WEIGHT_DUE = 10_000
const WEIGHT_WEAK = 5_000
const WEIGHT_IN_PROGRESS = 1_000
const WEIGHT_PREFERRED = 500

/**
 * 首页条目的排序（WORK-034 REQ-006）。
 *
 * <p>纯函数：不读时间、不发请求。信号全空时**保持输入顺序**——"没有个性化依据"时
 * 任何重排都是噪声，用户会以为首页在乱跳。
 */
export function rankHomeEntries(candidates: HomeCandidate[], signals: HomeSignals): RankedEntry[] {
  const due = new Set(signals.dueEntryIds)
  const inProgress = new Set(signals.inProgressEntryIds)
  const preferred = new Set(signals.preferredCategoryIds)
  const weak = new Set(signals.weakConceptIds)
  const hasAnySignal =
    due.size > 0 || inProgress.size > 0 || preferred.size > 0 || weak.size > 0

  const scored = candidates.map((candidate, index) => {
    const reason = reasonOf(candidate, due, weak, inProgress, preferred, signals.conceptByEntryId)
    return { candidate, index, reason, score: scoreOf(reason) }
  })

  if (!hasAnySignal) {
    return scored.map((item) => ({ entryId: item.candidate.entryId, reason: 'latest' as const }))
  }

  scored.sort((left, right) => {
    if (right.score !== left.score) {
      return right.score - left.score
    }
    const byTime = Date.parse(right.candidate.publishedAt) - Date.parse(left.candidate.publishedAt)
    if (byTime !== 0) {
      return byTime
    }
    // 同一时刻按输入顺序，保证结果稳定
    return left.index - right.index
  })
  return scored.map((item) => ({ entryId: item.candidate.entryId, reason: item.reason }))
}

function scoreOf(reason: RankedEntry['reason']): number {
  switch (reason) {
    case 'due':
      return WEIGHT_DUE
    case 'weak':
      return WEIGHT_WEAK
    case 'in-progress':
      return WEIGHT_IN_PROGRESS
    case 'preferred':
      return WEIGHT_PREFERRED
    default:
      return 0
  }
}

function reasonOf(
  candidate: HomeCandidate,
  due: Set<number>,
  weak: Set<number>,
  inProgress: Set<number>,
  preferred: Set<number>,
  conceptByEntryId: Record<number, number[]>,
): RankedEntry['reason'] {
  if (due.has(candidate.entryId)) {
    return 'due'
  }
  const concepts = conceptByEntryId[candidate.entryId] ?? []
  if (concepts.some((conceptId) => weak.has(conceptId))) {
    return 'weak'
  }
  if (inProgress.has(candidate.entryId)) {
    return 'in-progress'
  }
  if (candidate.categoryId !== null && preferred.has(candidate.categoryId)) {
    return 'preferred'
  }
  return 'latest'
}