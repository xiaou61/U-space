import {
  Badge,
  Button,
  Card,
  CardContent,
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
  Input,
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
import { ListOrdered } from 'lucide-react'
import { useState } from 'react'

import { api } from '../api'

/**
 * AI 调用明细（WORK-022 REQ-009 / REQ-010）。
 *
 * <p>列表按时间倒序，可按场景 / 状态 / 用户 / 模型筛。点一行展开该轮的**步骤表**——
 * 那才是回答"这一轮慢在哪"的地方：第 1 轮上游 800ms、工具 4.2s、第 2 轮 600ms。
 *
 * <p>**不展示正文与提示词**：本项刻意不采集它们（隐私与体积）。要看对话内容去会话历史。
 */
interface RunSummary {
  traceId: string
  userId: number
  conversationId: number | null
  kind: string
  harness: string | null
  model: string | null
  status: string
  errorType: string | null
  startedAt: string
  endedAt: string | null
  durationMs: number | null
  ttftMs: number | null
  llmRounds: number
  toolCalls: number
  inputTokens: number
  outputTokens: number
  credits: number
}

interface SpanView {
  seq: number
  parentSeq: number | null
  kind: string
  name: string
  status: string
  errorType: string | null
  startedAt: string
  durationMs: number | null
  inputTokens: number
  outputTokens: number
  attrs: Record<string, unknown>
}

interface RunDetail {
  run: RunSummary
  attrs: Record<string, unknown>
  spans: SpanView[]
}

interface Page<T> {
  total: number
  page: number
  size: number
  items: T[]
}

interface Meta {
  kinds: { name: string; count: number }[]
  models: { name: string; count: number }[]
  statuses: { name: string; count: number }[]
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

const STATUS_LABEL: Record<string, string> = {
  ok: '成功',
  failed: '失败',
  timeout: '超时',
  cancelled: '取消',
  interrupted: '中断',
  running: '进行中',
}

function ms(value: number | null): string {
  if (value === null || value === undefined) {
    return '—'
  }
  return value < 1000 ? `${value} ms` : `${(value / 1000).toFixed(1)} s`
}

function when(value: string | null): string {
  if (value === null) {
    return '—'
  }
  const at = new Date(value)
  return Number.isNaN(at.getTime()) ? value : at.toLocaleString('zh-CN', { hour12: false })
}

/** 时间窗默认 7 天；与概览页同一口径。 */
function windowParams(): string {
  const to = new Date()
  const from = new Date(to.getTime() - 7 * 24 * 3600 * 1000)
  return `from=${from.toISOString()}&to=${to.toISOString()}`
}

export function AiRunsListPage() {
  const [kind, setKind] = useState('')
  const [status, setStatus] = useState('')
  const [userId, setUserId] = useState('')
  const [expanded, setExpanded] = useState<string | null>(null)

  const meta = useQuery({
    queryKey: ['ai-runs-meta'],
    queryFn: () => api.requestEnvelope<Meta>(`/api/v1/ai/admin/runs/meta?${windowParams()}`),
  })

  const list = useQuery({
    queryKey: ['ai-runs-list', kind, status, userId],
    queryFn: () => {
      const params = new URLSearchParams()
      if (kind !== '') params.set('kind', kind)
      if (status !== '') params.set('status', status)
      if (userId.trim() !== '') params.set('userId', userId.trim())
      params.set('size', '50')
      return api.requestEnvelope<Page<RunSummary>>(
        `/api/v1/ai/admin/runs?${windowParams()}&${params.toString()}`,
      )
    },
  })

  const rows = list.data?.items ?? []

  return (
    <div className="space-y-4">
      <PageHeader
        title="调用明细"
        description="每一行是一轮调用。点开可看该轮的步骤耗时——「慢在上游还是慢在工具」只能在这里看出来。"
      />

      <Card>
        <CardContent className="flex flex-wrap items-end gap-3 py-3">
          <label className="space-y-1 text-xs">
            <span className="text-muted-foreground block">场景</span>
            <select
              className="border-input h-9 rounded-md border bg-transparent px-2 text-sm"
              data-testid="runs-filter-kind"
              value={kind}
              onChange={(event) => { setKind(event.target.value) }}
            >
              <option value="">全部</option>
              {(meta.data?.kinds ?? []).map((item) => (
                <option key={item.name} value={item.name}>
                  {KIND_LABEL[item.name] ?? item.name}
                </option>
              ))}
            </select>
          </label>
          <label className="space-y-1 text-xs">
            <span className="text-muted-foreground block">状态</span>
            <select
              className="border-input h-9 rounded-md border bg-transparent px-2 text-sm"
              data-testid="runs-filter-status"
              value={status}
              onChange={(event) => { setStatus(event.target.value) }}
            >
              <option value="">全部</option>
              {(meta.data?.statuses ?? []).map((item) => (
                <option key={item.name} value={item.name}>
                  {STATUS_LABEL[item.name] ?? item.name}
                </option>
              ))}
            </select>
          </label>
          <label className="space-y-1 text-xs">
            <span className="text-muted-foreground block">用户 id</span>
            <Input
              className="h-9 w-32"
              data-testid="runs-filter-user"
              inputMode="numeric"
              placeholder="全部"
              value={userId}
              onChange={(event) => { setUserId(event.target.value) }}
            />
          </label>
          {list.isSuccess ? (
            <span className="text-muted-foreground ml-auto text-xs" data-testid="runs-list-total">
              共 {list.data.total} 条
            </span>
          ) : null}
        </CardContent>
      </Card>

      {list.isLoading ? <Spinner /> : null}

      {list.isSuccess && rows.length === 0 ? (
        <Card>
          <Empty data-testid="runs-list-empty">
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <ListOrdered />
              </EmptyMedia>
              <EmptyTitle>没有符合条件的调用</EmptyTitle>
              <EmptyDescription>放宽筛选条件，或等有人用过 AI 之后再来看。</EmptyDescription>
            </EmptyHeader>
          </Empty>
        </Card>
      ) : null}

      {rows.length > 0 ? (
        <Card>
          <CardContent className="px-0">
            <Table data-testid="runs-table">
              <TableHeader>
                <TableRow>
                  <TableHead>时间</TableHead>
                  <TableHead>场景</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead className="text-right">耗时</TableHead>
                  <TableHead className="text-right">首字</TableHead>
                  <TableHead className="text-right">轮次/工具</TableHead>
                  <TableHead className="text-right">tokens</TableHead>
                  <TableHead className="text-right">扣分</TableHead>
                  <TableHead>用户</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((row) => (
                  <Row key={row.traceId} row={row} expanded={expanded === row.traceId}
                    onToggle={() => { setExpanded(expanded === row.traceId ? null : row.traceId) }} />
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      ) : null}
    </div>
  )
}

function Row({ row, expanded, onToggle }: { row: RunSummary; expanded: boolean; onToggle: () => void }) {
  return (
    <>
      <TableRow className="cursor-pointer" onClick={onToggle} data-testid={`runs-row-${row.traceId}`}>
        <TableCell className="text-xs whitespace-nowrap">{when(row.startedAt)}</TableCell>
        <TableCell className="text-xs">{KIND_LABEL[row.kind] ?? row.kind}</TableCell>
        <TableCell>
          <Badge variant={row.status === 'ok' ? 'secondary' : 'destructive'}>
            {STATUS_LABEL[row.status] ?? row.status}
          </Badge>
          {row.errorType === null ? null : (
            <span className="text-muted-foreground ml-1 text-[10px]">{row.errorType}</span>
          )}
        </TableCell>
        <TableCell className="text-right text-xs tabular-nums">{ms(row.durationMs)}</TableCell>
        <TableCell className="text-right text-xs tabular-nums">{ms(row.ttftMs)}</TableCell>
        <TableCell className="text-right text-xs tabular-nums">
          {row.llmRounds} / {row.toolCalls}
        </TableCell>
        <TableCell className="text-right text-xs tabular-nums">
          {row.inputTokens} / {row.outputTokens}
        </TableCell>
        <TableCell className="text-right text-xs tabular-nums">{row.credits}</TableCell>
        <TableCell className="text-xs tabular-nums">#{row.userId}</TableCell>
      </TableRow>
      {expanded ? (
        <TableRow>
          <TableCell colSpan={9} className="bg-muted/40">
            <RunSteps traceId={row.traceId} />
          </TableCell>
        </TableRow>
      ) : null}
    </>
  )
}

/** 展开后的步骤表。单独请求：列表不背这份数据。 */
function RunSteps({ traceId }: { traceId: string }) {
  const detail = useQuery({
    queryKey: ['ai-run-detail', traceId],
    queryFn: () => api.requestEnvelope<RunDetail>(`/api/v1/ai/admin/runs/${traceId}`),
  })

  if (detail.isLoading) {
    return <Spinner />
  }
  if (detail.isError || detail.data === undefined) {
    return <p className="text-destructive text-xs">读取这一轮的步骤失败。</p>
  }
  if (detail.data.spans.length === 0) {
    return (
      <p className="text-muted-foreground text-xs" data-testid="run-spans-empty">
        这一轮没有步骤记录（非对话路径通常只有一次调用）。
      </p>
    )
  }
  return (
    <div className="space-y-2 py-1" data-testid="run-spans">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-12">#</TableHead>
            <TableHead>类型</TableHead>
            <TableHead>名称</TableHead>
            <TableHead>状态</TableHead>
            <TableHead className="text-right">耗时</TableHead>
            <TableHead className="text-right">tokens</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {detail.data.spans.map((span) => (
            <TableRow key={span.seq}>
              <TableCell className="text-xs tabular-nums">{span.seq}</TableCell>
              <TableCell className="text-xs">{span.kind}</TableCell>
              <TableCell className="text-xs">{span.name}</TableCell>
              <TableCell className="text-xs">
                {STATUS_LABEL[span.status] ?? span.status}
                {span.errorType === null ? null : (
                  <span className="text-muted-foreground ml-1 text-[10px]">{span.errorType}</span>
                )}
              </TableCell>
              <TableCell className="text-right text-xs tabular-nums">{ms(span.durationMs)}</TableCell>
              <TableCell className="text-right text-xs tabular-nums">
                {span.inputTokens} / {span.outputTokens}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <div className="text-muted-foreground flex flex-wrap gap-3 text-[11px]">
        {Object.entries(detail.data.attrs).map(([key, value]) => (
          <span key={key}>
            {key}={String(value)}
          </span>
        ))}
      </div>
      <Button variant="outline" size="sm" onClick={() => { void detail.refetch() }}>
        刷新
      </Button>
    </div>
  )
}
