import { expect, test } from '@playwright/test'

import { REMOTE } from './env'
import { signIn } from './session'

/**
 * 身份化与模拟面试（WORK-010 T-03 / T-09 / T-12 / AC-002）。
 *
 * <p>只验**界面上真能走通的那部分**：设置页写读回、导航按身份重排、面试页能发起会话。
 *
 * <p><b>不断言"回复内容好不好"</b>：那取决于模型与积分（种子账号余额为 0 时这一轮会被积分闸
 * 拦下，界面显示"积分不足"）。这里验的是"入口在、请求发出去了、状态说清楚了"，
 * 不把上游质量当成验收对象。
 */
test('身份与目标：能改画像并读回，且明写"不影响你能做什么"', async ({ page }) => {
  await signIn(page)
  await page.goto('/#/profile')

  // 这句话是这一页最要紧的文案：身份不是第二套权限
  await expect(page.getByText('不影响你能做什么')).toBeVisible()

  const role = `后端开发-${String(Date.now())}`
  await page.getByTestId('profile-target-role').fill(role)
  await page.getByTestId('profile-save').click()
  await expect(page.getByTestId('profile-saved')).toBeVisible()

  // 刷新后仍在：说明真的落库了，不是只存在表单状态里
  await page.reload()
  await expect(page.getByTestId('profile-target-role')).toHaveValue(role)
})

test('导航按身份重排：求职者有面试入口、且排在第一', async ({ page }) => {
  await signIn(page)
  // 先把身份确认为求职者（默认就是，但显式存一次避免被别的用例改过）
  await page.goto('/#/profile')
  await page.getByTestId('profile-identity').selectOption('job_seeker')
  await page.getByTestId('profile-save').click()
  await expect(page.getByTestId('profile-saved')).toBeVisible()

  await page.goto('/#/knowledge')
  const nav = page.getByRole('navigation', { name: '主导航' })
  await expect(nav.getByTestId('learner-nav-interview')).toBeVisible()

  // 排在第一个：身份的 preferredNav 里 interview 在首位，这是 AC-002 在导航上的观察点
  const first = nav.locator('a').first()
  await expect(first).toHaveAttribute('data-testid', 'learner-nav-interview')
})

test('面试页：能发起会话；积分不足时说清楚而不是静默失败', async ({ page }) => {
  test.skip(REMOTE, '依赖"余额 0 才会被闸拦下"；线上验收账号有积分，这一轮会真调上游')
  await signIn(page)
  await page.goto('/#/interview')

  // 隐私口径要明示：笔记默认不送上游
  await expect(page.getByText('你的笔记默认不参与')).toBeVisible()

  await page.getByTestId('interview-start').click()

  // 种子账号余额 0：这一轮会被积分闸拦下。断言"给了明确原因"，而不是断言有没有回复
  await expect(page.getByTestId('interview-line-candidate')).toBeVisible()
  await expect(page.getByText('积分不足')).toBeVisible()
  // 失败时不该留一条空的"面试官"气泡在那装样子
  await expect(page.getByTestId('interview-review')).toBeVisible()
})
