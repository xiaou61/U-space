import { useEffect, useMemo, useState } from 'react'

import { extractHeadings } from './heading-slug'

/**
 * 正文目录：**贴右侧的一条竖线 + 当前章节高亮**。
 *
 * <p>这一版照文档站的做法改过：原来是正文上方一个灰底方框，读长文时它早就滚出屏幕了，
 * 而右侧的竖线是"读到哪了"唯一能常驻的指示。
 *
 * <p><b>用按钮 + `scrollIntoView` 而不是 `href="#id"`</b>：本应用用 Hash 路由，
 * 地址栏的 hash 是**路由**——写成 `href="#常见误解"` 会被路由器当成"跳到名为
 * 常见误解的路由"，结果是跳到一个空白页而不是滚动到章节。这是 Hash 路由下很容易踩的一脚。
 *
 * <p>也因此不改地址栏：滚动是纯视图行为，不值得为它生成一个路由状态。
 *
 * <p><b>当前章节按 DOM 顺序对应，而不是按 id 去找</b>：正文里标题的 id 由 `MarkdownBody`
 * **在 effect 里**回填，而本组件的 effect 可能先跑——那时按 id 一个都找不到。改成取正文容器
 * 里的标题元素（同一批、同一顺序，这正是 `extractHeadings` 的既有约定），与回填时机无关。
 */
export function EntryToc({ markdown }: { markdown: string }) {
  const headings = useMemo(() => extractHeadings(markdown), [markdown])
  const [activeId, setActiveId] = useState<string | null>(null)

  useEffect(() => {
    if (headings.length === 0 || typeof IntersectionObserver === 'undefined') {
      return
    }
    const nodes = document.querySelectorAll(
      '[data-testid="markdown-body"] h1, [data-testid="markdown-body"] h2, [data-testid="markdown-body"] h3, [data-testid="markdown-body"] h4',
    )
    if (nodes.length === 0) {
      return
    }
    // 只在"页面偏上"那一段判定：标题滚到接近顶部才算当前，否则刚进页面就会高亮最后一个
    const observer = new IntersectionObserver(
      (records) => {
        const entering = records
          .filter((record) => record.isIntersecting)
          .sort((left, right) => left.boundingClientRect.top - right.boundingClientRect.top)[0]
        const id = entering?.target.id ?? ''
        if (id !== '') {
          setActiveId(id)
        }
      },
      { rootMargin: '-72px 0px -70% 0px' },
    )
    nodes.forEach((node) => {
      observer.observe(node)
    })
    return () => {
      observer.disconnect()
    }
  }, [headings])

  if (headings.length < 2) {
    // 只有一个标题（通常就是开头那一句）时目录没有导航价值，不如不显示
    return null
  }

  return (
    <nav aria-label="正文目录" data-testid="entry-toc">
      <div className="text-muted-foreground mb-3 text-xs font-medium">本页目录</div>
      <ul className="border-border border-l">
        {headings.map((heading) => {
          const active = heading.id === activeId
          return (
            <li key={heading.id} style={{ paddingLeft: `${(heading.level - 1) * 10}px` }}>
              <button
                type="button"
                aria-current={active ? 'location' : undefined}
                onClick={() => {
                  const target = document.getElementById(heading.id)
                  target?.scrollIntoView({ behavior: 'smooth', block: 'start' })
                }}
                className={`-ml-px block w-full border-l-2 py-1 pl-3 text-left text-xs leading-6 transition-colors ${
                  active
                    ? 'border-primary text-primary font-medium'
                    : 'text-muted-foreground hover:border-border hover:text-foreground border-transparent'
                }`}
              >
                {heading.text}
              </button>
            </li>
          )
        })}
      </ul>
    </nav>
  )
}
