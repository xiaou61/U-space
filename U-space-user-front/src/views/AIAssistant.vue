<template>
  <div class="ai-chat-page">
    <header class="chat-header">
      <h3>AI 助手</h3>
      <el-icon class="settings-ic" @click="settingsVisible = true"><Setting /></el-icon>
    </header>
    <div class="messages" ref="msgRef">
      <ChatBubble
        v-for="(msg, idx) in messages"
        :key="idx"
        :role="msg.role"
        :content="msg.content"
      />
    </div>

    <!-- 设置抽屉 -->
    <el-drawer v-model="settingsVisible" title="AI 设置" direction="rtl" size="260">
      <el-form label-width="120px">
        <el-form-item label="启用 RAG">
          <el-switch v-model="enableRag" />
        </el-form-item>
        <el-form-item label="启用工具">
          <el-switch v-model="enableTools" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsVisible = false">关闭</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </template>
    </el-drawer>

    <div class="input-bar">
      <el-input
        v-model="input"
        type="textarea"
        rows="2"
        resize="none"
        placeholder="请输入，Ctrl+Enter 发送"
        @keyup.ctrl.enter="send"
      />
      <el-button class="send-btn" type="primary" :loading="loading" @click="send"
        >发送</el-button
      >
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import ChatBubble from '../components/ChatBubble.vue'
import { renderMarkdown } from '../utils/markdown'
import { listChatHistory } from '../api/ai'
import { Setting } from '@element-plus/icons-vue'

// 消息结构：{ role: 'user' | 'assistant', content: string }
const messages = ref([])
const input = ref('')
const loading = ref(false)
const msgRef = ref(null)

// 可配置项
const enableRag = ref(localStorage.getItem('ai_enable_rag') === 'true')
const enableTools = ref(localStorage.getItem('ai_enable_tools') === 'true')
const settingsVisible = ref(false)

const baseURL = import.meta.env.VITE_API_BASE || '/uapi'

function scrollToBottom() {
  nextTick(() => {
    const el = msgRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

// 尝试修复可能出现的 Latin1 编码乱码 → UTF-8
function fixEncoding(text) {
  try {
    // escape 将 utf-8 字节 (已被错误按 latin1 解码) 转为 %xx，然后 decodeURIComponent 还原
    // 若 text 本身正常，不会发生变化
    return decodeURIComponent(escape(text))
  } catch (e) {
    return text
  }
}

function send() {
  if (!input.value.trim() || loading.value) return

  const text = input.value.trim()
  input.value = ''
  // push user message
  messages.value.push({ role: 'user', content: text })

  loading.value = true

  // prepare assistant container for streaming (需 reactive 保证内容变更可响应)
  const assistantMsg = reactive({ role: 'assistant', content: '' })
  messages.value.push(assistantMsg)

  scrollToBottom()

  const url = `${baseURL}/ai/campus_app/chat/stream?message=${encodeURIComponent(
    text
  )}&tools=${enableTools.value}&rag=${enableRag.value}`

  const es = new EventSource(url, { withCredentials: true })
  let closedByClient = false

  // 后端最后会发送 event: end\ndata: [DONE]\n\n
  const handleEnd = () => {
    if (closedByClient) return
    closedByClient = true
    loading.value = false
    es.close()
  }

  es.onmessage = (event) => {
    const data = event.data
    if (data === '[DONE]') {
      handleEnd()
      return
    }
    // 尝试修正编码后追加
    const chunk = fixEncoding(data)
    assistantMsg.content += chunk
    scrollToBottom()
  }

  // 监听自定义 end 事件
  es.addEventListener('end', () => {
    handleEnd()
  })

  es.onerror = (e) => {
    if (closedByClient) return // 忽略主动关闭后的错误事件
    console.error('AI SSE error', e)
    ElMessage.error('AI 助手连接失败，请稍后重试')
    loading.value = false
    es.close()
  }
}

function saveSettings() {
  localStorage.setItem('ai_enable_rag', enableRag.value)
  localStorage.setItem('ai_enable_tools', enableTools.value)
  settingsVisible.value = false
  ElMessage.success('已保存')
}

// 加载历史记录
async function fetchHistory() {
  try {
    const res = await listChatHistory()
    const list = res.data || []
    // 按创建时间升序排序，避免后端顺序变化
    list.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))

    const historyMsgs = []
    list.forEach((m) => {
      if (!m) return
      if (m.userContent) historyMsgs.push({ role: 'user', content: m.userContent })
      if (m.aiContent) historyMsgs.push({ role: 'assistant', content: m.aiContent })
    })

    messages.value = historyMsgs

    // 等 DOM 更新后滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  } catch (e) {
    console.error('加载历史对话失败', e)
  }
}

onMounted(fetchHistory)
</script>

<style scoped>
.chat-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  z-index: 1000;
}
.chat-header h3 {
  margin: 0;
  font-size: 1rem;
}
.settings-ic {
  cursor: pointer;
  font-size: 1.2rem;
}
.ai-chat-page {
  position: relative;
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding-bottom: 136px; /* 输入栏 + 底部导航 */
  padding-top: 48px; /* 留出头部空间 */
  background: #f5f6fa;
}
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: transparent;
}
.input-bar {
  position: fixed;
  bottom: 56px; /* 56px 为底部导航高度 */
  left: 0;
  right: 0;
  display: flex;
  gap: 8px;
  padding: 8px 16px;
  background: #ffffff;
  border-top: 1px solid #e0e0e0;
}
.send-btn {
  flex-shrink: 0;
}
</style> 