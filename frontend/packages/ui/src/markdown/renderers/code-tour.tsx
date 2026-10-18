import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useEffect, useMemo, useState, type ReactNode } from 'react'
import type { CustomRendererProps } from 'streamdown'

import { Button } from '../../components/button'
import { AskButton, BlockFallback, BlockFrame, Pill } from './blocks'
import { useSourceReading } from './context'
import { lineCount, parseCodeTour, permalinkOf, type TourStep } from './schema'

/**
 * 代码走读块（WORK-014）。
 *
 * <p><b>逐行渲染而不是整块高亮</b>：`focus` 要精确到行，所以把 `code` 按行拆开、每行带
 * 真实行号（`step.line + index`）自己排版。代价是**这一步没有 token 级语法着色**——
 * 普通围栏代码块仍由 Shiki 上色。`ponytail:` 天花板：若要着色，可改用 `@streamdown/code`
 * 的 `highlight` API 按行切 token，但会引入异步状态与缓存，当前不值得。
 *
 * <p><b>预测先于解释</b>：带 `predict` 的步骤在作答前**不渲染解释**（否则"先猜"没有意义）。
 * 有一条「直接看解释」的逃生口，避免卡住时被强制答题。
 */
export function CodeTour({ code }: CustomRendererProps) {
  const parsed = useMemo(() => parseCodeTour(code), [code])
  const { activeStep, setActiveStep, entryTitle, progress, renderText } = useSourceReading()
  const [predicted, setPredicted] = useState<Record<number, number>>({})
  const [revealed, setRevealed] = useState<Record<number, boolean>>({})

  const tour = parsed.ok ? parsed.value : null
  const total = tour?.steps.length ?? 0
  /**
   * 进页面时**先看本地存到第几步**（WORK-014 AC-008）。
   *
   * <p>用「读一次、当默认值」而不是"挂载后 setState"：后者会先渲染第 0 步、再跳一下，
   * 而且那一瞬间写进进度的就是 0，等于把上次的位置抹掉。这里把已存的步骤当成
   * `activeStep` 的缺省值，用户一移动就被 `activeStep` 覆盖，两边不打架。
   */
  const savedStep = useMemo(() => {
    if (tour === null || progress === undefined) {
      return 0
    }
    return progress.read()?.tours[tour.id]?.step ?? 0
  }, [tour, progress])
  const raw = tour === null ? 0 : (activeStep[tour.id] ?? savedStep)
  const current = total === 0 ? 0 : Math.min(Math.max(raw, 0), total - 1)
  const step = tour?.steps[current]

  // 进度只在"当前步"变化时写一次；写失败在端口内部静默，不打扰阅读
  useEffect(() => {
    if (tour === null || progress === undefined) {
      return
    }
    progress.update({
      tours: { [tour.id]: { step: current, total, done: current >= total - 1 } },
    })
  }, [tour, current, total, progress])

  if (!parsed.ok) {
    return <BlockFallback title="代码走读" error={parsed.error} raw={code} />
  }
  if (tour === null || step === undefined) {
    return null
  }

  const lines = step.code.split('\n')
  const focus = new Set(step.focus ?? [])
  const answered = predicted[current]
  const skipExplain = step.predict !== undefined && answered === undefined && revealed[current] !== true

  const move = (next: number) => {
    setActiveStep(tour.id, Math.max(0, Math.min(next, total - 1)))
  }

  return (
    <BlockFrame
      title={tour.title ?? `走读 · ${tour.repo}`}
      testId="code-tour"
      actions={
        <>
          <Pill>
            {current + 1} / {total}
          </Pill>
          {progress === undefined ? null : <Pill tone="primary">已记录进度</Pill>}
        </>
      }
    >
      <div
        className="grid gap-0 lg:grid-cols-[minmax(0,1.15fr)_minmax(0,1fr)]"
        // 左右键切换步骤：容器自己接事件，不抢占全局快捷键
        onKeyDown={(event) => {
          if (event.key === 'ArrowRight') {
            event.preventDefault()
            move(current + 1)
          } else if (event.key === 'ArrowLeft') {
            event.preventDefault()
            move(current - 1)
          }
        }}
        tabIndex={-1}
        role="group"
        aria-label={`代码走读：${tour.title ?? tour.repo}`}
      >
        <div className="border-border max-h-[60dvh] min-w-0 overflow-x-auto overflow-y-auto border-b lg:max-h-none lg:border-r lg:border-b-0">
          <div className="min-w-max py-3 font-mono text-[12.5px] leading-6">
            {lines.map((line, index) => {
              const number = step.line + index
              const highlighted = focus.has(index + 1)
              return (
                <div
                  key={number}
                  data-testid={highlighted ? 'code-tour-focus-line' : undefined}
                  className={`flex gap-3 px-4 ${
                    highlighted ? 'bg-primary/10 border-primary border-l-2' : 'border-l border-transparent'
                  }`}
                >
                  <span className="text-muted-foreground w-10 shrink-0 text-right select-none">{number}</span>
                  <span className="whitespace-pre">{line === '' ? ' ' : line}</span>
                </div>
              )
            })}
          </div>
        </div>

        <div className="flex min-w-0 flex-col gap-4 p-4">
          <div>
            <div className="text-muted-foreground mb-1 text-[11px]">
              第 {current + 1} 步 · {tour.file}:{step.line}
            </div>
            <h4 className="text-base font-medium">{step.title}</h4>
          </div>

          <StepPredict
            step={step}
            answered={answered}
            revealed={revealed[current] === true}
            onAnswer={(choice) => setPredicted((previous) => ({ ...previous, [current]: choice }))}
            onReveal={() => setRevealed((previous) => ({ ...previous, [current]: true }))}
            renderText={renderText}
          />

          {skipExplain ? null : (
            <div className="text-sm leading-7" data-testid="code-tour-explain">
              {render(step.explain, renderText)}
            </div>
          )}

          {step.state === undefined ? null : <StatePanel step={step} />}

          <div className="mt-auto flex flex-wrap items-center gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              data-testid="code-tour-prev"
              disabled={current === 0}
              onClick={() => move(current - 1)}
            >
              <ChevronLeft className="size-3.5" />
              上一步
            </Button>
            <Button
              type="button"
              size="sm"
              data-testid="code-tour-next"
              disabled={current >= total - 1}
              onClick={() => move(current + 1)}
            >
              下一步
              <ChevronRight className="size-3.5" />
            </Button>
            <AskButton
              label="问 AI"
              prompt={`结合《${entryTitle ?? tour.title ?? '这篇内容'}》第 ${current + 1} 步（${tour.file}:${step.line}），解释「${step.title}」这一步为什么这样做。`}
            />
          </div>
        </div>
      </div>

      <footer className="border-border text-muted-foreground flex flex-wrap items-center gap-x-2 gap-y-1 border-t px-4 py-2.5 text-[11px]">
        <span className="font-mono">
          {tour.repo}@{tour.commit}
        </span>
        <span>·</span>
        <span className="font-mono break-all">
          {tour.file}:{step.line}-{step.line + lineCount(step.code) - 1}
        </span>
        <span>·</span>
        <span>{tour.license}</span>
        <a
          href={permalinkOf(tour, step)}
          target="_blank"
          rel="noreferrer noopener"
          className="text-primary ml-auto underline underline-offset-2"
        >
          查看 GitHub 原文 ↗
        </a>
      </footer>
    </BlockFrame>
  )
}

