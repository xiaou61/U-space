import { expect, test, type Page } from '@playwright/test'

/**
 * 悬浮助手壳的端到端检查（AC-001 的界面一半）。
 *
 * <p>**不调真实模型**：本环境没有网络可达的上游，也不该花谁的 Key。所以这里只验"用户看得见、
 * 点得动、并且不会误触发调用"这三点——生成本身由后端的集成测试用桩覆盖（那里能证明
 * "命中缓存不再调用"）。
 *
 * <p>种子账号没有配模型凭据，这正是要验的一种状态：能读别人的公开结果、**不能**触发生成，
 * 按钮是禁用而不是报错。
 */

const SEED_LEARNER = { identifier: 'seed-learner', password: 'seed-learner-password' }

async function signIn(page: Page, account: { identifier: string; password: string }) {
  await page.goto('/')
  await expect(page.getByTestId('login-identifier')).toBeVisible()
  await page.getByTestId('login-identifier').fill(account.identifier)
  await page.getByTestId('login-password').fill(account.password)
  await page.getByTestId('login-submit').click()
}

test('壳常驻全站；不在条目页时说清"打开一条内容后可用"', async ({ page }) => {
  await signIn(page, SEED_LEARNER)
  await page.goto('/#/knowledge')

  await expect(page.getByTestId('assistant-launcher')).toBeVisible()
  await page.getByTestId('assistant-launcher').click()

  await expect(page.getByTestId('assistant-shell')).toBeVisible()
  await expect(page.getByTestId('assistant-no-entry')).toHaveText('打开一条内容后可用。')
  await expect(page.getByTestId('assistant-credential-state')).toHaveText('未配置凭据')
})

test('打开条目后壳取到当前内容；没有凭据时给配置入口而不是报错，也不会发起调用', async ({ page }) => {
  // 只盯 **POST**：壳打开时会发一个 GET 读已有结果，那是免费的读路径（命中缓存的意义就在这）。
  // 会花钱的只有 POST（生成），"打开条目不偷偷花钱"这条要验的正是没有 POST。
  const generateCalls: string[] = []
  page.on('request', (request) => {
    if (request.method() === 'POST' && request.url().includes('/api/v1/ai/summaries')) {
      generateCalls.push(request.url())
    }
  })

  await signIn(page, SEED_LEARNER)
  await page.goto('/#/knowledge')
  await page.getByTestId('entry-link-3').click()

  await page.getByTestId('assistant-launcher').click()
  await expect(page.getByTestId('assistant-shell')).toBeVisible()
  // 壳里显示的是当前条目的标题（上下文由条目页提供）
  await expect(page.getByTestId('assistant-no-entry')).toHaveCount(0)

  // 没配凭据：两个生成按钮都不可点，并说明原因
  await expect(page.getByTestId('assistant-generate-public')).toBeDisabled()
  await expect(page.getByTestId('assistant-generate-private')).toBeDisabled()
  await expect(page.getByTestId('assistant-shell')).toContainText('精简需要你自己的模型凭据')

  // 配置入口可用，且 Key 输入框是密码类型（不在屏幕上明文显示）
  await page.getByTestId('assistant-open-credential').click()
  await expect(page.getByTestId('assistant-credential-dialog')).toBeVisible()
  await expect(page.getByTestId('assistant-api-key')).toHaveAttribute('type', 'password')
  await page.keyboard.press('Escape')

  // 关键：整条流程一次生成都没有发出（打开条目不会偷偷花钱）
  expect(generateCalls).toEqual([])
})
