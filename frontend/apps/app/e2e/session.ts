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
  /**
   * 登录态的锚点用**账号菜单**而不是主导航：WORK-016 之后 `<md` 主导航收进了抽屉，
   * `learner-nav-knowledge` 在手机上不可见——还拿它当锚点的话，移动 project 里每条用例
   * 都会以为"没登录"而去填表单。账号菜单在两种宽度下都在页头。
   */
  const signedIn = page.getByTestId('account-menu-trigger')

  // **等一个确定状态**，不能只问"有没有登录表单"：带会话进来时 App 启动有一段"加载中"，
  // 那一刻既没有表单也还没进站，问得太早会直接返回、后续断言全部落空（实测 16 条红）。
  await expect(identifier.or(signedIn)).toBeVisible({ timeout: 20_000 })
  if (await signedIn.isVisible()) {
    return
  }
  await identifier.fill(LEARNER.identifier)
  await page.getByTestId('login-password').fill(LEARNER.password)
  await page.getByTestId('login-submit').click()
  await expect(page.getByTestId('login-submit')).toHaveCount(0)
  // 登录成功后 `LoginPage` 还会 `navigate('/')` 一次。等它落地再返回，否则调用方紧接着的
  // 深链 `goto` 会被这次跳转覆盖回首页——2026-10-17 整套跑里偶发落到首页、单跑不复现。
  await expect(page).toHaveURL(/#\/$/)
}
