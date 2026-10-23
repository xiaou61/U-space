import { createLearningApi } from '@paideia/knowledge'
import {
  Alert,
  AlertDescription,
  AlertTitle,
  AppShell,
  Badge,
  Card,
  CardContent,
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
  PageHeader,
  Spinner,
} from '@paideia/ui'
import { useQuery } from '@tanstack/react-query'
import { CalendarClock, RotateCcw } from 'lucide-react'
import { Link } from 'react-router'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'

const learning = createLearningApi(api)

/**
 * 复习（WORK-034 S1）。
 *
 * <p>S1 只做到期列表：按到期日排，点进去回到条目。真正的「出 5 道新题」在 S2，
 * 本页不调 AI、不扣积分。
 */
export function ReviewPage() {
  const reviews = useQuery({
    queryKey: ['learning-reviews'],
    queryFn: () => learning.reviews(),
  })
  const items = reviews.data?.items ?? []

  return (
    <AppShell title="Paideia" actions={<LearnerHeader />}>
      <PageHeader
        eyebrow="复习"
        title="今天该回头看的"
        description="按到期日排好，点进去重温一遍。答错会顺延到明天，不会退回更早的档位。"
      />

      {reviews.isPending ? (
        <p className="text-muted-foreground flex items-center gap-2 text-sm" data-testid="review-state">
          <Spinner />
          加载中…
        </p>
      ) : null}

      {reviews.isError ? (
        <Alert variant="destructive" className="mt-6" data-testid="review-state">
          <AlertTitle>复习列表加载失败</AlertTitle>
          <AlertDescription>稍后重试一下，这不影响其它功能。</AlertDescription>
        </Alert>
      ) : null}

      {reviews.isSuccess && items.length === 0 ? (
        <Empty className="mt-6 border" data-testid="review-empty">
          <EmptyHeader>
            <EmptyTitle>今天没有到期的复习</EmptyTitle>
            <EmptyDescription>学完一关并通过小测后，这里会按 1 / 3 / 7 / 21 天提醒你。</EmptyDescription>
          </EmptyHeader>
          <div className="mt-4 flex flex-wrap gap-2">
            <Link
              to="/learn"
              className="border-border hover:border-primary/50 inline-flex min-h-11 items-center rounded-md border px-4 text-sm"
            >
              去从零开始
            </Link>
            <Link
              to="/knowledge"
              className="border-border hover:border-primary/50 inline-flex min-h-11 items-center rounded-md border px-4 text-sm"
            >
              去知识库
            </Link>
          </div>
        </Empty>
      ) : null}

      {items.length > 0 ? (
        <ul className="mt-6 grid gap-3" data-testid="review-list">
          {items.map((item) => (
            <li key={item.entryId}>
              <Link to={`/knowledge/entries/${item.entryId}`} data-testid="review-item">
                <Card className="hover:border-primary/40 transition-colors">
                  <CardContent className="flex flex-wrap items-center gap-3 pt-5">
                    <RotateCcw className="text-primary size-4 shrink-0" />
                    <span className="min-w-0 flex-1 font-medium">{item.title}</span>
                    {item.conceptName === null ? null : (
                      <Badge variant="secondary">{item.conceptName}</Badge>
                    )}
                    <span className="text-muted-foreground flex items-center gap-1.5 text-xs">
                      <CalendarClock className="size-3.5" />
                      第 {item.stage} 档 ·{' '}
                      {new Date(item.reviewDueAt).toLocaleDateString('zh-CN')}
                    </span>
                  </CardContent>
                </Card>
              </Link>
            </li>
          ))}
        </ul>
      ) : null}
    </AppShell>
  )
}