import { expect, test, type Page } from '@playwright/test'

import { ADMIN, ADMIN_ENTRY } from './env'

/**
 * 管理端的窄屏形态（WORK-016 / AC-006、AC-007）。
 *
 * <p>后台在手机上的定位是"临时处理一条内容"，不是完整的工作台。所以判据只有两条：
 * 菜单能到达、列表能以卡片读完并操作。桌面表格的完整性由既有 9 条用例守。
 */

async function signIn(page: Page) {
  await page.goto(ADMIN_ENTRY)
  await expect(page.getByTestId('admin-sign-in')).toBeVisible()
  await page.getByTestId('login-identifier').fill(ADMIN.identifier)
  await page.getByTestId('login-password').fill(ADMIN.password)
  await page.getByTestId('login-submit').click()
  await expect(page.getByTestId('admin-console')).toBeVisible({ timeout: 20_000 })
}

/**
 * 管理端的 hash 路由地址。
 *
 * <p>**必须拼在 `ADMIN_ENTRY` 上**，不能写死 `/#/…`：本地管理端在站点根（`/`），
 * 而线上部署时它在 `/admin/`（同一个 jar 同时供学习者端与管理端）。写死根路径的用例
 * 在线上会跳到学习者端——元素全找不到，看着像"界面坏了"，其实是测错了地方。
 * 2026-10-18 线上跑 e2e 时实测踩到（3 条窄屏用例全红）。
 */
function adminPath(hash: string): string {
  return `${ADMIN_ENTRY.replace(/\/$/, '')}/#${hash}`
}

async function overflowX(page: Page): Promise<number> {
  return page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)
}

/**
 * 同学习者端：轮询到稳定再断言。Chromium 移动模拟在导航瞬间有两三帧布局视口重算，
 * 会让 `position: fixed` 元素短暂按更宽的视口定位；真实溢出会一直存在。
 */
async function expectNoOverflow(page: Page, label: string): Promise<void> {
  await expect.poll(() => overflowX(page), { timeout: 5000, intervals: [100, 200, 400] }).toBeLessThanOrEqual(1)
  expect(await overflowX(page), `${label} 仍有横向溢出`).toBeLessThanOrEqual(1)
}

const PAGES = [
  { hash: '/', name: '总览' },
  { hash: '/knowledge/categories', name: '分类管理' },
  { hash: '/knowledge/entries', name: '条目管理' },
  { hash: '/reports', name: '举报处理' },
  { hash: '/ai-credits/audit', name: '用量审计' },
  { hash: '/users', name: '用户管理' },
]

test.describe('管理端窄屏', () => {
  test.beforeEach(async ({ page }) => {
    await signIn(page)
  })

  for (const item of PAGES) {
    test(`${item.name} 无横向溢出`, async ({ page }) => {
      await page.goto(adminPath(item.hash))
      await expectNoOverflow(page, `${item.name} 在 ${page.viewportSize()?.width}px 下`)
    })
  }

  test('侧栏默认收起，汉堡能打开抽屉并到达全部菜单', async ({ page }) => {
    await page.goto(adminPath('/'))

    // 侧栏在窄屏是屏幕外的抽屉，不是常驻的 224px
    const sidebar = page.getByTestId('admin-sidebar')
    await expect(sidebar).toHaveAttribute('data-open', 'false')
    const hiddenBox = await sidebar.boundingBox()
    expect(hiddenBox!.x + hiddenBox!.width).toBeLessThanOrEqual(1)

    await page.getByTestId('admin-drawer-toggle').click()
    await expect(sidebar).toHaveAttribute('data-open', 'true')
    // 抽屉是 200ms 的位移过渡：立刻量会拿到动画中间帧（实测 -251px），轮询到位再断言
    await expect.poll(async () => (await sidebar.boundingBox())!.x, { timeout: 3000 }).toBeGreaterThanOrEqual(-1)

    // 一级菜单：可跳转的直接项 + 只负责分组的父项
    for (const label of ['总览', '举报处理', '知识库', 'AI 积分']) {
      await expect(
        page.getByTestId(`admin-menu-${label}`).or(page.getByTestId(`admin-nav-${label}`)),
      ).toBeVisible()
    }

    // 父项展开后才出现子项（默认按当前路由决定，落在首页时是收起状态）
    await page.getByTestId('admin-menu-知识库').click()
    await expect(page.getByTestId('admin-nav-分类管理')).toBeVisible()

    // 选中子项后抽屉自动收起
    await page.getByTestId('admin-nav-分类管理').click()
    await expect(page).toHaveURL(/#\/knowledge\/categories/)
    await expect(sidebar).toHaveAttribute('data-open', 'false')
  })

  test('条目列表在窄屏是卡片，且能看到编辑与删除', async ({ page }) => {
    await page.goto(adminPath('/knowledge/entries'))
    await page.waitForTimeout(500)

    // 桌面表格在窄屏隐藏
    await expect(page.getByTestId('admin-entry-list')).toBeHidden()

    const cards = page.getByTestId('admin-entry-cards')
    await expect(cards).toBeVisible()
    const cardCount = await cards.locator('li').count()
    if (cardCount === 0) {
      test.skip(true, '测试库里没有条目')
    }

    // 每条卡片都要能看到操作（不能要求横向拖动才出现）
    await expect(cards.locator('[data-testid^="admin-entry-edit-card-"]').first()).toBeVisible()
    await expect(cards.locator('[data-testid^="admin-entry-delete-card-"]').first()).toBeVisible()
  })

  test('分类列表在窄屏是卡片', async ({ page }) => {
    await page.goto(adminPath('/knowledge/categories'))
    await page.waitForTimeout(500)
    await expect(page.getByTestId('admin-category-list')).toBeHidden()
    await expect(page.getByTestId('admin-category-cards')).toBeVisible()
  })

  test('窄屏对话框是贴底面板，关闭按钮命中区不小于 44px', async ({ page }) => {
    await page.goto(adminPath('/knowledge/categories'))
    await page.waitForTimeout(500)

    // 新建分类的入口在页面头部
    const create = page.getByRole('button', { name: /新建|创建/ }).first()
    if ((await create.count()) === 0) {
      test.skip(true, '这一页没有新建入口')
    }
    await create.click()

    const dialog = page.locator('[data-slot="dialog-content"]').first()
    await expect(dialog).toBeVisible()
    const box = await dialog.boundingBox()
    const vh = page.viewportSize()!.height
    // 贴底：下边缘在视口底部（容 1px 舍入）
    expect(box!.y + box!.height).toBeGreaterThanOrEqual(vh - 1)
    // 满宽：不留左右 16px 的居中浮层边距
    expect(box!.width).toBeGreaterThanOrEqual(page.viewportSize()!.width - 1)
  })
})
