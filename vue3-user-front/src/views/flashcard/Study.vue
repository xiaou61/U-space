<template>
  <div class="study-container">
    <!-- 顶部导航 -->
    <div class="study-header">
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </button>
      <div class="deck-info">
        <h2>{{ deckInfo.name || '学习中' }}</h2>
        <span class="progress-text">{{ currentIndex + 1 }} / {{ totalCards }}</span>
      </div>
      <div class="header-actions">
        <el-progress 
          :percentage="progressPercent" 
          :stroke-width="8"
          :show-text="false"
          color="#667eea"
          style="width: 120px;"
        />
      </div>
    </div>

    <!-- 学习完成状态 -->
    <div v-if="studyComplete" class="complete-state">
      <div class="complete-icon">🎉</div>
      <h2>太棒了！</h2>
      <p>本轮学习已完成</p>
      <div class="complete-stats">
        <div class="stat">
          <span class="value">{{ sessionStats.totalReviewed }}</span>
          <span class="label">已复习</span>
        </div>
        <div class="stat">
          <span class="value">{{ sessionStats.correctCount }}</span>
          <span class="label">记住了</span>
        </div>
        <div class="stat">
          <span class="value">{{ Math.round(sessionStats.correctRate * 100) }}%</span>
          <span class="label">正确率</span>
        </div>
      </div>
      <div class="complete-actions">
        <button class="primary-btn" @click="continueStudy">继续学习</button>
        <button class="secondary-btn" @click="goBack">返回卡组</button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-state">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>正在加载卡片...</p>
    </div>

    <!-- 无卡片状态 -->
    <div v-else-if="!currentCard" class="empty-state">
      <div class="empty-icon">📭</div>
      <p>暂无待学习的卡片</p>
      <button class="secondary-btn" @click="goBack">返回卡组</button>
    </div>

    <!-- 闪卡学习区域 -->
    <div v-else class="flashcard-area">
      <div 
        class="flashcard" 
        :class="{ 'flipped': isFlipped }"
        @click="flipCard"
      >
        <div class="card-face card-front">
          <div class="card-type-badge" :class="getCardTypeBadgeClass(currentCard.cardType)">
            {{ getCardTypeText(currentCard.cardType) }}
          </div>
          <div class="card-content">
            <div class="question-text">{{ currentCard.front }}</div>
          </div>
          <div class="flip-hint">
            <el-icon><Refresh /></el-icon>
            点击查看答案
          </div>
        </div>
        <div class="card-face card-back">
          <div class="card-type-badge" :class="getCardTypeBadgeClass(currentCard.cardType)">
            答案
          </div>
          <div class="card-content markdown-body">
            <div class="answer-text" v-html="renderedBack"></div>
          </div>
          <div class="flip-hint">
            <el-icon><Refresh /></el-icon>
            点击返回问题
          </div>
        </div>
      </div>

      <!-- 反馈按钮 - 只在翻转后显示 -->
      <div class="feedback-area" v-show="isFlipped">
        <p class="feedback-hint">你记住了吗？</p>
        <div class="feedback-buttons">
          <button class="feedback-btn forgot" @click="submitFeedback(0)">
            <span class="btn-icon">😟</span>
            <span class="btn-text">忘记了</span>
            <span class="btn-interval">重新学习</span>
          </button>
          <button class="feedback-btn hard" @click="submitFeedback(1)">
            <span class="btn-icon">😐</span>
            <span class="btn-text">有点难</span>
            <span class="btn-interval">{{ getIntervalText(1) }}</span>
          </button>
          <button class="feedback-btn good" @click="submitFeedback(2)">
            <span class="btn-icon">😊</span>
            <span class="btn-text">记住了</span>
            <span class="btn-interval">{{ getIntervalText(2) }}</span>
          </button>
          <button class="feedback-btn easy" @click="submitFeedback(3)">
            <span class="btn-icon">🤩</span>
            <span class="btn-text">太简单</span>
            <span class="btn-interval">{{ getIntervalText(3) }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 快捷键提示 -->
    <div class="keyboard-hints" v-if="currentCard && !studyComplete">
      <span><kbd>Space</kbd> 翻转</span>
      <span v-if="isFlipped"><kbd>1-4</kbd> 选择难度</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading, Refresh } from '@element-plus/icons-vue'
