import { createKnowledgeApi } from '@paideia/knowledge'
import { useAssistantContext } from '@paideia/assistant'
// 只引 schema 子入口：它是纯函数、不含 CodeMirror，静态引入不会把编辑器带进主 chunk。
// 编辑器/结果表/执行计划/侧栏都走下面的 lazy()。
import { parseSchema, toCompletionSchema } from '@paideia/sql-workbench/schema'
import {
  Alert,
  AlertDescription,
  AlertTitle,
  AppShell,
  Button,
  PageHeader,
  Spinner,
} from '@paideia/ui'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Play, Search } from 'lucide-react'
import { lazy, Suspense, useEffect, useState } from 'react'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'

const knowledge = createKnowledgeApi(api)/**
 * 工作台组件必须**懒加载**（WORK-035 T-01 / AC-001）：CodeMirror 精简版 gzip 后约 137 kB，
 * 是本站唯一的重前端依赖。静态 import 会让每个进入首页的人为它付费。
 *
 * <p>懒加载由 `scripts/check-lazy-editor.mjs` 在构建时强制（已反向验证：改成静态 import 会让构建失败）。
 */
const SqlEditor = lazy(async () => {
  const module = await import('@paideia/sql-workbench')
  return { default: module.SqlEditor }
})

const ResultTables = lazy(async () => {
  const module = await import('@paideia/sql-workbench')
  return { default: module.ResultTables }
})

const ExplainTree = lazy(async () => {
  const module = await import('@paideia/sql-workbench')
  return { default: module.ExplainTree }
})

const SchemaSidebar = lazy(async () => {
  const module = await import('@paideia/sql-workbench')
  return { default: module.SchemaSidebar }
})

const ErDiagram = lazy(async () => {
  const module = await import('@paideia/sql-workbench')
  return { default: module.ErDiagram }
})

const ResultChart = lazy(async () => {
  const module = await import('@paideia/sql-workbench')
  return { default: module.ResultChart }
})

/**
 * SQL 工作台（WORK-032 的自由练习，WORK-035 起升级）。
 *
 * <p><b>与关卡的区别是"不计分"</b>：这里跑什么、跑对没跑对，都不写 `learning_progress`。
 * 关卡里的 SQL 题要判题、要计入这一关的通过与否；这里只是"随时打开、随便写"。
 *
 * <p><b>执行计划是一次独立的运行</b>：点「看执行计划」会真的起一个容器跑 `EXPLAIN FORMAT=TREE`，
 * 所以它同样占配额。这样做的理由是语义干净——计划与数据不会拼在同一个输出里让前端去猜。
 *
 * <p>数据集仍只有服务端下发的那些（隐藏集不下发，见 WORK-035 的修复）。
 */
