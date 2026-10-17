import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'

import type { AssistantApi } from './client'
import { AssistantProvider } from './context'
import { ConversationPanel } from './conversation-panel'

/**
 * 收起面板必须中断在飞的上游调用（WORK-011 REQ-011 / AC-009 后半）。
 *
 * <p>这条只在**这一层**验得动：真正要盯的是"卸载时的那个 cleanup effect"。放到 e2e 里就得伪造
 * 一个永不结束的流，那测的是桩，不是产品。
 *
 * <p>它会在什么时候红：那个 `useEffect(() => () => abortRef.current?.abort(), [])` 被删掉，
 * 或者"中断"被改成只在收起按钮里做——而除卸载外面板没有第二个中断入口（外壳收起走的是另一条
 * return 分支，等于卸载）。
 */
// 与 summary-card.test.tsx 同因：本包没把 vitest 配成 globals，RTL 不会自动清理。
afterEach(cleanup)

/**
 * 只实现这条用例走到的方法。
 *
 * <p>`sendMessage` **永不落定**——"在流式中"就是它不回来这件事本身；本用例不关心回复内容，
 * 只关心卸载时那个 signal 有没有被 abort。
 */
function stubApi(onSend: (signal: AbortSignal | undefined) => void): AssistantApi {
  return {
    conversations: async () => [],
    messages: async () => [],
    createConversation: async () => ({ id: 1 }),
    // 面板挂载时会读一次记忆开关（WORK-013）；三个都开着，与"没设置过"的后端默认一致。
    settings: async () => ({
      memoryEnabled: true,
      referenceSavedMemories: true,
      referenceChatHistory: true,
    }),
    sendMessage: (_id: number, _text: string, _onEvent: unknown, signal?: AbortSignal) => {
      onSend(signal)
      return new Promise<void>(() => {})
    },
  } as unknown as AssistantApi
}

describe('收起面板', () => {
  it('卸载即中断在飞的上游调用（否则会继续计费到用户看不见的地方）', async () => {
    let signal: AbortSignal | undefined
    const { unmount } = render(
      <AssistantProvider>
        <ConversationPanel api={stubApi((value) => { signal = value })} />
      </AssistantProvider>,
    )

    fireEvent.change(screen.getByTestId('conversation-input'), { target: { value: '问一句' } })
    fireEvent.click(screen.getByTestId('conversation-send'))

    await waitFor(() => expect(signal).toBeDefined())
    expect(signal?.aborted).toBe(false)

    unmount()

    expect(signal?.aborted).toBe(true)
  })
})
