import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

import { describe, expect, it } from 'vitest'

import { canonicalize, judgeSql, type SqlNormalize, type SqlTable } from './sql-judge'

/**
 * 判题口径的语料测试（WORK-032 TASK-004）。
 *
 * <p>语料 `tests/sql-judge/corpus.json` 同时被 `scripts/sql_judge.py` 消费。**两侧各自实现、
 * 同一份语料对照**——这是防止判题规则漂移的唯一护栏。
 *
 * <p>除了「判定结果一致」，语料还对 `canonical`（规范序列化）断言：隐藏数据集的期望结果
 * 靠这个串的 SHA-256 传输，两侧差一个分隔符就会让所有隐藏集判错。
 */
interface CorpusCase {
  name: string
  why: string
  normalize: SqlNormalize
  expected: SqlTable
  actual: SqlTable
  pass: boolean
  canonical?: string
}

interface Corpus {
  version: number
  nullMarker: string
  cases: CorpusCase[]
}

const here = dirname(fileURLToPath(import.meta.url))
const corpusPath = join(here, '../../../../../../tests/sql-judge/corpus.json')

describe('SQL 判题语料', () => {
  const corpus = JSON.parse(readFileSync(corpusPath, 'utf8')) as Corpus

  it('语料本身不是空的（防止文件路径写错却静默通过）', () => {
    expect(corpus.cases.length).toBeGreaterThan(10)
  })

  for (const item of corpus.cases) {
    it(`${item.name}：${item.why}`, () => {
      expect(judgeSql(item.expected, item.actual, item.normalize)).toBe(item.pass)
    })

    if (item.canonical !== undefined) {
      it(`${item.name}：规范序列化与语料一致`, () => {
        expect(canonicalize(item.expected, item.normalize)).toBe(item.canonical)
      })
    }
  }
})
