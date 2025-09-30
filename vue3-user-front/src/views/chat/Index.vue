<template>
  <div class="chat-page">
    <!-- 聊天室头部 -->
    <el-card class="chat-header">
      <div class="header-content">
        <div class="room-info">
          <el-icon :size="24" color="#409eff"><ChatDotRound /></el-icon>
          <span class="room-name">Code-Nest官方群组</span>
        </div>
        <div class="online-info">
          <el-badge :value="onlineCount" :max="999" type="success">
            <el-button size="small" @click="showOnlineUsersDialog">
              <el-icon><User /></el-icon>
              在线用户
            </el-button>
          </el-badge>
        </div>
      </div>
    </el-card>

    <!-- 消息区域 -->
    <el-card class="chat-messages" v-loading="loading">
      <div class="messages-container" ref="messagesContainer" @scroll="handleScroll">
        <!-- 加载更多提示 -->
        <div v-if="hasMore" class="load-more" @click="loadMore">
          <el-button :loading="loadingMore" size="small" text>
            {{ loadingMore ? '加载中...' : '点击加载更多' }}
          </el-button>
        </div>

        <!-- 消息列表 -->
        <div v-for="message in messages" :key="message.id" class="message-wrapper">
          <!-- 系统消息 -->
          <div v-if="message.messageType === 3" class="system-message">
            <el-tag type="warning">系统公告</el-tag>
            <span class="system-content">{{ message.content }}</span>
          </div>

          <!-- 普通消息 -->
          <div v-else :class="['message-item', message.userId === currentUserId ? 'my-message' : 'other-message']">
            <!-- 其他人的消息显示头像和昵称 -->
            <div v-if="message.userId !== currentUserId" class="message-left">
              <el-avatar :size="40" :src="message.userAvatar">
                {{ message.userNickname?.charAt(0) }}
              </el-avatar>
              <div class="message-content">
                <div class="user-name">{{ message.userNickname }}</div>
                <div class="message-bubble">
                  <!-- 文本消息 -->
                  <p v-if="message.messageType === 1" class="text-message">{{ message.content }}</p>
                  <!-- 图片消息 -->
                  <el-image 
                    v-else-if="message.messageType === 2" 
                    :src="message.imageUrl" 
                    fit="cover"
                    class="image-message"
                    :preview-src-list="[message.imageUrl]"
                  />
                </div>
                <div class="message-time">{{ message.createTime }}</div>
              </div>
            </div>

            <!-- 自己的消息右对齐 -->
            <div v-else class="message-right">
              <div class="message-content">
                <div class="message-bubble">
                  <!-- 文本消息 -->
                  <p v-if="message.messageType === 1" class="text-message">{{ message.content }}</p>
                  <!-- 图片消息 -->
                  <el-image 
                    v-else-if="message.messageType === 2" 
                    :src="message.imageUrl" 
                    fit="cover"
                    class="image-message"
                    :preview-src-list="[message.imageUrl]"
                  />
                </div>
                <div class="message-actions">
                  <el-button v-if="message.canRecall" size="small" text type="danger" @click="recallMsg(message)">
                    撤回
                  </el-button>
                  <span class="message-time">{{ message.createTime }}</span>
                </div>
              </div>
              <el-avatar :size="40" :src="message.userAvatar">
                {{ message.userNickname?.charAt(0) }}
              </el-avatar>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty v-if="!loading && messages.length === 0" description="暂无消息，快来开启聊天吧！" />
      </div>
    </el-card>

    <!-- 输入区域 -->
    <el-card class="chat-input">
      <div class="input-tools">
        <el-button circle @click="showEmojiPicker = !showEmojiPicker">
          <el-icon><Sunny /></el-icon>
        </el-button>
      </div>
      <el-input
        v-model="inputMessage"
        type="textarea"
        placeholder="输入消息... (Enter发送，Shift+Enter换行)"
        :rows="3"
        maxlength="500"
        show-word-limit
        @keydown.enter="handleEnter"
      />
      <el-button type="primary" :loading="sending" @click="sendMessage">
        发送
      </el-button>
    </el-card>

    <!-- 在线用户对话框 -->
    <el-dialog v-model="onlineUsersVisible" title="在线用户" width="500px">
      <el-scrollbar height="400px">
        <div v-for="user in onlineUsers" :key="user.userId" class="online-user-item">
          <el-avatar :size="40" :src="user.userAvatar">
            {{ user.userNickname?.charAt(0) }}
          </el-avatar>
          <div class="user-detail">
            <div class="user-nickname">{{ user.userNickname }}</div>
            <div class="connect-time">{{ user.connectTime }}</div>
          </div>
        </div>
      </el-scrollbar>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, User, Sunny } from '@element-plus/icons-vue'
