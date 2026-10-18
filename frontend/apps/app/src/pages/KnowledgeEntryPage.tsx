import {
  EntryToc,
  KnowledgeNeighborhood,
  MarkdownBody,
  SelfAssessmentButtons,
  createKnowledgeApi,
} from '@paideia/knowledge'
import type { EntryRef, SelfAssessmentLevel } from '@paideia/knowledge'
import {
  Alert,
  AlertDescription,
  AlertTitle,
  AppShell,
  PageHeader,
  Spinner,
} from '@paideia/ui'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useAssistantContext } from '@paideia/assistant'
import { ArrowLeft, ArrowRight, ChevronLeft } from 'lucide-react'
import { useCallback, useEffect, useMemo } from 'react'
import { Link, useNavigate, useParams } from 'react-router'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'
import { API_BASE_URL } from '../env'

const knowledge = createKnowledgeApi(api)

/**
 * 条目详情：**一栏阅读正文 + 右侧常驻目录**。
 *
 * <p>照文档站的正文页排：左栏是阅读区（宽度上限 46rem 左右，汉字一行 30 多字最好读），
 * 右侧是常驻目录，正文之后才是"上一篇／下一篇"与关系列表——三块阅读辅助都不打断阅读流。
 *
 * <p>上下篇做成**卡片**而不是两个文字链接：它们是长文里唯一的"继续读"出口，
 * 而文字链接在一屏里几乎看不见。
 */
export function KnowledgeEntryPage() {
  const params = useParams()
  const entryId = Number(params['id'])
  const valid = Number.isFinite(entryId)
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { setEntry, ask } = useAssistantContext()

  /**
   * 源码解读的双向入口（WORK-014）：内容里的「问 AI」把一句话交给助手输入框，
   * 学习路径点击某站直接跳过去。两个都保持**稳定引用**——它们会进 `MarkdownBody`
   * 的 memo 依赖，每次渲染换新函数会让整篇正文重解析。
   */
  const openEntry = useCallback((id: number) => navigate(`/knowledge/entries/${id}`), [navigate])

  /**
   * 代码执行（WORK-015）：把能力探测与运行封成端口交给正文块。
   *
   * <p>**只在条目页注入**——管理端预览、笔记预览都没有它，所以那些地方的「运行」按钮不会出现。
   * `ready` 每次都问一次（很轻），因为用户可能在别的标签页跑完了当天的额度。
   */
  const sandbox = useMemo(
    () => ({
      ready: () => knowledge.sandboxStatus().then((status) => status.ready),
      run: (input: { language: string; code: string; stdin?: string; caseId?: string }) =>
        knowledge.runCode({ ...input, entryId }),
    }),
    [entryId],
  )

  const entry = useQuery({
    queryKey: ['knowledge-entry', entryId],
    queryFn: () => knowledge.entry(entryId),
    enabled: valid,
  })

  // 把"当前在读哪一条"交给悬浮壳：它是唯一知道这件事的地方。
  // 离开页面时清空，壳随即回到"打开一条内容后可用"的状态。
  useEffect(() => {
    if (entry.data === undefined) {
      return
    }
    setEntry({ id: entry.data.id, title: entry.data.title })
    return () => setEntry(null)
  }, [entry.data, setEntry])

  // 标记与取消共用一次 mutation：对用户来说是"改我的标记"，不是两个动作
  const assess = useMutation({
    mutationFn: (next: SelfAssessmentLevel | null) =>
      next === null ? knowledge.clearSelfAssessment(entryId) : knowledge.markSelfAssessment(entryId, next),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['knowledge-entry', entryId] })
      await queryClient.invalidateQueries({ queryKey: ['my-assessments'] })
    },
  })

  return (
    <AppShell title="Paideia" actions={<LearnerHeader />} wide>
      <Link
        to="/knowledge"
        className="text-muted-foreground hover:text-foreground mb-6 inline-flex items-center gap-1 text-sm transition-colors"
        data-testid="back-to-categories"
      >
        <ChevronLeft className="size-4" />
        返回知识库
      </Link>

      {!valid ? (
        <Alert variant="destructive" data-testid="entry-state">
          <AlertTitle>地址里的条目编号不合法</AlertTitle>
        </Alert>
      ) : null}

      {valid && entry.isPending ? (
        <p className="text-muted-foreground flex items-center gap-2 text-sm" data-testid="entry-state">
          <Spinner />
          加载条目…
        </p>
      ) : null}

      {valid && entry.isError ? (
        <Alert variant="destructive" data-testid="entry-state">
          <AlertTitle>打不开这条内容</AlertTitle>
          <AlertDescription>{(entry.error as Error).message}</AlertDescription>
        </Alert>
      ) : null}

      {entry.isSuccess ? (
        <div className="grid gap-12 lg:grid-cols-[minmax(0,1fr)_15rem]">
          <article className="min-w-0 lg:max-w-[46rem]">
            <PageHeader
              eyebrow={entry.data.categoryName === null ? '知识库' : entry.data.categoryName}
              title={entry.data.title}
            />

            <MarkdownBody
              markdown={entry.data.body}
              mediaBaseUrl={API_BASE_URL}
              entryId={entry.data.id}
              entryTitle={entry.data.title}
              onAskAssistant={ask}
              onOpenEntry={openEntry}
              sandbox={sandbox}
            />

            <div className="mt-10">
              <SelfAssessmentButtons
                current={entry.data.selfAssessment}
                disabled={assess.isPending}
                onChange={(next) => assess.mutate(next)}
              />
            </div>

            <nav className="border-border mt-12 grid gap-4 border-t pt-8 sm:grid-cols-2">
              <NeighborCard side="previous" entry={entry.data.previous} testId="entry-previous" />
              <NeighborCard side="next" entry={entry.data.next} testId="entry-next" />
            </nav>

            <KnowledgeNeighborhood entry={entry.data} />

            <section className="mt-10 grid gap-6 sm:grid-cols-3">
              <RelationList title="需要先看" items={entry.data.prerequisites} testId="entry-prerequisites" />
              <RelationList title="相关内容" items={entry.data.related} testId="entry-related" />
              <RelationList title="看完这篇再看" items={entry.data.dependents} testId="entry-dependents" />
            </section>
          </article>

          {/*
            目录在右侧常驻：长文里"边读边看我在哪"比在正文上方摆一个框有用；
            小屏没有侧栏空间，此时不显示（窄屏目录价值本来也低）。
          */}
          <aside className="hidden lg:block">
            <div className="lg:sticky lg:top-20">
              <EntryToc markdown={entry.data.body} />
            </div>
          </aside>
        </div>
      ) : null}
    </AppShell>
  )
}

