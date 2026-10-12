import { Alert, AlertDescription, AlertTitle, Button, Card, CardContent, Input, Spinner } from '@paideia/ui'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'

import { LearnerHeader } from '../components/LearnerHeader'
import { notes, type NoteSummary } from '../notes-api'

/**
 * 我的笔记：左侧列表、右侧编辑。
 *
 * <p>三条取舍：
 * <ul>
 *   <li><b>列表项不含正文</b>（服务端就不返回）：一个几百条的列表没必要把正文都拽回来。</li>
 *   <li><b>保存是显式的</b>：不做自动保存。自动保存要在"什么时候算一次编辑"上做一堆猜测，
 *       而写错一个字就被自动存下去，比多按一次按钮更让人不安。</li>
 *   <li><b>删除要二次确认</b>：笔记是用户自己写的东西，误删没有撤销。</li>
 * </ul>
 */
export function NotesPage() {
  const queryClient = useQueryClient()
  const [selected, setSelected] = useState<number | null>(null)
  const [title, setTitle] = useState('')
  const [body, setBody] = useState('')
  const [error, setError] = useState<string | null>(null)

  const list = useQuery({ queryKey: ['notes'], queryFn: () => notes.list() })
  const detail = useQuery({
    queryKey: ['note', selected],
    queryFn: () => notes.read(selected as number),
    enabled: selected !== null,
  })

  // 选中项变化或详情回来时，把草稿同步成服务端的值
  useEffect(() => {
    if (detail.data !== undefined) {
      setTitle(detail.data.title)
      setBody(detail.data.body)
    }
  }, [detail.data])

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['notes'] })
    void queryClient.invalidateQueries({ queryKey: ['note', selected] })
  }

  const create = useMutation({
    mutationFn: () => notes.create({ entryId: null, title: '未命名笔记', body: '' }),
    onSuccess: (created) => {
      setError(null)
      setSelected(created.id)
      invalidate()
    },
    onError: (cause: unknown) => setError(cause instanceof Error ? cause.message : String(cause)),
  })

  const save = useMutation({
    mutationFn: () => notes.update(selected as number, { entryId: detail.data?.entryId ?? null, title, body }),
    onSuccess: () => {
      setError(null)
      invalidate()
    },
    onError: (cause: unknown) => setError(cause instanceof Error ? cause.message : String(cause)),
  })

  const remove = useMutation({
    mutationFn: (id: number) => notes.remove(id),
    onSuccess: () => {
      setError(null)
      setSelected(null)
      invalidate()
    },
    onError: (cause: unknown) => setError(cause instanceof Error ? cause.message : String(cause)),
  })

  return (
    <div className="mx-auto max-w-5xl px-6 py-8">
      <header className="mb-6 flex items-center justify-between gap-4">
        <h1 className="text-2xl font-medium">我的笔记</h1>
        <LearnerHeader />
      </header>

      {error === null ? null : (
        <Alert variant="destructive" data-testid="notes-error" className="mb-4">
          <AlertTitle>没能完成</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-4 md:grid-cols-[16rem_1fr]">
        <Card>
          <CardContent className="space-y-2 py-4">
            <Button
              size="sm"
              className="w-full"
              disabled={create.isPending}
              data-testid="notes-create"
              onClick={() => create.mutate()}
            >
              {create.isPending ? <Spinner /> : null}
              新建笔记
            </Button>
            {list.isLoading ? (
              <Spinner />
            ) : (list.data ?? []).length === 0 ? (
              <p className="text-muted-foreground text-sm" data-testid="notes-empty">
                还没有笔记。新建一条，或让助手把答案存进来。
              </p>
            ) : (
              <ul className="space-y-1" data-testid="notes-list">
                {(list.data ?? []).map((item: NoteSummary) => (
                  <li key={item.id}>
                    <button
                      type="button"
                      data-testid={`note-item-${item.id}`}
                      onClick={() => setSelected(item.id)}
                      className={`w-full rounded px-2 py-1 text-left text-sm ${
                        selected === item.id ? 'bg-muted font-medium' : 'hover:bg-muted/60'
                      }`}
                    >
                      {item.title}
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardContent className="space-y-3 py-4">
            {selected === null ? (
              <p className="text-muted-foreground text-sm" data-testid="notes-placeholder">
                左侧选一条，或新建。
              </p>
            ) : detail.isLoading ? (
              <Spinner />
            ) : (
              <>
                <Input
                  value={title}
                  aria-label="标题"
                  data-testid="note-title"
                  onChange={(event) => setTitle(event.target.value)}
                />
                <textarea
                  value={body}
                  rows={16}
                  aria-label="正文"
                  data-testid="note-body"
                  onChange={(event) => setBody(event.target.value)}
                  className="border-border bg-background w-full rounded border px-2 py-1 font-mono text-sm"
                />
                <div className="flex items-center gap-2">
                  <Button size="sm" disabled={save.isPending} data-testid="note-save" onClick={() => save.mutate()}>
                    {save.isPending ? <Spinner /> : null}
                    保存
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    disabled={remove.isPending}
                    data-testid="note-delete"
                    onClick={() => {
                      // 二次确认：笔记是用户自己写的东西，误删没有撤销
                      if (window.confirm('删除这条笔记？删了不可恢复。')) {
                        remove.mutate(selected)
                      }
                    }}
                  >
                    删除
                  </Button>
                  {detail.data?.entryId === null || detail.data === undefined ? null : (
                    <span className="text-muted-foreground text-xs" data-testid="note-entry">
                      挂靠条目 #{detail.data.entryId}
                    </span>
                  )}
                </div>
              </>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
