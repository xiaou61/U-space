import { useAuth } from '@paideia/auth'
import { Badge, Button } from '@paideia/ui'
import { Link, useLocation } from 'react-router'

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
  { to: '/my-assessments', label: '我的自评', testId: 'learner-nav-my-assessments' },
]

export function LearnerHeader() {
  const { user, logout } = useAuth()
  const { pathname } = useLocation()
  if (user === null) {
    return null
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
              className={`rounded-md px-2 py-1 text-sm transition-colors ${
                active
                  ? 'text-foreground font-medium'
                  : 'text-muted-foreground hover:bg-muted/60 hover:text-foreground'
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
      <Button variant="ghost" size="sm" data-testid="logout" onClick={() => void logout()}>
        登出
      </Button>
    </>
  )
}
