<template>
  <div class="mock-interview-index">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Mic /></el-icon>
          AI 模拟面试
        </h1>
        <p class="page-subtitle">智能面试官，助你轻松拿下心仪Offer</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" size="large" @click="goToHistory" :icon="Clock">
          历史记录
        </el-button>
      </div>
    </div>

    <!-- 用户统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalInterviews || 0 }}</div>
              <div class="stat-label">累计面试</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon><TrophyBase /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.avgScore || 0 }}</div>
              <div class="stat-label">平均分数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.highestScore || 0 }}</div>
              <div class="stat-label">最高分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.interviewStreak || 0 }}</div>
              <div class="stat-label">连续练习(天)</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 面试方向选择 -->
    <el-card shadow="never" class="direction-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">
            <el-icon><Aim /></el-icon>
            选择面试方向
          </span>
          <span class="header-tip">选择你想要挑战的技术方向</span>
        </div>
      </template>

      <div v-loading="loading" class="direction-grid">
        <div 
          v-for="direction in directions" 
          :key="direction.directionCode"
          class="direction-item"
          :class="{ 'is-selected': selectedDirection === direction.directionCode }"
          @click="selectDirection(direction)"
        >
          <div class="direction-icon">
            <el-icon :size="32">
              <component :is="getDirectionIcon(direction.directionCode)" />
            </el-icon>
          </div>
          <div class="direction-info">
            <h3 class="direction-name">{{ direction.directionName }}</h3>
            <p class="direction-desc">{{ direction.description || '技术面试' }}</p>
          </div>
          <div class="direction-check" v-if="selectedDirection === direction.directionCode">
            <el-icon><Check /></el-icon>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 开始面试按钮 -->
    <div class="start-section">
      <el-button 
        type="primary" 
        size="large" 
        class="start-btn"
        :disabled="!selectedDirection"
        @click="goToConfig"
      >
        <el-icon><VideoPlay /></el-icon>
        开始 AI 面试
      </el-button>
      <p class="start-tip" v-if="!selectedDirection">请先选择面试方向</p>
    </div>

    <!-- 功能介绍 -->
    <el-card shadow="never" class="feature-card">
      <template #header>
        <span class="header-title">
          <el-icon><InfoFilled /></el-icon>
          功能特点
        </span>
      </template>

      <el-row :gutter="20">
        <el-col :span="6">
          <div class="feature-item">
            <div class="feature-icon">🤖</div>
            <h4>AI 智能出题</h4>
            <p>根据方向和难度智能生成面试题</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="feature-item">
            <div class="feature-icon">💬</div>
            <h4>实时评价反馈</h4>
            <p>AI即时评价答案，给出改进建议</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="feature-item">
            <div class="feature-icon">🎯</div>
            <h4>追问深入考察</h4>
            <p>模拟真实面试，追问考察深度</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="feature-item">
            <div class="feature-icon">📊</div>
            <h4>详细面试报告</h4>
            <p>多维度评分，个性化学习建议</p>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Mic, Clock, DataLine, TrophyBase, Timer, Calendar, Aim, Check, 
  VideoPlay, InfoFilled, Monitor, Cpu, DataBoard, Connection, Setting
} from '@element-plus/icons-vue'
import { mockInterviewApi } from '@/api/mockInterview'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const directions = ref([])
const selectedDirection = ref(null)
const stats = reactive({
  totalInterviews: 0,
  completedInterviews: 0,
  avgScore: 0,
  highestScore: 0,
  totalQuestions: 0,
  correctQuestions: 0,
  correctRate: 0,
  interviewStreak: 0,  // 连续面试天数
  maxStreak: 0,
  completionRate: 0
})

// 获取方向图标
const getDirectionIcon = (directionCode) => {
  const iconMap = {
    'java': Cpu,
    'frontend': Monitor,
    'python': DataBoard,
    'go': Connection,
    'fullstack': Setting,
    'database': DataBoard,
    'devops': Setting,
    'algorithm': DataLine
  }
  return iconMap[directionCode] || Cpu
}

// 选择方向
const selectDirection = (direction) => {
  selectedDirection.value = direction.directionCode
}

// 跳转到配置页面
const goToConfig = () => {
  if (!selectedDirection.value) {
    ElMessage.warning('请先选择面试方向')
    return
  }
  router.push({
    path: '/mock-interview/config',
    query: { direction: selectedDirection.value }
  })
}

// 跳转到历史记录
const goToHistory = () => {
  router.push('/mock-interview/history')
}

// 获取方向列表
const fetchDirections = async () => {
  loading.value = true
  try {
    const data = await mockInterviewApi.getDirections()
    directions.value = data || []
  } catch (error) {
    console.error('获取方向列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const data = await mockInterviewApi.getStats()
    if (data) {
      Object.assign(stats, data)
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

onMounted(() => {
  fetchDirections()
  fetchStats()
})
</script>

<style scoped>
.mock-interview-index {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-subtitle {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 方向选择 */
.direction-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-tip {
  font-size: 13px;
  color: #909399;
}

.direction-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.direction-item {
  position: relative;
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.direction-item:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.direction-item.is-selected {
  border-color: #409eff;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9ff 100%);
}

.direction-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 12px;
}

.direction-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px 0;
  color: #303133;
}

.direction-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.direction-check {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #67c23a;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 开始按钮 */
.start-section {
  text-align: center;
  margin-bottom: 24px;
}

.start-btn {
  width: 240px;
  height: 56px;
  font-size: 18px;
  border-radius: 28px;
}

.start-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
}

/* 功能介绍 */
.feature-card {
  border-radius: 12px;
}

.feature-item {
  text-align: center;
  padding: 20px;
}

.feature-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.feature-item h4 {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #303133;
}

.feature-item p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .direction-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .direction-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stats-row .el-col {
    margin-bottom: 12px;
  }
}
</style>
