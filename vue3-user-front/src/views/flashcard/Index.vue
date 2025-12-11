<template>
  <div class="flashcard-container">
    <!-- 统计概览卡片 -->
    <div class="stats-card">
      <div class="stats-header">
        <h2>🧠 闪卡记忆</h2>
        <p class="stats-subtitle">科学复习，高效记忆</p>
      </div>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ stats.studyDays || 0 }}</div>
          <div class="stat-label">连续学习</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalCards || 0 }}</div>
          <div class="stat-label">总卡片数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.masteredCards || 0 }}</div>
          <div class="stat-label">已掌握</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.todayReviewed || 0 }}</div>
          <div class="stat-label">今日已复习</div>
        </div>
      </div>
    </div>

    <!-- 今日学习任务 -->
    <div class="section today-section">
      <div class="section-header">
        <h3>📚 今日学习</h3>
        <router-link to="/flashcard/stats" class="view-more">
          查看统计 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      
      <div v-if="todayLoading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <div v-else class="today-task-card">
        <div class="task-info">
          <div class="task-numbers">
            <div class="number-item">
              <span class="num new">{{ todayStudy.newCount || 0 }}</span>
              <span class="label">新卡片</span>
            </div>
            <div class="number-item">
              <span class="num review">{{ todayStudy.reviewCount || 0 }}</span>
              <span class="label">待复习</span>
            </div>
            <div class="number-item">
              <span class="num done">{{ todayStudy.completedCount || 0 }}</span>
              <span class="label">已完成</span>
            </div>
          </div>
        </div>
        <button 
          class="start-study-btn"
          :disabled="(todayStudy.newCount || 0) + (todayStudy.reviewCount || 0) === 0"
          @click="startStudyToday"
        >
          <el-icon><VideoPlay /></el-icon>
          {{ (todayStudy.newCount || 0) + (todayStudy.reviewCount || 0) > 0 ? '开始学习' : '今日任务已完成' }}
        </button>
      </div>
    </div>

    <!-- 我的卡组 -->
    <div class="section deck-section">
      <div class="section-header">
        <h3>📋 我的卡组</h3>
        <button class="create-btn" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新建卡组
        </button>
      </div>

      <div v-if="deckLoading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="deckList.length === 0" class="empty-state">
        <div class="empty-icon">📚</div>
        <p>暂无卡组，点击上方按钮创建你的第一个卡组</p>
        <p class="empty-tip">或从面试题库生成闪卡</p>
      </div>

      <div v-else class="deck-grid">
        <DeckCard
          v-for="deck in deckList"
          :key="deck.id"
          :deck="deck"
          @study="handleStudyDeck"
          @edit="handleEditDeck"
          @delete="handleDeleteDeck"
          @add-card="handleAddCard"
        />
      </div>
    </div>

    <!-- 官方卡组 -->
    <div class="section official-section">
      <div class="section-header">
        <h3>🏆 官方推荐</h3>
      </div>

      <div v-if="officialLoading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else class="deck-grid">
        <DeckCard
          v-for="deck in officialDecks"
          :key="deck.id"
          :deck="deck"
          :is-official="true"
          @study="handleStudyDeck"
        />
      </div>
    </div>

    <!-- 创建/编辑卡组弹窗 -->
    <CreateDeckDialog
      v-model="showCreateDialog"
      :deck-data="editingDeck"
      @success="onDeckSaved"
    />

    <!-- 生成闪卡弹窗 -->
    <GenerateCardDialog
      v-model="showGenerateDialog"
      :deck-id="selectedDeckId"
      @success="onCardGenerated"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, ArrowRight, VideoPlay, Plus } from '@element-plus/icons-vue'
import flashcardApi from '@/api/flashcard'
import DeckCard from './components/DeckCard.vue'
import CreateDeckDialog from './components/CreateDeckDialog.vue'
import GenerateCardDialog from './components/GenerateCardDialog.vue'

const router = useRouter()

// 统计数据
const stats = ref({})

// 今日学习
const todayStudy = ref({})
const todayLoading = ref(false)

// 卡组列表
const deckList = ref([])
const deckLoading = ref(false)

// 官方卡组
const officialDecks = ref([])
const officialLoading = ref(false)

// 弹窗控制
const showCreateDialog = ref(false)
const editingDeck = ref(null)
const showGenerateDialog = ref(false)
const selectedDeckId = ref(null)

