/**
 * 采集 markdown 渲染语料（WORK-012 T-05）。
 *
 * 用**真实上游**跑一批提示词，把**流式增量序列**存下来。
 *
 * **为什么存增量而不是最终文本**：界面收到的本来就是增量（content delta）。回放真实增量，
 * 比"把整段按字符切开"忠实得多——后者会掩盖"上游一次吐一大段"这种真实分布，也就测不出
 * 真实场景下的跳动。
 *
 * 凭据只从环境变量读，**不进仓库、不进日志、不进命令行**：
 *
 *   PAIDEIA_AI_BASE_URL=https://api.deepseek.com \
 *   PAIDEIA_AI_MODEL=deepseek-flash \
 *   PAIDEIA_AI_API_KEY=... \
 *   node scripts/collect-md-corpus.mjs
 *
 * 产物 `frontend/packages/assistant/src/markdown-corpus.json` 是**纯模型输出**，可以提交。
 */

import { readFileSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const BASE_URL = process.env.PAIDEIA_AI_BASE_URL ?? 'https://api.deepseek.com'
const MODEL = process.env.PAIDEIA_AI_MODEL ?? 'deepseek-flash'
const API_KEY = process.env.PAIDEIA_AI_API_KEY

if (!API_KEY) {
  console.error('缺少 PAIDEIA_AI_API_KEY：凭据只从环境变量读，不写进任何文件。')
  process.exit(1)
}

/**
 * 语料要覆盖的形态。**每条都刻意要"多结构混排"**——单一结构太好渲染，
 * 真正会出问题的是它们混在一起、且被流式切开的时候。
 *
 * 最后三条是**恶劣输入**：深嵌套、长回答、HTML 与 markdown 混排。
 */
const PROMPTS = [
  { id: 'plain-prose', prompt: '用三句话解释什么是进程与线程的区别。' },
  { id: 'code-java', prompt: '给我一段 Java 的快速排序实现，带注释，用代码块输出。' },
  { id: 'code-python-long', prompt: '写一段 Python 脚本：读取一个 csv，按某列分组求平均，输出结果。要求代码块完整可运行。' },
  { id: 'table-compare', prompt: '用一个 markdown 表格比较 TCP 与 UDP：连接、可靠性、首部开销、典型用途。' },
  { id: 'nested-list', prompt: '用嵌套的 markdown 列表梳理「准备一场技术面试」要做的事，至少三层。' },
  { id: 'quote-and-inline', prompt: '解释 CAP 定理，中间引用一句原话（用引用块），并用到行内代码与粗体。' },
  { id: 'mixed-everything', prompt: '写一篇短文讲「索引为什么能加速查询」：要有二级标题、一段正文、一段 SQL 代码块、一个对比表格、以及一个要点列表。' },
  { id: 'headings-deep', prompt: '用 h1 到 h4 的标题层级，列一份后端工程师的学习路线。' },
  // ⚠️ 这两条**原本写的是"不要简化 / 篇幅不限"，结果采集时全是空响应**：SSE 里只有
  // `reasoning_content`，`content` 始终为 null —— flash 把 8192 个 token 全花在思考上，
  // 正文一个字都没吐。（这是已记录过的坑的一个新复现：这次不是上限给少了，是提示词在逼它长篇。）
  // 语料要的是**语法形态**，不是逼模型长篇——所以把要求收窄，让它稳定产出结构。
  { id: 'adversarial-deep-nest', prompt: '用一个五层嵌套的 markdown 列表写项目的 WBS，每层一行、用最少的字。' },
  { id: 'adversarial-long', prompt: '分五小节讲 MySQL 的 InnoDB 索引：B+ 树结构、聚簇索引、二级索引与回表、最左前缀、覆盖索引。每节 2-3 句话。' },
  { id: 'adversarial-html', prompt: '讲讲 HTML 与 Markdown 的关系，并给我一小段 HTML 示例（用代码块包住）。' },
]

/** 逐行读 SSE，把 content 增量收下来。**不打印任何凭据。** */
async function streamOnce(prompt) {
  const response = await fetch(`${BASE_URL}/chat/completions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${API_KEY}`,
    },
    body: JSON.stringify({
      model: MODEL,
      stream: true,
      // **8192 而不是 4096**：DeepSeek 的思考 token 计入 max_tokens，长回答（"写得尽量详细、篇幅不限"
      // 那类）会把上限吃满，然后**静默返回空正文**——第一次采集时两条长提示词就是这样拿到 0 增量的。
      // 这是已经记录过的坑，抬高上限是唯一的对策。
      max_tokens: 8192,
      messages: [{ role: 'user', content: prompt }],
    }),
  })

  if (!response.ok) {
    throw new Error(`上游拒绝：HTTP ${response.status}`)
  }

  const deltas = []
  let buffer = ''
  // 原始报文留一份：空响应时它是唯一能说明"上游到底说了什么"的东西
  let raw = ''
  for await (const chunk of response.body) {
    const piece = Buffer.from(chunk).toString('utf8')
    raw += piece
    buffer += piece
    const lines = buffer.split('\n')
    buffer = lines.pop() ?? ''
    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed.startsWith('data:')) {
        continue
      }
      const payload = trimmed.slice(5).trim()
      if (payload === '[DONE]') {
        continue
      }
      try {
        const parsed = JSON.parse(payload)
        const delta = parsed?.choices?.[0]?.delta?.content
        if (typeof delta === 'string' && delta !== '') {
          deltas.push(delta)
        }
      } catch {
        // 半截 JSON：留在 buffer 里等下一块
        buffer = line + '\n' + buffer
      }
    }
  }
  return { deltas, raw }
}

