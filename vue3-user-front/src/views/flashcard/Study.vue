<template>
  <div class="study-page">
    <!-- 返回按钮 -->
    <div class="study-header">
      <el-button :icon="ArrowLeft" text @click="goBack">退出学习</el-button>
      <h2 class="deck-name" v-if="deckName">{{ deckName }}</h2>
    </div>

    <!-- 学习进度 -->
    <StudyProgress 
      :current="studiedCount"
      :total="totalCards"
      :new-count="remainingNew"
      :due-count="remainingDue"
      class="progress-section"
    />

    <!-- 学习区域 -->
    <div class="study-area" v-if="currentCard && !studyComplete">
      <FlashCard 
        ref="flashcardRef"
        :front-content="currentCard.frontContent"
        :back-content="currentCard.backContent"
        :content-type="currentCard.contentType"
        @flip="handleFlip"
      />

      <!-- 评分按钮 -->
      <transition name="fade">
        <div class="rating-buttons" v-if="showRating">
          <p class="rating-hint">你记得这张卡片吗？</p>
          <div class="buttons-row">
            <el-button 
              class="rating-btn forget"
              @click="submitRating(1)"
              :loading="submitting"
            >
              <span class="rating-label">完全忘记</span>
              <span class="rating-desc">需要重新学习</span>
            </el-button>
            <el-button 
              class="rating-btn hard"
              @click="submitRating(2)"
              :loading="submitting"
            >
              <span class="rating-label">模糊记忆</span>
              <span class="rating-desc">有点印象但不确定</span>
            </el-button>
            <el-button 
              class="rating-btn good"
              @click="submitRating(3)"
              :loading="submitting"
            >
              <span class="rating-label">想起来了</span>
              <span class="rating-desc">经过思考想起来了</span>
            </el-button>
            <el-button 
              class="rating-btn easy"
              @click="submitRating(4)"
              :loading="submitting"
            >
              <span class="rating-label">完全记住</span>
              <span class="rating-desc">轻松回忆无压力</span>
            </el-button>
          </div>
        </div>
      </transition>
    </div>

    <!-- 学习完成 -->
    <div class="study-complete" v-else-if="studyComplete">
      <div class="complete-icon">🎉</div>
      <h2>学习完成！</h2>
      <p class="complete-message">太棒了，你已经完成今天的学习任务</p>
      
      <div class="complete-stats">
        <div class="stat-item">
          <span class="value">{{ studiedCount }}</span>
          <span class="label">学习卡片</span>
        </div>
        <div class="stat-item">
          <span class="value">{{ studyDuration }}</span>
          <span class="label">学习时长(分钟)</span>
        </div>
      </div>

      <div class="complete-actions">
        <el-button type="primary" size="large" @click="goBack">返回卡组</el-button>
        <el-button size="large" @click="continueStudy">继续学习</el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <el-icon class="loading-icon" :size="48"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-if="!loading && !currentCard && !studyComplete">
      <el-empty description="暂无待学习的卡片">
        <el-button type="primary" @click="goBack">返回</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { flashcardApi } from '@/api/flashcard'
import FlashCard from './components/FlashCard.vue'
import StudyProgress from './components/StudyProgress.vue'

const router = useRouter()
const route = useRoute()

const flashcardRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const showRating = ref(false)
const studyComplete = ref(false)

const deckId = computed(() => route.params.deckId)
const deckName = ref('')
const cardQueue = ref([])
const currentCard = ref(null)
const studiedCount = ref(0)
const totalCards = ref(0)
const remainingNew = ref(0)
const remainingDue = ref(0)

// 学习时长计算
const startTime = ref(null)
const cardStartTime = ref(null)

const studyDuration = computed(() => {
  if (!startTime.value) return 0
  return Math.round((Date.now() - startTime.value) / 60000)
})

// 加载学习卡片
const loadStudyCards = async () => {
  loading.value = true
  try {
    let cards
    if (deckId.value) {
      // 卡组学习模式
      const deck = await flashcardApi.getDeckById(deckId.value)
      deckName.value = deck.name
      cards = await flashcardApi.getCardsByDeckId(deckId.value)
      // 过滤出待学习的卡片（简化：取所有未掌握的）
      cards = cards.filter(c => !c.studyStatus || c.studyStatus.masteryLevel < 3)
    } else {
      // 全局学习模式
      cards = await flashcardApi.getTodayStudyCards(50)
    }

    cardQueue.value = cards || []
    totalCards.value = cardQueue.value.length
    remainingNew.value = cardQueue.value.filter(c => !c.studyStatus || c.studyStatus.masteryLevel === 1).length
    remainingDue.value = totalCards.value - remainingNew.value

    if (cardQueue.value.length > 0) {
      loadNextCard()
      startTime.value = Date.now()
    }
  } catch (error) {
    console.error('加载卡片失败:', error)
    ElMessage.error('加载卡片失败')
  } finally {
    loading.value = false
  }
}

