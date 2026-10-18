import { createIdentityApi, configFor, type LearnerProfile } from '@paideia/identity'
import { type AssistantApi, type InterviewEvaluationView } from '@paideia/assistant'
import { useQuery } from '@tanstack/react-query'
import {
  Alert,
  AlertDescription,
  AlertTitle,
  AppShell,
  Button,
  Card,
  CardContent,
  Input,
  PageHeader,
  Separator,
  Spinner,
} from '@paideia/ui'
import { useCallback, useEffect, useRef, useState } from 'react'

import { api } from '../api'
import { createAssistantApi } from '@paideia/assistant'
import { LearnerHeader } from '../components/LearnerHeader'

const identity = createIdentityApi(api)
const assistant: AssistantApi = createAssistantApi(api)

/** 半场对话里的一条。面试页自己维护，不复用助手的会话面板（那里面向"随手问一句"）。 */
interface Line {
  role: 'interviewer' | 'candidate'
  content: string
  thinking?: string
}

/**
 * 模拟面试（WORK-010 T-09 / T-11）。
 *
 * <p><b>行为由会话决定，不由页面决定</b>：会话建的时候带 `harness=interview`，之后即使从通用助手
 * 继续这段对话，说话的还是面试官。页面只是"发起这段会话的地方"。
 *
 * <p><b>笔记默认不参与</b>：本页**不把任何笔记内容送给上游**。用户在开关上勾选后，才把
 * （他自己写的）笔记正文拼进第一条消息。这是"私有数据离开本机要用户自己点"的保守默认
 * （design §5.1）。一期先只做提示语与勾选状态，不真的读取笔记接口——那需要笔记 API 的
 * 批量读，属后续；眼下把"默认不送"这条做成界面上的**明示**。
 *
 * <p><b>评分是独立的一次调用</b>：点「生成点评」才发，返回结构化评分并落库。
 */
