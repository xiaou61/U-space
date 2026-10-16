import { ThemeToggle } from '@paideia/ui'
import {
  ChevronDown,
  ChevronRight,
  Coins,
  FileText,
  Flag,
  FolderTree,
  LayoutDashboard,
  Library,
  PanelLeftClose,
  PanelLeftOpen,
  Receipt,
  Wallet,
} from 'lucide-react'
import { useState, type ReactNode } from 'react'
import { Link, useLocation } from 'react-router'

interface MenuItem {
  to: string
  label: string
  icon: typeof FileText
  /** 有子项的是一级菜单，点了展开/收起，自身不跳转。 */
  children?: MenuItem[]
}

/**
 * 侧边菜单。层级与路由表一一对应，父项只是分组、不指向任何页面。
 *
 * <p>放在管理端而不是设计系统里：学习者端是内容优先的单列阅读界面，两侧的信息架构本就
 * 不同，把侧栏做成共享组件等于给阅读界面塞一套它不需要的导航。
 */
const MENU: MenuItem[] = [
  { to: '/', label: '总览', icon: LayoutDashboard },
  {
    to: '/knowledge',
    label: '知识库',
    icon: Library,
    children: [
      { to: '/knowledge/categories', label: '分类管理', icon: FolderTree },
      { to: '/knowledge/entries', label: '条目管理', icon: FileText },
    ],
  },
  // 举报处理是学习者端分享出去的内容的**唯一治理入口**（用户裁决 11：由管理员处理）
  { to: '/reports', label: '举报处理', icon: Flag },
  // AI 积分（WORK-009）：本期不做充值，所以"积分调整"就是积分唯一的入口，
  // "用量审计"是回答"谁用了多少、花了多少、谁给他发的"的唯一地方。两页共用一份流水。
  {
    to: '/ai-credits',
    label: 'AI 积分',
    icon: Coins,
    children: [
      { to: '/ai-credits/grant', label: '积分调整', icon: Wallet },
      { to: '/ai-credits/audit', label: '用量审计', icon: Receipt },
    ],
  },
]

/** 面包屑：按菜单层级写死的路径（首页固定在最前，不再逐级可点）。 */
const CRUMBS: Record<string, string[]> = {
  '/': ['总览'],
  '/knowledge/categories': ['知识库', '分类管理'],
  '/knowledge/entries': ['知识库', '条目管理'],
  '/reports': ['举报处理'],
  '/ai-credits/grant': ['AI 积分', '积分调整'],
  '/ai-credits/audit': ['AI 积分', '用量审计'],
}

/**
 * 管理端外壳：左侧菜单 + 顶栏（面包屑 / 操作区）+ 内容区。
 *
 * <p>与学习者端的 `AppShell` 是两种骨架：那边是顶部横条 + 单列窄内容，这边是后台常见
 * 的"深色边框侧栏 + 灰底内容 + 白卡片"。共用的是色板与组件，不是布局——两边的信息
 * 密度差一个量级，硬套同一个外壳会让这两边都不好用。
 *
 * <p>侧栏可收起（宽窄两档），收起后只留图标并用 `title` 兜住可读性：后台的表单页常需要
 * 横向空间，这是这个布局最该给的一个开关。
 */
