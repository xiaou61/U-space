import { describe, expect, it } from 'vitest'

import { configFor } from './config'
import type { Identity } from './types'

/**
 * 按身份的推荐配置（WORK-010 T-12）。
 *
 * <p>这里验的是"配置真的按身份分开了"，而不是"某个字符串长什么样"——后者改一次文案就红，
 * 前者才是这一层要守的东西。
 */
describe('按身份的推荐配置', () => {
  it('求职者的首页推荐与导航顺序与其它身份不同（否则切换身份看不出变化）', () => {
    const jobSeeker = configFor('job_seeker')
    const daily = configFor('daily_learning')

    // 首页推荐分类不同——这是 AC-002「切换后首页顺序变化」的数据来源
    expect(jobSeeker.preferredCategories).not.toEqual(daily.preferredCategories)
    // 求职者导航里把面试排第一，其它身份没有面试入口
    expect(jobSeeker.preferredNav[0]).toBe('interview')
    expect(daily.preferredNav).not.toContain('interview')
    // 开场白不同：求职者会提到找工作与模拟面试
    expect(jobSeeker.assistantGreeting).toContain('找工作')
    expect(daily.assistantGreeting).not.toContain('找工作')
  })

  it('每个身份的 preferredNav 都含 digest——漏一处首页排序就取不到那张卡（WORK-027）', () => {
    const all: Identity[] = ['job_seeker', 'postgrad', 'civil_service', 'daily_learning']
    for (const identity of all) {
      expect(configFor(identity).preferredNav).toContain('digest')
    }
  })

  it('留位的三个身份也各有一份配置，不会拿到空壳', () => {
    const placeholders: Identity[] = ['postgrad', 'civil_service', 'daily_learning']
    for (const identity of placeholders) {
      const config = configFor(identity)
      expect(config.preferredCategories.length).toBeGreaterThan(0)
      expect(config.preferredNav.length).toBeGreaterThan(0)
      expect(config.assistantGreeting.length).toBeGreaterThan(0)
      expect(config.interviewIntro.length).toBeGreaterThan(0)
    }
  })

  it('身份缺失或不认识时退回求职者配置——与后端同一个兜底方向', () => {
    // 两处兜底方向必须一致：一边退回 job_seeker、另一边退回 assistant 行为，
    // 否则"没有画像的新用户"会看到一个与后端行为不符的界面。
    expect(configFor(null)).toEqual(configFor('job_seeker'))
    expect(configFor(undefined)).toEqual(configFor('job_seeker'))
    expect(configFor('nonsense' as Identity)).toEqual(configFor('job_seeker'))
  })
})
