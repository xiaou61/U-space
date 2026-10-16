import { Alert, AlertDescription, AlertTitle, Badge, Button, Spinner } from '@paideia/ui'
import { Sparkles, X } from 'lucide-react'
import { useCallback, useEffect, useRef, useState } from 'react'

import type { AssistantApi } from './client'
import { ConversationPanel } from './conversation-panel'
import { CreditsPanel } from './credits-panel'
import { MemoryPanel } from './memory-panel'
import { useAssistantContext } from './context'
import { SummaryCardView } from './summary-card'
import type { CredentialView, NoteDraftInput, SummaryView, SummaryVisibility } from './types'

/** 四个标签页。后三个与条目无关，所以换条目时不该把它们挤走。 */
type Tab = 'chat' | 'summary' | 'memory' | 'credits'

/** 面板状态的本地键。展开与否 + 宽度都是**这台设备上的使用状态**，不进服务端（WORK-011）。 */
const PANEL_KEY = 'paideia:assistant-panel'
const DEFAULT_WIDTH = 384
const MIN_WIDTH = 360
const MAX_WIDTH = 520

interface PanelState {
  open: boolean
  width: number
}

function readPanelState(): PanelState {
  try {
    const raw = window.localStorage.getItem(PANEL_KEY)
    if (raw !== null) {
      const parsed = JSON.parse(raw) as Partial<PanelState>
      const width = Number(parsed.width)
      return {
        open: parsed.open === true,
        width: Number.isFinite(width) ? Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, width)) : DEFAULT_WIDTH,
      }
    }
  } catch {
    // 隐私模式、坏值：一律当"没设过"
  }
  return { open: false, width: DEFAULT_WIDTH }
}

function storePanelState(state: PanelState): void {
  try {
    window.localStorage.setItem(PANEL_KEY, JSON.stringify(state))
  } catch {
    // 写不进去只影响下次打开时的形态，不影响本次
  }
}

/** 视口是否匹配。用来做窄屏降级——**这是布局判断，不是渲染分支的唯一依据**（面板照常挂载）。 */
function useMediaQuery(query: string): boolean {
  const [matches, setMatches] = useState(() =>
    typeof window === 'undefined' ? false : window.matchMedia(query).matches,
  )
  useEffect(() => {
    const list = window.matchMedia(query)
    const sync = () => setMatches(list.matches)
    sync()
    list.addEventListener('change', sync)
    return () => list.removeEventListener('change', sync)
  }, [query])
  return matches
}

