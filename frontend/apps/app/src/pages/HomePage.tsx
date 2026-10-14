import {
  Alert,
  AlertDescription,
  AlertTitle,
  AppShell,
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  PageHeader,
  Spinner,
} from '@paideia/ui'
import { useAuth } from '@paideia/auth'
import { BookOpen, NotebookPen, Target } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'
import { API_BASE_URL } from '../env'
import { usePlatform } from '../platform'

interface HealthResponse {
  status: string
  groups?: string[]
}

interface SubjectResponse {
  subject: string
}

export function HomePage() {
  const { user } = useAuth()
  const platform = usePlatform()
  const health = useQuery({
    queryKey: ['backend-health'],
    queryFn: () => api.request<HealthResponse>('/actuator/health'),
  })
  // 受保护接口：身份只从令牌解析。登录之后这里会返回当前主体的标识，
  // 而在没有令牌时后端会拒为 401（e2e 用不带令牌的直连请求断言这一点）。
  const identity = useQuery({
    queryKey: ['backend-identity'],
    queryFn: () => api.requestEnvelope<SubjectResponse>('/api/v1/me').then((data) => data.subject),
  })

  return (
    <AppShell title="Paideia" subtitle="首页" actions={<LearnerHeader />}>
      <PageHeader
        title={`你好，${user?.username ?? ''}`}
        description="右下角的「助手」会读当前内容、记得你说过的话，也能把答案存成笔记。"
      />

      {/* 首页的第一件事是**把人送进去**：登录后落在 /，这里没有任何去处就等于没有首页 */}
      <div className="mb-10 grid gap-4 sm:grid-cols-3" data-testid="home-entries">
        <Card className="transition-shadow hover:shadow-card">
          <CardHeader>
            <span className="bg-accent text-accent-foreground mb-2 flex size-9 items-center justify-center rounded-lg">
              <BookOpen className="size-4" />
            </span>
            <CardTitle>
              <Link to="/knowledge" data-testid="home-entry-knowledge">
                知识库
              </Link>
            </CardTitle>
            <CardDescription>按分类浏览、按关键词检索，打开条目阅读。</CardDescription>
          </CardHeader>
        </Card>
        <Card className="transition-shadow hover:shadow-card">
          <CardHeader>
            <span className="bg-accent text-accent-foreground mb-2 flex size-9 items-center justify-center rounded-lg">
              <NotebookPen className="size-4" />
            </span>
            <CardTitle>
              <Link to="/notes" data-testid="home-entry-notes">
                我的笔记
              </Link>
            </CardTitle>
            <CardDescription>随手记点什么，或让助手把答案存进来；也能只读分享。</CardDescription>
          </CardHeader>
        </Card>
        <Card className="transition-shadow hover:shadow-card">
          <CardHeader>
            <span className="bg-accent text-accent-foreground mb-2 flex size-9 items-center justify-center rounded-lg">
              <Target className="size-4" />
            </span>
            <CardTitle>
              <Link to="/my-assessments" data-testid="home-entry-assessments">
                我的自评
              </Link>
            </CardTitle>
            <CardDescription>你标记过「懂了／还不懂」的条目。</CardDescription>
          </CardHeader>
        </Card>
      </div>

      <h2 className="text-muted-foreground mb-3 text-sm font-medium">运行状态</h2>
      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>运行环境</CardTitle>
            <CardDescription>平台能力来自 Platform 端口，界面代码不认识宿主。</CardDescription>
          </CardHeader>
          <CardContent>
            <dl className="grid grid-cols-[auto_1fr] gap-x-6 gap-y-2 text-sm">
              <dt className="text-muted-foreground">平台</dt>
              <dd className="m-0">
                <Badge variant="secondary" data-testid="platform-kind">
                  {platform.kind}
                </Badge>
              </dd>
              <dt className="text-muted-foreground">后端基址</dt>
              <dd className="m-0 font-mono text-xs" data-testid="api-base">
                {API_BASE_URL === '' ? '同源（开发期由 Vite 代理转发）' : API_BASE_URL}
              </dd>
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>后端健康检查 · GET /actuator/health</CardTitle>
            <CardDescription>这个端点是公开的，不需要令牌。</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {health.isPending && (
              <p className="text-muted-foreground flex items-center gap-2 text-sm" data-testid="health-state">
                <Spinner />
                查询中…
              </p>
            )}
            {health.isError && (
              <Alert variant="destructive" data-testid="health-state">
                <AlertTitle>请求失败</AlertTitle>
                <AlertDescription>{(health.error as Error).message}</AlertDescription>
              </Alert>
            )}
            {health.isSuccess && (
              <>
                <p className="m-0 text-sm" data-testid="health-state">
                  状态：
                  <strong data-testid="health-status">{health.data.status}</strong>
                </p>
                <pre
                  data-testid="health-raw"
                  className="bg-muted text-muted-foreground overflow-x-auto rounded-md p-3 font-mono text-xs"
                >
                  {JSON.stringify(health.data, null, 2)}
                </pre>
              </>
            )}
            <Button variant="outline" size="sm" onClick={() => void health.refetch()}>
              重新查询
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>当前身份 · GET /api/v1/me</CardTitle>
            <CardDescription>
              受保护接口。身份只从请求令牌解析，不接受客户端传入的用户标识。
            </CardDescription>
          </CardHeader>
          <CardContent>
            {identity.isPending && (
              <p className="text-muted-foreground flex items-center gap-2 text-sm" data-testid="identity-state">
                <Spinner />
                查询中…
              </p>
            )}
            {identity.isSuccess && (
              <p className="m-0 text-sm" data-testid="identity-state">
                已认证：<strong data-testid="identity-subject">{identity.data}</strong>
              </p>
            )}
            {identity.isError && (
              <p className="m-0 text-sm" data-testid="identity-state">
                <span data-testid="identity-subject">未登录</span>
                <span className="text-muted-foreground">（{(identity.error as Error).message}）</span>
              </p>
            )}
          </CardContent>
        </Card>
      </div>
    </AppShell>
  )
}
