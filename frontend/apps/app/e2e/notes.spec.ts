import { expect, test } from '@playwright/test'

import { signIn } from './session'

/**
 * 我的笔记（AC-010 的界面一半）。
 *
 * <p>走真实后端：建档→改名改正文→保存→**重新加载页面**（证明真的落了库，而不是只活在内存里）
 * →删除。删除的二次确认**从 `window.confirm` 换成了应用自己的对话框**（2026-10-15），
 * 所以这里点的是界面上的按钮，不再接管原生对话框。
 *
 * <p>种子账号的笔记是持久的，所以每条用例自己建、自己删，并且标题带唯一后缀——
 * 否则重复跑会互相看见对方的残留。
 */

test('从导航进入笔记：建档→编辑→保存→重载仍在→删除', async ({ page }) => {
  await signIn(page)
  // 入口在主导航里（"某个页面忘了加入口"是 2026-10-12 修过的真缺陷，这里顺带钉住）
  await page.getByTestId('learner-nav-notes').click()
  await expect(page.getByTestId('notes-placeholder')).toBeVisible()

  await page.getByTestId('notes-create').click()
  // 等编辑器真的换到新那条：不然紧接着的 fill 可能落到上一条的输入框上
  await expect(page.getByTestId('note-title')).toHaveValue('未命名笔记')

  const title = `e2e 笔记 ${Date.now()}`
  await page.getByTestId('note-title').fill(title)
  await page.getByTestId('note-body').fill('正文第一版')

  // 改过之后"存了没有"必须说得清，而不是只有一个保存按钮
  await expect(page.getByTestId('note-dirty')).toBeVisible()
  await page.getByTestId('note-save').click()
  await expect(page.getByTestId('note-dirty')).toHaveCount(0)
  await expect(page.getByTestId('note-save-state')).toHaveText('与服务端一致')

  // 保存后列表里出现新标题，并且**带上了摘要**——摘要是后端列表查询截的（LEFT(body,200)），
  // 所以这一条同时验了"列表带摘要"这条新行为
  await expect(page.getByTestId('notes-list')).toContainText(title)
  await expect(page.getByTestId('notes-list')).toContainText('正文第一版')

  // 重新加载：内容是从库里读回来的，不是内存里的草稿
  await page.reload()
  await page.getByTestId('learner-nav-notes').click()
  await page.getByTestId('notes-list').getByText(title).click()
  await expect(page.getByTestId('note-body')).toHaveValue('正文第一版')

  // 删除要二次确认：现在是应用自己的对话框
  await page.getByTestId('note-delete').click()
  await expect(page.getByTestId('note-confirm-dialog')).toBeVisible()
  await page.getByTestId('note-confirm').click()
  // 断"这段文字从页面消失"，而不是断言列表容器里不含它：删掉唯一一条之后列表整个不渲染了
  // （变成空状态），对不存在的容器做 not.toContainText 会报 element not found
  await expect(page.getByText(title)).toHaveCount(0)
})

test('分享：链接免登录可读，撤回后立刻失效', async ({ page, browser }) => {
  await signIn(page)
  await page.getByTestId('learner-nav-notes').click()
  await page.getByTestId('notes-create').click()
  await expect(page.getByTestId('note-title')).toHaveValue('未命名笔记')

  const title = `e2e 分享 ${Date.now()}`
  await page.getByTestId('note-title').fill(title)
  await page.getByTestId('note-body').fill('分享出去的正文')
  await page.getByTestId('note-save').click()
  await page.getByTestId('note-share').click()
  // 打开只读分享对话框；**打开本身不产生副作用**，要显式点"生成"（原来点一下按钮就生成了）
  await page.getByTestId('note-share-create').click()

  const link = await page.getByTestId('note-share-link').inputValue()
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
  // 撤回后对话框回到"还没有分享"的状态
  await expect(page.getByTestId('note-share-create')).toBeVisible()
  await guest.reload()
  await expect(guest.getByTestId('share-missing')).toBeVisible()

  await anonymous.close()
  await page.keyboard.press('Escape')
  // 收尾：删掉这条笔记（种子账号的数据是持久的）
  await page.getByTestId('note-delete').click()
  await page.getByTestId('note-confirm').click()
  await expect(page.getByText(title)).toHaveCount(0)
})