export function InterviewPage() {
  const { data: profile } = useQuery({ queryKey: ['profile'], queryFn: () => identity.profile(), retry: false })
  const config = configFor(profile?.identity)

  const [conversationId, setConversationId] = useState<number | null>(null)
  const [lines, setLines] = useState<Line[]>([])
  const [input, setInput] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [evaluation, setEvaluation] = useState<InterviewEvaluationView | null>(null)
  const [reviewing, setReviewing] = useState(false)
  /** 一次面试只要建过会话，就可能已经落库了消息——这里只用来控制按钮的可用性。 */
  const started = conversationId !== null

  /** 当前这轮在飞的请求；离开页面时中断它（与助手那条同一纪律：不能让上游继续烧钱）。 */
  const abortRef = useRef<AbortController | null>(null)
  useEffect(() => () => {
    abortRef.current?.abort()
  }, [])

  const send = useCallback(async (text: string, openingTurn = false) => {
    if (text.trim() === '' || busy) {
      return
    }
    setError(null)
    setBusy(true)
    setLines((current) => [...current, { role: 'candidate', content: text }])
    setInput('')

    let id = conversationId
    try {
      if (id === null) {
        // 会话建的时候就钉死 harness：这决定了"谁在说话"，不该由页面在每轮里重复指定
        const created = await assistant.createConversation(null, 'interview')
        id = created.id
        setConversationId(id)
      }

      const controller = new AbortController()
      abortRef.current = controller
      const message = openingTurn ? openingMessage(profile, config.interviewIntro, text) : text

      const interviewer: Line = { role: 'interviewer', content: '' }
      setLines((current) => [...current, interviewer])

      await assistant.sendMessage(
        id,
        message,
        (event) => {
          if (event.type === 'content' && event.delta !== undefined) {
            setLines((current) => updateLast(current, 'interviewer', (line) => ({
              ...line,
              content: line.content + (event.delta ?? ''),
            })))
          } else if (event.type === 'thinking' && event.delta !== undefined) {
            setLines((current) => updateLast(current, 'interviewer', (line) => ({
              ...line,
              thinking: (line.thinking ?? '') + (event.delta ?? ''),
            })))
          } else if (event.type === 'error') {
            setError(event.message ?? '生成失败，请稍后再试')
          }
        },
        controller.signal,
      )
    } catch (cause) {
      if (!isAbort(cause)) {
        setError(cause instanceof Error ? cause.message : String(cause))
      }
    } finally {
      abortRef.current = null
      setBusy(false)
    }
  }, [busy, conversationId, profile, config.interviewIntro])

  const start = () => {
    void send(`开始面试。目标岗位：${profile?.targetRole ?? '未填写'}。`, true)
  }

  const review = async () => {
    if (conversationId === null) {
      return
    }
    setReviewing(true)
    setError(null)
    try {
      setEvaluation(await assistant.reviewInterview(conversationId))
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause))
    } finally {
      setReviewing(false)
    }
  }

  return (
    <AppShell title="Paideia" actions={<LearnerHeader />}>
      <div className="mx-auto max-w-3xl space-y-6">
        <PageHeader
          eyebrow="求职者"
          title="模拟面试"
          description="一次一个问题，答不上来就说不会。随时可以点「生成点评」拿一份结构化评分。"
        />

        <Alert>
          <AlertTitle>关于隐私</AlertTitle>
          <AlertDescription>
            这场对话会发给你配置的 AI 服务（第三方）。
            <strong>你的笔记默认不参与</strong>——只有你自己选择带上时才会发出去。
          </AlertDescription>
        </Alert>

        {lines.length === 0 ? (
          <Card>
            <CardContent className="space-y-4 pt-6">
              <p className="text-sm">{config.interviewIntro}</p>
              <Button data-testid="interview-start" disabled={busy} onClick={start}>
                {busy ? '开场中…' : '开始面试'}
              </Button>
              {busy ? <Spinner /> : null}
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-3" data-testid="interview-transcript">
            {lines.map((line, index) => (
              <article
                key={index}
                data-testid={`interview-line-${line.role}`}
                className={line.role === 'candidate' ? 'text-right' : ''}
              >
                {line.thinking === undefined || line.thinking === '' ? null : (
                  <details className="border-border mb-1 rounded border px-2 py-1 text-xs">
                    <summary className="text-muted-foreground cursor-pointer">思考过程</summary>
                    <p className="text-muted-foreground mt-1 whitespace-pre-wrap">{line.thinking}</p>
                  </details>
                )}
                <p
                  className={
                    line.role === 'candidate'
                      ? 'bg-muted inline-block rounded px-2 py-1 text-sm whitespace-pre-wrap'
                      : 'text-sm whitespace-pre-wrap'
                  }
                >
                  {line.content}
                </p>
              </article>
            ))}
          </div>
        )}

        {started ? (
          <div className="flex gap-2">
            <Input
              data-testid="interview-input"
              placeholder="回答…（Enter 发送）"
              value={input}
              disabled={busy}
              onChange={(event) => { setInput(event.target.value) }}
              onKeyDown={(event) => {
                if (event.key === 'Enter' && !event.shiftKey) {
                  event.preventDefault()
                  void send(input)
                }
              }}
            />
            <Button data-testid="interview-send" disabled={busy || input.trim() === ''} onClick={() => { void send(input) }}>
              发送
            </Button>
            <Button variant="outline" data-testid="interview-review" disabled={reviewing || busy} onClick={() => { void review() }}>
              {reviewing ? '点评中…' : '生成点评'}
            </Button>
          </div>
        ) : null}

        {error === null ? null : (
          <Alert variant="destructive">
            <AlertTitle>没成功</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {evaluation === null ? null : <EvaluationCard view={evaluation} />}
      </div>
    </AppShell>
  )
}

/** 评估卡片：总分 + 各维度 + 优点 + 改进点。 */
function EvaluationCard({ view }: { view: InterviewEvaluationView }) {
  const { evaluation } = view
  return (
    <Card data-testid="interview-evaluation">
      <CardContent className="space-y-4 pt-6">
        <div className="flex items-baseline gap-3">
          <h2 className="text-lg font-semibold">评估结果</h2>
          <span className="text-3xl font-semibold" data-testid="interview-overall">{evaluation.overall}</span>
          <span className="text-muted-foreground text-sm">/ 5</span>
        </div>
        <Separator />
        <dl className="space-y-3">
          {evaluation.dimensions.map((dimension) => (
            <div key={dimension.name} data-testid="interview-dimension">
              <div className="flex items-center justify-between text-sm">
                <dt className="font-medium">{dimension.name}</dt>
                <dd className="text-muted-foreground">{dimension.score} / 5</dd>
              </div>
              <p className="text-muted-foreground mt-1 text-sm">{dimension.comment}</p>
            </div>
          ))}
        </dl>
        {evaluation.improvements.length === 0 ? null : (
          <>
            <Separator />
            <div>
              <h3 className="text-sm font-medium">可以改进的地方</h3>
              <ul className="mt-2 list-disc space-y-1 pl-5 text-sm">
                {evaluation.improvements.map((item) => (
                  <li key={item} data-testid="interview-improvement">{item}</li>
                ))}
              </ul>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  )
}

/** 把"开始面试"变成一个模型看得懂的请求：目标岗位 + 本页约定的开场说明。 */
function openingMessage(profile: LearnerProfile | undefined, intro: string, fallback: string): string {
  const role = profile?.targetRole ?? ''
  const head = role === '' ? fallback : `开始面试。目标岗位：${role}。`
  return `${head}\n（本场约定：${intro}）`
}

/** 替换最后一条指定角色的消息。用函数式更新避免闭包里拿到旧数组。 */
function updateLast(lines: Line[], role: Line['role'], patch: (line: Line) => Line): Line[] {
  const index = lines.map((line) => line.role).lastIndexOf(role)
  if (index < 0) {
    return lines
  }
  return lines.map((line, position) => (position === index ? patch(line) : line))
}

/** 用户主动离开或被中断都不该显示成错误。 */
function isAbort(cause: unknown): boolean {
  return cause instanceof DOMException && cause.name === 'AbortError'
}