export function SqlPracticePage() {
  const [code, setCode] = useState('SELECT * FROM customers;')
  const [tables, setTables] = useState<{ statementIndex: number; columns: string[]; rows: string[][] }[] | null>(null)
  const [explainOutput, setExplainOutput] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [dataset, setDataset] = useState<string | null>(null)

  const { setPageContext, pendingSql, clearPendingSql, ask } = useAssistantContext()

  const status = useQuery({
    queryKey: ['sandbox-status'],
    queryFn: () => knowledge.sandboxStatus(),
  })

  const datasets = status.data?.datasets ?? []
  const selected = dataset ?? datasets[0] ?? null
  const sqlReady = status.data?.ready === true && (status.data?.languages ?? []).includes('sql')
  const remaining =
    status.data?.sqlDailyLimit === undefined || status.data?.sqlUsedToday === undefined
      ? null
      : status.data.sqlDailyLimit - status.data.sqlUsedToday

  // 结构快照只读、不占配额，所以选中数据集就拉一次；切数据集会重新拉。
  const schemaQuery = useQuery({
    queryKey: ['dataset-schema', selected],
    queryFn: () => knowledge.datasetSchema(selected!),
    enabled: selected !== null,
    // 快照是构建期生成的静态内容，不会在会话中变——没必要反复拉
    staleTime: Infinity,
  })
  const schema = parseSchema(schemaQuery.data)
  const completionSchema = toCompletionSchema(schema)
  const [showEr, setShowEr] = useState(false)
  const [showChart, setShowChart] = useState(false)

  const run = useMutation({
    mutationFn: async (explain: boolean) => {
      if (selected === null) {
        throw new Error('没有可用的数据集')
      }
      return knowledge.runCode({ language: 'sql', code, dataset: selected, explain })
    },
    onSuccess: (result, explain) => {
      setError(
        result.exitCode === 0 && !result.timedOut && !result.outOfMemory ? null : describeFailure(result),
      )
      if (explain) {
        // 执行计划的树在**一个字段值**里（实测：1 列 × 1 行，多行用 \n）。
        // 所以取第一个结果集的第一行第一列，而不是把 tables 交给结果表组件。
        const first = result.tables?.[0]
        setExplainOutput(first?.rows[0]?.[0] ?? null)
        setTables(null)
      } else {
        setTables(result.tables ?? [])
        setExplainOutput(null)
      }
      status.refetch()
    },
    onError: (cause) => setError(cause instanceof Error ? cause.message : String(cause)),
  })

  /**
   * 上报页面场景给助手（WORK-035 T-15）。
   *
   * <p>**只放错误或行数摘要，不放数据行**：结果集可能上千行，塞进提示词既贵又可能
   * 把用户没想分享的数据送进模型。模型想看结构有 `sql_describe_dataset`。
   *
   * <p>离开页面时必须清掉——否则从 SQL 页跳到别处，助手还在说"你这段 SQL……"。
   */
  useEffect(() => {
    if (selected === null) {
      setPageContext(null)
      return
    }
    const summary =
      error !== null
        ? `报错：${error}`
        : tables !== null
          ? `返回 ${tables.length} 个结果集，共 ${tables.reduce((sum, table) => sum + table.rows.length, 0)} 行`
          : explainOutput !== null
            ? '刚才看了执行计划'
            : ''
    setPageContext({ kind: 'sql-workbench', datasetKey: selected, draftSql: code, lastResult: summary })
    return () => setPageContext(null)
  }, [selected, code, error, tables, explainOutput, setPageContext])

  /**
   * 消费助手填回的 SQL（WORK-035 T-15）。
   *
   * <p>**只填回编辑器，不执行**：助手没有执行 SQL 的能力（跑一次要 3 秒与一次配额，
   * 写操作还会真的落数据）。用户点"运行"才算数——这与 WORK-006 的"自主只建议不动作"一致。
   */
  useEffect(() => {
    if (pendingSql === null) {
      return
    }
    setCode(pendingSql.text)
    clearPendingSql(pendingSql.id)
  }, [pendingSql, clearPendingSql])

  return (
    <AppShell title="Paideia" actions={<LearnerHeader />} wide>
      <PageHeader
        eyebrow="SQL 工作台"
        title="写一段 SQL，立刻看到结果"
        description="不计分、不进学习进度。每次都在干净的数据库里跑，随便改随便试。"
      />

      {status.isPending ? (
        <p className="text-muted-foreground flex items-center gap-2 text-sm">
          <Spinner />
          检查运行环境…
        </p>
      ) : null}

      {status.isSuccess && !sqlReady ? (
        <Alert data-testid="sql-practice-unavailable">
          <AlertTitle>SQL 练习暂不可用</AlertTitle>
          <AlertDescription>
            {status.data.reason ?? '沙箱未开启或 SQL 镜像缺失'}。课程里的选择题仍然可以做。
          </AlertDescription>
        </Alert>
      ) : null}

      {sqlReady ? (
        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_280px]">
          <div className="space-y-4">
            <div className="flex flex-wrap items-center gap-3">
              <label className="text-sm" htmlFor="sql-dataset">
                数据集
              </label>
              <select
                id="sql-dataset"
                className="border-border bg-card rounded-md border px-2 py-1 text-sm"
                value={selected ?? ''}
                onChange={(event) => setDataset(event.target.value)}
              >
                {datasets.map((key) => (
                  <option key={key} value={key}>
                    {key}
                  </option>
                ))}
              </select>
              {remaining === null ? null : (
                <span className="text-muted-foreground text-xs" data-testid="sql-practice-quota">
                  今天还能跑 {remaining} 次
                </span>
              )}
            </div>

            <Suspense
              fallback={
                <div
                  className="border-border bg-muted/30 flex min-h-56 w-full items-center justify-center rounded-lg border"
                  data-testid="sql-editor-loading"
                >
                  <Spinner />
                </div>
              }
            >
              <SqlEditor
                value={code}
                onChange={setCode}
                schema={completionSchema}
                placeholder="写一段 SQL，按运行看结果"
                ariaLabel="SQL 编辑器"
              />
            </Suspense>

            <div className="flex flex-wrap items-center gap-3">
              <Button
                type="button"
                data-testid="sql-practice-run"
                disabled={run.isPending || selected === null}
                onClick={() => run.mutate(false)}
              >
                <Play className="size-4" />
                {run.isPending && run.variables === false ? '正在运行…' : '运行'}
              </Button>
              <Button
                type="button"
                variant="outline"
                data-testid="sql-practice-explain"
                disabled={run.isPending || selected === null}
                onClick={() => run.mutate(true)}
              >
                <Search className="size-4" />
                {run.isPending && run.variables === true ? '分析中…' : '看执行计划'}
              </Button>
              <Button
                type="button"
                variant="ghost"
                data-testid="sql-ask-assistant"
                onClick={() => ask('帮我看看这段 SQL 哪里有问题')}
              >
                问助手
              </Button>
              <span className="text-muted-foreground text-xs">
                每次运行约 4–5 秒（要起一个干净的数据库）
              </span>
            </div>

            {error === null ? null : (
              <Alert variant="destructive" data-testid="sql-practice-error">
                <AlertTitle>运行失败</AlertTitle>
                <AlertDescription className="whitespace-pre-wrap font-mono text-xs">
                  {error}
                </AlertDescription>
              </Alert>
            )}

            <Suspense fallback={<Spinner />}>
              {explainOutput === null ? null : <ExplainTree output={explainOutput} />}
            </Suspense>

            <Suspense fallback={<Spinner />}>
              {tables === null ? null : <ResultTables tables={tables} />}
            </Suspense>

            {/* 图表：只对第一张结果集提供（多语句时用户通常只关心主查询的那张） */}
            {tables !== null && tables.length > 0 && tables[0] !== undefined ? (
              <div className="space-y-2">
                <Button
                  type="button"
                  size="sm"
                  variant="ghost"
                  data-testid="sql-toggle-chart"
                  onClick={() => setShowChart((current) => !current)}
                >
                  {showChart ? '收起图表' : '把结果画成图'}
                </Button>
                {showChart ? (
                  <Suspense fallback={<Spinner />}>
                    <ResultChart table={tables[0]} />
                  </Suspense>
                ) : null}
              </div>
            ) : null}
          </div>

          <aside className="space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-medium">表结构</h2>
              <Button
                type="button"
                size="sm"
                variant="ghost"
                data-testid="sql-toggle-er"
                onClick={() => setShowEr((current) => !current)}
              >
                {showEr ? '看列表' : '看关系图'}
              </Button>
            </div>
            <Suspense fallback={<Spinner />}>
              {showEr ? <ErDiagram schema={schema} /> : <SchemaSidebar schema={schema} />}
            </Suspense>
          </aside>
        </div>
      ) : null}
    </AppShell>
  )
}

function describeFailure(result: {
  stderr: string
  timedOut: boolean
  outOfMemory: boolean
  truncated: boolean
}): string {
  if (result.timedOut) {
    return '查询超过时限，已被终止。'
  }
  if (result.outOfMemory) {
    return '内存超限，已被终止。'
  }
  if (result.truncated) {
    return '返回结果过多，请加条件缩小范围。'
  }
  const message = result.stderr.trim()
  return message === '' ? '执行失败（没有错误输出）' : message
}
