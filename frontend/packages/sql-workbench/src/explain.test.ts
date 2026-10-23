import { describe, expect, it } from 'vitest'

import { classifyAccess, parseExplainTree, summarize } from './explain'

/**
 * 这组用例的输入**全部来自实测**（2026-10-23，codenest-online 上的真实
 * `paideia-sandbox-mysql` 镜像 + 生产加固参数，走应用真实的 `mysql --xml` 路径）。
 *
 * <p>不自己编样例的理由：判读错了用户会拿到一条假的性能结论（"你这条走了索引"而其实全表扫），
 * 而那种错在界面上看起来完全正常。真实输出是唯一可信的语料。
 */

/** 走二级索引的真实输出（`EXPLAIN FORMAT=TREE SELECT * FROM customers WHERE city = '北京'`）。 */
const INDEX_OUTPUT =
  "-> Index lookup on customers using idx_customers_city (city='北京')  (cost=0.7 rows=2)"

/** 全表扫描的真实输出（`... WHERE name LIKE '%a%'`）。 */
const SCAN_OUTPUT =
  "-> Filter: (customers.`name` like '%a%')  (cost=0.55 rows=1)\n" +
  '    -> Table scan on customers  (cost=0.55 rows=3)'

describe('classifyAccess', () => {
  it('走二级索引', () => {
    expect(classifyAccess(INDEX_OUTPUT)).toBe('index')
  })

  it('全表扫描', () => {
    expect(classifyAccess('-> Table scan on customers  (cost=0.55 rows=3)')).toBe('scan')
  })

  /**
   * 顺序陷阱：`Index scan` 与 `Table scan` 都含 "scan"。
   * 先判索引族会把 `Table scan` 误判成走索引——用户会以为自己优化成功了。
   */
  it('Table scan 不能因为含 scan 被当成索引', () => {
    expect(classifyAccess('-> Table scan on customers')).toBe('scan')
    expect(classifyAccess('-> Index scan on customers using idx')).toBe('index')
    expect(classifyAccess('-> Index range scan on customers using idx')).toBe('index')
  })

  it('Filter / Sort 这类算子不算索引也不算全表扫', () => {
    expect(classifyAccess("-> Filter: (customers.`name` like '%a%')")).toBe('other')
    expect(classifyAccess('-> Sort: customers.id')).toBe('other')
    expect(classifyAccess('-> Aggregate: count(1)')).toBe('other')
  })
})

describe('parseExplainTree', () => {
  it('单行计划解析成一个节点', () => {
    const nodes = parseExplainTree(INDEX_OUTPUT)
    expect(nodes).toHaveLength(1)
    expect(nodes[0]?.depth).toBe(0)
    expect(nodes[0]?.access).toBe('index')
    // `-> ` 前缀要去掉，否则界面上会显示成 "-> Index lookup"
    expect(nodes[0]?.text).not.toContain('->')
    expect(nodes[0]?.text).toContain('Index lookup on customers')
  })

  /**
   * 这是实测发现的关键形态：一条查询里 Filter 在上、Table scan 在下。
   * 如果按整条查询判一个标签，就会漏掉"下面是全表扫"这件事。
   */
  it('多层计划保留层级，且每层各自判定', () => {
    const nodes = parseExplainTree(SCAN_OUTPUT)
    expect(nodes).toHaveLength(2)
    expect(nodes[0]?.depth).toBe(0)
    expect(nodes[0]?.access).toBe('other')
    expect(nodes[1]?.depth).toBe(1)
    expect(nodes[1]?.access).toBe('scan')
  })

  it('空输出返回空数组', () => {
    expect(parseExplainTree('')).toEqual([])
    expect(parseExplainTree('\n\n')).toEqual([])
  })

  /** 缩进宽度不写死 4：换 MySQL 版本后宽度变了也不该全平掉。 */
  it('缩进宽度变化不影响层级判定', () => {
    const twoSpaces = '-> Filter: x\n  -> Table scan on t'
    const eightSpaces = '-> Filter: x\n        -> Table scan on t'
    expect(parseExplainTree(twoSpaces).map((n) => n.depth)).toEqual([0, 1])
    expect(parseExplainTree(eightSpaces).map((n) => n.depth)).toEqual([0, 1])
  })
})

describe('summarize', () => {
  it('纯索引查询', () => {
    expect(summarize(parseExplainTree(INDEX_OUTPUT))).toEqual({
      hasScan: false,
      hasIndex: true,
      maxRows: 2,
    })
  })

  it('含全表扫的查询', () => {
    const summary = summarize(parseExplainTree(SCAN_OUTPUT))
    expect(summary.hasScan).toBe(true)
    expect(summary.hasIndex).toBe(false)
    // 两个节点的 rows 取最大：1 与 3
    expect(summary.maxRows).toBe(3)
  })

  it('没有 rows 信息时为 null', () => {
    expect(summarize(parseExplainTree('-> Table scan on t')).maxRows).toBeNull()
  })
})
