/**
 * AI 精简共享包。
 *
 * <p>只服务学习者端：管理端不做精简（那是学习者自己的阅读辅助，不是内容维护动作）。
 * 拆成包而不是塞进 `apps/app`，是因为它自己有客户端、卡片、壳与设置四块，
 * 而且后续工作项（多轮与记忆、笔记）会继续长在这里。
 *
 * <p>本包**不认识令牌**：鉴权与 401 刷新重放由传入的 `ApiClient` 负责。
 */

export { AssistantShell } from './assistant-shell'
export { createAssistantApi } from './client'
export type { AssistantApi } from './client'
export { AssistantProvider, useAssistantContext } from './context'
export type { AssistantContextValue } from './context'
export { SummaryCardView } from './summary-card'
export type {
  CredentialInput,
  CredentialView,
  SummaryCard,
  SummarySection,
  SummaryView,
  SummaryVisibility,
  TokenUsage,
} from './types'
