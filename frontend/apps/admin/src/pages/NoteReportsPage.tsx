import { Alert, AlertDescription, AlertTitle, Badge, Button, Card, CardContent, PageHeader, Spinner, Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@paideia/ui'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'

import { api } from '../api'

/** 一条举报。`noteTitle` 可能为空——笔记可能已经被作者删了。 */
interface Report {
  id: number
  shareId: string
  noteTitle: string | null
  reason: string
  status: 'pending' | 'taken_down'
  createdAt: string | null
}

const list = () => api.requestEnvelope<Report[]>('/api/v1/notes/admin/reports')
const takedown = (id: number) =>
  api.requestEnvelope<boolean>(`/api/v1/notes/admin/reports/${id}/takedown`, { method: 'POST' })

/**
 * 举报处理（用户裁决 11：由管理员处理，不做自动判定）。
 *
 * <p>页面只做两件事：**看见**与**下架**。没有自动判定——"内容该不该撤"是人做的决定，
 * 系统只负责把举报按"待处理优先、早的在前"排好，以及让下架**立刻生效**。
 *
 * <p>下架是二次确认的：它不可逆（撤回链接不进回收站），而且影响的是别人能不能看到一份内容。
 */
export function NoteReportsPage() {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)

  const reports = useQuery({ queryKey: ['note-reports'], queryFn: list })

  const remove = useMutation({
    mutationFn: takedown,
    onSuccess: () => {
      setError(null)
      void queryClient.invalidateQueries({ queryKey: ['note-reports'] })
    },
    onError: (cause: unknown) => setError(cause instanceof Error ? cause.message : String(cause)),
  })

  return (
    <div className="space-y-4">
      <PageHeader title="举报处理" description="被举报的分享链接。下架后链接立刻不可读，且不可恢复。" />

      {error === null ? null : (
        <Alert variant="destructive" data-testid="reports-error">
          <AlertTitle>没能完成</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardContent className="py-4">
          {reports.isLoading ? (
            <Spinner />
          ) : (reports.data ?? []).length === 0 ? (
            <p className="text-muted-foreground text-sm" data-testid="reports-empty">
              还没有人举报。
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>笔记</TableHead>
                  <TableHead>理由</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead className="text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody data-testid="reports-table">
                {(reports.data ?? []).map((report) => (
                  <TableRow key={report.id} data-testid={`report-row-${report.id}`}>
                    <TableCell className="max-w-xs truncate">{report.noteTitle ?? '（笔记已删除）'}</TableCell>
                    <TableCell className="max-w-md whitespace-pre-wrap">{report.reason}</TableCell>
                    <TableCell>
                      <Badge variant="outline" data-testid={`report-status-${report.id}`}>
                        {report.status === 'pending' ? '待处理' : '已下架'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      {report.status === 'pending' ? (
                        <Button
                          size="sm"
                          variant="outline"
                          disabled={remove.isPending}
                          data-testid={`report-takedown-${report.id}`}
                          onClick={() => {
                            if (window.confirm('下架这条分享？链接会立刻失效，不可恢复。')) {
                              remove.mutate(report.id)
                            }
                          }}
                        >
                          下架
                        </Button>
                      ) : null}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
