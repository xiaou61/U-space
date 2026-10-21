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
    // 先记下当前的进度条数，跑完页面再比。**用接口断，不用肉眼**：
    // "看起来没有增加"不能证明没有写入。
    const before = await progressCount(page)
    await page.goto('/#/practice/sql')
    await expect(page.getByRole('heading', { name: /写一段 SQL/ })).toBeVisible()

    // 页面停留与交互（编辑一下再离开），这期间任何进度写入都该被这里抓到
    const editor = page.getByTestId('sql-practice-editor')
    if (await editor.isVisible()) {
      await editor.fill('SELECT 1;')
    }
    await page.waitForTimeout(500)

    const after = await progressCount(page)
    expect(after, '自由练习不允许写 learning_progress').toBe(before)
  })
})

/** 当前用户的学习记录条数。 */
async function progressCount(page: import('@playwright/test').Page): Promise<number> {
  const response = await page.request.get('/api/v1/learning/progress?limit=500')
  expect(response.ok()).toBeTruthy()
  const json = (await response.json()) as { data?: unknown[] }
  return Array.isArray(json.data) ? json.data.length : 0
}
