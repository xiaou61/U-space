import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { NULL_MARKER, ResultTables, looksNumeric, toCsv } from './result-tables'

/**
 * 结果表（WORK-035 T-02）。
 *
 * <p>核心是 **AC-002：`NULL` 与空串在界面上必须可区分**。这条不是"好看"问题——
 * 课程第 02 关专门讲 `NULL` 与 `''` 的区别，如果界面把它们渲染成一样，那一关的教学就白讲了。
 */

const TABLE = {
  statementIndex: 0,
  columns: ['id', 'name', 'note'],
  rows: [
    ['1', 'ann', 'VIP'],
    ['2', 'bob', ''],
    ['3', 'cid', NULL_MARKER],
  ],
}

describe('NULL 与空串的区分（AC-002）', () => {
  it('NULL 渲染成 NULL 字样，空串渲染成空标记', () => {
    render(<ResultTables tables={[TABLE]} />)
    // NULL 必须出现文字，不能留白
    expect(screen.getByText('NULL')).toBeDefined()
    // 空串用 ∅ 表示，与 NULL 视觉不同
    expect(screen.getByText('∅')).toBeDefined()
  })

  it('两者不是同一个渲染结果', () => {
    const { container } = render(<ResultTables tables={[TABLE]} />)
    const cells = [...container.querySelectorAll('td')].map((cell) => cell.textContent)
    // bob 的 note（空串）与 cid 的 note（NULL）必须不同
    expect(cells).toContain('∅')
    expect(cells).toContain('NULL')
    expect(cells.filter((text) => text === 'NULL')).toHaveLength(1)
  })
})

describe('多语句分卡（AC-002）', () => {
  it('两条语句渲染两张卡', () => {
    const { container } = render(
      <ResultTables
        tables={[
          { statementIndex: 0, columns: ['a'], rows: [['1']] },
          { statementIndex: 1, columns: ['b'], rows: [['2']] },
        ]}
      />,
    )
    expect(container.querySelectorAll('[data-testid="result-table"]')).toHaveLength(2)
  })

  it('多语句时标注第几条', () => {
    const { container } = render(
      <ResultTables
        tables={[
          { statementIndex: 0, columns: ['a'], rows: [['1']] },
          { statementIndex: 1, columns: ['b'], rows: [['2']] },
        ]}
      />,
    )
    // 两段文案在同一个 span 里拼成一段，所以用容器文本断言而不是 getByText 正则
    const headers = [...container.querySelectorAll('[data-testid="result-table"] > div > span')].map(
      (node) => node.textContent,
    )
    expect(headers[0]).toContain('第 1 条语句')
    expect(headers[1]).toContain('第 2 条语句')
  })

  it('空结果集给明确文案而不是空白', () => {
    render(<ResultTables tables={[]} />)
    expect(screen.getByText(/没有返回结果集/)).toBeDefined()
  })
})

describe('looksNumeric', () => {
  it('全数值列判为数值', () => {
    expect(looksNumeric([['1'], ['2'], ['3.5']], 0)).toBe(true)
  })

  it('含非数值判为非数值', () => {
    expect(looksNumeric([['1'], ['abc']], 0)).toBe(false)
  })

  /** NULL 与空串不该让一个数值列被判成非数值——数值列里它们很常见。 */
  it('NULL 与空串不影响判定', () => {
    expect(looksNumeric([['1'], [NULL_MARKER], [''], ['2']], 0)).toBe(true)
  })

  it('全空列判为非数值（没有依据就别右对齐）', () => {
    expect(looksNumeric([[''], [NULL_MARKER]], 0)).toBe(false)
  })
})

describe('toCsv', () => {
  it('NULL 导出成空单元格而不是字符串 NULL', () => {
    const csv = toCsv(TABLE)
    const lines = csv.split('\n')
    expect(lines[0]).toBe('id,name,note')
    expect(lines[2]).toBe('2,bob,')
    // cid 那行的 note 是 NULL，导出后也应是空
    expect(lines[3]).toBe('3,cid,')
  })

  it('含逗号与引号的值被正确转义', () => {
    const csv = toCsv({
      statementIndex: 0,
      columns: ['a', 'b'],
      rows: [['x,y', 'he said "hi"']],
    })
    expect(csv.split('\n')[1]).toBe('"x,y","he said ""hi"""')
  })

  it('含换行的值被包在引号里', () => {
    const csv = toCsv({ statementIndex: 0, columns: ['a'], rows: [['line1\nline2']] })
    expect(csv.split('\n')[1]).toBe('"line1')
  })
})
