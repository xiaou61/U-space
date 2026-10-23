import { describe, expect, it } from 'vitest'

import { rankHomeEntries, type HomeCandidate, type HomeSignals } from './home-ranking'

const candidate = (entryId: number, publishedAt: string, categoryId: number | null = null): HomeCandidate => ({
  entryId,
  title: `条目 ${entryId}`,
  publishedAt,
  categoryId,
})

const noSignals: HomeSignals = {
  dueEntryIds: [],
  conceptByEntryId: {},
  weakConceptIds: [],
  inProgressEntryIds: [],
  preferredCategoryIds: [],
}

describe('rankHomeEntries', () => {
  it('按 到期 > 薄弱 > 进行中 > 身份偏好 > 最新 排序', () => {
    const candidates = [
      candidate(1, '2026-10-01T00:00:00Z', 9),
      candidate(2, '2026-10-02T00:00:00Z', 8),
      candidate(3, '2026-10-03T00:00:00Z', 7),
      candidate(4, '2026-10-04T00:00:00Z', 6),
      candidate(5, '2026-10-05T00:00:00Z', 5),
    ]
    const ranked = rankHomeEntries(candidates, {
      dueEntryIds: [1],
      conceptByEntryId: { 2: [100] },
      weakConceptIds: [100],
      inProgressEntryIds: [4],
      preferredCategoryIds: [7],
    })

    expect(ranked.map((item) => item.entryId)).toEqual([1, 2, 4, 3, 5])
    expect(ranked.map((item) => item.reason)).toEqual([
      'due',
      'weak',
      'in-progress',
      'preferred',
      'latest',
    ])
  })

  it('同一优先级时按发布时间倒序', () => {
    const ranked = rankHomeEntries(
      [candidate(1, '2026-10-01T00:00:00Z'), candidate(2, '2026-10-09T00:00:00Z')],
      { ...noSignals, dueEntryIds: [1, 2] },
    )

    expect(ranked.map((item) => item.entryId)).toEqual([2, 1])
  })

  it('零信号时保持输入顺序，不按时间重排', () => {
    const ranked = rankHomeEntries(
      [candidate(1, '2026-10-01T00:00:00Z'), candidate(2, '2026-10-09T00:00:00Z')],
      noSignals,
    )

    expect(ranked.map((item) => item.entryId)).toEqual([1, 2])
    expect(ranked.every((item) => item.reason === 'latest')).toBe(true)
  })
})