import { Navigate, createHashRouter } from 'react-router'

import { AdminHomePage } from './pages/AdminHomePage'
import { AiCreditsAuditPage } from './pages/AiCreditsAuditPage'
import { AiRunsListPage } from './pages/AiRunsListPage'
import { AiRunsOverviewPage } from './pages/AiRunsOverviewPage'
import { AiCreditsGrantPage } from './pages/AiCreditsGrantPage'
import { KnowledgeCategoriesPage } from './pages/KnowledgeCategoriesPage'
import { KnowledgeConceptsPage } from './pages/KnowledgeConceptsPage'
import { KnowledgeEntriesPage } from './pages/KnowledgeEntriesPage'
import { NoteReportsPage } from './pages/NoteReportsPage'
import { OpsPage } from './pages/OpsPage'
import { RequireAdmin } from './require-admin'

/**
 * 管理端路由。
 *
 * <p>用 Hash 路由是为了与学习者端保持一致（同一套约定、同一份经验）；管理端只在 Web 上跑，
 * 所以这里不是被桌面壳逼的，而是"两端不要各有一套路由习惯"。
 *
 * <p>每个页面都包在 `RequireAdmin` 里，门只有一处。
 *
 * <p>分类与条目各占一条路由（对应侧栏两个菜单项）。`/knowledge` 留一条转向条目的重定向：
 * 拆页之前只有这一个地址，留着免得已经打开的旧标签刷新后是一片空白。
 */
export const router = createHashRouter([
  {
    path: '/',
    element: (
      <RequireAdmin>
        <AdminHomePage />
      </RequireAdmin>
    ),
  },
  { path: '/knowledge', element: <Navigate to="/knowledge/entries" replace /> },
  {
    path: '/knowledge/categories',
    element: (
      <RequireAdmin>
        <KnowledgeCategoriesPage />
      </RequireAdmin>
    ),
  },
  {
    path: '/knowledge/concepts',
    element: (
      <RequireAdmin>
        <KnowledgeConceptsPage />
      </RequireAdmin>
    ),
  },
  {
    path: '/reports',
    element: (
      <RequireAdmin>
        <NoteReportsPage />
      </RequireAdmin>
    ),
  },
  {
    path: '/knowledge/entries',
    element: (
      <RequireAdmin>
        <KnowledgeEntriesPage />
      </RequireAdmin>
    ),
  },
  // AI 积分（WORK-009）。`/ai-credits` 留一条转向：父菜单只分组、不指向页面，
  // 但没有它时手敲这个地址就是一片空白。
  { path: '/ai-credits', element: <Navigate to="/ai-credits/grant" replace /> },
  {
    path: '/ai-credits/grant',
    element: (
      <RequireAdmin>
        <AiCreditsGrantPage />
      </RequireAdmin>
    ),
  },
  // AI 调用统计（WORK-022）。父项只分组，子项才是页面。
  { path: '/ai-runs', element: <Navigate to="/ai-runs/overview" replace /> },
  {
    path: '/ai-runs/overview',
    element: (
      <RequireAdmin>
        <AiRunsOverviewPage />
      </RequireAdmin>
    ),
  },
  {
    path: '/ai-runs/list',
    element: (
      <RequireAdmin>
        <AiRunsListPage />
      </RequireAdmin>
    ),
  },
  // 运维面板（WORK-028）。单页，不需要父项分组；`/ops` 一条转向只是为了
  // 手敲地址时不落到空白。
  { path: '/ops', element: <Navigate to="/ops/panel" replace /> },
  {
    path: '/ops/panel',
    element: (
      <RequireAdmin>
        <OpsPage />
      </RequireAdmin>
    ),
  },
  {
    path: '/ai-credits/audit',
    element: (
      <RequireAdmin>
        <AiCreditsAuditPage />
      </RequireAdmin>
    ),
  },
])
