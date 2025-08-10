<template>
  <div class="vote-activities-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Trophy /></el-icon>
        投票活动
      </h2>
      <p class="page-subtitle">参与校园投票，表达你的声音</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-section">
      <el-skeleton animated>
        <template #template>
          <div v-for="i in 3" :key="i" class="activity-skeleton">
            <el-skeleton-item variant="rect" style="width: 100%; height: 60px; margin-bottom: 15px;" />
            <el-skeleton-item variant="text" style="width: 80%; margin-bottom: 10px;" />
            <el-skeleton-item variant="text" style="width: 60%;" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <!-- 活动列表 -->
    <div v-else-if="activities.length > 0" class="activities-list">
      <div 
        v-for="activity in activities" 
        :key="activity.id" 
        class="activity-card"
      >
        <!-- 活动标题和描述 -->
        <div class="activity-header">
          <h3 class="activity-title">{{ activity.title }}</h3>
          <p class="activity-desc">{{ activity.description }}</p>
          <div class="activity-meta">
            <span class="time-info">
              <el-icon><Clock /></el-icon>
              {{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}
            </span>
          </div>
        </div>

        <!-- 投票选项 -->
        <div class="vote-options">
          <div class="options-title">
            <span>投票选项</span>
            <span class="vote-status" :class="getVoteStatusClass(activity.id)">
              {{ getVoteStatusText(activity.id) }}
            </span>
          </div>
          
          <div class="options-list">
            <div 
              v-for="option in activity.options" 
              :key="option.id"
              class="option-item"
              :class="{
                'voted': votedActivities[activity.id],
                'submitting': submittingVotes[activity.id] === option.id
              }"
              @click="handleVote(activity.id, option.id)"
            >
              <div class="option-content">
                <div class="option-info">
                  <img 
                    v-if="option.imageUrl" 
                    :src="option.imageUrl" 
                    :alt="option.optionName"
                    class="option-image"
                  />
                  <div class="option-text">
                    <h4 class="option-name">{{ option.optionName }}</h4>
                    <p v-if="option.optionDesc" class="option-desc">{{ option.optionDesc }}</p>
                  </div>
                </div>
                
                <div class="option-votes">
                  <div class="vote-count">
                    <el-icon><Trophy /></el-icon>
                    <span>{{ option.voteCount || 0 }}</span>
                  </div>
                  
                  <!-- 投票进度条 -->
                  <div class="vote-progress">
                    <div 
                      class="progress-bar"
                      :style="{ width: getVotePercentage(activity.options, option.voteCount) + '%' }"
                    ></div>
                  </div>
                  
                  <span class="vote-percentage">
                    {{ getVotePercentage(activity.options, option.voteCount) }}%
                  </span>
                </div>
              </div>

              <!-- 投票按钮 -->
              <div class="vote-action" v-if="!votedActivities[activity.id]">
                <el-button 
                  type="primary" 
                  size="small"
                  :loading="submittingVotes[activity.id] === option.id"
                  @click.stop="handleVote(activity.id, option.id)"
                >
                  {{ submittingVotes[activity.id] === option.id ? '投票中...' : '投票' }}
                </el-button>
              </div>
              
              <!-- 已投票标识 -->
              <div class="voted-mark" v-else>
                <el-icon class="check-icon"><Check /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty description="暂无进行中的投票活动">
        <template #image>
          <el-icon size="80" color="#d3d3d3"><Trophy /></el-icon>
        </template>
        <template #description>
          <p>当前没有进行中的投票活动</p>
          <p>敬请期待更多精彩活动</p>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Trophy, Clock, Check } from '@element-plus/icons-vue'
import { getActiveVoteActivities, submitVote, checkUserVoted } from '../api/vote'

// 响应式数据
const loading = ref(true)
const activities = ref([])
const votedActivities = reactive({}) // 记录用户已投票的活动
const submittingVotes = reactive({}) // 记录正在提交的投票

