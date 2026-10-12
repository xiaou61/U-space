import { Button, Spinner } from '@paideia/ui'
import { useEffect, useState } from 'react'

import type { AssistantApi } from './client'
import type { MemoryView } from './types'

/**
 * 记忆面板：看它记住了什么、逐条删掉（用户裁决 13）。
 *
 * <p>放在助手壳里而不是另开一个设置页：记忆是**这一个助手**的东西，与凭据对话框同一类。
 * 顺着"它记住我什么"这个疑问点开壳就能看到，比让人去别处找要直接。
 *
 * <p>**没有编辑**：用户只选了查看与删除（第 13 条）。改记忆的正确做法是让助手重新记一条，
 * 而不是手改一行——手改会让"这条是谁写的"变得含糊。
 */
export function MemoryPanel({ api }: { api: AssistantApi }) {
  const [items, setItems] = useState<MemoryView[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const reload = () => {
    api
      .memories()
      .then(setItems)
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : String(cause)))
  }

  useEffect(reload, [api])

  const remove = async (item: MemoryView) => {
    setBusy(true)
    setError(null)
    try {
      await api.deleteMemory(item.source, item.id)
      // 重新拉整份而不是本地删一条：服务端返回的是"是否真的删掉"，
      // 本地推断会和它对不上（比如这条本来就不是本人的）
      reload()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="space-y-2" data-testid="memory-panel">
      <p className="text-muted-foreground text-xs">助手只为你本人记住以下内容，可逐条删除。</p>

      {error === null ? null : <p className="text-destructive text-xs">{error}</p>}

      {items === null ? (
        <Spinner />
      ) : items.length === 0 ? (
        <p className="text-muted-foreground text-sm" data-testid="memory-empty">
          还没有任何记忆。
        </p>
      ) : (
        <ul className="space-y-1">
          {items.map((item) => (
            <li
              key={`${item.source}-${item.id}`}
              data-testid="memory-item"
              className="border-border flex items-start justify-between gap-2 rounded border px-2 py-1"
            >
              <span className="text-sm break-words">{item.text}</span>
              <Button
                variant="ghost"
                size="sm"
                disabled={busy}
                aria-label="删除这条记忆"
                data-testid={`memory-delete-${item.source}-${item.id}`}
                onClick={() => void remove(item)}
              >
                删除
              </Button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
