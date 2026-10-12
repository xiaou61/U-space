import { Alert, AlertDescription, AlertTitle, Button, Spinner } from '@paideia/ui'
import { Send } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'

import type { AssistantApi } from './client'
import { useAssistantContext } from './context'
import type { ChatMessage } from './types'

/**
 * 对话面板：多轮 + 流式 + 思考折叠。
 *
 * <p>三条与后端语义对应的规则：
 * <ol>
 *   <li><b>思考默认折叠</b>（用户裁决）。用原生 `<details>` 实现：展开／收起是平台能力，
 *       不需要为它维护一份状态，也不会因此多一次渲染。</li>
 *   <li><b>换条目就开新会话</b>：旧会话绑在另一条内容上，接着用会让上下文对不上——
 *       用户以为在问新条目，助手答的却是旧条目。</li>
 *   <li><b>用户关掉面板即中断上游调用</b>：不断开的话，这次调用会继续计费到用户看不见的地方
 *       （BYOK，花的是他自己的钱）。</li>
 * </ol>
 *
 * <p>没有条目上下文时照样能聊（用户裁决：助手任意处可开，条目只是**可选**上下文）。
 *
 * <p>`onSaveToNote` 由**应用**注入而不是本包直接调笔记接口：本包不认识笔记（它只认识助手），
 * 让应用把"存进笔记"这件事接上，本包就不必知道笔记模块存在。
 */
export function ConversationPanel({
  api,
  onSaveToNote,
}: {
  api: AssistantApi
  onSaveToNote?: (text: string, entryId: number | null) => Promise<unknown>
}) {
  const { entryId, entryTitle } = useAssistantContext()
  const [conversationId, setConversationId] = useState<number | null>(null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  /** 已存进笔记的消息下标：同一个回答可以再存一次（用户可能删过那条笔记），所以不锁按钮。 */
  const [savedIndexes, setSavedIndexes] = useState<number[]>([])
  /** 同上的"已记住"。 */
  const [rememberedIndexes, setRememberedIndexes] = useState<number[]>([])
  const abortRef = useRef<AbortController | null>(null)

  // 换条目：中断当前流并重开一个会话
  useEffect(() => {
    abortRef.current?.abort()
    abortRef.current = null
    setConversationId(null)
    setMessages([])
    setError(null)
  }, [entryId])

  // 卸载即中断：面板收起来之后还在计费的调用是纯浪费
  useEffect(() => () => abortRef.current?.abort(), [])

  const patchLast = (patch: (message: ChatMessage) => ChatMessage) =>
    setMessages((previous) =>
      previous.map((message, index) => (index === previous.length - 1 ? patch(message) : message)),
    )

  const send = async () => {
    const text = input.trim()
    if (text === '' || busy) {
      return
    }
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
      }
      await api.sendMessage(
        id,
        text,
        (event) => {
          if (event.type === 'thinking') {
            patchLast((message) => ({ ...message, thinking: message.thinking + (event.delta ?? '') }))
          } else if (event.type === 'content') {
            patchLast((message) => ({ ...message, content: message.content + (event.delta ?? '') }))
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
    }
  }

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-2">
      <p className="text-muted-foreground truncate text-xs" data-testid="conversation-context">
        {entryId === null ? '没有条目上下文，可直接提问。' : `上下文：${entryTitle ?? ''}`}
      </p>

      <div className="min-h-[8rem] flex-1 space-y-3 overflow-y-auto" data-testid="conversation-messages">
        {messages.length === 0 ? (
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
              ) : (
                <p
                  className={
                    message.role === 'user'
                      ? 'bg-muted inline-block rounded px-2 py-1 text-sm whitespace-pre-wrap'
                      : message.emptyHint
                        ? 'text-muted-foreground text-sm whitespace-pre-wrap'
                        : 'text-sm whitespace-pre-wrap'
                  }
                >
                  {message.content}
                </p>
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
                  {/* 存进笔记要宿主注入回调（本包不认识笔记）；没注入就不显示这个按钮 */}
                  {onSaveToNote === undefined ? null : (
                    <Button
                      variant="ghost"
                      size="sm"
                      data-testid="conversation-save-to-note"
                      onClick={() => {
                        // 闭包里 TS 不再知道上面的 undefined 判断成立，所以这里再守一次
                        if (onSaveToNote === undefined) {
                          return
                        }
                        void onSaveToNote(message.content, entryId)
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
                  {/* 「记住这条」不需要外部注入：记忆就是助手自己的数据 */}
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

      {error === null ? null : (
        <Alert variant="destructive" data-testid="conversation-error">
          <AlertTitle>没能完成</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
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
