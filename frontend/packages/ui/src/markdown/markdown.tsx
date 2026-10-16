import { cn } from 'cn'
import { lazy, Suspense, useMemo } from 'react'
import type { Components, CustomRenderer } from 'streamdown'

/**
 * markdown 渲染的**唯一实现**（WORK-012）。
 *
 * <p><b>为什么在这里而不是某个业务包</b>：学习者端的知识库条目、管理端的草稿预览、AI 助手的回答
 * 都要按 markdown 呈现。渲染器若跟业务概念走（原先在 `@paideia/knowledge`），"助手要说知识库的话"
 * 就得让助手依赖业务包；更要紧的是**两处各留一套就会在"预览与线上不一致"上翻车**，而预览的全部
 * 价值恰恰是"看到的就是用户看到的"（AC-008）。
 *
 * <p><b>两种模式，一条管线</b>：`mode` 是上游的公开开关，不是我们分叉出来的两条路。
 * 实测其语义（读的是 `streamdown` 打包产物里那行 `mode === "streaming" && parseIncompleteMarkdown
 * ? remend(text) : text`）：**只有流式模式才做未闭合补全**，静态模式原样使用文本。所以
 * `streaming` 这个 prop 的准确含义是"**这段文字可能还没长完**"——流式回答是，边打字边预览的笔记
 * 也是；已经定稿的知识库条目不是。
 *
 * <p><b>懒加载</b>：`streamdown` 基础包实测 +343KB，而它只有"真的要渲染 markdown"时才需要。
 * 面板一打开就 {@link preloadMarkdown}，别等第一个 token 到了才开始下载。
 *
 * <p><b>样式</b>：它的类名在它自己的 `dist` 里，靠 `globals.css` 的 `@source` 登记（漏了会静默缺样式）；
 * 它依赖的 `bg-sidebar` 令牌我们必须自己补（见该文件的注释）。
 */

const Streamdown = lazy(async () => {
  const module = await import('streamdown')
  return { default: module.Streamdown }
})

/** 预热渲染器（下载它的 chunk）。面板打开时调，让下载与用户打字并行。 */
export function preloadMarkdown(): void {
  void import('streamdown')
}

/**
 * 静态（长文）模式的呈现开关。**这两个是模块常量，不是每次渲染新建的字面量**——
 * 上游那个组件的记忆化比较里含 props 的引用相等，随手新建的对象会白让它重算一遍顶层。
 */
const DOCUMENT_CONTROLS = { table: false }
const DOCUMENT_LINK_SAFETY = { enabled: false }

/**
 * 一种围栏语言 → 我们自己的渲染器。
 *
 * <p>这是上游的公开扩展点（`plugins.renderers`），**加一种我们平台的元素不需要新依赖**：
 * 将来 ` ```面试评分 ` 或 ` ```架构图 ` 想渲染成平台特有的卡片，就是往这里加一项。
 */
export type MarkdownRenderer = CustomRenderer

export type MarkdownProps = {
  markdown: string
  /** 这段文字**可能还没长完**（流式回答、正在输入的预览）→ 走补全与动效。默认静态。 */
  streaming?: boolean
  className?: string
  /**
   * 调用方追加/覆盖的元素渲染（如知识库给图片挂灯箱）。
   * **必须是稳定引用**（`useMemo` 或模块常量）：它变了会让上游的块级记忆化失效，
   * 于是每个增量都重解析整篇——那正是本项要避免的事。
   */
  components?: Components
  /** 平台特有的围栏渲染器。同样是稳定引用。 */
  renderers?: MarkdownRenderer[]
}

/**
 * 长文阅读的排版预设。
 *
 * <p>这些类原先写在知识库条目的正文里，是照"15px 正文、中文长文"调过的：标题与正文拉开距离，
 * 行高 8（`leading-8`）。**它只覆盖排版，不覆盖代码块与表格的呈现**——代码块/表格是上游的
 * 结构化组件（带复制按钮、行号、横向滚动），两种模式共用同一套，这才是 AC-008 说的"单一实现"。
 */
const documentComponents: Components = {
  h1: ({ node, ...rest }) => <h1 className="mt-12 mb-4 text-2xl font-semibold" {...rest} />,
  h2: ({ node, ...rest }) => <h2 className="mt-10 mb-3 text-xl font-semibold" {...rest} />,
  h3: ({ node, ...rest }) => <h3 className="mt-8 mb-2 text-lg font-semibold" {...rest} />,
  h4: ({ node, ...rest }) => <h4 className="mt-6 mb-2 text-base font-semibold" {...rest} />,
  p: ({ node, ...rest }) => <p className="my-4 leading-8" {...rest} />,
  ul: ({ node, ...rest }) => <ul className="my-4 list-disc space-y-1.5 pl-6" {...rest} />,
  ol: ({ node, ...rest }) => <ol className="my-4 list-decimal space-y-1.5 pl-6" {...rest} />,
  blockquote: ({ node, ...rest }) => (
    <blockquote
      className="border-info/50 bg-info/5 my-6 rounded-r-lg border-l-4 px-4 py-1 [&>p:first-child]:mt-3 [&>p:last-child]:mb-3"
      {...rest}
    />
  ),
  a: ({ node, ...rest }) => (
    <a {...rest} target="_blank" rel="noreferrer noopener" className="underline underline-offset-2" />
  ),
}

/**
 * @example
 * // 流式回答（AI 助手）
 * <Markdown markdown={content} streaming={isGenerating} className="text-sm" />
 *
 * @example
 * // 定稿的长文（知识库条目）
 * <Markdown markdown={entry.body} />
 */
export function Markdown({ markdown, streaming = false, className, components, renderers }: MarkdownProps) {
  // 合并后的对象身份必须稳定：它进上游的块级记忆化比较，每次渲染换一个新对象会让记忆化全失效
  const mergedComponents = useMemo(
    () => (streaming ? components : { ...documentComponents, ...components }),
    [streaming, components],
  )
  const plugins = useMemo(() => (renderers === undefined ? undefined : { renderers }), [renderers])

  return (
    <Suspense fallback={<MarkdownLoading className={className} markdown={markdown} />}>
      <Streamdown
        mode={streaming ? 'streaming' : 'static'}
        isAnimating={streaming}
        className={className}
        components={mergedComponents}
        plugins={plugins}
        // 长文里的外链是管理员写进去的，直接打开；流式回答里的链接是模型生成的，走上游的确认弹窗
        linkSafety={streaming ? undefined : DOCUMENT_LINK_SAFETY}
        // 长文里不需要表格的复制/下载下拉——那是聊天场景里"把我给你的表拿走"才需要的
        controls={streaming ? undefined : DOCUMENT_CONTROLS}
      >
        {markdown}
      </Streamdown>
    </Suspense>
  )
}

/**
 * 渲染器还没下载完时的样子：**纯文本**。
 *
 * <p>这不是"随便给个占位"——换渲染器之前，助手回答本来就是纯文本，所以这里恰好是**上一版的真实
 * 行为**：网络慢时用户看到的是自己上一版看到的东西，而不是空白或骨架屏。文字内容一字不少，
 * 排版晚到而已。
 */
export function MarkdownLoading({ markdown, className }: { markdown: string; className?: string }) {
  return (
    <div className={cn('whitespace-pre-wrap', className)} data-testid="markdown-loading">
      {markdown}
    </div>
  )
}
