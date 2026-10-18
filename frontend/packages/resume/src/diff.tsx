import { Fragment } from 'react'

/**
 * 改动高亮（WORK-018 AC-003「对比可见」）。
 *
 * <p>要求是"能看见原文 vs 改后"，而不是把两段文字并排贴上就完事。这里做一个**字符级**的
 * 最小差异：找出两段共有的最长公共前缀与后缀，中间那块就是改动。
 *
 * <p>为什么不用真正的 LCS 或 diff 库：
 * <ul>
 *   <li>输入是**一条简历要点**（几十字），不是整篇文档——前缀/后缀就能覆盖绝大多数真实改写
 *       （"把接口响应从 800ms 降到 200ms" → "高并发场景下把接口响应从 800ms 降到 200ms"
 *       改的是开头；"…团队交付" → "…团队交付，带 8 人"改的是结尾）。</li>
 *   <li>引一个 diff 库要加依赖与体积，换来的只是中间插入块的高亮更细。等真的出现
 *       "中间大段重写"的样本再升级。</li>
 * </ul>
 *
 * <p>ponytail: 前缀/后缀差异。天花板是"中间同时有多处改动"时会被标成一整块；
 * 升级路径是换成 LCS 或词级 diff。
 */

export interface DiffPart {
  text: string
  changed: boolean
}

/** 计算最小差异片段。相同的前缀与后缀保持 `changed=false`，中间那段是 `changed=true`。 */
export function diffParts(before: string, after: string): DiffPart[] {
  if (before === after) {
    return after === '' ? [] : [{ text: after, changed: false }]
  }
  const prefix = commonPrefix(before, after)
  const suffix = commonSuffix(before, after, prefix)

  const parts: DiffPart[] = []
  const beforeMiddle = before.slice(prefix, before.length - suffix)
  const afterMiddle = after.slice(prefix, after.length - suffix)
  const head = before.slice(0, prefix)
  const tail = suffix === 0 ? '' : after.slice(after.length - suffix)

  // 原文的中间段没有出现在"改后"里 —— 只有 in-place 展示时才需要（见 ChangedText）
  void beforeMiddle

  if (head !== '') {
    parts.push({ text: head, changed: false })
  }
  if (afterMiddle !== '') {
    parts.push({ text: afterMiddle, changed: true })
  }
  if (tail !== '') {
    parts.push({ text: tail, changed: false })
  }
  return parts
}

/** 被删掉的那部分（原文里有、改后没有）。 */
export function removedParts(before: string, after: string): DiffPart[] {
  if (before === after || before === '') {
    return []
  }
  const prefix = commonPrefix(before, after)
  const suffix = commonSuffix(before, after, prefix)
  const middle = before.slice(prefix, before.length - suffix)
  return middle === '' ? [] : [{ text: middle, changed: true }]
}

function commonPrefix(a: string, b: string): number {
  const limit = Math.min(a.length, b.length)
  let index = 0
  while (index < limit && a[index] === b[index]) {
    index++
  }
  return index
}

function commonSuffix(a: string, b: string, prefix: number): number {
  const limit = Math.min(a.length, b.length) - prefix
  let index = 0
  while (index < limit && a[a.length - 1 - index] === b[b.length - 1 - index]) {
    index++
  }
  return index
}

/** 改后文本，改动处用 `<mark>` 标出。 */
export function ChangedText({ text, before }: { text: string; before: string }) {
  const parts = diffParts(before, text)
  return (
    <span>
      {parts.map((part, index) => (
        <Fragment key={index}>
          {part.changed ? <mark className="rounded-sm bg-[var(--color-accent)]/25 px-0.5">{part.text}</mark> : part.text}
        </Fragment>
      ))}
    </span>
  )
}

/** 原文，被删掉的部分标出（让用户看见"改后少了什么"）。 */
export function RemovedText({ text, after }: { text: string; after: string }) {
  const parts = removedParts(text, after)
  if (parts.length === 0) {
    return <span>{text}</span>
  }
  const prefix = commonPrefix(text, after)
  const suffix = commonSuffix(text, after, prefix)
  return (
    <span>
      {text.slice(0, prefix)}
      {parts.map((part, index) => (
        <mark key={index} className="rounded-sm bg-[var(--color-destructive)]/20 px-0.5 line-through">
          {part.text}
        </mark>
      ))}
      {suffix === 0 ? '' : text.slice(text.length - suffix)}
    </span>
  )
}
