import { createHash } from 'node:crypto'

import { describe, expect, it } from 'vitest'

import { sha256Hex } from './sha256'

/**
 * 纯 TS 的 SHA-256 必须与 Node 的实现逐字一致。
 *
 * <p>为什么值一条独立的测试：这段代码存在的理由是 `crypto.subtle` 在 http 下不可用
 * （线上已踩过），所以**不能反过来用它来验证自己**。用 Node 的 `createHash` 做对照——
 * 它是独立实现，两边算出的十六进制必须一样。
 *
 * <p>语料里刻意包含多字节字符与空串：判题的规范串里有中文列名、`\u001f`/`\u001e` 分隔符
 * 与 `\u0000NULL` 标记，UTF-8 编码错一位，哈希就全错，而那种错只会在部署后表现为"写对了却判错"。
 */
const SAMPLES = [
  '',
  'a',
  'abc',
  'id\u001fname\n1\u001fann',
  'total\n92.5',
  'cnt\u001fcreated\u001ftotal\n2\u001f2026-10-21 00:00:00\u001f487.5',
  'note\n\u0000NULL',
  '客户\t总额\n张三\t92.50',
  'x'.repeat(1000),
  'y'.repeat(55),
  'z'.repeat(56),
  'w'.repeat(64),
]

describe('纯 TS SHA-256', () => {
  for (const [index, sample] of SAMPLES.entries()) {
    it(`与 Node crypto 一致（样本 ${index}，长度 ${sample.length}）`, () => {
      const expected = createHash('sha256').update(sample, 'utf8').digest('hex')
      expect(sha256Hex(sample)).toBe(expected)
    })
  }

  it('空串的哈希是标准值', () => {
    expect(sha256Hex('')).toBe('e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855')
  })
})
