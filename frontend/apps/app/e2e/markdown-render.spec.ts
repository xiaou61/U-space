import { expect, test } from '@playwright/test'

import { signIn } from './session'

/**
 * markdown 在**真浏览器**里的呈现（WORK-012）。
 *
 * <p>为什么要单独一组：渲染器的正确性已经被 `packages/ui/src/markdown/markdown.test.tsx` 的
 * 四条口径盯住了，但那全是 jsdom——**它没有 CSS**。而本项真正踩过的坑恰好是样式：
 * `streamdown` 的类名要能被 Tailwind 扫到（`@source` 指错了目录时它一条都不生成、还不报错），
 * 它要的 `bg-sidebar` 令牌还得我们自己补。这两件事**只有在真浏览器里读计算样式才验得出来**。
 *
 * <p>用的是种子里那条同时带表格与 ```java 代码块的条目（`模块化单体的边界`，id=3）。
 *
 * <p><b>每条用例照本目录的约定先 `signIn(page)`</b>：只靠 `storageState` 直接跳深链，
 * 从第二条用例起会落到登录页（会话在第一次启动时被轮换掉，文件里那份就旧了）。
 */

const ENTRY_WITH_TABLE_AND_CODE = '/#/knowledge/entries/3'

/** 取某个元素的某个计算属性（读不出来就抛，避免"读到空串还当通过"）。 */
function computed(page: import('@playwright/test').Page, selector: string, property: string) {
  return page
    .locator(selector)
    .first()
    .evaluate((element, name) => window.getComputedStyle(element).getPropertyValue(name), property)
    .then((value) => {
      expect(value, `${selector} 的 ${property} 读不出来`).not.toBe('')
      return value
    })
}

/** 打开那条带表格与代码块的正文，并等懒加载的渲染器就位（兜底是纯文本，量样式前必须等它换掉）。 */
async function openEntry(page: import('@playwright/test').Page) {
  await signIn(page)
  await page.goto(ENTRY_WITH_TABLE_AND_CODE)
  const body = page.getByTestId('markdown-body')
  await expect(body).toBeVisible()
  await expect(body.locator('[data-testid="markdown-loading"]')).toHaveCount(0)
  return body
}

const CODE_BLOCK = '[data-streamdown="code-block"]'
/** 行号的伪元素：`before:content-[counter(line)]`，这个类只有 streamdown 的 dist 里才有。 */
const LINE_NUMBER = '[data-streamdown="code-block"] code > span'

test('条目正文里的表格与代码块是真结构，不是被当成纯文本', async ({ page }) => {
  const body = await openEntry(page)

  await expect(body.locator('table')).toHaveCount(1)
  await expect(body.locator('table thead th')).toHaveCount(2)
  await expect(body.locator('table tbody tr')).toHaveCount(2)

  const codeBlock = body.locator(CODE_BLOCK)
  await expect(codeBlock).toHaveCount(1)
  await expect(codeBlock).toContainText('ApplicationModules.of')
  await expect(codeBlock).toContainText('java')
})

test('样式真的生效：行号由 streamdown 的类画出来，代码块有自己的底色', async ({ page }) => {
  await openEntry(page)

  // 这是"它 dist 里的类有没有被 Tailwind 扫到"的判定性证据：少了 @source 那一行，
  // 这里读到的就是 `none`（jsdom 里读不到计算样式，所以这条只能在真浏览器里验）
  const gutter = page.locator(LINE_NUMBER).first()
  await expect(gutter).toBeVisible()
  const marker = await gutter.evaluate(
    (element) => window.getComputedStyle(element, '::before').content,
  )
  expect(marker, '行号的伪元素没有内容：streamdown 的类没被扫到（检查 @source 那一行）').not.toBe('none')
  expect(marker).not.toBe('')

  // 底色来自 `bg-sidebar`，那个令牌是我们自己补的（缺了它就是透明）
  const codeBackground = await computed(page, CODE_BLOCK, 'background-color')
  expect(codeBackground, '代码块底色是透明的：`--color-sidebar` 令牌丢了').not.toBe('transparent')
  expect(codeBackground).not.toBe('rgba(0, 0, 0, 0)')
  // 它还不能跟页面底色一样，否则"有底色"等于没有
  expect(codeBackground).not.toBe(await computed(page, 'body', 'background-color'))
})

test('深浅两套主题下都成立（不是只在浅色下凑对）', async ({ page }) => {
  await openEntry(page)

  const lightCode = await computed(page, CODE_BLOCK, 'background-color')
  const lightText = await computed(page, CODE_BLOCK, 'color')

  await page.getByRole('button', { name: '切换到深色主题' }).click()
  await expect(page.locator('html')).toHaveClass(/dark/)

  const darkCode = await computed(page, CODE_BLOCK, 'background-color')
  const darkText = await computed(page, CODE_BLOCK, 'color')
  expect(darkCode, '切到深色后代码块底色没变，说明主题令牌没接上').not.toBe(lightCode)
  expect(darkCode).not.toBe('rgba(0, 0, 0, 0)')
  expect(darkText).not.toBe(lightText)

  // 行号在深色下同样要画出来（颜色可能是另一套）
  const marker = await page
    .locator(LINE_NUMBER)
    .first()
    .evaluate((element) => window.getComputedStyle(element, '::before').content)
  expect(marker, '深色下行号没画出来').not.toBe('none')
})