// 获取投票活动列表
const fetchActivities = async () => {
  try {
    loading.value = true
    const res = await getActiveVoteActivities()
    
    if (res.code === 200) {
      activities.value = res.data || []
      
      // 检查用户投票状态
      await checkAllVoteStatus()
    } else {
      ElMessage.error(res.msg || '获取活动列表失败')
    }
  } catch (error) {
    console.error('获取活动列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 检查所有活动的投票状态
const checkAllVoteStatus = async () => {
  for (const activity of activities.value) {
    try {
      const res = await checkUserVoted(activity.id)
      if (res.code === 200) {
        votedActivities[activity.id] = res.data
      }
    } catch (error) {
      console.error(`检查活动 ${activity.id} 投票状态失败:`, error)
    }
  }
}

// 处理投票
const handleVote = async (activityId, optionId) => {
  // 如果已经投过票，不允许再投
  if (votedActivities[activityId]) {
    ElMessage.warning('您已经参与过该活动的投票')
    return
  }

  // 如果正在提交，不允许重复提交
  if (submittingVotes[activityId]) {
    return
  }

  try {
    submittingVotes[activityId] = optionId
    
    const res = await submitVote(activityId, optionId)
    
    if (res.code === 200) {
      ElMessage.success('投票成功！')
      votedActivities[activityId] = true
      
      // 刷新活动列表以更新票数
      await fetchActivities()
    } else {
      ElMessage.error(res.msg || '投票失败')
    }
  } catch (error) {
    console.error('投票错误:', error)
    ElMessage.error('网络错误，投票失败')
  } finally {
    delete submittingVotes[activityId]
  }
}

// 计算投票百分比
const getVotePercentage = (options, voteCount) => {
  if (!options || options.length === 0) return 0
  
  const totalVotes = options.reduce((sum, option) => sum + (option.voteCount || 0), 0)
  if (totalVotes === 0) return 0
  
  return Math.round(((voteCount || 0) / totalVotes) * 100)
}

// 获取投票状态文本
const getVoteStatusText = (activityId) => {
  return votedActivities[activityId] ? '已投票' : '未投票'
}

// 获取投票状态样式类
const getVoteStatusClass = (activityId) => {
  return votedActivities[activityId] ? 'voted' : 'not-voted'
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 页面加载时获取数据
onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.vote-activities-page {
  padding: 20px 16px 80px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
}

.page-title .el-icon {
  color: #409eff;
}

.page-subtitle {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.loading-section {
  padding: 20px;
}

.activity-skeleton {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.activity-card:hover {
  transform: translateY(-2px);
}

.activity-header {
  margin-bottom: 20px;
}

.activity-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
}

.activity-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 12px 0;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 12px;
}

.vote-options {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.options-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: bold;
  color: #333;
}

.vote-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: normal;
}

.vote-status.voted {
  background-color: #f0f9ff;
  color: #1890ff;
}

.vote-status.not-voted {
  background-color: #fff7e6;
  color: #fa8c16;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.option-item:hover:not(.voted) {
  border-color: #409eff;
  background-color: #fafbfc;
  transform: translateY(-1px);
  box-shadow: 0 3px 12px rgba(64, 158, 255, 0.15);
}

.option-item.voted {
  background-color: #f6ffed;
  border-color: #b7eb8f;
  cursor: default;
}

.option-item.submitting {
  opacity: 0.7;
  cursor: not-allowed;
}

.option-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.option-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.option-image {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.option-text {
  flex: 1;
}

.option-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin: 0 0 4px 0;
}

.option-desc {
  font-size: 12px;
  color: #666;
  margin: 0;
  line-height: 1.4;
}

.option-votes {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  min-width: 80px;
}

.vote-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
  font-weight: bold;
}

.vote-progress {
  width: 60px;
  height: 4px;
  background-color: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #66b3ff);
  transition: width 0.3s ease;
}

.vote-percentage {
  font-size: 12px;
  color: #666;
}

.vote-action {
  position: absolute;
  top: 50%;
  right: 16px;
  transform: translateY(-50%);
}

.voted-mark {
  position: absolute;
  top: 50%;
  right: 16px;
  transform: translateY(-50%);
  color: #52c41a;
}

.check-icon {
  font-size: 20px;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .vote-activities-page {
    padding: 16px 12px 80px;
  }
  
  .activity-card {
    padding: 16px;
  }
  
  .option-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .option-votes {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
  
  .vote-progress {
    flex: 1;
    margin: 0 12px;
  }
  
  .vote-action,
  .voted-mark {
    position: static;
    transform: none;
    margin-top: 12px;
  }
}
</style> 