import type { ApiClient } from '@paideia/core'

import type { DigestByGroup, DigestWeekly } from './digest-types'

/**
 * 每日速递的客户端（WORK-027）。
 *
 * <p>三个只读端点；写操作（抓取）由服务端定时任务做，前端不触发。
 */
export function createDigestApi(client: ApiClient) {
  return {
    /** 某天的全部条目，已按 groupKey 分组。不传 date 取今天（UTC）。 */
    byDate: (date?: string) =>
      client.requestEnvelope<DigestByGroup>(
        `/api/v1/digest${date === undefined ? '' : `?date=${encodeURIComponent(date)}`}`,
      ),

    /** 有内容的日期，新到旧。给日期选择器。 */
    dates: (limit?: number) =>
      client.requestEnvelope<string[]>(
        `/api/v1/digest/dates${limit === undefined ? '' : `?limit=${limit}`}`,
      ),

    /** 某周周报。不传 week 取上一周；没有时服务端回 null（不是 404）。 */
    weekly: (week?: string) =>
      client.requestEnvelope<DigestWeekly | null>(
        `/api/v1/digest/weekly${week === undefined ? '' : `?week=${encodeURIComponent(week)}`}`,
      ),
  }
}

export type DigestApi = ReturnType<typeof createDigestApi>
