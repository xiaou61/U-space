<template>
  <div class="chat-container" :class="{ 'sidebar-collapsed': !showSidebar }">
    <!-- 聊天室头部 -->
    <header class="chat-header">
      <div class="header-left">
        <div class="room-avatar">
          <el-icon :size="28" color="#fff"><ChatDotRound /></el-icon>
        </div>
        <div class="room-info">
          <h2 class="room-name">Code-Nest官方群组</h2>
          <p class="room-desc">大家一起交流技术问题</p>
        </div>
      </div>
      <div class="header-right">
        <!-- 消息搜索 -->
        <div class="search-wrapper">
          <el-tooltip content="搜索消息" placement="bottom">
            <el-button circle size="small" @click="toggleSearch">
              <el-icon :size="18"><Search /></el-icon>
            </el-button>
          </el-tooltip>
          <Transition name="fade">
            <div v-if="showSearch" class="search-box">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索消息..."
                size="small"
                clearable
                @input="handleSearch"
                @keydown.enter="jumpToNextResult"
              />
              <div v-if="searchKeyword && searchResults.length > 0" class="search-result-hint">
                {{ currentSearchIndex + 1 }} / {{ searchResults.length }}
                <el-button size="small" text @click="jumpToPrevResult">↑</el-button>
                <el-button size="small" text @click="jumpToNextResult">↓</el-button>
              </div>
              <div v-else-if="searchKeyword && searchResults.length === 0" class="search-result-hint">
                无结果
              </div>
            </div>
          </Transition>
        </div>
        <div class="connection-status" :class="connectionStatusClass">
          <span class="status-dot"></span>
          <span class="status-text">{{ connectionStatusText }}</span>
        </div>
        <el-badge :value="onlineCount" :max="999" type="success">
          <el-button 
            class="toggle-sidebar-btn" 
            :type="showSidebar ? 'primary' : 'default'"
            @click="toggleSidebar"
          >
            <el-icon><User /></el-icon>
            <span class="btn-text">在线用户</span>
          </el-button>
        </el-badge>
      </div>
    </header>

    <!-- 主体内容区 -->
    <main class="chat-main">
      <!-- 消息区域 -->
      <section 
        class="messages-section"
        @drop="handleDrop"
        @dragover="handleDragOver"
        @paste="handlePaste"
      >
        <div class="messages-container" ref="messagesContainer" @scroll="handleScroll">
          <!-- 加载更多 -->
          <div v-if="hasMore" class="load-more-wrapper">
            <el-button 
              :loading="loadingMore" 
              size="small" 
              round
              @click="loadMore"
            >
              <el-icon v-if="!loadingMore"><ArrowUp /></el-icon>
              {{ loadingMore ? '加载中...' : '加载更多历史消息' }}
            </el-button>
          </div>

          <!-- 消息列表 -->
          <TransitionGroup name="message-fade">
            <div 
              v-for="message in messages" 
              :key="message.id" 
              :data-message-id="message.id"
              class="message-wrapper"
              :class="{ 'my-message': message.userId === currentUserId }"
            >
              <!-- 系统消息 -->
              <div v-if="message.messageType === 3" class="system-message">
                <div class="system-message-content">
                  <el-icon><Bell /></el-icon>
                  <span>{{ message.content }}</span>
                </div>
              </div>

              <!-- 用户加入/离开消息 -->
              <div v-else-if="message.messageType === 4" class="join-leave-message">
                <span>{{ message.content }}</span>
              </div>

              <!-- 普通消息 -->
              <div v-else class="message-item">
                <!-- 他人消息 -->
                <template v-if="message.userId !== currentUserId">
                  <el-avatar 
                    :size="40" 
                    :src="message.userAvatar"
                    class="message-avatar"
                  >
                    {{ message.userNickname?.charAt(0) }}
                  </el-avatar>
                  <div class="message-body">
                    <div class="message-meta">
                      <span class="user-name">{{ message.userNickname }}</span>
                      <span class="message-time">{{ formatTime(message.createTime) }}</span>
                    </div>
                  <div class="message-bubble other-bubble" @dblclick="startReply(message)">
                      <!-- 回复引用 -->
                      <div v-if="message.replyToId" class="reply-quote" @click="scrollToMessage(message.replyToId)">
                        <span class="reply-user">回复 {{ message.replyToUser }}:</span>
                        <span class="reply-content">{{ message.replyToContent }}</span>
                      </div>
                      <p v-if="message.messageType === 1" class="text-content" v-html="renderMentions(message.content)"></p>
                      <el-image 
                        v-else-if="message.messageType === 2" 
                        :src="message.imageUrl" 
                        fit="cover"
                        class="image-content"
                        :preview-src-list="[message.imageUrl]"
                        loading="lazy"
                      />
                    </div>
                    <el-button class="reply-btn" size="small" text @click="startReply(message)">
                      <el-icon><ChatLineSquare /></el-icon>
                    </el-button>
                  </div>
                </template>

                <!-- 自己的消息 -->
                <template v-else>
                  <div class="message-body my-body">
                    <div class="message-meta my-meta">
                      <span class="message-time">{{ formatTime(message.createTime) }}</span>
                      <span class="send-status" :class="message.status">
                        <el-icon v-if="message.status === 'sending'" class="loading-icon"><Loading /></el-icon>
                        <template v-else-if="message.status === 'sent'">✓</template>
                        <template v-else-if="message.status === 'delivered'">✓✓</template>
                        <el-icon v-else-if="message.status === 'failed'" class="failed-icon"><WarningFilled /></el-icon>
                      </span>
                    </div>
                    <div class="message-bubble my-bubble">
                      <!-- 回复引用 -->
                      <div v-if="message.replyToId" class="reply-quote my-reply-quote" @click="scrollToMessage(message.replyToId)">
                        <span class="reply-user">回复 {{ message.replyToUser }}:</span>
                        <span class="reply-content">{{ message.replyToContent }}</span>
                      </div>
                      <p v-if="message.messageType === 1" class="text-content" v-html="renderMentions(message.content)"></p>
                      <el-image 
                        v-else-if="message.messageType === 2" 
                        :src="message.imageUrl" 
                        fit="cover"
                        class="image-content"
                        :preview-src-list="[message.imageUrl]"
                        loading="lazy"
                      />
                    </div>
                    <div v-if="message.canRecall || message.status === 'failed'" class="message-actions">
                      <el-button 
                        v-if="message.canRecall" 
                        size="small" 
                        text 
                        type="info"
                        @click="recallMsg(message)"
                      >
                        撤回
                      </el-button>
                      <el-button 
                        v-if="message.status === 'failed'" 
                        size="small" 
                        text 
                        type="primary"
                        @click="resendMessage(message)"
                      >
                        重试
                      </el-button>
                    </div>
                  </div>
                  <el-avatar 
                    :size="40" 
                    :src="message.userAvatar"
                    class="message-avatar"
                  >
                    {{ message.userNickname?.charAt(0) }}
                  </el-avatar>
                </template>
              </div>
            </div>
          </TransitionGroup>

          <!-- 空状态 -->
          <el-empty 
            v-if="!loading && messages.length === 0" 
            description="暂无消息，快来开启聊天吧！"
            :image-size="120"
          />

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-wrapper">
            <el-icon class="loading-icon" :size="32"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
        </div>

        <!-- 新消息提示 -->
        <Transition name="slide-up">
          <div v-if="newMessageCount > 0" class="new-message-tip" @click="scrollToBottom">
            <el-icon><ArrowDown /></el-icon>
            <span>{{ newMessageCount }} 条新消息</span>
          </div>
        </Transition>

        <!-- 输入状态提示 -->
        <Transition name="fade">
          <div v-if="typingUsers.length > 0" class="typing-indicator">
            <span class="typing-dots"><span></span><span></span><span></span></span>
            <span class="typing-text">{{ typingUsersText }}</span>
          </div>
        </Transition>
      </section>

      <!-- 右侧用户列表 -->
      <Transition name="slide-right">
        <aside v-show="showSidebar" class="users-sidebar">
          <div class="sidebar-header">
            <h3>在线用户 ({{ onlineCount }})</h3>
            <el-input 
              v-model="userSearchKey" 
              placeholder="搜索用户"
              size="small"
              clearable
              :prefix-icon="Search"
            />
          </div>
          <el-scrollbar class="users-list">
            <div 
              v-for="user in filteredOnlineUsers" 
              :key="user.userId" 
              class="user-item"
              @click="mentionUser(user)"
            >
              <div class="user-avatar-wrapper">
                <el-avatar :size="36" :src="user.userAvatar">
                  {{ user.userNickname?.charAt(0) }}
                </el-avatar>
                <span class="online-dot"></span>
              </div>
              <div class="user-info">
                <div class="user-nickname">{{ user.userNickname }}</div>
                <div class="user-join-time">{{ user.connectTime }}</div>
              </div>
            </div>
            <el-empty v-if="filteredOnlineUsers.length === 0" description="暂无在线用户" :image-size="60" />
          </el-scrollbar>
        </aside>
      </Transition>
    </main>

    <!-- 输入区域 -->
    <footer class="chat-footer">
      <!-- 回复提示 -->
      <Transition name="slide-down">
        <div v-if="replyingTo" class="replying-hint">
          <span class="replying-text">
            <el-icon><ChatLineSquare /></el-icon>
            回复 {{ replyingTo.userNickname }}: {{ replyingTo.messageType === 2 ? '[图片]' : truncateText(replyingTo.content, 30) }}
          </span>
          <el-button size="small" text type="info" @click="cancelReply">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </Transition>
      <div class="input-row">
        <div class="input-toolbar">
          <el-tooltip content="表情" placement="top">
            <el-button circle size="small" @click="toggleEmojiPicker">
              <el-icon :size="18"><Sunny /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="图片" placement="top">
            <el-button circle size="small" @click="triggerImageUpload">
              <el-icon :size="18"><Picture /></el-icon>
            </el-button>
          </el-tooltip>
          <input 
            ref="imageInput"
            type="file" 
            accept="image/*" 
            style="display: none"
            @change="handleImageSelect"
          />
        </div>
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            placeholder="输入消息... (Enter发送，@提及用户)"
            :rows="2"
            :maxlength="500"
            show-word-limit
            resize="none"
            @keydown="handleKeydown"
            @input="handleInput"
          />
          <!-- @提及选择器 -->
          <Transition name="fade">
            <div v-if="showMentionPicker && mentionUsers.length > 0" class="mention-picker">
              <div 
                v-for="(user, index) in mentionUsers" 
                :key="user.userId"
                class="mention-item"
                :class="{ selected: index === selectedMentionIndex }"
                @click="selectMentionUser(user)"
                @mouseenter="selectedMentionIndex = index"
              >
                <el-avatar :size="28" :src="user.userAvatar">
                  {{ user.userNickname?.charAt(0) }}
                </el-avatar>
                <span class="mention-name">{{ user.userNickname }}</span>
              </div>
            </div>
          </Transition>
        </div>
        <el-button 
          type="primary" 
          class="send-btn"
          :loading="sending" 
          :disabled="!inputMessage.trim()"
          @click="sendMessage"
        >
          <el-icon v-if="!sending"><Promotion /></el-icon>
          发送
        </el-button>
      </div>
    </footer>

    <!-- 表情选择器弹窗 -->
    <Transition name="fade">
      <div v-if="showEmojiPicker" class="emoji-picker-wrapper" @click.self="showEmojiPicker = false">
        <div class="emoji-picker">
          <div class="emoji-tabs">
            <div 
              v-for="(category, index) in emojiCategories" 
              :key="index"
              class="emoji-tab"
              :class="{ active: activeEmojiTab === index }"
              @click="activeEmojiTab = index"
            >
              {{ category.icon }}
            </div>
          </div>
          <div class="emoji-content">
            <span 
              v-for="emoji in emojiCategories[activeEmojiTab].emojis" 
              :key="emoji"
              class="emoji-item"
              @click="insertEmoji(emoji)"
            >
              {{ emoji }}
            </span>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ChatDotRound, User, Sunny, Picture, Promotion, 
  ArrowUp, ArrowDown, Bell, Loading, WarningFilled, Search,
  ChatLineSquare, Close
} from '@element-plus/icons-vue'
import { getChatHistory, getOnlineCount, getOnlineUsers, recallMessage, uploadChatImage } from '@/api/chat'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id)

