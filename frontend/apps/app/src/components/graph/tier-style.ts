import type { MasteryTier } from '@paideia/knowledge'

/**
 * 掌握度档位的视觉映射（WORK-021）。
 *
 * <p>**单一来源**：图谱节点、详情面板、图例都从这里取颜色与文案。三处各写一份必然漂移，
 * 而"图上说是熟悉、面板上说是了解"是会让用户怀疑整张图的那种不一致。
 *
 * <p><b>颜色来自令牌，不写死</b>：图谱画布（sigma / 3d-force-graph）要的是可解析的颜色值，
 * 类名给不了它。所以这里只声明**变量名**，真实值在 `packages/ui/src/styles/globals.css`
 * 的 `--tier-*` 里，浅深两套各一份。`tokens.test.ts` 守着"应用里不许出现硬编码颜色"这条。
 */
export const TIER_STYLE: Record<MasteryTier, { label: string; hint: string; cssVar: string }> = {
  unknown: {
    label: '未接触',
    hint: '还没读过承载这个概念的内容',
    cssVar: '--tier-unknown',
  },
  learning: {
    label: '了解',
    hint: '看过相关内容，但还没有可靠的证据说明掌握了',
    cssVar: '--tier-learning',
  },
  familiar: {
    label: '熟悉',
    hint: '自评会、或练习里拿到了及格以上的分数',
    cssVar: '--tier-familiar',
  },
  mastered: {
    label: '掌握',
    hint: '练习达标且自评懂，两路证据一致',
    cssVar: '--tier-mastered',
  },
}

export function tierStyle(tier: MasteryTier) {
  return TIER_STYLE[tier] ?? TIER_STYLE.unknown
}

/**
 * 解析成画布能用的颜色值。
 *
 * <p>只能在浏览器里调（读 `document`）：画布组件都在 `useEffect` 里用，那时主题已经挂上。
 * SSR / 测试环境下退回 `currentColor`，不会抛。
 */
export function tierColor(tier: MasteryTier): string {
  return resolveVar(tierStyle(tier).cssVar)
}

/** 读一个 CSS 自定义属性；读不到时退回 `currentColor`（画布会继承文本色，不会变成透明）。 */
export function resolveVar(name: string): string {
  if (typeof document === 'undefined') {
    return 'currentColor'
  }
  const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  return value === '' ? 'currentColor' : value
}
