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

/**
 * v3：布局加了 `spreadApart`（最小间距），间距比例又从 0.12 提到 0.18。
 * 版本必须升——否则已存过坐标的浏览器会继续用旧的拥挤布局，节点间距问题对老用户永远不消失。
 */
const STORAGE_PREFIX = 'paideia:concept-graph:v3:'

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
    if (Object.keys(positions).length === 0) {
      return null
    }
    // **只在两个以上坐标时判重合**：一个坐标谈不上"互相重合"，那是正常存档。
    if (Object.keys(positions).length > 1) {
      const distinct = new Set(
        Object.values(positions).map((point) => `${point.x.toFixed(2)},${point.y.toFixed(2)}`),
      )
      if (distinct.size === 1) {
        return null
      }
    }
    return positions
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

/**
 * 把靠得太近的节点推开，保证两两间距不小于 `minDistanceRatio × 布局跨度`。
 *
 * <p><b>为什么必须有这一步</b>：sigma 不做标签碰撞检测，它只按网格限制"每格画几个标签"。
 * 力导向在十几个节点时会把它们挤进几个单位里（实测相邻两点只差总跨度的 6%），
 * 映射到屏幕上不到 60px，比一个中文标签还窄——于是标签必然互相压住。
 * 调密度只能让标签少画几个，画出来的照样重叠；要真正解决只能让节点分开。
 *
 * <p>取 0.18 的依据：推开后跨度会略增，实际最小间距约为新跨度的 12%，画布 1000px 时约 120px，
 * 横向放得下 5~6 个汉字标签，而十几个节点也还排得下（不会把图撑成一团待爆的气球）。
 *
 * <p>不变量：**纯推开、不旋转不缩放**，节点之间的相对方位基本保留，人对图谱的空间记忆不会失效。
 * 确定性：距离为零时按索引取方向，同一份输入永远得到同一份输出。
 */
export function spreadApart(
  positions: Record<number, Point>,
  minDistanceRatio = 0.18,
): Record<number, Point> {
  const ids = Object.keys(positions).map(Number)
  if (ids.length < 2) {
    return positions
  }
  const result: Record<number, Point> = {}
  for (const id of ids) {
    const point = positions[id]
    if (point !== undefined) {
      result[id] = { x: point.x, y: point.y }
    }
  }

  const xs = ids.map((id) => result[id]?.x ?? 0)
  const ys = ids.map((id) => result[id]?.y ?? 0)
  const span = Math.max(Math.max(...xs) - Math.min(...xs), Math.max(...ys) - Math.min(...ys))
  if (span <= 0) {
    return circularLayout(ids.map((id) => ({ id })))
  }
  const minDistance = span * minDistanceRatio

  // 成对推开是逐次逼近：推一次会改变别的对，所以跑到"这一轮没人被推开"为止。
  // 上限 80 轮只是防死循环，正常十几个节点十几轮就收敛。
  for (let round = 0; round < 80; round++) {
    let moved = false
    for (let i = 0; i < ids.length; i++) {
      for (let j = i + 1; j < ids.length; j++) {
        const a = result[ids[i] ?? -1]
        const b = result[ids[j] ?? -1]
        if (a === undefined || b === undefined) {
          continue
        }
        const dx = b.x - a.x
        const dy = b.y - a.y
        const distance = Math.hypot(dx, dy)
        if (distance >= minDistance) {
          continue
        }
        // 完全重合时方向未定义：按索引取一个确定的角度，避免随机数与 NaN
        const angle = (i * 7 + j * 13) % 360
        const ux = distance > 0 ? dx / distance : Math.cos((angle * Math.PI) / 180)
        const uy = distance > 0 ? dy / distance : Math.sin((angle * Math.PI) / 180)
        const push = (minDistance - distance) / 2
        a.x -= ux * push
        a.y -= uy * push
        b.x += ux * push
        b.y += uy * push
        moved = true
      }
    }
    if (!moved) {
      break
    }
  }
  return result
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
