import { expect, test, type Page } from '@playwright/test'

import { signIn } from './session'

/**
 * 从零开始的端到端检查（WORK-024）。
 *
 * <p><b>形态已经不是知识库</b>：`/learn` 列的是**轨道**，点进去是**关卡地图**，
 * 再点是关卡正文。这条链路正是用户要的"闯关"形态，所以 e2e 也要按这个走，
 * 不再断言旧的"线卡片"。
 *
 * <p><b>断言规则而不是课名</b>：不断言"一定有 Java"，只要求"至少有一条轨道、
 * 能进地图、地图里有可点的关卡"。线上内容没导入时那条轨道会显示"内容准备中"，
 * 但页面结构仍然成立。
 */

/** 第一张轨道卡。 */
function firstTrack(page: Page) {
  return page.locator('[data-testid^="learn-track-"]').first()
}

test.describe('从零开始', () => {
  test.beforeEach(async ({ page }) => {
    await signIn(page)
  })

  test('首页入口进得去，轨道卡由服务端渲染', async ({ page }) => {
    await page.goto('/#/')
    await expect(page.getByTestId('home-entry-learn')).toBeVisible()
    await page.getByTestId('home-entry-learn').click()

    await expect(page).toHaveURL(/#\/learn/)
    await expect(page.getByRole('heading', { name: /挑一条轨道/ })).toBeVisible()

    // 轨道集合来自服务端：只要求"至少有一条"
    await expect(firstTrack(page)).toBeVisible({ timeout: 15_000 })
  })

  test('点轨道进入关卡地图，地图里有关卡', async ({ page }) => {
    await page.goto('/#/learn')
    const track = firstTrack(page)
    await expect(track).toBeVisible({ timeout: 15_000 })
    await track.click()

    // 地图页：URL 带 slug，且渲染出关卡格子
    await expect(page).toHaveURL(/#\/learn\/[a-z0-9-]+/)
    await expect(page.getByTestId('track-map')).toBeVisible({ timeout: 15_000 })
    await expect(page.locator('[data-testid^="stage-"]').first()).toBeVisible()
  })

  test('关卡状态可辨：至少有 current 或 done，locked 不可点', async ({ page }) => {
    await page.goto('/#/learn')
    await firstTrack(page).click()
    await expect(page.getByTestId('track-map')).toBeVisible({ timeout: 15_000 })

    const stages = page.locator('[data-testid^="stage-"]')
    const count = await stages.count()
    expect(count).toBeGreaterThan(0)

    const statuses = await stages.evaluateAll((nodes) =>
      nodes.map((node) => node.getAttribute('data-status')),
    )
    // 一进地图，第一关应该是 current（或已做过则 done），其余可能是 locked
    expect(statuses.some((status) => status === 'current' || status === 'done')).toBe(true)

    // 锁住的关卡是 div 而不是链接：点了不该跳走
    const locked = page.locator('[data-status="locked"]').first()
    if ((await locked.count()) > 0) {
      expect(await locked.evaluate((node) => node.tagName)).toBe('DIV')
    }
  })

  test('从可进入的关卡点到正文', async ({ page }) => {
    await page.goto('/#/learn')
    await firstTrack(page).click()
    await expect(page.getByTestId('track-map')).toBeVisible({ timeout: 15_000 })

    const open = page.locator('[data-status="current"], [data-status="done"]').first()
    await expect(open).toBeVisible()
    await open.click()

    await expect(page).toHaveURL(/#\/knowledge\/entries\/\d+/)
    await expect(page.getByTestId('markdown-body')).toBeVisible({ timeout: 20_000 })
  })

  test('难度可见且能筛选：入门与深入分得开', async ({ page }) => {
    await page.goto('/#/learn')
    await firstTrack(page).click()
    await expect(page.getByTestId('track-map')).toBeVisible({ timeout: 15_000 })

    // 每关都有难度徽章，且文案是大白话
    const badges = page.locator('[data-testid^="stage-level-"]')
    expect(await badges.count()).toBeGreaterThan(0)
    const labels = await badges.evaluateAll((nodes) =>
      nodes.map((node) => node.textContent?.trim() ?? ''),
    )
    expect(labels.some((label) => label === '入门' || label === '进阶' || label === '深入')).toBe(true)

    // 筛选"入门"后，剩下的关卡都必须是入门
    const beginnerFilter = page.getByTestId('level-filter-beginner')
    if ((await beginnerFilter.count()) > 0) {
      const before = await page.locator('[data-testid^="stage-"]').count()
      await beginnerFilter.click()
      const after = await page.locator('[data-testid^="stage-"]').count()
      expect(after).toBeLessThanOrEqual(before)
      expect(after).toBeGreaterThan(0)
      const levels = await page.locator('[data-testid^="stage-level-"]').evaluateAll((nodes) =>
        nodes.map((node) => node.textContent?.trim() ?? ''),
      )
      expect(levels.every((label) => label === '入门')).toBe(true)
    }
  })

  test('轨道与地图在 360px 下不横向溢出', async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 780 })
    await page.goto('/#/learn')
    await firstTrack(page).waitFor({ timeout: 15_000 })
    let overflow = await page.evaluate(
      () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
    )
    expect(overflow).toBeLessThanOrEqual(1)

    await firstTrack(page).click()
    await page.getByTestId('track-map').waitFor({ timeout: 15_000 })
    overflow = await page.evaluate(
      () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
    )
    expect(overflow).toBeLessThanOrEqual(1)
  })
})
