import { Tabs, TabsContent, TabsList, TabsTrigger } from '@paideia/ui'
import { useState } from 'react'

import { MarkdownBody } from './markdown-body'

/**
 * markdown 正文编辑器：**编辑 / 预览**两个标签共用一份内容。
 *
 * <p>**放在共享包里而不是某个应用里**：学习者端的笔记（新建页与详情页）与管理端的条目编辑
 * 用的是同一块东西——"预览等于最终效果"这条承诺，两边必须由同一份实现来兑现。
 *
 * <p>抽出来的原因很实际：新建页与详情页都要这一块，而"编辑"与"预览"的对应关系
 * （同一份文本、同一套渲染）是这个页面里最不能走样的部分——各写一份，两边迟早在
 * "预览里看到的"和"保存下来的"之间出现差异。
 *
 * <p>预览直接复用知识库的 `MarkdownBody`：学习者的笔记与知识库条目用的是同一套渲染，
 * 所以编辑器里的预览就是最终的样子，不需要第二套渲染实现。
 *
 * <p>`testIdPrefix` 让两个页面各有稳定的测试钩子（`note-body` / `new-note-body`…），
 * 而不是让测试去猜 DOM 结构。
 */
export function MarkdownField({
  value,
  onChange,
  testIdPrefix,
  rows = 16,
  placeholder = '用 markdown 写点什么…',
  mediaBaseUrl,
}: {
  value: string
  onChange: (next: string) => void
  testIdPrefix: string
  rows?: number
  placeholder?: string
  /** 正文里相对附件地址（`/api/v1/knowledge/files/...`）要拼上的后端基址。 */
  mediaBaseUrl?: string
}) {
  const [tab, setTab] = useState('edit')

  return (
    // `flex-col` 显式写出来：Tabs 根是靠 `data-horizontal:flex-col` 竖排的，而在实际渲染里
    // 那个变体没有生效（编辑/预览被排到了正文左侧），所以这里把方向写死
    <Tabs value={tab} onValueChange={(next) => setTab(String(next))} className="flex flex-col gap-3">
      <TabsList variant="line">
        <TabsTrigger value="edit" data-testid={`${testIdPrefix}-tab-edit`}>
          编辑
        </TabsTrigger>
        <TabsTrigger value="preview" data-testid={`${testIdPrefix}-tab-preview`}>
          预览
        </TabsTrigger>
      </TabsList>

      <TabsContent value="edit">
        <textarea
          value={value}
          rows={rows}
          aria-label="正文（markdown）"
          placeholder={placeholder}
          data-testid={`${testIdPrefix}-body`}
          onChange={(event) => onChange(event.target.value)}
          className="border-border bg-background focus:border-ring w-full resize-y rounded-lg border p-4 font-mono text-sm leading-7 outline-none"
        />
      </TabsContent>

      <TabsContent value="preview">
        {value.trim() === '' ? (
          <p className="text-muted-foreground py-6 text-sm" data-testid={`${testIdPrefix}-preview-empty`}>
            还没有正文。
          </p>
        ) : (
          <div className="border-border rounded-lg border p-4" data-testid={`${testIdPrefix}-preview`}>
            <MarkdownBody markdown={value} mediaBaseUrl={mediaBaseUrl} />
          </div>
        )}
      </TabsContent>
    </Tabs>
  )
}
