import { describe, expect, it } from 'vitest'

import { formatRelative } from './relative-time'

/** 固定"现在"，否则测试会随运行时刻漂移。2026-10-15 14:30（本地时区）。 */
const NOW = new Date(2026, 9, 15, 14, 30, 0)
const ago = (millis: number) => new Date(NOW.getTime() - millis).toISOString()

const MINUTE = 60_000
const HOUR = 60 * MINUTE

describe('formatRelative', () => {
  it('一分钟以内是"刚刚"', () => {
    expect(formatRelative(ago(59_000), NOW)).toBe('刚刚')
    expect(formatRelative(ago(0), NOW)).toBe('刚刚')
  })

  it('分钟与小时', () => {
    expect(formatRelative(ago(MINUTE), NOW)).toBe('1 分钟前')
    expect(formatRelative(ago(59 * MINUTE), NOW)).toBe('59 分钟前')
    expect(formatRelative(ago(HOUR), NOW)).toBe('1 小时前')
    expect(formatRelative(ago(9 * HOUR), NOW)).toBe('9 小时前')
  })

  it('按日历天算"昨天"，而不是按 24 小时', () => {
    // 今天 00:10 与现在只差 14 小时，但它属于今天
    expect(formatRelative(new Date(2026, 9, 15, 0, 10).toISOString(), NOW)).toBe('14 小时前')
    // 昨天 23:00 只差 15 小时 —— 不能因为"不到 24 小时"就说成小时数
    expect(formatRelative(new Date(2026, 9, 14, 23, 0).toISOString(), NOW)).toBe('昨天')
    // 昨天凌晨 00:30 已经过去 38 小时 —— 也不能说成"38 小时前"
    expect(formatRelative(new Date(2026, 9, 14, 0, 30).toISOString(), NOW)).toBe('昨天')
    expect(formatRelative(new Date(2026, 9, 13, 0, 30).toISOString(), NOW)).toBe('2 天前')
  })

  it('30 天以内是天数，超过就换成日期', () => {
    expect(formatRelative(new Date(2026, 9, 13, 9, 0).toISOString(), NOW)).toBe('2 天前')
    expect(formatRelative(new Date(2026, 9, 12, 9, 0).toISOString(), NOW)).toBe('3 天前')
    expect(formatRelative(new Date(2026, 8, 1, 9, 0).toISOString(), NOW)).toBe('9 月 1 日')
  })

  it('跨年才写年份', () => {
    expect(formatRelative(new Date(2025, 11, 31, 9, 0).toISOString(), NOW)).toBe('2025 年 12 月 31 日')
  })

  it('未来时间与坏值是"刚刚"和空串，不是负数', () => {
    expect(formatRelative(new Date(NOW.getTime() + HOUR).toISOString(), NOW)).toBe('刚刚')
    expect(formatRelative(null, NOW)).toBe('')
    expect(formatRelative('', NOW)).toBe('')
    expect(formatRelative('不是时间', NOW)).toBe('')
  })
})
