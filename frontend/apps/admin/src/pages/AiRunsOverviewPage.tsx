import {
  Badge,
  Card,
  CardContent,
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
  PageHeader,
  Spinner,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@paideia/ui'
import { useQuery } from '@tanstack/react-query'
import { Activity } from 'lucide-react'

import { api } from '../api'

/**
 * AI 调用概览（WORK-022 REQ-008）。
 *
 * <p>回答五个问题：调了多少次、成功多少、慢不慢、花了多少、错在哪。
 * 数据只有 `ai_runs` 一张表——轮次行本身就带状态、耗时、用量与扣分。
 *
 * <p>**分位数在服务端算**（窗口函数），这里不做任何统计——前端只负责显示。
 */
interface CountBy {
  name: string
  count: number
}

interface Overview {
  from: string
  to: string
  clamped: boolean
  total: number
  byStatus: {
    ok: number
    failed: number
    timeout: number
    cancelled: number
    interrupted: number
    running: number
  }
  successRate: number
  durationP50: number | null
  durationP95: number | null
  ttftP50: number | null
  ttftP95: number | null
  inputTokens: number
  outputTokens: number
  credits: number
  byKind: CountBy[]
  byModel: CountBy[]
  errorTypes: CountBy[]
  tools: CountBy[]
}

const KIND_LABEL: Record<string, string> = {
  chat: '对话',
  memory_extract: '记忆整理',
  interview_review: '面试点评',
  mcp_ask: '外部问答',
  resume_import: '简历解析',
  resume_optimize: '简历优化',
  entry_summary: '条目精简',
}

/** 毫秒显示：小于 1 秒给 ms，否则给秒（一位小数）。空值给破折号，不显示 null。 */
function ms(value: number | null): string {
  if (value === null || value === undefined) {
    return '—'
  }
  return value < 1000 ? `${value} ms` : `${(value / 1000).toFixed(1)} s`
}

/** 百分比：一位小数。0 次调用时显示破折号而不是 0%，那会被读成"全是失败"。 */
function percent(value: number, total: number): string {
  if (total === 0) {
    return '—'
  }
  return `${(value * 100).toFixed(1)}%`
}

function num(value: number): string {
  return value.toLocaleString('zh-CN')
}

const fetchOverview = (days: number) => {
  const to = new Date()
  const from = new Date(to.getTime() - days * 24 * 3600 * 1000)
  return api.requestEnvelope<Overview>(
    `/api/v1/ai/admin/runs/overview?from=${from.toISOString()}&to=${to.toISOString()}`,
  )
}

export function AiRunsOverviewPage() {
  const days = 7
  const overview = useQuery({ queryKey: ['ai-runs-overview', days], queryFn: () => fetchOverview(days) })
  const data = overview.data

  const cards = [
    { label: '调用次数', value: data === undefined ? '—' : num(data.total), testId: 'runs-total' },
    {
      label: '成功率',
      value: data === undefined ? '—' : percent(data.successRate, data.byStatus.ok + data.byStatus.failed
        + data.byStatus.timeout + data.byStatus.cancelled + data.byStatus.interrupted),
      testId: 'runs-success-rate',
    },
    { label: '耗时 p50', value: data === undefined ? '—' : ms(data.durationP50), testId: 'runs-p50' },
    { label: '耗时 p95', value: data === undefined ? '—' : ms(data.durationP95), testId: 'runs-p95' },
    { label: '首字 p50', value: data === undefined ? '—' : ms(data.ttftP50), testId: 'runs-ttft' },
    {
      label: '扣分合计',
      value: data === undefined ? '—' : `${num(data.credits)} 分`,
      testId: 'runs-credits',
    },
  ]

  return (
    <div className="space-y-4">
      <PageHeader
        title="AI 调用"
        description="近 7 天的调用情况。每次 AI 调用都会留下一行记录，步骤级明细可到「调用明细」下钻。"
      />

      {overview.isLoading ? <Spinner /> : null}

      {overview.isError ? (
        <Card>
          <CardContent className="text-destructive py-6 text-sm">
            读取失败：{overview.error instanceof Error ? overview.error.message : '未知原因'}
          </CardContent>
        </Card>
      ) : null}

      {data !== undefined ? (
        <>
          {data.clamped ? (
            <p className="text-muted-foreground text-xs" data-testid="runs-clamped">
              时间窗已收敛到保留期上限显示。
            </p>
          ) : null}

          <div className="grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-6">
            {cards.map((card) => (
              <Card key={card.label}>
                <CardContent className="py-4">
                  <p className="text-muted-foreground text-xs">{card.label}</p>
                  <p className="mt-1 text-2xl font-semibold tabular-nums" data-testid={card.testId}>
                    {card.value}
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>

          {data.total === 0 ? (
            <Card>
              <Empty data-testid="runs-overview-empty">
                <EmptyHeader>
                  <EmptyMedia variant="icon">
                    <Activity />
                  </EmptyMedia>
                  <EmptyTitle>这段时间没有任何调用</EmptyTitle>
                  <EmptyDescription>有人用过 AI 之后，这里会有记录。</EmptyDescription>
                </EmptyHeader>
              </Empty>
            </Card>
          ) : (
            <>
              <div className="grid gap-4 lg:grid-cols-2">
                <RankCard title="按场景" rows={data.byKind} label={(name) => KIND_LABEL[name] ?? name}
                  testId="runs-by-kind" />
                <RankCard title="错误类型" rows={data.errorTypes} label={(name) => name}
                  testId="runs-error-types" emptyHint="没有失败记录" />
              </div>
              <div className="grid gap-4 lg:grid-cols-2">
                <RankCard title="按模型" rows={data.byModel} label={(name) => name} testId="runs-by-model" />
                <RankCard title="工具调用" rows={data.tools} label={(name) => name}
                  testId="runs-tools" emptyHint="这段时间没有工具调用" />
              </div>

              <Card>
                <CardContent className="py-4">
                  <h2 className="text-sm font-medium">状态分布</h2>
                  <div className="mt-3 flex flex-wrap gap-2">
                    <Badge variant="secondary">成功 {num(data.byStatus.ok)}</Badge>
                    <Badge variant="secondary">失败 {num(data.byStatus.failed)}</Badge>
                    <Badge variant="secondary">超时 {num(data.byStatus.timeout)}</Badge>
                    <Badge variant="secondary">中断 {num(data.byStatus.interrupted)}</Badge>
                    <Badge variant="secondary">进行中 {num(data.byStatus.running)}</Badge>
                  </div>
                  <p className="text-muted-foreground mt-3 text-xs">
                    输入 {num(data.inputTokens)} / 输出 {num(data.outputTokens)} tokens。
                    「进行中」是仍在跑的轮次；长期停在这里说明有任务卡住或被进程以外的原因中断。
                  </p>
                </CardContent>
              </Card>
            </>
          )}
        </>
      ) : null}
    </div>
  )
}

/** 一个小排行表。空数据时给一句可读的提示，不是一张空表。 */
function RankCard({
  title,
  rows,
  label,
  testId,
  emptyHint = '暂无数据',
}: {
  title: string
  rows: CountBy[]
  label: (name: string) => string
  testId: string
  emptyHint?: string
}) {
  return (
    <Card>
      <CardContent className="px-0">
        <h2 className="px-(--card-spacing) pb-2 text-sm font-medium">{title}</h2>
        {rows.length === 0 ? (
          <p className="text-muted-foreground px-(--card-spacing) pb-4 text-xs" data-testid={testId}>
            {emptyHint}
          </p>
        ) : (
          <Table data-testid={testId}>
            <TableHeader>
              <TableRow>
                <TableHead>名称</TableHead>
                <TableHead className="text-right">次数</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((row) => (
                <TableRow key={row.name}>
                  <TableCell>{label(row.name)}</TableCell>
                  <TableCell className="text-right tabular-nums">{num(row.count)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  )
}
