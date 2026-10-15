import { expect, type Page } from '@playwright/test'

import { LEARNER } from './env'

/**
 * 登录，或复用已经登录的会话。
 *
 * <p>**线上有登录限流**：每条用例各登一次，跑一轮就会撞 429（实测 18 条用例把 e2e-learner 打到
 * 429）。所以正常路径是 `global-setup.ts` 登录一次、把 storageState 交给所有用例复用；
 * 这个函数只在真的没有会话时才走登录表单。
 */
export async function signIn(page: Page): Promise<void> {
  await page.goto('/')
  const identifier = page.getByTestId('login-identifier')
  if ((await identifier.count()) === 0) {
    return
  }
  await identifier.fill(LEARNER.identifier)
  await page.getByTestId('login-password').fill(LEARNER.password)
  await page.getByTestId('login-submit').click()
  await expect(page.getByTestId('login-submit')).toHaveCount(0)
}
