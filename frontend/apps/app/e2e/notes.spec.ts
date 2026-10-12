import { expect, test, type Page } from '@playwright/test'

/**
 * 我的笔记（AC-010 的界面一半）。
 *
 * <p>走真实后端：建档→改名改正文→保存→**重新加载页面**（证明真的落了库，而不是只活在内存里）
 * →删除。删除走 `window.confirm`，所以要先接管对话框。
 *
 * <p>种子账号的笔记是持久的，所以每条用例自己建、自己删，并且标题带唯一后缀——
 * 否则重复跑会互相看见对方的残留。
 */

const SEED_LEARNER = { identifier: 'seed-learner', password: 'seed-learner-password' }

async function signIn(page: Page, account: { identifier: string; password: string }) {
  await page.goto('/')
  await expect(page.getByTestId('login-identifier')).toBeVisible()
  await page.getByTestId('login-identifier').fill(account.identifier)
  await page.getByTestId('login-password').fill(account.password)
  await page.getByTestId('login-submit').click()
  await expect(page.getByTestId('login-submit')).toHaveCount(0)
}

test('从导航进入笔记：建档→编辑→保存→重载仍在→删除', async ({ page }) => {
  await signIn(page, SEED_LEARNER)
  // 入口在主导航里（"某个页面忘了加入口"是 2026-10-12 修过的真缺陷，这里顺带钉住）
  await page.getByTestId('learner-nav-notes').click()
  await expect(page.getByTestId('notes-placeholder')).toBeVisible()

  await page.getByTestId('notes-create').click()
  await expect(page.getByTestId('note-title')).toBeVisible()

  const title = `e2e 笔记 ${Date.now()}`
  await page.getByTestId('note-title').fill(title)
  await page.getByTestId('note-body').fill('正文第一版')
  await page.getByTestId('note-save').click()
  // 保存后列表里出现新标题
  await expect(page.getByTestId('notes-list')).toContainText(title)

  // 重新加载：内容是从库里读回来的，不是内存里的草稿
  await page.reload()
  await page.getByTestId('learner-nav-notes').click()
  await page.getByTestId('notes-list').getByText(title).click()
  await expect(page.getByTestId('note-body')).toHaveValue('正文第一版')

  // 删除要二次确认
  page.on('dialog', (dialog) => void dialog.accept())
  await page.getByTestId('note-delete').click()
  // 断"这段文字从页面消失"，而不是断言列表容器里不含它：删掉唯一一条之后列表整个不渲染了
  // （变成空状态），对不存在的容器做 not.toContainText 会报 element not found
  await expect(page.getByText(title)).toHaveCount(0)
})

test('分享：链接免登录可读，撤回后立刻失效', async ({ page, browser }) => {
  await signIn(page, SEED_LEARNER)
  await page.getByTestId('learner-nav-notes').click()
  await page.getByTestId('notes-create').click()

  const title = `e2e 分享 ${Date.now()}`
  await page.getByTestId('note-title').fill(title)
  await page.getByTestId('note-body').fill('分享出去的正文')
  await page.getByTestId('note-save').click()
  await page.getByTestId('note-share').click()

  const link = await page.getByTestId('note-share-link').innerText()
  expect(link).toContain('/#/share/')

  // **全新的匿名上下文**：没有任何登录状态，这正是"免登录可读"要验的形态
  const anonymous = await browser.newContext()
  const guest = await anonymous.newPage()
  await guest.goto(link)
  await expect(guest.getByTestId('share-title')).toHaveText(title)
  await expect(guest.getByTestId('share-body')).toHaveText('分享出去的正文')
  // 举报入口在**未登录**时给的是登录引导（举报需要身份），而不是一个点了会 401 的按钮
  await expect(guest.getByTestId('share-report-need-login')).toBeVisible()

  // 撤回：同一个链接立刻不可读
  await page.getByTestId('note-revoke').click()
  await expect(page.getByTestId('note-share')).toBeVisible()
  await guest.reload()
  await expect(guest.getByTestId('share-missing')).toBeVisible()

  await anonymous.close()
  // 收尾：删掉这条笔记（种子账号的数据是持久的）
  page.on('dialog', (dialog) => void dialog.accept())
  await page.getByTestId('note-delete').click()
  await expect(page.getByText(title)).toHaveCount(0)
})

test('跟着助手的回答走：回答上带"存进笔记"入口（本环境没凭据，只验入口的挂载规则）', async ({ page }) => {
  await signIn(page, SEED_LEARNER)
  await page.goto('/#/knowledge')
  await page.getByTestId('assistant-launcher').click()
  await page.getByTestId('assistant-tab-chat').click()

  // 还没有任何回答：不该出现存笔记入口（空正文与提示都不该被存成笔记）
  await expect(page.getByTestId('conversation-save-to-note')).toHaveCount(0)
  // 聊天输入照常在：面板本身是通的
  await expect(page.getByTestId('conversation-input')).toBeVisible()
})
