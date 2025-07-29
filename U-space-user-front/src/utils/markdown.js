import { marked } from 'marked'
import hljs from 'highlight.js/lib/common'
import 'highlight.js/styles/github.css'

// 自定义 renderer 以生成带 hljs class 的代码块并高亮
const renderer = new marked.Renderer()
renderer.code = (code, infoString) => {
  const lang = (infoString || '').match(/^[^\s]+/)?.[0] || ''
  let highlighted
  if (lang && hljs.getLanguage(lang)) {
    highlighted = hljs.highlight(code, { language: lang }).value
  } else {
    highlighted = hljs.highlightAuto(code).value
  }
  return `<pre><code class="hljs language-${lang}">${highlighted}</code></pre>`
}

marked.setOptions({
  gfm: true,
  breaks: true,
  renderer,
})

export function renderMarkdown(text = '') {
  return marked.parse(text)
} 