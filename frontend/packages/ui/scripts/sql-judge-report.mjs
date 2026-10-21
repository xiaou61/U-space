/**
 * 把 TS 侧的判题结论打成 JSON，供 `scripts/check_sql_judge_parity.py` 与 Python 侧逐条比对
 * （WORK-032 TASK-004，AC-014）。
 *
 * 走 Node 直接 import `.ts` 是不行的（没有编译步骤），所以这里用 vite 的 `ssrLoadModule`
 * 加载源文件——它认得 TypeScript，也不需要先构建一遍包。
 *
 * 用法：node scripts/sql-judge-report.mjs
 */
import { readFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

import { createServer } from 'vite'

const here = dirname(fileURLToPath(import.meta.url))
const uiRoot = resolve(here, '..')
const corpusPath = resolve(uiRoot, '../../../tests/sql-judge/corpus.json')

const server = await createServer({
  root: uiRoot,
  configFile: false,
  server: { middlewareMode: true },
  appType: 'custom',
  logLevel: 'error',
})

try {
  const module = await server.ssrLoadModule('/src/markdown/renderers/sql-judge.ts')
  const corpus = JSON.parse(readFileSync(corpusPath, 'utf8'))
  const report = {}
  for (const item of corpus.cases) {
    report[item.name] = {
      pass: module.judgeSql(item.expected, item.actual, item.normalize ?? {}),
      canonical: module.canonicalize(item.expected, item.normalize ?? {}),
    }
  }
  process.stdout.write(`${JSON.stringify(report, null, 2)}\n`)
} finally {
  await server.close()
}