import flashcardApi from '@/api/flashcard'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const router = useRouter()

// 卡组信息
const deckId = computed(() => route.params.id)
const deckInfo = ref({})

// 学习状态
const loading = ref(false)
const currentCard = ref(null)
const isFlipped = ref(false)
const currentIndex = ref(0)
const totalCards = ref(10) // 每轮固定10张
const studyComplete = ref(false)

// 本轮学习统计
const sessionStats = ref({
  totalReviewed: 0,
  correctCount: 0,
  correctRate: 0
})

// 计算进度百分比
const progressPercent = computed(() => {
  if (totalCards.value === 0) return 0
  return Math.round((currentIndex.value / totalCards.value) * 100)
})

// 渲染答案 Markdown
const renderedBack = computed(() => {
  if (!currentCard.value?.back) return ''
  return renderMarkdown(currentCard.value.back)
})

// 页面初始化
onMounted(() => {
  startStudy()
  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})

// 开始学习
const startStudy = async () => {
  loading.value = true
  studyComplete.value = false
  currentIndex.value = 0
  totalCards.value = 10 // 每轮固定10张
  sessionStats.value = { totalReviewed: 0, correctCount: 0, correctRate: 0 }
  
  try {
    // 拉取卡组信息用于标题
    const deck = await flashcardApi.getDeckDetail(deckId.value)
    deckInfo.value = deck || {}

    // 开始学习会话（后端会返回第一张卡片或 null）
    const first = await flashcardApi.startStudy({ deckId: deckId.value })
    if (first) {
      currentCard.value = mapStudyCard(first)
      currentIndex.value = first.current || 1
      totalCards.value = first.total || 10
      isFlipped.value = false
    } else {
      studyComplete.value = true
      calculateSessionStats()
    }
  } catch (error) {
    console.error('开始学习失败:', error)
    ElMessage.error('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载下一张卡片
const loadNextCard = async () => {
  try {
    const response = await flashcardApi.getNextCard(deckId.value)
    if (response) {
      currentCard.value = mapStudyCard(response)
      currentIndex.value = response.current || (currentIndex.value + 1)
      totalCards.value = response.total || 10
      isFlipped.value = false
    } else {
      // 本轮学习完成
      studyComplete.value = true
      calculateSessionStats()
    }
  } catch (error) {
    console.error('加载卡片失败:', error)
    if (error?.response?.status === 404) {
      studyComplete.value = true
      calculateSessionStats()
    }
  }
}

// 翻转卡片
const flipCard = () => {
  isFlipped.value = !isFlipped.value
}

// 将后端 StudyCardResponse 映射为前端使用的结构
const mapStudyCard = (resp) => {
  return {
    id: resp.cardId,
    front: resp.frontContent,
    back: resp.backContent,
    deckId: resp.deckId,
    deckName: resp.deckName,
    cardType: 1 // 暂无类型字段，默认普通题型
  }
}

// 提交反馈
const submitFeedback = async (quality) => {
  if (!currentCard.value) return
  
  try {
    await flashcardApi.submitReview({
      cardId: currentCard.value.id,
      quality: quality
    })
    
    // 更新统计
    sessionStats.value.totalReviewed++
    if (quality >= 2) {
      sessionStats.value.correctCount++
    }
    
    // 加载下一张
    await loadNextCard()
  } catch (error) {
    console.error('提交反馈失败:', error)
    ElMessage.error('提交失败，请重试')
  }
}

// 计算本轮统计
const calculateSessionStats = () => {
  if (sessionStats.value.totalReviewed > 0) {
    sessionStats.value.correctRate = 
      sessionStats.value.correctCount / sessionStats.value.totalReviewed
  }
}

// 获取下次复习间隔文本
const getIntervalText = (quality) => {
  const intervals = {
    1: '10分钟后',
    2: '1天后',
    3: '4天后'
  }
  return intervals[quality] || ''
}

// 获取卡片类型文本
const getCardTypeText = (type) => {
  const typeMap = {
    1: '概念题',
    2: '代码题',
    3: '场景题'
  }
  return typeMap[type] || '问题'
}

// 获取卡片类型样式
const getCardTypeBadgeClass = (type) => {
  const classMap = {
    1: 'type-concept',
    2: 'type-code',
    3: 'type-scenario'
  }
  return classMap[type] || ''
}

// 继续学习
const continueStudy = () => {
  startStudy()
}

// 返回
const goBack = () => {
  router.push('/flashcard')
}

// 键盘事件处理
const handleKeyDown = (e) => {
  if (studyComplete.value || !currentCard.value) return
  
  switch (e.code) {
    case 'Space':
      e.preventDefault()
      flipCard()
      break
    case 'Digit1':
    case 'Numpad1':
      if (isFlipped.value) submitFeedback(0)
      break
    case 'Digit2':
    case 'Numpad2':
      if (isFlipped.value) submitFeedback(1)
      break
    case 'Digit3':
    case 'Numpad3':
      if (isFlipped.value) submitFeedback(2)
      break
    case 'Digit4':
    case 'Numpad4':
      if (isFlipped.value) submitFeedback(3)
      break
  }
}
</script>

<style lang="scss" scoped>
.study-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  display: flex;
  flex-direction: column;
}

// 顶部导航
.study-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px 20px;
  color: white;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
}

.deck-info {
  text-align: center;
  
  h2 {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 4px 0;
  }
  
  .progress-text {
    font-size: 14px;
    opacity: 0.8;
  }
}

// 完成状态
.complete-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  text-align: center;
  
  .complete-icon {
    font-size: 80px;
    margin-bottom: 20px;
  }
  
  h2 {
    font-size: 28px;
    margin: 0 0 8px 0;
  }
  
  p {
    font-size: 16px;
    opacity: 0.9;
    margin: 0 0 32px 0;
  }
}

