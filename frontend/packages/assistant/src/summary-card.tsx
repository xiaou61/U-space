import type { SummaryView } from './types'

/**
 * 结构化精简卡片。
 *
 * <p>三处刻意的表达：
 * <ul>
 *   <li>**来源必须标出来**（"你自己生成的"／"来自其他学习者"）。同一条内容两次点出不同文字
 *       时，不标来源会被读成"模型不稳定"。</li>
 *   <li>**命中缓存要说清"本次未产生调用"**，而不是显示 0 个 token——后者会被读成
 *       "这次几乎没花钱"，而事实是这次根本没调用。</li>
 *   <li>被截断过就写在卡片上（"已按前 N 字生成"），静默截断会让人以为模型漏读了正文。</li>
 * </ul>
 */
export function SummaryCardView({ view }: { view: SummaryView }) {
  const { card, cached, source, usage, truncated } = view

  return (
    <article className="space-y-3" data-testid="assistant-summary-card">
      <h3 className="text-sm leading-snug font-semibold" data-testid="assistant-summary-headline">
        {card.headline}
      </h3>

      {card.sections.map((section) => (
        <section key={section.heading} className="space-y-1">
          <h4 className="text-muted-foreground text-xs font-medium">{section.heading}</h4>
          <p className="text-sm leading-relaxed whitespace-pre-wrap">{section.body}</p>
        </section>
      ))}

      <footer className="text-muted-foreground border-border border-t pt-2 text-[11px]">
        <span data-testid="assistant-summary-source">
          {source === 'mine' ? '你自己生成的' : '来自其他学习者'}
        </span>
        <span className="mx-1.5">·</span>
        {cached ? (
          // 命中缓存时用量为 null：说"未产生调用"比写"0 tokens"诚实
          <span data-testid="assistant-summary-usage">本次未产生调用</span>
        ) : (
          <span data-testid="assistant-summary-usage">
            本次用量 {usage === null ? '未知' : `${usage.inputTokens + usage.outputTokens} tokens`}
          </span>
        )}
        {truncated ? <span className="ml-1.5">· 已按前段内容生成</span> : null}
      </footer>
    </article>
  )
}
