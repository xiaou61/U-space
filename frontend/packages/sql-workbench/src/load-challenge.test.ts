import { describe, expect, it } from 'vitest'

import { findSqlChallenge } from './load-challenge'

/**
 * 从条目正文取关卡题（WORK-035 T-14）。
 *
 * <p>语料取自真实课程内容（`content/courses/mysql/07-索引与约束.md` 的 `my-07-write` 块形状）。
 *
 * <p>要守的是**取不到就返回 null，不抛**：调用方是"从关卡跳过来试一下"，
 * 拿不到题该退回普通工作台，而不是让页面崩。
 */

const BODY = `
## 动手

这道题会在真实的 MySQL 里跑。

\`\`\`code-challenge
{
  "id": "my-07-write",
  "kind": "sql-write",
  "title": "列出每个客户最近一笔订单",
  "goal": "用相关子查询取「每组最大值」",
  "dataset": ["orders-public", "orders-hidden-a"],
  "normalize": { "rowOrder": "ignore" },
  "expectHash": { "orders-public": "abc", "orders-hidden-a": "def" },
  "explain": "MAX 取出的是时间，不是订单号",
  "starter": "SELECT\\n  \\nFROM ;"
}
\`\`\`

后面还有别的段落。
`

describe('findSqlChallenge', () => {
  it('按 blockKey 取出题目', () => {
    const challenge = findSqlChallenge(BODY, 'my-07-write')
    expect(challenge).not.toBeNull()
    expect(challenge?.title).toBe('列出每个客户最近一笔订单')
    expect(challenge?.starter).toContain('SELECT')
    expect(challenge?.dataset).toEqual(['orders-public', 'orders-hidden-a'])
  })

  it('id 不匹配返回 null', () => {
    expect(findSqlChallenge(BODY, 'my-99-write')).toBeNull()
  })

  it('正文为空返回 null', () => {
    expect(findSqlChallenge(null, 'my-07-write')).toBeNull()
    expect(findSqlChallenge(undefined, 'my-07-write')).toBeNull()
    expect(findSqlChallenge('', 'my-07-write')).toBeNull()
  })

  it('blockKey 为空返回 null', () => {
    expect(findSqlChallenge(BODY, '')).toBeNull()
  })

  /** 坏 JSON 不该抛——正文里可能有作者写错的块。 */
  it('坏块被跳过而不是抛异常', () => {
    const broken = '```code-challenge\n{ not json }\n```'
    expect(() => findSqlChallenge(broken, 'x')).not.toThrow()
    expect(findSqlChallenge(broken, 'x')).toBeNull()
  })

  /** 非 SQL 题型在同一个 id 上返回 null（工作台只有一个 SQL 编辑器）。 */
  it('非 SQL 题型返回 null', () => {
    const fill = `
\`\`\`code-challenge
{
  "id": "fill-1",
  "kind": "fill",
  "title": "填空",
  "template": "int a = {{1}};",
  "blanks": [{ "answer": "1" }]
}
\`\`\`
`
    expect(findSqlChallenge(fill, 'fill-1')).toBeNull()
  })

  it('多个块时只取匹配的那个', () => {
    const two = `
\`\`\`code-challenge
{"id":"a","kind":"sql-write","title":"A","dataset":["orders-public"],"starter":"SELECT 1;","expectHash":{"orders-public":"x"}}
\`\`\`

\`\`\`code-challenge
{"id":"b","kind":"sql-write","title":"B","dataset":["orders-public"],"starter":"SELECT 2;","expectHash":{"orders-public":"y"}}
\`\`\`
`
    expect(findSqlChallenge(two, 'b')?.title).toBe('B')
    expect(findSqlChallenge(two, 'a')?.title).toBe('A')
  })
})
