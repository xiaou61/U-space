import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs'
import path from 'node:path'

import { describe, expect, it } from 'vitest'

/**
 * 扫描路径覆盖检查。
 *
 * <p>`globals.css` 用 `@import "tailwindcss" source(none)` 关掉了自动探测，改为显式登记
 * 每个含类名的源码目录（原因见该文件顶部注释：pnpm 把 workspace 包软链在 node_modules 下，
 * 自动探测会跳过它）。
 *
 * <p><b>漏登记的失败是静默的</b>：凡是别处也用过的类名仍然会生成，只有本包独有的那些
 * 会消失——表现为"布局类不生效但没有任何报错"。这个坑已经踩过一次（新加的
 * `packages/auth` 没登记，`justify-end` 这类只在那儿出现的类名一直没生成）。
 * 所以这里把它变成会失败的检查，而不是靠人记住。
 */

const CSS_PATH = path.join(process.cwd(), 'src', 'styles', 'globals.css')

function workspaceRoot(): string {
  let directory = path.resolve(process.cwd())
  for (let depth = 0; depth < 6; depth += 1) {
    if (existsSync(path.join(directory, 'pnpm-workspace.yaml'))) {
      return directory
    }
    directory = path.dirname(directory)
  }
  throw new Error('向上找不到 pnpm-workspace.yaml，无法定位工作区根目录')
}

/** 有渲染组件（.tsx）的源码目录才需要被扫描；纯 TS 的包不写类名。 */
function sourceRootsWithComponents(root: string): string[] {
  const roots: string[] = []
  for (const group of ['packages', 'apps']) {
    const groupDir = path.join(root, group)
    if (!existsSync(groupDir)) {
      continue
    }
    for (const entry of readdirSync(groupDir, { withFileTypes: true })) {
      if (!entry.isDirectory()) {
        continue
      }
      const source = path.join(groupDir, entry.name, 'src')
      if (existsSync(source) && statSync(source).isDirectory() && hasTsx(source)) {
        roots.push(source)
      }
    }
  }
  return roots
}

function hasTsx(directory: string): boolean {
  for (const entry of readdirSync(directory, { withFileTypes: true })) {
    if (entry.name === 'node_modules' || entry.name === 'dist') {
      continue
    }
    const full = path.join(directory, entry.name)
    if (entry.isDirectory() && hasTsx(full)) {
      return true
    }
    if (entry.isFile() && entry.name.endsWith('.tsx')) {
      return true
    }
  }
  return false
}

/** 把 @source 的写法（可能是 glob）折算成它覆盖的起点目录。 */
function coveredRoot(cssDirectory: string, entry: string): string {
  const withoutGlob = entry.replace(/\*\*.*$/, '').replace(/\*.*$/, '')
  return path.resolve(cssDirectory, withoutGlob.replace(/[/\\]+$/, ''))
}

describe('Tailwind 扫描路径覆盖', () => {
  it('每个含组件的源码目录都在 globals.css 的 @source 里', () => {
    const root = workspaceRoot()
    const cssPath = CSS_PATH
    const css = readFileSync(cssPath, 'utf8')
    const cssDirectory = path.dirname(cssPath)

    const entries = [...css.matchAll(/@source\s+'([^']+)'/g)].map((match) => match[1] ?? '')
    expect(entries.length, 'globals.css 里没有任何 @source，检查失去意义').toBeGreaterThan(0)
    const covered = entries.map((entry) => coveredRoot(cssDirectory, entry))

    const uncovered = sourceRootsWithComponents(root).filter((source) => {
      const normalized = path.resolve(source)
      return !covered.some(
        (base) => normalized === base || normalized.startsWith(base + path.sep),
      )
    })

    expect(
      uncovered.map((item) => path.relative(root, item)),
      '这些目录含 .tsx 但没被 globals.css 的 @source 登记：它们独有的 Tailwind 类名会静默不生成',
    ).toEqual([])
  })

  it('每个 @source 指向的目录都真实存在（不存在时那一行形同虚设，且没有任何报错）', () => {
    // 实测踩过：`streamdown` 那一行写成了仓库根的 `node_modules/streamdown/dist/*.js`，
    // 而它其实是 `@paideia/ui` 的依赖、落在 `packages/ui/node_modules` 下——于是整行**什么都没扫**。
    // 更糟的是它看起来"像是生效了"：`bg-sidebar` 这条规则确实出现了，因为我自己的注释里写过这几个字。
    // 真相要靠**只有它 dist 里才有**的类来验：`--sdm-c`、`counter(line)` 当时一条都没有。
    const css = readFileSync(CSS_PATH, 'utf8')
    const cssDirectory = path.dirname(CSS_PATH)

    const missing = [...css.matchAll(/@source\s+'([^']+)'/g)]
      .map((match) => match[1] ?? '')
      .filter((entry) => !existsSync(coveredRoot(cssDirectory, entry)))

    expect(
      missing,
      '这些 @source 指向的目录不存在：Tailwind 不会报错，只是那些类名静默不生成',
    ).toEqual([])
  })
})