import { getChatHistory, getOnlineCount, getOnlineUsers, recallMessage } from '@/api/chat'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id)

// 状态变量
const loading = ref(false)
const loadingMore = ref(false)
const sending = ref(false)
const messages = ref([])
const inputMessage = ref('')
const onlineCount = ref(0)
const onlineUsers = ref([])
const onlineUsersVisible = ref(false)
const showEmojiPicker = ref(false)
const hasMore = ref(true)
const messagesContainer = ref(null)

// WebSocket相关
let ws = null
let reconnectTimer = null
let heartbeatTimer = null
const WS_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:9999/api'

// 初始化
onMounted(async () => {
  await loadHistory()
  await loadOnlineCount()
  connectWebSocket()
})

// 清理
onUnmounted(() => {
  disconnectWebSocket()
})

// 加载历史消息
const loadHistory = async () => {
  try {
    loading.value = true
    const res = await getChatHistory({
      lastMessageId: 0,
      pageSize: 20
    })
    messages.value = res.messages || []
    hasMore.value = res.hasMore
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载历史消息失败')
  } finally {
    loading.value = false
  }
}

// 加载更多历史消息
const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  
  try {
    loadingMore.value = true
    const oldestId = messages.value.length > 0 ? messages.value[0].id : 0
    const res = await getChatHistory({
      lastMessageId: oldestId,
      pageSize: 20
    })
    
    if (res.messages && res.messages.length > 0) {
      messages.value.unshift(...res.messages)
      hasMore.value = res.hasMore
    }
  } catch (error) {
    ElMessage.error('加载更多消息失败')
  } finally {
    loadingMore.value = false
  }
}

// 加载在线人数
const loadOnlineCount = async () => {
  try {
    const count = await getOnlineCount()
    onlineCount.value = count || 0
  } catch (error) {
    console.error('加载在线人数失败', error)
  }
}

// 显示在线用户列表
const showOnlineUsersDialog = async () => {
  try {
    const users = await getOnlineUsers()
    onlineUsers.value = users || []
    onlineUsersVisible.value = true
  } catch (error) {
    ElMessage.error('获取在线用户失败')
  }
}

// 连接WebSocket
const connectWebSocket = () => {
  const token = userStore.token
  if (!token) {
    ElMessage.error('请先登录')
    return
  }

  try {
    ws = new WebSocket(`${WS_URL}/ws/chat?token=${token}`)

    ws.onopen = () => {
      console.log('WebSocket连接成功')
      ElMessage.success('进入聊天室')
      startHeartbeat()
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
      ElMessage.error('连接异常')
    }

    ws.onclose = () => {
      console.log('WebSocket连接关闭')
      stopHeartbeat()
      // 3秒后重连
      reconnectTimer = setTimeout(() => {
        connectWebSocket()
      }, 3000)
    }
  } catch (error) {
    console.error('WebSocket连接失败', error)
  }
}