.complete-stats {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
  
  .stat {
    text-align: center;
    
    .value {
      display: block;
      font-size: 36px;
      font-weight: bold;
    }
    
    .label {
      font-size: 14px;
      opacity: 0.8;
    }
  }
}

.complete-actions {
  display: flex;
  gap: 16px;
}

.primary-btn, .secondary-btn {
  padding: 14px 32px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.primary-btn {
  background: white;
  color: #667eea;
  border: none;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  }
}

.secondary-btn {
  background: transparent;
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.5);
  
  &:hover {
    border-color: white;
    background: rgba(255, 255, 255, 0.1);
  }
}

// 加载和空状态
.loading-state, .empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  
  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }
  
  p {
    font-size: 16px;
    opacity: 0.9;
    margin: 16px 0;
  }
}

// 闪卡区域
.flashcard-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

// 闪卡
.flashcard {
  width: 100%;
  max-width: 800px;
  height: 520px;
  perspective: 1000px;
  cursor: pointer;
  margin-bottom: 32px;
  
  @media (max-width: 768px) {
    height: 450px;
    max-width: 95%;
  }
}

.card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 20px;
  padding: 28px;
  display: flex;
  flex-direction: column;
  transition: transform 0.6s;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.card-front {
  background: white;
  transform: rotateY(0deg);
}

.card-back {
  background: #f8f9fc;
  transform: rotateY(180deg);
}

.flashcard.flipped {
  .card-front {
    transform: rotateY(-180deg);
  }
  
  .card-back {
    transform: rotateY(0deg);
  }
}

