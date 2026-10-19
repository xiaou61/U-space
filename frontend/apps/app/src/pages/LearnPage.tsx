import { createLearningApi } from '@paideia/knowledge'
import type { LearningLine } from '@paideia/knowledge'
import {
  Alert,
  AlertDescription,
  AlertTitle,
  AppShell,
  Button,
  PageHeader,
  Section,
  Spinner,
} from '@paideia/ui'
import { useQuery } from '@tanstack/react-query'
import { ArrowRight, BookOpenCheck, BookMarked, RotateCcw } from 'lucide-react'
import type { ReactNode } from 'react'
import { Link } from 'react-router'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'

const learning = createLearningApi(api)

/**
 * 学习首页（WORK-020）。
 *
 * <p><b>这一页没有任何课名是写死的</b>：它只遍历 `GET /learning/lines` 返回的线，按 `kind` 分组。
 * 加一门语言、加一个专题，都是后端改一条数据，这里自动出现——这是本项"可扩展性"的可见证据。
 *
 * <p>分组键是 `kind` 而不是分类 id：`course` 是"从零学"，`reading` 是"读别人的代码"。
 * 将来多一种呈现形态（比如练习册）才需要在这里加一个分组——那是一次有意识的代码改动，
 * 不是每加一门课都改。
 */
export function LearnPage() {
  const lines = useQuery({
    queryKey: ['learning-lines'],
    queryFn: () => learning.lines(),
  })

  const courses = lines.data?.lines.filter((line) => line.kind === 'course') ?? []
  const readings = lines.data?.lines.filter((line) => line.kind === 'reading') ?? []

  return (
    <AppShell title="Paideia" actions={<LearnerHeader />} wide>
      <PageHeader
        eyebrow="从零开始"
        title="挑一条线，一课一课往下走"
        description="每课二十分钟左右：一个能跑的示例、一个动手题、三道小测。做完会记录通过，隔天提醒复习。"
      />

      {lines.isPending ? (
        <p className="text-muted-foreground flex items-center gap-2 text-sm" data-testid="learn-state">
          <Spinner />
          加载学习线…
        </p>
      ) : null}

      {lines.isError ? (
        <Alert variant="destructive" data-testid="learn-state">
          <AlertTitle>学习线加载失败</AlertTitle>
          <AlertDescription>{(lines.error as Error).message}</AlertDescription>
        </Alert>
      ) : null}

      {lines.isSuccess && lines.data.lines.length === 0 ? (
        <Alert data-testid="learn-empty">
          <AlertTitle>还没有学习线</AlertTitle>
          <AlertDescription>
            学习线由管理员在知识库里建分类并标记为课程（line_kind）后出现。
          </AlertDescription>
        </Alert>
      ) : null}

      {courses.length > 0 ? (
        <div data-testid="learn-courses">
        <Section title="从零开始学">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {courses.map((line) => (
              <LineCard key={line.categoryId} line={line} icon={<BookOpenCheck className="size-5" />} />
            ))}
          </div>
        </Section>
        </div>
      ) : null}

      {readings.length > 0 ? (
        <div data-testid="learn-readings">
        <Section title="读源码与专题">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {readings.map((line) => (
              <LineCard key={line.categoryId} line={line} icon={<BookMarked className="size-5" />} />
            ))}
          </div>
        </Section>
        </div>
      ) : null}
    </AppShell>
  )
}

/**
 * 一张线的卡。进度、下一课、到期复习都来自服务端算好的字段，前端不自己排序。
 *
 * <p>`min-w-0` 不能删：卡片在 grid 里默认按 min-content 撑开，而"下一站：…"那行是
 * `truncate`（= nowrap）。少了它，长标题会把卡片顶得比视口还宽——360px 实测溢出 55px。
 */
function LineCard({ line, icon }: { line: LearningLine; icon: ReactNode }) {
  const percent = line.stationCount === 0 ? 0 : Math.round((line.passedCount / line.stationCount) * 100)
  const href = line.nextEntry === null ? `#/knowledge` : `#/knowledge/entries/${line.nextEntry.entryId}`

  return (
    <article
      data-testid={`learn-line-${line.slug}`}
      data-passed={line.passedCount}
      data-stations={line.stationCount}
      className="border-border bg-card hover:border-primary/40 flex min-w-0 flex-col rounded-xl border p-5 transition-colors"
    >
      <div className="flex items-start gap-3">
        <span className="bg-primary/10 text-primary flex size-10 shrink-0 items-center justify-center rounded-lg">
          {icon}
        </span>
        <div className="min-w-0 flex-1">
          <h3 className="text-base font-medium">{line.name}</h3>
          <p className="text-muted-foreground mt-0.5 text-xs">
            {line.stationCount} 站 · 已通过 {line.passedCount}
          </p>
        </div>
      </div>

      <div
        className="bg-muted mt-4 h-1.5 w-full overflow-hidden rounded-full"
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={line.stationCount}
        aria-valuenow={line.passedCount}
        aria-label={`${line.name} 进度`}
      >
        <div className="bg-primary h-full rounded-full transition-all" style={{ width: `${percent}%` }} />
      </div>

      {line.dueReviewCount > 0 ? (
        <p className="text-muted-foreground mt-3 flex items-center gap-1.5 text-xs">
          <RotateCcw className="size-3.5" />
          今天该复习 {line.dueReviewCount} 站
        </p>
      ) : null}

      <div className="mt-4 flex items-center justify-between gap-2">
        <span className="text-muted-foreground min-w-0 truncate text-sm">
          {line.nextEntry === null ? '全部通过' : `下一站：${line.nextEntry.title}`}
        </span>
        <Button
          size="sm"
          variant={line.passedCount === 0 ? 'default' : 'outline'}
          render={<Link to={href.replace('#', '')} />}
        >
          {line.passedCount === 0 ? '开始' : line.nextEntry === null ? '回看' : '继续'}
          <ArrowRight className="size-4" />
        </Button>
      </div>
    </article>
  )
}
