import { useAuth } from '@paideia/auth'
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
} from '@paideia/ui'
import { useState } from 'react'
import { Link, useLocation } from 'react-router'

import { api } from '../api'

/**
 * 学习者端页头：**主导航 + 账号区**放在一个组件里。
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
 */
const NAV = [
  { to: '/knowledge', label: '知识库', testId: 'learner-nav-knowledge' },
  { to: '/notes', label: '我的笔记', testId: 'learner-nav-notes' },
  { to: '/my-assessments', label: '我的自评', testId: 'learner-nav-my-assessments' },
]

export function LearnerHeader() {
  const { user, logout } = useAuth()
  const { pathname } = useLocation()
  const [deleting, setDeleting] = useState(false)
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (user === null) {
    return null
  }

  /**
   * 注销。**不可恢复**：会话、记忆、笔记与已发出的分享一并删除（用户裁决 10）。
   *
   * <p>要口令：只凭一个被偷走的令牌就能清空一个人的全部数据，代价不对等。
   * 服务端删完刷新记录之后，本地这份令牌也刷不出新的了（会话最多活到访问令牌到期）。
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
      <nav aria-label="主导航" className="flex items-center gap-1">
        {NAV.map((item) => {
          // 条目页的地址是 /knowledge/entries/:id，所以前缀匹配也算在知识库里
          const active = pathname === item.to || pathname.startsWith(`${item.to}/`)
          return (
            <Link
              key={item.to}
              to={item.to}
              aria-current={active ? 'page' : undefined}
              data-testid={item.testId}
              className={`rounded-lg px-2.5 py-1.5 text-sm transition-colors ${
                active
                  ? 'bg-accent text-accent-foreground font-medium'
                  : 'text-muted-foreground hover:bg-accent/60 hover:text-foreground'
              }`}
            >
              {item.label}
            </Link>
          )
        })}
      </nav>
      <span className="text-muted-foreground text-xs" data-testid="current-user">
        {user.username}
      </span>
      <Badge variant="outline">{user.role === 'admin' ? '管理员' : '学习者'}</Badge>
      <Button variant="ghost" size="sm" data-testid="account-deletion-open" onClick={() => setDeleting(true)}>
        注销账号
      </Button>
      <Button variant="ghost" size="sm" data-testid="logout" onClick={() => void logout()}>
        登出
      </Button>

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
