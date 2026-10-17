import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { createEntryProgressPort, recentReading } from './source-reading-progress'

/**
 * 本地进度的三条纪律：**坏了不炸**、**只动自己那条**、**按时间倒序**。
 * 存储被 mock 成内存表，不测浏览器实现。
 */
const KEY = 'paideia:source-reading:v1'

beforeEach(() => {
  const store = new Map<string, string>()
  vi.stubGlobal('window', {
    localStorage: {
      getItem: (key: string) => store.get(key) ?? null,
      setItem: (key: string, value: string) => void store.set(key, value),
      removeItem: (key: string) => void store.delete(key),
    },
  })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('createEntryProgressPort', () => {
  it('没有 entryId 时返回 undefined（管理端预览不记录）', () => {
    expect(createEntryProgressPort(undefined)).toBeUndefined()
  })

  it('写入后能读回，并合并多个块的成绩', () => {
    const port = createEntryProgressPort(7, '读懂更新流程')
    expect(port).toBeDefined()
    port!.update({ tours: { t1: { step: 1, total: 3, done: false } } })
    port!.update({ quizzes: { q1: { score: 2, total: 3 } } })

    const read = port!.read()
    expect(read?.title).toBe('读懂更新流程')
    expect(read?.tours['t1']?.step).toBe(1)
    expect(read?.quizzes['q1']?.score).toBe(2)
  })

  it('可以读其他条目的进度（学习路径用）', () => {
    const other = createEntryProgressPort(8, '另一站')
    other!.update({ tours: { t: { step: 2, total: 3, done: true } } })
    const self = createEntryProgressPort(7)!
    expect(self.read(8)?.tours['t']?.done).toBe(true)
    expect(self.read(9)).toBeNull()
  })

  it('重置只清自己那条', () => {
    const first = createEntryProgressPort(1, '一')!
    const second = createEntryProgressPort(2, '二')!
    first.update({ tours: { t: { step: 0, total: 1, done: true } } })
    second.update({ tours: { t: { step: 0, total: 1, done: true } } })
    first.reset()
    expect(first.read()).toBeNull()
    expect(second.read()).not.toBeNull()
  })

  it('存储里是坏 JSON 时视为空进度，不抛错', () => {
    window.localStorage.setItem(KEY, '{broken')
    expect(createEntryProgressPort(1)!.read()).toBeNull()
  })

  it('写入失败（隐私模式）时静默', () => {
    vi.stubGlobal('window', {
      localStorage: {
        getItem: () => null,
        setItem: () => {
          throw new Error('QuotaExceededError')
        },
        removeItem: () => undefined,
      },
    })
    expect(() => createEntryProgressPort(1)!.update({ tours: { t: { step: 0, total: 1, done: true } } })).not.toThrow()
  })
})

describe('recentReading', () => {
  it('按更新时间倒序并限制条数', () => {
    const store = new Map<string, string>()
    vi.stubGlobal('window', {
      localStorage: {
        getItem: (key: string) => store.get(key) ?? null,
        setItem: (key: string, value: string) => void store.set(key, value),
        removeItem: () => undefined,
      },
    })
    window.localStorage.setItem(
      KEY,
      JSON.stringify({
        1: { updatedAt: '2026-10-17T10:00:00Z', title: '早' },
        2: { updatedAt: '2026-10-17T12:00:00Z', title: '晚' },
      }),
    )
    const recent = recentReading(1)
    expect(recent).toHaveLength(1)
    expect(recent[0]).toMatchObject({ entryId: 2, title: '晚' })
  })

  it('没有标题时用条目编号兜底', () => {
    window.localStorage.setItem(KEY, JSON.stringify({ 5: { updatedAt: '2026-10-17T10:00:00Z' } }))
    expect(recentReading(3)[0]?.title).toBe('条目 #5')
  })
})
