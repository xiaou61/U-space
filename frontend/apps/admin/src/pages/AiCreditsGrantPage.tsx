import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Card,
  CardContent,
  Input,
  Label,
  PageHeader,
} from '@paideia/ui'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'

import { api } from '../api'

interface BalanceView {
  userId: number
  balance: number
}

const balance = (userId: string) => api.requestEnvelope<BalanceView>(`/api/v1/ai/admin/credits/${userId}`)

const adjust = (userId: string, delta: number, note: string) =>
  api.requestEnvelope<BalanceView>(`/api/v1/ai/admin/credits/${userId}/adjust`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ delta, note }),
  })

/**
 * 积分调整（WORK-009 REQ-004）。
 *
 * <p><b>本期没有充值</b>（用户决定 3）：积分只通过充值获得、而充值不做，所以这个页面就是
 * 积分唯一的入口——它就是本期的"充值"。
 *
 * <p>因此它也是**必须留痕**的写操作：数量与原因都会进流水，审计页上能看到"谁、什么时候、
 * 给谁、多少、为什么"。原因留空也能提交（后端只要求 delta 非 0），但填上才能在事后解释。
 *
 * <p>先"查余额"再调整：用户 id 是内部主键，敲错一位就会给另一个人发分，而这一步能顺手发现。
 */
export function AiCreditsGrantPage() {
  const [userId, setUserId] = useState('')
  const [delta, setDelta] = useState('')
  const [note, setNote] = useState('')
  const trimmedId = userId.trim()

  const current = useQuery({
    queryKey: ['ai-credits-balance', trimmedId],
    queryFn: () => balance(trimmedId),
    // 只有点了"查余额"才去查：否则边打字边发请求，而且打错一位会一直报错
    enabled: false,
    retry: false,
  })

  const change = useMutation({
    mutationFn: () => adjust(trimmedId, Number(delta), note.trim()),
    onSuccess: () => {
      void current.refetch()
    },
  })

  const onSubmit = (event: FormEvent) => {
    event.preventDefault()
    if (trimmedId === '' || delta.trim() === '' || Number(delta) === 0) {
      return
    }
    change.mutate()
  }

  return (
    <div className="space-y-4">
      <PageHeader
        title="积分调整"
        description="给某个用户增减 AI 积分。本项不做充值，这里是积分唯一的入口——每一次调整都会留下操作人与原因。"
      />

      {change.isError ? (
        <Alert variant="destructive" data-testid="credits-grant-error">
          <AlertTitle>没能完成</AlertTitle>
          <AlertDescription>
            {change.error instanceof Error ? change.error.message : String(change.error)}
          </AlertDescription>
        </Alert>
      ) : null}

      {change.isSuccess ? (
        <Alert data-testid="credits-grant-done">
          <AlertTitle>已调整</AlertTitle>
          <AlertDescription>
            用户 {change.data.userId} 现在的余额是 {change.data.balance} 分。
          </AlertDescription>
        </Alert>
      ) : null}

      <Card>
        <CardContent className="space-y-4 pt-(--card-spacing)">
          <form onSubmit={onSubmit} className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <Label htmlFor="credits-user-id">用户 id</Label>
                <div className="mt-1 flex gap-2">
                  <Input
                    id="credits-user-id"
                    value={userId}
                    inputMode="numeric"
                    placeholder="例如 12"
                    onChange={(event) => setUserId(event.target.value)}
                    data-testid="credits-user-id"
                  />
                  <Button
                    type="button"
                    variant="outline"
                    disabled={trimmedId === '' || current.isFetching}
                    onClick={() => void current.refetch()}
                    data-testid="credits-check-balance"
                  >
                    查余额
                  </Button>
                </div>
                {current.isSuccess ? (
                  <p className="text-muted-foreground mt-1 text-xs" data-testid="credits-current-balance">
                    当前余额 {current.data.balance} 分
                  </p>
                ) : null}
              </div>

              <div>
                <Label htmlFor="credits-delta">数量</Label>
                <Input
                  id="credits-delta"
                  className="mt-1"
                  value={delta}
                  inputMode="numeric"
                  placeholder="正数发放、负数回收"
                  onChange={(event) => setDelta(event.target.value)}
                  data-testid="credits-delta"
                />
              </div>
            </div>

            <div>
              <Label htmlFor="credits-note">原因</Label>
              <Input
                id="credits-note"
                className="mt-1"
                value={note}
                placeholder="例如：秋招活动发放"
                onChange={(event) => setNote(event.target.value)}
                data-testid="credits-note"
              />
              <p className="text-muted-foreground mt-1 text-xs">
                会连同操作人与时间一起进流水。留空也能提交，但事后要解释就靠它。
              </p>
            </div>

            <Button
              type="submit"
              disabled={change.isPending || trimmedId === '' || delta.trim() === '' || Number(delta) === 0}
              data-testid="credits-adjust-submit"
            >
              提交调整
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
