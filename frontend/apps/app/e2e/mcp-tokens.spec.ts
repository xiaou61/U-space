import { expect, test } from '@playwright/test'

import { signIn } from './session'

/**
 * MCP 接入凭据页（WORK-019 AC-013）。
 *
 * <p>验收点只有三条，都是"用户视角能观察到的"：创建后明文出现一次、刷新后只剩前缀、
 * 吊销后该行变成失效。
 *
 * <p>**不断言 token 的具体值**：它是随机的，断言只会让用例在别处变红。
 */
test('MCP 凭据：创建后明文只出现一次，刷新后只显示前缀', async ({ page }) => {
  await signIn(page)
  await page.goto('/#/settings/mcp')

  await expect(page.getByText('MCP 接入')).toBeVisible()
  const name = `e2e-${String(Date.now())}`
  await page.getByTestId('mcp-token-name').fill(name)
  await page.getByTestId('mcp-token-create').click()

  // 明文只在创建成功那一刻出现，且以固定前缀开头
  const plaintext = page.getByTestId('mcp-token-plaintext')
  await expect(plaintext).toBeVisible()
  await expect(plaintext).toContainText('pd_mcp_')

  // 刷新后明文消失、列表里有这一条（只剩前缀）
  await page.reload()
  await expect(page.getByTestId('mcp-token-plaintext')).toHaveCount(0)
  await expect(page.getByTestId('mcp-token-row').filter({ hasText: name })).toBeVisible()
  await expect(page.getByTestId('mcp-token-row').filter({ hasText: name })).toContainText('pd_mcp_')
})

test('MCP 凭据：吊销后该行显示已失效', async ({ page }) => {
  await signIn(page)
  await page.goto('/#/settings/mcp')

  const name = `revoke-${String(Date.now())}`
  await page.getByTestId('mcp-token-name').fill(name)
  await page.getByTestId('mcp-token-create').click()
  await expect(page.getByTestId('mcp-token-plaintext')).toBeVisible()

  // window.confirm 是原生弹窗：e2e 必须先接受它，否则点击会被挂住
  page.on('dialog', (dialog) => { void dialog.accept() })
  const row = page.getByTestId('mcp-token-row').filter({ hasText: name })
  await row.getByTestId('mcp-token-revoke').click()

  await expect(row).toContainText('已失效')
  await expect(row.getByTestId('mcp-token-revoke')).toHaveCount(0)
})

test('MCP 凭据：390px 宽下不出现横向滚动', async ({ page }) => {
  await signIn(page)
  await page.setViewportSize({ width: 390, height: 800 })
  await page.goto('/#/settings/mcp')

  const overflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  )
  // 允许 1px 的亚像素误差；真溢出通常是几十像素
  expect(overflow).toBeLessThanOrEqual(1)
})
