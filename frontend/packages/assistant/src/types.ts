/** AI 精简的对外数据形状，与后端 DTO 字段名一致（不再加一层映射）。 */

/** 结构化卡片的一节。 */
export interface SummarySection {
  heading: string
  body: string
}

/** 结构化精简卡片：总标题 + 若干带小标题的小节。 */
export interface SummaryCard {
  headline: string
  sections: SummarySection[]
}

/**
 * 一次精简的结果。
 *
 * <p>`usage` 在**命中缓存时为 `null`**，而不是 0：那代表"本次没有产生调用"，
 * 与"花了 0 个 token"是两件事，混成一个值会让人以为又付了一次钱。
 */
export interface SummaryView {
  card: SummaryCard
  cached: boolean
  /** `mine` = 当前用户生成的；`others` = 他人公开的结果。 */
  source: 'mine' | 'others'
  visibility: 'public' | 'private'
  usage: TokenUsage | null
  truncated: boolean
  generatedAt: string
}

export interface TokenUsage {
  inputTokens: number
  outputTokens: number
  totalTokens: number
}

/** 凭据的**脱敏**视图：永远没有 Key，只有后四位。 */
export interface CredentialView {
  configured: boolean
  baseUrl: string | null
  model: string | null
  apiKeyLast4: string | null
}

export interface CredentialInput {
  baseUrl: string
  model: string
  apiKey: string
}

/** 生成时的可见性，由**生成者**选择（谁付费谁决定要不要分享）。 */
export type SummaryVisibility = 'public' | 'private'
