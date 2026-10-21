/**
 * SQL 结果集判题口径（WORK-032 TASK-004）。
 *
 * <p><b>为什么单独一个模块</b>：SQL 一题多解是常态，判题必须按**结果**比而不是按 SQL 文本比；
 * 但归一化过头又会把「排序」这类题判成永远正确。所以每一条规则都要能指认，并且与
 * `scripts/sql_judge.py`（内容门禁用的那侧）在同一份语料上得出相同结论。
 *
 * <p><b>两侧不共享代码</b>：TS 与 Python 各写一遍，靠 `tests/sql-judge/corpus.json` 对照。
 * 这条纪律的理由在 `scripts/verify_course.py` 顶部已经踩过一次——两份规则合写一处必然漂移，
 * 而分开写又没有语料就必然各自漂移。语料是唯一的共同真相。
 *
 * <p><b>值一律是字符串</b>：服务端按字符串返回结果，类型信息来自题目声明（`normalize.types`）。
 * 这样归一化规则集中在这一个文件里，不依赖 JSON 数字在两侧各自丢精度。
 */

/** 列类型。`float` 刻意不提供：浮点误差会让判题不稳定，题目数据必须用 DECIMAL。 */
export type SqlColumnType = 'string' | 'int' | 'decimal' | 'datetime'

/** 服务端返回的一个结果集。 */
export interface SqlTable {
  columns: string[]
  rows: string[][]
}

/** 归一化口径。缺省：行序忽略、列序忽略、所有列按字符串比较。 */
export interface SqlNormalize {
  rowOrder?: 'ignore' | 'strict'
  columnOrder?: 'ignore' | 'strict'
  /** 按列名声明类型；未声明的列按 `string` 处理。 */
  types?: Record<string, SqlColumnType>
}

/** NULL 的固定标记。不能用空串——NULL 与 `''` 是两回事。 */
export const NULL_MARKER = '\u0000NULL'

const COLUMN_SEP = '\u001f'
const ROW_SEP = '\u001e'

/**
 * 按类型归一单个值。
 *
 * <p>字符串列**不做 trim**：尾部空格在 VARCHAR 里是有意义的值（NO PAD 排序规则下 `'ab'` ≠ `'ab '`），
 * 替用户裁掉等于把一处真实的差异藏起来。日期与数值才需要归一。
 */
export function normalizeCell(value: string, type: SqlColumnType = 'string'): string {
  if (value === NULL_MARKER) {
    return NULL_MARKER
  }
  switch (type) {
    case 'int':
      return normalizeInt(value)
    case 'decimal':
      return normalizeDecimal(value)
    case 'datetime':
      return normalizeDateTime(value)
    default:
      return value
  }
}

/** 整数：去前导零与多余的正负号；`-0` 归一为 `0`。不匹配整数形态时原样返回。 */
function normalizeInt(raw: string): string {
  const text = raw.trim()
  if (!/^[+-]?\d+$/.test(text)) {
    return raw
  }
  const negative = text.startsWith('-')
  const digits = text.replace(/^[+-]/, '').replace(/^0+(?=\d)/, '')
  return negative && /[1-9]/.test(digits) ? `-${digits}` : digits
}

/**
 * 定点数：按十进制字符串归一，**不经过浮点**——`0.1 + 0.2` 在二进制里是无限循环，
 * 用浮点归一化会让「金额精度」那一课自己打自己的脸。
 */
function normalizeDecimal(raw: string): string {
  const text = raw.trim()
  if (!/^[+-]?(\d+(\.\d*)?|\.\d+)$/.test(text)) {
    return raw
  }
  const negative = text.startsWith('-')
  const body = text.replace(/^[+-]/, '')
  const dot = body.indexOf('.')
  let integer = dot < 0 ? body : body.slice(0, dot)
  let fraction = dot < 0 ? '' : body.slice(dot + 1)
  integer = integer.replace(/^0+(?=\d)/, '')
  if (integer === '') {
    integer = '0'
  }
  fraction = fraction.replace(/0+$/, '')
  const unsigned = fraction === '' ? integer : `${integer}.${fraction}`
  return negative && unsigned !== '0' ? `-${unsigned}` : unsigned
}

/**
 * 日期时间：接受 `YYYY-MM-DD`、`YYYY-MM-DD HH:MM[:SS[.fff]]` 与 `T` 分隔的 ISO 写法，
 * 一律归一成 `YYYY-MM-DD HH:MM:SS`。日期按当天零点处理，于是 `2026-10-21` 与
 * `2026-10-21 00:00:00` 相等。
 */
function normalizeDateTime(raw: string): string {
  const text = raw.trim().replace('T', ' ')
  const match = /^(\d{4}-\d{2}-\d{2})(?:[ ](\d{2}):(\d{2})(?::(\d{2})(?:\.\d+)?)?)?$/.exec(text)
  if (match === null) {
    return raw
  }
  const day = match[1]
  const hour = match[2] ?? '00'
  const minute = match[3] ?? '00'
  const second = match[4] ?? '00'
  return `${day} ${hour}:${minute}:${second}`
}

