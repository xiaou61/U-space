import { Alert, AlertDescription, AlertTitle, Button, Spinner } from '@paideia/ui'
import { History, Plus, Send } from 'lucide-react'
import { useCallback, useEffect, useRef, useState } from 'react'
import { Streamdown } from 'streamdown'

import type { AssistantApi } from './client'
import { useAssistantContext } from './context'
import { readStoredConversation, storeConversation } from './session-store'
import type { ChatMessage, ConversationView, NoteDraftInput } from './types'

/**
 * 对话面板：多轮 + 流式 + 思考折叠，**并记住上次那一段**（WORK-011）。
 *
 * <p>四条与后端语义对应的规则：
 * <ol>
 *   <li><b>思考默认折叠</b>（用户裁决）。用原生 `<details>`：展开／收起是平台能力，
 *       不需要为它维护一份状态。</li>
 *   <li><b>打开就回到上次的会话</b>，而不是每次从零开始；**只有点「新开对话」才另起一段**。</li>
 *   <li><b>换条目不再静默清空</b>：那条条目有绑定的会话就切过去；没有就保持当前会话不动。
 *       旧实现一换条目就把上下文丢掉，那是用户抱怨最直接的一点。</li>
 *   <li><b>用户收起面板即中断上游调用</b>：不断开的话，这次调用会继续计费到用户看不见的地方。
 *       收起 = 本组件卸载（外壳收起时走的是另一条 return 分支），所以卸载时的 abort 就够。</li>
 * </ol>
 *
 * <p>没有条目上下文时照样能聊（用户裁决：助手任意处可开，条目只是**可选**上下文）。
 *
 * <p>`onSaveNote` 由**应用**注入而不是本包直接调笔记接口：本包不认识笔记，让应用把"存进笔记"
 * 这件事接上。
 */
