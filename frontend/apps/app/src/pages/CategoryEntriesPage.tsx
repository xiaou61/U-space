import { createKnowledgeApi } from '@paideia/knowledge'
import {
  Alert,
  AlertDescription,
  AlertTitle,
  AppShell,
  Badge,
  Button,
  Card,
  CardContent,
  Input,
  PageHeader,
  Spinner,
} from '@paideia/ui'
import { useQuery } from '@tanstack/react-query'
import { ArrowRight, ChevronLeft } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'

const knowledge = createKnowledgeApi(api)

/**
 * 某个分类（或全部）下的条目列表——分类那一层的**二级页**。
 *
 * <p>同一个组件服务两条路由：`/knowledge/categories/:categoryId`（某个分类）与 `/knowledge/entries`
 * （全部）。差别只有 `categoryId` 与标题，合成一个组件比复制两份真实。
 *
 * <p><b>检索是"回车/点按钮"而不是输入即搜</b>：中文输入法在拼字过程中会连续触发 change 事件，
 * 按字数防抖会把"并发"这种词的中间态（拼音或半截字）也发出去。提交式检索没有这个问题。
 */
export function CategoryEntriesPage() {
  const { categoryId } = useParams<{ categoryId: string }>()
  const scopedId = categoryId === undefined ? null : Number(categoryId)
  const [draft, setDraft] = useState('')
  const [keyword, setKeyword] = useState('')

  const categories = useQuery({
    queryKey: ['knowledge-categories'],
    queryFn: () => knowledge.categories(),
  })

  const entries = useQuery({
    queryKey: ['knowledge-entries', scopedId, keyword],
    queryFn: () => knowledge.entries({ categoryId: scopedId, q: keyword, size: 50 }),
  })

  // 标题用当前分类名；分类还没加载出来时退回一句通用的
  const current = categories.data?.find((node) => node.id === scopedId)
  const title = scopedId === null ? '全部条目' : (current?.name ?? '分类')

  const onSubmit = (event: FormEvent) => {
    event.preventDefault()
    setKeyword(draft.trim())
  }

  return (
    <AppShell title="Paideia" subtitle="知识库" actions={<LearnerHeader />} wide>
      <Link
        to="/knowledge"
        className="text-muted-foreground hover:text-foreground mb-4 inline-flex items-center gap-1 text-sm transition-colors"
        data-testid="back-to-categories"
      >
        <ChevronLeft className="size-4" />
        所有分类
      </Link>

      <PageHeader title={title} description={scopedId === null ? '按更新时间排列。' : '这个分类下的条目。'} />

      <form onSubmit={onSubmit} className="mb-6 flex items-center gap-2">
        <Input
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
          placeholder="搜索标题或正文"
          aria-label="搜索知识条目"
          data-testid="knowledge-search"
          className="max-w-md"
        />
        <Button type="submit" data-testid="knowledge-search-submit">
          搜索
        </Button>
        {keyword === '' ? null : (
          <Button
            type="button"
            variant="ghost"
            data-testid="knowledge-search-clear"
            onClick={() => {
              setDraft('')
              setKeyword('')
            }}
          >
            清除
          </Button>
        )}
      </form>

      {entries.isPending ? (
        <p className="text-muted-foreground flex items-center gap-2 text-sm" data-testid="entry-state">
          <Spinner />
          加载条目…
        </p>
      ) : null}
      {entries.isError ? (
        <Alert variant="destructive" data-testid="entry-state">
          <AlertTitle>条目加载失败</AlertTitle>
          <AlertDescription>{(entries.error as Error).message}</AlertDescription>
        </Alert>
      ) : null}
      {entries.isSuccess && entries.data.items.length === 0 ? (
        <p className="text-muted-foreground text-sm" data-testid="entry-empty">
          没有符合条件的条目
        </p>
      ) : null}

      {entries.isSuccess && entries.data.items.length > 0 ? (
        <div className="grid gap-4 md:grid-cols-2" data-testid="entry-list">
          {entries.data.items.map((item, index) => (
            <Card
              key={item.id}
              className="animate-fade-up hover:border-primary/40 hover:shadow-card transition-all hover:-translate-y-0.5"
              style={{ animationDelay: `${index * 30}ms` }}
            >
              <CardContent className="p-5">
                <Link
                  to={`/knowledge/entries/${item.id}`}
                  data-testid={`entry-link-${item.id}`}
                  className="group flex h-full flex-col justify-between gap-4"
                >
                  <span className="text-base font-medium leading-snug">{item.title}</span>
                  <span className="flex items-center gap-3">
                    {item.categoryName === null ? null : <Badge variant="secondary">{item.categoryName}</Badge>}
                    <span className="text-muted-foreground text-xs">
                      {new Date(item.updatedAt).toLocaleDateString('zh-CN')}
                    </span>
                    <ArrowRight className="text-muted-foreground group-hover:text-primary ml-auto size-4 transition-colors" />
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
