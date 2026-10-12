import { Button, Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, Input, Label } from '@paideia/ui'
import { useState, type FormEvent } from 'react'

import type { AssistantApi } from './client'
import type { CredentialView } from './types'

/**
 * 模型凭据设置。
 *
 * <p>放在悬浮壳内的弹窗里（而不是独立设置页）：用它的入口就在壳上，设置也就该在那儿。
 *
 * <p>只回显 **last4**：用户需要确认"我配的是哪一把"，但任何界面、任何响应都不该出现明文 Key——
 * 这条由服务端保证，这里只是不去期待一个拿不到的字段。
 */
export function CredentialDialog({
  api,
  current,
  open,
  onClose,
  onSaved,
  onError,
}: {
  api: AssistantApi
  current: CredentialView | null
  open: boolean
  onClose: () => void
  onSaved: () => void
  onError: (message: string) => void
}) {
  const [baseUrl, setBaseUrl] = useState(current?.baseUrl ?? 'https://api.deepseek.com')
  const [model, setModel] = useState(current?.model ?? 'deepseek-flash')
  const [apiKey, setApiKey] = useState('')
  const [saving, setSaving] = useState(false)

  if (!open) {
    return null
  }

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault()
    if (apiKey.trim() === '') {
      onError('请填写 API Key（已保存的那把不会回显，换 Key 就重新填一次）')
      return
    }
    setSaving(true)
    try {
      await api.saveCredential({ baseUrl: baseUrl.trim(), model: model.trim(), apiKey })
      setApiKey('')
      onSaved()
      onClose()
    } catch (cause) {
      onError(cause instanceof Error ? cause.message : String(cause))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open onOpenChange={() => onClose()}>
      <DialogContent data-testid="assistant-credential-dialog">
        <DialogHeader>
          <DialogTitle>配置你的模型凭据</DialogTitle>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-3">
          <div>
            <Label htmlFor="assistant-base-url">baseUrl</Label>
            <Input
              id="assistant-base-url"
              className="mt-1"
              value={baseUrl}
              onChange={(event) => setBaseUrl(event.target.value)}
              data-testid="assistant-base-url"
            />
          </div>
          <div>
            <Label htmlFor="assistant-model">模型</Label>
            <Input
              id="assistant-model"
              className="mt-1"
              value={model}
              onChange={(event) => setModel(event.target.value)}
              data-testid="assistant-model"
            />
          </div>
          <div>
            <Label htmlFor="assistant-api-key">API Key</Label>
            <Input
              id="assistant-api-key"
              className="mt-1"
              type="password"
              value={apiKey}
              onChange={(event) => setApiKey(event.target.value)}
              data-testid="assistant-api-key"
            />
            <p className="text-muted-foreground mt-1 text-[11px]">
              {current?.configured === true
                ? `已配置 ····${current.apiKeyLast4}；重新填写会覆盖它。`
                : '只保存在服务端（加密存储），界面上永远只显示后四位。'}
            </p>
          </div>
          <DialogFooter>
            <Button type="button" variant="ghost" onClick={onClose}>
              取消
            </Button>
            <Button type="submit" disabled={saving || model.trim() === ''} data-testid="assistant-credential-save">
              保存
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