function render(text: string, renderText?: (markdown: string) => ReactNode): ReactNode {
  return renderText === undefined ? <p className="whitespace-pre-wrap">{text}</p> : renderText(text)
}

/** 预测题：答完才显示解释，同时留一条"直接看解释"的逃生口。 */
function StepPredict({
  step,
  answered,
  revealed,
  onAnswer,
  onReveal,
  renderText,
}: {
  step: TourStep
  answered: number | undefined
  revealed: boolean
  onAnswer: (choice: number) => void
  onReveal: () => void
  renderText?: (markdown: string) => ReactNode
}) {
  const predict = step.predict
  if (predict === undefined) {
    return null
  }
  const correct = answered === predict.answer
  const correctOption = predict.options[predict.answer]
  return (
    <div className="border-border bg-muted/30 rounded-lg border p-3" data-testid="code-tour-predict">
      <p className="mb-2 text-sm font-medium">{predict.question}</p>
      {answered === undefined && !revealed ? (
        <>
          <div className="grid gap-1.5">
            {predict.options.map((option, index) => (
              <button
                key={index}
                type="button"
                data-testid={`code-tour-predict-${index}`}
                onClick={() => onAnswer(index)}
                className="border-border bg-background hover:bg-muted rounded-md border px-3 py-2 text-left text-sm"
              >
                {option}
              </button>
            ))}
          </div>
          <button
            type="button"
            onClick={onReveal}
            className="text-muted-foreground mt-2 text-xs underline underline-offset-2"
          >
            直接看解释
          </button>
        </>
      ) : (
        <div className="space-y-2 text-sm">
          <p className={correct ? 'text-success' : 'text-destructive'}>
            {answered === undefined ? '已跳过预测。' : correct ? '✓ 猜对了' : '✗ 不对'}
            {answered !== undefined && !correct && correctOption !== undefined ? `（正确：${correctOption}）` : ''}
          </p>
          <div className="leading-7">{render(predict.reveal, renderText)}</div>
        </div>
      )}
    </div>
  )
}

