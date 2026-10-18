import { expect, test } from '@playwright/test'

import { signIn } from './session'

/**
 * 简历的编辑主链路（WORK-018 AC-002 的 e2e 部分）。
 *
 * <p>只验三件事，因为这三件是这一页存在的前提：
 * <ol>
 *   <li>手工新建一份简历能进编辑器</li>
 *   <li>改一个字段后**刷新页面仍在新值**（自动保存真的落地了）</li>
 *   <li>体检清单会随内容出现</li>
 * </ol>
 *
 * <p>**不测需要模型调用的路径**（上传识别、按岗位优化）：那要真打上游、花积分，且结果不确定。
 * 那两条由后端集成测试与人工验收覆盖；e2e 的价值在"界面与后端接得通"，
 * 而自动保存正好是这条线上最容易断的一环。
 */
test.describe('简历编辑', () => {
  test('新建 → 改名字 → 刷新后仍在', async ({ page }) => {
    await signIn(page)

    await page.goto('/#/resume')
    await expect(page.getByRole('heading', { name: '我的简历' })).toBeVisible()

    // 走"手工新建"：它不需要上传文件，也不消耗任何模型调用
    await page.goto('/#/resume/create')
    const titleInput = page.locator('#resume-title')
    const unique = `e2e-简历-${Date.now()}`
    await titleInput.fill(unique)
    await page.getByRole('button', { name: '创建并开始编辑' }).click()

    // 进编辑器
    await expect(page.getByTestId('resume-section-basics')).toBeVisible()

    // 改姓名
    const nameInput = page.locator('label', { hasText: '姓名' }).locator('input')
    const name = `姓名-${Date.now()}`
    await nameInput.fill(name)

    // 等自动保存落地（防抖 800ms + 一次 PUT）
    await expect(page.getByTestId('resume-save-status')).toContainText('已保存', { timeout: 15_000 })

    // 刷新后仍是新值 —— 这一条才是"自动保存真的生效"的证据
    await page.reload()
    await expect(page.getByTestId('resume-section-basics')).toBeVisible()
    await expect(page.locator('label', { hasText: '姓名' }).locator('input')).toHaveValue(name, {
      timeout: 15_000,
    })

    // 体检清单存在（内容随简历状态变化，这里只断言它渲染了）
    await expect(page.getByTestId('resume-checkup')).toBeVisible()
  })
})
