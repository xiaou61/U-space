import type { ApiClient } from '@paideia/core'

import type { ConceptGraph, EntryProgressView, LearningLine, ProgressReport, TrackMap, TrackSummary } from './learning-types'

/**
 * 学习线 / 学习记录的客户端（WORK-020）。
 *
 * <p>与知识库客户端同一装配来源（带令牌的 `ApiClient`），本包仍然不认识令牌。
 *
 * <p>**没有"我的课"这类方法**：线清单由服务端从分类标记投影出来，前端只读。
 */
export function createLearningApi(client: ApiClient) {
  return {
    lines: () => client.requestEnvelope<{ lines: LearningLine[] }>('/api/v1/learning/lines'),

    progress: (limit?: number) =>
      client.requestEnvelope<EntryProgressView[]>(
        `/api/v1/learning/progress${limit === undefined ? '' : `?limit=${limit}`}`,
      ),

    entry: (entryId: number) =>
      client.requestEnvelope<EntryProgressView>(`/api/v1/learning/entries/${entryId}`),

    report: (entryId: number, body: ProgressReport) =>
      client.requestEnvelope<EntryProgressView>(`/api/v1/learning/entries/${entryId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      }),

    reset: (entryId: number) =>
      client.requestEnvelope<null>(`/api/v1/learning/entries/${entryId}`, { method: 'DELETE' }),

    /** 我的知识图谱（WORK-021）。掌握度是服务端现算的，前端不缓存也不推导。 */
    graph: () => client.requestEnvelope<ConceptGraph>('/api/v1/learning/graph'),

    /** 学习轨道（WORK-024）。与知识库无关的独立入口。 */
    tracks: () => client.requestEnvelope<TrackSummary[]>('/api/v1/learn/tracks'),

    /** 一条轨道的关卡地图：按章节分组，每格带 done/current/locked。 */
    trackMap: (slug: string) =>
      client.requestEnvelope<TrackMap>(`/api/v1/learn/tracks/${encodeURIComponent(slug)}`),
  }
}

export type LearningApi = ReturnType<typeof createLearningApi>
