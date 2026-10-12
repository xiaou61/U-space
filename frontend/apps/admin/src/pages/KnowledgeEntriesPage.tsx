import {
  MarkdownBody,
  type AdminEntryDetail,
  type AdminEntrySummary,
  type EntryInput,
} from '@paideia/knowledge'
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
  Card,
  CardContent,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  PageHeader,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Spinner,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@paideia/ui'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus, RotateCcw, Upload } from 'lucide-react'
import { useMemo, useState, type FormEvent } from 'react'

import { API_BASE_URL } from '../env'
import { ConfirmDeleteDialog, Loading, admin, categoryOptions, flatten, type FlatCategory } from '../knowledge-shared'

/** 每页条数。列表用前端分页：接口一次给一页（size=200），条目量到千级再改成服务端分页。 */
const PAGE_SIZE = 10

type StatusFilter = 'all' | 'draft' | 'published'

const STATUS_FILTER_ITEMS = [
  { value: 'all', label: '全部状态' },
  { value: 'draft', label: '仅草稿' },
  { value: 'published', label: '仅已发布' },
]

const ENTRY_STATUS_ITEMS = [
  { value: 'draft', label: '草稿' },
  { value: 'published', label: '已发布' },
]

/** 关系类型的可选项：与后端 `Relation` 的常量一一对应。 */
const RELATION_TYPES = [
  { value: 'prerequisite', label: '前置（本条目是它的前置）' },
  { value: 'related', label: '相关' },
]

/** 后端时间戳是 UTC 的 ISO 串，列表里按浏览器本地时区显示——后台是给人看的钟点，不是给机器的。 */
function formatTime(iso: string): string {
  return new Date(iso).toLocaleString('zh-CN', { hour12: false })
}

/**
 * 条目管理：录入、预览、发布与删除。
 *
 * <p>与分类管理分开成两页（理由见 `KnowledgeCategoriesPage`），但**分类树仍要在这里取**——
 * 新建条目要挂在分类上，编辑器里的分类下拉就是那两个页面共用的展开逻辑。
 *
 * <p><b>草稿预览复用学习者端的 `MarkdownBody`</b>，不另做一套渲染：预览的全部价值就是
 * "看到的就是学习者看到的"，另写一个渲染器等于让预览说谎。
 */
