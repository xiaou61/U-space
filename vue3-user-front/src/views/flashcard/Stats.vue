<template>
  <div class="stats-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <router-link to="/flashcard" class="back-link">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </router-link>
      <h1>📊 学习统计</h1>
    </div>

    <!-- 总体统计卡片 -->
    <div class="overview-card">
      <div class="overview-grid">
        <div class="overview-item">
          <div class="item-icon streak">🔥</div>
          <div class="item-info">
            <span class="item-value">{{ overview.studyDays || 0 }}</span>
            <span class="item-label">连续学习天数</span>
          </div>
        </div>
        <div class="overview-item">
          <div class="item-icon cards">📚</div>
          <div class="item-info">
            <span class="item-value">{{ overview.totalCards || 0 }}</span>
            <span class="item-label">总卡片数</span>
          </div>
        </div>
        <div class="overview-item">
          <div class="item-icon mastered">✅</div>
          <div class="item-info">
            <span class="item-value">{{ overview.masteredCards || 0 }}</span>
            <span class="item-label">已掌握</span>
          </div>
        </div>
        <div class="overview-item">
          <div class="item-icon rate">📈</div>
          <div class="item-info">
            <span class="item-value">{{ masteryRate }}%</span>
            <span class="item-label">掌握率</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 今日学习 -->
    <div class="section">
      <h3>📅 今日学习</h3>
      <div class="today-stats">
        <div class="today-item">
          <el-progress 
            type="circle" 
            :percentage="todayProgress" 
            :stroke-width="10"
            :width="100"
            color="#667eea"
          >
            <template #default>
              <span class="progress-text">{{ overview.todayReviewed || 0 }}</span>
            </template>
          </el-progress>
          <span class="today-label">今日已复习</span>
        </div>
        <div class="today-divider"></div>
        <div class="today-detail">
          <div class="detail-row">
            <span class="detail-label">新学习卡片</span>
            <span class="detail-value new">{{ overview.todayNew || 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">复习卡片</span>
            <span class="detail-value review">{{ overview.todayReview || 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">学习时长</span>
            <span class="detail-value">{{ formatDuration(overview.todayDuration) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习趋势 -->
    <div class="section">
      <div class="section-header">
        <h3>📈 学习趋势</h3>
        <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="14">近14天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
        </el-radio-group>
      </div>
      
      <div v-if="trendLoading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <div v-else class="trend-chart">
        <div class="chart-bars">
          <div 
            v-for="(day, index) in trendData" 
            :key="index"
            class="chart-bar-wrapper"
          >
            <div class="bar-tooltip">{{ day.count }}张</div>
            <div 
              class="chart-bar" 
              :style="{ height: getBarHeight(day.count) + '%' }"
            ></div>
            <span class="bar-label">{{ formatDateLabel(day.date) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 卡组进度 -->
    <div class="section">
      <h3>📋 卡组进度</h3>
      
      <div v-if="deckLoading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <div v-else-if="deckProgress.length === 0" class="empty-state">
        <p>暂无卡组数据</p>
      </div>
      
      <div v-else class="deck-progress-list">
        <div 
          v-for="deck in deckProgress" 
          :key="deck.id"
          class="deck-progress-item"
        >
          <div class="deck-info">
            <span class="deck-name">{{ deck.name }}</span>
            <span class="deck-count">{{ deck.masteredCount }} / {{ deck.totalCount }}</span>
          </div>
          <el-progress 
            :percentage="getDeckPercent(deck)" 
            :stroke-width="8"
            :show-text="false"
            :color="getProgressColor(getDeckPercent(deck))"
          />
          <span class="deck-percent">{{ getDeckPercent(deck) }}%</span>
        </div>
      </div>
    </div>

    <!-- 成就徽章 -->
    <div class="section">
      <h3>🏆 学习成就</h3>
      <div class="achievement-grid">
        <div 
          class="achievement-item"
          :class="{ 'unlocked': overview.studyDays >= 7 }"
        >
          <div class="achievement-icon">🌱</div>
          <div class="achievement-info">
            <span class="achievement-name">初学者</span>
            <span class="achievement-desc">连续学习7天</span>
          </div>
        </div>
        <div 
          class="achievement-item"
          :class="{ 'unlocked': overview.studyDays >= 30 }"
        >
          <div class="achievement-icon">🌿</div>
          <div class="achievement-info">
            <span class="achievement-name">坚持者</span>
            <span class="achievement-desc">连续学习30天</span>
          </div>
        </div>
        <div 
          class="achievement-item"
          :class="{ 'unlocked': overview.masteredCards >= 100 }"
        >
          <div class="achievement-icon">📖</div>
          <div class="achievement-info">
            <span class="achievement-name">知识达人</span>
            <span class="achievement-desc">掌握100张卡片</span>
          </div>
        </div>
        <div 
          class="achievement-item"
          :class="{ 'unlocked': overview.totalReviews >= 1000 }"
        >
          <div class="achievement-icon">⚡</div>
          <div class="achievement-info">
            <span class="achievement-name">刷题狂人</span>
            <span class="achievement-desc">累计复习1000次</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import flashcardApi from '@/api/flashcard'

// 总体统计
const overview = ref({})

// 趋势数据
const trendDays = ref(7)
const trendData = ref([])
const trendLoading = ref(false)

// 卡组进度
const deckProgress = ref([])
const deckLoading = ref(false)

// 计算掌握率
const masteryRate = computed(() => {
  if (!overview.value.totalCards) return 0
  return Math.round((overview.value.masteredCards / overview.value.totalCards) * 100)
})

// 今日进度
const todayProgress = computed(() => {
  const target = 20 // 每日目标
  const reviewed = overview.value.todayReviewed || 0
  return Math.min(Math.round((reviewed / target) * 100), 100)
})

// 页面初始化
onMounted(() => {
  loadOverview()
  loadTrend()
  loadDeckProgress()
})

// 加载总体统计
const loadOverview = async () => {
  try {
    const response = await flashcardApi.getStatsOverview()
    overview.value = response || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载趋势数据
const loadTrend = async () => {
  trendLoading.value = true
  try {
    const response = await flashcardApi.getDailyTrend(trendDays.value)
    trendData.value = response || []
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  } finally {
    trendLoading.value = false
  }
}

// 加载卡组进度
const loadDeckProgress = async () => {
  deckLoading.value = true
  try {
    const response = await flashcardApi.getMyDecks()
    deckProgress.value = response || []
  } catch (error) {
    console.error('加载卡组进度失败:', error)
  } finally {
    deckLoading.value = false
  }
}

// 获取柱状图高度
const getBarHeight = (count) => {
  if (trendData.value.length === 0) return 0
  const maxCount = Math.max(...trendData.value.map(d => d.count), 1)
  return Math.round((count / maxCount) * 100)
}

// 格式化日期标签
const formatDateLabel = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes) return '0分钟'
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
}

// 获取卡组掌握百分比
const getDeckPercent = (deck) => {
  if (!deck.totalCount) return 0
  return Math.round((deck.masteredCount / deck.totalCount) * 100)
}

// 获取进度条颜色
const getProgressColor = (percent) => {
  if (percent >= 80) return '#67c23a'
  if (percent >= 50) return '#e6a23c'
  return '#409eff'
}
</script>

<style lang="scss" scoped>
.stats-container {
  padding: 24px 32px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  
  @media (max-width: 768px) {
    padding: 16px;
  }
}

// 页面标题
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  
  .back-link {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #666;
    text-decoration: none;
    font-size: 14px;
    
    &:hover {
      color: #409eff;
    }
  }
  
  h1 {
    font-size: 22px;
    font-weight: 600;
    margin: 0;
    color: #333;
  }
}

// 总体统计卡片
.overview-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  color: white;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  
  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  
  .item-icon {
    font-size: 32px;
  }
  
  .item-info {
    display: flex;
    flex-direction: column;
    
    .item-value {
      font-size: 24px;
      font-weight: bold;
    }
    
    .item-label {
      font-size: 13px;
      opacity: 0.9;
    }
  }
}

// 区块通用
.section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 20px 0;
    color: #333;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h3 {
    margin: 0;
  }
}

// 加载和空状态
.loading-state, .empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

// 今日学习
.today-stats {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 12px;
  
  @media (max-width: 600px) {
    flex-direction: column;
  }
}

.today-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  
  .progress-text {
    font-size: 24px;
    font-weight: bold;
    color: #667eea;
  }
  
  .today-label {
    font-size: 14px;
    color: #666;
  }
}

.today-divider {
  width: 1px;
  height: 80px;
  background: #e0e0e0;
  
  @media (max-width: 600px) {
    width: 100%;
    height: 1px;
  }
}

.today-detail {
  flex: 1;
  
  .detail-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid #eee;
    
    &:last-child {
      border-bottom: none;
    }
    
    .detail-label {
      color: #666;
      font-size: 14px;
    }
    
    .detail-value {
      font-weight: 500;
      
      &.new { color: #409eff; }
      &.review { color: #e6a23c; }
    }
  }
}

// 趋势图表
.trend-chart {
  padding: 20px 0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 200px;
  padding: 0 10px;
}

.chart-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 60px;
  position: relative;
  
  &:hover {
    .bar-tooltip {
      opacity: 1;
    }
    
    .chart-bar {
      background: #764ba2;
    }
  }
}

.bar-tooltip {
  position: absolute;
  top: -30px;
  background: #333;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
  white-space: nowrap;
}

.chart-bar {
  width: 24px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: all 0.3s;
}

.bar-label {
  margin-top: 8px;
  font-size: 11px;
  color: #999;
}

// 卡组进度
.deck-progress-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.deck-progress-item {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .deck-info {
    width: 140px;
    
    .deck-name {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin-bottom: 2px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .deck-count {
      font-size: 12px;
      color: #999;
    }
  }
  
  .el-progress {
    flex: 1;
  }
  
  .deck-percent {
    width: 45px;
    text-align: right;
    font-size: 14px;
    font-weight: 500;
    color: #666;
  }
}

// 成就徽章
.achievement-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.achievement-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 12px;
  border: 2px solid transparent;
  opacity: 0.5;
  filter: grayscale(100%);
  transition: all 0.3s;
  
  &.unlocked {
    opacity: 1;
    filter: none;
    border-color: #667eea;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  }
  
  .achievement-icon {
    font-size: 36px;
  }
  
  .achievement-info {
    display: flex;
    flex-direction: column;
    
    .achievement-name {
      font-size: 14px;
      font-weight: 600;
      color: #333;
      margin-bottom: 2px;
    }
    
    .achievement-desc {
      font-size: 12px;
      color: #999;
    }
  }
}
</style>