/** 只跑指定的 id（逗号分隔），用于重采失败的那几条。 */
const only = process.env.ONLY?.split(',').map((value) => value.trim()).filter(Boolean)
const selected = only && only.length > 0 ? PROMPTS.filter((item) => only.includes(item.id)) : PROMPTS

const entries = []
for (const item of selected) {
  process.stdout.write(`采集 ${item.id} … `)
  const { deltas, raw } = await streamOnce(item.prompt)
  const text = deltas.join('')
  if (text === '') {
    // **空响应绝不入库**：把它当成一条"空语料"会让测试对着空字符串断言，等于假装测过了。
    // 同时把原始报文打出来——不然只能猜（第一次就是靠这个才发现不是 max_tokens 的问题）。
    console.log('空响应！原始报文前 400 字：')
    console.log(raw.slice(0, 400))
    continue
  }
  entries.push({ id: item.id, prompt: item.prompt, deltas, text })
  console.log(`${deltas.length} 个增量 / ${text.length} 字`)
}

const here = path.dirname(fileURLToPath(import.meta.url))
// 必须是 markdown.test.tsx 真正 import 的那一份（./corpus.json）：
// 写到别处等于重采后测试永远拿到旧语料，而且"合并而非覆盖"读的也是那个错路径上的旧文件。
const target = path.join(here, '..', 'frontend', 'packages', 'ui', 'src', 'markdown', 'corpus.json')

/**
 * **只重采了部分时要合并，不能覆盖**：`ONLY=…` 重跑两条却把整个语料文件写成两条，
 * 会让"重采失败项"这个动作悄悄删掉其它语料——那是最难发现的一类破坏。
 */
let previous = []
try {
  const existing = JSON.parse(readFileSync(target, 'utf8'))
  previous = Array.isArray(existing.entries) ? existing.entries : []
} catch {
  // 首次采集：没有旧文件
}
const merged = new Map(previous.map((item) => [item.id, item]))
for (const item of entries) {
  merged.set(item.id, item)
}
const order = PROMPTS.map((item) => item.id).filter((id) => merged.has(id))
const finalEntries = order.map((id) => merged.get(id))

writeFileSync(
  target,
  `${JSON.stringify({ model: MODEL, collectedAt: new Date().toISOString(), entries: finalEntries }, null, 2)}\n`,
  'utf8',
)
console.log(`已写入 ${target}（本次采到 ${entries.length} 条，文件共 ${finalEntries.length} 条）`)
