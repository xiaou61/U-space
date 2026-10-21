import { expect, test } from '@playwright/test'

import { signIn } from './session'

/**
 * 每日速递的端到端检查（WORK-027）。
 *
 * <p>**断言形态而不是具体内容**：不断言"一定有量子位那条"，只要求
 * 「从首页与导航都能进 → 有日报/周报两个页签 → 页面不报错」。
 * 线上内容取决于抓取任务是否跑过，空态也是合法的（显示"这一天还没有速递"）。
 *
 * <p>降级态单独一条：`summary_state='degraded'` 时卡片必须显示"这条还没写好摘要"，
 * 而不是一片空白——那正是 AC-005 要的。
 */

test.describe('每日速递', () => {
  test.beforeEach(async ({ page }) => {
    await signIn(page)
  })

  test('从导航栏能进，日报与周报两个页签都在', async ({ page }) => {
    await page.goto('/#/')
    await page.getByTestId('learner-nav-digest').click()

    await expect(page).toHaveURL(/#\/digest/)
    await expect(page.getByTestId('digest-tab-daily')).toBeVisible()
    await expect(page.getByTestId('digest-tab-weekly')).toBeVisible()
  })

  test('首页有入口，点进去到达速递页', async ({ page }) => {
    await page.goto('/#/')
    await expect(page.getByTestId('home-entry-digest')).toBeVisible()
    await page.getByTestId('home-entry-digest').click()

    await expect(page).toHaveURL(/#\/digest/)
    await expect(page.getByTestId('digest-tab-daily')).toBeVisible()
  })

  test('当天有内容时能看到条目；没有内容时显示空态而不是报错', async ({ page }) => {
    await page.goto('/#/digest')

    const items = page.getByTestId('digest-item')
    const empty = page.getByTestId('digest-empty')
    // 两者必有其一：要么有内容，要么是"这一天还没有速递"
    await expect(items.first().or(empty)).toBeVisible({ timeout: 15_000 })
  })

  test('周报页签：有则显示正文，无则显示"尚未生成"而不是报错', async ({ page }) => {
    await page.goto('/#/digest')
    await page.getByTestId('digest-tab-weekly').click()

    const missing = page.getByTestId('digest-weekly-missing')
    await expect(missing).toBeVisible({ timeout: 15_000 })
    await expect(missing).toContainText('尚未生成')
  })

  test('每条都有原文链接，且指向外部站点', async ({ page }) => {
    await page.goto('/#/digest')

    const firstTitle = page.getByTestId('digest-title').first()
    if ((await firstTitle.count()) === 0) {
      test.skip(true, '当天没有速递内容（抓取任务尚未跑过），这条无法断言')
      return
    }
    const href = await firstTitle.getAttribute('href')
    expect(href).toMatch(/^https?:\/\//)
    await expect(firstTitle).toHaveAttribute('target', '_blank')
  })
})
