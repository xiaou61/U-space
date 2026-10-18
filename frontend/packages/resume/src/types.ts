/**
 * 简历的 DTO（WORK-018）。
 *
 * <p>结构与后端 `ResumeData` 一一对应，但不共享代码（前后端不同语言）。两处改一处会立刻
 * 表现为类型错误或接口 400，所以不需要再加一层共享 schema。
 *
 * <p>**它不含任何权限语义**：简历的归属完全由后端 SQL 校验，前端拿到的永远只是"我的"。
 */

export interface ResumeBasics {
  name: string
  phone: string
  email: string
  city: string
  links: string[]
}

export interface ResumeIntent {
  role: string
  city: string
  availability: string
}

export interface ResumeEducation {
  school: string
  degree: string
  major: string
  start: string
  end: string
  highlights: string[]
}

export interface ResumeWork {
  company: string
  title: string
  start: string
  end: string
  summary: string
  bullets: string[]
}

export interface ResumeProject {
  name: string
  role: string
  period: string
  bullets: string[]
}

export interface ResumeSkillGroup {
  group: string
  items: string[]
}

export interface ResumeAward {
  name: string
  issuer: string
  date: string
}

export interface ResumeData {
  basics: ResumeBasics
  intent: ResumeIntent
  education: ResumeEducation[]
  work: ResumeWork[]
  projects: ResumeProject[]
  skills: ResumeSkillGroup[]
  awards: ResumeAward[]
  selfEvaluation: string
}

/** 解析草稿。`plainText` 是抽取出的原文，保存时一起回传（事实闸门的比对基准）。 */
export interface ResumeDraft {
  data: ResumeData
  plainText: string
  dropped: number
  truncatedFields: number
  truncatedChars: number
  pages: number
}

export interface ResumeDocumentSummary {
  id: number
  title: string
  isDefault: boolean
  revision: number
  updatedAt: string
}

export interface ResumeDocument {
  id: number
  title: string
  isDefault: boolean
  revision: number
  data: ResumeData
  updatedAt: string
}

export interface ResumeHistoryEntry {
  id: number
  createdAt: string
}

/** 体检项。`ok=false` 的那几条才是要用户动手的。 */
export interface CheckupItem {
  code: string
  title: string
  ok: boolean
  detail: string
  location: string | null
}

export interface Checkup {
  ok: boolean
  items: CheckupItem[]
}

/** 三桶。名字与后端一致，界面按它分组。 */
export type KeywordBucket = 'existing' | 'evidenced' | 'gap'

export interface ClassifiedKeyword {
  term: string
  quote: string
  bucket: KeywordBucket
  hit: string | null
}

export interface ResumeAnalysis {
  summary: string
  classified: ClassifiedKeyword[]
  covered: number
  total: number
}

export type ChangeType = 'rewrite' | 'reorder' | 'add' | 'remove'
export type GateStatus = 'pending' | 'passed' | 'failed'

export interface ResumeProposal {
  id: number
  seq: number
  entryRef: string
  field: string
  originalText: string
  proposedText: string
  changeType: ChangeType
  rationale: string
  coversKeywords: string[]
  accepted: boolean
  gateStatus: GateStatus
  gateViolations: string[]
}

export interface ResumeVersionSummary {
  id: number
  company: string | null
  role: string | null
  status: 'draft' | 'finalized'
  proposalCount: number
  acceptedCount: number
  createdAt: string
}

export interface ResumeVersionDetail {
  id: number
  documentId: number
  company: string | null
  role: string | null
  status: 'draft' | 'finalized'
  analysis: ResumeAnalysis | null
  finalData: ResumeData | null
  createdAt: string
}

/** 空简历。新建与"模型什么都没识别出来"都用它。 */
export function emptyResume(): ResumeData {
  return {
    basics: { name: '', phone: '', email: '', city: '', links: [] },
    intent: { role: '', city: '', availability: '' },
    education: [],
    work: [],
    projects: [],
    skills: [],
    awards: [],
    selfEvaluation: '',
  }
}

/** 三桶的人话名。 */
export const BUCKET_LABELS: Record<KeywordBucket, string> = {
  existing: '技能区写明',
  evidenced: '正文出现过',
  gap: '缺口',
}
