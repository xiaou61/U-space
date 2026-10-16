import { Alert, AlertDescription, AlertTitle, Badge, Button, preloadMarkdown, Spinner } from '@paideia/ui'
import { Sparkles, X } from 'lucide-react'
import { useCallback, useEffect, useRef, useState } from 'react'

import type { AssistantApi } from './client'
import { ConversationPanel } from './conversation-panel'
import { CreditsPanel } from './credits-panel'
import { MemoryPanel } from './memory-panel'
import { useAssistantContext } from './context'
import { hasSeenHint, markHintSeen } from './session-store'
import { SummaryCardView } from './summary-card'
import type { CredentialView, NoteDraftInput, SummaryView, SummaryVisibility } from './types'

/** 四个标签页。后三个与条目无关，所以换条目时不该把它们挤走。 */
type Tab = 'chat' | 'summary' | 'memory' | 'credits'

/**
 * 面板的本地状态（WORK-011，2026-10-16 按用户反馈改成**浮动对话框**）。
 *
 * <p>位置与尺寸都存本地：这是**这台设备上的使用状态**，不是学习数据。存下来的位置可能因为窗口
 * 变小而跑到屏幕外，所以每次读出来都要夹回可视区（见 {@link clampToViewport}）。
 */
const PANEL_KEY = 'paideia:assistant-panel'
const DEFAULT_W = 384
const DEFAULT_H = 560
const MIN_W = 320
const MIN_H = 360
const MAX_W = 720
const MAX_H = 900
/** 与视口边缘至少留这么多，且拖拽时也要留住它——否则面板会被拖到只剩一条边。 */
const MARGIN = 16

interface PanelState {
  open: boolean
  x: number
  y: number
  w: number
  h: number
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value))
}

/** 把面板夹回可视区：至少留下 MARGIN 的可见部分，且不超出右/下边缘。 */
function clampToViewport(state: PanelState): PanelState {
  const vw = typeof window === 'undefined' ? 1280 : window.innerWidth
  const vh = typeof window === 'undefined' ? 800 : window.innerHeight
  const w = clamp(state.w, MIN_W, Math.max(MIN_W, Math.min(MAX_W, vw - MARGIN * 2)))
  const h = clamp(state.h, MIN_H, Math.max(MIN_H, Math.min(MAX_H, vh - MARGIN * 2)))
  return {
    open: state.open,
    w,
    h,
    x: clamp(state.x, MARGIN, Math.max(MARGIN, vw - w - MARGIN)),
    y: clamp(state.y, MARGIN, Math.max(MARGIN, vh - h - MARGIN)),
  }
}

function defaultState(): PanelState {
  const vw = typeof window === 'undefined' ? 1280 : window.innerWidth
  const vh = typeof window === 'undefined' ? 800 : window.innerHeight
  return clampToViewport({
    open: false,
    w: DEFAULT_W,
    h: DEFAULT_H,
    x: vw - DEFAULT_W - 24,
    y: vh - DEFAULT_H - 24,
  })
}

function readPanelState(): PanelState {
  const fallback = defaultState()
  try {
    const raw = window.localStorage.getItem(PANEL_KEY)
    if (raw === null) {
      return fallback
    }
    const parsed = JSON.parse(raw) as Partial<PanelState>
    const numbers = [parsed.x, parsed.y, parsed.w, parsed.h].every((value) => typeof value === 'number')
    if (!numbers) {
      return fallback
    }
    return clampToViewport({
      open: parsed.open === true,
      x: parsed.x as number,
      y: parsed.y as number,
      w: parsed.w as number,
      h: parsed.h as number,
    })
  } catch {
    return fallback
  }
}

function storePanelState(state: PanelState): void {
  try {
    window.localStorage.setItem(PANEL_KEY, JSON.stringify(state))
  } catch {
    // 写不进去只影响下次打开时的形态，不影响本次
  }
}

