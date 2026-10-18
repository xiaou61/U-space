import { Button, Spinner } from '@paideia/ui'
import { createResumeApi, type ResumeData } from '@paideia/resume'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Printer } from 'lucide-react'
import { useNavigate, useParams } from 'react-router'

import { api } from '../api'

const resumes = createResumeApi(api)

/**
 * 打印视图（WORK-018 REQ-021）。
 *
 * <p><b>刻意不复用 AppShell</b>：打印时页头、导航、助手面板都不该出现。这一页只有内容 +
 * 一个"返回"和一个"打印"按钮，两者在 `@media print` 下都被隐藏。
 *
 * <p>版式：A4 单栏、黑白可读、条目 `break-inside: avoid`（一条经历不被切成两页）。
 * 不嵌字体、不做分页计算——浏览器打印天然有这些能力。
 */
export function ResumePrintPage() {
  const { versionId } = useParams<{ versionId: string }>()
  const navigate = useNavigate()
  const id = Number(versionId)

  const { data: version, isPending } = useQuery({
    queryKey: ['resume-version', id],
    queryFn: () => resumes.getVersion(id),
    enabled: Number.isFinite(id),
  })

  if (isPending) {
    return (
      <div className="flex justify-center py-20">
        <Spinner />
      </div>
    )
  }

  if (version === undefined || version.finalData === null) {
    return (
      <div className="mx-auto max-w-2xl py-20 text-center">
        <p>这个版本还没有定稿，无法打印。</p>
        <Button className="mt-4" variant="outline" onClick={() => navigate(-1)}>
          返回
        </Button>
      </div>
    )
  }

  return (
    <div className="bg-background min-h-screen">
      {/* 打印时隐藏的工具条。用 data-print-hide 而不是 Tailwind 的 print: 变体：
          这一页的样式规则集中写在下面的 <style>，与 @media print 一起维护更好找。 */}
      <div className="mx-auto flex max-w-[210mm] items-center justify-between gap-2 px-6 py-4" data-print-hide>
        <Button variant="ghost" onClick={() => navigate(-1)}>
          <ArrowLeft className="size-4" />
          返回
        </Button>
        <Button onClick={() => window.print()}>
          <Printer className="size-4" />
          打印 / 存为 PDF
        </Button>
      </div>

      <article className="resume-print mx-auto max-w-[210mm] px-6 pb-16" data-testid="resume-print">
        <ResumeSheet data={version.finalData} />
      </article>

      <style>{`
        @media print {
          @page { size: A4; margin: 14mm; }
          [data-print-hide] { display: none !important; }
          .resume-print { max-width: none; padding: 0; }
          .resume-entry { break-inside: avoid; }
        }
      `}</style>
    </div>
  )
}

/** 定稿内容的只读渲染。所有文字都是黑/深灰，保证黑白打印可读。 */
function ResumeSheet({ data }: { data: ResumeData }) {
  return (
    <div className="text-foreground space-y-5">
      <header className="border-border border-b pb-3">
        <h1 className="text-2xl font-semibold">{data.basics.name}</h1>
        <p className="mt-1 text-sm">
          {[data.basics.phone, data.basics.email, data.basics.city, ...data.basics.links]
            .filter((value) => value !== '')
            .join(' · ')}
        </p>
        {data.intent.role !== '' && <p className="mt-1 text-sm">求职意向：{data.intent.role}</p>}
      </header>

      {data.work.length > 0 && (
        <section>
          <h2 className="mb-2 text-sm font-semibold tracking-wide">工作与实习经历</h2>
          {data.work.map((item, index) => (
            <div key={index} className="resume-entry mb-3">
              <div className="flex items-baseline justify-between gap-2">
                <p className="font-medium">
                  {item.company}
                  {item.title !== '' && <span> · {item.title}</span>}
                </p>
                <p className="shrink-0 text-xs">
                  {item.start} - {item.end}
                </p>
              </div>
              {item.summary !== '' && <p className="text-sm">{item.summary}</p>}
              <ul className="ml-4 list-disc text-sm">
                {item.bullets.map((bullet, bulletIndex) => (
                  <li key={bulletIndex}>{bullet}</li>
                ))}
              </ul>
            </div>
          ))}
        </section>
      )}

      {data.projects.length > 0 && (
        <section>
          <h2 className="mb-2 text-sm font-semibold tracking-wide">项目经历</h2>
          {data.projects.map((item, index) => (
            <div key={index} className="resume-entry mb-3">
              <div className="flex items-baseline justify-between gap-2">
                <p className="font-medium">
                  {item.name}
                  {item.role !== '' && <span> · {item.role}</span>}
                </p>
                <p className="shrink-0 text-xs">{item.period}</p>
              </div>
              <ul className="ml-4 list-disc text-sm">
                {item.bullets.map((bullet, bulletIndex) => (
                  <li key={bulletIndex}>{bullet}</li>
                ))}
              </ul>
            </div>
          ))}
        </section>
      )}

      {data.education.length > 0 && (
        <section>
          <h2 className="mb-2 text-sm font-semibold tracking-wide">教育经历</h2>
          {data.education.map((item, index) => (
            <div key={index} className="resume-entry flex items-baseline justify-between gap-2 text-sm">
              <p>
                {item.school}
                {item.major !== '' && <span> · {item.major}</span>}
                {item.degree !== '' && <span> · {item.degree}</span>}
              </p>
              <p className="shrink-0 text-xs">
                {item.start} - {item.end}
              </p>
            </div>
          ))}
        </section>
      )}

      {data.skills.length > 0 && (
        <section>
          <h2 className="mb-2 text-sm font-semibold tracking-wide">技能</h2>
          <ul className="space-y-1 text-sm">
            {data.skills.map((group, index) => (
              <li key={index}>
                <span className="font-medium">{group.group}</span>：{group.items.join('、')}
              </li>
            ))}
          </ul>
        </section>
      )}

      {data.awards.length > 0 && (
        <section>
          <h2 className="mb-2 text-sm font-semibold tracking-wide">证书与奖项</h2>
          <ul className="list-disc text-sm">
            {data.awards.map((item, index) => (
              <li key={index} className="ml-4">
                {item.name}
                {item.issuer !== '' && <span> · {item.issuer}</span>}
                {item.date !== '' && <span> · {item.date}</span>}
              </li>
            ))}
          </ul>
        </section>
      )}

      {data.selfEvaluation !== '' && (
        <section>
          <h2 className="mb-2 text-sm font-semibold tracking-wide">自我评价</h2>
          <p className="whitespace-pre-line text-sm">{data.selfEvaluation}</p>
        </section>
      )}
    </div>
  )
}
