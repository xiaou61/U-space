import { describe, expect, it } from 'vitest'

import { mergeProgress } from './learning-progress'
import type { EntryProgressView } from './learning-types'

/**
 * 进度合并的取优规则（WORK-020）。
 *
 * <p>规则写反了不会报错，只会让用户看到进度**倒退**——所以逐条钉住：
 * 逐块取更高分、走读取更远步骤、服务端没有的块保留本地那份。
 */
function view(overrides: Partial<EntryProgressView> = {}): EntryProgressView {
  return {
    entryId: 1,
    title: '01 · 环境',
    lineKind: 'course',
    status: 'in_progress',
    tourStep: 0,
    passedAt: null,
    reviewStage: 0,
    reviewDueAt: null,
    challenges: {},
    quizzes: {},
    ...overrides,
  }
}

describe('mergeProgress', () => {
  it('逐块取更高分：本地 2/3 与服务端 3/3 合并得到 3/3', () => {
    const merged = mergeProgress(
      view({ challenges: { c1: { score: 3, total: 3 } } }),
      {
        updatedAt: '2026-01-01T00:00:00.000Z',
        tours: {},
        challenges: { c1: { score: 2, total: 3 } },
        quizzes: {},
      },
    )
    expect(merged.challenges['c1']).toEqual({ score: 3, total: 3 })
  })

  it('比较的是比例：3/4 优于 7/10', () => {
    const merged = mergeProgress(view({ quizzes: { q1: { score: 7, total: 10 } } }), {
      updatedAt: '2026-01-01T00:00:00.000Z',
      tours: {},
      challenges: {},
      quizzes: { q1: { score: 3, total: 4 } },
    })
    expect(merged.quizzes['q1']).toEqual({ score: 3, total: 4 })
  })

  it('服务端没有的块保留本地那份', () => {
    const merged = mergeProgress(view(), {
      updatedAt: '2026-01-01T00:00:00.000Z',
      tours: { tour: { step: 2, total: 5, done: false } },
      challenges: { localOnly: { score: 1, total: 1 } },
      quizzes: {},
    })
    expect(merged.challenges['localOnly']).toEqual({ score: 1, total: 1 })
    expect(merged.tours['tour']?.step).toBe(2)
  })

  it('两边都没有时给空对象，不返回 undefined', () => {
    const merged = mergeProgress(null, null)
    expect(merged.challenges).toEqual({})
    expect(merged.quizzes).toEqual({})
    expect(merged.tours).toEqual({})
  })
})