// ==================== 状态变量 ====================
const loading = ref(false)
const loadingMore = ref(false)
const sending = ref(false)
const messages = ref([])
const inputMessage = ref('')
const onlineCount = ref(0)
const onlineUsers = ref([])
const showSidebar = ref(true)
const showEmojiPicker = ref(false)
const hasMore = ref(true)
const messagesContainer = ref(null)
const imageInput = ref(null)
const userSearchKey = ref('')
const newMessageCount = ref(0)
const activeEmojiTab = ref(0)
const replyingTo = ref(null) // 正在回复的消息

// @提及功能
const showMentionPicker = ref(false)
const mentionSearchKey = ref('')
const mentionCursorPos = ref(0)
const selectedMentionIndex = ref(0)

// 消息搜索功能
const showSearch = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const currentSearchIndex = ref(0)

// 输入状态提示
const typingUsers = ref([])  // 正在输入的用户列表
let typingTimer = null       // 发送输入状态的防抖定时器
let typingClearTimers = {}   // 清除其他用户输入状态的定时器

// 主题切换
const isDarkMode = ref(false)

// ==================== WebSocket相关 ====================
let ws = null
let reconnectTimer = null
let heartbeatTimer = null
let pongTimer = null
const connectionStatus = ref('disconnected')
const WS_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:9999/api'

