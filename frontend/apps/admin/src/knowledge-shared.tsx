import { createKnowledgeAdminApi, type CategoryNode } from '@paideia/knowledge'
import {
  Button,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Spinner,
} from '@paideia/ui'
import { TriangleAlert } from 'lucide-react'

import { api } from './api'

/**
 * 分类管理与条目管理共用的东西。
 *
 * <p>两个页面各占一个菜单项，但仍然共用同一份分类树：**条目要挂到分类上**，所以拆分界面
 * 不等于拆开数据。共享的是"怎么把树摊平、怎么列成下拉、删除怎么确认"这三件事，
 * 页面各自的表格、工具栏与弹窗仍留在各自的文件里。
 */
export const admin = createKnowledgeAdminApi(api)

export interface FlatCategory {
  id: number
  name: string
  slug: string
  depth: number
  /** 上级分类名：表格里显式列出来，光靠缩进说不清"缩进到哪一层算谁的孩子"。 */
  parentName: string | null
}

export function flatten(nodes: CategoryNode[], depth = 0, parentName: string | null = null): FlatCategory[] {
  return nodes.flatMap((node) => [
    { id: node.id, name: node.name, slug: node.slug, depth, parentName },
    ...flatten(node.children, depth + 1, node.name),
  ])
}

/** 分类下拉的选项：`null` 是"未分类/根分类"，用 `null` 而不是空串——空串是一个合法值，当哨兵会混淆。 */
export function categoryOptions(flat: FlatCategory[], noneLabel: string) {
  return [
    { value: null as number | null, label: noneLabel },
    ...flat.map((category) => ({
      value: category.id as number | null,
      label: `${'\u3000'.repeat(category.depth)}${category.name}`,
    })),
  ]
}

export function Loading({ label }: { label: string }) {
  return (
    <p className="text-muted-foreground flex items-center gap-2 px-(--card-spacing) pb-3 text-sm">
      <Spinner />
      {label}
    </p>
  )
}

/**
 * 删除前的二次确认。
 *
 * <p>两个列表共用：删除不可撤销，一次误点就该被拦下，不该指望用户"下次小心"。
 */
export function ConfirmDeleteDialog({
  open,
  label,
  name,
  hint,
  busy,
  onCancel,
  onConfirm,
}: {
  open: boolean
  /** 被删对象的类别名，如「条目」「分类」。 */
  label: string
  name: string
  /** 额外提醒，例如分类下还有子分类时会被拒。 */
  hint?: string
  busy: boolean
  onCancel: () => void
  onConfirm: () => void
}) {
  if (!open) {
    return null
  }
  return (
    <Dialog open onOpenChange={() => onCancel()}>
      <DialogContent className="sm:max-w-md" data-testid="admin-delete-dialog">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <TriangleAlert className="text-destructive size-4" />
            确认删除
          </DialogTitle>
        </DialogHeader>
        <p className="text-muted-foreground text-sm">
          将删除{label}「{name}」，此操作不可撤销。{hint ?? ''}
        </p>
        <DialogFooter>
          <Button variant="ghost" onClick={onCancel} data-testid="admin-delete-cancel">
            取消
          </Button>
          <Button variant="destructive" disabled={busy} onClick={onConfirm} data-testid="admin-delete-confirm">
            删除
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