// 加载下一张卡片
const loadNextCard = () => {
  if (cardQueue.value.length === 0) {
    studyComplete.value = true
    return
  }

  currentCard.value = cardQueue.value.shift()
  showRating.value = false
  cardStartTime.value = Date.now()

  // 重置翻转状态
  if (flashcardRef.value) {
    flashcardRef.value.reset()
  }
}

// 翻转卡片
const handleFlip = (isFlipped) => {
  if (isFlipped) {
    showRating.value = true
  }
}

// 提交评分
const submitRating = async (quality) => {
  if (!currentCard.value) return

  submitting.value = true
  try {
    const duration = Math.round((Date.now() - cardStartTime.value) / 1000)
    
    const result = await flashcardApi.submitStudyResult({
      cardId: currentCard.value.id,
      quality,
      duration
    })

    studiedCount.value++
    remainingNew.value = result.remainingNew ?? remainingNew.value
    remainingDue.value = result.remainingDue ?? remainingDue.value

    // 如果评分低，将卡片重新加入队列
    if (quality <= 2) {
      cardQueue.value.push(currentCard.value)
    }

    loadNextCard()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// 继续学习
const continueStudy = () => {
  studyComplete.value = false
  studiedCount.value = 0
  loadStudyCards()
}

// 返回
const goBack = () => {
  if (deckId.value) {
    router.push(`/flashcard/deck/${deckId.value}`)
  } else {
    router.push('/flashcard')
  }
}

// 键盘快捷键
const handleKeydown = (e) => {
  if (!showRating.value || submitting.value) return

  switch (e.key) {
    case '1':
      submitRating(1)
      break
    case '2':
      submitRating(2)
      break
    case '3':
      submitRating(3)
      break
    case '4':
      submitRating(4)
      break
    case ' ':
      e.preventDefault()
      if (flashcardRef.value) {
        flashcardRef.value.flip()
      }
      break
  }
}

onMounted(() => {
  loadStudyCards()
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style lang="scss" scoped>
.study-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}

.study-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  .deck-name {
    font-size: 18px;
    font-weight: 500;
    margin: 0;
    color: var(--el-text-color-primary);
  }
}

.progress-section {
  margin-bottom: 32px;
}

.study-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 32px;
}

.rating-buttons {
  width: 100%;
  max-width: 600px;

  .rating-hint {
    text-align: center;
    color: var(--el-text-color-secondary);
    margin: 0 0 16px 0;
  }

  .buttons-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }

  .rating-btn {
    height: auto;
    padding: 16px 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    border-radius: 12px;
    transition: all 0.2s;

    .rating-label {
      font-size: 14px;
      font-weight: 600;
    }

    .rating-desc {
      font-size: 11px;
      opacity: 0.8;
    }

    &.forget {
      background: #fef0f0;
      border-color: #f56c6c;
      color: #f56c6c;

      &:hover {
        background: #f56c6c;
        color: #fff;
      }
    }

    &.hard {
      background: #fdf6ec;
      border-color: #e6a23c;
      color: #e6a23c;

      &:hover {
        background: #e6a23c;
        color: #fff;
      }
    }

    &.good {
      background: #ecf5ff;
      border-color: #409eff;
      color: #409eff;

      &:hover {
        background: #409eff;
        color: #fff;
      }
    }

    &.easy {
      background: #f0f9eb;
      border-color: #67c23a;
      color: #67c23a;

      &:hover {
        background: #67c23a;
        color: #fff;
      }
    }
  }
}

.study-complete {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;

  .complete-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }

  h2 {
    font-size: 28px;
    margin: 0 0 8px 0;
    color: var(--el-text-color-primary);
  }

  .complete-message {
    color: var(--el-text-color-secondary);
    margin: 0 0 32px 0;
  }

  .complete-stats {
    display: flex;
    gap: 48px;
    margin-bottom: 32px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .value {
        font-size: 36px;
        font-weight: 700;
        color: var(--el-color-primary);
      }

      .label {
        font-size: 14px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .complete-actions {
    display: flex;
    gap: 16px;
  }
}

.loading-state,
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .loading-icon {
    animation: spin 1s linear infinite;
    color: var(--el-color-primary);
  }

  p {
    margin-top: 16px;
    color: var(--el-text-color-secondary);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 640px) {
  .study-page {
    padding: 16px;
  }

  .rating-buttons .buttons-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
