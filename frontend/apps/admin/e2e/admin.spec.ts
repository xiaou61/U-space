import { expect, test, type Page } from '@playwright/test'

import { ADMIN, ADMIN_ENTRY, LEARNER } from './env'

/**
 * 管理端的端到端检查。
 *
 * <p>三条最小可信检查：未登录看到登录视图、非管理员被明确拒绝、管理员放行；
 * 以及被拒状态下仍能证明接口链路是通的（后端状态徽标取真实返回）。
 *
 * <p>账号默认取 db/devdata 的种子（只在本地跑时存在），指向已部署实例时由环境变量给出，
 * 见 `e2e/accounts.ts`。注册产生的账号一律是学习者，所以放行的只有管理员那一个。
 */

async function signIn(page: Page, account: { identifier: string; password: string }) {
  await page.goto(ADMIN_ENTRY)
  await expect(page.getByTestId('admin-sign-in')).toBeVisible()
  await page.getByTestId('login-identifier').fill(account.identifier)
  await page.getByTestId('login-password').fill(account.password)
  await page.getByTestId('login-submit').click()
}

test('未登录时显示登录视图，而不是管理区', async ({ page }) => {
  await page.goto(ADMIN_ENTRY)

  await expect(page.getByTestId('admin-sign-in')).toBeVisible()
  await expect(page.getByTestId('admin-console')).toHaveCount(0)
  await expect(page.getByTestId('admin-denied')).toHaveCount(0)
})

test('学习者登录后被明确拒绝，并显示当前身份', async ({ page }) => {
  await signIn(page, LEARNER)

  await expect(page.getByTestId('admin-denied')).toBeVisible()
  await expect(page.getByTestId('admin-console')).toHaveCount(0)
  await expect(page.getByText('无管理权限')).toBeVisible()
  await expect(page.getByTestId('admin-identity')).toHaveText('学习者')
})

test('管理员登录后进入管理区', async ({ page }) => {
  await signIn(page, ADMIN)

  await expect(page.getByTestId('admin-console')).toBeVisible()
  await expect(page.getByTestId('admin-denied')).toHaveCount(0)
  await expect(page.getByTestId('admin-current-user')).toHaveText(ADMIN.identifier)
})

test('被拒页面上仍显示后端真实状态', async ({ page }) => {
  await signIn(page, LEARNER)

  // 徽标取的是 /actuator/health 的真实返回，不是硬编码；被拒不等于什么都连不上
  await expect(page.getByTestId('admin-health-status')).toHaveText('UP')
})

test('静态资源用相对路径，产物可放在任意来源下', async ({ page }) => {
  const response = await page.goto(ADMIN_ENTRY)
  expect(response?.status()).toBe(200)

  const html = await page.content()
  expect(html).not.toContain('src="/assets/')
})

test('举报处理页：从侧栏进得去，空列表也说清楚', async ({ page }) => {
  await signIn(page, ADMIN)
  await expect(page.getByTestId('admin-console')).toBeVisible()

  // 叶子菜单项的可访问名就是它的标签；带子菜单的那些才有 admin-menu-* 的 testid
  await page.getByRole('link', { name: '举报处理' }).click()
  await expect(page.getByRole('heading', { name: '举报处理' })).toBeVisible()
  // 本地种子库没有举报：这里断的是"页面接上了、空状态说得清楚"，
  // 举报→列表→下架→链接失效那条链路由后端集成用例覆盖（NoteReportIntegrationTest）
  await expect(page.getByTestId('reports-empty')).toBeVisible()
})

/**
 * SQL 数据集治理（WORK-035 T-11）。
 *
 * <p>两页都只断"从侧栏进得去、空状态说得清楚"——本地种子库没有社区数据集。
 * 真正的发布 → 举报 → 下架链路要真实数据，由后端集成用例覆盖。
 */
test('SQL 数据集管理页：从侧栏进得去，空列表也说清楚', async ({ page }) => {
  await signIn(page, ADMIN)
  await expect(page.getByTestId('admin-console')).toBeVisible()

  await page.getByTestId('admin-menu-SQL 数据集').click()
  await page.getByRole('link', { name: '数据集管理' }).click()

  await expect(page.getByRole('heading', { name: 'SQL 数据集' })).toBeVisible()
  await expect(page.getByTestId('sql-datasets-empty')).toBeVisible()
})

test('SQL 数据集举报页：进得去，空列表也说清楚', async ({ page }) => {
  await signIn(page, ADMIN)
  await expect(page.getByTestId('admin-console')).toBeVisible()

  await page.getByTestId('admin-menu-SQL 数据集').click()
  // 这个子项与笔记举报同名，所以按 href 定位而不是按名字
  await page.locator('a[href="#/sql-datasets/reports"]').click()

  await expect(page.getByTestId('sql-dataset-reports-empty')).toBeVisible()
})
