// Simple SSE manager
let es = null
let failCount = 0
const MAX_FAILS = 10
import { ElMessageBox, ElMessage } from 'element-plus'

export function connectSSE() {
  if (es) return es

  const tokenName = localStorage.getItem('tokenName')
  const tokenValue = localStorage.getItem('tokenValue')
  // Only connect when token exists
  if (!tokenName || !tokenValue) return null

  const url = '/uapi/resource/sse'

  es = new EventSource(url, { withCredentials: true })
  console.log('SSE connecting:', url)

  // 监听公告事件
  es.addEventListener('announcement', (event) => {
    const content = event.data
    if (content) {
      // 弹出公告弹窗，标题“公告”
      ElMessageBox.alert(content, '公告', {
        confirmButtonText: '我知道了',
        dangerouslyUseHTMLString: false,
      })
    }
  })

  // 监听点赞事件
  es.addEventListener('like', (event) => {
    const content = event.data
    if (content) {
      ElMessage({ message: content, grouping: true, duration: 3000 })
      window.dispatchEvent(new CustomEvent('notify-unread', { detail: { type: 'like' } }))
    }
  })

  es.onmessage = (event) => {
    console.log('SSE message:', event.data)
    // 可在此分发全局事件或存入 store
  }
  es.onerror = (event) => {
    console.error('SSE error', event)
    // 关闭并重置
    closeSSE()
    failCount += 1
    if (failCount < MAX_FAILS) {
      setTimeout(connectSSE, 5000)
    } else {
      console.warn('SSE: reached max retry limit, stop reconnecting')
    }
  }
  es.onopen = () => {
    console.log('SSE open')
    failCount = 0
  }
  return es
}

export function closeSSE() {
  if (es) {
    es.close()
    es = null
    console.log('SSE closed')
  }
} 