// 断开WebSocket
const disconnectWebSocket = () => {
  if (ws) {
    ws.close()
    ws = null
  }
  stopHeartbeat()
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

// 处理WebSocket消息
const handleWebSocketMessage = (data) => {
  const { type, data: messageData } = data

  switch (type) {
    case 'CONNECT':
      console.log('连接成功', messageData)
      break
    case 'MESSAGE':
      // 新消息
      messages.value.push(messageData)
      nextTick(() => scrollToBottom())
      break
    case 'SYSTEM':
      // 系统消息
      messages.value.push({
        id: Date.now(),
        messageType: 3,
        content: messageData.content,
        createTime: messageData.createTime
      })
      nextTick(() => scrollToBottom())
      break
    case 'ONLINE_COUNT':
      onlineCount.value = messageData.count
      break
    case 'MESSAGE_RECALL':
      // 消息撤回
      const recallIndex = messages.value.findIndex(m => m.id === messageData.messageId)
      if (recallIndex !== -1) {
        messages.value.splice(recallIndex, 1)
      }
      break
    case 'MESSAGE_DELETE':
      // 消息删除
      const deleteIndex = messages.value.findIndex(m => m.id === messageData.messageId)
      if (deleteIndex !== -1) {
        messages.value.splice(deleteIndex, 1)
      }
      break
    case 'KICK_OUT':
      ElMessage.error(messageData.message)
      disconnectWebSocket()
      break
    case 'ERROR':
      ElMessage.error(messageData.message)
      break
  }
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  if (!ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.error('连接已断开，请刷新页面重试')
    return
  }

  try {
    sending.value = true
    ws.send(JSON.stringify({
      type: 'MESSAGE',
      data: {
        messageType: 1,
        content: inputMessage.value.trim()
      }
    }))
    inputMessage.value = ''
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

// 撤回消息
const recallMsg = async (message) => {
  try {
    await ElMessageBox.confirm('确定要撤回这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await recallMessage({ messageId: message.id })
    const index = messages.value.findIndex(m => m.id === message.id)
    if (index !== -1) {
      messages.value.splice(index, 1)
    }
    ElMessage.success('消息已撤回')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '撤回失败')
    }
  }
}

// 处理Enter键
const handleEnter = (e) => {
  if (e.shiftKey) {
    // Shift+Enter换行，不处理
    return
  }
  e.preventDefault()
  sendMessage()
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 处理滚动
const handleScroll = (e) => {
  // 滚动到顶部时加载更多
  if (e.target.scrollTop === 0 && hasMore.value && !loadingMore.value) {
    const oldHeight = e.target.scrollHeight
    loadMore().then(() => {
      nextTick(() => {
        e.target.scrollTop = e.target.scrollHeight - oldHeight
      })
    })
  }
}

// 心跳
const startHeartbeat = () => {
  heartbeatTimer = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'HEARTBEAT' }))
    }
  }, 30000) // 30秒一次心跳
}

const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}
</script>

<style scoped lang="scss">
.chat-page {
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
}

.chat-header {
  flex-shrink: 0;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .room-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .room-name {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.chat-messages {
  flex: 1;
  overflow: hidden;
  
  :deep(.el-card__body) {
    height: 100%;
    padding: 0;
  }
  
  .messages-container {
    height: 100%;
    overflow-y: auto;
    padding: 16px;
  }
  
  .load-more {
    text-align: center;
    margin-bottom: 16px;
  }
  
  .message-wrapper {
    margin-bottom: 20px;
  }
  
  .system-message {
    text-align: center;
    padding: 12px;
    background: #fef0f0;
    border-radius: 4px;
    margin-bottom: 12px;
    
    .system-content {
      margin-left: 8px;
      color: #606266;
    }
  }
  
  .message-item {
    display: flex;
    
    &.my-message {
      justify-content: flex-end;
    }
    
    .message-left, .message-right {
      display: flex;
      gap: 12px;
      max-width: 70%;
    }
    
    .message-content {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    
    .user-name {
      font-size: 12px;
      color: #909399;
    }
    
    .message-bubble {
      background: white;
      padding: 12px 16px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      
      .text-message {
        margin: 0;
        word-break: break-word;
        white-space: pre-wrap;
        color: #303133;
        line-height: 1.6;
      }
      
      .image-message {
        max-width: 200px;
        border-radius: 4px;
        cursor: pointer;
      }
    }
    
    &.my-message .message-bubble {
      background: #409eff;
      color: white;
      
      .text-message {
        color: white;
      }
    }
    
    .message-time {
      font-size: 12px;
      color: #909399;
    }
    
    .message-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      justify-content: flex-end;
    }
  }
}

.chat-input {
  flex-shrink: 0;
  
  :deep(.el-card__body) {
    display: flex;
    gap: 12px;
    align-items: flex-start;
  }
  
  .input-tools {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  :deep(.el-textarea) {
    flex: 1;
  }
}

.online-user-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .user-detail {
    flex: 1;
    
    .user-nickname {
      font-size: 14px;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .connect-time {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
