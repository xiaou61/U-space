import { RequireAuth } from '@paideia/auth'
import { createHashRouter } from 'react-router'

import { HomePage } from './pages/HomePage'
import { KnowledgeEntryPage } from './pages/KnowledgeEntryPage'
import { CategoryEntriesPage } from './pages/CategoryEntriesPage'
import { KnowledgeCategoriesPage } from './pages/KnowledgeCategoriesPage'
import { LoginPage } from './pages/LoginPage'
import { InterviewPage } from './pages/InterviewPage'
import { ProfilePage } from './pages/ProfilePage'
import { MyAssessmentsPage } from './pages/MyAssessmentsPage'
import { NoteNewPage } from './pages/NoteNewPage'
import { NotesPage } from './pages/NotesPage'
import { RegisterPage } from './pages/RegisterPage'
import { SharedNotePage } from './pages/SharedNotePage'

/**
 * 使用 Hash 路由：桌面壳从自定义协议加载页面，基于浏览器历史的路径路由在那种来源下会失效。
 * Hash 路由让同一套路由同时服务 Web 与桌面端，不必为两端维护两份路由配置。
 *
 * <p>学习者端**整体要求登录**：内容页统一包在 `RequireAuth` 里，登录页与注册页是仅有的两个
 * 不需要登录的路由。公开页不在本应用里——它是独立的 apps/public。
 *
 * <p>这条守卫只做界面分流，**不是授权边界**：真正的拦截在后端每个接口上。
 *
 * <p><b>Hash 路由的一个副作用</b>：地址栏的 hash 就是路由，所以正文目录里的章节锚点
 * **不能**写成 `href="#章节"`——那会被当成一次路由跳转，跳到空白页。目录用的是按钮 +
 * `scrollIntoView`，见 `packages/knowledge` 的 `EntryToc`。
 */
export const router = createHashRouter([
  {
    // 分享页**不包 RequireAuth**：它是给站外的人看的（用户裁决 12）。
    // 代价是这个路由也加载了学习者端的整包（含认证代码）——比为此单开一个前端产物便宜得多，
    // 而且登录门之外这唯一的一个页面，正是匿名访问者真正会走到的入口
    // （apps/public 在当前部署里并没有被服务，线上根路径就是学习者端）。
    path: '/share/:shareId',
    element: <SharedNotePage />,
  },
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  {
    path: '/',
    element: (
      <RequireAuth>
        <HomePage />
      </RequireAuth>
    ),
  },
  // 知识库是两级：先分类（卡片网格），点进去才是条目列表。
  // 原来把分类塞进一个 16rem 侧栏、条目挤在旁边，一屏又空又单调（用户反馈）。
  {
    path: '/knowledge',
    element: (
      <RequireAuth>
        <KnowledgeCategoriesPage />
      </RequireAuth>
    ),
  },
  {
    path: '/knowledge/entries',
    element: (
      <RequireAuth>
        <CategoryEntriesPage />
      </RequireAuth>
    ),
  },
  {
    path: '/knowledge/categories/:categoryId',
    element: (
      <RequireAuth>
        <CategoryEntriesPage />
      </RequireAuth>
    ),
  },
  {
    path: '/knowledge/entries/:id',
    element: (
      <RequireAuth>
        <KnowledgeEntryPage />
      </RequireAuth>
    ),
  },
  {
    path: '/notes',
    element: (
      <RequireAuth>
        <NotesPage />
      </RequireAuth>
    ),
  },
  // 新建是**独立的一页**而不是"点一下生成一条空的"：这里以后还要长模板、标签、AI 摘要
  {
    path: '/notes/new',
    element: (
      <RequireAuth>
        <NoteNewPage />
      </RequireAuth>
    ),
  },
  {
    path: '/interview',
    element: (
      <RequireAuth>
        <InterviewPage />
      </RequireAuth>
    ),
  },
  {
    path: '/profile',
    element: (
      <RequireAuth>
        <ProfilePage />
      </RequireAuth>
    ),
  },
  {
    path: '/my-assessments',
    element: (
      <RequireAuth>
        <MyAssessmentsPage />
      </RequireAuth>
    ),
  },
])