export function AdminShell({ actions, children }: { actions?: ReactNode; children: ReactNode }) {
  const { pathname } = useLocation()
  const [collapsed, setCollapsed] = useState(false)
  // 只记录"用户手动点过"的父菜单；没点过的跟着当前路由走
  const [toggled, setToggled] = useState<Record<string, boolean>>({})
  const crumbs = CRUMBS[pathname] ?? ['管理端']

  return (
    <div className="flex min-h-screen">
      <aside
        className={`border-border bg-card sticky top-0 flex h-screen shrink-0 flex-col border-r transition-[width] duration-200 ${
          collapsed ? 'w-14' : 'w-56'
        }`}
      >
        <div className="border-border flex h-14 items-center gap-2.5 border-b px-3">
          <span className="bg-primary text-primary-foreground grid size-7 shrink-0 place-items-center rounded-md text-xs font-bold">
            P
          </span>
          {collapsed ? null : (
            <div className="min-w-0">
              <div className="truncate text-sm leading-tight font-semibold">Paideia</div>
              <div className="text-muted-foreground truncate text-[11px] leading-tight">管理端</div>
            </div>
          )}
        </div>

        <nav className="flex-1 space-y-1 overflow-y-auto p-2">
          {MENU.map((item) => {
            const Icon = item.icon
            if (item.children === undefined) {
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  label={item.label}
                  icon={Icon}
                  collapsed={collapsed}
                  active={pathname === item.to}
                />
              )
            }

            // 展开与否：用户手动点过的以手动为准，没点过就跟着当前路由走（进到子页面自动展开）
            const open = toggled[item.label] ?? item.children.some((child) => child.to === pathname)
            const active = item.children.some((child) => child.to === pathname)
            return (
              <div key={item.to}>
                <button
                  type="button"
                  title={item.label}
                  aria-expanded={open}
                  data-testid={`admin-menu-${item.label}`}
                  onClick={() => {
                    // 收起状态下点父菜单先把侧栏展开——否则子项根本没地方显示
                    if (collapsed) {
                      setCollapsed(false)
                      setToggled((current) => ({ ...current, [item.label]: true }))
                      return
                    }
                    setToggled((current) => ({ ...current, [item.label]: !open }))
                  }}
                  className={`flex h-8 w-full items-center gap-2 rounded-md px-2 text-sm transition-colors ${
                    active
                      ? 'text-foreground font-medium'
                      : 'text-muted-foreground hover:bg-accent/60 hover:text-foreground'
                  } ${collapsed ? 'justify-center' : ''}`}
                >
                  <Icon className="size-4 shrink-0" />
                  {collapsed ? null : (
                    <>
                      <span className="flex-1 truncate text-left">{item.label}</span>
                      <ChevronDown
                        className={`size-3.5 shrink-0 transition-transform ${open ? '' : '-rotate-90'}`}
                      />
                    </>
                  )}
                </button>
                {open && !collapsed ? (
                  <div className="border-border mt-1 ml-4 space-y-1 border-l pl-1">
                    {item.children.map((child) => (
                      <NavLink
                        key={child.to}
                        to={child.to}
                        label={child.label}
                        icon={child.icon}
                        collapsed={false}
                        active={pathname === child.to}
                      />
                    ))}
                  </div>
                ) : null}
              </div>
            )
          })}
        </nav>

        <div className="border-border border-t p-2">
          <button
            type="button"
            onClick={() => setCollapsed((value) => !value)}
            title={collapsed ? '展开菜单' : '收起菜单'}
            aria-label={collapsed ? '展开菜单' : '收起菜单'}
            data-testid="admin-menu-toggle"
            className={`text-muted-foreground hover:bg-muted hover:text-foreground flex h-8 w-full items-center gap-2 rounded-md px-2 text-sm ${
              collapsed ? 'justify-center' : ''
            }`}
          >
            {collapsed ? <PanelLeftOpen className="size-4" /> : <PanelLeftClose className="size-4" />}
            {collapsed ? null : <span>收起菜单</span>}
          </button>
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="border-border bg-background sticky top-0 z-10 flex h-14 shrink-0 items-center justify-between gap-4 border-b px-4">
          <nav aria-label="面包屑" className="flex min-w-0 items-center gap-1.5 text-sm">
            <span className="text-muted-foreground shrink-0">首页</span>
            {crumbs.map((crumb, index) => (
              <span key={crumb} className="flex min-w-0 items-center gap-1.5">
                <ChevronRight className="text-muted-foreground size-3.5 shrink-0" />
                <span className={index === crumbs.length - 1 ? 'truncate font-medium' : 'text-muted-foreground truncate'}>
                  {crumb}
                </span>
              </span>
            ))}
          </nav>
          <div className="flex shrink-0 items-center gap-2">
            {actions}
            <ThemeToggle />
          </div>
        </header>

        <main className="bg-muted/60 flex-1 p-6">{children}</main>
      </div>
    </div>
  )
}

/** 菜单项（一级与子项共用）：收起状态只剩图标，用 `title` 与无障碍名称兜住可读性。 */
function NavLink({
  to,
  label,
  icon: Icon,
  collapsed,
  active,
}: {
  to: string
  label: string
  icon: typeof FileText
  collapsed: boolean
  active: boolean
}) {
  return (
    <Link
      to={to}
      title={label}
      aria-current={active ? 'page' : undefined}
      data-active={active ? 'true' : undefined}
      className={`flex h-9 items-center gap-2 rounded-lg border px-2.5 text-sm transition-colors ${
        active
          ? 'border-border bg-accent text-accent-foreground font-medium'
          : 'text-muted-foreground hover:bg-accent/60 hover:text-foreground border-transparent'
      } ${collapsed ? 'justify-center' : ''}`}
    >
      <Icon className="size-4 shrink-0" />
      {collapsed ? null : <span className="truncate">{label}</span>}
    </Link>
  )
}
