/**
 * 「3 分钟前」这类相对时间。
 *
 * <p>放在 core 而不是应用里：它是纯函数，与平台、与 React 都无关，而 core 本来就有单测环境
 * （`apps/app` 只有 e2e）。列表里每一行都要显示时间，这是"一眼看出哪条是刚改的"的主要线索。
 *
 * <p>三条取舍：
 * <ul>
 *   <li><b>超过一个月就显示日期</b>："3 个月前"对"我上周改过的那条"没有帮助，具体日期才有。</li>
 *   <li><b>同年不写年份</b>：列表里绝大多数是今年的，写上年份只是噪音。</li>
 *   <li><b>只处理"过去"</b>：时间戳来自服务端的 updated_at。真遇到未来时间（两端时钟不同步）
 *       一律显示"刚刚"，而不是"-3 分钟前"。</li>
 * </ul>
 */
export function formatRelative(iso: string | null | undefined, now: Date = new Date()): string {
  if (iso === null || iso === undefined || iso === '') {
    return ''
  }
  const then = new Date(iso)
  if (Number.isNaN(then.getTime())) {
    return ''
  }

  const seconds = Math.floor((now.getTime() - then.getTime()) / 1000)
  // 未来时间（两端时钟不同步）与"刚刚"同一条分支：负数当然小于 60
  if (seconds < 60) {
    return '刚刚'
  }

  // **先判"隔了几个日历天"，再判小时/分钟**。反过来会错：昨晚 23:00 只差 15 小时，
  // 会先说成"15 小时前"而永远轮不到"昨天"；前天夜里那条更是被说成"38 小时前"——
  // 用户嘴里的"昨天""前天"就是按日历天数的，不是按 24 小时的倍数。
  const dayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const thenDayStart = new Date(then.getFullYear(), then.getMonth(), then.getDate())
  const days = Math.round((dayStart.getTime() - thenDayStart.getTime()) / 86_400_000)

  if (days >= 1) {
    if (days === 1) {
      return '昨天'
    }
    if (days < 30) {
      return `${days} 天前`
    }
    const month = then.getMonth() + 1
    const date = then.getDate()
    return then.getFullYear() === now.getFullYear()
      ? `${month} 月 ${date} 日`
      : `${then.getFullYear()} 年 ${month} 月 ${date} 日`
  }

  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) {
    return `${minutes} 分钟前`
  }
  return `${Math.floor(minutes / 60)} 小时前`
}
