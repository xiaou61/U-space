import {
  IDENTITY_LABELS,
  STAGE_LABELS,
  TIMELINE_LABELS,
  createIdentityApi,
  type Identity,
  type JobStage,
  type JobTimeline,
  type LearnerProfileInput,
} from '@paideia/identity'
import { useQuery } from '@tanstack/react-query'
import { AppShell, Alert, AlertDescription, AlertTitle, Button, Card, CardContent, Input, Label, PageHeader, Spinner } from '@paideia/ui'
import { useEffect, useState } from 'react'

import { api } from '../api'
import { LearnerHeader } from '../components/LearnerHeader'

const identity = createIdentityApi(api)

/**
 * 我的身份与画像（WORK-010 T-03）。
 *
 * <p><b>这里改什么都不影响权限</b>：身份只决定"首页推荐什么、导航谁排前面、助手以什么形态出现"。
 * 页面顶部把这句话写出来，是因为它是最容易被误读的一处——"选了考研"不等于"失去了求职功能"。
 *
 * <p>四个画像字段全部可空：清空输入框等于"没有这个值"，不是存一个空字符串。
 */
export function ProfilePage() {
  const { data, isLoading, error } = useQuery({ queryKey: ['profile'], queryFn: () => identity.profile() })

  const [form, setForm] = useState<LearnerProfileInput | null>(null)
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  // 服务端值到位后填进表单。**只填一次**：否则用户正在改的时候后台刷新会把他的输入盖掉
  // （与笔记页那个"同步 effect"是同一类缺陷，这里从开始就避开）。
  useEffect(() => {
    if (data !== undefined && form === null) {
      setForm({
        identity: data.identity,
        targetRole: data.targetRole,
        targetCity: data.targetCity,
        timeline: data.timeline,
        stage: data.stage,
      })
    }
  }, [data, form])

  if (isLoading || form === null) {
    return (
      <AppShell title="Paideia" actions={<LearnerHeader />}>
        <Spinner />
      </AppShell>
    )
  }
  if (error !== null) {
    return (
      <AppShell title="Paideia" actions={<LearnerHeader />}>
        <Alert variant="destructive">
          <AlertTitle>读不到你的身份</AlertTitle>
          <AlertDescription>{error instanceof Error ? error.message : String(error)}</AlertDescription>
        </Alert>
      </AppShell>
    )
  }

  const selectable = data?.selectableIdentities ?? ['job_seeker']
  const patch = (next: Partial<LearnerProfileInput>) => {
    setSaved(false)
    setForm((current) => (current === null ? current : { ...current, ...next }))
  }

  const submit = async () => {
    setSaving(true)
    setSaveError(null)
    try {
      await identity.saveProfile(form)
      setSaved(true)
    } catch (cause) {
      setSaveError(cause instanceof Error ? cause.message : String(cause))
    } finally {
      setSaving(false)
    }
  }

  return (
    <AppShell title="Paideia" actions={<LearnerHeader />}>
      <div className="mx-auto max-w-2xl space-y-6">
        <PageHeader
          eyebrow="我的"
          title="身份与目标"
          description="身份只影响推荐顺序与助手的形态（比如求职者会多一个面试入口），不影响你能做什么——每个身份都能读全部内容、记笔记、用助手。"
        />

        <Card>
          <CardContent className="space-y-5 pt-6">
            <div className="space-y-2">
              <Label htmlFor="profile-identity">当前身份</Label>
              <select
                id="profile-identity"
                data-testid="profile-identity"
                value={form.identity}
                onChange={(event) => { patch({ identity: event.target.value as Identity }) }}
                className="border-input bg-background h-10 w-full rounded-md border px-3 text-sm"
              >
                {selectable.map((key) => (
                  <option key={key} value={key}>
                    {IDENTITY_LABELS[key]}
                  </option>
                ))}
              </select>
              <p className="text-muted-foreground text-xs">
                一期只有「{IDENTITY_LABELS.job_seeker}」可选用；其它身份还在准备中。
              </p>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="profile-role">目标岗位</Label>
                <Input
                  id="profile-role"
                  data-testid="profile-target-role"
                  placeholder="例如：后端开发"
                  value={form.targetRole ?? ''}
                  onChange={(event) => { patch({ targetRole: event.target.value }) }}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="profile-city">目标城市</Label>
                <Input
                  id="profile-city"
                  data-testid="profile-target-city"
                  placeholder="例如：武汉"
                  value={form.targetCity ?? ''}
                  onChange={(event) => { patch({ targetCity: event.target.value }) }}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="profile-timeline">招聘季</Label>
                <select
                  id="profile-timeline"
                  data-testid="profile-timeline"
                  value={form.timeline ?? ''}
                  onChange={(event) => {
                    patch({ timeline: event.target.value === '' ? null : (event.target.value as JobTimeline) })
                  }}
                  className="border-input bg-background h-10 w-full rounded-md border px-3 text-sm"
                >
                  <option value="">未填写</option>
                  {Object.entries(TIMELINE_LABELS).map(([key, label]) => (
                    <option key={key} value={key}>{label}</option>
                  ))}
                </select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="profile-stage">当前阶段</Label>
                <select
                  id="profile-stage"
                  data-testid="profile-stage"
                  value={form.stage ?? ''}
                  onChange={(event) => {
                    patch({ stage: event.target.value === '' ? null : (event.target.value as JobStage) })
                  }}
                  className="border-input bg-background h-10 w-full rounded-md border px-3 text-sm"
                >
                  <option value="">未填写</option>
                  {Object.entries(STAGE_LABELS).map(([key, label]) => (
                    <option key={key} value={key}>{label}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <Button data-testid="profile-save" disabled={saving} onClick={() => { void submit() }}>
                {saving ? '保存中…' : '保存'}
              </Button>
              {saved ? (
                <span className="text-muted-foreground text-sm" data-testid="profile-saved">已保存</span>
              ) : null}
            </div>

            {saveError === null ? null : (
              <Alert variant="destructive">
                <AlertTitle>没保存成功</AlertTitle>
                <AlertDescription>{saveError}</AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>
      </div>
    </AppShell>
  )
}