/** 状态快照：变量、调用栈、堆对象、输出。作者不写就不渲染（不占位）。 */
function StatePanel({ step }: { step: TourStep }) {
  const state = step.state
  if (state === undefined) {
    return null
  }
  const empty =
    (state.vars?.length ?? 0) + (state.stack?.length ?? 0) + (state.heap?.length ?? 0) + (state.output?.length ?? 0) === 0
  if (empty) {
    return null
  }
  return (
    <div className="border-border rounded-lg border" data-testid="code-tour-state">
      <div className="border-border text-muted-foreground border-b px-3 py-1.5 text-[11px]">此刻的状态</div>
      <div className="grid gap-3 p-3 text-xs sm:grid-cols-2">
        {state.vars === undefined || state.vars.length === 0 ? null : (
          <StateGroup title="变量">
            {state.vars.map((variable) => (
              <div key={variable.name} className="flex gap-2 font-mono">
                <span className="text-primary">{variable.name}</span>
                <span className="text-muted-foreground">=</span>
                <span className="break-all">{variable.value}</span>
              </div>
            ))}
          </StateGroup>
        )}
        {state.stack === undefined || state.stack.length === 0 ? null : (
          <StateGroup title="调用栈（顶在上）">
            {state.stack.map((frame, index) => (
              <div key={index} className="font-mono">
                {index === 0 ? '▸ ' : '  '}
                {frame}
              </div>
            ))}
          </StateGroup>
        )}
        {state.heap === undefined || state.heap.length === 0 ? null : (
          <StateGroup title="堆对象">
            {state.heap.map((object) => (
              <div key={object.id} className="font-mono">
                <span className="text-primary">{object.id}</span>: {object.label}
                {object.fields === undefined ? null : (
                  <span className="text-muted-foreground">
                    {' '}
                    {Object.entries(object.fields)
                      .map(([key, value]) => `${key}=${value}`)
                      .join(', ')}
                  </span>
                )}
              </div>
            ))}
          </StateGroup>
        )}
        {state.output === undefined || state.output.length === 0 ? null : (
          <StateGroup title="输出">
            <pre className="whitespace-pre-wrap">{state.output.join('\n')}</pre>
          </StateGroup>
        )}
      </div>
    </div>
  )
}

function StateGroup({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div>
      <div className="text-muted-foreground mb-1">{title}</div>
      <div className="space-y-0.5">{children}</div>
    </div>
  )
}