// 重连配置 - 指数退避
const reconnectConfig = {
  maxRetries: 10,
  baseDelay: 1000,
  maxDelay: 30000,
  retryCount: 0
}

// 自适应心跳配置
const heartbeatConfig = {
  baseInterval: 30000,     // 基础间隔 30s
  minInterval: 15000,      // 最小间隔 15s
  maxInterval: 60000,      // 最大间隔 60s
  currentInterval: 30000,  // 当前间隔
  missedCount: 0,
  maxMissed: 3,
  lastPongTime: 0,
  avgLatency: 0,           // 平均延迟
  latencyHistory: []       // 延迟历史记录
}

// ==================== 表情数据 ====================
const emojiCategories = [
  {
    icon: '😊',
    emojis: ['😀', '😁', '😂', '🤣', '😃', '😄', '😅', '😆', '😉', '😊', '😋', '😎', '😍', '🥰', '😘', '😗', '😙', '😚', '🙂', '🤗', '🤩', '🤔', '🤨', '😐', '😑', '😶', '🙄', '😏', '😣', '😥', '😮', '🤐', '😯', '😪', '😫', '🥱', '😴', '😌', '😛', '😜', '😝', '🤤', '😒', '😓', '😔', '😕', '🙃', '🤑', '😲']
  },
  {
    icon: '👋',
    emojis: ['👋', '🤚', '🖐️', '✋', '🖖', '👌', '🤌', '🤏', '✌️', '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '🖕', '👇', '☝️', '👍', '👎', '✊', '👊', '🤛', '🤜', '👏', '🙌', '👐', '🤲', '🤝', '🙏', '✍️', '💪', '🦾', '🦿', '🦵', '🦶']
  },
  {
    icon: '❤️',
    emojis: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️', '✝️', '☪️', '🕉️', '☸️', '✡️', '🔯', '🕎', '☯️', '☦️', '🛐', '⛎', '♈', '♉', '♊', '♋', '♌', '♍', '♎', '♏', '♐', '♑', '♒', '♓']
  },
  {
    icon: '🎉',
    emojis: ['🎉', '🎊', '🎈', '🎂', '🎁', '🎄', '🎃', '🎗️', '🎟️', '🎫', '🎖️', '🏆', '🏅', '🥇', '🥈', '🥉', '⚽', '🏀', '🏈', '⚾', '🥎', '🎾', '🏐', '🏉', '🥏', '🎱', '🪀', '🏓', '🏸', '🏒', '🏑', '🥍', '🏏', '🪃', '🏹', '🎣', '🤿', '🥊', '🥋', '🎽', '🛹', '🛼', '🛷', '⛸️', '🥌', '🎿']
  }
]

// ==================== 计算属性 ====================
const connectionStatusClass = computed(() => connectionStatus.value)
const connectionStatusText = computed(() => {
  const map = {
    disconnected: '未连接',
    connecting: '连接中...',
    connected: '已连接',
    reconnecting: `重连中(${reconnectConfig.retryCount}/${reconnectConfig.maxRetries})`
  }
  return map[connectionStatus.value]
})

const filteredOnlineUsers = computed(() => {
  if (!userSearchKey.value) return onlineUsers.value
  return onlineUsers.value.filter(user => 
    user.userNickname?.toLowerCase().includes(userSearchKey.value.toLowerCase())
  )
})

// @提及筛选列表
const mentionUsers = computed(() => {
  if (!mentionSearchKey.value) return onlineUsers.value.slice(0, 8)
  return onlineUsers.value.filter(user =>
    user.userNickname?.toLowerCase().includes(mentionSearchKey.value.toLowerCase())
  ).slice(0, 8)
})

// 输入状态文本
const typingUsersText = computed(() => {
  const users = typingUsers.value
  if (users.length === 0) return ''
  if (users.length === 1) return `${users[0].nickname} 正在输入...`
  if (users.length === 2) return `${users[0].nickname} 和 ${users[1].nickname} 正在输入...`
  return `${users[0].nickname} 等 ${users.length} 人正在输入...`
})

// ==================== 生命周期 ====================
onMounted(async () => {
  await loadHistory()
  await loadOnlineCount()
  connectWebSocket()
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  disconnectWebSocket()
  window.removeEventListener('resize', handleResize)
})

// ==================== 方法 ====================
const handleResize = () => {
  showSidebar.value = window.innerWidth > 768
}

const toggleSidebar = () => {
  showSidebar.value = !showSidebar.value
}

const toggleSearch = () => {
  showSearch.value = !showSearch.value
  if (!showSearch.value) {
    searchKeyword.value = ''
    searchResults.value = []
    currentSearchIndex.value = 0
    // 清除高亮
    document.querySelectorAll('.search-highlight').forEach(el => el.classList.remove('search-highlight'))
  }
}

const handleSearch = () => {
  // 清除之前的高亮
  document.querySelectorAll('.search-highlight').forEach(el => el.classList.remove('search-highlight'))
  
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    currentSearchIndex.value = 0
    return
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  searchResults.value = messages.value.filter(msg => 
    msg.messageType === 1 && msg.content?.toLowerCase().includes(keyword)
  )
  currentSearchIndex.value = 0
  
  if (searchResults.value.length > 0) {
    highlightSearchResult()
  }
}

const highlightSearchResult = () => {
  if (searchResults.value.length === 0) return
  const msg = searchResults.value[currentSearchIndex.value]
  const el = document.querySelector(`[data-message-id="${msg.id}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el.classList.add('search-highlight')
  }
}

const jumpToNextResult = () => {
  if (searchResults.value.length === 0) return
  // 清除当前高亮
  const prevMsg = searchResults.value[currentSearchIndex.value]
  document.querySelector(`[data-message-id="${prevMsg.id}"]`)?.classList.remove('search-highlight')
  
  currentSearchIndex.value = (currentSearchIndex.value + 1) % searchResults.value.length
  highlightSearchResult()
}

const jumpToPrevResult = () => {
  if (searchResults.value.length === 0) return
  // 清除当前高亮
  const prevMsg = searchResults.value[currentSearchIndex.value]
  document.querySelector(`[data-message-id="${prevMsg.id}"]`)?.classList.remove('search-highlight')
  
  currentSearchIndex.value = (currentSearchIndex.value - 1 + searchResults.value.length) % searchResults.value.length
  highlightSearchResult()
}

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const insertEmoji = (emoji) => {
  inputMessage.value += emoji
  showEmojiPicker.value = false
}

// 从侧边栏点击用户提及
const mentionUser = (user) => {
  inputMessage.value += `@${user.userNickname} `
}

// 从@选择器选择用户
const selectMentionUser = (user) => {
  const text = inputMessage.value
  // 查找最后一个@符号的位置
  const lastAtIndex = text.lastIndexOf('@', mentionCursorPos.value - 1)
  if (lastAtIndex !== -1) {
    // 替换@后面的内容为选择的用户名
    inputMessage.value = text.substring(0, lastAtIndex) + `@${user.userNickname} ` + text.substring(mentionCursorPos.value)
  }
  closeMentionPicker()
}

// 关闭@选择器
const closeMentionPicker = () => {
  showMentionPicker.value = false
  mentionSearchKey.value = ''
  selectedMentionIndex.value = 0
}

// 输入处理 - 检测@符号 & 发送输入状态
const handleInput = (e) => {
  const text = inputMessage.value
  const cursorPos = e.target?.selectionStart || text.length
  mentionCursorPos.value = cursorPos
  
  // 查找光标前最后一个@
  const textBeforeCursor = text.substring(0, cursorPos)
  const lastAtIndex = textBeforeCursor.lastIndexOf('@')
  
  if (lastAtIndex !== -1) {
    const textAfterAt = textBeforeCursor.substring(lastAtIndex + 1)
    // 检查@后面是否有空格（空格表示已经完成提及）
    if (!textAfterAt.includes(' ') && !textAfterAt.includes('\n')) {
      mentionSearchKey.value = textAfterAt
      showMentionPicker.value = true
      selectedMentionIndex.value = 0
      sendTypingStatus()
      return
    }
  }
  closeMentionPicker()
  
  // 发送输入状态（防抖）
  if (text.trim()) {
    sendTypingStatus()
  }
}

// 发送输入状态（防抖）
const sendTypingStatus = () => {
  if (typingTimer) clearTimeout(typingTimer)
  typingTimer = setTimeout(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'TYPING' }))
    }
  }, 300)
}

// 处理其他用户的输入状态
const handleTypingStatus = (data) => {
  const { userId, nickname } = data
  // 不显示自己的输入状态
  if (userId === currentUserId.value) return
  
  // 添加到正在输入列表
  const existIndex = typingUsers.value.findIndex(u => u.userId === userId)
  if (existIndex === -1) {
    typingUsers.value.push({ userId, nickname })
  }
  
  // 清除之前的定时器
  if (typingClearTimers[userId]) {
    clearTimeout(typingClearTimers[userId])
  }
  
  // 3秒后清除输入状态
  typingClearTimers[userId] = setTimeout(() => {
    typingUsers.value = typingUsers.value.filter(u => u.userId !== userId)
    delete typingClearTimers[userId]
  }, 3000)
}

// 渲染消息中的@提及，高亮显示
const renderMentions = (content) => {
  if (!content) return ''
  // 先转义HTML特殊字符
  const escaped = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
  // 然后高亮@提及
  return escaped.replace(/@([\u4e00-\u9fa5\w]+)/g, '<span class="mention-highlight">@$1</span>')
}

// 截取文本
const truncateText = (text, maxLen) => {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}

// 开始回复
const startReply = (message) => {
  replyingTo.value = message
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
}

// 滚动到指定消息
const scrollToMessage = (messageId) => {
  const msgElement = document.querySelector(`[data-message-id="${messageId}"]`)
  if (msgElement) {
    msgElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
    msgElement.classList.add('highlight-message')
    setTimeout(() => msgElement.classList.remove('highlight-message'), 2000)
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const loadHistory = async () => {
  try {
    loading.value = true
    const res = await getChatHistory({ lastMessageId: 0, pageSize: 30 })
    messages.value = (res.messages || []).map(msg => ({ ...msg, status: 'sent' }))
    hasMore.value = res.hasMore
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载历史消息失败')
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  try {
    loadingMore.value = true
    const oldestId = messages.value.length > 0 ? messages.value[0].id : 0
    const res = await getChatHistory({ lastMessageId: oldestId, pageSize: 20 })
    if (res.messages && res.messages.length > 0) {
      const newMessages = res.messages.map(msg => ({ ...msg, status: 'sent' }))
      messages.value.unshift(...newMessages)
      hasMore.value = res.hasMore
    } else {
      hasMore.value = false
    }
  } catch (error) {
    ElMessage.error('加载更多消息失败')
  } finally {
    loadingMore.value = false
  }
}

const loadOnlineCount = async () => {
  try {
    const count = await getOnlineCount()
    onlineCount.value = count || 0
  } catch (error) {
    console.error('加载在线人数失败', error)
  }
}

const loadOnlineUsers = async () => {
  try {
    const users = await getOnlineUsers()
    onlineUsers.value = users || []
  } catch (error) {
    console.error('获取在线用户失败', error)
  }
}

// ==================== WebSocket ====================
const connectWebSocket = () => {
  const token = userStore.token
  if (!token) {
    ElMessage.error('请先登录')
    return
  }
  connectionStatus.value = reconnectConfig.retryCount > 0 ? 'reconnecting' : 'connecting'
  try {
    ws = new WebSocket(`${WS_URL}/ws/chat?token=${token}`)
    ws.onopen = () => {
      console.log('WebSocket连接成功')
      connectionStatus.value = 'connected'
      reconnectConfig.retryCount = 0
      ElMessage.success('进入聊天室')
      startHeartbeat()
      loadOnlineUsers()
    }
    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handleWebSocketMessage(data)
      } catch (error) {
        console.error('解析消息失败', error)
      }
    }
    ws.onerror = (error) => {
      console.error('WebSocket错误', error)
    }
    ws.onclose = (event) => {
      console.log('WebSocket连接关闭', event.code)
      connectionStatus.value = 'disconnected'
      stopHeartbeat()
      if (event.code !== 1000) {
        scheduleReconnect()
      }
    }
  } catch (error) {
    console.error('WebSocket连接失败', error)
    scheduleReconnect()
  }
}

const scheduleReconnect = () => {
  if (reconnectConfig.retryCount >= reconnectConfig.maxRetries) {
    ElMessage.error('连接失败，请刷新页面重试')
    connectionStatus.value = 'disconnected'
    return
  }
  const delay = Math.min(
    reconnectConfig.baseDelay * Math.pow(2, reconnectConfig.retryCount),
    reconnectConfig.maxDelay
  )
  const jitter = Math.random() * 1000
  reconnectConfig.retryCount++
  connectionStatus.value = 'reconnecting'
  console.log(`${delay + jitter}ms 后进行第 ${reconnectConfig.retryCount} 次重连`)
  reconnectTimer = setTimeout(() => {
    connectWebSocket()
  }, delay + jitter)
}

const disconnectWebSocket = () => {
  if (ws) {
    ws.close(1000, 'User leaving')
    ws = null
  }
  stopHeartbeat()
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

const handleWebSocketMessage = (data) => {
  const { type, data: messageData } = data
  switch (type) {
    case 'CONNECT':
      console.log('连接成功', messageData)
      break
    case 'MESSAGE':
      handleNewMessage(messageData)
      break
    case 'SYSTEM':
      messages.value.push({
        id: Date.now(),
        messageType: 3,
        content: messageData.content,
        createTime: messageData.createTime || new Date().toISOString(),
        status: 'sent'
      })
      scrollToBottomIfNeeded()
      break
    case 'USER_JOIN':
      messages.value.push({
        id: Date.now(),
        messageType: 4,
        content: `${messageData.username} 加入了聊天室`,
        createTime: new Date().toISOString()
      })
      loadOnlineUsers()
      break
    case 'USER_LEAVE':
      messages.value.push({
        id: Date.now(),
        messageType: 4,
        content: `${messageData.username} 离开了聊天室`,
        createTime: new Date().toISOString()
      })
      loadOnlineUsers()
      break
    case 'ONLINE_COUNT':
      onlineCount.value = messageData.count
      break
    case 'MESSAGE_RECALL':
      handleMessageRecall(messageData.messageId)
      break
    case 'MESSAGE_DELETE':
      handleMessageDelete(messageData.messageId)
      break
    case 'MESSAGE_ACK':
      handleMessageAck(messageData)
      break
    case 'KICK_OUT':
      ElMessage.error(messageData.message)
      disconnectWebSocket()
      break
    case 'PONG':
      handlePongReceived(messageData?.timestamp || Date.now())
      break
    case 'TYPING':
      handleTypingStatus(messageData)
      break
    case 'ERROR':
      ElMessage.error(messageData.message)
      break
  }
}

const handleNewMessage = (messageData) => {
  const newMsg = { ...messageData, status: 'sent' }
  if (messageData.tempId) {
    const index = messages.value.findIndex(m => m.tempId === messageData.tempId)
    if (index !== -1) {
      messages.value[index] = { ...messages.value[index], ...newMsg, status: 'sent' }
      return
    }
  }
  messages.value.push(newMsg)
  scrollToBottomIfNeeded()
}

const handleMessageRecall = (messageId) => {
  const index = messages.value.findIndex(m => m.id === messageId)
  if (index !== -1) messages.value.splice(index, 1)
}

const handleMessageDelete = (messageId) => {
  const index = messages.value.findIndex(m => m.id === messageId)
  if (index !== -1) messages.value.splice(index, 1)
}

const handleMessageAck = (data) => {
  const { tempId, messageId, status } = data
  const index = messages.value.findIndex(m => m.tempId === tempId)
  if (index !== -1) {
    messages.value[index].id = messageId
    messages.value[index].status = status || 'sent'
  }
}

const sendMessage = () => {
  const content = inputMessage.value.trim()
  if (!content) {
    ElMessage.warning('请输入消息内容')
    return
  }
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.error('连接已断开，正在重连...')
    scheduleReconnect()
    return
  }
  const tempId = `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  const tempMessage = {
    id: tempId,
    tempId,
    userId: currentUserId.value,
    userNickname: userStore.userInfo?.userName || userStore.userInfo?.nickName,
    userAvatar: userStore.userInfo?.userAvatar,
    messageType: 1,
    content,
    createTime: new Date().toISOString(),
    status: 'sending',
    canRecall: true,
    // 回复信息
    replyToId: replyingTo.value?.id || null,
    replyToUser: replyingTo.value?.userNickname || null,
    replyToContent: replyingTo.value ? (replyingTo.value.messageType === 2 ? '[图片]' : truncateText(replyingTo.value.content, 50)) : null
  }
  messages.value.push(tempMessage)
  inputMessage.value = ''
  const replyToId = replyingTo.value?.id || null
  replyingTo.value = null // 清空回复状态
  scrollToBottom()
  try {
    ws.send(JSON.stringify({
      type: 'MESSAGE',
      data: { messageType: 1, content, tempId, replyToId }
    }))
    setTimeout(() => {
      const msg = messages.value.find(m => m.tempId === tempId)
      if (msg && msg.status === 'sending') msg.status = 'failed'
    }, 5000)
  } catch (error) {
    const msg = messages.value.find(m => m.tempId === tempId)
    if (msg) msg.status = 'failed'
    ElMessage.error('发送失败')
  }
}

const resendMessage = (message) => {
  const index = messages.value.findIndex(m => m.tempId === message.tempId)
  if (index !== -1) messages.value.splice(index, 1)
  inputMessage.value = message.content
  sendMessage()
}

const recallMsg = async (message) => {
  try {
    await ElMessageBox.confirm('确定要撤回这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await recallMessage({ messageId: message.id })
    const index = messages.value.findIndex(m => m.id === message.id)
    if (index !== -1) messages.value.splice(index, 1)
    ElMessage.success('消息已撤回')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message || '撤回失败')
  }
}

const handleKeydown = (e) => {
  // @选择器激活时的键盘导航
  if (showMentionPicker.value && mentionUsers.value.length > 0) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      selectedMentionIndex.value = (selectedMentionIndex.value + 1) % mentionUsers.value.length
      return
    }
    if (e.key === 'ArrowUp') {
      e.preventDefault()
      selectedMentionIndex.value = (selectedMentionIndex.value - 1 + mentionUsers.value.length) % mentionUsers.value.length
      return
    }
    if (e.key === 'Tab' || (e.key === 'Enter' && !e.shiftKey)) {
      e.preventDefault()
      selectMentionUser(mentionUsers.value[selectedMentionIndex.value])
      return
    }
    if (e.key === 'Escape') {
      e.preventDefault()
      closeMentionPicker()
      return
    }
  }
  
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
  newMessageCount.value = 0
}

