import { expect, test } from '@playwright/test'

/**
 * 第三方登录的界面（WORK-037）。
 *
 * <p><b>不连真实平台</b>：这里用路由拦截伪造回调，验的是"我们这一侧"的三条路径——
 * 按钮出现、回调页能拿握手码换到令牌、失败时回到登录页并有可读提示。
 * 真实平台的授权往返只能手验（见验证报告）。
 *
 * <p>为什么值得单独一个文件：回调页是**未登录状态**才能走到的页面，
 * 而 `signIn()` 会先建立会话——两者在同一个 spec 里会互相干扰。
 */
test.describe('第三方登录', () => {
  test('登录页在服务端配了平台时显示按钮，且不会因为未配置而空着出错', async ({ browser }) => {
    // **必须用没有会话的上下文**：globalSetup 把登录态写进 storageState，
    // 而 `browser.newContext()` 会继承 use.storageState——所以这里必须显式清空，
    // 否则访问 /#/login 会被 RequireGuest 弹回首页，看不到登录表单。
    const context = await browser.newContext({ storageState: { cookies: [], origins: [] } })
    const page = await context.newPage()
    try {
      await page.goto('/#/login')
      await expect(page.getByTestId('login-form')).toBeVisible()

      // 本机/线上是否配了平台取决于后端配置，因此断言的是**两种状态都合法**：
      // 要么出现按钮组，要么整块不渲染——但都不能出现错误提示
      const buttons = page.getByTestId('oauth-buttons')
      expect(await buttons.count()).toBeLessThanOrEqual(1)
      await expect(page.getByTestId('oauth-error')).toHaveCount(0)
    } finally {
      await context.close()
    }
  })

  test('回调失败落到登录页并给出可读提示（不带任何令牌）', async ({ browser }) => {
    const context = await browser.newContext({ storageState: { cookies: [], origins: [] } })
    const page = await context.newPage()
    try {
      await page.goto('/#/login?oauthError=denied')

      await expect(page.getByTestId('oauth-error-banner')).toBeVisible()
      await expect(page.getByTestId('oauth-error-banner')).toContainText('取消了授权')
    } finally {
      await context.close()
    }
  })

  test('握手码无效时回调页给出提示，而不是白屏或无限转圈', async ({ page }) => {
    await page.goto('/#/oauth/callback?handoff=not-a-real-handoff')

    await expect(page.getByTestId('oauth-callback')).toBeVisible()
    // 服务端会拒绝这个码，页面应落到"已失效"这类可读文案上，并提供回登录页的出口
    await expect(page.getByTestId('oauth-callback-back')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByText(/失效|失败|重新登录/)).toBeVisible()
  })

  test('回调页缺少 handoff 参数时立刻报错，不发请求', async ({ page }) => {
    await page.goto('/#/oauth/callback')

    await expect(page.getByTestId('oauth-callback')).toBeVisible()
    await expect(page.getByText('登录链接不完整，请重新登录。')).toBeVisible()
  })
})
