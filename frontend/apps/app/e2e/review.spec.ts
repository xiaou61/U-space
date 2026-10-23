import { expect, test } from '@playwright/test'

import { REMOTE } from './env'
import { signIn } from './session'

/** 复习页与入口（WORK-034 S1）。线上没有种子进度，只验入口与页面可达。 */
test.describe('复习', () => {
  test.beforeEach(() => {
    test.skip(REMOTE, '依赖本地种子账号；线上只验过接口')
  })

  test('从「更多」进复习页，列表或空态二选一', async ({ page }) => {
    await signIn(page)
    await page.getByTestId('learner-nav-more').click()
    await page.getByTestId('learner-nav-review').click()
    await expect(page).toHaveURL(/#\/review/)

    await expect(
      page.getByTestId('review-list').or(page.getByTestId('review-empty')),
    ).toBeVisible({ timeout: 20_000 })
  })

  test('空态给出两个去处', async ({ page }) => {
    await signIn(page)
    await page.goto('/#/review')
    const empty = page.getByTestId('review-empty')
    if (await empty.isVisible()) {
      await expect(empty.getByRole('link', { name: '去从零开始' })).toHaveAttribute('href', /\/learn/)
      await expect(empty.getByRole('link', { name: '去知识库' })).toHaveAttribute('href', /\/knowledge/)
    }
  })
})