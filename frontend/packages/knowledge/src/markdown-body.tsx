import { Markdown, preloadMarkdown, type MarkdownProps, type SandboxPort } from '@paideia/ui'
import { useEffect, useMemo, useRef } from 'react'

import { extractHeadings } from './heading-slug'
import { FigureWithLightbox } from './image-lightbox'
import { resolveMediaUrl } from './media'
import { createEntryProgressPort } from './source-reading-progress'

/**
 * 渲染器（约 500 kB）**在本模块被求值时就开下**，不等数据回来。
 *
 * <p>它是懒加载的（否则会拖累主包——`@paideia/knowledge` 的 barrel 被首页那些页面静态引入，
 * 静态引入渲染器等于把它塞进主包）。而本模块只在"要看 markdown 的那几个页面"的 chunk 里，
 * 所以这里发起下载是安全的：**与条目的接口请求并行**，而不是等正文到手才开始下。
 * 少了这一句，读长文的人会先看到一小段没排版的原文。
 */
preloadMarkdown()

/**
 * 知识库条目正文渲染。
 *
 * <p><b>渲染器不在这里</b>（WORK-012）：markdown 的呈现由 `@paideia/ui` 的 `Markdown` 统一提供，
 * 助手回答与这里跑的是同一条管线。本组件只做三件"知识库特有"的事：
 * 回填标题锚点（目录要用）、图片换成可放大的图注（{@link FigureWithLightbox}）、
 * 把正文里的相对附件地址补成绝对地址（{@link resolveMediaUrl}）。
 *
 * <p><b>标题 id 在 effect 里回填，而不是在组件里用一个计数闭包。</b>两种写法的差别在
 * 重复渲染上：React 在开发模式下会渲染两遍，若 id 由一个"渲染期间自增"的闭包给出，
 * 第二遍会把序号继续往后推，于是目录里的锚点全部对不上（而且只在开发模式暴露）。
 * 回填是幂等的：同样的 markdown 永远得到同一批 id。
 *
 * <p>与本包 {@link import('./entry-toc').EntryToc} 共用 `extractHeadings`，
 * 因此目录与正文按**同一顺序**编号，重复标题也不会串。
 *
 * <p><b>源码解读的接入点（WORK-014）</b>：传了 `entryId` 才有进度、传了 `onAskAssistant`
 * 才有「问 AI」、传了 `onOpenEntry` 才有站点跳转。三样都是可选，所以管理端预览与笔记预览
 * 什么都不用改：块照常渲染，只是不可点、不留痕。
 */
export function MarkdownBody({
  markdown,
  className,
  mediaBaseUrl,
  entryId,
  entryTitle,
  onAskAssistant,
  onOpenEntry,
  sandbox,
}: {
  markdown: string
  className?: string
  /**
   * 正文里相对附件地址（`/api/v1/knowledge/files/...`）要拼上的后端基址。
   * 由调用方从自己的 `API_BASE_URL` 传进来——本包不认识部署形态，也不该认识。
   */
  mediaBaseUrl?: string
  /** 当前条目 id；**只有它是数字时**才记录阅读进度。 */
  entryId?: number
  entryTitle?: string
  /** 由应用注入：把一句话交给助手输入框（不自动发送）。 */
  onAskAssistant?: (prompt: string) => void
  /** 由应用注入：学习路径里点击某站时跳转。 */
  onOpenEntry?: (entryId: number) => void
  /** 由应用注入：代码执行（WORK-015）。不传时「运行」按钮不渲染。 */
  sandbox?: SandboxPort
}) {
  const container = useRef<HTMLDivElement>(null)
  // 只依赖 markdown：同一份正文的标题序列是确定的
  const headings = useMemo(() => extractHeadings(markdown), [markdown])

  useEffect(() => {
    const nodes = container.current?.querySelectorAll('h1, h2, h3, h4')
    if (nodes === undefined || nodes === null) {
      return
    }
    nodes.forEach((node, index) => {
      const heading = headings[index]
      if (heading !== undefined) {
        node.id = heading.id
      }
    })
  }, [headings])

  // 必须是稳定引用：它变了会让上游的块级记忆化失效（长文每次重渲染都重解析整篇）
  const components = useMemo<MarkdownProps['components']>(
    () => ({
      img: (props) => (
        <FigureWithLightbox
          src={resolveMediaUrl(typeof props.src === 'string' ? props.src : '', mediaBaseUrl)}
          alt={typeof props.alt === 'string' ? props.alt : ''}
        />
      ),
    }),
    [mediaBaseUrl],
  )

  const progress = useMemo(() => createEntryProgressPort(entryId, entryTitle), [entryId, entryTitle])

  const documentContext = useMemo(
    () => ({ entryId, entryTitle, onAskAssistant, onOpenEntry, progress, sandbox }),
    [entryId, entryTitle, onAskAssistant, onOpenEntry, progress, sandbox],
  )

  return (
    <div
      ref={container}
      data-testid="markdown-body"
      className={className ?? 'text-[0.9375rem] leading-8'}
    >
      <Markdown markdown={markdown} components={components} documentContext={documentContext} />
    </div>
  )
}
