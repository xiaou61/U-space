import { expect, test } from '@playwright/test'

import { signIn } from './session'

/**
 * 悬浮助手壳的端到端检查（AC-001 的界面一半，外加 WORK-009 的积分闸与积分面板）。
 *
 * <p>**不调真实模型**：本环境不该花谁的钱。所以这里只验"用户看得见、点得动、并且不会误触发
 * 调用"这三点——生成本身由后端的集成测试用桩覆盖。
 *
 * <p>2026-10-16 按 WORK-009 改写：凭据改由**平台**持有（用户不再配置 Key），于是
 * "没配凭据"这个状态对用户侧不再可达。取而代之要验的是两件新事：
 * ① **积分闸**——种子账号余额为 0，发送会在调模型**之前**被拒（所以这条不需要上游，也不产生费用）；
 * ② **积分面板**——能看到余额与空状态。
 */

test('壳常驻全站；不在条目页时说清"打开一条内容后可用"', async ({ page }) => {
  await signIn(page)
  await page.goto('/#/knowledge')

  await expect(page.getByTestId('assistant-launcher')).toBeVisible()
  await page.getByTestId('assistant-launcher').click()

  await expect(page.getByTestId('assistant-shell')).toBeVisible()
  // 没有条目时默认落在「对话」（助手任意处可开），精简那页才说"打开一条内容后可用"
  await expect(page.getByTestId('conversation-input')).toBeVisible()
  await page.getByTestId('assistant-tab-summary').click()
  await expect(page.getByTestId('assistant-no-entry')).toHaveText('打开一条内容后可用。')
  // 凭据由平台持有：用户侧看到的是"服务就绪没有"，不是"我配没配"
  await expect(page.getByTestId('assistant-credential-state')).toHaveText('AI 服务已就绪')
})

test('记忆面板：能看到"它记住了什么"，并说明可逐条删除', async ({ page }) => {
  await signIn(page)
  await page.goto('/#/knowledge')
  await page.getByTestId('assistant-launcher').click()
  await page.getByTestId('assistant-tab-memory').click()

  await expect(page.getByTestId('memory-panel')).toBeVisible()
  await expect(page.getByTestId('memory-panel')).toContainText('可逐条删除')
  // 种子账号没有记忆：这里断的是"空状态说得清楚"，而不是"列表里有什么"
  await expect(page.getByTestId('memory-empty')).toHaveText('还没有任何记忆。')
})

test('积分面板：能看到余额与空状态，并说明没有自动充值', async ({ page }) => {
  await signIn(page)
  await page.goto('/#/knowledge')
  await page.getByTestId('assistant-launcher').click()
  await page.getByTestId('assistant-tab-credits').click()

  await expect(page.getByTestId('credits-panel')).toBeVisible()
  // 种子账号没有被发过积分
  await expect(page.getByTestId('credits-balance')).toHaveText('0')
  await expect(page.getByTestId('credits-empty')).toHaveText('还没有任何消耗。')
  await expect(page.getByTestId('credits-panel')).toContainText('没有自动充值')
})

test('积分不足时发送给出明确提示，而不是静默失败（也不会打到上游）', async ({ page }) => {
  // 这一轮会真的发 POST：余额为 0 时后端在调模型**之前**就拒了，
  // 所以它验证的是"接线通了且失败可见"，不会产生任何上游调用与费用。
  await signIn(page)
  await page.goto('/#/knowledge')
  await page.getByTestId('assistant-launcher').click()

  await expect(page.getByTestId('conversation-context')).toHaveText('没有条目上下文，可直接提问。')

  await page.getByTestId('conversation-input').fill('解释一下导数')
  await page.getByTestId('conversation-send').click()

  await expect(page.getByTestId('conversation-error')).toContainText('积分不足')
  // 用户那句话要留在界面上：他确实说过，失败不该把它抹掉
  await expect(page.getByTestId('conversation-message-user')).toContainText('解释一下导数')
  // 没成的那一轮不留空气泡
  await expect(page.getByTestId('conversation-message-assistant')).toHaveCount(0)
})

test('打开条目后壳取到当前内容；不会偷偷发起生成，也不再出现"配置凭据"入口', async ({ page }) => {
  // 只盯 **POST**：壳打开时会发一个 GET 读已有结果，那是免费的读路径（命中缓存的意义就在这）。
  // 会花钱的只有 POST（生成），"打开条目不偷偷花钱"这条要验的正是没有 POST。
  const generateCalls: string[] = []
  page.on('request', (request) => {
    if (request.method() === 'POST' && request.url().includes('/api/v1/ai/summaries')) {
      generateCalls.push(request.url())
    }
  })

  await signIn(page)
  // 条目列表在二级页；这条用例验的是助手取上下文，不是导航
  await page.goto('/#/knowledge/entries')
  await page.getByTestId('entry-link-3').click()

  await page.getByTestId('assistant-launcher').click()
  await expect(page.getByTestId('assistant-shell')).toBeVisible()
  // 壳里显示的是当前条目的标题（上下文由条目页提供）
  await expect(page.getByTestId('assistant-no-entry')).toHaveCount(0)

  // **WORK-009：用户侧不再有"配置凭据"这个入口**——Key 由平台持有，用户没有可填的地方
  await expect(page.getByTestId('assistant-open-credential')).toHaveCount(0)
  await expect(page.getByTestId('assistant-api-key')).toHaveCount(0)

  // 关键：整条流程一次生成都没有发出（打开条目不会偷偷花钱）
  expect(generateCalls).toEqual([])
})
