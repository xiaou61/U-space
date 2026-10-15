import { createKnowledgeApi } from '@paideia/knowledge'
import { AppShell, Button, Card, CardContent, Input, Section, Spinner } from '@paideia/ui'
import { useAuth } from '@paideia/auth'
import { useQuery } from '@tanstack/react-query'
import { ArrowRight, BookOpen, Layers, NotebookPen, Search, Target } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'

const knowledge = createKnowledgeApi(api)

/** 摘要带着 markdown 记号，卡片上要的是"读得懂的一句话"，所以把常见记号抹掉（不渲染 md）。 */
function plainText(markdown: string): string {
  return markdown
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/[`*_>#]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
}

const QUICK_LINKS = [
  {
    to: '/knowledge',
    testId: 'home-entry-knowledge',
    icon: BookOpen,
    title: '知识库',
    description: '按分类浏览、按关键词检索，打开条目阅读。',
  },
  {
    to: '/notes',
    testId: 'home-entry-notes',
    icon: NotebookPen,
    title: '我的笔记',
    description: '随手记点什么，或让助手把答案存进来。',
  },
  {
    to: '/my-assessments',
    testId: 'home-entry-assessments',
    icon: Target,
    title: '我的自评',
    description: '你标记过「懂了／还不懂」的条目。',
  },
]

/**
 * 首页 = 仪表盘。
 *
 * <p><b>这里曾经是骨架期的"诊断页"</b>：进去先看到健康检查的原始 JSON 与平台类型，
 * 真正的去处只有下面三张卡。用户 2026-10-15 明确要求去掉那段（"运行状态什么的，这个就去掉呗"）
 * ——诊断信息的读者是开发者，不是学习者，它放在首页只会让人以为这是一套演示环境。
 * 平台标识改挂在 `<html data-platform>` 上（见 `main.tsx`），测试与排障仍有锚点。
 *
 * <p>现在首页回答三件事：**我在哪、最近有什么可读、我的学习痕迹**。三个数字都来自接口，
 * 没有一条是写死的。
 */
export function HomePage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')

  // 一次请求同时供"条目总数"与"最近更新"两块用：首页不该为两个数字打两次接口
  const entries = useQuery({
    queryKey: ['knowledge-entries', null, '', 'home'],
    queryFn: () => knowledge.entries({ size: 4 }),
  })
  const categories = useQuery({
    queryKey: ['knowledge-categories'],
    queryFn: () => knowledge.categories(),
  })
  const assessments = useQuery({
    queryKey: ['my-assessments'],
    queryFn: () => knowledge.myAssessments(),
  })

  const submitSearch = (event: FormEvent) => {
    event.preventDefault()
    const trimmed = keyword.trim()
    if (trimmed !== '') {
      navigate(`/knowledge/entries?q=${encodeURIComponent(trimmed)}`)
    }
  }

  return (
    <AppShell title="Paideia" subtitle="首页" actions={<LearnerHeader />} wide>
      {/* hero：左边是"你要做什么"，右边是"最近有什么"——右侧放真实内容而不是插画，
          一屏之内既有行动也有去处，不会只剩一大片留白 */}
      <section className="border-border from-accent via-background to-background mb-8 grid gap-8 rounded-2xl border bg-gradient-to-br p-8 lg:grid-cols-[1.1fr_1fr] lg:p-10">
        <div className="min-w-0">
          <div className="text-primary mb-3 text-xs font-medium tracking-wide">
            千人千面 · 个性化学习
          </div>
          <h1 className="text-3xl">你好，{user?.username ?? ''}</h1>
          <p className="text-muted-foreground mt-3 max-w-xl text-base leading-7">
            右下角的「助手」会读当前内容、记得你说过的话，也能把答案存成笔记。
          </p>

          <form onSubmit={submitSearch} className="mt-7 flex max-w-lg items-center gap-2">
            <div className="relative flex-1">
              <Search className="text-muted-foreground pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2" />
              <Input
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
                placeholder="搜一条内容，例如「依赖注入」"
                aria-label="搜索知识库"
                data-testid="home-search"
                className="h-11 rounded-full pl-9"
              />
            </div>
            <Button type="submit" size="lg" data-testid="home-search-submit">
              搜索
            </Button>
          </form>

          <div className="mt-6 flex flex-wrap items-center gap-2">
            <Button variant="outline" render={<Link to="/knowledge" />} data-testid="hero-open-knowledge">
              去知识库
            </Button>
            <Button variant="ghost" render={<Link to="/knowledge/entries" />}>
              看全部条目
            </Button>
          </div>
        </div>

        <Card className="overflow-hidden py-0" data-testid="home-recent">
          <div className="border-border flex items-center justify-between border-b px-5 py-3.5">
            <h2 className="text-sm font-medium">最近更新</h2>
            <Link
              to="/knowledge/entries"
              className="text-muted-foreground hover:text-foreground text-xs"
            >
              全部 →
            </Link>
          </div>
          {entries.isPending ? (
            <div className="text-muted-foreground flex items-center gap-2 p-5 text-sm">
              <Spinner />
              加载中…
            </div>
          ) : null}
          {entries.isError ? (
            <p className="text-muted-foreground p-5 text-sm">
              暂时读不到内容（{(entries.error as Error).message}）
            </p>
          ) : null}
          {entries.isSuccess ? (
            <ul className="divide-border divide-y">
              {entries.data.items.slice(0, 4).map((entry) => (
                <li key={entry.id}>
                  <Link
                    to={`/knowledge/entries/${entry.id}`}
                    className="hover:bg-accent/40 group flex items-start gap-3 px-5 py-3.5 transition-colors"
                  >
                    <span className="min-w-0 flex-1">
                      <span className="group-hover:text-primary block truncate text-sm font-medium transition-colors">
                        {entry.title}
                      </span>
                      {entry.excerpt === null || plainText(entry.excerpt) === '' ? null : (
                        <span className="text-muted-foreground mt-1 line-clamp-1 block text-xs">
                          {plainText(entry.excerpt)}
                        </span>
                      )}
                    </span>
                    <ArrowRight className="text-muted-foreground group-hover:text-primary mt-0.5 size-4 shrink-0 transition-colors" />
                  </Link>
                </li>
              ))}
              {entries.data.items.length === 0 ? (
                <li className="text-muted-foreground p-5 text-sm">知识库里还没有内容。</li>
              ) : null}
            </ul>
          ) : null}
        </Card>
      </section>

      {/* 三个数字：都来自接口 */}
      <div className="mb-12 grid gap-4 sm:grid-cols-3" data-testid="home-stats">
        <StatCard
          testId="home-stat-entries"
          label="可读条目"
          value={entries.data?.total}
          to="/knowledge/entries"
        />
        <StatCard
          testId="home-stat-categories"
          label="分类"
          value={categories.data?.length}
          to="/knowledge"
        />
        <StatCard
          testId="home-stat-assessments"
          label="我标记过"
          value={assessments.data?.length}
          to="/my-assessments"
        />
      </div>

      <Section
        title="按分类浏览"
        description="先看分类，点进去才是条目。"
        actions={
          <Button variant="ghost" size="sm" render={<Link to="/knowledge" />}>
            全部分类
          </Button>
        }
      >
        {categories.isPending ? (
          <p className="text-muted-foreground flex items-center gap-2 text-sm">
            <Spinner />
            加载分类…
          </p>
        ) : null}
        {categories.isSuccess && categories.data.length === 0 ? (
          <p className="text-muted-foreground text-sm">还没有建分类。</p>
        ) : null}
        {categories.isSuccess && categories.data.length > 0 ? (
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3" data-testid="home-categories">
            {categories.data.slice(0, 6).map((category) => (
              <Link
                key={category.id}
                to={`/knowledge/categories/${category.id}`}
                className="border-border bg-card hover:border-primary/40 hover:shadow-card group flex items-center justify-between rounded-xl border p-4 transition-all hover:-translate-y-0.5"
              >
                <span className="flex min-w-0 items-center gap-3">
                  <span className="tile-surface border-border flex size-9 shrink-0 items-center justify-center rounded-lg border">
                    <Layers className="text-primary size-4" />
                  </span>
                  <span className="min-w-0">
                    <span className="block truncate text-sm font-medium">{category.name}</span>
                    <span className="text-muted-foreground block text-xs">
                      {category.children.length === 0 ? '暂无子分类' : `${category.children.length} 个子分类`}
                    </span>
                  </span>
                </span>
                <ArrowRight className="text-muted-foreground group-hover:text-primary size-4 shrink-0 transition-colors" />
              </Link>
            ))}
          </div>
        ) : null}
      </Section>

      <Section title="快捷入口">
        <div className="grid gap-4 sm:grid-cols-3" data-testid="home-entries">
          {QUICK_LINKS.map((item) => (
            <Card key={item.to} className="hover:shadow-card transition-shadow">
              <CardContent className="pt-6">
                <span className="tile-surface border-border mb-3 flex size-10 items-center justify-center rounded-lg border">
                  <item.icon className="text-primary size-5" />
                </span>
                <Link
                  to={item.to}
                  data-testid={item.testId}
                  className="block font-medium underline-offset-4 hover:underline"
                >
                  {item.title}
                </Link>
                <p className="text-muted-foreground mt-1.5 text-sm leading-6">{item.description}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </Section>
    </AppShell>
  )
}

/** 首页上的一个数字。数据还没回来时显示占位而不是 0——0 是一个有意义的数字，不能拿它顶替"不知道"。 */
function StatCard({
  label,
  value,
  to,
  testId,
}: {
  label: string
  value: number | undefined
  to: string
  testId: string
}) {
  return (
    <Link
      to={to}
      data-testid={testId}
      className="border-border bg-card hover:border-primary/40 hover:shadow-card flex items-baseline justify-between rounded-xl border p-5 transition-all hover:-translate-y-0.5"
    >
      <span className="text-muted-foreground text-sm">{label}</span>
      <span className="text-2xl font-semibold tracking-tight">
        {value === undefined ? <Spinner className="size-4" /> : value}
      </span>
    </Link>
  )
}