test('编辑器：改没改、存没存说得清；正文能预览', async ({ page }) => {
  await signIn(page)
  await page.getByTestId('learner-nav-notes').click()
  await page.getByTestId('notes-create').click()
  await expect(page.getByTestId('note-title')).toHaveValue('未命名笔记')

  const title = `e2e 状态 ${Date.now()}`
  await page.getByTestId('note-title').fill(title)
  // 用模板字符串写正文：里面的换行就是 markdown 的换行，不写成 \n 转义
  await page.getByTestId('note-body').fill(`## 小标题

一条带 **粗体** 的正文`)

  await expect(page.getByTestId('note-dirty')).toBeVisible()
  await expect(page.getByTestId('note-meta')).toContainText('尚未保存')

  // 预览：markdown 真的被渲染成元素，而不是把源码当文字显示
  await page.getByTestId('note-tab-preview').click()
  await expect(page.getByTestId('note-preview').getByRole('heading', { name: '小标题' })).toBeVisible()
  await expect(page.getByTestId('note-preview')).toContainText('粗体')

  // 回到编辑、保存，状态翻转
  await page.getByTestId('note-tab-edit').click()
  await page.getByTestId('note-save').click()
  await expect(page.getByTestId('note-dirty')).toHaveCount(0)
  await expect(page.getByTestId('note-meta')).toContainText('最后编辑')

  // 收尾
  await page.getByTestId('note-delete').click()
  await page.getByTestId('note-confirm').click()
  await expect(page.getByText(title)).toHaveCount(0)
})

test('带着未保存的改动切走，会先问一句而不是静默丢掉', async ({ page }) => {
  await signIn(page)
  await page.getByTestId('learner-nav-notes').click()

  const kept = `e2e 拦截A ${Date.now()}`
  await page.getByTestId('notes-create').click()
  await expect(page.getByTestId('note-title')).toHaveValue('未命名笔记')
  await page.getByTestId('note-title').fill(kept)
  await page.getByTestId('note-save').click()

  const editing = `e2e 拦截B ${Date.now()}`
  await page.getByTestId('notes-create').click()
  await expect(page.getByTestId('note-title')).toHaveValue('未命名笔记')
  // 记下它的 id：这条**从没保存过**，列表里始终叫「未命名笔记」，只能按 id 找它
  const unfinishedTestId =
    (await page.getByTestId('notes-list').locator('button[data-testid^="note-item-"]').first().getAttribute('data-testid')) ?? ''
  await page.getByTestId('note-title').fill(editing)
  await page.getByTestId('note-body').fill('还没保存的内容')
  await expect(page.getByTestId('note-dirty')).toBeVisible()
  // 点回第一条：必须弹确认
  await page.getByTestId('notes-list').getByText(kept).click()
  await expect(page.getByTestId('note-confirm-dialog')).toBeVisible()
  await page.getByRole('button', { name: '取消' }).click()
  // 取消之后仍在原来那条上
  await expect(page.getByTestId('note-title')).toHaveValue(editing)

  // 这次确认丢掉，切过去
  await page.getByTestId('notes-list').getByText(kept).click()
  await page.getByTestId('note-confirm').click()
  await expect(page.getByTestId('note-title')).toHaveValue(kept)

  // 收尾：两条都删掉（种子账号的笔记是持久的，留着会污染下一轮）
  for (const target of [unfinishedTestId, kept]) {
    if (target.startsWith('note-item-')) {
      await page.getByTestId(target).click()
    } else {
      await page.getByTestId('notes-list').getByText(target).click()
    }
    await page.getByTestId('note-delete').click()
    await page.getByTestId('note-confirm').click()
  }
  await expect(page.getByText(kept)).toHaveCount(0)
  await expect(page.getByTestId(unfinishedTestId)).toHaveCount(0)
})

test('跟着助手的回答走：回答上带"存进笔记"入口（本环境没凭据，只验入口的挂载规则）', async ({ page }) => {
  await signIn(page)
  await page.goto('/#/knowledge')
  await page.getByTestId('assistant-launcher').click()
  await page.getByTestId('assistant-tab-chat').click()

  // 还没有任何回答：不该出现存笔记入口（空正文与提示都不该被存成笔记）
  await expect(page.getByTestId('conversation-save-to-note')).toHaveCount(0)
  // 聊天输入照常在：面板本身是通的
  await expect(page.getByTestId('conversation-input')).toBeVisible()
})
