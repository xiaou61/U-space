import { createKnowledgeApi } from '@paideia/knowledge'
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
import { Play } from 'lucide-react'
import { lazy, Suspense, useState } from 'react'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'

const knowledge = createKnowledgeApi(api)

/**
 * 编辑器必须**懒加载**（WORK-035 T-01 / AC-001）：CodeMirror 精简版 gzip 后约 137 kB，
 * 是本站唯一的重前端依赖。静态 import 会让每个进入首页的人为它付费。
 */
const SqlEditor = lazy(async () => {
  const module = await import('@paideia/sql-workbench')
  return { default: module.SqlEditor }
})

/**
 * SQL 自由练习（WORK-032，WORK-035 起升级为工作台）。
 *
 * <p><b>与关卡的区别是"不计分"</b>：这里跑什么、跑对没跑对，都不写 `learning_progress`。
 * 关卡里的 SQL 题要判题、要计入这一关的通过与否；这里只是"随时打开、随便写"。
 *
 * <p>数据集仍只有服务端下发的那几个（隐藏集不下发，见 WORK-035 的修复）。
 *
 * <p>额度与关卡实训共用一份 SQL 配额，顶部显示剩余次数。
 */
export function SqlPracticePage() {
  const [code, setCode] = useState('SELECT * FROM customers;')
  const [tables, setTables] = useState<{ columns: string[]; rows: string[][] }[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [dataset, setDataset] = useState<string | null>(null)

  const status = useQuery({
    queryKey: ['sandbox-status'],
    queryFn: () => knowledge.sandboxStatus(),
  })

  const datasets = status.data?.datasets ?? []
  const selected = dataset ?? datasets[0] ?? null
  const sqlReady = status.data?.ready === true && (status.data?.languages ?? []).includes('sql')
  const remaining = status.data?.sqlDailyLimit === undefined || status.data?.sqlUsedToday === undefined
    ? null
    : status.data.sqlDailyLimit - status.data.sqlUsedToday

  const run = useMutation({
    mutationFn: async () => {
      if (selected === null) {
        throw new Error('没有可用的数据集')
      }
      return knowledge.runCode({ language: 'sql', code, dataset: selected })
    },
    onSuccess: (result) => {
      setError(result.exitCode === 0 && !result.timedOut && !result.outOfMemory
        ? null
        : describeFailure(result))
      setTables(result.tables ?? [])
      // 跑完刷新额度：服务端记了一条审计
      status.refetch()
    },
    onError: (cause) => setError(cause instanceof Error ? cause.message : String(cause)),
  })

  return (
    <AppShell title="Paideia" actions={<LearnerHeader />} wide>
      <PageHeader
        eyebrow="自由练习"
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
              placeholder="写一段 SQL，按运行看结果"
              ariaLabel="SQL 编辑器"
            />
          </Suspense>

          <div className="flex items-center gap-3">
            <Button
              type="button"
              data-testid="sql-practice-run"
              disabled={run.isPending || selected === null}
              onClick={() => run.mutate()}
            >
              <Play className="size-4" />
              {run.isPending ? '正在运行…' : '运行'}
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

          {tables === null ? null : tables.length === 0 ? (
            <p className="text-muted-foreground text-sm">语句执行成功，没有返回结果集。</p>
          ) : (
            tables.map((table, index) => (
              <div key={index} className="border-border overflow-x-auto rounded-lg border">
                <table className="w-full text-left text-sm" data-testid="sql-practice-result">
                  <thead className="bg-muted/40">
                    <tr>
                      {table.columns.map((column) => (
                        <th key={column} className="px-3 py-2 font-medium">
                          {column}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {table.rows.map((row, rowIndex) => (
                      <tr key={rowIndex} className="border-border border-t">
                        {row.map((cell, cellIndex) => (
                          <td key={cellIndex} className="px-3 py-2 font-mono text-[12.5px]">
                            {cell === '\u0000NULL'
                              ? <span className="text-muted-foreground">NULL</span>
                              : cell}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ))
          )}
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