/**
 * 规范序列化。**隐藏数据集的期望结果用它的哈希传输**，所以这个格式是两侧的硬契约：
 *
 * ```text
 * 列名（按名排序，除非声明 columnOrder=strict）以 \u001f 连接
 * \n
 * 每行各格以 \u001f 连接，行与行以 \u001e 连接（行序忽略时按行串排序）
 * ```
 *
 * 列按名排序的作用是：等价查询换个 SELECT 顺序得到同一个哈希。
 */
export function canonicalize(table: SqlTable, normalize: SqlNormalize = {}): string {
  const order = columnOrder(table, normalize)
  const columns = order.map((index) => table.columns[index])
  const rows = table.rows.map((row) => order.map((index) => cellAt(row, index, table, normalize)))
  if (normalize.rowOrder !== 'strict') {
    rows.sort((left, right) => compareStrings(left.join(COLUMN_SEP), right.join(COLUMN_SEP)))
  }
  return `${columns.join(COLUMN_SEP)}\n${rows.map((row) => row.join(COLUMN_SEP)).join(ROW_SEP)}`
}

/** 列索引顺序：默认按列名排序，`strict` 时保持原序。 */
function columnOrder(table: SqlTable, normalize: SqlNormalize): number[] {
  const indexes = table.columns.map((_, index) => index)
  if (normalize.columnOrder === 'strict') {
    return indexes
  }
  return indexes.sort((left, right) =>
    compareStrings(table.columns[left] ?? '', table.columns[right] ?? '') || left - right)
}

/** 按列名取类型；未声明或列名缺失时按字符串。 */
function typeOf(table: SqlTable, index: number, normalize: SqlNormalize): SqlColumnType {
  const name = table.columns[index]
  return (name === undefined ? undefined : normalize.types?.[name]) ?? 'string'
}

function cellAt(row: string[], index: number, table: SqlTable, normalize: SqlNormalize): string {
  const value = row[index]
  if (value === undefined) {
    throw new Error(`结果集第 ${index} 列缺失值（列名 ${table.columns[index] ?? '?'}）`)
  }
  return normalizeCell(value, typeOf(table, index, normalize))
}

/** 判题：把 actual 与 expected 按同一口径归一后比较。 */
export function judgeSql(expected: SqlTable, actual: SqlTable, normalize: SqlNormalize = {}): boolean {
  const pairing = pairColumns(expected.columns, actual.columns, normalize.columnOrder === 'strict')
  if (pairing === null) {
    return false
  }
  if (expected.rows.length !== actual.rows.length) {
    return false
  }
  // 按 expected 的列顺序取值，这样列序不同也能比；类型也按 expected 声明的列名取
  const expectedRows = expected.rows.map((row) =>
    row.map((value, index) => normalizeCell(value, typeOf(expected, index, normalize))))
  const actualRows = actual.rows.map((row) =>
    expected.columns.map((_, position) =>
      normalizeCell(row[pairing[position] ?? -1] ?? NULL_MARKER, typeOf(expected, position, normalize))))
  if (normalize.rowOrder === 'strict') {
    return expectedRows.every((row, index) => sameRow(row, actualRows[index] ?? []))
  }
  const sortKey = (row: string[]) => row.join(COLUMN_SEP)
  expectedRows.sort((left, right) => compareStrings(sortKey(left), sortKey(right)))
  actualRows.sort((left, right) => compareStrings(sortKey(left), sortKey(right)))
  return expectedRows.every((row, index) => sameRow(row, actualRows[index] ?? []))
}

function sameRow(left: string[], right: string[]): boolean {
  return left.length === right.length && left.every((value, index) => value === right[index])
}

/** expected 的第 i 列对应 actual 的哪一列。返回 null 表示列集合对不上。 */
function pairColumns(expected: string[], actual: string[], strictOrder: boolean): number[] | null {
  if (expected.length !== actual.length) {
    return null
  }
  if (strictOrder) {
    return expected.every((name, index) => actual[index] === name)
      ? expected.map((_, index) => index)
      : null
  }
  const remaining = new Map<string, number[]>()
  actual.forEach((name, index) => {
    const list = remaining.get(name) ?? []
    list.push(index)
    remaining.set(name, list)
  })
  const pairing: number[] = []
  for (const name of expected) {
    const list = remaining.get(name)
    const next = list === undefined ? undefined : list.shift()
    if (next === undefined) {
      return null
    }
    pairing.push(next)
  }
  return pairing
}

/** 按码点比较，不用 localeCompare：两侧必须给出同一个顺序。 */
function compareStrings(left: string, right: string): number {
  if (left === right) {
    return 0
  }
  return left < right ? -1 : 1
}

/** 规范串的 SHA-256（十六进制，小写）。浏览器与 Node 18+ 都有 `crypto.subtle`。 */
export async function canonicalSha256(table: SqlTable, normalize: SqlNormalize = {}): Promise<string> {
  const bytes = new TextEncoder().encode(canonicalize(table, normalize))
  const digest = await globalThis.crypto.subtle.digest('SHA-256', bytes)
  return [...new Uint8Array(digest)].map((byte) => byte.toString(16).padStart(2, '0')).join('')
}