const scrollToBottomIfNeeded = () => {
  if (!messagesContainer.value) return
  const { scrollTop, scrollHeight, clientHeight } = messagesContainer.value
  const isNearBottom = scrollHeight - scrollTop - clientHeight < 150
  if (isNearBottom) {
    scrollToBottom()
  } else {
    newMessageCount.value++
  }
}

const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  if (scrollTop === 0 && hasMore.value && !loadingMore.value) {
    const oldHeight = scrollHeight
    loadMore().then(() => {
      nextTick(() => { e.target.scrollTop = e.target.scrollHeight - oldHeight })
    })
  }
  if (scrollHeight - scrollTop - clientHeight < 50) {
    newMessageCount.value = 0
  }
}

// 处理PONG响应，计算延迟并调整心跳间隔
const handlePongReceived = (serverTimestamp) => {
  heartbeatConfig.missedCount = 0
  if (pongTimer) clearTimeout(pongTimer)
  
  const now = Date.now()
  const latency = now - heartbeatConfig.lastPongTime
  
  // 记录延迟历史（保留最近的10条记录）
  if (heartbeatConfig.lastPongTime > 0) {
    heartbeatConfig.latencyHistory.push(latency)
    if (heartbeatConfig.latencyHistory.length > 10) {
      heartbeatConfig.latencyHistory.shift()
    }
    
    // 计算平均延迟
    heartbeatConfig.avgLatency = heartbeatConfig.latencyHistory.reduce((a, b) => a + b, 0) / heartbeatConfig.latencyHistory.length
    
    // 自适应调整心跳间隔
    adjustHeartbeatInterval()
  }
  heartbeatConfig.lastPongTime = now
}

