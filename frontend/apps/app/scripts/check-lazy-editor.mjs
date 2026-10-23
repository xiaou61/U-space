#!/usr/bin/env node
/**
 * 懒加载守卫（WORK-035 AC-001）。
 *
 * <p><b>要守的是什么</b>：CodeMirror 是本站唯一的重前端依赖（精简版 gzip 约 137 kB）。
 * 它必须只在进入 `/practice/sql` 时才下载。一旦有人把 `@paideia/sql-workbench` 改成静态 import，
 * 构建照样成功、页面照样能用，**只是每个进首页的人都要多下 137 kB**——没有任何报错会提示这件事。
 *
 * <p><b>怎么判"静态"而不是"动态"</b>：`lazy()` 编译后长这样：
 * ```
 * ZO=(0,K.lazy)(async()=>({default:(await yv(()=>import(`./src-XXX.js`),...))}))
 * ```
 * 静态 import 则是顶层的 `import"./src-XXX.js"` 或 `from"./src-XXX.js"`。
 * 所以判据是：**在主 chunk 里，编辑器 chunk 的每一处出现都必须落在 `import(` 里面**。
 *
 * <p>不用「主 chunk 里不含该文件名」当判据：那会因为动态 import 的映射表而误报。
 */
import { readdirSync, readFileSync, statSync } from 'node:fs'
import { gzipSync } from 'node:zlib'
import { join } from 'node:path'

const DIST = process.argv[2] ?? 'dist/assets'
/** CodeMirror 的特征串：这些只会出现在编辑器代码里。 */
const MARKERS = ['cm-editor', 'cm-content', 'cm-gutters']
/** 门槛：编辑器 chunk gzip 后不得超过这个值（设计里定的 160 kB）。 */
const MAX_GZIP_KB = 160

function files(dir) {
  return readdirSync(dir)
    .filter((name) => name.endsWith('.js'))
    .map((name) => ({ name, path: join(dir, name), size: statSync(join(dir, name)).size }))
}

const all = files(DIST)
if (all.length === 0) {
  console.error(`✗ ${DIST} 里没有 .js —— 先跑构建`)
  process.exit(1)
}

// 1. 找出所有含 CodeMirror 特征的 chunk
const editorChunks = all.filter((file) => {
  const text = readFileSync(file.path, 'utf8')
  return MARKERS.some((marker) => text.includes(marker))
})

if (editorChunks.length === 0) {
  console.error('✗ 找不到含 CodeMirror 特征的 chunk —— 编辑器没被构建进去？')
  process.exit(1)
}
if (editorChunks.length > 1) {
  // 多于一个说明它被打散了，仍可能都只在 SQL 页加载，但值得看一眼
  console.warn(`! 有 ${editorChunks.length} 个 chunk 含编辑器代码：${editorChunks.map((f) => f.name).join(', ')}`)
}

// 2. 体积门槛（gzip）
let overLimit = false
for (const chunk of editorChunks) {
  const raw = readFileSync(chunk.path)
  const gz = gzipSync(raw).length
  const kb = gz / 1024
  const mark = kb > MAX_GZIP_KB ? '✗' : '✓'
  console.log(`${mark} ${chunk.name}: ${(raw.length / 1024).toFixed(1)} kB min / ${kb.toFixed(1)} kB gzip（门槛 ${MAX_GZIP_KB} kB）`)
  if (kb > MAX_GZIP_KB) {
    overLimit = true
  }
}

// 3. 静态引用检查：入口 chunk（`index-*.js`）里不许静态 import 编辑器 chunk。
//    只查入口而不是"最大的 chunk"：`3d-force-graph` 体积更大，但它本身也是按需加载的，
//    拿它当判据会看错地方。
//
//    **反向验证过的边界**：如果把编辑器改成静态 import，编辑器代码会并进入口 chunk，
//    于是"含编辑器特征"与"是入口"落在同一个文件上。那时不能因为"排除编辑器后没有入口"
//    就放行——那正是最坏的情况。所以这里先判这一条。
const editorNames = new Set(editorChunks.map((f) => f.name))
const entryCandidates = all.filter((file) => /^index-.*\.js$/.test(file.name))

if (entryCandidates.length === 0) {
  console.error('✗ 找不到入口 chunk（index-*.js）')
  process.exit(1)
}

const mergedIntoEntry = entryCandidates.filter((file) => editorNames.has(file.name))
if (mergedIntoEntry.length > 0) {
  console.error('')
  console.error('✗ 编辑器被静态打进入口 chunk（它已经和入口合成一个文件）：')
  for (const file of mergedIntoEntry) {
    console.error(`  - ${file.name}（${(file.size / 1024).toFixed(1)} kB min）既含编辑器代码又是入口`)
  }
  console.error('  修法：用 lazy(() => import("@paideia/sql-workbench"))，不要静态 import。')
  process.exit(1)
}

const entryChunks = entryCandidates

const violations = []

/**
 * Vite 会把所有动态导入的依赖清单集中成一个 `__vite__mapDeps([...])` 数组，
 * 里面**字面出现**每个懒加载 chunk 的文件名。那不是 import 语句，只是映射表。
 * 所以判据必须看**文件名前面紧邻的语法形态**，而不是"出现过就算"：
 *   - 静态：`from"./x.js"` / `import"./x.js"` / `import{...}from"./x.js"`
 *   - 动态：`import(\`./x.js\`)` / `import("./x.js")`
 *   - 映射表：`"./x.js"` 前面是 `,` 或 `[`（在数组里）
 */
function isStaticReference(text, index) {
  const before = text.slice(Math.max(0, index - 60), index)
  // 动态导入：文件名前有 import( 且中间没有闭合括号
  if (/import\(\s*[`'"]?\.?\/?$/.test(before)) {
    return false
  }
  // 数组元素（__vite__mapDeps 的清单）：前面是逗号或左方括号
  if (/[,[]\s*[`'"]?\.?\/?$/.test(before)) {
    return false
  }
  // 其余落在 from/import 之后的才算静态引用
  return /from\s*[`'"]?\.?\/?$|import\s*[`'"]?\.?\/?$/.test(before)
}

for (const mainChunk of entryChunks) {
  const mainText = readFileSync(mainChunk.path, 'utf8')
  for (const name of editorNames) {
    let index = 0
    while ((index = mainText.indexOf(name, index)) !== -1) {
      if (isStaticReference(mainText, index)) {
        const before = mainText.slice(Math.max(0, index - 60), index)
        violations.push(`${name} 在 ${mainChunk.name} 里被静态引用（上下文：…${before.slice(-50)}）`)
      }
      index += name.length
    }
  }
}

if (violations.length > 0) {
  console.error('')
  console.error('✗ 编辑器没有懒加载：')
  for (const violation of violations) {
    console.error(`  - ${violation}`)
  }
  console.error('  修法：用 lazy(() => import("@paideia/sql-workbench"))，不要静态 import。')
  process.exit(1)
}

if (overLimit) {
  console.error('')
  console.error(`✗ 编辑器 chunk 超过 ${MAX_GZIP_KB} kB gzip 门槛`)
  process.exit(1)
}

console.log('')
console.log(
  `✓ 懒加载守卫通过：编辑器在 ${[...editorNames].join(', ')}，入口 chunk ${entryChunks.map((f) => f.name).join(', ')} 只动态引用它`,
)
