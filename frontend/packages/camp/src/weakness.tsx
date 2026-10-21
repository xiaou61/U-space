'use client'

import { Badge, Card, CardContent, CardHeader, CardTitle } from '@paideia/ui'
import type { ReactNode } from 'react'

import { DIFFICULTY_LABEL, type CampVerdict, type ComparedTopic, type TopicResult } from './types'

/**
 * 薄弱点与强项（WORK-029）。
 *
 * <p>**只显示主题级计数，不显示总分**：跨主题求平均会把"强项 + 弱项"糊成一个看不出方向的
 * 数字。界面上也不出现「通过率」「等级」这类词——本项刻意不打分。
 */
export function WeaknessCards({
  topics,
  weak,
  strong,
}: {
  topics: TopicResult[]
  weak: string[]
  strong: string[]
}) {
  const byTopic = new Map(topics.map((topic) => [topic.topic, topic]))

  return (
    <div className="grid gap-4 sm:grid-cols-2" data-testid="camp-weakness">
      <Card>
        <CardHeader>
          <CardTitle>需要补的</CardTitle>
        </CardHeader>
        <CardContent>
          {weak.length === 0 ? (
            <p className="text-muted-foreground text-sm">这次没有明显薄弱项。</p>
          ) : (
            <ul className="space-y-2">
              {weak.map((topic) => (
                <TopicLine key={topic} topic={topic} result={byTopic.get(topic)} tone="weak" />
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>可以放一放的</CardTitle>
        </CardHeader>
        <CardContent>
          {strong.length === 0 ? (
            <p className="text-muted-foreground text-sm">这次还没有达到「可以放一放」的主题。</p>
          ) : (
            <ul className="space-y-2">
              {strong.map((topic) => (
                <TopicLine key={topic} topic={topic} result={byTopic.get(topic)} tone="strong" />
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

function TopicLine({
  topic,
  result,
  tone,
}: {
  topic: string
  result: TopicResult | undefined
  tone: 'weak' | 'strong'
}): ReactNode {
  return (
    <li className="flex flex-wrap items-center gap-2 text-sm">
      <Badge variant={tone === 'weak' ? 'destructive' : 'success'}>
        {tone === 'weak' ? '要补' : '放一放'}
      </Badge>
      <span className="font-medium">{topic}</span>
      {result !== undefined ? (
        <span className="text-muted-foreground tabular-nums">
          {result.correct} / {result.total} 题答对
        </span>
      ) : null}
    </li>
  )
}

/** 逐主题 × 难度的计数表。刻意不出现"得分""正确率"字样。 */
export function TopicTable({ topics }: { topics: TopicResult[] }): ReactNode {
  if (topics.length === 0) {
    return <p className="text-muted-foreground text-sm">还没有作答记录。</p>
  }
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="text-muted-foreground border-border border-b text-left">
            <th className="py-2 pr-4 font-medium">主题</th>
            <th className="py-2 pr-4 font-medium">答对 / 总题</th>
            <th className="py-2 font-medium">按难度</th>
          </tr>
        </thead>
        <tbody>
          {topics.map((topic) => (
            <tr key={topic.topic} className="border-border/60 border-b last:border-0">
              <td className="py-2 pr-4 font-medium">{topic.topic}</td>
              <td className="py-2 pr-4 tabular-nums">
                {topic.correct} / {topic.total}
              </td>
              <td className="py-2">
                <span className="text-muted-foreground flex flex-wrap gap-2 text-xs">
                  {Object.entries(topic.byDifficulty).map(([tier, count]) => (
                    <span key={tier} className="tabular-nums">
                      {DIFFICULTY_LABEL[tier] ?? tier} {count.correct}/{count.total}
                    </span>
                  ))}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

/**
 * 诊断 vs 复测并排（AC-012 / AC-017）。
 *
 * <p>**窄屏上下堆叠**：并排两张表在 390px 下会横向滚动，所以用 `grid` 在 `md` 以上才分两列。
 */
export function ComparisonTable({
  compared,
  onlyInDiagnostic,
  onlyInRetest,
}: {
  compared: ComparedTopic[]
  onlyInDiagnostic: string[]
  onlyInRetest: string[]
}): ReactNode {
  if (compared.length === 0 && onlyInDiagnostic.length === 0 && onlyInRetest.length === 0) {
    return <p className="text-muted-foreground text-sm">两次卷子没有共同的主题，无法对比。</p>
  }
  return (
    <div className="space-y-4" data-testid="camp-comparison">
      {compared.length > 0 ? (
        <div className="grid gap-4 md:grid-cols-2">
          {compared.map((item) => (
            <ComparisonCard key={item.topic} item={item} />
          ))}
        </div>
      ) : null}

      {onlyInDiagnostic.length > 0 || onlyInRetest.length > 0 ? (
        <p className="text-muted-foreground text-xs">
          {onlyInDiagnostic.length > 0 ? `只在诊断卷出现：${onlyInDiagnostic.join('、')}。` : ''}
          {onlyInRetest.length > 0 ? `只在复测卷出现：${onlyInRetest.join('、')}。` : ''}
          这两类不给结论。
        </p>
      ) : null}
    </div>
  )
}

const VERDICT_TEXT: Record<CampVerdict, { label: string; variant: 'success' | 'secondary' | 'destructive' | 'outline' }> = {
  IMPROVED: { label: '进步', variant: 'success' },
  SAME: { label: '持平', variant: 'secondary' },
  WORSE: { label: '退步', variant: 'destructive' },
  INCONCLUSIVE: { label: '仅供参考', variant: 'outline' },
}

function ComparisonCard({ item }: { item: ComparedTopic }): ReactNode {
  const verdict = VERDICT_TEXT[item.verdict]
  return (
    <Card size="sm">
      <CardHeader>
        <div className="flex flex-wrap items-center gap-2">
          <CardTitle>{item.topic}</CardTitle>
          <Badge variant={verdict.variant}>{verdict.label}</Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-1 text-sm">
        <p className="tabular-nums">
          诊断 {item.diagnostic.correct} / {item.diagnostic.total} 题答对
        </p>
        <p className="tabular-nums">
          复测 {item.retest.correct} / {item.retest.total} 题答对
        </p>
        {item.reason !== null ? <p className="text-muted-foreground text-xs">{item.reason}</p> : null}
      </CardContent>
    </Card>
  )
}
