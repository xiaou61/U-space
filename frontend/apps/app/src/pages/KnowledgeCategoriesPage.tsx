import { createKnowledgeApi } from '@paideia/knowledge'
import { Alert, AlertDescription, AlertTitle, AppShell, Badge, Card, CardContent, PageHeader, Spinner } from '@paideia/ui'
import { useQuery } from '@tanstack/react-query'
import { ArrowRight, Layers } from 'lucide-react'
import { Link } from 'react-router'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'

const knowledge = createKnowledgeApi(api)

/**
 * 知识库首屏：**分类卡片网格**（取代原来的左侧窄树）。
 *
 * <p>原来把分类塞进一个 16rem 的侧栏里，一屏看得到七八条、每条只有名字，整页因此显得又空又单调
 * （用户反馈："分类这里就不能弄成二级页面那样吗……页面过于单调"）。改成"先看分类、点进去才是条目"
 * 之后：卡片能放下名字、子分类与条目去向，点击目标也大得多。
 *
 * <p>顶部仍然保留"全部条目"的去处——分类是导航，不是唯一入口。
 */
export function KnowledgeCategoriesPage() {
  const categories = useQuery({
    queryKey: ['knowledge-categories'],
    queryFn: () => knowledge.categories(),
  })

  return (
    <AppShell title="Paideia" subtitle="知识库" actions={<LearnerHeader />} wide>
      <PageHeader title="知识库" description="挑一个分类进去读，或直接看全部条目。" />

      <Link
        to="/knowledge/entries"
        data-testid="all-entries-card"
        className="border-border bg-card hover:border-primary/40 hover:shadow-card group mb-8 flex items-center justify-between rounded-xl border p-5 transition-all hover:-translate-y-0.5"
      >
        <span className="flex items-center gap-3">
          <span className="bg-primary text-primary-foreground flex size-10 items-center justify-center rounded-lg">
            <Layers className="size-5" />
          </span>
          <span>
            <span className="block font-medium">全部条目</span>
            <span className="text-muted-foreground block text-sm">不挑分类，按更新时间看全部内容</span>
          </span>
        </span>
        <ArrowRight className="text-muted-foreground group-hover:text-primary size-5 transition-colors" />
      </Link>

      {categories.isPending ? (
        <p className="text-muted-foreground flex items-center gap-2 text-sm" data-testid="category-state">
          <Spinner />
          加载分类…
        </p>
      ) : null}
      {categories.isError ? (
        <Alert variant="destructive" data-testid="category-state">
          <AlertTitle>分类加载失败</AlertTitle>
          <AlertDescription>{(categories.error as Error).message}</AlertDescription>
        </Alert>
      ) : null}

      {categories.isSuccess && categories.data.length === 0 ? (
        <p className="text-muted-foreground text-sm" data-testid="category-empty">
          还没有建分类。
        </p>
      ) : null}

      {categories.isSuccess && categories.data.length > 0 ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3" data-testid="category-grid">
          {categories.data.map((category, index) => (
            <Card
              key={category.id}
              // 逐个淡入：一次性铺满的网格没有任何"进入"的感觉
              className="animate-fade-up hover:border-primary/40 hover:shadow-card transition-all hover:-translate-y-0.5"
              style={{ animationDelay: `${index * 40}ms` }}
            >
              <CardContent className="p-5">
                <Link to={`/knowledge/categories/${category.id}`} data-testid={`category-card-${category.id}`}>
                  <span className="block font-medium">{category.name}</span>
                  <span className="text-muted-foreground mt-1 block text-xs">
                    {category.children.length === 0 ? '暂无子分类' : `${category.children.length} 个子分类`}
                  </span>
                  <span className="mt-3 flex flex-wrap gap-1.5">
                    {category.children.slice(0, 4).map((child) => (
                      <Badge key={child.id} variant="secondary">
                        {child.name}
                      </Badge>
                    ))}
                  </span>
                </Link>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : null}
    </AppShell>
  )
}
