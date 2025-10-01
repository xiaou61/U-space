<template>
  <div v-if="modelValue" class="dialog-overlay" @click="closeDialog">
    <div class="dialog-content" @click.stop>
      <div class="dialog-header">
        <h3>打卡统计</h3>
        <button class="close-btn" @click="closeDialog">✕</button>
      </div>
      
      <div class="dialog-body">
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner">⏳</div>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="statisticsData" class="statistics-container">
          <!-- 总体统计 -->
          <div class="overview-stats">
            <div class="stat-card primary">
              <div class="stat-icon">🔥</div>
              <div class="stat-content">
                <div class="stat-number">{{ statisticsData.continuousDays || 0 }}</div>
                <div class="stat-label">连续打卡天数</div>
              </div>
            </div>
            
            <div class="stat-card success">
              <div class="stat-icon">📅</div>
              <div class="stat-content">
                <div class="stat-number">{{ statisticsData.totalCheckinDays || 0 }}</div>
                <div class="stat-label">累计打卡天数</div>
              </div>
            </div>
            
            <div class="stat-card warning">
              <div class="stat-icon">💰</div>
              <div class="stat-content">
                <div class="stat-number">{{ statisticsData.totalPointsEarned || 0 }}</div>
                <div class="stat-label">打卡获得积分</div>
              </div>
            </div>
          </div>
          
          <!-- 月度统计 -->
          <div class="monthly-stats">
            <h4>最近几个月打卡情况</h4>
            
            <div class="monthly-list">
              <div 
                v-for="month in statisticsData.monthlyStats || []" 
                :key="month.yearMonth"
                class="month-item"
              >
                <div class="month-header">
                  <span class="month-name">{{ formatMonth(month.yearMonth) }}</span>
                  <span class="month-days">{{ month.checkinDays }}/{{ month.totalDays }} 天</span>
                </div>
                
                <div class="month-progress">
                  <div class="progress-bar">
                    <div 
                      class="progress-fill" 
                      :style="{ width: getProgressPercent(month.checkinDays, month.totalDays) + '%' }"
                    ></div>
                  </div>
                  <span class="progress-percent">
                    {{ getProgressPercent(month.checkinDays, month.totalDays) }}%
                  </span>
                </div>
                
                <div class="month-points">
                  本月打卡获得 <span class="points-earned">{{ month.pointsEarned || 0 }}</span> 积分
                </div>
              </div>
            </div>
          </div>
          
          <!-- 成就展示 -->
          <div class="achievements">
            <h4>打卡成就</h4>
            
            <div class="achievement-list">
              <div 
                v-for="achievement in getAchievements()"
                :key="achievement.id"
                class="achievement-item"
                :class="{ 'achieved': achievement.achieved }"
              >
                <div class="achievement-icon">{{ achievement.icon }}</div>
                <div class="achievement-content">
                  <div class="achievement-name">{{ achievement.name }}</div>
                  <div class="achievement-desc">{{ achievement.description }}</div>
                </div>
                <div class="achievement-status">
                  {{ achievement.achieved ? '✅' : '🔒' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import pointsApi from '@/api/points'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

// 响应式数据
const statisticsData = ref(null)
const loading = ref(false)

// 监听弹窗打开
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    loadStatisticsData()
  }
})

// 加载打卡统计数据
const loadStatisticsData = async () => {
  loading.value = true
  
  try {
    const response = await pointsApi.getCheckinStatistics(3) // 最近3个月
    statisticsData.value = response
  } catch (error) {
    console.error('加载打卡统计失败:', error)
    statisticsData.value = null
  } finally {
    loading.value = false
  }
}

// 关闭弹窗
const closeDialog = () => {
  emit('update:modelValue', false)
}

// 格式化月份
const formatMonth = (yearMonth) => {
  if (!yearMonth) return ''
  const [year, month] = yearMonth.split('-')
  return `${year}年${parseInt(month)}月`
}

// 计算进度百分比
const getProgressPercent = (checkinDays, totalDays) => {
  if (!totalDays || totalDays === 0) return 0
  return Math.round((checkinDays / totalDays) * 100)
}

