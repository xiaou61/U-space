import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'

import { SummaryCardView } from './summary-card'
import type { SummaryView } from './types'

/**
 * 卡片的界面语义。
 *
 * <p>盯两条最容易写错、也最容易被误读的表述：命中缓存时说的是"本次未产生调用"而**不是**
 * "0 tokens"（后者会被读成"几乎没花钱"），以及**来源必须标出来**（否则同一条内容两次点出
 * 不同文字会被当成模型不稳定）。
 *
 * <p>断言直接看 `textContent`：本仓没有引 `@testing-library/jest-dom`，也不必为几条断言引它。
 */
// 本包没有把 vitest 配成 globals，RTL 就不会自动注册 afterEach 清理——
// 不显式清理的话，第二个用例会在上一个用例残留的 DOM 里找到多个同 testid 的元素。
afterEach(cleanup)

function view(overrides: Partial<SummaryView> = {}): SummaryView {
  return {
    card: {
      headline: '三次握手是为了双向确认收发能力',
      sections: [{ heading: '为什么是三次', body: '两次无法确认客户端已收到 SYN-ACK。' }],
    },
    cached: false,
    source: 'mine',
    visibility: 'public',
    usage: { inputTokens: 30, outputTokens: 12, totalTokens: 42 },
    truncated: false,
    generatedAt: '2026-10-12T10:00:00Z',
    ...overrides,
  }
}

describe('精简卡片', () => {
  it('渲染结构化字段与本次用量', () => {
    render(<SummaryCardView view={view()} />)

    expect(screen.getByTestId('assistant-summary-headline').textContent).toContain('三次握手是为了双向确认收发能力')
    expect(screen.getByText('为什么是三次')).toBeTruthy()
    expect(screen.getByTestId('assistant-summary-usage').textContent).toContain('本次用量 42 tokens')
    expect(screen.getByTestId('assistant-summary-source').textContent).toContain('你自己生成的')
  })

  it('命中缓存时说"未产生调用"，而不是显示 0', () => {
    render(<SummaryCardView view={view({ cached: true, usage: null })} />)

    const usage = screen.getByTestId('assistant-summary-usage').textContent ?? ''
    expect(usage).toContain('本次未产生调用')
    expect(usage).not.toContain('0 tokens')
  })

  it('他人公开的结果要标明来源', () => {
    render(<SummaryCardView view={view({ cached: true, usage: null, source: 'others' })} />)

    expect(screen.getByTestId('assistant-summary-source').textContent).toContain('来自其他学习者')
  })
})
