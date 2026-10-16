/**
 * 当前会话的本地记忆（WORK-011）。
 *
 * <p><b>三个状态必须分清</b>——它们都表现为"没有当前会话"，但意思完全不同：
 * <ul>
 *   <li><b>键不存在</b>：从没选过 → 打开面板时**恢复最近的会话**；</li>
 *   <li><b>值为空串</b>：刚点了「新开对话」 → 面板保持空白，**不要再**去恢复旧的；</li>
 *   <li><b>值为数字</b>：恢复这一条。</li>
 * </ul>
 * 少了这个区分，"点了新开、关掉面板、再打开"就会把刚被丢开的那条又拉回来。
 *
 * <p><b>放本地而不是服务端</b>：面板的展开/宽度/当前会话是**这台设备上的使用状态**，
 * 不是学习数据；进库只会多一份需要同步（换端会互相覆盖）与清理的东西。
 *
 * <p>读写都吞掉异常（隐私模式、配额满）：拿不到就当"没选过"，写不进就只影响下一次。
 */
const KEY = 'paideia:assistant-conversation'

export type StoredConversation =
  | { kind: 'unset' }
  | { kind: 'new' }
  | { kind: 'id'; id: number }

export function readStoredConversation(): StoredConversation {
  let raw: string | null
  try {
    raw = window.localStorage.getItem(KEY)
  } catch {
    return { kind: 'unset' }
  }
  if (raw === null) {
    return { kind: 'unset' }
  }
  if (raw === '') {
    return { kind: 'new' }
  }
  const id = Number(raw)
  return Number.isInteger(id) && id > 0 ? { kind: 'id', id } : { kind: 'unset' }
}

/** {@code null} 表示"刚新开、还没有会话"（写空串，与"从没选过"区分开）。 */
export function storeConversation(id: number | null): void {
  try {
    window.localStorage.setItem(KEY, id === null ? '' : String(id))
  } catch {
    // 写不进去不影响本次使用，只是下次打开会回到"最近会话"
  }
}

/**
 * 入口指引是否已经给过（WORK-011）。
 *
 * <p><b>指引只给一次</b>：用户打开过助手之后就永久不再提示，动效也随之消失。
 * 常驻动效是调研里点名的暗黑模式反模式之一——"协助不该变成劫持"。所以这里记的不是
 * "看过动画"，而是"**打开过助手**"。
 */
const HINT_KEY = 'paideia:assistant-hint-seen'

export function hasSeenHint(): boolean {
  try {
    return window.localStorage.getItem(HINT_KEY) === '1'
  } catch {
    // 读不到就不提示：宁可少一次指引，也不要在记不住的环境里反复弹
    return true
  }
}

export function markHintSeen(): void {
  try {
    window.localStorage.setItem(HINT_KEY, '1')
  } catch {
    // 记不住就下次再提示一次，无伤
  }
}