// 获取成就列表
const getAchievements = () => {
  if (!statisticsData.value) return []
  
  const { continuousDays, totalCheckinDays } = statisticsData.value
  
  return [
    {
      id: 'first_checkin',
      name: '初来乍到',
      description: '完成第一次打卡',
      icon: '🎯',
      achieved: totalCheckinDays >= 1
    },
    {
      id: 'week_warrior',
      name: '一周打卡王',
      description: '连续打卡7天',
      icon: '⭐',
      achieved: continuousDays >= 7
    },
    {
      id: 'month_master',
      name: '月度大师',
      description: '连续打卡30天',
      icon: '👑',
      achieved: continuousDays >= 30
    },
    {
      id: 'century_club',
      name: '百日俱乐部',
      description: '累计打卡100天',
      icon: '💎',
      achieved: totalCheckinDays >= 100
    },
    {
      id: 'persistent_pro',
      name: '坚持达人',
      description: '连续打卡100天',
      icon: '🚀',
      achieved: continuousDays >= 100
    },
    {
      id: 'year_legend',
      name: '年度传奇',
      description: '累计打卡365天',
      icon: '🏆',
      achieved: totalCheckinDays >= 365
    }
  ]
}
</script>

<style lang="scss" scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.dialog-content {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 500px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  
  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }
  
  .close-btn {
    background: none;
    border: none;
    font-size: 20px;
    color: #999;
    cursor: pointer;
    padding: 4px;
    border-radius: 4px;
    
    &:hover {
      background: #f5f5f5;
      color: #666;
    }
  }
}

.dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #999;
  
  .loading-spinner {
    font-size: 32px;
    margin-bottom: 12px;
    animation: spin 1s linear infinite;
  }
  
  p {
    margin: 0;
    font-size: 14px;
  }
}

.statistics-container {
  .overview-stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 24px;
    
    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      
      &.primary {
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: white;
      }
      
      &.success {
        background: linear-gradient(135deg, #67c23a, #85ce61);
        color: white;
      }
      
      &.warning {
        background: linear-gradient(135deg, #e6a23c, #f56c6c);
        color: white;
      }
      
      .stat-icon {
        font-size: 24px;
        opacity: 0.9;
      }
      
      .stat-content {
        flex: 1;
        
        .stat-number {
          font-size: 20px;
          font-weight: bold;
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 12px;
          opacity: 0.9;
        }
      }
    }
  }
  
  .monthly-stats, .achievements {
    margin-bottom: 24px;
    
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin: 0 0 16px 0;
    }
  }
  
  .monthly-list {
    .month-item {
      background: #f8f9fa;
      border-radius: 12px;
      padding: 16px;
      margin-bottom: 12px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .month-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        .month-name {
          font-size: 14px;
          font-weight: 600;
          color: #333;
        }
        
        .month-days {
          font-size: 14px;
          color: #666;
        }
      }
      
      .month-progress {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 8px;
        
        .progress-bar {
          flex: 1;
          height: 8px;
          background: #e9ecef;
          border-radius: 4px;
          overflow: hidden;
          
          .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #67c23a, #85ce61);
            transition: width 0.3s ease;
          }
        }
        
        .progress-percent {
          font-size: 12px;
          color: #666;
          font-weight: 500;
          min-width: 40px;
        }
      }
      
      .month-points {
        font-size: 12px;
        color: #666;
        
        .points-earned {
          color: #67c23a;
          font-weight: 600;
        }
      }
    }
  }
  
  .achievement-list {
    .achievement-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      border-radius: 8px;
      margin-bottom: 8px;
      background: #f8f9fa;
      transition: all 0.3s ease;
      
      &.achieved {
        background: rgba(103, 194, 58, 0.1);
        border: 1px solid rgba(103, 194, 58, 0.2);
      }
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .achievement-icon {
        font-size: 24px;
        opacity: 0.7;
      }
      
      .achievement-content {
        flex: 1;
        
        .achievement-name {
          font-size: 14px;
          font-weight: 600;
          color: #333;
          margin-bottom: 2px;
        }
        
        .achievement-desc {
          font-size: 12px;
          color: #666;
        }
      }
      
      .achievement-status {
        font-size: 16px;
      }
      
      &.achieved {
        .achievement-icon {
          opacity: 1;
        }
        
        .achievement-name {
          color: #67c23a;
        }
      }
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
