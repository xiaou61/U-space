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

/** 会话列表项。**不含消息内容**：列表只用来选，拉开某一条才取内容。 */
export interface ConversationView {
  id: number
  entryId: number | null
  updatedAt: string | null
}

/**
 * 一条记忆（设置页用，用户裁决 13）。
 *
 * <p>`source` 必须回给删除动作：结构化与全文两路的 id **各自独立**，只说 id 会删错一路。
 */
export interface MemoryView {
  source: 'structured' | 'text'
  id: number
  text: string
  createdAt: string | null
}

/** 界面上的一条对话消息。与后端 `role` 一致。 */
export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  /** 思考过程**只在当次显示**（服务端不落库），所以它挂在消息上、随会话消失而消失。 */
  thinking: string
  /** 助手消息的用量；上游没给或用户消息为 null。 */
  usage: TokenUsage | null
  /** 正文为空、服务端给了可见提示时为 true——界面据此区别"这是提示，不是回答"。 */
  emptyHint: boolean
}

/**
 * 回调给界面的对话事件——**用量已归一成 {@link TokenUsage}**。
 *
 * <p>与 `sse.ts` 的 {@link import('./sse').AssistantEvent} 分开：那个是"线上是什么形状"，
 * 这个是"界面想要什么形状"。归一只有一处（`client.ts`），界面不必知道线上用 `input` 还是 `inputTokens`。
 */
export interface ChatEvent {
  type: 'thinking' | 'content' | 'done' | 'error'
  delta?: string
  message?: string
  usage?: TokenUsage | null
  empty?: boolean
}
