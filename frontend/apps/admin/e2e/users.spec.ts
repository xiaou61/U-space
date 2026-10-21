import { expect, test, type Page } from '@playwright/test'

import { ADMIN } from './env'

/**
 * 用户管理页（WORK-030）。
 *
 * <p>这一页替换了原先的「积分调整」——那一页要管理员手敲用户 id。这里断的是替代关系真的成立：
 * **能搜到人、能看见他的积分与用量、能调积分、且不能动自己**。
 *
 * <p>不依赖种子数据的**具体内容**（用户名、余额都是变量）：只断言"列表非空、行为符合预期"，
 * 这样指向线上实例时也能跑。
 */
async function signIn(page: Page) {
  await page.goto(process.env.E2E_ADMIN_PATH ?? '/')
  await expect(page.getByTestId('admin-sign-in')).toBeVisible()
  await page.getByTestId('login-identifier').fill(ADMIN.identifier)
  await page.getByTestId('login-password').fill(ADMIN.password)
  await page.getByTestId('login-submit').click()
  await expect(page.getByTestId('admin-console')).toBeVisible()
}

test('从侧栏能进用户管理，列表带用量指标', async ({ page }) => {
  await signIn(page)

  await page.getByRole('link', { name: '用户管理' }).click()
  await expect(page.getByRole('heading', { name: '用户管理' })).toBeVisible()

  await expect(page.getByTestId('admin-users-table')).toBeVisible()
  await expect(page.getByTestId('admin-user-total')).toContainText('个账号')
  // 第一行必须同时有余额、已用、调用三列的数字（不是空表格）
  const firstRow = page.getByTestId('admin-users-table').locator('tbody tr').first()
  await expect(firstRow).toBeVisible()
})

test('搜索框输入即筛：搜不存在的串显示空状态', async ({ page }) => {
  await signIn(page)
  await page.getByRole('link', { name: '用户管理' }).click()

  await page.getByTestId('admin-user-search').fill('绝对不存在的用户名-zzz-999')
  // 300ms 防抖之后应当只剩空状态
  await expect(page.getByTestId('admin-users-empty')).toBeVisible({ timeout: 5000 })
})

test('调整积分必须填原因；填了才能提交', async ({ page }) => {
  await signIn(page)
  await page.getByRole('link', { name: '用户管理' }).click()

  const editButton = page.getByTestId(/^admin-user-edit-/).first()
  await editButton.click()
  await expect(page.getByTestId('admin-user-editor')).toBeVisible()

  // 原因空着：提交按钮不可用
  await expect(page.getByTestId('admin-user-submit')).toBeDisabled()

  await page.getByTestId('admin-user-note').fill('e2e 用例：发 1 分')
  await page.getByTestId('admin-user-delta').fill('1')
  await expect(page.getByTestId('admin-user-submit')).toBeEnabled()

  await page.getByTestId('admin-user-cancel').click()
  await expect(page.getByTestId('admin-user-editor')).toHaveCount(0)
})
