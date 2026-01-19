<template>
  <div class="team-stats">
    <!-- 统计周期选择 -->
    <div class="stats-header">
      <div class="stats-tabs">
        <el-button-group>
          <el-button 
            :type="period === 'weekly' ? 'primary' : 'default'"
            @click="period = 'weekly'"
          >
            本周
          </el-button>
          <el-button 
            :type="period === 'monthly' ? 'primary' : 'default'"
            @click="period = 'monthly'"
          >
            本月
          </el-button>
        </el-button-group>
      </div>
    </div>
    
    <!-- 统计数据 -->
    <div class="stats-content" v-loading="loading">
      <!-- 概览卡片 -->
      <div class="stats-overview">
        <div class="stat-card">
          <div class="stat-icon checkin">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.checkinCount || 0 }}</div>
            <div class="stat-label">总打卡次数</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon duration">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ formatDuration(stats.totalDuration) }}</div>
            <div class="stat-label">总学习时长</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon active">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeMembers || 0 }}</div>
            <div class="stat-label">活跃成员</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon rate">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ formatRate(stats.completionRate) }}</div>
            <div class="stat-label">完成率</div>
          </div>
        </div>
      </div>
      
      <!-- 每日数据 -->
      <div class="daily-stats" v-if="dailyData.length > 0">
        <h3>每日数据</h3>
        <div class="daily-chart">
          <div 
            v-for="(day, index) in dailyData" 
            :key="index"
            class="day-bar"
            :style="{ height: getBarHeight(day.count) + '%' }"
            :title="`${day.date}: ${day.count}次打卡`"
          >
            <span class="day-count" v-if="day.count > 0">{{ day.count }}</span>
          </div>
        </div>
        <div class="daily-labels">
          <span v-for="(day, index) in dailyData" :key="index">
            {{ formatDayLabel(day.date, index) }}
          </span>
        </div>
      </div>
      
      <!-- 成员贡献排名 -->
      <div class="contribution-rank" v-if="topMembers.length > 0">
        <h3>贡献排名</h3>
        <div class="rank-list">
          <div 
            v-for="(member, index) in topMembers" 
            :key="member.userId"
            class="rank-item"
          >
            <span class="rank-pos">{{ index + 1 }}</span>
            <div class="member-avatar">
              <img v-if="member.userAvatar" :src="member.userAvatar" />
              <span v-else>{{ member.userName?.charAt(0) || '用' }}</span>
            </div>
            <span class="member-name">{{ member.userName }}</span>
            <span class="member-count">{{ member.checkinCount }}次</span>
            <div class="progress-bar">
              <div 
                class="progress-fill"
                :style="{ width: getProgressWidth(member.checkinCount) + '%' }"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { Check, Timer, User, TrendCharts } from '@element-plus/icons-vue'
import teamApi from '@/api/team'

const props = defineProps({
  teamId: { type: [String, Number], required: true }
})

const period = ref('weekly')
const loading = ref(false)
const stats = ref({})
const dailyData = ref([])
const topMembers = ref([])

const maxCount = computed(() => {
  if (topMembers.value.length === 0) return 1
  return Math.max(...topMembers.value.map(m => m.checkinCount || 0), 1)
})

const maxDaily = computed(() => {
  if (dailyData.value.length === 0) return 1
  return Math.max(...dailyData.value.map(d => d.count || 0), 1)
})

onMounted(() => {
  loadStats()
})

watch(period, () => {
  loadStats()
})

watch(() => props.teamId, () => {
  loadStats()
})

const loadStats = async () => {
  loading.value = true
  try {
    let response
    if (period.value === 'weekly') {
      response = await teamApi.getWeeklyStats(props.teamId)
    } else {
      response = await teamApi.getMonthlyStats(props.teamId)
    }
    
    stats.value = response || {}
    dailyData.value = response?.dailyData || []
    topMembers.value = response?.topMembers || []
  } catch (error) {
    console.error('加载统计数据失败:', error)
    stats.value = {}
    dailyData.value = []
    topMembers.value = []
  } finally {
    loading.value = false
  }
}

const formatDuration = (minutes) => {
  if (!minutes) return '0h'
  const hours = Math.floor(minutes / 60)
  return hours > 0 ? `${hours}h` : `${minutes}min`
}

const formatRate = (rate) => {
  if (!rate) return '0%'
  return `${Math.round(rate * 100)}%`
}

const getBarHeight = (count) => {
  return Math.max((count / maxDaily.value) * 100, 5)
}

const getProgressWidth = (count) => {
  return (count / maxCount.value) * 100
}

const formatDayLabel = (date, index) => {
  if (!date) return ''
  const d = new Date(date)
  // 只显示部分标签
  if (dailyData.value.length <= 7 || index % 2 === 0) {
    return d.getDate()
  }
  return ''
}
</script>

<style lang="scss" scoped>
.team-stats {
  min-height: 300px;
}

.stats-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  
  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 12px;
  
  .stat-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      font-size: 22px;
      color: white;
    }
    
    &.checkin { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    &.duration { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
    &.active { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
    &.rate { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
  }
  
  .stat-info {
    .stat-value {
      font-size: 20px;
      font-weight: bold;
      color: #333;
    }
    
    .stat-label {
      font-size: 13px;
      color: #999;
      margin-top: 2px;
    }
  }
}

.daily-stats {
  margin-bottom: 24px;
  
  h3 {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin: 0 0 16px 0;
  }
}

.daily-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 120px;
  padding: 0 8px;
  background: #f8f9fc;
  border-radius: 8px;
  
  .day-bar {
    flex: 1;
    max-width: 30px;
    margin: 0 2px;
    background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
    border-radius: 4px 4px 0 0;
    position: relative;
    min-height: 4px;
    transition: height 0.3s;
    
    .day-count {
      position: absolute;
      top: -20px;
      left: 50%;
      transform: translateX(-50%);
      font-size: 11px;
      color: #666;
    }
  }
}

.daily-labels {
  display: flex;
  justify-content: space-between;
  padding: 8px 8px 0;
  
  span {
    flex: 1;
    max-width: 30px;
    text-align: center;
    font-size: 11px;
    color: #999;
  }
}

.contribution-rank {
  h3 {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin: 0 0 16px 0;
  }
}

.rank-list {
  .rank-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;
    
    & + .rank-item {
      border-top: 1px solid #f0f0f0;
    }
  }
  
  .rank-pos {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f0f0f0;
    border-radius: 50%;
    font-size: 12px;
    font-weight: 500;
    color: #666;
  }
  
  .rank-item:nth-child(1) .rank-pos {
    background: #ffd700;
    color: white;
  }
  
  .rank-item:nth-child(2) .rank-pos {
    background: #c0c0c0;
    color: white;
  }
  
  .rank-item:nth-child(3) .rank-pos {
    background: #cd7f32;
    color: white;
  }
  
  .member-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    overflow: hidden;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    flex-shrink: 0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    span {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      color: white;
      font-size: 14px;
    }
  }
  
  .member-name {
    flex: 1;
    font-size: 14px;
    color: #333;
    min-width: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .member-count {
    font-size: 13px;
    color: #999;
    width: 50px;
    text-align: right;
  }
  
  .progress-bar {
    width: 100px;
    height: 6px;
    background: #f0f0f0;
    border-radius: 3px;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
      border-radius: 3px;
      transition: width 0.3s;
    }
  }
}
</style>
