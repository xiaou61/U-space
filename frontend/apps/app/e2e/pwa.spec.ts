import { expect, test } from '@playwright/test'

/**
 * PWA 在生产构建下真的生效（WORK-016 / AC-014、AC-015）。
 *
 * <p>这条只在显式指向生产预览时跑（`PWA_BASE_URL`），默认跳过：开发服务器
 * （`pnpm dev`）刻意不注册 SW，拿它测这条会永远红。
 */
const PWA_BASE_URL = process.env.PWA_BASE_URL

test.describe('PWA', () => {
  test.skip(PWA_BASE_URL === undefined, '只在生产预览下跑：设 PWA_BASE_URL=http://127.0.0.1:5199')

  test('manifest 可解析且 SW 注册成功', async ({ page }) => {
    await page.goto(PWA_BASE_URL!)

    const manifestHref = await page.locator('link[rel="manifest"]').getAttribute('href')
    expect(manifestHref, 'index.html 里应有 manifest 链接').not.toBeNull()
    const manifest = await page.evaluate(async (href) => {
      const res = await fetch(href)
      return res.json()
    }, manifestHref!)
    expect(manifest.display).toBe('standalone')
    expect(manifest.icons.length).toBeGreaterThanOrEqual(3)
    expect(manifest.theme_color).toBeTruthy()

    // SW 在 load 之后注册，给它一点时间
    const registered = await expect
      .poll(
        () => page.evaluate(async () => {
          const reg = await navigator.serviceWorker.getRegistration()
          return reg !== undefined && reg.active !== null
        }),
        { timeout: 20_000 },
      )
      .toBe(true)
  })

  test('Service Worker 不缓存 /api', async ({ page }) => {
    await page.goto(PWA_BASE_URL!)
    await expect
      .poll(() => page.evaluate(async () => (await navigator.serviceWorker.getRegistration())?.active != null), {
        timeout: 20_000,
      })
      .toBe(true)

    // 打一次接口（未登录会 401，这不重要——重要的是它不该进 Cache Storage）
    await page.evaluate(() => fetch('/api/v1/knowledge/categories').catch(() => undefined))
    await page.waitForTimeout(800)

    const cachedUrls = await page.evaluate(async () => {
      const names = await caches.keys()
      const all: string[] = []
      for (const name of names) {
        const cache = await caches.open(name)
        for (const req of await cache.keys()) {
          all.push(new URL(req.url).pathname)
        }
      }
      return all
    })
    expect(cachedUrls.filter((path) => path.startsWith('/api'))).toEqual([])
  })
})
