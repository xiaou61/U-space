import { Pause, Play, SkipBack, SkipForward, StepBack, StepForward } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'

import { Button } from '../../components/button'
import type { TraceResult, TraceStep } from './context'

/**
 * 逐步执行播放器（WORK-023 REQ-006/007/008）。
 *
 * <p>数据是**一次录制的结果**，播放是纯本地状态：前后步、跳首尾都不发请求（REQ-006）。
 * 这是本项目"录到磁盘再回放"那条路的消费端——不做真单步（执行挂起等用户点下一步），
 * 那要把执行变成可暂停的会话、跨沙箱进程边界维持状态，成本高一个数量级。
 *
 * <p>三条可访问性要求（质量要求）：键盘 ←/→ 可用；步数对读屏可见（aria-live）；
 * `prefers-reduced-motion` 下不做补间，直接切帧。
 */
export function TracePlayer({ trace }: { trace: TraceResult }) {
  const [index, setIndex] = useState(0)
  const [playing, setPlaying] = useState(false)
  const timer = useRef<number | null>(null)

  const total = trace.steps.length
  const step: TraceStep | undefined = trace.steps[Math.min(index, Math.max(total - 1, 0))]

  // 播放：每 700ms 前进一步，到最后一步自动停（不循环——循环会让人以为程序在重跑）
  useEffect(() => {
    if (!playing) {
      return
    }
    timer.current = window.setInterval(() => {
      setIndex((current) => {
        if (current + 1 >= total) {
          setPlaying(false)
          return current
        }
        return current + 1
      })
    }, 700)
    return () => {
      if (timer.current !== null) {
        window.clearInterval(timer.current)
        timer.current = null
      }
    }
  }, [playing, total])

  // 键盘：←/→ 步进。**只在播放器获得焦点时生效**，避免和页面滚动抢键
  const onKeyDown = (event: React.KeyboardEvent) => {
    if (event.key === 'ArrowRight') {
      event.preventDefault()
      setPlaying(false)
      setIndex((current) => Math.min(current + 1, total - 1))
    } else if (event.key === 'ArrowLeft') {
      event.preventDefault()
      setPlaying(false)
      setIndex((current) => Math.max(current - 1, 0))
    }
  }

  if (total === 0) {
    return (
      <p className="text-muted-foreground text-xs" data-testid="trace-empty">
        没有录到任何执行步（可能是语法错误，或程序立刻结束）。
      </p>
    )
  }

  const previous = index > 0 ? trace.steps[index - 1] : undefined
  const changed = step === undefined ? [] : changedKeys(previous, step)

  return (
    <div className="space-y-3" data-testid="trace-player" tabIndex={0} onKeyDown={onKeyDown}>
      <div className="flex flex-wrap items-center gap-2">
        <Button size="sm" variant="outline" aria-label="回到第一步" data-testid="trace-first"
          onClick={() => { setPlaying(false); setIndex(0) }}>
          <SkipBack className="size-4" />
        </Button>
        <Button size="sm" variant="outline" aria-label="上一步" data-testid="trace-prev"
          onClick={() => { setPlaying(false); setIndex(Math.max(index - 1, 0)) }}>
          <StepBack className="size-4" />
        </Button>
        <Button size="sm" aria-label={playing ? '暂停' : '播放'} data-testid="trace-play"
          onClick={() => { setPlaying(!playing) }}>
          {playing ? <Pause className="size-4" /> : <Play className="size-4" />}
          {playing ? '暂停' : '播放'}
        </Button>
        <Button size="sm" variant="outline" aria-label="下一步" data-testid="trace-next"
          onClick={() => { setPlaying(false); setIndex(Math.min(index + 1, total - 1)) }}>
          <StepForward className="size-4" />
        </Button>
        <Button size="sm" variant="outline" aria-label="跳到最后一步" data-testid="trace-last"
          onClick={() => { setPlaying(false); setIndex(total - 1) }}>
          <SkipForward className="size-4" />
        </Button>
        {/* 读屏要能听到"第几步"：纯视觉的进度条对它没有意义 */}
        <span className="text-muted-foreground ml-2 text-xs tabular-nums" aria-live="polite"
          data-testid="trace-step-indicator">
          第 {index + 1} / {total} 步 · 第 {step?.line ?? 0} 行
        </span>
      </div>

      <div className="grid gap-3 md:grid-cols-2">
        <div>
          <p className="text-muted-foreground mb-1 text-xs">变量</p>
          <ul className="space-y-1 text-sm" data-testid="trace-locals">
            {step === undefined || Object.keys(step.locals).length === 0 ? (
              <li className="text-muted-foreground text-xs">这一步还没有变量</li>
            ) : (
              Object.entries(step.locals).map(([name, value]) => (
                <li key={name} className="flex items-baseline gap-2 font-mono text-xs">
                  <span className="font-sans font-medium">{name}</span>
                  <span className="text-muted-foreground">=</span>
                  <span data-testid={`trace-var-${name}`}>{value}</span>
                  {/* 变化标记同时给符号与文字：不靠颜色单一表达（REQ-007） */}
                  {changed.includes(name) ? (
                    <span className="text-xs" data-testid={`trace-changed-${name}`}>
                      ↑ 变了
                    </span>
                  ) : null}
                </li>
              ))
            )}
          </ul>
        </div>
        <div>
          <p className="text-muted-foreground mb-1 text-xs">已打印</p>
          <pre className="bg-muted max-h-40 overflow-auto rounded p-2 text-xs whitespace-pre-wrap"
            data-testid="trace-output">{trace.output}</pre>
        </div>
      </div>

      {step?.event === 'exception' ? (
        <p className="text-destructive text-xs" data-testid="trace-exception">
          在第 {step.line} 行抛出异常：{step.locals.__error__ ?? '未知类型'}
        </p>
      ) : null}

      {trace.truncated || trace.reason !== 'done' ? (
        <p className="text-muted-foreground text-xs" data-testid="trace-reason">
          {reasonText(trace.reason, trace.truncated)}
        </p>
      ) : null}
    </div>
  )
}

/** 与上一步相比值变了的变量名。 */
function changedKeys(previous: TraceStep | undefined, current: TraceStep): string[] {
  if (previous === undefined) {
    return []
  }
  return Object.keys(current.locals).filter((name) => previous.locals[name] !== current.locals[name])
}

/** 结束原因说人话。触顶不是错误，要说清"只录到这里"。 */
function reasonText(reason: string, truncated: boolean): string {
  if (reason === 'step_limit' || truncated) {
    return '执行步数超过上限，只录到这里。想看得更长，把循环次数改小。'
  }
  if (reason === 'time_limit') {
    return '执行超时，只录到被杀之前的部分。'
  }
  if (reason === 'error') {
    return '程序中途出错，录制在出错处停止。'
  }
  return `录制结束：${reason}`
}
