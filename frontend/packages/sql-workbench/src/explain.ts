/**
 * `EXPLAIN FORMAT=TREE` 的解析与判读（WORK-035 T-05）。
 *
 * <p><b>为什么需要这个函数而不是直接显示结果表</b>：实测（2026-10-23，真实镜像 + 生产加固参数）
 * 确认 `mysql --xml` 把整棵执行计划塞进**一个字段值**里，拿到的是「1 列 × 1 行」，多行之间是
 * `\n`。也就是说结构信息不在结果集里，只能从文本还原。
 *
 * <p><b>判读是字符串匹配，不是结构化字段</b>：MySQL 8 的 TREE 输出没有机器可读的"是否用索引"
 * 标记，只有 `Index lookup` / `Table scan` 这类文本。所以这里把判读收在一个纯函数里并单测，
 * 不散到组件中——判错了用户会得到一条假的性能结论。
 */

/** 一个执行计划节点。 */
export interface ExplainNode {
  /** 原始行（去掉缩进与 `-> ` 前缀）。 */
  text: string
  /** 缩进层级，从 0 开始。 */
  depth: number
  /** 这一行描述的访问方式。 */
  access: ExplainAccess
}

export type ExplainAccess = 'index' | 'scan' | 'other'

/**
 * 判定一行的访问方式。
 *
 * <p>顺序重要：`Index scan` 含 `scan`，`Table scan` 也含 `scan`，但前者是走索引、后者是全表扫。
 * 所以先判 `Table scan`（全表）再判索引族，不能反过来。
 *
 * <p>**不含 `Filter` / `Sort` / `Aggregate` 这类算子**：它们本身不决定"用不用索引"，
 * 标成 `other` 才不会把"Filter 在上、Table scan 在下"误读成"这行走了索引"。
 */
export function classifyAccess(text: string): ExplainAccess {
  const lower = text.toLowerCase()
  // 全表扫必须先判：`Table scan` 里也有 scan，晚判会被索引族的规则吃掉
  if (lower.includes('table scan')) {
    return 'scan'
  }
  if (
    lower.includes('index lookup') ||
    lower.includes('index range scan') ||
    lower.includes('index scan') ||
    lower.includes('index merge') ||
    lower.includes('covering index')
  ) {
    return 'index'
  }
  return 'other'
}

/**
 * 把 TREE 文本切成节点树（扁平数组，`depth` 表达层级）。
 *
 * <p>输入形如：
 * ```
 * -> Filter: (customers.`name` like '%a%')  (cost=0.55 rows=1)
 *     -> Table scan on customers  (cost=0.55 rows=3)
 * ```
 *
 * <p>缩进用空格数判定层级：MySQL 每层 4 个空格，但这里按「相对上一行的缩进增量」算，
 * 不写死 4——换 MySQL 版本后缩进宽度变了也不至于全平掉。
 */
export function parseExplainTree(output: string): ExplainNode[] {
  const lines = output.split('\n')
  const nodes: ExplainNode[] = []
  // 每层缩进宽度栈：stack[0] = 0
  const indents: number[] = [0]

  for (const raw of lines) {
    const trimmedRight = raw.replace(/\s+$/, '')
    if (trimmedRight.trim() === '') {
      continue
    }
    const leading = trimmedRight.length - trimmedRight.trimStart().length
    // 去掉 `-> ` 前缀（可能带前导空格，已在 leading 里扣掉）
    const body = trimmedRight.trimStart().replace(/^->\s*/, '')

    // 调整层级：当前缩进比栈顶深就压栈，比栈顶浅就出栈
    while (indents.length > 1 && leading < indents[indents.length - 1]!) {
      indents.pop()
    }
    if (leading > indents[indents.length - 1]!) {
      indents.push(leading)
    }
    nodes.push({
      text: body,
      depth: indents.length - 1,
      access: classifyAccess(body),
    })
  }
  return nodes
}

/**
 * 整条查询的汇总：有没有全表扫、有没有走索引。
 *
 * <p>给「一眼看出问题」用。注意它**不能替代节点级标注**：一条查询可以既有索引又有全表扫，
 * 汇总只是给一个总览判断。
 */
export interface ExplainSummary {
  hasScan: boolean
  hasIndex: boolean
  /** 成本估算里最大的 rows（找不到就是 null）。 */
  maxRows: number | null
}

export function summarize(nodes: ExplainNode[]): ExplainSummary {
  let hasScan = false
  let hasIndex = false
  let maxRows: number | null = null
  for (const node of nodes) {
    if (node.access === 'scan') {
      hasScan = true
    }
    if (node.access === 'index') {
      hasIndex = true
    }
    const match = /rows=(\d+)/.exec(node.text)
    if (match !== null) {
      const rows = Number(match[1])
      maxRows = maxRows === null ? rows : Math.max(maxRows, rows)
    }
  }
  return { hasScan, hasIndex, maxRows }
}
