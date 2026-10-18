/**
 * 学习者身份与画像的 DTO（WORK-010）。
 *
 * <p>**身份不是角色**：`role` 决定"能做什么"，身份只决定"默认推荐什么、助手以什么形态出现"。
 * 两者正交，前端也必须有这条纪律——**任何权限判断都不许读这里的字段**。
 */

/** 身份。一期只有 `job_seeker` 是实心的；其余是留位，界面上不出现。 */
export type Identity = 'job_seeker' | 'postgrad' | 'civil_service' | 'daily_learning'

/** 求职者画像的枚举取值（可空 = 用户没填）。 */
export type JobTimeline = 'autumn' | 'spring'
export type JobStage = 'applying' | 'written_test' | 'interview'

/**
 * 后端返回的画像。
 *
 * `selectableIdentities` 由后端给（哪几个身份能被选），前端**不自己写一份**：
 * 两处各写一份的话，加第二个真身份时必然有一边忘了改。
 */
export interface LearnerProfile {
  identity: Identity
  identityLabel: string
  targetRole: string | null
  targetCity: string | null
  timeline: JobTimeline | null
  stage: JobStage | null
  selectableIdentities: Identity[]
}

/** 保存画像的请求体。四个画像字段都可空——清空输入框等于"没有这个值"。 */
export interface LearnerProfileInput {
  identity: Identity
  targetRole: string | null
  targetCity: string | null
  timeline: JobTimeline | null
  stage: JobStage | null
}

/** 秋招 / 春招的显示名。 */
export const TIMELINE_LABELS: Record<JobTimeline, string> = {
  autumn: '秋招',
  spring: '春招',
}

/** 投递 / 笔试 / 面试的显示名。 */
export const STAGE_LABELS: Record<JobStage, string> = {
  applying: '投递中',
  written_test: '笔试',
  interview: '面试',
}
