import { Button } from '@paideia/ui'
import { ArrowDown, ArrowUp, Check, Copy } from 'lucide-react'
import { useMemo, useState } from 'react'

/**
 * 结果集渲染（WORK-035 T-02）。
 *
 * <p><b>三条都来自实测</b>：服务端用 `mysql --xml` 跑，`tables[]` 每项带 `statementIndex`，
 * 值一律是字符串，`NULL` 是 `\u0000NULL` 这个哨兵（与 `SqlResultCodec.NULL_MARKER` 一致）。
 * 所以这里不需要"猜类型"，只需要把哨兵认出来。
 *
 * <p><b>为什么分页而不用虚拟滚动</b>：服务端已把单结果集截到 500 行（`maxRowsPerSet`），
 * 一次渲染 500 行的 DOM 在实测规模下没有可感卡顿。虚拟滚动要多一个依赖与一套测量逻辑，
 * 换不来用户能感觉到的差别。
 */

/** 与服务端 `SqlResultCodec.NULL_MARKER` 逐字一致——改一处必须改两处。 */
export const NULL_MARKER = '\u0000NULL'

export interface ResultTable {
  statementIndex: number
  columns: string[]
  rows: string[][]
}

/** 每页行数。500 行上限下最多 5 页，分页条不会长到没法用。 */
const PAGE_SIZE = 100

type SortState = { column: number; direction: 'asc' | 'desc' } | null

/**
 * 判断一列是否该右对齐。
 *
 * <p>**按值判断而不是按列名**：SQL 结果集不带类型信息（服务端把一切都变成字符串），
 * 而列名千奇百怪（`cnt`、`总数`、`x`）。取前 20 行看是不是全都能解析成数字更可靠。
 * 空值与 NULL 不算反例——`NULL` 在数值列里也很常见。
 */
export function looksNumeric(rows: string[][], column: number): boolean {
  const sample = rows.slice(0, 20).filter((row) => {
    const value = row[column]
    return value !== undefined && value !== NULL_MARKER && value !== ''
  })
  if (sample.length === 0) {
    return false
  }
  return sample.every((row) => {
    const value = row[column]!
    return /^-?\d+(\.\d+)?$/.test(value)
  })
}

/** 排序：NULL 始终排最后（升序降序都一样），因为"没有值"不该混进有序段里。 */
function sortRows(rows: string[][], sort: SortState): string[][] {
  if (sort === null) {
    return rows
  }
  const { column, direction } = sort
  const factor = direction === 'asc' ? 1 : -1
  return [...rows].sort((left, right) => {
    const a = left[column]
    const b = right[column]
    // NULL 排最后，不参与比较
    if (a === NULL_MARKER && b === NULL_MARKER) return 0
    if (a === NULL_MARKER) return 1
    if (b === NULL_MARKER) return -1
    if (a === undefined || b === undefined) return 0
    // 两列都是数值时按数值比，否则按字符串比（与 MySQL 的排序直觉一致）
    const na = Number(a)
    const nb = Number(b)
    if (a !== '' && b !== '' && !Number.isNaN(na) && !Number.isNaN(nb)) {
      return (na - nb) * factor
    }
    return a.localeCompare(b) * factor
  })
}

