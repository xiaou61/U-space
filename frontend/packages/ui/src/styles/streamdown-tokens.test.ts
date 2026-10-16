import { existsSync, readFileSync, readdirSync } from 'node:fs'
import path from 'node:path'

import { describe, expect, it } from 'vitest'

/**
 * `streamdown` 依赖的语义色令牌检查。
 *
 * <p><b>为什么需要它</b>：Tailwind v4 **只为已定义的主题值生成工具类**，没有定义就**什么都不生成、
 * 也不报错**。而 `streamdown` 的代码块容器与悬浮操作条用的是 shadcn 的 `bg-sidebar` / `border-sidebar`
 * ——我们没定义过 `sidebar` 这套语义。实测（WORK-012 接线后）构建产物里 `bg-sidebar` 的规则数是
 * **0**，症状是代码块没有底色、操作条悬在正文上，而控制台干干净净。
 *
 * <p>所以这里把"它要的令牌我们都有"变成会失败的检查：**它升级后若要一个新令牌，这条会先红**，
 * 而不是等到用户说"怎么变丑了"。
 */

/** 这些不是颜色名，是 Tailwind 自己的工具词（尺寸、对齐、边框宽度…），不属于令牌。 */
const NOT_A_COLOR = new Set([
  'base',
  'xs',
  'sm',
  'md',
  'lg',
  'xl',
  '2xl',
  '3xl',
  'left',
  'right',
  'center',
  'justify',
  'collapse',
  'separate',
  // `divide-x` / `divide-y` 是"在哪个轴上加分隔线"，不是颜色
  'x',
  'y',
  // Tailwind 的关键字颜色：它们在 utilities 里有固定取值，不需要令牌
  'current',
  'transparent',
  'inherit',
  'auto',
  'none',
])

/** `border-b-2`、`border-l-4` 这类是**边框宽度**，名字以数字结尾，不是颜色。 */
const IS_WIDTH_UTILITY = /^[a-z]-?\d+$/

/** Tailwind 自带调色板里的名字（如 `red-600`）：它们的令牌来自 tailwindcss 自己的 theme.css。 */
const DEFAULT_PALETTE = /^(?:red|orange|amber|yellow|lime|green|emerald|teal|cyan|sky|blue|indigo|violet|purple|fuchsia|pink|rose|slate|gray|zinc|neutral|stone|black|white)(?:-\d{2,3})?$/

function uiDirectory(): string {
  // 本文件在 packages/ui/src/styles/ 下
  return path.resolve(process.cwd())
}

/** 从某个 package 的安装目录里找它的 dist（pnpm 把依赖软链在该包的 node_modules 下）。 */
function packageDist(name: string): string {
  const directory = path.join(uiDirectory(), 'node_modules', name, 'dist')
  expect(existsSync(directory), `${name} 的 dist 不在 ${directory}：依赖或 pnpm 布局变了`).toBe(true)
  return directory
}

function readAll(directory: string): string {
  return readdirSync(directory)
    .filter((entry) => entry.endsWith('.js'))
    .map((entry) => readFileSync(path.join(directory, entry), 'utf8'))
    .join('\n')
}

function definedColorTokens(): Set<string> {
  const files = [
    path.join(uiDirectory(), 'src', 'styles', 'globals.css'),
    // Tailwind 自带调色板在这里（`--color-red-600` 等），不算我们的缺失
    path.join(uiDirectory(), 'node_modules', 'tailwindcss', 'theme.css'),
  ]
  const tokens = new Set<string>()
  for (const file of files) {
    expect(existsSync(file), `找不到 ${file}，这条检查会变成假绿`).toBe(true)
    for (const match of readFileSync(file, 'utf8').matchAll(/--color-([a-z0-9-]+)\s*:/g)) {
      tokens.add(match[1] ?? '')
    }
  }
  return tokens
}

describe('streamdown 用到的颜色令牌都已定义', () => {
  it('它 dist 里出现过的每个颜色工具类，其令牌都能解析到', () => {
    const source = readAll(packageDist('streamdown'))
    const prefixes = ['bg', 'text', 'border', 'ring', 'fill', 'stroke', 'divide', 'outline', 'from', 'via', 'to', 'placeholder', 'caret', 'decoration', 'accent']
    const pattern = new RegExp(
      `(?:^|[^\\w-])(${prefixes.join('|')})-([a-z][a-z0-9-]*)(?:/\\d+)?(?![\\w-])`,
      'g',
    )

    const used = new Set<string>()
    for (const match of source.matchAll(pattern)) {
      const name = match[2] ?? ''
      if (NOT_A_COLOR.has(name) || IS_WIDTH_UTILITY.test(name) || DEFAULT_PALETTE.test(name)) {
        continue
      }
      used.add(name)
    }

    expect(used.size, '一个颜色类都没扫到，说明扫描方式失效了').toBeGreaterThan(3)

    const defined = definedColorTokens()
    const missing = [...used].filter((name) => !defined.has(name)).sort()
    expect(
      missing,
      '这些颜色令牌 streamdown 在用、但我们没定义：Tailwind 不会为它们生成类，也不会报错（静默缺样式）',
    ).toEqual([])
  })

  it('反例护栏：`sidebar` 这一族确实是它要的，且我们已经补上', () => {
    // 单独钉住这个真实踩过的坑：它用的 `bg-sidebar` 不属于任何 Tailwind 默认调色板
    const source = readAll(packageDist('streamdown'))
    expect(source.includes('bg-sidebar')).toBe(true)
    expect(definedColorTokens().has('sidebar')).toBe(true)
  })
})
