<template>
  <el-card class="review-reminder-card" shadow="hover" v-if="hasReviewTasks">
    <div class="card-header">
      <span class="card-title">📚 今日待复习</span>
      <el-button text type="primary" @click="goToReview">查看全部</el-button>
    </div>

    <div class="stats-row">
      <div class="stat-item overdue" v-if="stats.overdueCount > 0">
        <span class="stat-icon">🔴</span>
        <span class="stat-label">紧急复习（已逾期）</span>
        <span class="stat-value">{{ stats.overdueCount }} 题</span>
      </div>
      <div class="stat-item today" v-if="stats.todayCount > 0">
        <span class="stat-icon">🟡</span>
        <span class="stat-label">今日待复习</span>
        <span class="stat-value">{{ stats.todayCount }} 题</span>
      </div>
      <div class="stat-item week" v-if="stats.weekCount > 0">
        <span class="stat-icon">🟢</span>
        <span class="stat-label">本周待复习</span>
        <span class="stat-value">{{ stats.weekCount }} 题</span>
      </div>
    </div>

    <div class="action-row">
      <el-button type="primary" @click="startReview" :disabled="!hasReviewTasks">
        开始复习
      </el-button>
      <span class="learned-info">
        已学习 {{ stats.totalLearned || 0 }} 题
      </span>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { interviewApi } from '@/api/interview'

const router = useRouter()

const stats = ref({
  overdueCount: 0,
  todayCount: 0,
  weekCount: 0,
  totalLearned: 0
})

const hasReviewTasks = computed(() => {
  return stats.value.overdueCount > 0 || stats.value.todayCount > 0 || stats.value.weekCount > 0
})

const fetchStats = async () => {
  try {
    const res = await interviewApi.getReviewStats()
    // request拦截器已经提取了data
    if (res) {
      stats.value = res
    }
  } catch (error) {
    console.error('获取复习统计失败', error)
  }
}

const goToReview = () => {
  router.push('/interview/review')
}

const startReview = () => {
  // 优先复习逾期的题目
  if (stats.value.overdueCount > 0) {
    router.push('/interview/review?type=overdue')
  } else if (stats.value.todayCount > 0) {
    router.push('/interview/review?type=today')
  } else {
    router.push('/interview/review?type=week')
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.review-reminder-card {
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

:deep(.el-card__body) {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.card-header :deep(.el-button) {
  color: rgba(255, 255, 255, 0.9);
}

.card-header :deep(.el-button:hover) {
  color: #fff;
}

.stats-row {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
}

.stat-icon {
  font-size: 14px;
}

.stat-label {
  flex: 1;
  font-size: 14px;
}

.stat-value {
  font-weight: 600;
  font-size: 15px;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-row :deep(.el-button) {
  background: #fff;
  color: #667eea;
  border: none;
}

.action-row :deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.9);
}

.learned-info {
  font-size: 13px;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .stat-item {
    padding: 8px 12px;
  }

  .stat-label {
    font-size: 13px;
  }
}
</style>