export function ConversationPanel({
  api,
  onSaveNote,
}: {
  api: AssistantApi
  onSaveNote?: (note: NoteDraftInput, entryId: number | null) => Promise<unknown>
}) {
  const { entryId, entryTitle } = useAssistantContext()
  const [conversationId, setConversationId] = useState<number | null>(null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [sessions, setSessions] = useState<ConversationView[]>([])
  const [listOpen, setListOpen] = useState(false)
  /** 正在从服务端恢复历史——期间不显示"问点什么吧"的空态，否则会闪一下"没有对话"。 */
  const [restoring, setRestoring] = useState(true)
  const [input, setInput] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [savedIndexes, setSavedIndexes] = useState<number[]>([])
  const [rememberedIndexes, setRememberedIndexes] = useState<number[]>([])
  const [drafts, setDrafts] = useState<{ title: string; body: string }[]>([])
  const [savedDrafts, setSavedDrafts] = useState<number[]>([])
  const abortRef = useRef<AbortController | null>(null)
  /**
   * 用户是否已经自己动过这一段（发消息 / 新开 / 切换）。
   *
   * <p>挂载时的"恢复上次会话"是**异步**的（先列会话、再拉消息），而用户可能在这几十毫秒里已经
   * 点了发送。恢复完成后如果照旧写入，就会把在飞的那一轮 **abort 掉**（实测报
   * `signal is aborted without reason`）或把消息覆盖回历史。所以一旦用户动过，恢复就作废。
   */
  const touchedRef = useRef(false)

  /** 切到某条会话：读回它的消息（旧 → 新）并记住。 */
  const openConversation = useCallback(
    async (id: number, options?: { abort?: boolean }) => {
      // 恢复那一路**不许打断**用户已经发起的调用（见 touchedRef 的注释）
      if (options?.abort !== false) {
        abortRef.current?.abort()
        abortRef.current = null
      }
      setConversationId(id)
      storeConversation(id)
      setDrafts([])
      setError(null)
      try {
        const history = await api.messages(id)
        if (touchedRef.current) {
          return
        }
        setMessages(
          history.map((message) => ({
            role: message.role,
            content: message.content,
            // 思考不落库，历史里没有它
            thinking: '',
            usage: message.usage,
            emptyHint: false,
            createdAt: message.createdAt,
          })),
        )
      } catch (cause) {
        // 会话过期（7 天滑动）是常态：清掉本地记忆，退回"新的一段"，而不是留一个打不开的 id
        storeConversation(null)
        setConversationId(null)
        setMessages([])
        setError(cause instanceof Error ? cause.message : String(cause))
      }
    },
    [api],
  )

  /** 第一次挂载：按本地记的三态决定看哪一段（见 session-store 的注释）。 */
  useEffect(() => {
    let alive = true
    void (async () => {
      try {
        const list = await api.conversations()
        if (!alive || touchedRef.current) {
          return
        }
        setSessions(list)
        const stored = readStoredConversation()
        if (stored.kind === 'new') {
          // 刚点过「新开对话」：保持空白，**不要**再去恢复旧的
          return
        }
        if (stored.kind === 'id') {
          await openConversation(stored.id, { abort: false })
          return
        }
        // 从没选过：恢复最近一条；有当前条目时优先绑在这个条目上的那条
        const bound = list.find((item) => item.entryId !== null && item.entryId === entryId)
        const target = bound ?? list[0]
        if (target !== undefined) {
          await openConversation(target.id, { abort: false })
        }
      } catch (cause) {
        if (alive) {
          setError(cause instanceof Error ? cause.message : String(cause))
        }
      } finally {
        if (alive) {
          setRestoring(false)
        }
      }
    })()
    return () => {
      alive = false
    }
    // 只在挂载时跑一次：之后换条目由下面那个 effect 处理（且**不许**清空）
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [api, openConversation])

  /** 换条目：该条目有绑定的会话就切过去，**没有就保持当前这一段**（不再静默清空）。 */
  useEffect(() => {
    const bound = sessions.find((item) => item.entryId !== null && item.entryId === entryId)
    if (bound !== undefined && bound.id !== conversationId) {
      void openConversation(bound.id)
    }
    // sessions 变化不该触发"切换"，只看 entryId
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [entryId])

  // 卸载即中断：面板收起来之后还在计费的调用是纯浪费
  useEffect(() => () => abortRef.current?.abort(), [])

  /** 新开一段：清空界面并**把本地记成"空白"**，这样关掉再打开也不会把旧的拉回来。 */
  const startNew = () => {
    touchedRef.current = true
    abortRef.current?.abort()
    abortRef.current = null
    setConversationId(null)
    storeConversation(null)
    setMessages([])
    setDrafts([])
    setSavedIndexes([])
    setRememberedIndexes([])
    setError(null)
    setListOpen(false)
  }

  const refreshSessions = async () => {
    try {
      setSessions(await api.conversations())
    } catch {
      // 列表拉不到不影响当前这段对话，静默即可
    }
  }

  const patchLast = (patch: (message: ChatMessage) => ChatMessage) =>
    setMessages((previous) =>
      previous.map((message, index) => (index === previous.length - 1 ? patch(message) : message)),
    )

  const send = async () => {
    const text = input.trim()
    if (text === '' || busy) {
      return
    }
    touchedRef.current = true
    setBusy(true)
    setError(null)
    setInput('')
    setMessages((previous) => [
      ...previous,
      { role: 'user', content: text, thinking: '', usage: null, emptyHint: false },
      { role: 'assistant', content: '', thinking: '', usage: null, emptyHint: false },
    ])

    const controller = new AbortController()
    abortRef.current = controller
    try {
      let id = conversationId
      if (id === null) {
        id = (await api.createConversation(entryId)).id
        setConversationId(id)
        storeConversation(id)
      }
      await api.sendMessage(
        id,
        text,
        (event) => {
          if (event.type === 'thinking') {
            patchLast((message) => ({ ...message, thinking: message.thinking + (event.delta ?? '') }))
          } else if (event.type === 'content') {
            patchLast((message) => ({ ...message, content: message.content + (event.delta ?? '') }))
          } else if (event.type === 'draft') {
            if (event.draft !== undefined) {
              setDrafts((previous) => [...previous, event.draft as { title: string; body: string }])
            }
          } else if (event.type === 'done') {
            patchLast((message) => ({
              ...message,
              usage: event.usage ?? null,
              emptyHint: event.empty === true,
            }))
          } else if (event.type === 'error') {
            setError(event.message ?? '生成失败，请稍后再试')
          }
        },
        controller.signal,
      )
      // 这一轮落库了，列表里的更新时间跟着变——顺手刷新，免得列表显示的时间是旧的
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause))
      // 这一轮没成：把占位的空气泡去掉，否则看起来像"助手回了空内容"
      setMessages((previous) => {
        const last = previous[previous.length - 1]
        if (last !== undefined && last.role === 'assistant' && last.content === '' && last.thinking === '') {
          return previous.slice(0, -1)
        }
        return previous
      })
    } finally {
      setBusy(false)
      abortRef.current = null
      // **成功与失败都要刷**：这一轮可能已经建好了会话（例如被积分闸拦下时），
      // 不刷的话「历史」按钮不会出现，用户以为这一段没被记住。
      void refreshSessions()
    }
  }

  const current = sessions.find((item) => item.id === conversationId)

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-2" data-testid="conversation-panel">
      <div className="flex items-center justify-between gap-2">
        <p className="text-muted-foreground min-w-0 flex-1 truncate text-xs" data-testid="conversation-context">
          {entryId === null ? '没有条目上下文，可直接提问。' : `上下文：${entryTitle ?? ''}`}
        </p>
        <Button
          variant="ghost"
          size="sm"
          disabled={busy}
          data-testid="conversation-new"
          onClick={startNew}
        >
          <Plus className="size-3.5" />
          新开对话
        </Button>
        {sessions.length === 0 ? null : (
          <Button
            variant="ghost"
            size="sm"
            aria-expanded={listOpen}
            data-testid="conversation-history"
            onClick={() => setListOpen((value) => !value)}
          >
            <History className="size-3.5" />
            历史
          </Button>
        )}
      </div>

      {listOpen ? (
        <ul className="border-border max-h-48 space-y-1 overflow-y-auto rounded border p-1" data-testid="conversation-sessions">
          {sessions.map((item) => (
            <li key={item.id}>
              <button
                type="button"
                data-testid={`conversation-session-${item.id}`}
                data-active={item.id === conversationId ? 'true' : undefined}
                onClick={() => {
                  setListOpen(false)
                  void openConversation(item.id)
                }}
                className={`hover:bg-accent/60 flex w-full items-center justify-between gap-2 rounded px-2 py-1 text-left text-xs ${
                  item.id === conversationId ? 'bg-accent text-accent-foreground' : 'text-muted-foreground'
                }`}
              >
                <span className="truncate">
                  {item.updatedAt === null ? '较早的一段' : new Date(item.updatedAt).toLocaleString('zh-CN', { hour12: false })}
                </span>
                {item.entryId === null ? null : <span className="shrink-0 opacity-70">条目 {item.entryId}</span>}
              </button>
            </li>
          ))}
        </ul>
      ) : null}

      <div className="min-h-[8rem] flex-1 space-y-3 overflow-y-auto" data-testid="conversation-messages">
        {restoring ? (
          <Spinner />
        ) : messages.length === 0 ? (
          <p className="text-muted-foreground text-sm">问点什么吧，比如让它解释当前内容里的一个概念。</p>
        ) : (
          messages.map((message, index) => (
            <article
              key={index}
              data-testid={`conversation-message-${message.role}`}
              className={message.role === 'user' ? 'text-right' : ''}
            >
              {message.thinking === '' ? null : (
                <details
                  className="border-border mb-1 rounded border px-2 py-1 text-xs"
                  data-testid="conversation-thinking"
                >
                  <summary className="text-muted-foreground cursor-pointer">思考过程</summary>
                  <p className="text-muted-foreground mt-1 whitespace-pre-wrap">{message.thinking}</p>
                </details>
              )}
              {message.content === '' ? (
                message.role === 'assistant' && busy ? <Spinner /> : null
              ) : message.role === 'user' ? (
                <p className="bg-muted inline-block rounded px-2 py-1 text-sm whitespace-pre-wrap">
                  {message.content}
                </p>
              ) : message.emptyHint ? (
                // 这条是**我们自己**给的提示文案（"思考 token 吃满了"那类），不是模型输出——不走 markdown
                <p className="text-muted-foreground text-sm whitespace-pre-wrap">{message.content}</p>
              ) : (
                /**
                 * 模型输出：**按 markdown 渲染**（WORK-012）。
                 *
                 * <p>`isAnimating` 只影响动效；**"已完成块不重解析"**由 Streamdown 的块级 memo 保证，
                 * 不依赖这个开关。
                 */
                <Streamdown isAnimating={busy} className="text-sm">
                  {message.content}
                </Streamdown>
              )}
              {message.usage === null ? null : (
                <p className="text-muted-foreground text-[11px]" data-testid="conversation-usage">
                  本次用量：{message.usage.totalTokens} tokens（输入 {message.usage.inputTokens}／输出{' '}
                  {message.usage.outputTokens}）
                </p>
              )}
              {/* 只对"真正的回答"给这两个入口：空正文的提示不该被存成笔记或记忆 */}
              {message.role !== 'assistant' || message.content === '' || message.emptyHint ? null : (
                <div className="mt-1 flex items-center gap-2">
                  {onSaveNote === undefined ? null : (
                    <Button
                      variant="ghost"
                      size="sm"
                      data-testid="conversation-save-to-note"
                      onClick={() => {
                        if (onSaveNote === undefined) {
                          return
                        }
                        void onSaveNote({ body: message.content }, entryId)
                          .then(() => {
                            setError(null)
                            setSavedIndexes((previous) => [...previous, index])
                          })
                          .catch((cause: unknown) =>
                            setError(cause instanceof Error ? cause.message : String(cause)),
                          )
                      }}
                    >
                      存进笔记
                    </Button>
                  )}
                  <Button
                    variant="ghost"
                    size="sm"
                    data-testid="conversation-remember"
                    onClick={() => {
                      void api
                        .remember(message.content)
                        .then(() => {
                          setError(null)
                          setRememberedIndexes((previous) => [...previous, index])
                        })
                        .catch((cause: unknown) =>
                          setError(cause instanceof Error ? cause.message : String(cause)),
                        )
                    }}
                  >
                    记住这条
                  </Button>
                  {savedIndexes.includes(index) ? (
                    <span className="text-muted-foreground text-[11px]" data-testid="conversation-saved">
                      已存进笔记
                    </span>
                  ) : null}
                  {rememberedIndexes.includes(index) ? (
                    <span className="text-muted-foreground text-[11px]" data-testid="conversation-remembered">
                      已记住
                    </span>
                  ) : null}
                </div>
              )}
            </article>
          ))
        )}
      </div>

      {/* 助手提出的笔记草稿：**点了才存**。它不是回答的一部分，所以单独放在消息区下面 */}
      {drafts.length === 0 ? null : (
        <div className="space-y-1" data-testid="conversation-drafts">
          {drafts.map((draft, index) => (
            <div key={index} className="border-border rounded border px-2 py-1" data-testid="conversation-draft">
              <p className="text-xs font-medium">{draft.title}</p>
              <p className="text-muted-foreground text-xs whitespace-pre-wrap">{draft.body}</p>
              <div className="mt-1 flex items-center gap-2">
                {onSaveNote === undefined ? null : (
                  <Button
                    size="sm"
                    data-testid={`conversation-draft-save-${index}`}
                    onClick={() => {
                      if (onSaveNote === undefined) {
                        return
                      }
                      void onSaveNote({ title: draft.title, body: draft.body }, entryId)
                        .then(() => {
                          setError(null)
                          setSavedDrafts((previous) => [...previous, index])
                        })
                        .catch((cause: unknown) =>
                          setError(cause instanceof Error ? cause.message : String(cause)),
                        )
                    }}
                  >
                    保存为笔记
                  </Button>
                )}
                <span className="text-muted-foreground text-[11px]">
                  {savedDrafts.includes(index) ? '已保存' : '助手只是拟好，点了才存'}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {error === null ? null : (
        <Alert variant="destructive" data-testid="conversation-error">
          <AlertTitle>没能完成</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {current === undefined ? null : (
        <p className="text-muted-foreground text-[11px]" data-testid="conversation-current">
          正在这一段对话里（消息会留在服务端 7 天）
        </p>
      )}

      <form
        className="flex items-end gap-2"
        onSubmit={(event) => {
          event.preventDefault()
          void send()
        }}
      >
        <textarea
          value={input}
          onChange={(event) => setInput(event.target.value)}
          rows={2}
          placeholder="问点什么…"
          data-testid="conversation-input"
          className="border-border bg-background min-h-0 flex-1 resize-none rounded border px-2 py-1 text-sm"
        />
        <Button type="submit" size="sm" disabled={busy || input.trim() === ''} data-testid="conversation-send">
          {busy ? <Spinner /> : <Send className="size-4" />}
          发送
        </Button>
      </form>
    </div>
  )
}
