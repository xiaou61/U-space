import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'

import type { TraceResult } from './context'
import { TracePlayer } from './trace-player'

/**
 * 播放器的关键行为（WORK-023 REQ-006/007/008）。
 *
 * <p>只覆盖**纯本地状态**：步进改的是 index、变化标记只标真正变了的变量、
 * 触顶时说清"只录到这里"。执行与录制本身由后端集成测试覆盖。
 */
const TRACE: TraceResult = {
  steps: [
    { line: 1, locals: {}, event: 'line' },
    { line: 2, locals: { total: '0' }, event: 'line' },
    { line: 3, locals: { total: '0', i: '0' }, event: 'line' },
    { line: 3, locals: { total: '1', i: '1' }, event: 'line' },
  ],
  output: '3',
  reason: 'done',
  truncated: false,
  durationMs: 120,
}

function indicator(): string {
  return screen.getByTestId('trace-step-indicator').textContent ?? ''
}

// vitest 的 globals 没开，`@testing-library` 不会自动卸载：不清理的话第二个用例
// 会看到第一个用例留下的 DOM（"Found multiple elements"）。
afterEach(cleanup)

describe('逐步执行播放器', () => {
  it('显示步数与当前行号，走到哪一步就显示哪一步的变量', () => {
    render(<TracePlayer trace={TRACE} />)
    expect(indicator()).toContain('第 1 / 4 步')

    fireEvent.click(screen.getByTestId('trace-next'))
    fireEvent.click(screen.getByTestId('trace-next'))
    fireEvent.click(screen.getByTestId('trace-next'))

    expect(indicator()).toContain('第 4 / 4 步')
    expect(screen.getByTestId('trace-var-total').textContent).toBe('1')
    expect(screen.getByTestId('trace-var-i').textContent).toBe('1')
  })

  it('只给真正变了的变量打变化标记，且标记带文字不只是颜色', () => {
    render(<TracePlayer trace={TRACE} />)
    fireEvent.click(screen.getByTestId('trace-next'))
    fireEvent.click(screen.getByTestId('trace-next'))
    fireEvent.click(screen.getByTestId('trace-next'))

    expect(screen.getByTestId('trace-changed-total').textContent).toContain('变了')
    expect(screen.getByTestId('trace-changed-i').textContent).toContain('变了')
  })

  it('回到第一步时不再显示变化标记（没有上一步可比）', () => {
    render(<TracePlayer trace={TRACE} />)
    fireEvent.click(screen.getByTestId('trace-last'))
    fireEvent.click(screen.getByTestId('trace-first'))
    expect(screen.queryByTestId('trace-changed-total')).toBeNull()
  })

  it('键盘左右键能步进', () => {
    render(<TracePlayer trace={TRACE} />)
    const player = screen.getByTestId('trace-player')
    fireEvent.keyDown(player, { key: 'ArrowRight' })
    fireEvent.keyDown(player, { key: 'ArrowRight' })
    expect(indicator()).toContain('第 3 / 4 步')
    fireEvent.keyDown(player, { key: 'ArrowLeft' })
    expect(indicator()).toContain('第 2 / 4 步')
  })

  it('没有录到步数时给可读的空状态，不渲染播放控件', () => {
    render(<TracePlayer trace={{ ...TRACE, steps: [] }} />)
    expect(screen.queryByTestId('trace-empty')).not.toBeNull()
    expect(screen.queryByTestId('trace-player')).toBeNull()
  })

  it('触顶时说明"只录到这里"，而不是报错', () => {
    render(<TracePlayer trace={{ ...TRACE, reason: 'step_limit', truncated: true }} />)
    expect(screen.getByTestId('trace-reason').textContent).toContain('只录到这里')
  })
})