/**
 * 助手外壳：**可随处拖动的浮动对话框**（WORK-011，2026-10-16 按用户反馈改向）。
 *
 * <p><b>为什么不是停靠侧栏</b>：调研那份材料把"浮动小挂件"列为反模式，但它讲的是 **B2B 后台**
 * （用户在那里是"处理一条记录"，面板该贴住对象）。这里是**阅读界面**：用户要的是"随手问一句，
 * 然后把它拖开继续读"。用户 2026-10-16 明确要"随心所欲拖动、任意区域、小小的对话框、不要固定到
 * 某个界面"——所以形态以此为准。
 *
 * <p>三条与后端语义对应的界面规则不变：打开条目**不自动生成**；命中缓存按"本次未产生调用"表述；
 * **AI 服务由平台统一配置**，没配好时给说明而不是让用户填 Key。
 *
 * <p>拖动与调尺寸都**落本地**（位置、宽高、展开与否），刷新后保持。
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
  const [panel, setPanel] = useState<PanelState>(readPanelState)
  const [credential, setCredential] = useState<CredentialView | null>(null)
  const [view, setView] = useState<SummaryView | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [tab, setTab] = useState<Tab>(entryId === null ? 'chat' : 'summary')
  /** 正在拖拽/缩放时不落本地——松手才写，省得每帧一次 localStorage。 */
  const dragging = useRef(false)

  useEffect(() => {
    setTab((current) =>
      current === 'memory' || current === 'credits' ? current : entryId === null ? 'chat' : 'summary',
    )
  }, [entryId])

  useEffect(() => {
    if (!dragging.current) {
      storePanelState(panel)
    }
  }, [panel])

  // 视口变小（窗口缩小、转屏）时把面板夹回来，否则它会留在屏幕外再也点不到
  useEffect(() => {
    const onResize = () => setPanel((current) => clampToViewport(current))
    window.addEventListener('resize', onResize)
    return () => window.removeEventListener('resize', onResize)
  }, [])

  const toggle = useCallback(() => {
    // 一旦打开过就不再提示：首次指引只给一次，动效也随之消失
    markHintSeen()
    // 同时把 markdown 渲染器（约 350KB）的下载**与用户打字并行**起来：
    // 否则第一个 token 到达时才开始下载，那一段回答会是纯文本（WORK-012 T-03）
    preloadMarkdown()
    setPanel((current) => ({ ...current, open: !current.open }))
  }, [])
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
        if (alive) {
          setCredential(null)
        }
      })
    return () => {
      alive = false
    }
  }, [api])

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

  /** 拖拽标题栏 / 拖拽握把共用：按位移改位置或尺寸，松手才落本地。 */
  const beginDrag = (event: React.PointerEvent<HTMLElement>, mode: 'move' | 'resize') => {
    event.preventDefault()
    const target = event.currentTarget
    // 指针起点与面板原值**必须分开存**：`{x: clientX, ...panel}` 会被 spread 覆盖掉，位移就全错了
    const pointer = { x: event.clientX, y: event.clientY }
    const origin = { x: panel.x, y: panel.y, w: panel.w, h: panel.h }
    target.setPointerCapture(event.pointerId)
    dragging.current = true
    const onMove = (move: PointerEvent) => {
      const dx = move.clientX - pointer.x
      const dy = move.clientY - pointer.y
      setPanel((current) =>
        mode === 'move'
          ? clampToViewport({ ...current, x: origin.x + dx, y: origin.y + dy, open: true })
          : clampToViewport({ ...current, w: origin.w + dx, h: origin.h + dy, open: true }),
      )
    }
    const onUp = () => {
      dragging.current = false
      target.releasePointerCapture(event.pointerId)
      target.removeEventListener('pointermove', onMove)
      target.removeEventListener('pointerup', onUp)
      setPanel((current) => {
        storePanelState(current)
        return current
      })
    }
    target.addEventListener('pointermove', onMove)
    target.addEventListener('pointerup', onUp)
  }

  // ── 收起态：一个**显眼**的悬浮入口，首次进入时给一次指引 ──────────────────────────
  if (!panel.open) {
    const firstTime = !hasSeenHint()
    return (
      <div className="fixed right-6 bottom-6 z-40 flex flex-col items-end gap-2">
        {firstTime ? (
          <div
            className="border-border bg-card text-muted-foreground rounded-lg border px-3 py-1.5 text-xs shadow-lg"
            data-testid="assistant-hint"
          >
            有不懂的可以问我
          </div>
        ) : null}
        <button
          type="button"
          onClick={toggle}
          data-testid="assistant-launcher"
          aria-label="打开 AI 助手"
          title="打开 AI 助手（Ctrl/Cmd + J）"
          className={`border-border bg-card hover:bg-accent flex items-center gap-2 rounded-full border px-4 py-3 text-sm font-medium shadow-xl ${
            // `motion-safe:` 让"减少动效"的用户拿到的是静止版本（无障碍要求，不是可选装饰）
            firstTime ? 'motion-safe:animate-pulse' : ''
          }`}
        >
          <Sparkles className="size-4" />
          AI 助手
        </button>
      </div>
    )
  }

  // ── 展开态：浮动对话框，位置与尺寸都来自本地状态 ──────────────────────────────────
  return (
    <section
      className="border-border bg-card fixed z-40 flex flex-col overflow-hidden rounded-xl border shadow-2xl"
      style={{ left: panel.x, top: panel.y, width: panel.w, height: panel.h }}
      data-testid="assistant-shell"
      aria-label="AI 助手"
    >
      {/* 标题栏＝拖动区：整条都能拖，不只是一小块 */}
      <header
        onPointerDown={(event) => beginDrag(event, 'move')}
        className="border-border bg-muted/40 flex shrink-0 cursor-move touch-none items-center justify-between gap-2 border-b px-3 py-2"
        data-testid="assistant-drag"
      >
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
          onPointerDown={(event) => event.stopPropagation()}
          onClick={() => setPanel((current) => ({ ...current, open: false }))}
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

      {/* 右下角缩放握把：往上/往左都是变大（与既有 e2e 的拖拽方向一致） */}
      <div
        role="separator"
        aria-label="调整助手大小"
        tabIndex={-1}
        data-testid="assistant-resize"
        onPointerDown={(event) => beginDrag(event, 'resize')}
        onKeyDown={(event) => {
          if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
            event.preventDefault()
            const step = event.key === 'ArrowLeft' ? 24 : -24
            setPanel((current) => clampToViewport({ ...current, w: current.w + step }))
          }
        }}
        className="text-muted-foreground/60 hover:text-foreground absolute right-0 bottom-0 flex size-5 cursor-nwse-resize touch-none items-end justify-end pr-0.5 pb-0.5"
      >
        <svg viewBox="0 0 12 12" className="size-2.5" aria-hidden="true">
          <path d="M11 1v10H1" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </svg>
      </div>
    </section>
  )
}
