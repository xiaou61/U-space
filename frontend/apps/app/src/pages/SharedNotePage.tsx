import { Alert, AlertDescription, AlertTitle, Card, CardContent, CardHeader, CardTitle, Spinner } from '@paideia/ui'
import { useEffect, useState } from 'react'
import { useParams } from 'react-router'

/** 分享端点返回的形状。**没有作者、没有时间**——服务端就不给。 */
interface SharedNote {
  title: string
  body: string
  entryId: number | null
}

/**
 * 只读分享页。**不需要登录**（用户裁决 12）。
 *
 * <p><b>用裸 fetch 而不是应用的 `api` 客户端</b>：那个客户端会附令牌、并在 401 时刷新重放——
 * 这两件事在这个页面上都没有意义（匿名访问者没有令牌，而这个端点也从不返回 401）。
 * 走它反而会让一次"分享链接失效"触发一次没必要的刷新尝试。
 *
 * <p>这里**不做 markdown 渲染**：分享的是用户自己的笔记（纯文本），而渲染 md 会引入
 * "笔记里的 HTML/链接在别人浏览器里执行"这一整类问题。站外页面按纯文本显示最稳。
 */
export function SharedNotePage() {
  const { shareId } = useParams<{ shareId: string }>()
  const [note, setNote] = useState<SharedNote | null>(null)
  const [missing, setMissing] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let alive = true
    if (shareId === undefined) {
      setMissing(true)
      return
    }
    fetch(`/api/v1/public/shares/${encodeURIComponent(shareId)}`)
      .then(async (response) => {
        if (!alive) {
          return
        }
        if (response.status === 404) {
          // 过期、撤回、下架、编造的 id —— 服务端统一 404，这里也统一一句话，
          // 不区分原因（区分开等于告诉访问者"这条分享曾经存在"）
          setMissing(true)
          return
        }
        if (!response.ok) {
          throw new Error(`请求失败：HTTP ${response.status}`)
        }
        const envelope = (await response.json()) as { data: SharedNote }
        setNote(envelope.data)
      })
      .catch((cause: unknown) => {
        if (alive) {
          setError(cause instanceof Error ? cause.message : String(cause))
        }
      })
    return () => {
      alive = false
    }
  }, [shareId])

  return (
    <main className="mx-auto max-w-2xl px-6 py-16">
      <h1 className="text-muted-foreground mb-6 text-sm">Paideia · 分享的笔记</h1>

      {error === null ? null : (
        <Alert variant="destructive">
          <AlertTitle>没能打开</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {missing ? (
        <p className="text-muted-foreground" data-testid="share-missing">
          这条分享不存在或已失效。
        </p>
      ) : note === null ? (
        error === null ? <Spinner /> : null
      ) : (
        <Card>
          <CardHeader>
            <CardTitle data-testid="share-title">{note.title}</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="whitespace-pre-wrap" data-testid="share-body">
              {note.body}
            </p>
            {note.entryId === null ? null : (
              <p className="text-muted-foreground mt-4 text-xs" data-testid="share-entry">
                相关条目 #{note.entryId}
              </p>
            )}
          </CardContent>
        </Card>
      )}
    </main>
  )
}
