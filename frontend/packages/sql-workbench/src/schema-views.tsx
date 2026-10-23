import { Badge } from '@paideia/ui'
import { Key } from 'lucide-react'

import { primaryKeys, type DatasetSchema, type SchemaTable } from './dataset-schema'

/**
 * 表结构侧栏（WORK-035 T-04）。
 *
 * <p>数据来自构建期的 schema 快照，**不查数据库、不起容器**。这一点很重要：
 * 侧栏在每次进页面时都会渲染，如果它去跑一次 `SHOW TABLES`，用户每打开一次练习页就等 3 秒。
 */
export function SchemaSidebar({ schema }: { schema: DatasetSchema | null }) {
  if (schema === null) {
    return (
      <p className="text-muted-foreground text-sm" data-testid="schema-unavailable">
        这个数据集还没有结构信息。
      </p>
    )
  }
  return (
    <div className="space-y-3" data-testid="schema-sidebar">
      <p className="text-muted-foreground text-xs">
        共 {schema.tables.length} 张表 ·{' '}
        {schema.tables.reduce((sum, table) => sum + table.columns.length, 0)} 列
      </p>
      {schema.tables.map((table) => (
        <TableCard key={table.name} table={table} />
      ))}
    </div>
  )
}

function TableCard({ table }: { table: SchemaTable }) {
  const keys = primaryKeys(table)
  return (
    <details className="border-border rounded-lg border" open>
      <summary className="cursor-pointer px-3 py-2 font-mono text-sm font-medium">
        {table.name}
        <span className="text-muted-foreground ml-2 text-xs font-normal">
          {table.columns.length} 列
        </span>
      </summary>
      <ul className="border-border divide-border divide-y border-t">
        {table.columns.map((column) => (
          <li key={column.name} className="flex items-center gap-2 px-3 py-1.5 text-xs">
            {column.primaryKey ? (
              <Key className="text-primary size-3 shrink-0" aria-label="主键" />
            ) : (
              <span className="size-3 shrink-0" />
            )}
            <span className="font-mono">{column.name}</span>
            <span className="text-muted-foreground font-mono">{column.type}</span>
            {column.nullable ? null : (
              <Badge variant="outline" className="ml-auto shrink-0">
                非空
              </Badge>
            )}
          </li>
        ))}
      </ul>
      {keys.length === 0 ? null : (
        <p className="text-muted-foreground border-border border-t px-3 py-1.5 text-xs">
          主键：{keys.join('、')}
        </p>
      )}
    </details>
  )
}

/**
 * ER 图（WORK-035 T-04）。
 *
 * <p><b>用纯 SVG 而不是引图库</b>：ER 图的布局是"表按网格摆、关系画线"，网格布局是十几行代码，
 * 而引入一个图布局库（哪怕 `graphology` 已在 app 依赖里）要为这点东西带上一套力导向算法与
 * 交互状态。更关键的是：ER 图**不该自动布局**——表的位置稳定，用户才能记住它在哪。
 *
 * <p>**没有关联时明确说明而不是画空图**：课程第 08 关讲"很多团队不用外键"，所以数据集
 * 常常一张外键都没有。这时画一张只有框没有线的图，用户会以为功能坏了。
 */
export function ErDiagram({ schema }: { schema: DatasetSchema | null }) {
  if (schema === null || schema.tables.length === 0) {
    return <p className="text-muted-foreground text-sm">没有可画的结构。</p>
  }

  const relations = schema.relations.filter(
    (relation) =>
      schema.tables.some((table) => table.name === relation.from) &&
      schema.tables.some((table) => table.name === relation.to),
  )

  const CARD_W = 150
  const CARD_H = 54
  const GAP = 24
  const COLS = 2
  const rows = Math.ceil(schema.tables.length / COLS)
  const width = COLS * CARD_W + (COLS - 1) * GAP
  const height = rows * CARD_H + (rows - 1) * GAP

  const position = (index: number) => ({
    x: (index % COLS) * (CARD_W + GAP),
    y: Math.floor(index / COLS) * (CARD_H + GAP),
  })

  return (
    <div className="space-y-2" data-testid="er-diagram">
      <svg
        viewBox={`0 0 ${width} ${height}`}
        className="border-border w-full max-w-xl rounded-lg border"
        role="img"
        aria-label="表关系图"
      >
        {relations.map((relation, index) => {
          const fromIndex = schema.tables.findIndex((table) => table.name === relation.from)
          const toIndex = schema.tables.findIndex((table) => table.name === relation.to)
          const from = position(fromIndex)
          const to = position(toIndex)
          return (
            <line
              key={`${relation.from}-${relation.to}-${index}`}
              x1={from.x + CARD_W / 2}
              y1={from.y + CARD_H / 2}
              x2={to.x + CARD_W / 2}
              y2={to.y + CARD_H / 2}
              stroke="var(--border)"
              strokeWidth={1.5}
            />
          )
        })}
        {schema.tables.map((table, index) => {
          const { x, y } = position(index)
          return (
            <g key={table.name}>
              <rect
                x={x}
                y={y}
                width={CARD_W}
                height={CARD_H}
                rx={6}
                fill="var(--card)"
                stroke="var(--border)"
              />
              <text x={x + 10} y={y + 22} fontSize={12} fontFamily="monospace" fill="var(--foreground)">
                {table.name}
              </text>
              <text x={x + 10} y={y + 40} fontSize={10} fill="var(--muted-foreground)">
                {table.columns.length} 列
              </text>
            </g>
          )
        })}
      </svg>
      {relations.length === 0 ? (
        <p className="text-muted-foreground text-xs">
          这个数据集没有标注表间关联，所以图上只有表没有连线。
          很多生产库也刻意不建外键（课程第 08 关讲过原因），这不代表结构有问题。
        </p>
      ) : null}
    </div>
  )
}