// 页面初始化
onMounted(() => {
  loadStats()
  loadTodayStudy()
  loadAllDecks()
})

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await flashcardApi.getStatsOverview()
    stats.value = response || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载今日学习任务
const loadTodayStudy = async () => {
  todayLoading.value = true
  try {
    const response = await flashcardApi.getTodayStudy()
    todayStudy.value = response || {}
  } catch (error) {
    console.error('加载今日学习失败:', error)
  } finally {
    todayLoading.value = false
  }
}

// 加载所有卡组（一次请求获取个人+官方）
const loadAllDecks = async () => {
  deckLoading.value = true
  officialLoading.value = true
  try {
    const response = await flashcardApi.getMyDecks()
    // 后端返回 {official: [], personal: []}
    deckList.value = response?.personal || []
    officialDecks.value = response?.official || []
  } catch (error) {
    console.error('加载卡组失败:', error)
  } finally {
    deckLoading.value = false
    officialLoading.value = false
  }
}

// 开始今日学习
const startStudyToday = () => {
  // 找到有待学习卡片的卡组
  const deckWithCards = deckList.value.find(d => 
    (d.newCount || 0) + (d.reviewCount || 0) > 0
  )
  if (deckWithCards) {
    router.push(`/flashcard/study/${deckWithCards.id}`)
  } else {
    ElMessage.info('暂无待学习的卡片')
  }
}

// 学习卡组
const handleStudyDeck = (deck) => {
  router.push(`/flashcard/study/${deck.id}`)
}

// 编辑卡组
const handleEditDeck = (deck) => {
  editingDeck.value = deck
  showCreateDialog.value = true
}

// 删除卡组
const handleDeleteDeck = async (deck) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除卡组"${deck.name}"吗？删除后卡组内的所有闪卡也会被删除`,
      '警告',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    await flashcardApi.deleteDeck(deck.id)
    ElMessage.success('删除成功')
    loadAllDecks()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除卡组失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 添加卡片
const handleAddCard = (deck) => {
  selectedDeckId.value = deck.id
  showGenerateDialog.value = true
}

// 卡组保存成功
const onDeckSaved = () => {
  editingDeck.value = null
  loadAllDecks()
  loadStats()
}

// 闪卡生成成功
const onCardGenerated = () => {
  loadAllDecks()
  loadTodayStudy()
  loadStats()
}
</script>

<style lang="scss" scoped>
.flashcard-container {
  padding: 24px 32px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  
  @media (max-width: 768px) {
    padding: 16px;
  }
}

// 统计卡片
.stats-card {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border-radius: 16px;
  padding: 24px 32px;
  margin-bottom: 24px;
  color: white;
}

.stats-header {
  text-align: left;
  margin-bottom: 20px;
  
  h2 {
    font-size: 22px;
    font-weight: 600;
    margin: 0 0 4px 0;
  }
  
  .stats-subtitle {
    font-size: 14px;
    opacity: 0.8;
    margin: 0;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  
  @media (max-width: 600px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-item {
  text-align: center;
  padding: 16px 12px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  
  .stat-value {
    font-size: 28px;
    font-weight: bold;
  }
  
  .stat-label {
    font-size: 13px;
    opacity: 0.9;
    margin-top: 4px;
  }
}

// 区块通用样式
.section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
    color: #333;
  }
  
  .view-more {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 14px;
    color: #409eff;
    text-decoration: none;
    
    &:hover {
      color: #337ecc;
    }
  }
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: #337ecc;
    transform: translateY(-1px);
  }
}

// 加载和空状态
.loading-state, .empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }
  
  p {
    margin: 0 0 8px 0;
  }
  
  .empty-tip {
    font-size: 13px;
    color: #bbb;
  }
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

// 今日学习卡片
.today-task-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: #f8f9fc;
  border-radius: 12px;
  
  @media (max-width: 600px) {
    flex-direction: column;
    gap: 16px;
  }
}

.task-numbers {
  display: flex;
  gap: 32px;
  
  @media (max-width: 600px) {
    gap: 24px;
  }
}

.number-item {
  text-align: center;
  
  .num {
    display: block;
    font-size: 32px;
    font-weight: bold;
    
    &.new { color: #409eff; }
    &.review { color: #e6a23c; }
    &.done { color: #67c23a; }
  }
  
  .label {
    font-size: 13px;
    color: #666;
  }
}

.start-study-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 14px 28px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }
  
  &:disabled {
    background: #c0c4cc;
    cursor: not-allowed;
  }
}

// 卡组网格
.deck-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  
  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}
</style>
