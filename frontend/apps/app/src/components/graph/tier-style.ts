import type { MasteryTier } from '@paideia/knowledge'

/**
 * 掌握度档位的视觉映射（WORK-021）。
 *
 * <p>**单一来源**：图谱节点、详情面板、图例都从这里取颜色与文案。三处各写一份必然漂移，
 * 而"图上说是熟悉、面板上说是了解"是会让用户怀疑整张图的那种不一致。
 */
export const TIER_STYLE: Record<MasteryTier, { label: string; hint: string; color: string; ring: string }> = {
  unknown: {
    label: '未接触',
    hint: '还没读过承载这个概念的内容',
    color: '#94a3b8',
    ring: 'border-muted-foreground/30',
  },
  learning: {
    label: '了解',
    hint: '看过相关内容，但还没有可靠的证据说明掌握了',
    color: '#60a5fa',
    ring: 'border-blue-400/40',
  },
  familiar: {
    label: '熟悉',
    hint: '自评会、或练习里拿到了及格以上的分数',
    color: '#34d399',
    ring: 'border-emerald-400/40',
  },
  mastered: {
    label: '掌握',
    hint: '练习达标且自评懂，两路证据一致',
    color: '#4f46e5',
    ring: 'border-indigo-400/50',
  },
}

export function tierStyle(tier: MasteryTier) {
  return TIER_STYLE[tier] ?? TIER_STYLE.unknown
}
