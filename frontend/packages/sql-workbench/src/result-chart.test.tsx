import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'

import { ResultChart, numericColumns, toNumber } from './result-chart'
import { NULL_MARKER } from './result-tables'

/**
 * 结果图表（WORK-035 T-06）。
 *
 * <p>最该守的是 **AC-005 的后半句：非数值列当 Y 轴时给明确提示，而不是画一张错图**。
 * 一张错图比没有图更糟——用户会拿它下结论。
 *
 * <p>**必须手动 cleanup**：本包没有配 `globals: true`，testing-library 的自动清理不会注册，
 * 于是上一个用例的 DOM 会留到下一个，`getByTestId` 撞到两个元素（实测踩过）。
 */
afterEach(() => cleanup())

const TABLE = {
  statementIndex: 0,
  columns: ['city', 'cnt'],
  rows: [
    ['北京', '2'],
    ['上海', '1'],
  ],
}

describe('toNumber', () => {
  it('正常数字', () => {
    expect(toNumber('42')).toBe(42)
    expect(toNumber('3.5')).toBe(3.5)
    expect(toNumber('-1')).toBe(-1)
  })

  /** NULL 与空串必须视为"缺失"而不是 0：当 0 会在图上凭空造出一个数据点。 */
  it('NULL 与空串是缺失，不是 0', () => {
    expect(toNumber(NULL_MARKER)).toBeNull()
    expect(toNumber('')).toBeNull()
    expect(toNumber(undefined)).toBeNull()
  })

  it('非数字是缺失', () => {
    expect(toNumber('abc')).toBeNull()
  })
})

describe('numericColumns', () => {
  it('只挑出数值列', () => {
    expect(numericColumns(TABLE)).toEqual([1])
  })
})

describe('ResultChart', () => {
  it('有数值列时渲染图表', () => {
    render(<ResultChart table={TABLE} />)
    expect(screen.getByTestId('result-chart')).toBeDefined()
    // 柱子或折线至少出现一个
    expect(document.querySelector('rect, polyline')).not.toBeNull()
  })

  /** AC-005 后半句：没有数值列时给提示，不渲染图表。 */
  it('没有数值列时给明确提示且不画图', () => {
    render(
      <ResultChart
        table={{
          statementIndex: 0,
          columns: ['a', 'b'],
          rows: [
            ['x', 'y'],
            ['z', 'w'],
          ],
        }}
      />,
    )
    expect(screen.getByTestId('chart-unavailable')).toBeDefined()
    expect(screen.queryByTestId('result-chart')).toBeNull()
  })

  it('Y 轴候选只包含数值列', () => {
    render(<ResultChart table={TABLE} />)
    const ySelect = screen.getByTestId('chart-y') as HTMLSelectElement
    const options = [...ySelect.options].map((option) => option.textContent)
    expect(options).toEqual(['cnt'])
  })
})
