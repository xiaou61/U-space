import { expect, test, type Page } from '@playwright/test'

import { signIn } from './session'

/**
 * 学习线的端到端检查（WORK-020）。
 *
 * <p><b>断言"规则"而不是"某一门课的名字"</b>：这张卡列表**由服务端返回的线渲染**，
 * 加一门语言不会改前端。所以这里验的是"线的集合非空、每张卡都能点进去、进度能读回"，
 * 而不是"一定有 Python 和 Java"——后者在线上内容还没导入时必然红。
 *
 * <p>**不做"做完题看到通过"的整链路 e2e**：那需要一门完整课程 + 逐题作答，而本地种子里
 * 没有课程内容（课程由导入脚本写入开发库）。通过判定由 {@code LearningProgressIntegrationTest}
 * 在接口层覆盖（9 条），这里只验"页面能进、能读、不溢出"。</p>
 */

/** 第一张学习线卡片。 */
function firstLineCard(page: Page) {
  return page.locator('[data-testid^="learn-line-"]').first()
}

test.describe('学习线', () => {
  test.beforeEach(async ({ page }) => {
    await signIn(page)
  })

  test('首页入口进得去，卡片由服务端渲染', async ({ page }) => {
    await page.goto('/#/')
    await expect(page.getByTestId('home-entry-learn')).toBeVisible()
    await page.getByTestId('home-entry-learn').click()

    await expect(page).toHaveURL(/#\/learn/)
    await expect(page.getByRole('heading', { name: /挑一条线/ })).toBeVisible()

    // 线的集合来自服务端：这里不断言具体是哪几门，只要求"至少有一条，且带进度字段"
    const card = firstLineCard(page)
    await expect(card).toBeVisible({ timeout: 15_000 })
    const stations = await card.getAttribute('data-stations')
    const passed = await card.getAttribute('data-passed')
    expect(Number(stations)).toBeGreaterThan(0)
    expect(Number(passed)).toBeGreaterThanOrEqual(0)
  })

  test('点卡片进入第一站，进度记录可读', async ({ page }) => {
    await page.goto('/#/learn')
    const card = firstLineCard(page)
    await expect(card).toBeVisible({ timeout: 15_000 })

    await card.getByRole('link').first().click()
    await expect(page).toHaveURL(/#\/knowledge\/entries\/\d+/)
    // 条目页会真的取一次学习记录（GET /learning/entries/{id}），之后才渲染正文
    await expect(page.getByTestId('markdown-body')).toBeVisible({ timeout: 20_000 })
  })

  test('课程条目页在 360px 下不横向溢出', async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 780 })
    await page.goto('/#/learn')
    await firstLineCard(page).waitFor({ timeout: 15_000 })

    const overflow = await page.evaluate(
      () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
    )
    expect(overflow).toBeLessThanOrEqual(1)
  })

  test('已有的源码解读被认成一条线，没有重复建（AC-018）', async ({ page }) => {
    // 这条一度只在本地成立（`source-reading` 原本只是 devdata 种子）。
    // 2026-10-19 已作为内容源（`content/reading/source/`）导入生产库，线上也有了，
    // 所以不再跳过——它现在正是"线上真的把 reading 线渲染出来了"的证据。
    await page.goto('/#/learn')
    await firstLineCard(page).waitFor({ timeout: 15_000 })

    // 种子里 source-reading 分类被 V17 标成 reading；它必须出现在**同一个清单**的
    // "读源码与专题"分组里，而不是另开一套专题入口。断言按分组 testid 与卡片 slug，
    // 不读接口：这条要证的正是"界面真的把它当线渲染了"。
    const readings = page.getByTestId('learn-readings')
    await expect(readings).toBeVisible()
    await expect(readings.locator('[data-testid="learn-line-source-reading"]')).toBeVisible()
    // 也不该被复制成第二条：同一 slug 只出现一次
    await expect(page.locator('[data-testid="learn-line-source-reading"]')).toHaveCount(1)
  })
})
