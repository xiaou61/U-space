import { beforeEach, describe, expect, it } from 'vitest'

import { readStoredConversation, storeConversation } from './session-store'

/**
 * 当前会话的本地记忆（WORK-011）。
 *
 * <p>这里要守住的核心是**三个状态的区分**：它们都表现为"没有当前会话"，但意思不同。
 * 少了这个区分，"点了新开对话 → 收面板 → 再打开"就会把刚丢开的那条又拉回来——
 * 而那正是用户抱怨"怎么不是回到上次、想新开才新开"的反面。
 */
describe('当前会话的本地记忆', () => {
  beforeEach(() => {
    window.localStorage.clear()
  })

  it('从没写过 → unset（打开时恢复最近一条）', () => {
    expect(readStoredConversation()).toEqual({ kind: 'unset' })
  })

  it('写成 null → new（保持空白，**不再**恢复旧的）', () => {
    storeConversation(null)
    expect(readStoredConversation()).toEqual({ kind: 'new' })
  })

  it('写成 id → id（恢复这一条）', () => {
    storeConversation(42)
    expect(readStoredConversation()).toEqual({ kind: 'id', id: 42 })
  })

  it('坏值一律当没设过（不抛异常，也不当成合法会话）', () => {
    for (const bad of ['abc', '-3', '0', '1.5', 'NaN']) {
      window.localStorage.setItem('paideia:assistant-conversation', bad)
      expect(readStoredConversation()).toEqual({ kind: 'unset' })
    }
  })
})
