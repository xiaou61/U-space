import { expect, test } from '@playwright/test'

import { signIn } from './session'

/**
 * SQL 自由练习（WORK-032）。
 *
 * <p>只断三件不依赖沙箱具体状态的事：
 * <ol>
 *   <li>**入口能到**——从首页卡片点进去，落在 `/practice/sql`；</li>
 *   <li>**页面结构成立**——有编辑器与运行按钮，或者明确说明"暂不可用"；</li>
 *   <li>**不写学习进度**——这一条是 AC-011，必须用接口断，不能靠肉眼。练习是"不计分"的，
 *       一旦它偷偷写进度，用户会莫名其妙看到某个关卡被点亮。</li>
 * </ol>
 *
 * <p>**不断言真的跑出一条 SQL**：沙箱开关、镜像、每日配额都是环境状态，线上也不一定开着。
 * 那些由 `SandboxIntegrationTest` 与人工验证覆盖。这里只保证界面不会把用户带进死路。
 */
test.describe('SQL 自由练习', () => {
  test.beforeEach(async ({ page }) => {
    await signIn(page)
  })

  test('首页入口进得去，页面有编辑器或明确说明不可用', async ({ page }) => {
    await page.goto('/#/')
    const entry = page.getByTestId('home-entry-sql-practice')
    await expect(entry).toBeVisible()
    await entry.click()

    await expect(page).toHaveURL(/#\/practice\/sql/)
    await expect(page.getByRole('heading', { name: /写一段 SQL/ })).toBeVisible()

    // 两种合法形态：可用（有编辑器 + 运行按钮）或不可用（有明确提示）。
    // 不能两种都没有——那说明页面白屏了。
    const editor = page.getByTestId('sql-practice-editor')
    const unavailable = page.getByTestId('sql-practice-unavailable')
    await expect(editor.or(unavailable)).toBeVisible({ timeout: 15_000 })
  })

  test('练习不写学习进度（AC-011）', async ({ page }) => {
    /**
     * **用网络层证据，不用数据库条数**。
     *
     * <p>一开始写的是"查一次进度条数，练完再查一次，应当不变"。这条路走不通：
     * 这个应用是纯 Bearer + refresh 轮换，测试进程里拿不到访问令牌（`page.request` 401），
     * 而从页面里用 refresh 令牌换也会因为轮换后过期而 401（两种都实测过）。
     *
     * <p>更直接也更强的做法：**监视页面发出的请求**。学习进度只会通过
     * `PUT /api/v1/learning/entries/{id}` 写入，只要整个练习过程里一个都没发出去，
     * 就不可能有进度被写——这比"条数没变"更接近要证明的事。
     */
    const writes: string[] = []
    page.on('request', (request) => {
      const url = request.url()
      if (request.method() !== 'GET' && /\/api\/v1\/learning\//.test(url)) {
        writes.push(`${request.method()} ${url}`)
      }
    })

    await page.goto('/#/practice/sql')
    await expect(page.getByRole('heading', { name: /写一段 SQL/ })).toBeVisible()

    // 编辑一下并等渲染稳定：这期间任何进度写入都该被上面那个监听抓到
    const editor = page.getByTestId('sql-practice-editor')
    if (await editor.isVisible()) {
      await editor.fill('SELECT 1;')
      await expect(editor).toHaveValue('SELECT 1;')
    }
    await page.waitForTimeout(500)

    expect(writes, '自由练习不允许写 learning_progress').toEqual([])
  })
})
