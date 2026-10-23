import { parseCodeChallenge } from '@paideia/ui'

/**
 * 从条目正文里取出一道 SQL 关卡题（WORK-035 T-14）。
 *
 * <p><b>为什么在客户端解析而不是后端给接口</b>：题面就是条目正文里的一个围栏块，
 * 后端已经有"取条目正文"的接口（`knowledge.entry(id)`）。再开一个"按 blockKey 取题"的
 * 接口，等于把同一份内容解析两次，而两处的解析结果迟早会不一致。
 *
 * <p><b>只认 `sql-write` / `sql-fix`</b>：其它题型（填空、排序、改错、运行）在工作台里
 * 没有意义——工作台只有一个 SQL 编辑器。
 */

/** 一个围栏块的原文：```code-challenge ... ``` */
const CHALLENGE_BLOCK = /```code-challenge\s*\n([\s\S]*?)```/g

export interface LoadedChallenge {
  id: string
  title: string
  goal?: string
  /** 起始 SQL（`sql-write` 是骨架，`sql-fix` 是写坏的那段）。 */
  starter: string
  /** 这道题能用的数据集 key。 */
  dataset: string[]
}

/**
 * 在正文里找 `blockKey` 对应的 SQL 题。
 *
 * <p>找不到或解析失败返回 null——**不要抛**：调用方是"从关卡跳过来试一下"，
 * 拿不到题就退回普通工作台，不该让页面崩。
 */
export function findSqlChallenge(body: string | null | undefined, blockKey: string): LoadedChallenge | null {
  if (body === null || body === undefined || blockKey === '') {
    return null
  }
  for (const match of body.matchAll(CHALLENGE_BLOCK)) {
    const raw = match[1]
    if (raw === undefined) {
      continue
    }
    const parsed = parseCodeChallenge(raw)
    if (!parsed.ok) {
      continue
    }
    const challenge = parsed.value
    if (challenge.id !== blockKey) {
      continue
    }
    if (challenge.kind !== 'sql-write' && challenge.kind !== 'sql-fix') {
      // 找到了 id 但不是 SQL 题：不是错误，只是这道题不该在工作台里打开
      return null
    }
    return toLoaded(challenge)
  }
  return null
}

/**
 * 用结构类型而不是 `SqlChallenge`：那个类型没有从 `@paideia/ui` 导出，
 * 而为一个内部类型去改公共包的导出面不值得。这里只依赖实际用到的四个字段。
 */
function toLoaded(challenge: {
  id: string
  title: string
  goal?: string
  starter: string
  dataset: string[]
}): LoadedChallenge {
  return {
    id: challenge.id,
    title: challenge.title,
    ...(challenge.goal === undefined ? {} : { goal: challenge.goal }),
    starter: challenge.starter,
    dataset: challenge.dataset,
  }
}