// 自适应调整心跳间隔
const adjustHeartbeatInterval = () => {
  const { avgLatency, baseInterval, minInterval, maxInterval } = heartbeatConfig
  
  // 根据延迟调整策略：
  // - 延迟 < 100ms: 网络良好，增加心跳间隔节省资源
  // - 延迟 100-500ms: 网络一般，保持基础间隔
  // - 延迟 > 500ms: 网络较差，减少心跳间隔以更快检测断开
  
  let newInterval = baseInterval
  if (avgLatency < 100) {
    newInterval = Math.min(baseInterval * 1.5, maxInterval)
  } else if (avgLatency > 500) {
    newInterval = Math.max(baseInterval * 0.6, minInterval)
  } else if (avgLatency > 300) {
    newInterval = Math.max(baseInterval * 0.8, minInterval)
  }
  
  // 如果间隔变化较大，重新设置定时器
  if (Math.abs(newInterval - heartbeatConfig.currentInterval) > 5000) {
    heartbeatConfig.currentInterval = newInterval
    restartHeartbeat()
    console.log(`心跳间隔调整为: ${newInterval}ms (平均延迟: ${Math.round(avgLatency)}ms)`)
  }
}

const startHeartbeat = () => {
  const sendHeartbeat = () => {
    if (!ws || ws.readyState !== WebSocket.OPEN) return
    if (heartbeatConfig.missedCount >= heartbeatConfig.maxMissed) {
      console.log('心跳超时，重新连接')
      stopHeartbeat()
      ws.close()
      scheduleReconnect()
      return
    }
    heartbeatConfig.missedCount++
    heartbeatConfig.lastPongTime = Date.now()
    ws.send(JSON.stringify({ type: 'HEARTBEAT', timestamp: Date.now() }))
    pongTimer = setTimeout(() => { console.log('Pong超时') }, 5000)
  }
  
  // 立即发送第一个心跳
  sendHeartbeat()
  heartbeatTimer = setInterval(sendHeartbeat, heartbeatConfig.currentInterval)
}

const restartHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = setInterval(() => {
      if (!ws || ws.readyState !== WebSocket.OPEN) return
      if (heartbeatConfig.missedCount >= heartbeatConfig.maxMissed) {
        console.log('心跳超时，重新连接')
        stopHeartbeat()
        ws.close()
        scheduleReconnect()
        return
      }
      heartbeatConfig.missedCount++
      heartbeatConfig.lastPongTime = Date.now()
      ws.send(JSON.stringify({ type: 'HEARTBEAT', timestamp: Date.now() }))
      pongTimer = setTimeout(() => { console.log('Pong超时') }, 5000)
    }, heartbeatConfig.currentInterval)
  }
}

const stopHeartbeat = () => {
  if (heartbeatTimer) { clearInterval(heartbeatTimer); heartbeatTimer = null }
  if (pongTimer) { clearTimeout(pongTimer); pongTimer = null }
  heartbeatConfig.missedCount = 0
  heartbeatConfig.latencyHistory = []
  heartbeatConfig.currentInterval = heartbeatConfig.baseInterval
}

const triggerImageUpload = () => { imageInput.value?.click() }

const uploadProgress = ref(0)
const isUploading = ref(false)

const handleImageSelect = async (e) => {
  const file = e.target.files?.[0]
  if (file) await processAndUploadImage(file)
  e.target.value = ''
}