export function KnowledgeEntriesPage() {
  const queryClient = useQueryClient()
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all')
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(1)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [creating, setCreating] = useState(false)
  const [pendingDelete, setPendingDelete] = useState<{ id: number; name: string } | null>(null)
  const [error, setError] = useState<string | null>(null)

  const editorOpen = creating || editingId !== null

  const categories = useQuery({
    queryKey: ['admin-knowledge-categories'],
    queryFn: () => admin.categories(),
  })
  const entries = useQuery({
    queryKey: ['admin-knowledge-entries', statusFilter],
    queryFn: () => admin.entries(statusFilter === 'all' ? undefined : statusFilter),
  })
  // 关系目标要能选到**所有**条目，不能跟着表格上的状态筛选走；只有编辑器打开时才取。
  // 与首页共用同一个缓存键，所以筛选为「全部」时不会多发一次请求。
  const allEntries = useQuery({
    queryKey: ['admin-knowledge-entries', 'all'],
    queryFn: () => admin.entries(),
    enabled: editorOpen,
  })

  const flat = useMemo(() => flatten(categories.data ?? []), [categories.data])

  const report = (cause: unknown) => setError(cause instanceof Error ? cause.message : String(cause))

  const invalidate = async () => {
    await queryClient.invalidateQueries({ queryKey: ['admin-knowledge-categories'] })
    await queryClient.invalidateQueries({ queryKey: ['admin-knowledge-entries'] })
    await queryClient.invalidateQueries({ queryKey: ['admin-knowledge-entry'] })
  }

  const remove = useMutation({
    mutationFn: (id: number) => admin.deleteEntry(id),
    onSuccess: async () => {
      setPendingDelete(null)
      await invalidate()
    },
    onError: report,
  })

  const all = entries.data?.items ?? []
  const needle = keyword.trim().toLowerCase()
  const filtered = needle === '' ? all : all.filter((item) => item.title.toLowerCase().includes(needle))
  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  // 过滤后条数变少时把页码夹回范围内：夹在渲染里算，就不必再写一个"筛选变化时重置页码"的副作用
  const current = Math.min(page, totalPages)
  const rows = filtered.slice((current - 1) * PAGE_SIZE, current * PAGE_SIZE)

  return (
    <>
      <PageHeader
        title="条目管理"
        description="草稿不会出现在学习者端；发布后学习者即可浏览。"
        actions={
          <Button data-testid="admin-entry-new" onClick={() => setCreating(true)}>
            <Plus className="size-4" />
            新建条目
          </Button>
        }
      />

      {error === null ? null : (
        <Alert variant="destructive" className="mb-6" data-testid="admin-knowledge-error">
          <AlertTitle>操作失败</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
          <Button variant="ghost" size="sm" className="mt-2" onClick={() => setError(null)}>
            知道了
          </Button>
        </Alert>
      )}

      <Card>
        <CardContent className="px-0">
          <div className="flex flex-wrap items-center gap-2 px-(--card-spacing) pb-3">
            <Select
              items={STATUS_FILTER_ITEMS}
              value={statusFilter}
              onValueChange={(value) => setStatusFilter(value as StatusFilter)}
            >
              <SelectTrigger size="sm" className="w-32" data-testid="admin-entry-status-filter">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {STATUS_FILTER_ITEMS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Input
              className="h-7 w-56"
              placeholder="按标题筛选"
              value={keyword}
              onChange={(event) => setKeyword(event.target.value)}
              data-testid="admin-entry-keyword"
            />

            <Button
              variant="ghost"
              size="sm"
              disabled={keyword === '' && statusFilter === 'all'}
              onClick={() => {
                setKeyword('')
                setStatusFilter('all')
              }}
              data-testid="admin-entry-reset"
            >
              <RotateCcw className="size-4" />
              重置
            </Button>
          </div>

          {entries.isPending ? <Loading label="加载条目…" /> : null}

          {entries.isError ? (
            <div className="px-(--card-spacing) pb-3">
              <Alert variant="destructive">
                <AlertTitle>条目加载失败</AlertTitle>
                <AlertDescription>{(entries.error as Error).message}</AlertDescription>
              </Alert>
            </div>
          ) : null}

          <Table data-testid="admin-entry-list">
            <TableHeader>
              <TableRow>
                <TableHead className="w-20 pl-(--card-spacing)">ID</TableHead>
                <TableHead>标题</TableHead>
                <TableHead className="w-40">分类</TableHead>
                <TableHead className="w-24">状态</TableHead>
                <TableHead className="w-40">更新时间</TableHead>
                <TableHead className="w-32 pr-(--card-spacing) text-right">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.length === 0 && !entries.isPending ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-muted-foreground h-24 text-center" data-testid="admin-entry-empty">
                    {all.length === 0 ? '还没有条目' : '没有符合条件的条目'}
                  </TableCell>
                </TableRow>
              ) : null}
              {rows.map((entry) => (
                <TableRow key={entry.id} data-testid={`admin-entry-row-${entry.id}`}>
                  <TableCell className="text-muted-foreground pl-(--card-spacing) tabular-nums">
                    {entry.id}
                  </TableCell>
                  <TableCell className="max-w-0 truncate font-medium" title={entry.title}>
                    {entry.title}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {entry.categoryName ?? '未分类'}
                  </TableCell>
                  <TableCell>
                    <Badge variant={entry.status === 'published' ? 'outline' : 'secondary'}>
                      {entry.status === 'published' ? '已发布' : '草稿'}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-xs tabular-nums">
                    {formatTime(entry.updatedAt)}
                  </TableCell>
                  <TableCell className="pr-(--card-spacing) text-right">
                    <Button
                      variant="ghost"
                      size="xs"
                      data-testid={`admin-entry-edit-${entry.id}`}
                      onClick={() => setEditingId(entry.id)}
                    >
                      编辑
                    </Button>
                    <Button
                      variant="ghost"
                      size="xs"
                      className="text-destructive hover:text-destructive"
                      data-testid={`admin-entry-delete-${entry.id}`}
                      onClick={() => setPendingDelete({ id: entry.id, name: entry.title })}
                    >
                      删除
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          <Pagination total={filtered.length} current={current} totalPages={totalPages} onChange={setPage} />
        </CardContent>
      </Card>

      <EntryEditor
        open={editorOpen}
        entryId={editingId}
        categories={flat}
        entries={allEntries.data?.items ?? []}
        onClose={() => {
          setCreating(false)
          setEditingId(null)
        }}
        onSaved={invalidate}
        onError={report}
      />

      <ConfirmDeleteDialog
        open={pendingDelete !== null}
        label="条目"
        name={pendingDelete?.name ?? ''}
        busy={remove.isPending}
        onCancel={() => setPendingDelete(null)}
        onConfirm={() => {
          if (pendingDelete !== null) {
            remove.mutate(pendingDelete.id)
          }
        }}
      />
    </>
  )
}

function Pagination({
  total,
  current,
  totalPages,
  onChange,
}: {
  total: number
  current: number
  totalPages: number
  onChange: (page: number) => void
}) {
  return (
    <div className="border-border text-muted-foreground flex items-center justify-between gap-4 border-t px-(--card-spacing) py-3 text-xs">
      <span data-testid="admin-entry-total">共 {total} 条</span>
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="xs"
          disabled={current <= 1}
          onClick={() => onChange(current - 1)}
          data-testid="admin-entry-prev"
        >
          上一页
        </Button>
        <span className="tabular-nums">
          {current} / {totalPages}
        </span>
        <Button
          variant="outline"
          size="xs"
          disabled={current >= totalPages}
          onClick={() => onChange(current + 1)}
          data-testid="admin-entry-next"
        >
          下一页
        </Button>
      </div>
    </div>
  )
}

function EntryEditor({
  open,
  entryId,
  categories,
  entries,
  onClose,
  onSaved,
  onError,
}: {
  open: boolean
  entryId: number | null
  categories: FlatCategory[]
  entries: AdminEntrySummary[]
  onClose: () => void
  onSaved: () => Promise<void>
  onError: (cause: unknown) => void
}) {
  const detail = useQuery({
    queryKey: ['admin-knowledge-entry', entryId],
    queryFn: () => admin.entry(entryId as number),
    enabled: open && entryId !== null,
  })

  if (!open) {
    return null
  }
  if (entryId !== null && detail.isPending) {
    return (
      <Dialog open onOpenChange={() => onClose()}>
        <DialogContent>
          <DialogTitle>加载条目…</DialogTitle>
          <p className="text-muted-foreground flex items-center gap-2 text-sm">
            <Spinner />
            正在读取
          </p>
        </DialogContent>
      </Dialog>
    )
  }

  return (
    <EntryForm
      // 换条目时用 key 强制重建，免得把上一条的内容留在表单里
      key={entryId ?? 'new'}
      detail={entryId === null ? null : (detail.data ?? null)}
      categories={categories}
      entries={entries}
      onClose={onClose}
      onSaved={onSaved}
      onError={onError}
    />
  )
}

function EntryForm({
  detail,
  categories,
  entries,
  onClose,
  onSaved,
  onError,
}: {
  detail: AdminEntryDetail | null
  categories: FlatCategory[]
  entries: AdminEntrySummary[]
  onClose: () => void
  onSaved: () => Promise<void>
  onError: (cause: unknown) => void
}) {
  const [title, setTitle] = useState(detail?.title ?? '')
  const [body, setBody] = useState(detail?.body ?? '')
  const [categoryId, setCategoryId] = useState<number | null>(detail?.categoryId ?? null)
  const [status, setStatus] = useState<'draft' | 'published'>(
    detail?.status === 'published' ? 'published' : 'draft',
  )
  const [preview, setPreview] = useState(false)
  const [relationTarget, setRelationTarget] = useState<number | null>(null)
  const [relationType, setRelationType] = useState('prerequisite')

  const categoryItems = categoryOptions(categories, '（未分类）')
  // 关系目标不能是自己（后端也会拒，但下拉里就不该出现这个选项）
  const targetItems = entries
    .filter((entry) => entry.id !== detail?.id)
    .map((entry) => ({ value: entry.id as number | null, label: `#${entry.id} ${entry.title}` }))

  const save = useMutation({
    mutationFn: async () => {
      const input: EntryInput = { title: title.trim(), body, categoryId, status }
      if (detail === null) {
        return admin.createEntry(input)
      }
      await admin.updateEntry(detail.id, input)
      return detail.id
    },
    onSuccess: async () => {
      await onSaved()
      onClose()
    },
    onError,
  })

  const addRelation = useMutation({
    mutationFn: () => admin.createRelation(detail?.id as number, relationTarget as number, relationType),
    onSuccess: async () => {
      setRelationTarget(null)
      await onSaved()
    },
    onError,
  })

  const removeRelation = useMutation({
    mutationFn: (id: number) => admin.deleteRelation(id),
    onSuccess: onSaved,
    onError,
  })

  const upload = useMutation({
    mutationFn: (file: File) => admin.uploadFile(file),
    onSuccess: (uploaded) => {
      // 上传与保存是两件事：附件本身不依赖条目存在，所以先把引用插进正文，再随保存一起提交。
      // 图注取原文件名——正文里的 alt 直接决定图注与可访问名称，留空等于浪费一次表达机会。
      setBody((current) => {
        const separator = current === '' || current.endsWith('\n') ? '' : '\n'
        return `${current}${separator}\n![${uploaded.originalName}](${uploaded.url})\n`
      })
    },
    onError,
  })

  const onSubmit = (event: FormEvent) => {
    event.preventDefault()
    save.mutate()
  }

  return (
    <Dialog open onOpenChange={() => onClose()}>
      <DialogContent className="max-h-[88vh] overflow-y-auto sm:max-w-3xl" data-testid="admin-entry-editor">
        <DialogHeader>
          <DialogTitle>{detail === null ? '新建条目' : `编辑条目 #${detail.id}`}</DialogTitle>
        </DialogHeader>

        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <Label htmlFor="admin-entry-title">标题</Label>
            <Input
              id="admin-entry-title"
              className="mt-1"
              value={title}
              onChange={(event) => setTitle(event.target.value)}
              data-testid="admin-entry-title"
            />
          </div>

          <div className="flex flex-wrap gap-4">
            <div>
              <Label htmlFor="admin-entry-category">分类</Label>
              <Select items={categoryItems} value={categoryId} onValueChange={(value) => setCategoryId(value)}>
                <SelectTrigger id="admin-entry-category" className="mt-1 w-56" data-testid="admin-entry-category">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {categoryItems.map((option) => (
                    <SelectItem key={option.value ?? 'none'} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="admin-entry-status">状态</Label>
              <Select
                items={ENTRY_STATUS_ITEMS}
                value={status}
                onValueChange={(value) => setStatus(value as 'draft' | 'published')}
              >
                <SelectTrigger id="admin-entry-status" className="mt-1 w-32" data-testid="admin-entry-status">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {ENTRY_STATUS_ITEMS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between">
              <Label htmlFor="admin-entry-body">正文（markdown）</Label>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                data-testid="admin-entry-preview-toggle"
                onClick={() => setPreview((current) => !current)}
              >
                {preview ? '回到编辑' : '预览'}
              </Button>
            </div>
            {preview ? (
              // 预览与学习者端用同一个渲染组件：这样"预览"才等于"学习者看到的"
              <div className="border-border mt-1 rounded-md border p-4" data-testid="admin-entry-preview">
                <MarkdownBody markdown={body} mediaBaseUrl={API_BASE_URL} />
              </div>
            ) : (
              <textarea
                id="admin-entry-body"
                className="border-input bg-background mt-1 block h-64 w-full rounded-md border p-3 font-mono text-xs"
                value={body}
                onChange={(event) => setBody(event.target.value)}
                data-testid="admin-entry-body"
              />
            )}

            <div className="mt-2">
              {/* 原生 <input type="file"> 会渲染出英文的"Choose File"，这是这个弹窗里最扎眼的
                  一块：用 label 当按钮、把 input 藏起来，外观与其它按钮一致。 */}
              <label className="border-border bg-background hover:bg-muted inline-flex h-7 cursor-pointer items-center gap-1 rounded-[min(var(--radius-md),12px)] border px-2.5 text-[0.8rem] font-medium">
                <Upload className="size-3.5" />
                上传附件
                <input
                  type="file"
                  className="sr-only"
                  accept="image/png,image/jpeg,image/gif,image/webp,application/pdf"
                  aria-label="上传附件"
                  data-testid="admin-entry-upload"
                  onChange={(event) => {
                    const file = event.target.files?.[0]
                    // 清空 input 的值，否则连续选同一个文件不会触发 change
                    event.target.value = ''
                    if (file !== undefined) {
                      upload.mutate(file)
                    }
                  }}
                />
              </label>
              <p className="text-muted-foreground mt-1 text-xs">
                {upload.isPending
                  ? '上传中…'
                  : '允许 png / jpeg / gif / webp / pdf，单个不超过 10 MB；上传后会把引用插到正文末尾。'}
              </p>
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="ghost" onClick={onClose}>
              取消
            </Button>
            <Button
              type="submit"
              disabled={title.trim() === '' || save.isPending}
              data-testid="admin-entry-save"
            >
              {status === 'published' ? '保存并发布' : '保存草稿'}
            </Button>
          </DialogFooter>
        </form>

        {detail === null ? null : (
          <div className="border-border mt-2 border-t pt-4" data-testid="admin-entry-relations">
            <h3 className="mb-2 text-sm font-medium">关系</h3>
            {detail.relations.length === 0 ? (
              <p className="text-muted-foreground text-sm">还没有关系</p>
            ) : (
              <ul className="mb-3 space-y-1">
                {detail.relations.map((relation) => (
                  <li key={relation.id} className="flex items-center gap-2 text-sm">
                    <span>
                      {relation.relationType === 'prerequisite' ? '前置' : '相关'}：
                      {relation.fromEntryId === detail.id
                        ? `本条目 → 条目 ${relation.toEntryId}`
                        : `条目 ${relation.fromEntryId} → 本条目`}
                    </span>
                    <Button
                      variant="ghost"
                      size="xs"
                      className="text-destructive hover:text-destructive"
                      onClick={() => removeRelation.mutate(relation.id)}
                      data-testid={`admin-relation-delete-${relation.id}`}
                    >
                      删除
                    </Button>
                  </li>
                ))}
              </ul>
            )}

            <div className="flex flex-wrap items-end gap-2">
              <div>
                <Label htmlFor="admin-relation-target">目标条目</Label>
                <Select
                  items={targetItems}
                  value={relationTarget}
                  onValueChange={(value) => setRelationTarget(value)}
                >
                  <SelectTrigger
                    id="admin-relation-target"
                    className="mt-1 w-72"
                    data-testid="admin-relation-target"
                  >
                    <SelectValue placeholder="选择目标条目" />
                  </SelectTrigger>
                  <SelectContent>
                    {targetItems.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="admin-relation-type">类型</Label>
                <Select
                  items={RELATION_TYPES}
                  value={relationType}
                  onValueChange={(value) => setRelationType(value ?? 'prerequisite')}
                >
                  <SelectTrigger id="admin-relation-type" className="mt-1 w-52" data-testid="admin-relation-type">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {RELATION_TYPES.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <Button
                type="button"
                variant="outline"
                disabled={relationTarget === null || addRelation.isPending}
                onClick={() => addRelation.mutate()}
                data-testid="admin-relation-add"
              >
                建立关系
              </Button>
            </div>
            {targetItems.length === 0 ? (
              <p className="text-muted-foreground mt-2 text-xs">还没有其它条目可以关联。</p>
            ) : null}
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
