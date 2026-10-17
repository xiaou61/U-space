import { Component, type ComponentType, type ReactNode } from 'react'
import type { CustomRendererProps } from 'streamdown'

import { Button } from '../../components/button'
import { useSourceReading } from './context'

/**
 * 五种块共用的外壳与降级。
 *
 * <p>降级形态刻意保留**原始文本**：作者在管理端预览里写完就能看到"哪一块坏了、坏在哪"，
 * 复制回去改比对着一个"加载失败"发呆有用。读者侧看到的则是一句人话说明。
 */

export function BlockFrame({
  title,
  testId,
  actions,
  children,
  className,
}: {
  title: string
  testId: string
  actions?: ReactNode
  children: ReactNode
  className?: string
}) {
  return (
    <section
      data-testid={testId}
      className={`border-border bg-card my-6 overflow-hidden rounded-xl border ${className ?? ''}`}
    >
      <header className="border-border bg-muted/40 flex flex-wrap items-center justify-between gap-2 border-b px-4 py-2.5">
        <h3 className="text-sm font-medium">{title}</h3>
        {actions === undefined ? null : <div className="flex items-center gap-2">{actions}</div>}
      </header>
      {children}
    </section>
  )
}

export function BlockFallback({ title, error, raw }: { title: string; error: string; raw: string }) {
  return (
    <BlockFrame title={title} testId="block-fallback">
      <div className="space-y-3 p-4">
        <p className="text-destructive text-sm">这块内容的数据格式有误：{error}</p>
        <p className="text-muted-foreground text-xs">下面是原始内容，可复制回管理端修正。</p>
        <pre className="border-border bg-muted/40 max-h-64 overflow-auto rounded-lg border p-3 font-mono text-xs whitespace-pre-wrap">
          {raw}
        </pre>
      </div>
    </BlockFrame>
  )
}

export function Pill({ children, tone = 'muted' }: { children: ReactNode; tone?: 'muted' | 'primary' | 'success' | 'danger' }) {
  const toneClass = {
    muted: 'border-border bg-muted text-muted-foreground',
    primary: 'border-primary/40 bg-primary/10 text-primary',
    success: 'border-success/40 bg-success/10 text-success',
    danger: 'border-destructive/40 bg-destructive/10 text-destructive',
  }[tone]
  return (
    <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[11px] font-medium ${toneClass}`}>
      {children}
    </span>
  )
}

/** 「问 AI」只在页面提供了 ask 能力时出现；管理端预览里没有这个能力，按钮就不该存在。 */
export function AskButton({ prompt, label = '问 AI' }: { prompt: string; label?: string }) {
  const { ask } = useSourceReading()
  if (ask === undefined) {
    return null
  }
  return (
    <Button type="button" variant="outline" size="sm" data-testid="block-ask" onClick={() => ask(prompt)}>
      {label}
    </Button>
  )
}

/**
 * 单块错误边界。
 *
 * <p>包在**模块级**注册的渲染器上（`index.ts` 里一次性生成），不能写在组件内部——
 * 那样每次渲染都是一个新的组件类型，React 会卸载重建整棵子树。
 */
class BlockBoundary extends Component<
  { title: string; raw: string; children: ReactNode },
  { error: string | null }
> {
  state: { error: string | null } = { error: null }

  static getDerivedStateFromError(cause: unknown) {
    return { error: cause instanceof Error ? cause.message : String(cause) }
  }

  render() {
    if (this.state.error !== null) {
      return <BlockFallback title={this.props.title} error={this.state.error} raw={this.props.raw} />
    }
    return this.props.children
  }
}

export function guard(title: string, Inner: ComponentType<CustomRendererProps>): ComponentType<CustomRendererProps> {
  function Guarded(props: CustomRendererProps) {
    return (
      <BlockBoundary title={title} raw={props.code}>
        <Inner {...props} />
      </BlockBoundary>
    )
  }
  return Guarded
}