/** 转 CSV。`NULL` 导出成空单元格——CSV 没有 NULL 这个概念，写 "NULL" 会被当成字符串。 */
export function toCsv(table: ResultTable): string {
  const escape = (value: string) => {
    if (value === NULL_MARKER) return ''
    if (/[",\n\r]/.test(value)) {
      return `"${value.replace(/"/g, '""')}"`
    }
    return value
  }
  const lines = [table.columns.map(escape).join(',')]
  for (const row of table.rows) {
    lines.push(row.map(escape).join(','))
  }
  return lines.join('\n')
}

export function ResultTables({ tables }: { tables: ResultTable[] }) {
  if (tables.length === 0) {
    return <p className="text-muted-foreground text-sm">语句执行成功，没有返回结果集。</p>
  }
  return (
    <div className="space-y-4">
      {tables.map((table) => (
        <ResultTableCard key={table.statementIndex} table={table} multiple={tables.length > 1} />
      ))}
    </div>
  )
}

function ResultTableCard({ table, multiple }: { table: ResultTable; multiple: boolean }) {
  const [sort, setSort] = useState<SortState>(null)
  const [page, setPage] = useState(0)
  const [copied, setCopied] = useState(false)

  const sorted = useMemo(() => sortRows(table.rows, sort), [table.rows, sort])
  const pageCount = Math.max(1, Math.ceil(sorted.length / PAGE_SIZE))
  const safePage = Math.min(page, pageCount - 1)
  const visible = sorted.slice(safePage * PAGE_SIZE, safePage * PAGE_SIZE + PAGE_SIZE)

  const numeric = useMemo(
    () => table.columns.map((_, index) => looksNumeric(table.rows, index)),
    [table.columns, table.rows],
  )

  function toggleSort(index: number) {
    setSort((current) => {
      if (current === null || current.column !== index) {
        return { column: index, direction: 'asc' }
      }
      if (current.direction === 'asc') {
        return { column: index, direction: 'desc' }
      }
      return null
    })
    setPage(0)
  }

  async function copyCsv() {
    try {
      await navigator.clipboard.writeText(toCsv(table))
      setCopied(true)
      window.setTimeout(() => setCopied(false), 1500)
    } catch {
      // 剪贴板被拒（无权限/非安全上下文）：不弹错误，用户自己可以手动选。
      // 本项目不配 HTTPS，`navigator.clipboard` 在 http 下可能不可用——这是已知限制。
      setCopied(false)
    }
  }

  return (
    <div className="border-border overflow-hidden rounded-lg border" data-testid="result-table">
      <div className="bg-muted/40 flex items-center justify-between gap-2 px-3 py-2">
        <span className="text-muted-foreground text-xs">
          {multiple ? `第 ${table.statementIndex + 1} 条语句 · ` : ''}
          {table.rows.length} 行 · {table.columns.length} 列
        </span>
        <Button type="button" size="sm" variant="ghost" onClick={copyCsv} aria-label="复制为 CSV">
          {copied ? <Check className="size-3.5" /> : <Copy className="size-3.5" />}
          {copied ? '已复制' : 'CSV'}
        </Button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="bg-muted/20">
            <tr>
              {table.columns.map((column, index) => {
                const active = sort?.column === index
                return (
                  <th key={`${column}-${index}`} className="px-3 py-2 font-medium">
                    <button
                      type="button"
                      className="hover:text-foreground inline-flex items-center gap-1"
                      onClick={() => toggleSort(index)}
                      aria-label={`按 ${column} 排序`}
                    >
                      {column}
                      {active ? (
                        sort?.direction === 'asc' ? (
                          <ArrowUp className="size-3" />
                        ) : (
                          <ArrowDown className="size-3" />
                        )
                      ) : null}
                    </button>
                  </th>
                )
              })}
            </tr>
          </thead>
          <tbody>
            {visible.map((row, rowIndex) => (
              <tr key={rowIndex} className="border-border border-t">
                {row.map((cell, cellIndex) => (
                  <td
                    key={cellIndex}
                    className={`px-3 py-2 font-mono text-[12.5px] ${
                      numeric[cellIndex] === true ? 'text-right tabular-nums' : ''
                    }`}
                  >
                    {cell === NULL_MARKER ? (
                      // NULL 必须与空串在视觉上可区分（AC-002）：弱化 + 斜体，而不是留白
                      <span className="text-muted-foreground/60 italic">NULL</span>
                    ) : cell === '' ? (
                      // 空串也要有痕迹，否则用户分不清"空值"与"渲染丢了"
                      <span className="text-muted-foreground/40">∅</span>
                    ) : (
                      cell
                    )}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {pageCount > 1 ? (
        <div className="border-border flex items-center justify-between border-t px-3 py-2">
          <Button
            type="button"
            size="sm"
            variant="ghost"
            disabled={safePage === 0}
            onClick={() => setPage(safePage - 1)}
          >
            上一页
          </Button>
          <span className="text-muted-foreground text-xs">
            第 {safePage + 1} / {pageCount} 页
          </span>
          <Button
            type="button"
            size="sm"
            variant="ghost"
            disabled={safePage >= pageCount - 1}
            onClick={() => setPage(safePage + 1)}
          >
            下一页
          </Button>
        </div>
      ) : null}
    </div>
  )
}
