import type { ApiClient } from '@paideia/core'

import type { CredentialInput, CredentialView, SummaryView, SummaryVisibility } from './types'

export interface AssistantApi {
  credential(): Promise<CredentialView>
  saveCredential(input: CredentialInput): Promise<CredentialView>
  deleteCredential(): Promise<null>
  /** 只读：有现成结果就返回，没有返回 null（**不触发生成**，也不花钱）。 */
  summary(entryId: number): Promise<SummaryView | null>
  /** 生成或命中。这是唯一会花用户额度的动作。 */
  generate(entryId: number, visibility: SummaryVisibility): Promise<SummaryView>
}

/**
 * AI 精简端点客户端。
 *
 * <p>与知识库那份同样只包路径与请求体，鉴权与 401 刷新重放交给传入的 `ApiClient`——
 * 本包不认识令牌，也不需要知道它存在。
 */
export function createAssistantApi(client: ApiClient): AssistantApi {
  const base = '/api/v1/ai'
  const JSON_HEADERS = { 'Content-Type': 'application/json' }

  return {
    credential: () => client.requestEnvelope<CredentialView>(`${base}/credentials`),

    saveCredential: (input) =>
      client.requestEnvelope<CredentialView>(`${base}/credentials`, {
        method: 'PUT',
        headers: JSON_HEADERS,
        body: JSON.stringify(input),
      }),

    deleteCredential: () =>
      client.requestEnvelope<null>(`${base}/credentials`, { method: 'DELETE' }),

    summary: (entryId) => client.requestEnvelope<SummaryView | null>(`${base}/summaries/${entryId}`),

    generate: (entryId, visibility) =>
      client.requestEnvelope<SummaryView>(`${base}/summaries`, {
        method: 'POST',
        headers: JSON_HEADERS,
        body: JSON.stringify({ entryId, visibility }),
      }),
  }
}
