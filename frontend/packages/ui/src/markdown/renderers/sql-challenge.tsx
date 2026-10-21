import { useEffect, useState } from 'react'

import { Alert, AlertDescription, AlertTitle } from '../../components/alert'
import { Button } from '../../components/button'

import { useSourceReading } from './context'
import type { SqlChallenge } from './schema'
import { canonicalSha256, type SqlTable } from './sql-judge'

/**
 * SQL 写查询 / 改错（WORK-032）。
 *
 * <p><b>判题按结果、不按文本</b>：把服务端返回的 `tables` 按题面口径归一后比。SQL 一题多解
 * 是常态——`JOIN` 与子查询写的同一个查询必须都算对。
 *
 * <p><b>期望值来自 `expectHash`</b>：题面不带期望结果，只带规范序列化的 SHA-256。这样隐藏
 * 数据集的内容与答案都不必下发，而判定仍留在客户端（服务端不认识"正确答案"，与 WORK-015
 * 同一条纪律）。哈希用 Web Crypto 算，与 `scripts/sql_judge.py` 的规范串逐字节一致。
 *
 * <p><b>能力不探测就不渲染按钮</b>：与 `run` 题型同一条降级口径——渲染一个必然失败的按钮
 * 比不渲染更糟，用户会以为是自己点错了。沙箱不可用时只显示题面与起始 SQL。
 */
export function SqlBody({
  challenge,
  onGraded,
}: {
  challenge: SqlChallenge
  onGraded: (correct: number, total: number) => void
}) {
  const { sandbox } = useSourceReading()
  const [ready, setReady] = useState<boolean | null>(null)
  const [code, setCode] = useState(challenge.starter)
  const [running, setRunning] = useState(false)
  const [tables, setTables] = useState<SqlTable[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [results, setResults] = useState<{ key: string; passed: boolean }[] | null>(null)

  useEffect(() => {
    if (sandbox === undefined) {
      setReady(false)
      return
    }
    let alive = true
    sandbox
      .ready()
      .then((value) => {
        if (alive) {
          setReady(value)
        }
      })
      .catch(() => {
        if (alive) {
          setReady(false)
        }
      })
    return () => {
      alive = false
    }
  }, [sandbox])

  async function run() {
    if (sandbox === undefined) {
      return
    }
    setRunning(true)
    setError(null)
    setTables(null)
    setResults(null)
    try {
      // 每个数据集各跑一次：隐藏集只给 key，内容在服务端
      const outcomes: { key: string; passed: boolean }[] = []
      let lastTables: SqlTable[] | null = null
      for (const key of challenge.dataset) {
        const output = await sandbox.run({
          language: 'sql',
          code,
          dataset: key,
          caseId: challenge.id,
        })
        if (output.exitCode !== 0 || output.timedOut || output.outOfMemory) {
          setError(describeFailure(output))
          setTables(output.tables ?? null)
          return
        }
        const produced = output.tables ?? []
        lastTables = produced
        const first = produced[0]
        const expectedHash = challenge.expectHash[key]
        if (first === undefined || expectedHash === undefined) {
          outcomes.push({ key, passed: false })
          continue
        }
        const actualHash = canonicalSha256(first, challenge.normalize ?? {})
        outcomes.push({ key, passed: actualHash === expectedHash })
      }
      setTables(lastTables)
      setResults(outcomes)
      if (outcomes.length > 0) {
        onGraded(outcomes.filter((item) => item.passed).length, outcomes.length)
      }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause))
    } finally {
      setRunning(false)
    }
  }

  const firstTable = tables?.[0]

  return (
    <div className="space-y-3">
      {challenge.goal === undefined ? null : (
        <p className="text-muted-foreground text-sm">考的是：{challenge.goal}</p>
      )}

      <textarea
        aria-label="SQL 编辑器"
        className="border-border bg-muted/30 min-h-40 w-full rounded-lg border p-3 font-mono text-[12.5px] leading-6"
        value={code}
        spellCheck={false}
        onChange={(event) => setCode(event.target.value)}
      />

      {ready === false ? (
        <p className="text-muted-foreground text-sm">
          运行环境不可用（沙箱未开启或 SQL 镜像缺失），本题暂时只能阅读。
        </p>
      ) : (
        <div className="flex items-center gap-3">
          <Button type="button" size="sm" disabled={ready !== true || running} onClick={run}>
            {running ? '正在运行…' : '运行'}
          </Button>
          <span className="text-muted-foreground text-xs">
            数据集：{challenge.dataset.join('、')}
          </span>
        </div>
      )}

      {error === null ? null : (
        <Alert variant="destructive">
          <AlertTitle>运行失败</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {firstTable === undefined ? null : (
        <div className="border-border overflow-x-auto rounded-lg border">
          <table className="w-full text-left text-sm" data-testid="sql-result">
            <thead className="bg-muted/40">
              <tr>
                {firstTable.columns.map((column) => (
                  <th key={column} className="px-3 py-2 font-medium">
                    {column}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {firstTable.rows.map((row, index) => (
                <tr key={index} className="border-border border-t">
                  {row.map((cell, cellIndex) => (
                    <td key={cellIndex} className="px-3 py-2 font-mono text-[12.5px]">
                      {cell === '\u0000NULL' ? <span className="text-muted-foreground">NULL</span> : cell}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {results === null ? null : (
        <ul className="space-y-1 text-sm" data-testid="sql-verdict">
          {results.map((item) => (
            <li key={item.key} className={item.passed ? 'text-primary' : 'text-destructive'}>
              {item.key}：{item.passed ? '通过' : '结果与期望不一致'}
            </li>
          ))}
        </ul>
      )}

      {challenge.explain === undefined || results === null ? null : (
        <p className="text-muted-foreground text-sm">{challenge.explain}</p>
      )}
    </div>
  )
}

function describeFailure(output: {
  stderr: string
  timedOut: boolean
  outOfMemory: boolean
  truncated: boolean
}): string {
  if (output.timedOut) {
    return '查询超过时限，已被终止。'
  }
  if (output.outOfMemory) {
    return '内存超限，已被终止。'
  }
  if (output.truncated) {
    return '返回结果过多，请加条件缩小范围。'
  }
  const message = output.stderr.trim()
  return message === '' ? '执行失败（没有错误输出）' : message
}
