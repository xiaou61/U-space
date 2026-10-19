import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'

/** 一次对话的公共行为（WORK-020 起新增 tutor）。 */
export type AssistantHarness = 'assistant' | 'interview' | 'tutor'

/** 悬浮壳能看到的"当前内容"。 */
export interface AssistantContextValue {
  entryId: number | null
  entryTitle: string | null
  /**
   * 这一页的助手用哪种行为。**默认通用助手**；课程条目页会设成 `tutor`（WORK-020）。
   *
   * <p>为什么放在页面而不是壳里：只有页面知道"我正在看的是不是一门课"。
   * 壳只负责把它传给建会话的请求——行为会记在会话上，恢复时不丢。
   */
  harness: AssistantHarness
  /** 只影响显示范围（把别人的公开结果折叠掉），**不是权限**：私有结果由服务端的查询拦。 */
  onlyMine: boolean
}

/**
 * 一次"预填"请求（WORK-014）：源码解读的「问 AI」把一句话送进输入框。
 *
 * <p>**它不是一条待发送的消息**——`ask()` 只写这里，真正的发送仍然要用户点。
 * 带 `id` 是因为同一句话可能被连点两次：消费方按 id 清一次，避免把后面的新请求也清掉。
 */
export interface PendingPrompt {
  id: number
  text: string
}

interface AssistantContextApi extends AssistantContextValue {
  setEntry: (entry: { id: number; title: string; harness?: AssistantHarness } | null) => void
  setOnlyMine: (onlyMine: boolean) => void
  pendingPrompt: PendingPrompt | null
  /** 打开助手并把这句话填进输入框；**不发送**。 */
  ask: (text: string) => void
  clearPendingPrompt: (id: number) => void
}

const AssistantContext = createContext<AssistantContextApi | null>(null)

/**
 * 当前内容的提供者。
 *
 * <p>为什么要一个上下文而不是让壳自己去读路由：条目页是唯一知道"当前在读哪一条"的地方
 * （它已经在加载详情了）。壳只需要知道 id 与标题，页面把它交出来即可——壳不必了解路由结构，
 * 将来换路由方案也不用改壳。
 *
 * <p>`onlyMine` 存在这里而不是服务端：它只是**显示偏好**，收窄自己能看的东西，
 * 不构成任何权限——私有结果在服务端就查不出来。
 */
export function AssistantProvider({ children }: { children: ReactNode }) {
  const [entryId, setEntryId] = useState<number | null>(null)
  const [entryTitle, setEntryTitle] = useState<string | null>(null)
  const [harness, setHarness] = useState<AssistantHarness>('assistant')
  const [onlyMine, setOnlyMine] = useState(false)
  const [pendingPrompt, setPendingPrompt] = useState<PendingPrompt | null>(null)

  // setEntry 必须**稳定**：调用方（条目页）把它放进 useEffect 的依赖里，
  // 每次渲染都换一个新函数会让那个 effect 反复跑。
  const setEntry = useCallback((entry: { id: number; title: string; harness?: AssistantHarness } | null) => {
    setEntryId(entry === null ? null : entry.id)
    setEntryTitle(entry === null ? null : entry.title)
    // 离开条目页时回到通用助手：否则"上一次看的是课程"会让之后每个页面的助手都用导师口径
    setHarness(entry === null ? 'assistant' : entry.harness ?? 'assistant')
  }, [])

  const ask = useCallback((text: string) => {
    const trimmed = text.trim()
    if (trimmed === '') {
      return
    }
    setPendingPrompt({ id: Date.now(), text: trimmed })
  }, [])

  const clearPendingPrompt = useCallback((id: number) => {
    setPendingPrompt((current) => (current?.id === id ? null : current))
  }, [])

  const value = useMemo<AssistantContextApi>(
    () => ({
      entryId,
      entryTitle,
      harness,
      onlyMine,
      setEntry,
      setOnlyMine,
      pendingPrompt,
      ask,
      clearPendingPrompt,
    }),
    [entryId, entryTitle, harness, onlyMine, setEntry, pendingPrompt, ask, clearPendingPrompt],
  )

  return <AssistantContext.Provider value={value}>{children}</AssistantContext.Provider>
}

export function useAssistantContext(): AssistantContextApi {
  const value = useContext(AssistantContext)
  if (value === null) {
    throw new Error('useAssistantContext 必须在 AssistantProvider 内使用')
  }
  return value
}
