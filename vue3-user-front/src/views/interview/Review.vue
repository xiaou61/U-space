<template>
  <div class="review-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <el-button @click="goBack" :icon="Back" text>返回题库</el-button>
      <h1 class="page-title">📖 智能复习</h1>
      <div class="header-right">
        <el-select v-model="filterType" size="small" style="width: 120px;">
          <el-option label="全部" value="all" />
          <el-option label="已逾期" value="overdue" />
          <el-option label="今日" value="today" />
          <el-option label="本周" value="week" />
        </el-select>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card overdue">
        <span class="stat-icon">🔴</span>
        <div class="stat-info">
          <span class="stat-value">{{ reviewStats.overdueCount || 0 }}</span>
          <span class="stat-label">已逾期</span>
        </div>
      </div>
      <div class="stat-card today">
        <span class="stat-icon">🟡</span>
        <div class="stat-info">
          <span class="stat-value">{{ reviewStats.todayCount || 0 }}</span>
          <span class="stat-label">今日待复习</span>
        </div>
      </div>
      <div class="stat-card week">
        <span class="stat-icon">🟢</span>
        <div class="stat-info">
          <span class="stat-value">{{ reviewStats.weekCount || 0 }}</span>
          <span class="stat-label">本周待复习</span>
        </div>
      </div>
      <div class="stat-card total">
        <span class="stat-icon">📚</span>
        <div class="stat-info">
          <span class="stat-value">{{ reviewStats.totalLearned || 0 }}</span>
          <span class="stat-label">已学习</span>
        </div>
      </div>
    </div>

    <!-- 复习列表 -->
    <div class="review-list" v-loading="loading">
      <el-empty v-if="!loading && reviewList.length === 0" description="暂无待复习题目，继续学习新题目吧！" />
      
      <div v-else class="question-list">
        <div 
          v-for="item in reviewList" 
          :key="item.questionId" 
          class="question-item"
          @click="goToQuestion(item)"
        >
          <div class="question-main">
            <h3 class="question-title">{{ item.questionTitle }}</h3>
            <div class="question-meta">
              <el-tag size="small" type="info">{{ item.questionSetTitle }}</el-tag>
              <el-tag 
                size="small" 
                :type="getMasteryTagType(item.masteryLevel)"
              >
                {{ getMasteryText(item.masteryLevel) }}
              </el-tag>
              <span class="review-count">已复习 {{ item.reviewCount }} 次</span>
            </div>
          </div>
          <div class="question-extra">
            <div v-if="item.overdueDays > 0" class="overdue-badge">
              逾期 {{ item.overdueDays }} 天
            </div>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 掌握度分布 -->
    <div class="mastery-distribution">
      <h3 class="section-title">掌握度分布</h3>
      <div class="distribution-bars">
        <div class="bar-item">
          <div class="bar-label">
            <span>❌ 不会</span>
            <span>{{ reviewStats.level1Count || 0 }} 题</span>
          </div>
          <el-progress 
            :percentage="getPercentage(reviewStats.level1Count)" 
            :stroke-width="12"
            color="#F56C6C"
            :show-text="false"
          />
        </div>
        <div class="bar-item">
          <div class="bar-label">
            <span>🤔 模糊</span>
            <span>{{ reviewStats.level2Count || 0 }} 题</span>
          </div>
          <el-progress 
            :percentage="getPercentage(reviewStats.level2Count)" 
            :stroke-width="12"
            color="#E6A23C"
            :show-text="false"
          />
        </div>
        <div class="bar-item">
          <div class="bar-label">
            <span>😊 熟悉</span>
            <span>{{ reviewStats.level3Count || 0 }} 题</span>
          </div>
          <el-progress 
            :percentage="getPercentage(reviewStats.level3Count)" 
            :stroke-width="12"
            color="#409EFF"
            :show-text="false"
          />
        </div>
        <div class="bar-item">
          <div class="bar-label">
            <span>✅ 已掌握</span>
            <span>{{ reviewStats.level4Count || 0 }} 题</span>
          </div>
          <el-progress 
            :percentage="getPercentage(reviewStats.level4Count)" 
            :stroke-width="12"
            color="#67C23A"
            :show-text="false"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Back, ArrowRight } from '@element-plus/icons-vue'
import { interviewApi } from '@/api/interview'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const filterType = ref('all')
const reviewList = ref([])
const reviewStats = ref({
  overdueCount: 0,
  todayCount: 0,
  weekCount: 0,
  totalLearned: 0,
  level1Count: 0,
  level2Count: 0,
  level3Count: 0,
  level4Count: 0
})

// 从URL参数获取初始筛选类型
onMounted(() => {
  const typeParam = route.query.type
  if (typeParam && ['all', 'overdue', 'today', 'week'].includes(typeParam)) {
    filterType.value = typeParam
  }
  fetchData()
})

// 监听筛选类型变化
watch(filterType, () => {
  fetchReviewList()
})

const fetchData = async () => {
  await Promise.all([
    fetchReviewStats(),
    fetchReviewList()
  ])
}

const fetchReviewStats = async () => {
  try {
    const res = await interviewApi.getReviewStats()
    // request拦截器已经提取了data
    if (res) {
      reviewStats.value = res
    }
  } catch (error) {
    console.error('获取复习统计失败', error)
  }
}

const fetchReviewList = async () => {
  loading.value = true
  try {
    const res = await interviewApi.getReviewList(filterType.value)
    // request拦截器已经提取了data
    reviewList.value = res || []
  } catch (error) {
    console.error('获取复习列表失败', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/interview')
}

const goToQuestion = (item) => {
  router.push(`/interview/questions/${item.questionSetId}/${item.questionId}`)
}

const getMasteryText = (level) => {
  const texts = ['', '不会', '模糊', '熟悉', '已掌握']
  return texts[level] || '未知'
}

const getMasteryTagType = (level) => {
  const types = ['', 'danger', 'warning', 'primary', 'success']
  return types[level] || ''
}

const getPercentage = (count) => {
  const total = reviewStats.value.totalLearned || 1
  return Math.round((count || 0) / total * 100)
}
</script>

<style scoped>
.review-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  flex: 1;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  font-size: 28px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.review-list {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  min-height: 300px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f9fafc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.question-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.question-main {
  flex: 1;
}

.question-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-count {
  font-size: 12px;
  color: #909399;
}

.question-extra {
  display: flex;
  align-items: center;
  gap: 12px;
}

.overdue-badge {
  padding: 4px 8px;
  background: #fef0f0;
  color: #F56C6C;
  font-size: 12px;
  border-radius: 4px;
}

.arrow-icon {
  color: #c0c4cc;
}

.mastery-distribution {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.distribution-bars {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bar-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bar-label {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #606266;
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .page-header {
    flex-wrap: wrap;
  }

  .page-title {
    order: -1;
    width: 100%;
    margin-bottom: 12px;
  }
}
</style>
