import { circularLayout, loadPositions, savePositions, type ConceptEdge, type ConceptNode, type Point } from '@paideia/knowledge'
import { useEffect, useRef, useState } from 'react'

import { tierStyle } from './tier-style'

interface Props {
  nodes: ConceptNode[]
  edges: ConceptEdge[]
  layoutKeyValue: string
  selectedId: number | null
  onSelect: (id: number) => void
}

/**
 * 2D 图谱画布（WORK-021）。
 *
 * <p>sigma + graphology 都是**动态加载**的：图库本身不小，不该让其他页面付这份首屏成本。
 *
 * <p>坐标持久化是这个组件存在的主要理由。forceAtlas2 每次收敛都会挪动节点，而人对图谱的
 * 定位感依赖"这个点一直在那片"。所以：有存档直接用，没存档算一次并存下来；
 * 「重新布局」由页面清掉存档再重挂本组件。
 */
export function GraphCanvas2D({ nodes, edges, layoutKeyValue, selectedId, onSelect }: Props) {
  const container = useRef<HTMLDivElement | null>(null)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let disposed = false
    let destroy: (() => void) | undefined

    void (async () => {
      try {
        // **必须自己探测 WebGL**：sigma 拿不到上下文时不会抛，它只是画出一片空白。
        // 那正是用户所说的"白屏"，也是唯一能在没有 GPU 的机器上提前给出提示的时机。
        if (!supportsWebGl()) {
          setFailed(true)
          return
        }

        const [graphology, sigmaModule, layoutModule] = await Promise.all([
          import('graphology'),
          import('sigma'),
          import('graphology-layout-forceatlas2'),
        ])
        if (disposed || container.current === null) {
          return
        }
        const Graph = graphology.default
        const Sigma = sigmaModule.default
        const forceAtlas2 = layoutModule.default

        const graph = new Graph({ multi: false, type: 'directed' })
        nodes.forEach((node) => {
          graph.addNode(String(node.id), {
            label: node.name,
            size: node.tier === 'unknown' ? 6 : 8 + node.score * 6,
            color: tierStyle(node.tier).color,
            x: 0,
            y: 0,
          })
        })
        edges.forEach((edge, index) => {
          const from = String(edge.from)
          const to = String(edge.to)
          if (from === to || !graph.hasNode(from) || !graph.hasNode(to)) {
            return
          }
          graph.addEdgeWithKey(`${edge.type}-${index}`, from, to, {
            color: edge.type === 'prerequisite' ? '#cbd5e1' : '#e2e8f0',
            size: edge.type === 'prerequisite' ? 1.4 : 1,
          })
        })

        const saved = loadPositions(layoutKeyValue)
        if (saved === null) {
          savePositions(layoutKeyValue, computeLayout(graph, forceAtlas2, nodes))
        } else {
          Object.entries(saved).forEach(([id, point]) => {
            if (graph.hasNode(id)) {
              graph.setNodeAttribute(id, 'x', point.x)
              graph.setNodeAttribute(id, 'y', point.y)
            }
          })
        }

        // 这三个选项在 sigma 4 的运行期设置里存在，但没进导出的类型声明；
        // 断言一次比把默认值改成手写渲染层便宜得多。
        const renderer = new Sigma(graph, container.current, {
          labelDensity: 0.6,
          labelGridCellSize: 90,
          labelRenderedSizeThreshold: 6,
        } as ConstructorParameters<typeof Sigma>[2])

        renderer.on('clickNode', ({ node }) => onSelect(Number(node)))
        renderer.on('clickStage', () => onSelect(-1))

        destroy = () => {
          renderer.kill()
        }
      } catch {
        if (!disposed) {
          // 没有 WebGL / 图库加载失败：给一句人话，不给白屏
          setFailed(true)
        }
      }
    })()

    return () => {
      disposed = true
      destroy?.()
    }
    // 选中态不参与重建：点一下节点就重画整张图会闪。onSelect 由父组件用 setState 传入，引用稳定。
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [layoutKeyValue])

  if (failed) {
    return (
      <div
        className="text-muted-foreground flex h-full items-center justify-center px-6 text-center text-sm"
        data-testid="graph-fallback"
      >
        这个浏览器无法绘制图谱（需要 WebGL）。你仍然可以在学习线里查看每一个知识点。
      </div>
    )
  }

  return (
    <div
      ref={container}
      className="h-full w-full"
      data-testid="graph-canvas-2d"
      data-selected={selectedId ?? ''}
    />
  )
}

/**
 * 能不能画 WebGL。
 *
 * <p>单独探测而不是等 sigma 报错：实测 sigma 在拿不到上下文时**不抛异常**，
 * 只是留一片空白画布——那与"图里没有节点"在界面上无法区分。
 */
function supportsWebGl(): boolean {
  try {
    const canvas = document.createElement('canvas')
    return canvas.getContext('webgl2') !== null || canvas.getContext('webgl') !== null
  } catch {
    return false
  }
}

/**
 * 优先用力导向；节点太少或算法抛错时退回圆环。
 *
 * <p>两种情况下都必须返回**每个节点都有坐标**的映射——坐标会被存下来，
 * 缺一个节点就等于那个点下次会掉在原点。
 */
function computeLayout(
  graph: { order: number; forEachNode: (callback: (id: string) => void) => void; getNodeAttribute: (id: string, key: string) => unknown },
  forceAtlas2: { assign: (graph: never, options: { iterations: number }) => void },
  nodes: ConceptNode[],
): Record<number, Point> {
  if (nodes.length <= 2) {
    return circularLayout(nodes)
  }
  try {
    forceAtlas2.assign(graph as never, { iterations: 120 })
    const positions: Record<number, Point> = {}
    graph.forEachNode((id) => {
      positions[Number(id)] = {
        x: Number(graph.getNodeAttribute(id, 'x')),
        y: Number(graph.getNodeAttribute(id, 'y')),
      }
    })
    const complete = nodes.every((node) => Number.isFinite(positions[node.id]?.x))
    return complete ? positions : circularLayout(nodes)
  } catch {
    return circularLayout(nodes)
  }
}
