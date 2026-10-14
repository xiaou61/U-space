import { expect, test, type Page } from '@playwright/test'

/**
 * 学习者端的端到端检查。
 *
 * <p>**登录成了所有断言的入口**：整个学习者端要求登录，所以每个用例都以"登录种子账号"开场。
 * 原来那条"受保护接口在浏览器里确实被保护"的断言换了个形式：现在浏览器里已经带着令牌，
 * 因此用**不带令牌的直连请求**来证明接口仍然受保护——那才是这条断言真正要说的事。
 *
 * <p>种子账号来自 db/devdata（只有测试会加载它），口令是固定的测试值。
 */

const SEED_LEARNER = { identifier: 'seed-learner', password: 'seed-learner-password' }

async function signIn(page: Page) {
  await page.goto('/')
  await expect(page.getByTestId('login-form')).toBeVisible()
  await page.getByTestId('login-identifier').fill(SEED_LEARNER.identifier)
  await page.getByTestId('login-password').fill(SEED_LEARNER.password)
  await page.getByTestId('login-submit').click()
}

test('未登录访问学习者端会被导向登录页，登录后进入首页', async ({ page }) => {
  await page.goto('/')

  // 未登录时看到的是登录表单，而不是任何内容
  await expect(page.getByTestId('login-form')).toBeVisible()
  await expect(page.getByTestId('health-status')).toHaveCount(0)

  await signIn(page)

  // 登录后能看到原本要去的首页，并显示当前用户
  await expect(page.getByTestId('current-user')).toHaveText(SEED_LEARNER.identifier)
})

test('首页展示后端返回的真实健康状态', async ({ page }) => {
  await signIn(page)

  // 平台能力来自 Platform 端口，说明适配器链路是通的
  await expect(page.getByTestId('platform-kind')).toHaveText('web')

  // 必须是后端真实返回：状态值与原始响应体都来自接口
  await expect(page.getByTestId('health-status')).toHaveText('UP')
  await expect(page.getByTestId('health-raw')).toContainText('"status"')
  await expect(page.getByTestId('health-raw')).toContainText('liveness')
})

test('每个请求都带上后端返回的追踪标识', async ({ page }) => {
  const response = await page.request.get('/actuator/health')

  expect(response.status()).toBe(200)
  expect(response.headers()['x-trace-id']).toBeTruthy()
})

test('受保护接口在没有令牌时被拒，带着身份时返回主体', async ({ page }) => {
  // 不带 Authorization：能拿到接口地址，不等于能读到内容
  const unauthorized = await page.request.get('/api/v1/me')
  expect(unauthorized.status()).toBe(401)

  // 带着令牌（已登录）时它返回身份——两者对照才说明授权确实在起作用
  await signIn(page)
  await expect(page.getByTestId('identity-subject')).not.toHaveText('未登录')
})

test('登出后回到登录页', async ({ page }) => {
  await signIn(page)
  await expect(page.getByTestId('current-user')).toBeVisible()

  await page.getByTestId('logout').click()

  await expect(page.getByTestId('login-form')).toBeVisible()
})

/**
 * 缺陷回归（2026-10-12）：页头以前只有用户名与登出，**没有任何通往内容的入口**——
 * 知识库那一簇页面彼此连通，却没有人从首页指向它们，用户只能手输地址。
 *
 * <p>这个盲区是测试自己造成的：此前每条用例都用 `page.goto('/#/knowledge')` 直达，
 * 于是"页面能用"一直被验证，"用户走得到"从来没有。所以这条刻意**只点不输**地址。
 */
test('登录后从页头就能进入知识库，不必手输地址', async ({ page }) => {
  await signIn(page)

  await page.getByTestId('learner-nav-knowledge').click()
  await expect(page).toHaveURL(/#\/knowledge$/)
  // 首屏是分类网格（二级结构的第一级），条目在点进去之后
  await expect(page.getByTestId('category-grid')).toBeVisible()
  await page.getByTestId('all-entries-card').click()
  await expect(page).toHaveURL(/#\/knowledge\/entries$/)
  await expect(page.getByTestId('entry-list')).toBeVisible()

  // 条目页的地址是 /knowledge/entries/:id，仍该算在"知识库"这一档里（前缀匹配）
  await page.getByTestId('entry-link-1').click()
  await expect(page).toHaveURL(/#\/knowledge\/entries\/1$/)
  await expect(page.getByTestId('learner-nav-knowledge')).toHaveAttribute('aria-current', 'page')

  await page.getByTestId('learner-nav-my-assessments').click()
  await expect(page).toHaveURL(/#\/my-assessments$/)
  await expect(page.getByTestId('learner-nav-my-assessments')).toHaveAttribute('aria-current', 'page')
})

/**
 * 首页与"点导航就变样"的回归（2026-10-13，用户报的）：
 * ① 首页此前是骨架期诊断页，进去先看到的是 health 原始 JSON，**没有任何去处**；
 * ② `我的笔记` 页当时自己拼了 div+header，没走 AppShell ⇒ 点导航时整页宽度、页头、页脚全变。
 *
 * <p>这两条都只能"点着走"才验得到，所以这里从首页卡片一路点进笔记页，并断言
 * **三页共用同一套外壳**（页脚那句是 AppShell 独有的，三页都该有）。
 */
test('首页有去处；笔记页与知识库页共用同一套外壳', async ({ page }) => {
  await signIn(page)

  await expect(page.getByTestId('home-entries')).toBeVisible()
  await expect(page.getByTestId('home-entry-knowledge')).toBeVisible()
  const shellFooter = page.getByText('Paideia · 千人千面的 AI 个性化学习平台')
  await expect(shellFooter).toBeVisible()

  // 从首页卡片进笔记页（不是手输地址）
  await page.getByTestId('home-entry-notes').click()
  await expect(page).toHaveURL(/#\/notes$/)
  await expect(page.getByTestId('notes-create')).toBeVisible()
  // 同一个外壳：页脚还在（自己拼 div 的那版没有它）
  await expect(shellFooter).toBeVisible()

  // 再从页头切到知识库，外壳不变
  await page.getByTestId('learner-nav-knowledge').click()
  await expect(page.getByTestId('category-grid')).toBeVisible()
  await expect(shellFooter).toBeVisible()
})
