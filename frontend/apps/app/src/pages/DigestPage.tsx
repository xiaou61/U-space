import { createDigestApi, DIGEST_GROUPS, type DigestItem } from '@paideia/knowledge'
import {
  Alert,
  AlertDescription,
  AlertTitle,
  AppShell,
  Badge,
  Button,
  Card,
  CardContent,
  PageHeader,
  Spinner,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@paideia/ui'
import { useQuery } from '@tanstack/react-query'
import { ExternalLink, NotebookPen } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'

const digest = createDigestApi(api)

/**
 * 每日技术速递（WORK-027）。
 *
 * <p>一屏看完「今天值得知道的事」：四个固定分组（前沿新闻 / GitHub 热榜 / 社区热帖 / HN），
 * 每条一句话摘要 + 来源 + 原文链接。
 *
 * <p><b>不做按身份分组</b>：该需求已在 2026-10-21 撤销——后端只有 `job_seeker` 一个可选身份，
 * "切换身份看到不同源"这个动作不存在。分组顺序写死：news → github → community → hackernews。
 *
 * <p><b>降级态显示原文标题而不是空摘要</b>：AI 失败那天条目仍然入库（标题+链接本身有价值），
 * 界面必须让人看出"这条没摘要"，而不是显示一片空白让人以为坏了。
 */
export function DigestPage() {
  const [date, setDate] = useState<string | undefined>(undefined)

  const dates = useQuery({
    queryKey: ['digest-dates'],
    queryFn: () => digest.dates(30),
  })

  const today = useQuery({
    queryKey: ['digest', date ?? 'today'],
    queryFn: () => digest.byDate(date),
  })

  const weekly = useQuery({
    queryKey: ['digest-weekly'],
    queryFn: () => digest.weekly(),
  })

  const total = DIGEST_GROUPS.reduce((sum, group) => sum + (today.data?.[group.key]?.length ?? 0), 0)

  return (
    <AppShell title="Paideia" actions={<LearnerHeader />} wide>
      <PageHeader
        eyebrow="每日速递"
        title="今天值得知道的事"
        description="每天一次，把前沿新闻、GitHub 热榜、社区热帖与 Hacker News 收在一屏。每条可一键存成笔记。"
      />

      <Tabs defaultValue="daily">
        <TabsList>
          <TabsTrigger value="daily" data-testid="digest-tab-daily">
            日报
          </TabsTrigger>
          <TabsTrigger value="weekly" data-testid="digest-tab-weekly">
            周报
          </TabsTrigger>
        </TabsList>

        <TabsContent value="daily">
          <div className="mt-4 flex flex-wrap items-center gap-2" data-testid="digest-date-picker">
            <Button
              variant={date === undefined ? 'default' : 'outline'}
              size="sm"
              onClick={() => setDate(undefined)}
            >
              今天
            </Button>
            {(dates.data ?? []).slice(0, 7).map((available) => (
              <Button
                key={available}
                variant={date === available ? 'default' : 'outline'}
                size="sm"
                onClick={() => setDate(available)}
                data-testid="digest-date-option"
              >
                {available}
              </Button>
            ))}
          </div>

          {today.isPending ? (
            <div className="mt-8 flex justify-center">
              <Spinner />
            </div>
          ) : today.isError ? (
            <Alert variant="destructive" className="mt-6">
              <AlertTitle>速递加载失败</AlertTitle>
              <AlertDescription>稍后重试一下。这不会影响其它功能。</AlertDescription>
            </Alert>
          ) : total === 0 ? (
            <Alert className="mt-6" data-testid="digest-empty">
              <AlertTitle>这一天还没有速递</AlertTitle>
              <AlertDescription>
                采集每天自动跑一次。如果刚部署，第一次内容会在启动几分钟后出现。
              </AlertDescription>
            </Alert>
          ) : (
            <div className="mt-6 space-y-8">
              {DIGEST_GROUPS.map((group) => {
                const items = today.data?.[group.key] ?? []
                if (items.length === 0) {
                  return null
                }
                return (
                  <section key={group.key} data-testid={`digest-group-${group.key}`}>
                    <h2 className="mb-3 flex items-center gap-2 text-lg font-medium">
                      {group.title}
                      <Badge variant="secondary">{items.length}</Badge>
                    </h2>
                    <div className="grid gap-3 md:grid-cols-2">
                      {items.map((item) => (
                        <DigestCard key={item.id} item={item} />
                      ))}
                    </div>
                  </section>
                )
              })}
            </div>
          )}
        </TabsContent>

        <TabsContent value="weekly">
          {weekly.isPending ? (
            <div className="mt-8 flex justify-center">
              <Spinner />
            </div>
          ) : weekly.data === null || weekly.data === undefined ? (
            <Alert className="mt-6" data-testid="digest-weekly-missing">
              <AlertTitle>本周周报尚未生成</AlertTitle>
              <AlertDescription>周报在每周初从上周的日报聚合，一次 AI 调用写成。</AlertDescription>
            </Alert>
          ) : (
            <Card className="mt-6">
              <CardContent className="pt-6">
                <p className="mb-3 text-sm text-muted-foreground">
                  {weekly.data.weekStart} 那一周 · 基于 {weekly.data.itemCount} 条速递
                </p>
                <div className="whitespace-pre-wrap leading-relaxed">{weekly.data.body}</div>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </AppShell>
  )
}

/** 一条速递卡片。降级态用灰字标出"没摘要"，不显示空白。 */
function DigestCard({ item }: { item: DigestItem }) {
  const degraded = item.summaryState === 'degraded'
  // 「存成笔记」是纯前端跳转（I-16）：把标题与摘要预填进新建笔记页，用户可改再存
  const noteLink = `/notes/new?title=${encodeURIComponent(item.title)}&content=${encodeURIComponent(
    (item.summary ?? '') + '\n\n> 来源：' + item.url,
  )}`

  return (
    <Card data-testid="digest-item" className="flex flex-col">
      <CardContent className="flex flex-1 flex-col gap-2 pt-5">
        <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
          <Badge variant="outline">{item.sourceName}</Badge>
          {item.heat !== null ? <span data-testid="digest-heat">{item.heat}</span> : null}
          {item.meta !== null ? <span>{item.meta}</span> : null}
        </div>
        <a
          href={item.url}
          target="_blank"
          rel="noreferrer"
          className="font-medium leading-snug hover:underline"
          data-testid="digest-title"
        >
          {item.title}
          <ExternalLink className="ml-1 inline size-3 align-baseline opacity-60" />
        </a>
        {degraded ? (
          <p className="text-sm text-muted-foreground" data-testid="digest-degraded">
            这条还没写好摘要
          </p>
        ) : (
          <p className="text-sm leading-relaxed text-muted-foreground" data-testid="digest-summary">
            {item.summary}
          </p>
        )}
        <div className="mt-auto pt-1">
          <Button variant="ghost" size="sm" render={<Link to={noteLink} data-testid="digest-to-note" />}>
            <NotebookPen className="mr-1 size-4" />
            存成笔记
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
