/**
 * 极简趋势图（WORK-036）。
 *
 * <p><b>内联 SVG 而不是引图表库</b>：仓库当前零图表依赖，而我们只需要"单序列折线"与
 * "堆叠柱"两种形状。为一个折线引一个库，换来的是几十 KB 与一套新的版本约束。
 *
 * <p>刻意不做的事：不做 tooltip、不做缩放、不做图例交互。要精确数字看下面的表格——
 * 这张图只回答"有没有拐点"。
 */

export interface TrendPoint {
  /** 桶起点（ISO）。 */
  at: string
  value: number | null
}

interface TrendChartProps {
  title: string
  points: TrendPoint[]
  /** 数值格式化（毫秒 / 百分比 / 计数）。 */
  format: (value: number | null) => string
  /** 折线颜色，用 CSS 变量而不是写死色值——主题切换跟着走。 */
  tone?: 'primary' | 'danger'
  /** 把"没有数据"的点画断而不是落到 0：0 毫秒与没数据不是一回事。 */
  testId?: string
}

const WIDTH = 720
const HEIGHT = 120
const PADDING = 8

export function TrendChart({ title, points, format, tone = 'primary', testId }: TrendChartProps) {
  const values = points.map((point) => point.value)
  const present = values.filter((value): value is number => value !== null)
  const max = present.length === 0 ? 0 : Math.max(...present)
  const min = present.length === 0 ? 0 : Math.min(...present)
  // 全部相等时给一个假的高度差，否则折线会贴着上边或下边，看起来像"没有数据"
  const span = max - min === 0 ? Math.max(max, 1) : max - min

  const step = points.length <= 1 ? WIDTH - PADDING * 2 : (WIDTH - PADDING * 2) / (points.length - 1)
  const y = (value: number) => HEIGHT - PADDING - ((value - min) / span) * (HEIGHT - PADDING * 2)
  const x = (index: number) => PADDING + index * step

  // 把连续的非空点连成段：碰到 null 就断开
  const segments: string[] = []
  let current: string[] = []
  points.forEach((point, index) => {
    if (point.value === null) {
      if (current.length > 0) segments.push(current.join(' '))
      current = []
      return
    }
    current.push(`${x(index).toFixed(1)},${y(point.value).toFixed(1)}`)
  })
  if (current.length > 0) segments.push(current.join(' '))

  const stroke = tone === 'danger' ? 'var(--color-destructive)' : 'var(--color-primary)'
  const last: number | null = present.length === 0 ? null : present[present.length - 1] ?? null

  return (
    <div data-testid={testId}>
      <div className="flex items-baseline justify-between gap-2">
        <h3 className="text-muted-foreground text-xs">{title}</h3>
        <span className="text-xs tabular-nums">{format(last)}</span>
      </div>
      {present.length === 0 ? (
        <p className="text-muted-foreground py-6 text-xs">这段时间没有数据</p>
      ) : (
        <svg
          viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
          className="mt-1 h-24 w-full"
          role="img"
          aria-label={title}
          preserveAspectRatio="none"
        >
          {segments.map((segment, index) => (
            <polyline
              key={index}
              points={segment}
              fill="none"
              stroke={stroke}
              strokeWidth={2}
              vectorEffect="non-scaling-stroke"
            />
          ))}
        </svg>
      )}
    </div>
  )
}