// 处理图片上传
const processAndUploadImage = async (file) => {
  if (!file.type.startsWith('image/')) { 
    ElMessage.error('请选择图片文件')
    return 
  }
  if (file.size > 5 * 1024 * 1024) { 
    ElMessage.error('图片大小不能超过5MB')
    return 
  }
  
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.error('连接已断开，无法发送图片')
    return
  }

  try {
    isUploading.value = true
    uploadProgress.value = 0
    
    // 上传图片
    const result = await uploadChatImage(file, (percent) => {
      uploadProgress.value = percent
    })
    
    if (result && result.fileUrl) {
      // 发送图片消息
      const tempId = `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
      const tempMessage = {
        id: tempId,
        tempId,
        userId: currentUserId.value,
        userNickname: userStore.userInfo?.userName || userStore.userInfo?.nickName,
        userAvatar: userStore.userInfo?.userAvatar,
        messageType: 2,
        imageUrl: result.fileUrl,
        createTime: new Date().toISOString(),
        status: 'sending',
        canRecall: true
      }
      messages.value.push(tempMessage)
      scrollToBottom()
      
      ws.send(JSON.stringify({
        type: 'MESSAGE',
        data: { messageType: 2, imageUrl: result.fileUrl, tempId }
      }))
      
      setTimeout(() => {
        const msg = messages.value.find(m => m.tempId === tempId)
        if (msg && msg.status === 'sending') msg.status = 'failed'
      }, 5000)
      
      ElMessage.success('图片发送成功')
    } else {
      ElMessage.error('图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败', error)
    ElMessage.error('图片上传失败')
  } finally {
    isUploading.value = false
    uploadProgress.value = 0
  }
}

// 粘贴上传图片
const handlePaste = (e) => {
  const items = e.clipboardData?.items
  if (!items) return
  
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      e.preventDefault()
      const file = item.getAsFile()
      if (file) processAndUploadImage(file)
      break
    }
  }
}

// 拖拽上传图片
const handleDrop = (e) => {
  e.preventDefault()
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file.type.startsWith('image/')) {
      processAndUploadImage(file)
    }
  }
}

const handleDragOver = (e) => {
  e.preventDefault()
}
</script>

<style scoped lang="scss">
$header-height: 64px;
$footer-height: 80px;
$sidebar-width: 280px;
$primary-color: #409eff;
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;
$text-primary: #303133;
$text-secondary: #909399;
$bg-color: #f5f7fa;
$border-color: #ebeef5;

.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $bg-color;
  overflow: hidden;
}

.chat-header {
  height: $header-height;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, $primary-color 0%, #53a8ff 100%);
  color: white;
  flex-shrink: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .room-avatar {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .room-info {
      .room-name {
        font-size: 18px;
        font-weight: 600;
        margin: 0 0 2px 0;
      }
      .room-desc {
        font-size: 12px;
        margin: 0;
        opacity: 0.9;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .search-wrapper {
      position: relative;
      display: flex;
      align-items: center;
      gap: 8px;

      .search-box {
        display: flex;
        align-items: center;
        gap: 8px;
        background: rgba(255, 255, 255, 0.9);
        border-radius: 20px;
        padding: 4px 12px;

        :deep(.el-input__wrapper) {
          box-shadow: none;
          background: transparent;
        }

        .search-result-hint {
          font-size: 12px;
          color: $text-secondary;
          white-space: nowrap;
          display: flex;
          align-items: center;
          gap: 2px;
        }
      }
    }

    .connection-status {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      padding: 4px 10px;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.2);

      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #909399;
      }

      &.connected .status-dot {
        background: $success-color;
        box-shadow: 0 0 6px $success-color;
      }
      &.connecting .status-dot,
      &.reconnecting .status-dot {
        background: $warning-color;
        animation: pulse 1s infinite;
      }
      &.disconnected .status-dot {
        background: $danger-color;
      }
    }

    .toggle-sidebar-btn .btn-text {
      margin-left: 4px;
    }
  }
}

.chat-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

.messages-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  min-width: 0;

  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    scroll-behavior: smooth;

    &::-webkit-scrollbar {
      width: 6px;
    }
    &::-webkit-scrollbar-thumb {
      background: #c0c4cc;
      border-radius: 3px;
    }
  }

  .load-more-wrapper {
    text-align: center;
    padding: 12px 0;
  }

  .loading-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px;
    color: $text-secondary;
    gap: 12px;
  }

  .new-message-tip {
    position: absolute;
    bottom: 16px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: $primary-color;
    color: white;
    border-radius: 20px;
    cursor: pointer;
    font-size: 13px;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
    transition: transform 0.2s;

    &:hover {
      transform: translateX(-50%) scale(1.05);
    }
  }

  // 输入状态提示
  .typing-indicator {
    position: absolute;
    bottom: 8px;
    left: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    font-size: 12px;
    color: $text-secondary;

    .typing-dots {
      display: flex;
      gap: 3px;

      span {
        width: 6px;
        height: 6px;
        background: $primary-color;
        border-radius: 50%;
        animation: typingBounce 1.4s infinite ease-in-out;

        &:nth-child(1) { animation-delay: 0s; }
        &:nth-child(2) { animation-delay: 0.2s; }
        &:nth-child(3) { animation-delay: 0.4s; }
      }
    }
  }
}

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-4px); }
}

.message-wrapper {
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
  transition: background-color 0.3s ease;

  &.my-message .message-item {
    flex-direction: row-reverse;
  }

  // 消息高亮效果
  &.highlight-message {
    background-color: rgba(64, 158, 255, 0.15);
    border-radius: 8px;
    padding: 8px;
    margin: -8px;
    margin-bottom: 8px;
  }

  // 搜索结果高亮
  &.search-highlight {
    background-color: rgba(255, 200, 0, 0.25);
    border-radius: 8px;
    padding: 8px;
    margin: -8px;
    margin-bottom: 8px;
    animation: searchPulse 0.5s ease;
  }
}

@keyframes searchPulse {
  0%, 100% { background-color: rgba(255, 200, 0, 0.25); }
  50% { background-color: rgba(255, 200, 0, 0.5); }
}

.system-message {
  text-align: center;
  padding: 8px 0;

  .system-message-content {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: linear-gradient(135deg, #fef0f0, #fdf2f2);
    border-radius: 16px;
    color: $warning-color;
    font-size: 13px;
  }
}

.join-leave-message {
  text-align: center;
  padding: 6px 0;
  font-size: 12px;
  color: $text-secondary;
}

.message-item {
  display: flex;
  gap: 10px;
  max-width: 85%;

  .message-avatar {
    flex-shrink: 0;
  }

  .message-body {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;

    &.my-body {
      align-items: flex-end;
    }
  }

  .message-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;

    .user-name {
      color: $text-primary;
      font-weight: 500;
    }
    .message-time {
      color: $text-secondary;
    }

    &.my-meta {
      flex-direction: row-reverse;
    }
  }

  .send-status {
    font-size: 12px;
    color: $text-secondary;

    &.sending { color: $warning-color; }
    &.sent, &.delivered { color: $success-color; }
    &.failed { color: $danger-color; }

    .loading-icon {
      animation: spin 1s linear infinite;
    }
  }

  .message-bubble {
    padding: 10px 14px;
    border-radius: 4px 16px 16px 16px;
    max-width: 100%;
    word-break: break-word;

    &.other-bubble {
      background: white;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    &.my-bubble {
      background: linear-gradient(135deg, $primary-color 0%, #53a8ff 100%);
      color: white;
      border-radius: 16px 4px 16px 16px;
    }

    .text-content {
      margin: 0;
      line-height: 1.6;
      white-space: pre-wrap;

      // @提及高亮
      :deep(.mention-highlight) {
        color: $primary-color;
        font-weight: 500;
        background: rgba(64, 158, 255, 0.1);
        padding: 1px 4px;
        border-radius: 4px;
        cursor: pointer;

        &:hover {
          background: rgba(64, 158, 255, 0.2);
        }
      }
    }

    // 自己的消息中@高亮
    &.my-bubble .text-content :deep(.mention-highlight) {
      color: white;
      background: rgba(255, 255, 255, 0.2);

      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }

    .image-content {
      max-width: 240px;
      border-radius: 8px;
      cursor: pointer;
    }

    // 回复引用样式
    .reply-quote {
      padding: 6px 10px;
      margin-bottom: 8px;
      background: rgba(0, 0, 0, 0.04);
      border-left: 3px solid $primary-color;
      border-radius: 4px;
      font-size: 12px;
      cursor: pointer;
      transition: background 0.2s;

      &:hover {
        background: rgba(0, 0, 0, 0.08);
      }

      .reply-user {
        color: $primary-color;
        font-weight: 500;
        margin-right: 4px;
      }

      .reply-content {
        color: $text-secondary;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        display: inline-block;
        max-width: 200px;
        vertical-align: bottom;
      }

      &.my-reply-quote {
        background: rgba(255, 255, 255, 0.15);
        border-left-color: rgba(255, 255, 255, 0.6);

        &:hover {
          background: rgba(255, 255, 255, 0.25);
        }

        .reply-user {
          color: rgba(255, 255, 255, 0.9);
        }

        .reply-content {
          color: rgba(255, 255, 255, 0.7);
        }
      }
    }
  }

  // 回复按钮
  .reply-btn {
    opacity: 0;
    transition: opacity 0.2s;
    margin-left: 4px;
  }

  &:hover .reply-btn {
    opacity: 1;
  }

  .message-actions {
    display: flex;
    gap: 4px;
    margin-top: 2px;
  }
}

.users-sidebar {
  width: $sidebar-width;
  background: white;
  border-left: 1px solid $border-color;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;

  .sidebar-header {
    padding: 16px;
    border-bottom: 1px solid $border-color;

    h3 {
      margin: 0 0 12px 0;
      font-size: 15px;
      color: $text-primary;
    }
  }

  .users-list {
    flex: 1;
    padding: 8px;
  }

  .user-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: $bg-color;
    }

    .user-avatar-wrapper {
      position: relative;

      .online-dot {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 10px;
        height: 10px;
        background: $success-color;
        border: 2px solid white;
        border-radius: 50%;
      }
    }

    .user-info {
      flex: 1;
      min-width: 0;

      .user-nickname {
        font-size: 14px;
        color: $text-primary;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .user-join-time {
        font-size: 12px;
        color: $text-secondary;
      }
    }
  }
}

.chat-footer {
  min-height: $footer-height;
  padding: 12px 20px;
  background: white;
  border-top: 1px solid $border-color;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;

  // 回复提示栏
  .replying-hint {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    margin-bottom: 8px;
    background: #f0f7ff;
    border-radius: 8px;
    border-left: 3px solid $primary-color;

    .replying-text {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: $text-secondary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  // 底部工具栏容器
  > div:not(.replying-hint) {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .input-toolbar {
    display: flex;
    gap: 4px;
  }

  .input-wrapper {
    flex: 1;
    position: relative;

    :deep(.el-textarea__inner) {
      resize: none;
      border-radius: 8px;
    }

    // @提及选择器
    .mention-picker {
      position: absolute;
      bottom: 100%;
      left: 0;
      width: 200px;
      max-height: 240px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      overflow-y: auto;
      margin-bottom: 8px;
      z-index: 100;

      .mention-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px 12px;
        cursor: pointer;
        transition: background 0.15s;

        &:hover, &.selected {
          background: $bg-color;
        }

        .mention-name {
          font-size: 14px;
          color: $text-primary;
        }
      }
    }
  }

  .send-btn {
    height: 40px;
    padding: 0 20px;
  }
}

.emoji-picker-wrapper {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 100px;

  .emoji-picker {
    background: white;
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
    width: 320px;
    max-height: 300px;
    display: flex;
    flex-direction: column;

    .emoji-tabs {
      display: flex;
      border-bottom: 1px solid $border-color;
      padding: 8px;
      gap: 4px;

      .emoji-tab {
        padding: 6px 10px;
        border-radius: 6px;
        cursor: pointer;
        font-size: 18px;

        &:hover, &.active {
          background: $bg-color;
        }
      }
    }

    .emoji-content {
      flex: 1;
      overflow-y: auto;
      padding: 12px;
      display: flex;
      flex-wrap: wrap;
      gap: 4px;

      .emoji-item {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        border-radius: 6px;
        font-size: 20px;
        transition: transform 0.15s, background 0.15s;

        &:hover {
          background: $bg-color;
          transform: scale(1.2);
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .chat-header {
    padding: 0 12px;

    .header-left {
      .room-avatar { display: none; }
      .room-info .room-desc { display: none; }
    }

    .header-right {
      .connection-status .status-text { display: none; }
      .btn-text { display: none; }
    }
  }

  .users-sidebar {
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    z-index: 100;
    box-shadow: -4px 0 16px rgba(0, 0, 0, 0.1);
  }

  .message-item {
    max-width: 90%;
  }

  .chat-footer {
    padding: 8px 12px;
    .input-toolbar { display: none; }
  }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-icon {
  animation: spin 1s linear infinite;
}

.message-fade-enter-active { animation: fadeIn 0.3s ease; }

.slide-up-enter-active,
.slide-up-leave-active { transition: all 0.3s ease; }
.slide-up-enter-from,
.slide-up-leave-to { opacity: 0; transform: translateX(-50%) translateY(20px); }

.slide-right-enter-active,
.slide-right-leave-active { transition: all 0.3s ease; }
.slide-right-enter-from,
.slide-right-leave-to { transform: translateX(100%); }

.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

.slide-down-enter-active,
.slide-down-leave-active { transition: all 0.3s ease; }
.slide-down-enter-from,
.slide-down-leave-to { opacity: 0; transform: translateY(-10px); height: 0; padding: 0; margin: 0; }
</style>
