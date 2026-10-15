import { useAuth } from '@paideia/auth'
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  Input,
} from '@paideia/ui'
import { ChevronDown, LogOut, Search, UserX } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'

import { api } from '../api'

/**
 * 学习者端页头：**主导航 + 搜索 + 账号区**放在一个组件里。
 *
 * <p>2026-10-12 修的一个真缺陷：知识库、条目页、我的自评三处页面互相都连着，但**没有人
 * 从首页指向它们**——登录后落在 `/`，页头只有用户名与登出，用户不可能自己走到内容里去。
 * 界面本身没坏，是**入口**从来没被当成需求写下来，所以两轮的验收标准（"在条目页能…"）
 * 都绕过了它。
 *
 * <p>导航与账号区刻意合在一个组件、而不是分别由各页面拼装：分开写的话，将来加一页就
 * 有可能只记得其一，而"某个页面忘了加入口"正是这次缺陷的形态。
 *
 * <p>激活态用 `aria-current="page"` 而不是只靠颜色——颜色只是它的一种表现。
 *
 * <p>**搜索框放在页头**（照文档站与笔记类产品的通例）：它是这一站最常用的动作，藏在某个
 * 页面里等于每次都要先走进去。它只负责把关键词带到条目列表页（`?q=`），真正的检索逻辑
 * 仍然只有一处。
 */
const NAV = [
  { to: '/knowledge', label: '知识库', testId: 'learner-nav-knowledge' },
  { to: '/notes', label: '我的笔记', testId: 'learner-nav-notes' },
  { to: '/my-assessments', label: '我的自评', testId: 'learner-nav-my-assessments' },
]

export function LearnerHeader() {
  const { user, logout } = useAuth()
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const [deleting, setDeleting] = useState(false)
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [keyword, setKeyword] = useState('')

  if (user === null) {
    return null
  }

  const submitSearch = (event: FormEvent) => {
    event.preventDefault()
    const trimmed = keyword.trim()
    if (trimmed === '') {
      return
    }
    navigate(`/knowledge/entries?q=${encodeURIComponent(trimmed)}`)
    setKeyword('')
  }

  /**
   * 注销。**不可恢复**：会话、记忆、笔记与已发出的分享一并删除（用户裁决 10）。
   *
   * <p>要口令：只凭一个被偷走的令牌就能清空一个人的全部数据，代价不对等。
   * 服务端删完刷新记录之后，本地这份令牌也刷不出新会话了（会话最多活到访问令牌到期）。
   */
  const confirmDeletion = async () => {
    setBusy(true)
    setError(null)
    try {
      await api.requestEnvelope<boolean>('/api/v1/me/deletion', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ password }),
      })
      // 账号已经没了：登出只是一个本地收尾（服务端那条刷新记录也被删了，登出接口会失败，
      // 所以这里不 await 它的成败——本地令牌必须清掉，界面要回到登录门）
      void logout().catch(() => undefined)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause))
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <nav aria-label="主导航" className="flex items-center gap-0.5">
        {NAV.map((item) => {
          // 条目页的地址是 /knowledge/entries/:id，所以前缀匹配也算在知识库里
          const active = pathname === item.to || pathname.startsWith(`${item.to}/`)
          return (
            <Link
              key={item.to}
              to={item.to}
              aria-current={active ? 'page' : undefined}
              data-testid={item.testId}
              className={`rounded-full border px-3 py-1.5 text-sm transition-colors ${
                active
                  ? 'bg-accent text-accent-foreground border-border font-medium'
                  : 'text-muted-foreground hover:bg-accent/60 hover:text-foreground border-transparent'
              }`}
            >
              {item.label}
            </Link>
          )
        })}
      </nav>

      <form onSubmit={submitSearch} className="ml-2 hidden md:block">
        <div className="relative">
          <Search className="text-muted-foreground pointer-events-none absolute top-1/2 left-2.5 size-3.5 -translate-y-1/2" />
          <Input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="搜索知识库…"
            aria-label="搜索知识库"
            data-testid="header-search"
            className="h-9 w-56 rounded-full pl-8 text-sm"
          />
        </div>
      </form>

      <div className="flex-1" />

      <DropdownMenu>
        <DropdownMenuTrigger
          data-testid="account-menu-trigger"
          render={
            <button
              type="button"
              className="hover:bg-accent flex shrink-0 items-center gap-2 rounded-full border border-transparent py-1 pr-2.5 pl-1 transition-colors"
            />
          }
        >
          <span className="bg-primary text-primary-foreground flex size-7 items-center justify-center rounded-full text-xs font-semibold">
            {user.username.slice(0, 1).toUpperCase()}
          </span>
          {/* 用户名留在触发器里而不是菜单里：它同时是"我是谁"的显示位，
              也是 e2e 判断登录态的锚点 */}
          <span className="text-muted-foreground text-xs" data-testid="current-user">
            {user.username}
          </span>
          <ChevronDown className="text-muted-foreground size-3.5" />
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-56">
          {/* Base UI 的 GroupLabel 必须在 Group 里，否则展开时会抛 MenuGroupContext is missing */}
          <DropdownMenuGroup>
            <DropdownMenuLabel>
              <span className="block text-sm font-medium">{user.username}</span>
              <span className="text-muted-foreground block text-xs">
                {user.role === 'admin' ? '管理员' : '学习者'}
              </span>
            </DropdownMenuLabel>
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          {/* 注销与登出都收进菜单：它们是低频且危险的动作，摆在页头只会天天误点 */}
          <DropdownMenuItem data-testid="account-deletion-open" onClick={() => setDeleting(true)}>
            <UserX className="size-4" />
            注销账号
          </DropdownMenuItem>
          <DropdownMenuItem data-testid="logout" onClick={() => void logout()}>
            <LogOut className="size-4" />
            登出
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <Dialog open={deleting} onOpenChange={setDeleting}>
        <DialogContent data-testid="account-deletion-dialog">
          <DialogHeader>
            <DialogTitle>注销账号</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <Alert variant="destructive">
              <AlertTitle>这一步不可恢复</AlertTitle>
              <AlertDescription>
                你的会话、记忆、个人笔记与已发出的分享都会被删除，且无法找回。
              </AlertDescription>
            </Alert>
            <Input
              type="password"
              value={password}
              placeholder="输入当前密码确认"
              aria-label="当前密码"
              data-testid="account-deletion-password"
              onChange={(event) => setPassword(event.target.value)}
            />
            {error === null ? null : <p className="text-destructive text-sm">{error}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleting(false)} disabled={busy}>
              取消
            </Button>
            <Button
              variant="destructive"
              disabled={busy || password === ''}
              data-testid="account-deletion-confirm"
              onClick={() => void confirmDeletion()}
            >
              确认注销
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
