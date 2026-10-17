import { useMemo } from 'react'
import type { CustomRendererProps } from 'streamdown'

import { BlockFallback, BlockFrame, Pill } from './blocks'
import { useSourceReading } from './context'
import { parseLearningPath } from './schema'

/**
 * 学习路径（WORK-014）：专题总览的有序站点。
 *
 * <p>状态不来自后端，而来自本地进度：某站的走读记了 `done` 就算已完成；有任何记录就算进行中。
 * 这只是**阅读提示**，不是权限或解锁条件——不做"必须读完上一站才能看下一站"。
 */
export function LearningPath({ code }: CustomRendererProps) {
  const parsed = useMemo(() => parseLearningPath(code), [code])
  const { progress, onOpenEntry } = useSourceReading()

  if (!parsed.ok) {
    return <BlockFallback title="学习路径" error={parsed.error} raw={code} />
  }
  const path = parsed.value
  const finished = path.steps.filter((step) => {
    if (step.entryId === undefined || progress === undefined) {
      return false
    }
    const record = progress.read(step.entryId)
    if (record === null) {
      return false
    }
    return Object.values(record.tours).some((tour) => tour.done)
  }).length

  return (
    <BlockFrame
      title={path.title ?? '学习路径'}
      testId="learning-path"
      actions={<Pill>{`${finished} / ${path.steps.length} 已完成`}</Pill>}
    >
      <ol className="divide-border divide-y">
        {path.steps.map((step, index) => {
          const record = step.entryId === undefined || progress === undefined ? null : progress.read(step.entryId)
          const done =
            record !== null && Object.values(record.tours).some((tour) => tour.done)
          const started = record !== null
          const interactive = step.entryId !== undefined && onOpenEntry !== undefined
          return (
            <li key={index}>
              <button
                type="button"
                data-testid={`learning-path-step-${index}`}
                data-state={done ? 'done' : started ? 'started' : 'new'}
                disabled={!interactive}
                onClick={() => {
                  if (step.entryId !== undefined) {
                    onOpenEntry?.(step.entryId)
                  }
                }}
                className="hover:bg-muted flex w-full items-start gap-3 px-4 py-3 text-left disabled:cursor-default disabled:hover:bg-transparent"
              >
                <span
                  className={`mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-full border text-[11px] ${
                    done
                      ? 'border-success bg-success text-success-foreground'
                      : started
                        ? 'border-primary text-primary'
                        : 'border-border text-muted-foreground'
                  }`}
                  aria-hidden="true"
                >
                  {done ? '✓' : index + 1}
                </span>
                <span className="min-w-0 flex-1">
                  <span className="flex flex-wrap items-baseline gap-2">
                    <span className="text-sm font-medium">{step.title}</span>
                    {step.minutes === undefined ? null : (
                      <span className="text-muted-foreground text-[11px]">约 {step.minutes} 分钟</span>
                    )}
                    {done ? <Pill tone="success">已完成</Pill> : started ? <Pill tone="primary">进行中</Pill> : null}
                  </span>
                  {step.summary === undefined ? null : (
                    <span className="text-muted-foreground mt-0.5 block text-xs">{step.summary}</span>
                  )}
                </span>
              </button>
            </li>
          )
        })}
      </ol>
    </BlockFrame>
  )
}
