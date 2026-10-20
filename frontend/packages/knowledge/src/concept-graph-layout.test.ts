import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import {
  circularLayout,
  layoutKey,
  loadPositions,
  resetPositions,
  savePositions,
} from './concept-graph-layout'

/**
 * 坐标存档的三条纪律：**结构没变就复用**、**结构变了就重算**、**存档坏了当没有**。
 *
 * <p>第三条尤其重要：图谱页不该因为一段坏 JSON 就整页打不开。
 */
const KEY = 'paideia:concept-graph:v1:'

let store: Map<string, string>

beforeEach(() => {
  store = new Map()
  vi.stubGlobal('localStorage', {
    getItem: (key: string) => store.get(key) ?? null,
    setItem: (key: string, value: string) => void store.set(key, value),
    removeItem: (key: string) => void store.delete(key),
  })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('layoutKey', () => {
  it('节点顺序与边的方向不影响指纹', () => {
    const a = layoutKey([{ id: 2 }, { id: 1 }], [{ from: 2, to: 1 }])
    const b = layoutKey([{ id: 1 }, { id: 2 }], [{ from: 1, to: 2 }])
    expect(a).toBe(b)
  })

  it('新增节点会改变指纹', () => {
    const before = layoutKey([{ id: 1 }], [])
    const after = layoutKey([{ id: 1 }, { id: 2 }], [])
    expect(after).not.toBe(before)
  })

  it('新增边会改变指纹', () => {
    const before = layoutKey([{ id: 1 }, { id: 2 }], [])
    const after = layoutKey([{ id: 1 }, { id: 2 }], [{ from: 1, to: 2 }])
    expect(after).not.toBe(before)
  })
})

describe('positions', () => {
  it('同一指纹下保存后能原样读回', () => {
    savePositions('k1', { 1: { x: 1.5, y: -2 } })
    expect(loadPositions('k1')).toEqual({ 1: { x: 1.5, y: -2 } })
  })

  it('不同指纹互不影响', () => {
    savePositions('k1', { 1: { x: 1, y: 1 } })
    expect(loadPositions('k2')).toBeNull()
  })

  it('重置后回到没有存档', () => {
    savePositions('k1', { 1: { x: 1, y: 1 } })
    resetPositions('k1')
    expect(loadPositions('k1')).toBeNull()
  })

  it('坏 JSON 当作没有存档，而不是抛错', () => {
    store.set(KEY + 'bad', '{not json')
    expect(loadPositions('bad')).toBeNull()
  })

  it('坐标字段缺失的条目被忽略', () => {
    store.set(KEY + 'partial', JSON.stringify({ 1: { x: 1, y: 2 }, 2: { x: 'oops' } }))
    expect(loadPositions('partial')).toEqual({ 1: { x: 1, y: 2 } })
  })

  it('全部重合的存档当作没有存档', () => {
    // 旧版本节点初值全是 (0,0)，力导向推不开时就把这一整片零存了下来；
    // 用它画出来的是一颗巨大的色团，不是一张图。
    store.set(KEY + 'flat', JSON.stringify({ 1: { x: 0, y: 0 }, 2: { x: 0, y: 0 } }))
    expect(loadPositions('flat')).toBeNull()
  })

  it('单节点的存档不受重合规则影响', () => {
    savePositions('k1', { 1: { x: 0, y: 0 } })
    expect(loadPositions('k1')).toEqual({ 1: { x: 0, y: 0 } })
  })
})

describe('circularLayout', () => {
  it('每个节点都有唯一坐标', () => {
    const positions = circularLayout([{ id: 1 }, { id: 2 }, { id: 3 }])
    expect(Object.keys(positions)).toHaveLength(3)
    const points = Object.values(positions).map((point) => `${point.x.toFixed(3)},${point.y.toFixed(3)}`)
    expect(new Set(points).size).toBe(3)
  })

  it('只有一个节点时也给出坐标', () => {
    expect(circularLayout([{ id: 7 }])).toEqual({ 7: { x: 100, y: 0 } })
  })
})
