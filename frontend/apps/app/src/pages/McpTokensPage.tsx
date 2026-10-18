import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
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
  Label,
  PageHeader,
  Spinner,
} from '@paideia/ui'
import { useState } from 'react'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'
import { createMcpTokensApi } from '../mcp-tokens-api'

const tokens = createMcpTokensApi(api)

const SCOPE_LABELS: Record<string, string> = { read: '只读', write: '写入' }

/**
 * MCP 接入凭据（WORK-019）。
 *
 * <p>页面只做三件事：看一眼有哪些凭据、建一个、吊销一个。**没有任何"再显示一次"的按钮**——
 * 服务端不保存明文，刷新之后拿不回来；把这句话写在界面上，比事后解释好。
 *
 * <p>创建成功后明文卡片**留在页面上直到用户离开**：立刻切走会让"我还没来得及复制"变成
 * 一个无法补救的问题。
 */
export function McpTokensPage() {
  const queryClient = useQueryClient()
  const { data, isLoading, error } = useQuery({ queryKey: ['mcp-tokens'], queryFn: () => tokens.list() })

  const [name, setName] = useState('')
  const [write, setWrite] = useState(false)
  const [neverExpires, setNeverExpires] = useState(false)
  const [created, setCreated] = useState<string | null>(null)
  const [copyState, setCopyState] = useState<'idle' | 'copied'>('idle')
  const [createError, setCreateError] = useState<string | null>(null)

  const createToken = useMutation({
    mutationFn: () =>
      tokens.create({
        name: name.trim(),
        scopes: write ? ['read', 'write'] : ['read'],
        expiresInDays: neverExpires ? 0 : 90,
      }),
    onSuccess: async (result) => {
      setCreated(result.token)
      setCopyState('idle')
      setName('')
      setCreateError(null)
      await queryClient.invalidateQueries({ queryKey: ['mcp-tokens'] })
    },
    onError: (cause: unknown) => {
      setCreated(null)
      setCreateError(cause instanceof Error ? cause.message : String(cause))
    },
  })

  const revokeToken = useMutation({
    mutationFn: (id: number) => tokens.revoke(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['mcp-tokens'] })
    },
  })

  const revokeNow = (id: number, tokenName: string) => {
    // 二次确认：吊销不可撤销，而列表里每行长得一样，误点一次就等于让对方重新配一遍
    const confirmed = window.confirm(`吊销「${tokenName}」？用到它的外部助手会立刻失去访问权，无法恢复。`)
    if (confirmed) {
      revokeToken.mutate(id)
    }
  }

  const copy = async () => {
    if (created === null) {
      return
    }
    try {
      await navigator.clipboard.writeText(created)
      setCopyState('copied')
    } catch {
      // 剪贴板可能被浏览器策略拒绝：不报错，让用户手动选中复制
      setCopyState('idle')
    }
  }

  return (
    <AppShell title="Paideia" actions={<LearnerHeader />}>
      <div className="mx-auto max-w-3xl space-y-6">
        <PageHeader
          eyebrow="设置"
          title="MCP 接入"
          description="让 ZCode、Claude Desktop 这类外部助手直接读写你的知识库与笔记。每个凭据只显示一次明文，丢了就重新建一个。"
        />

        {created === null ? null : (
          <Card className="border-primary/40">
            <CardContent className="space-y-3 pt-6">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <p className="text-sm font-medium">凭据已创建</p>
                  <p className="text-muted-foreground text-xs">
                    只显示这一次。刷新页面后无法再查看，丢了只能吊销重建。
                  </p>
                </div>
                <Button size="sm" variant="outline" data-testid="mcp-token-copy" onClick={() => { void copy() }}>
                  {copyState === 'copied' ? '已复制' : '复制'}
                </Button>
              </div>
              <code
                data-testid="mcp-token-plaintext"
                className="bg-muted block w-full overflow-x-auto rounded-md p-3 font-mono text-xs"
              >
                {created}
              </code>
            </CardContent>
          </Card>
        )}

        <Card>
          <CardContent className="space-y-4 pt-6">
            <p className="text-sm font-medium">新建凭据</p>
            <div className="space-y-2">
              <Label htmlFor="mcp-token-name">名称</Label>
              <Input
                id="mcp-token-name"
                data-testid="mcp-token-name"
                placeholder="例如：家里的 Claude Desktop"
                value={name}
                onChange={(event) => { setName(event.target.value) }}
              />
            </div>
            <div className="space-y-3">
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  data-testid="mcp-token-write"
                  checked={write}
                  onChange={(event) => { setWrite(event.target.checked) }}
                />
                允许写入（建笔记、打自评标记）
              </label>
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  data-testid="mcp-token-never-expires"
                  checked={neverExpires}
                  onChange={(event) => { setNeverExpires(event.target.checked) }}
                />
                永不过期（默认 90 天）
              </label>
            </div>
            <Button
              data-testid="mcp-token-create"
              disabled={createToken.isPending || name.trim() === ''}
              onClick={() => { createToken.mutate() }}
            >
              {createToken.isPending ? '创建中…' : '创建'}
            </Button>
            {createError === null ? null : (
              <Alert variant="destructive">
                <AlertTitle>没建成功</AlertTitle>
                <AlertDescription>{createError}</AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardContent className="space-y-3 pt-6">
            <p className="text-sm font-medium">我的凭据</p>
            {isLoading ? <Spinner /> : null}
            {error === null ? null : (
              <Alert variant="destructive">
                <AlertTitle>读不到凭据列表</AlertTitle>
                <AlertDescription>{error instanceof Error ? error.message : String(error)}</AlertDescription>
              </Alert>
            )}
            {data !== undefined && data.length === 0 ? (
              <p className="text-muted-foreground text-sm" data-testid="mcp-token-empty">
                还没有任何凭据。
              </p>
            ) : null}
            <ul className="space-y-3">
              {(data ?? []).map((token) => (
                <li
                  key={token.id}
                  data-testid="mcp-token-row"
                  className="border-border flex flex-wrap items-center justify-between gap-3 rounded-md border p-3"
                >
                  <div className="min-w-0 space-y-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="truncate text-sm font-medium">{token.name}</span>
                      {token.scopes.map((scope) => (
                        <Badge key={scope} variant="secondary">{SCOPE_LABELS[scope] ?? scope}</Badge>
                      ))}
                      {token.active ? (
                        <Badge>有效</Badge>
                      ) : (
                        <Badge variant="destructive">已失效</Badge>
                      )}
                    </div>
                    <p className="text-muted-foreground font-mono text-xs">{token.prefix}…</p>
                    <p className="text-muted-foreground text-xs">
                      {token.expiresAt === null ? '永不过期' : `到期 ${formatDate(token.expiresAt)}`}
                      {' · '}
                      最近使用 {token.lastUsedAt === null ? '从未' : formatDate(token.lastUsedAt)}
                    </p>
                  </div>
                  {token.revokedAt === null ? (
                    <Button
                      size="sm"
                      variant="outline"
                      data-testid="mcp-token-revoke"
                      disabled={revokeToken.isPending}
                      onClick={() => { revokeNow(token.id, token.name) }}
                    >
                      吊销
                    </Button>
                  ) : null}
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="space-y-2 pt-6 text-sm">
            <p className="font-medium">怎么接</p>
            <p className="text-muted-foreground">
              在外部助手的 MCP 配置里填地址与本页签发的 token：
            </p>
            <code className="bg-muted block overflow-x-auto rounded-md p-3 font-mono text-xs">
              {`{"type":"http","url":"<站点地址>/mcp","headers":{"Authorization":"Bearer <token>"}}`}
            </code>
            <p className="text-muted-foreground text-xs">
              <code>/mcp</code> 是只读默认视图；<code>/mcp/x/knowledge</code> 单看知识库；
              <code>/mcp/x/all</code> 是全部只读；写工具在 <code>/mcp/write</code> 或
              <code>/mcp/x/{'{组}'}/write</code>，且需要上面勾过「允许写入」。
            </p>
          </CardContent>
        </Card>
      </div>
    </AppShell>
  )
}

function formatDate(iso: string): string {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) {
    return iso
  }
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function pad(value: number): string {
  return value < 10 ? `0${value}` : String(value)
}
