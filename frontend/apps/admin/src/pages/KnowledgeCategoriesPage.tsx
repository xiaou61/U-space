import {
  Alert,
  AlertDescription,
  AlertTitle,
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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@paideia/ui'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { useMemo, useState, type FormEvent } from 'react'

import { ConfirmDeleteDialog, Loading, admin, categoryOptions, flatten } from '../knowledge-shared'

/**
 * 分类管理。
 *
 * <p>与条目管理分开成两页：分类是"搭架子"，改一次用很久；条目是天天写的。挤在一页时
 * 上面的分类表会把条目推到折线以下，条目一多就要先滚过一整块用不到的表格。
 *
 * <p>拆开界面不等于拆开数据——新建条目时仍要在这棵树上选，所以两个页面共用
 * `knowledge-shared` 里的展开与下拉逻辑。
 */
export function KnowledgeCategoriesPage() {
  const queryClient = useQueryClient()
  const [creating, setCreating] = useState(false)
  const [pendingDelete, setPendingDelete] = useState<{ id: number; name: string } | null>(null)
  const [error, setError] = useState<string | null>(null)

  const categories = useQuery({
    queryKey: ['admin-knowledge-categories'],
    queryFn: () => admin.categories(),
  })
  const flat = useMemo(() => flatten(categories.data ?? []), [categories.data])

  const report = (cause: unknown) => setError(cause instanceof Error ? cause.message : String(cause))

  // 分类变了，条目列表上的「分类」列与编辑器里的下拉都跟着变，所以一起失效
  const invalidate = async () => {
    await queryClient.invalidateQueries({ queryKey: ['admin-knowledge-categories'] })
    await queryClient.invalidateQueries({ queryKey: ['admin-knowledge-entries'] })
    await queryClient.invalidateQueries({ queryKey: ['admin-knowledge-entry'] })
  }

  const remove = useMutation({
    mutationFn: (id: number) => admin.deleteCategory(id),
    onSuccess: async () => {
      setPendingDelete(null)
      await invalidate()
    },
    onError: report,
  })

  return (
    <>
      <PageHeader
        title="分类管理"
        description="层级树；删除前必须先移走它的子分类与条目。"
        actions={
          <Button data-testid="admin-category-new" onClick={() => setCreating(true)}>
            <Plus className="size-4" />
            新建分类
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
          {categories.isPending ? <Loading label="加载分类…" /> : null}
          {categories.isError ? (
            <div className="px-(--card-spacing) pb-3">
              <Alert variant="destructive">
                <AlertTitle>分类加载失败</AlertTitle>
                <AlertDescription>{(categories.error as Error).message}</AlertDescription>
              </Alert>
            </div>
          ) : null}

          <Table data-testid="admin-category-list">
            <TableHeader>
              <TableRow>
                <TableHead className="w-20 pl-(--card-spacing)">ID</TableHead>
                <TableHead>名称</TableHead>
                <TableHead className="w-40">上级</TableHead>
                <TableHead className="w-56">标识</TableHead>
                <TableHead className="w-24 pr-(--card-spacing) text-right">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {flat.length === 0 && !categories.isPending ? (
                <TableRow>
                  <TableCell colSpan={5} className="text-muted-foreground h-20 text-center" data-testid="admin-category-empty">
                    还没有分类
                  </TableCell>
                </TableRow>
              ) : null}
              {flat.map((category) => (
                <TableRow key={category.id}>
                  <TableCell className="text-muted-foreground pl-(--card-spacing) tabular-nums">
                    {category.id}
                  </TableCell>
                  <TableCell style={{ paddingLeft: `${16 + category.depth * 18}px` }}>
                    {category.name}
                  </TableCell>
                  <TableCell className="text-muted-foreground">{category.parentName ?? '—'}</TableCell>
                  <TableCell className="text-muted-foreground font-mono text-xs">{category.slug}</TableCell>
                  <TableCell className="pr-(--card-spacing) text-right">
                    <Button
                      variant="ghost"
                      size="xs"
                      data-testid={`admin-category-delete-${category.id}`}
                      onClick={() => setPendingDelete({ id: category.id, name: category.name })}
                    >
                      删除
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <CreateCategoryDialog
        open={creating}
        categories={flat}
        onClose={() => setCreating(false)}
        onCreated={invalidate}
        onError={report}
      />

      <ConfirmDeleteDialog
        open={pendingDelete !== null}
        label="分类"
        name={pendingDelete?.name ?? ''}
        hint="该分类下还有子分类或条目时会被拒绝。"
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

function CreateCategoryDialog({
  open,
  categories,
  onClose,
  onCreated,
  onError,
}: {
  open: boolean
  categories: ReturnType<typeof flatten>
  onClose: () => void
  onCreated: () => Promise<void>
  onError: (cause: unknown) => void
}) {
  const [name, setName] = useState('')
  const [slug, setSlug] = useState('')
  const [parentId, setParentId] = useState<number | null>(null)

  const options = categoryOptions(categories, '（作为根分类）')

  const create = useMutation({
    mutationFn: () => admin.createCategory({ parentId, name: name.trim(), slug: slug.trim() }),
    onSuccess: async () => {
      setName('')
      setSlug('')
      setParentId(null)
      await onCreated()
      onClose()
    },
    onError,
  })

  if (!open) {
    return null
  }

  const onSubmit = (event: FormEvent) => {
    event.preventDefault()
    create.mutate()
  }

  return (
    <Dialog open onOpenChange={() => onClose()}>
      <DialogContent data-testid="admin-category-editor">
        <DialogHeader>
          <DialogTitle>新建分类</DialogTitle>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <Label htmlFor="admin-category-parent">上级分类</Label>
            <Select items={options} value={parentId} onValueChange={(value) => setParentId(value)}>
              <SelectTrigger id="admin-category-parent" className="mt-1 w-full" data-testid="admin-category-parent">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {options.map((option) => (
                  <SelectItem key={option.value ?? 'none'} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label htmlFor="admin-category-name">名称</Label>
            <Input
              id="admin-category-name"
              className="mt-1"
              value={name}
              onChange={(event) => setName(event.target.value)}
              data-testid="admin-category-name"
            />
          </div>
          <div>
            <Label htmlFor="admin-category-slug">标识（小写字母、数字、连字符）</Label>
            <Input
              id="admin-category-slug"
              className="mt-1"
              value={slug}
              onChange={(event) => setSlug(event.target.value)}
              data-testid="admin-category-slug"
            />
          </div>
          <DialogFooter>
            <Button type="button" variant="ghost" onClick={onClose}>
              取消
            </Button>
            <Button
              type="submit"
              disabled={name.trim() === '' || slug.trim() === '' || create.isPending}
              data-testid="admin-category-create"
            >
              确定
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