/**
 * 助手外壳：**右侧停靠的工作区**（WORK-011 起，不再是右下角浮窗）。
 *
 * <p>三条与后端语义对应的界面规则：
 * <ol>
 *   <li>打开条目**不自动生成**（用户裁决：只给按钮）——自动生成会无条件花掉额度。</li>
 *   <li>命中缓存时按"本次未产生调用"表述，而不是显示 0 token。</li>
 *   <li>**AI 服务由平台统一配置**（WORK-009）：没配好时给的是说明，不是让用户去填 Key；
 *       余额为 0 时给的是"没有自动充值，请联系管理员"，不是一句泛泛的失败。</li>
 * </ol>
 *
 * <p>形态（WORK-011，照调研给的规格）：停靠右侧、宽度可拖（360–520，默认 384）、可收成一条窄条、
 * 展开与宽度**存本地**；展开且屏幕够宽（≥1024）时在根元素上设 `--assistant-panel-w`，
 * 让内容整体左移而不是被压窄；窄屏（<640）降级成底部抽屉。
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
  const initial = readPanelState()
  const [expanded, setExpanded] = useState(initial.open)
  const [width, setWidth] = useState(initial.width)
  const [credential, setCredential] = useState<CredentialView | null>(null)
  const [view, setView] = useState<SummaryView | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [tab, setTab] = useState<Tab>(entryId === null ? 'chat' : 'summary')

  /** 窄到放不下侧栏时：面板改成覆盖/抽屉，**不给内容让位**（避免把正文压成一条）。 */
  const compact = useMediaQuery('(max-width: 1023px)')
  const drawer = useMediaQuery('(max-width: 639px)')

  useEffect(() => {
    // 换条目只影响"对话/精简"的默认落点；用户手点过另外三页就留在那一页
    setTab((current) =>
      current === 'memory' || current === 'credits' ? current : entryId === null ? 'chat' : 'summary',
    )
  }, [entryId])

  // 展开/宽度变了就落本地：刷新后保持，且**导航不会把它改回去**（本组件是常驻的）
  useEffect(() => {
    storePanelState({ open: expanded, width })
  }, [expanded, width])

  /**
   * 让内容让位：**只在展开且屏幕够宽时**设根变量。
   *
   * <p>收起或窄屏时把它清掉（而不是设 0）——这样 `AppShell` 的 `var(--assistant-panel-w, 0px)`
   * 回到缺省，管理端与展览页那种从不设它的场景保持原样。
   */
  useEffect(() => {
    const root = document.documentElement
    if (expanded && !compact) {
      root.style.setProperty('--assistant-panel-w', `${String(width)}px`)
    } else {
      root.style.removeProperty('--assistant-panel-w')
    }
    return () => {
      // 注意：`removeProperty` **返回被移除的旧值**（string），所以清理函数必须用块体包起来，
      // 写成 `() => root.style.removeProperty(...)` 会被 React 当成析构函数类型不匹配而报错。
      root.style.removeProperty('--assistant-panel-w')
    }
  }, [expanded, compact, width])

  /** 快捷键开关面板（键位属实现选择：不与浏览器/编辑器常用键冲突）。 */
  const toggle = useCallback(() => setExpanded((value) => !value), [])
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'j') {
        event.preventDefault()
        toggle()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [toggle])

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

  // ── 收起态：右侧一条窄条。**不是完全消失**——否则用户找不到入口 ────────────────────────
  if (!expanded) {
    return (
      <button
        type="button"
        onClick={() => setExpanded(true)}
        data-testid="assistant-launcher"
        aria-label="打开 AI 助手"
        className="border-border bg-card hover:bg-muted fixed top-1/2 right-0 z-40 flex -translate-y-1/2 flex-col items-center gap-1 rounded-l-lg border border-r-0 px-1.5 py-3 text-xs shadow-lg"
      >
        <Sparkles className="size-4" />
        <span className="[writing-mode:vertical-rl]">助手</span>
      </button>
    )
  }

  // ── 展开态：停靠右侧（窄屏降级成底部抽屉） ──────────────────────────────────────────
  const position = drawer
    ? 'inset-x-0 bottom-0 h-[70dvh] rounded-t-2xl border-t'
    : 'top-0 right-0 h-dvh border-l'

  return (
    <aside
      className={`border-border bg-card fixed z-40 flex flex-col shadow-xl ${position}`}
      style={drawer ? undefined : { width }}
      data-testid="assistant-shell"
      aria-label="AI 助手"
    >
      {/* 拖拽把手：只有桌面端（抽屉模式下宽度无意义） */}
      {drawer ? null : (
        <div
          role="separator"
          aria-label="调整助手宽度"
          aria-orientation="vertical"
          tabIndex={-1}
          data-testid="assistant-resize"
          onPointerDown={(event) => {
            const startX = event.clientX
            const startWidth = width
            const target = event.currentTarget
            target.setPointerCapture(event.pointerId)
            const onMove = (move: PointerEvent) => {
              const next = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, startWidth - (move.clientX - startX)))
              setWidth(next)
            }
            const onUp = () => {
              target.releasePointerCapture(event.pointerId)
              target.removeEventListener('pointermove', onMove)
              target.removeEventListener('pointerup', onUp)
            }
            target.addEventListener('pointermove', onMove)
            target.addEventListener('pointerup', onUp)
          }}
          onKeyDown={(event) => {
            // 键盘也能调：左右箭头 ±16px（无障碍要求，不是可选装饰）
            if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
              event.preventDefault()
              const step = event.key === 'ArrowLeft' ? 16 : -16
              setWidth((value) => Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, value + step)))
            }
          }}
          className="hover:bg-primary/30 absolute top-0 left-0 h-full w-1 cursor-col-resize"
        />
      )}

      <header className="border-border flex shrink-0 items-center justify-between gap-2 border-b px-3 py-2">
        <div className="flex min-w-0 items-center gap-2">
          <Sparkles className="size-4 shrink-0" />
          <span className="shrink-0 text-sm font-medium">AI 助手</span>
          <Badge variant="outline" data-testid="assistant-credential-state" className="shrink-0">
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

      <nav className="border-border flex shrink-0 gap-1 border-b px-2 py-2" data-testid="assistant-tabs">
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

      <div className="flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto p-3">
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

            <footer className="mt-auto flex items-center justify-end gap-2">
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
      </div>
    </aside>
  )
}