.card-type-badge {
  align-self: flex-start;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 16px;
  
  &.type-concept { background: #e8f4fd; color: #409eff; }
  &.type-code { background: #f4ecfb; color: #9c27b0; }
  &.type-scenario { background: #fdf2e9; color: #e6a23c; }
}

.card-content {
  flex: 1;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  overflow-y: auto;
  width: 100%;
  
  &.markdown-body {
    padding: 8px 4px;
  }
}

.question-text {
  font-size: 18px;
  line-height: 1.8;
  color: #333;
  text-align: center;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
}

.answer-text {
  font-size: 15px;
  line-height: 1.75;
  color: #333;
  text-align: left;
  width: 100%;
  
  :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    margin: 16px 0 8px;
    font-weight: 600;
    color: #1a1a1a;
  }
  
  :deep(h1) { font-size: 1.5em; }
  :deep(h2) { font-size: 1.3em; }
  :deep(h3) { font-size: 1.15em; }
  
  :deep(p) {
    margin: 8px 0;
  }
  
  :deep(ul), :deep(ol) {
    padding-left: 20px;
    margin: 8px 0;
  }
  
  :deep(li) {
    margin: 4px 0;
  }
  
  :deep(code) {
    background: #f5f5f5;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
    font-size: 0.9em;
    color: #e83e8c;
  }
  
  :deep(pre) {
    background: #2d2d2d;
    color: #f8f8f2;
    padding: 14px;
    border-radius: 8px;
    overflow-x: auto;
    text-align: left;
    margin: 12px 0;
    font-size: 13px;
    
    code {
      background: transparent;
      color: inherit;
      padding: 0;
    }
  }
  
  :deep(blockquote) {
    border-left: 4px solid #667eea;
    padding-left: 16px;
    margin: 12px 0;
    color: #666;
    background: #f9f9f9;
    padding: 12px 16px;
    border-radius: 0 8px 8px 0;
  }
  
  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;
    font-size: 14px;
    
    th, td {
      border: 1px solid #e0e0e0;
      padding: 8px 12px;
      text-align: left;
    }
    
    th {
      background: #f5f5f5;
      font-weight: 600;
    }
  }
  
  :deep(strong) {
    font-weight: 600;
    color: #1a1a1a;
  }
  
  :deep(a) {
    color: #667eea;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.flip-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
  margin-top: 16px;
}

// 反馈区域
.feedback-area {
  text-align: center;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feedback-hint {
  color: white;
  font-size: 16px;
  margin: 0 0 16px 0;
  opacity: 0.9;
}

.feedback-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  
  @media (max-width: 600px) {
    gap: 8px;
  }
}

.feedback-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 20px;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 100px;
  
  @media (max-width: 600px) {
    padding: 12px 16px;
    min-width: 80px;
  }
  
  .btn-icon {
    font-size: 28px;
    margin-bottom: 6px;
    
    @media (max-width: 600px) {
      font-size: 24px;
    }
  }
  
  .btn-text {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 4px;
  }
  
  .btn-interval {
    font-size: 11px;
    opacity: 0.7;
  }
  
  &:hover {
    transform: translateY(-4px);
  }
  
  &.forgot {
    background: #fef0f0;
    color: #f56c6c;
    
    &:hover {
      box-shadow: 0 6px 20px rgba(245, 108, 108, 0.3);
    }
  }
  
  &.hard {
    background: #fdf6ec;
    color: #e6a23c;
    
    &:hover {
      box-shadow: 0 6px 20px rgba(230, 162, 60, 0.3);
    }
  }
  
  &.good {
    background: #f0f9eb;
    color: #67c23a;
    
    &:hover {
      box-shadow: 0 6px 20px rgba(103, 194, 58, 0.3);
    }
  }
  
  &.easy {
    background: #ecf5ff;
    color: #409eff;
    
    &:hover {
      box-shadow: 0 6px 20px rgba(64, 158, 255, 0.3);
    }
  }
}

// 快捷键提示
.keyboard-hints {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding: 16px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  
  kbd {
    background: rgba(255, 255, 255, 0.2);
    padding: 2px 8px;
    border-radius: 4px;
    margin-right: 4px;
  }
  
  @media (max-width: 600px) {
    display: none;
  }
}
</style>
