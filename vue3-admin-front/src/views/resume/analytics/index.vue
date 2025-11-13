<template>
  <div class="resume-analytics-page" v-loading="loading">
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div>
          <h2>简历数据总览</h2>
          <p>实时掌握模板与简历的全局表现</p>
        </div>
        <el-button type="primary" @click="fetchAnalytics">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="16" class="metric-grid">
      <el-col :xs="12" :md="6">
        <div class="metric-card primary">
          <p class="label">模板总数</p>
          <p class="value">{{ summary?.templateCount ?? 0 }}</p>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="metric-card">
          <p class="label">简历总数</p>
          <p class="value">{{ summary?.resumeCount ?? 0 }}</p>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="metric-card">
          <p class="label">发布简历</p>
          <p class="value">{{ summary?.publishedResumeCount ?? 0 }}</p>
          <el-progress :percentage="publishedRate" :stroke-width="6" />
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="metric-card">
          <p class="label">草稿</p>
          <p class="value">{{ summary?.draftResumeCount ?? 0 }}</p>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>访问行为</span>
            </div>
          </template>
          <div class="stat-item">
            <div>
              <p class="stat-label">累计浏览</p>
              <p class="stat-value">{{ summary?.totalViews ?? 0 }}</p>
            </div>
            <el-tag type="success">+{{ summary?.totalShares ?? 0 }} 分享带来</el-tag>
          </div>
          <div class="stat-item">
            <div>
              <p class="stat-label">导出次数</p>
              <p class="stat-value">{{ summary?.totalExports ?? 0 }}</p>
            </div>
            <el-progress :percentage="exportRate" :stroke-width="6" status="success" />
          </div>
          <div class="stat-item">
            <div>
              <p class="stat-label">分享次数</p>
              <p class="stat-value">{{ summary?.totalShares ?? 0 }}</p>
            </div>
            <el-progress :percentage="shareRate" :stroke-width="6" status="warning" />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>优化建议</span>
            </div>
          </template>
          <el-empty description="暂无巡检数据，查看健康巡检页获取详情" v-if="!healthTips.length" />
          <ul class="tips-list" v-else>
            <li v-for="tip in healthTips" :key="tip">
              <el-icon><Warning /></el-icon>
              {{ tip }}
            </li>
          </ul>
          <el-button text type="primary" @click="goReports">前往健康巡检</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Warning } from '@element-plus/icons-vue'
import { resumeApi } from '@/api/resume'

const router = useRouter()
const loading = ref(false)
const summary = ref(null)
const healthTips = ref([])

const fetchAnalytics = async () => {
  loading.value = true
  try {
    summary.value = await resumeApi.getAnalytics()
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

const loadHealthTips = async () => {
  try {
    const reports = await resumeApi.getHealthReports()
    healthTips.value = (reports || []).slice(0, 3).map(item => `${item.resumeName}: ${item.issue}`)
  } catch (error) {
    healthTips.value = []
  }
}

const publishedRate = computed(() => {
  if (!summary.value?.resumeCount) return 0
  return Math.round((summary.value.publishedResumeCount / summary.value.resumeCount) * 100)
})

const exportRate = computed(() => {
  if (!summary.value?.totalViews) return 0
  return Math.min(100, Math.round((summary.value.totalExports / summary.value.totalViews) * 100))
})

const shareRate = computed(() => {
  if (!summary.value?.resumeCount) return 0
  return Math.min(100, Math.round((summary.value.totalShares / summary.value.resumeCount) * 100))
})

const goReports = () => {
  router.push('/resume/reports')
}

onMounted(() => {
  fetchAnalytics()
  loadHealthTips()
})
</script>

<style scoped>
.resume-analytics-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.header-card {
  margin-bottom: 16px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-grid {
  margin-bottom: 16px;
}

.metric-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

.metric-card.primary {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
}

.metric-card .label {
  margin: 0;
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

.metric-card.primary .label {
  color: rgba(255, 255, 255, 0.9);
}

.metric-card .value {
  margin: 6px 0 0;
  font-size: 28px;
  font-weight: 600;
}

.stat-card {
  margin-bottom: 16px;
  min-height: 260px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.stat-label {
  margin: 0;
  color: #909399;
}

.stat-value {
  margin: 6px 0 0;
  font-size: 26px;
  font-weight: 600;
  color: #1f2d3d;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0 0 12px;
}

.tips-list li {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
  color: #ff9f43;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
