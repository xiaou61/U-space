import { Alert, AlertDescription, AlertTitle, Badge, Button, Spinner } from '@paideia/ui'
import { Sparkles, X } from 'lucide-react'
import { useEffect, useState } from 'react'

import type { AssistantApi } from './client'
import { ConversationPanel } from './conversation-panel'
import { CreditsPanel } from './credits-panel'
import { MemoryPanel } from './memory-panel'
import { useAssistantContext } from './context'
import { SummaryCardView } from './summary-card'
import type { CredentialView, NoteDraftInput, SummaryView, SummaryVisibility } from './types'

/** 四个标签页。后两个与条目无关，所以换条目时不该把它们挤走。 */
type Tab = 'chat' | 'summary' | 'memory' | 'credits'

/**
 * 悬浮助手壳：贯穿学习者端的常驻入口。
 *
 * <p>它只做一件事——把当前条目精简成一张卡片。**没有对话输入框**（用户裁决）：多轮、记忆与
 * 工具属于后续工作项，现在就摆一个输入框会让人以为它能聊。
 *
 * <p>三条与后端语义对应的界面规则：
 * <ol>
 *   <li>打开条目**不自动生成**（用户裁决：只给按钮）——自动生成会无条件花掉额度。</li>
 *   <li>命中缓存时按"本次未产生调用"表述，而不是显示 0 token。</li>
 *   <li>**AI 服务由平台统一配置**（WORK-009）：没配好时给的是说明，不是让用户去填 Key；
 *       余额为 0 时给的是"没有自动充值，请联系管理员"，不是一句泛泛的失败。</li>
 * </ol>
 */
export function AssistantShell({
  api,
  onSaveNote,
}: {
  api: AssistantApi
  /** 由应用注入：本包不认识笔记，只负责把"这一段话"交出去。 */
  onSaveNote?: (note: NoteDraftInput, entryId: number | null) => Promise<unknown>
}) {
  const { entryId, entryTitle, onlyMine, setOnlyMine } = useAssistantContext()
  const [expanded, setExpanded] = useState(false)
  const [credential, setCredential] = useState<CredentialView | null>(null)
  const [view, setView] = useState<SummaryView | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  /**
   * 标签页。
   *
   * <p>默认值取决于有没有条目：打开某条内容时多半就是想精简它；没有条目时只剩对话可做
   * （用户裁决：助手任意处可开，条目只是**可选**上下文）。条目变化时重算——"从没打开任何内容
   * 进到某个条目"这一步，正是最可能需要精简的时刻。
   */
  const [tab, setTab] = useState<Tab>(entryId === null ? 'chat' : 'summary')

  useEffect(() => {
    // 换条目只影响"对话/精简"的默认落点；用户手点过「记忆」或「积分」就留在那一页——
    // 正在看"它记住了什么/花了多少"时被跳走会很讨厌
    setTab((current) => (current === 'memory' || current === 'credits' ? current : entryId === null ? 'chat' : 'summary'))
  }, [entryId])

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
        // 服务状态读不出来不该打断阅读：壳会退化成"未就绪"，按钮保持禁用
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
      aria-label="AI 助手"
    >
      <header className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <Sparkles className="size-4" />
          <span className="text-sm font-medium">AI 助手</span>
          <Badge variant="outline" data-testid="assistant-credential-state">
            {credential?.configured === true ? 'AI 服务已就绪' : 'AI 服务未配置'}
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

      <nav className="flex gap-1" data-testid="assistant-tabs">
        <Button
          variant={tab === 'chat' ? 'secondary' : 'ghost'}
          size="sm"
          data-testid="assistant-tab-chat"
          onClick={() => setTab('chat')}
        >
          对话
        </Button>
        <Button
          variant={tab === 'summary' ? 'secondary' : 'ghost'}
          size="sm"
          data-testid="assistant-tab-summary"
          onClick={() => setTab('summary')}
        >
          精简
        </Button>
        <Button
          variant={tab === 'memory' ? 'secondary' : 'ghost'}
          size="sm"
          data-testid="assistant-tab-memory"
          onClick={() => setTab('memory')}
        >
          记忆
        </Button>
        <Button
          variant={tab === 'credits' ? 'secondary' : 'ghost'}
          size="sm"
          data-testid="assistant-tab-credits"
          onClick={() => setTab('credits')}
        >
          积分
        </Button>
      </nav>

      {tab === 'chat' ? (
        <ConversationPanel api={api} onSaveNote={onSaveNote} />
      ) : tab === 'memory' ? (
        <MemoryPanel api={api} />
      ) : tab === 'credits' ? (
        <CreditsPanel api={api} />
      ) : entryId === null ? (
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
                  AI 服务尚未由平台配置，请在管理端配置上游凭据后使用。
                </p>
              ) : null}
            </div>
          ) : (
            <SummaryCardView view={visible} />
          )}

          <footer className="flex items-center justify-end gap-2">
            <label className="text-muted-foreground flex items-center gap-1.5 text-xs">
              <input
                type="checkbox"
                checked={onlyMine}
                onChange={(event) => setOnlyMine(event.target.checked)}
                data-testid="assistant-only-mine"
              />
              只看我的
            </label>
          </footer>
        </>
      )}
    </aside>
  )
}
