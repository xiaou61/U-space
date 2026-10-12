import { Alert, AlertDescription, AlertTitle, Badge, Button, Spinner } from '@paideia/ui'
import { Sparkles, X } from 'lucide-react'
import { useEffect, useState } from 'react'

import type { AssistantApi } from './client'
import { CredentialDialog } from './credential-dialog'
import { useAssistantContext } from './context'
import { SummaryCardView } from './summary-card'
import type { CredentialView, SummaryView, SummaryVisibility } from './types'

/**
 * 悬浮助手壳：贯穿学习者端的常驻入口。
 *
 * <p>它只做一件事——把当前条目精简成一张卡片。**没有对话输入框**（用户裁决）：多轮、记忆与
 * 工具属于后续工作项，现在就摆一个输入框会让人以为它能聊。
 *
 * <p>三条与后端语义对应的界面规则：
 * <ol>
 *   <li>打开条目**不自动生成**（用户裁决：只给按钮）——自动生成会无条件花掉用户自己的额度。</li>
 *   <li>命中缓存时按"本次未产生调用"表述，而不是显示 0 token。</li>
 *   <li>没配凭据时给的是**配置入口**，不是错误；已有他人公开结果时照常可读。</li>
 * </ol>
 */
export function AssistantShell({ api }: { api: AssistantApi }) {
  const { entryId, entryTitle, onlyMine, setOnlyMine } = useAssistantContext()
  const [expanded, setExpanded] = useState(false)
  const [credential, setCredential] = useState<CredentialView | null>(null)
  const [view, setView] = useState<SummaryView | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [credentialOpen, setCredentialOpen] = useState(false)

  useEffect(() => {
    let alive = true
    api
      .credential()
      .then((value) => {
        if (alive) {
          setCredential(value)
        }
      })
      .catch(() => {
        // 凭据读不出来不该打断阅读：壳会退化成"配置入口"
        if (alive) {
          setCredential(null)
        }
      })
    return () => {
      alive = false
    }
  }, [api])

  // 换条目时清掉上一条的结果：留在界面上会让人以为新条目已经精简过了
  useEffect(() => {
    setView(null)
    setError(null)
    if (entryId === null) {
      setExpanded(false)
      return
    }
    let alive = true
    api
      .summary(entryId)
      .then((value) => {
        if (alive) {
          setView(value)
        }
      })
      .catch(() => {
        if (alive) {
          setView(null)
        }
      })
    return () => {
      alive = false
    }
  }, [api, entryId])

  const generate = async (visibility: SummaryVisibility) => {
    if (entryId === null) {
      return
    }
    setBusy(true)
    setError(null)
    try {
      setView(await api.generate(entryId, visibility))
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause))
    } finally {
      setBusy(false)
    }
  }

  const visible =
    view !== null && !(onlyMine && view.source === 'others' && view.visibility === 'public') ? view : null

  if (!expanded) {
    return (
      <button
        type="button"
        onClick={() => setExpanded(true)}
        data-testid="assistant-launcher"
        className="border-border bg-card hover:bg-muted fixed right-4 bottom-4 z-40 flex items-center gap-2 rounded-full border px-3 py-2 text-sm shadow-lg"
      >
        <Sparkles className="size-4" />
        助手
      </button>
    )
  }

  return (
    <aside
      className="border-border bg-card fixed right-4 bottom-4 z-40 flex max-h-[70vh] w-80 flex-col gap-3 overflow-y-auto rounded-lg border p-3 shadow-xl"
      data-testid="assistant-shell"
      aria-label="AI 精简助手"
    >
      <header className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <Sparkles className="size-4" />
          <span className="text-sm font-medium">AI 精简</span>
          <Badge variant="outline" data-testid="assistant-credential-state">
            {credential?.configured === true ? `已配置 ····${credential.apiKeyLast4}` : '未配置凭据'}
          </Badge>
        </div>
        <Button
          variant="ghost"
          size="icon-sm"
          aria-label="收起助手"
          data-testid="assistant-collapse"
          onClick={() => setExpanded(false)}
        >
          <X className="size-4" />
        </Button>
      </header>

      {entryId === null ? (
        <p className="text-muted-foreground text-sm" data-testid="assistant-no-entry">
          打开一条内容后可用。
        </p>
      ) : (
        <>
          <p className="text-muted-foreground truncate text-xs" title={entryTitle ?? undefined}>
            {entryTitle}
          </p>

          {error === null ? null : (
            <Alert variant="destructive" data-testid="assistant-error">
              <AlertTitle>没能完成</AlertTitle>
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {visible === null ? (
            <div className="space-y-2">
              <p className="text-muted-foreground text-sm">
                {onlyMine ? '没有你自己生成的精简结果。' : '这条内容还没有精简结果。'}
              </p>
              <div className="flex flex-wrap gap-2">
                <Button
                  size="sm"
                  disabled={busy || credential?.configured !== true}
                  onClick={() => void generate('public')}
                  data-testid="assistant-generate-public"
                >
                  {busy ? <Spinner /> : null}
                  生成并公开
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  disabled={busy || credential?.configured !== true}
                  onClick={() => void generate('private')}
                  data-testid="assistant-generate-private"
                >
                  只给自己
                </Button>
              </div>
              {credential?.configured !== true ? (
                <p className="text-muted-foreground text-xs">
                  精简需要你自己的模型凭据（调用费用由你自己的 Key 承担）。
                </p>
              ) : null}
            </div>
          ) : (
            <SummaryCardView view={visible} />
          )}

          <footer className="flex items-center justify-between gap-2">
            <label className="text-muted-foreground flex items-center gap-1.5 text-xs">
              <input
                type="checkbox"
                checked={onlyMine}
                onChange={(event) => setOnlyMine(event.target.checked)}
                data-testid="assistant-only-mine"
              />
              只看我的
            </label>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setCredentialOpen(true)}
              data-testid="assistant-open-credential"
            >
              {credential?.configured === true ? '更换凭据' : '配置凭据'}
            </Button>
          </footer>

          <CredentialDialog
            api={api}
            current={credential}
            open={credentialOpen}
            onClose={() => setCredentialOpen(false)}
            onSaved={() => {
              void api.credential().then(setCredential)
            }}
            onError={setError}
          />
        </>
      )}
    </aside>
  )
}
