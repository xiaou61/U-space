import type { ApiClient } from '@paideia/core'

import { readEventStream } from './sse'
import type {
  ChatEvent,
  ConversationView,
  CreditsView,
  CredentialView,
  HistoryMessage,
  MemoryView,
  SummaryView,
  SummaryVisibility,
} from './types'

export interface AssistantApi {
  /** AI 服务状态（平台配没配）。**只读**：WORK-009 起用户不再配置 Key。 */
  credential(): Promise<CredentialView>
  /** 本人的积分余额与消耗明细。**只读**：本项不做充值。 */
  credits(): Promise<CreditsView>
  /** 只读：有现成结果就返回，没有返回 null（**不触发生成**，也不花钱）。 */
  summary(entryId: number): Promise<SummaryView | null>
  /** 生成或命中。这是唯一会花用户额度的动作。 */
  generate(entryId: number, visibility: SummaryVisibility): Promise<SummaryView>
  /** 建会话。`entryId` 可空——助手在任意页都能开，条目只是可选上下文。 */
  createConversation(entryId: number | null): Promise<ConversationView>
  /** 本人的会话列表（**不含消息内容**，只用来选一条）。 */
  conversations(): Promise<ConversationView[]>
  /** 读回某会话的消息（**旧 → 新**），供界面恢复历史。 */
  messages(conversationId: number): Promise<HistoryMessage[]>
  /** 本人记忆（严格私有，没有"看别人的"这种调用）。 */
  memories(): Promise<MemoryView[]>
  /** 逐条删除。返回是否真的删掉了一行（不是本人的会返回 false）。 */
  deleteMemory(source: MemoryView['source'], id: number): Promise<boolean>
  /**
   * 记住一句话（写一条全文记忆）。**这是记忆唯一的写入口**，只有用户点击才会走到这里——
   * 助手自己无法调用它（端口里没有写工具）。
   */
  remember(text: string): Promise<number>
  /**
   * 发一轮消息，把 SSE 事件逐个回调出去。本方法返回时表示这一轮结束。
   *
   * <p>`signal` 用于"用户关掉面板/切换会话"时中断：不断开的话，上游调用会继续计费到
   * 用户看不见的地方。
   */
  sendMessage(
    conversationId: number,
    message: string,
    onEvent: (event: ChatEvent) => void,
    signal?: AbortSignal,
  ): Promise<void>
}

/**
 * AI 助手端点客户端。
 *
 * <p>与知识库那份同样只包路径与请求体，鉴权与 401 刷新重放交给传入的 `ApiClient`——
 * 本包不认识令牌，也不需要知道它存在。
 *
 * <p>对话那条走 `client.stream`（SSE）：**不是** `requestEnvelope`，后者会把响应体读完再解析 JSON，
 * 那样流式就退化成"等完整结果"了。
 */
export function createAssistantApi(client: ApiClient): AssistantApi {
  const base = '/api/v1/ai'
  const JSON_HEADERS = { 'Content-Type': 'application/json' }

  return {
    credential: () => client.requestEnvelope<CredentialView>(`${base}/credentials`),

    credits: () => client.requestEnvelope<CreditsView>(`${base}/credits`),

    summary: (entryId) => client.requestEnvelope<SummaryView | null>(`${base}/summaries/${entryId}`),

    generate: (entryId, visibility) =>
      client.requestEnvelope<SummaryView>(`${base}/summaries`, {
        method: 'POST',
        headers: JSON_HEADERS,
        body: JSON.stringify({ entryId, visibility }),
      }),

    createConversation: (entryId) =>
      client.requestEnvelope<ConversationView>(`${base}/conversations`, {
        method: 'POST',
        headers: JSON_HEADERS,
        body: JSON.stringify({ entryId }),
      }),

    conversations: () => client.requestEnvelope<ConversationView[]>(`${base}/conversations`),

    messages: (conversationId) =>
      client.requestEnvelope<HistoryMessage[]>(`${base}/conversations/${conversationId}/messages`),

    memories: () => client.requestEnvelope<MemoryView[]>(`${base}/memories`),

    deleteMemory: (source, id) =>
      client.requestEnvelope<boolean>(`${base}/memories/${source}/${id}`, { method: 'DELETE' }),

    remember: (text) =>
      client.requestEnvelope<number>(`${base}/memories`, {
        method: 'POST',
        headers: JSON_HEADERS,
        body: JSON.stringify({ type: 'text', text }),
      }),

    sendMessage: async (conversationId, message, onEvent, signal) => {
      const response = await client.stream(`${base}/conversations/${conversationId}/messages`, {
        method: 'POST',
        headers: JSON_HEADERS,
        body: JSON.stringify({ message }),
        ...(signal === undefined ? {} : { signal }),
      })
      // 用量在线上是 `input`/`output`/`total`，界面用的是 `TokenUsage`。**只在这一处归一**：
      // 分散到各个渲染点去换字段名，迟早有一条路径漏掉。
      await readEventStream(response, (event) => {
        const usage =
          event.type === 'done' && event.usage !== undefined && event.usage !== null
            ? {
                inputTokens: event.usage.input,
                outputTokens: event.usage.output,
                totalTokens: event.usage.total,
              }
            : null
        onEvent({
          type: event.type,
          delta: event.delta,
          message: event.message,
          empty: event.empty,
          // 草稿必须透传：界面靠 event.draft 收集"待确认草稿"，
          // 在这里漏掉会让"保存为笔记"整条链路静默失效且不报错
          draft: event.draft,
          usage,
        })
      })    },
  }
}