/** 上一篇／下一篇。两端结构相同、方向相反，合成一个组件。 */
function NeighborCard({
  side,
  entry,
  testId,
}: {
  side: 'previous' | 'next'
  entry: EntryRef | null
  testId: string
}) {
  const next = side === 'next'
  return (
    <div data-testid={testId} className={next ? 'sm:text-right' : undefined}>
      <div className="text-muted-foreground mb-2 flex items-center gap-1 text-xs">
        {next ? null : <ArrowLeft className="size-3.5" />}
        {next ? '下一篇' : '上一篇'}
        {next ? <ArrowRight className="size-3.5" /> : null}
      </div>
      {entry === null ? (
        <span className="text-muted-foreground text-sm">没有了</span>
      ) : (
        <Link
          to={`/knowledge/entries/${entry.id}`}
          className={`border-border hover:border-primary/40 hover:bg-accent/40 block rounded-lg border p-3.5 transition-all ${
            next ? 'sm:ml-auto' : ''
          }`}
        >
          <span className="text-sm font-medium">{entry.title}</span>
        </Link>
      )}
    </div>
  )
}

/** 一组关系链接。三组结构相同，只有标题与来源不同。 */
function RelationList({
  title,
  items,
  testId,
}: {
  title: string
  items: EntryRef[]
  testId: string
}) {
  return (
    <div data-testid={testId}>
      <h2 className="mb-2 text-sm font-medium">{title}</h2>
      {items.length === 0 ? (
        <p className="text-muted-foreground text-sm">暂时没有</p>
      ) : (
        <ul className="space-y-1">
          {items.map((item) => (
            <li key={item.id}>
              <Link
                to={`/knowledge/entries/${item.id}`}
                className="text-sm underline-offset-4 hover:underline"
              >
                {item.title}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
