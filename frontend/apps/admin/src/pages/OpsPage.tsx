import {
  Badge,
  Card,
  CardAction,
  CardContent,
  CardHeader,
  CardTitle,
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
  PageHeader,
  Spinner,
} from '@paideia/ui'
import { useQuery } from '@tanstack/react-query'
import { Activity, Database, HardDrive, RefreshCw, Server, ShieldAlert, Timer } from 'lucide-react'

import { api } from '../api'
import { auth } from '../auth'

/**
 * 运维面板（WORK-028）。
 *
 * <p>七张卡片各答一个问题：主机还剩多少、服务活没活、中间件通不通、外部依赖在不在、
 * 定时任务跑没跑、开关开没开、备份新不新。**深挖看下面的整块面板**——趋势只能靠时序图，
 * 卡片只回答"现在"。
 *
 * <p>两条显示纪律：
 * <ol>
 *   <li>**「未知」是独立的一档**，不能画成 0、也不能画成绿色。取不到数据时显示"未知"
 *       并给出原因，因为"监控自己挂了"与"一切正常"看起来必须不一样。</li>
 *   <li>**状态与原因都由后端判定**，前端不复算阈值——判据只有一份（`OpsSummaryService`）。</li>
 * </ol>
 */
type OpsStatus = 'OK' | 'WARN' | 'ERROR' | 'UNKNOWN'

interface Metric {
  name: string
  value: number | null
  unit: string
  text: string | null
}

interface Group {
  key: string
  title: string
  status: OpsStatus
  reason: string
  metrics: Metric[]
}

interface Summary {
  collectedAt: string
  prometheusReachable: boolean
  groups: Group[]
}

/** 卡片与图标一一对应；出现未知的 group 时用兜底图标，不能因为缺图标就不渲染。 */
const ICONS: Record<string, typeof Server> = {
  host: HardDrive,
  services: Server,
  middleware: Database,
  external: Activity,
  scheduled: Timer,
  switches: ShieldAlert,
  backup: Database,
}

// 四态各有自己的色调：警告用 warning 而不是 outline，否则"警告"与"未知"在视觉上
// 分辨不出来——而这两者的处置动作完全不同
const STATUS_STYLE: Record<
  OpsStatus,
  { label: string; variant: 'success' | 'warning' | 'destructive' | 'outline' }
> = {
  OK: { label: '正常', variant: 'success' },
  WARN: { label: '警告', variant: 'warning' },
  ERROR: { label: '异常', variant: 'destructive' },
  UNKNOWN: { label: '未知', variant: 'outline' },
}

/**
 * 面板地址。
 *
 * `?kiosk` 去掉 Grafana 自己的外壳（侧栏与顶栏），只留面板内容。
 * `?ticket=` 是必须的：**浏览器对 iframe 的导航请求不附加 `Authorization` 头**，
 * 没有它 iframe 里的文档请求会被安全链挡成 401（实测一片空白）。
 * 后端只在这一条前缀上接受查询串里的令牌（见 `OpsTicketBearerResolver`）。
 */
function panelUrl(ticket: string): string {
  // **末尾斜杠是必要的**：Grafana 页面里的静态资源写成相对路径（`public/build/...`），
  // 浏览器按文档 URL 的目录部分解析。没有斜杠时基准变成 `/d/`，资源全 404，
  // 页面显示 "Grafana has failed to load its application files"。
  return `/api/v1/ops/grafana/d/paideia-ops/?kiosk&ticket=${encodeURIComponent(ticket)}`
}

export function OpsPage() {
  // access token 只在内存里（不落 localStorage），换页不丢、刷新页面要重新登录。
  // 面板 iframe 靠它作 ticket，所以拿不到时不渲染 iframe 而不是渲染一个注定 401 的框。
  const accessToken = auth.tokens.access()
  const summary = useQuery({
    queryKey: ['admin-ops-summary'],
    // requestEnvelope 取的是统一结构的 data 字段；request 返回整包（含 code/message/traceId）。
    // 用错它会在渲染时抛 "Cannot read properties of undefined (reading 'length')"——
    // 实测踩过：页面只显示 "Unexpected Application Error!"，接口本身是 200。
    queryFn: () => api.requestEnvelope<Summary>('/api/v1/ops/admin/summary'),
    // 面板本身 30 秒刷新一次；卡片跟同一个节奏，免得两处数字对不上
    refetchInterval: 30_000,
  })

  if (summary.isPending) {
    return (
      <>
        <PageHeader title="运维" description="服务、中间件、外部依赖与备份的当前状态。" />
        <div className="text-muted-foreground flex items-center gap-2 text-sm">
          <Spinner /> 正在采集…
        </div>
      </>
    )
  }

  if (summary.isError) {
    return (
      <>
        <PageHeader title="运维" description="服务、中间件、外部依赖与备份的当前状态。" />
        <Empty>
          <EmptyHeader>
            <EmptyMedia variant="icon">
              <ShieldAlert />
            </EmptyMedia>
            <EmptyTitle>采集失败</EmptyTitle>
            {/* 这里说"取不到"，不说"一切正常"——运维页最不该做的就是猜 */}
            <EmptyDescription>摘要接口没有返回数据。这不代表服务正常，只代表看不了。</EmptyDescription>
          </EmptyHeader>
        </Empty>
      </>
    )
  }

  const data = summary.data

  return (
    <>
      <PageHeader
        title="运维"
        description="服务、中间件、外部依赖与备份的当前状态。趋势与下钻在下面的面板里。"
        actions={
          <Badge variant={data.prometheusReachable ? 'success' : 'destructive'}>
            {data.prometheusReachable ? '采集正常' : 'Prometheus 不可达'}
          </Badge>
        }
      />

      {data.groups.length === 0 ? (
        <Empty>
          <EmptyHeader>
            <EmptyMedia variant="icon">
              <RefreshCw />
            </EmptyMedia>
            <EmptyTitle>还没有数据</EmptyTitle>
            <EmptyDescription>Prometheus 刚起来时可能还没有抓到第一轮。</EmptyDescription>
          </EmptyHeader>
        </Empty>
      ) : (
        <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {data.groups.map((group) => {
            const Icon = ICONS[group.key] ?? Server
            const style = STATUS_STYLE[group.status]
            return (
              <Card key={group.key} data-testid={`ops-group-${group.key}`}>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2 text-sm font-medium">
                    <span className="tile-surface border-border text-primary grid size-8 shrink-0 place-items-center rounded-lg border">
                      <Icon className="size-4" />
                    </span>
                    {group.title}
                  </CardTitle>
                  <CardAction>
                    <Badge variant={style.variant} data-testid={`ops-status-${group.key}`}>
                      {style.label}
                    </Badge>
                  </CardAction>
                </CardHeader>
                <CardContent className="text-muted-foreground text-xs leading-relaxed">
                  {group.reason}
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}

      <Card className="overflow-hidden py-0">
        {accessToken === null ? (
          <div className="text-muted-foreground flex items-center gap-2 p-6 text-sm">
            <Spinner /> 需要登录会话才能加载面板（令牌只存在内存里，刷新页面后请重新登录）。
          </div>
        ) : (
          <iframe
            title="Paideia 运维面板"
            src={panelUrl(accessToken)}
            className="h-[calc(100dvh-15rem)] min-h-[480px] w-full border-0"
          />
        )}
      </Card>
    </>
  )
}
