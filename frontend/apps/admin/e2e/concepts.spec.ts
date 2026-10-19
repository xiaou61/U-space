import { expect, test, type Page } from '@playwright/test'

import { ADMIN, ADMIN_ENTRY } from './env'

/**
 * 管理端「概念」页的端到端检查（WORK-021）。
 *
 * <p>这一页是**整个知识图谱的入口**：概念表为空，学习者的图谱就是空的。所以这里验的是
 * "管理员能建出概念、能删掉"，而不是图的渲染——后者在学习者端 `graph.spec.ts` 里验。
 *
 * <p>数据用带时间戳的唯一名字，跑完删掉：概念名称全局唯一，重跑不能让上一次的残留顶掉。
 */

async function signIn(page: Page) {
  await page.goto(ADMIN_ENTRY)
  await expect(page.getByTestId('admin-sign-in')).toBeVisible()
  await page.getByTestId('login-identifier').fill(ADMIN.identifier)
  await page.getByTestId('login-password').fill(ADMIN.password)
  await page.getByTestId('login-submit').click()
  await expect(page.getByTestId('admin-console')).toBeVisible()
}

test.describe('概念维护', () => {
  test('侧栏能进概念页，建一个概念再删掉', async ({ page }) => {
    await signIn(page)

    // 直接进路由：侧栏的子菜单默认收起，靠点击展开会让用例依赖动画时序
    await page.goto(`${ADMIN_ENTRY}#/knowledge/concepts`)
    await expect(page.getByRole('heading', { name: '概念', level: 1 })).toBeVisible({ timeout: 15_000 })

    const name = `e2e 概念 ${Date.now()}`
    await page.getByTestId('concept-create').click()

    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()
    await dialog.getByLabel('名称').fill(name)
    await dialog.getByLabel(/说明/).fill('e2e 自动创建')
    await page.getByTestId('concept-save').click()

    // 建完出现在列表里
    const row = page.locator('tr', { hasText: name }).first()
    await expect(row).toBeVisible({ timeout: 15_000 })

    // 删掉，避免污染共享库：行内最后一个按钮是删除
    await row.getByRole('button').last().click()
    await page.getByTestId('admin-delete-confirm').click()
    await expect(page.locator('tr', { hasText: name })).toHaveCount(0, { timeout: 15_000 })
  })
})
