import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'

/** 悬浮壳能看到的"当前内容"。 */
export interface AssistantContextValue {
  entryId: number | null
  entryTitle: string | null
  /** 只影响显示范围（把别人的公开结果折叠掉），**不是权限**：私有结果由服务端的查询拦。 */
  onlyMine: boolean
}

interface AssistantContextApi extends AssistantContextValue {
  setEntry: (entry: { id: number; title: string } | null) => void
  setOnlyMine: (onlyMine: boolean) => void
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
  const [onlyMine, setOnlyMine] = useState(false)

  // setEntry 必须**稳定**：调用方（条目页）把它放进 useEffect 的依赖里，
  // 每次渲染都换一个新函数会让那个 effect 反复跑。
  const setEntry = useCallback((entry: { id: number; title: string } | null) => {
    setEntryId(entry === null ? null : entry.id)
    setEntryTitle(entry === null ? null : entry.title)
  }, [])

  const value = useMemo<AssistantContextApi>(
    () => ({ entryId, entryTitle, onlyMine, setEntry, setOnlyMine }),
    [entryId, entryTitle, onlyMine, setEntry],
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
