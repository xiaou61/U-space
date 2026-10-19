import type { ConceptEdge, ConceptNode } from './learning-types'

/**
 * 图谱坐标的计算与持久化（WORK-021）。
 *
 * <p><b>为什么要存坐标</b>：力导向布局每次重新收敛都会改变节点位置，而人对图谱的记忆是
 * 空间性的（"Redis 那片在右下角"）。每次打开都换位置，图就退化成一张好看的星云，
 * 建立不起任何定位感。这是 2D/3D 都适用的前提。
 *
 * <p><b>什么时候重算</b>：图结构变了（新增概念或边）。用结构指纹判断，而不是时间——
 * 时间会让"什么都没变"也重算一次。
 */

export interface Point {
  x: number
  y: number
}

const STORAGE_PREFIX = 'paideia:concept-graph:v1:'

/**
 * 结构指纹：只由节点 id 与边的两端构成，不含分数与标题。
 *
 * <p>掌握度变化不该触发重新布局——那正是用户最不希望"图突然动了"的时刻。
 */
export function layoutKey(
  concepts: Pick<ConceptNode, 'id'>[],
  edges: Pick<ConceptEdge, 'from' | 'to'>[],
): string {
  const nodes = concepts.map((concept) => concept.id).sort((a, b) => a - b)
  const links = edges
    .map((edge) => `${Math.min(edge.from, edge.to)}-${Math.max(edge.from, edge.to)}`)
    .sort()
  return `${nodes.join(',')}|${links.join(',')}`
}

export function loadPositions(key: string): Record<number, Point> | null {
  const raw = read(key)
  if (raw === null) {
    return null
  }
  try {
    const parsed = JSON.parse(raw) as Record<string, Point>
    const positions: Record<number, Point> = {}
    for (const [id, point] of Object.entries(parsed)) {
      if (typeof point?.x === 'number' && typeof point?.y === 'number') {
        positions[Number(id)] = point
      }
    }
    return Object.keys(positions).length === 0 ? null : positions
  } catch {
    // 坏存档当作没有：重算一次即可，不值得让整页失败
    return null
  }
}

export function savePositions(key: string, positions: Record<number, Point>): void {
  write(key, JSON.stringify(positions))
}

export function resetPositions(key: string): void {
  try {
    globalThis.localStorage?.removeItem(STORAGE_PREFIX + key)
  } catch {
    // 隐私模式或配额满：没有存档也能用，只是下次会重算
  }
}

/**
 * 兜底布局：把节点摆成一个圆。
 *
 * <p>只在力导向不可用（节点太少、算法抛错）时用。它没有"好看"的追求，
 * 只保证每个点有唯一位置、彼此不重叠。
 */
export function circularLayout(concepts: Pick<ConceptNode, 'id'>[]): Record<number, Point> {
  const positions: Record<number, Point> = {}
  const count = Math.max(1, concepts.length)
  concepts.forEach((concept, index) => {
    const angle = (2 * Math.PI * index) / count
    positions[concept.id] = { x: Math.cos(angle) * 100, y: Math.sin(angle) * 100 }
  })
  return positions
}

function read(key: string): string | null {
  try {
    return globalThis.localStorage?.getItem(STORAGE_PREFIX + key) ?? null
  } catch {
    return null
  }
}

function write(key: string, value: string): void {
  try {
    globalThis.localStorage?.setItem(STORAGE_PREFIX + key, value)
  } catch {
    // 配额满或隐私模式：放弃持久化，不打断浏览
  }
}
