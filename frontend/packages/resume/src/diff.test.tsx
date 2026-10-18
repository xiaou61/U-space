import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { ChangedText, diffParts, removedParts } from './diff'

describe('改动高亮', () => {
  it('完全相同：只有一段未改动', () => {
    expect(diffParts('完全一样', '完全一样')).toEqual([{ text: '完全一样', changed: false }])
  })

  it('开头插入：插入部分被标为改动', () => {
    const parts = diffParts(
      '把接口响应从 800ms 降到 200ms',
      '高并发场景下把接口响应从 800ms 降到 200ms',
    )
    expect(parts[0]).toEqual({ text: '高并发场景下', changed: true })
    expect(parts[1]).toEqual({ text: '把接口响应从 800ms 降到 200ms', changed: false })
  })

  it('结尾追加：追加部分被标为改动', () => {
    const parts = diffParts('负责订单系统', '负责订单系统，覆盖日均 30 万单')
    expect(parts[0]).toEqual({ text: '负责订单系统', changed: false })
    expect(parts[1]).toEqual({ text: '，覆盖日均 30 万单', changed: true })
  })

  it('中间替换：只有中间被标为改动', () => {
    const parts = diffParts('接口响应 800ms', '接口响应 200ms')
    expect(parts.map((part) => part.text).join('')).toBe('接口响应 200ms')
    expect(parts.some((part) => part.changed)).toBe(true)
  })

  it('删除的部分能被识别（用于原文侧展示）', () => {
    expect(removedParts('负责订单系统，带 5 人团队', '负责订单系统')).toEqual([
      { text: '，带 5 人团队', changed: true },
    ])
  })

  it('空的原文不产生删除段', () => {
    expect(removedParts('', '新内容')).toEqual([])
  })

  it('渲染：改动处是 mark 元素，未改动处是纯文本', () => {
    render(<ChangedText text="高并发场景下把接口响应从 800ms 降到 200ms" before="把接口响应从 800ms 降到 200ms" />)
    const marks = screen.getByText('高并发场景下')
    expect(marks.tagName).toBe('MARK')
    expect(document.body.textContent).toContain('把接口响应从 800ms 降到 200ms')
  })
})
