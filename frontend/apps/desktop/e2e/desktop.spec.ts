import { _electron as electron, expect, test } from '@playwright/test'

/**
 * 桌面端从源码启动后的最小验收。
 *
 * <p>原来断言的是首页「运行状态」那一段（平台徽章 + 健康检查 + 当前身份），那段已按用户要求
 * 删掉（2026-10-15）。这里把三件事重新落到**仍然存在的东西**上：
 * ① 宿主标识改读 `<html data-platform>`（它从界面挪到了元数据，见 `main.tsx`）；
 * ② "后端真的连得上"由登录门自己说明——桌面端没有登录流程，所以必然是登录页；
 * ③ 登录页能渲染出来，本身就说明这套前端在 Electron 里跑起来了。
 */
test('桌面端从源码启动后使用桌面平台实现，并停在登录门', async () => {
  const app = await electron.launch({ args: ['.'] })
  try {
    const window = await app.firstWindow()

    // 平台实现必须是 desktop：说明组合根按构建模式选对了宿主
    await expect(window.locator('html')).toHaveAttribute('data-platform', 'desktop')

    // 没有登录流程就没有令牌，因此停在登录门（不是白屏、也不是半渲染的内容页）
    await expect(window.getByTestId('login-form')).toBeVisible()
    await expect(window.getByTestId('learner-nav-knowledge')).toHaveCount(0)
  } finally {
    await app.close()
  }
})
