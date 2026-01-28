<template>
  <div class="study-stats">
    <div class="stats-header">
      <el-icon><DataLine /></el-icon>
      <span>学习统计</span>
    </div>
    
    <div class="stats-grid">
      <div class="stat-card today">
        <div class="stat-icon">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats?.todayLearnedCount || 0 }}</div>
          <div class="stat-label">今日已学</div>
        </div>
      </div>
      
      <div class="stat-card due">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats?.todayDueCount || 0 }}</div>
          <div class="stat-label">待复习</div>
        </div>
      </div>
      
      <div class="stat-card new">
        <div class="stat-icon">
          <el-icon><Plus /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats?.todayNewCount || 0 }}</div>
          <div class="stat-label">新卡片</div>
        </div>
      </div>
      
      <div class="stat-card streak">
        <div class="stat-icon">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats?.streakDays || 0 }}</div>
          <div class="stat-label">连续天数</div>
        </div>
      </div>
    </div>
    
    <div class="mastery-section" v-if="stats">
      <div class="section-title">掌握程度</div>
      <div class="mastery-bar">
        <div 
          class="mastery-segment mastered" 
          :style="{ width: masteredPercent + '%' }"
          v-if="masteredPercent > 0"
        >
          <span v-if="masteredPercent > 15">{{ stats.masteredCount }}</span>
        </div>
        <div 
          class="mastery-segment learning" 
          :style="{ width: learningPercent + '%' }"
          v-if="learningPercent > 0"
        >
          <span v-if="learningPercent > 15">{{ stats.learningCount }}</span>
        </div>
        <div 
          class="mastery-segment new" 
          :style="{ width: newPercent + '%' }"
          v-if="newPercent > 0"
        >
          <span v-if="newPercent > 15">{{ stats.newCount }}</span>
        </div>
      </div>
      <div class="mastery-legend">
        <div class="legend-item">
          <span class="dot mastered"></span>
          <span>已掌握 {{ stats.masteredCount || 0 }}</span>
        </div>
        <div class="legend-item">
          <span class="dot learning"></span>
          <span>学习中 {{ stats.learningCount || 0 }}</span>
        </div>
        <div class="legend-item">
          <span class="dot new"></span>
          <span>新学 {{ stats.newCount || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { DataLine, Calendar, Clock, Plus, TrendCharts } from '@element-plus/icons-vue'

const props = defineProps({
  stats: {
    type: Object,
    default: null
  }
})

const total = computed(() => {
  if (!props.stats) return 0
  return (props.stats.masteredCount || 0) + 
         (props.stats.learningCount || 0) + 
         (props.stats.newCount || 0)
})

const masteredPercent = computed(() => {
  if (total.value === 0) return 0
  return Math.round((props.stats?.masteredCount || 0) / total.value * 100)
})

const learningPercent = computed(() => {
  if (total.value === 0) return 0
  return Math.round((props.stats?.learningCount || 0) / total.value * 100)
})

const newPercent = computed(() => {
  if (total.value === 0) return 0
  return 100 - masteredPercent.value - learningPercent.value
})
</script>

<style lang="scss" scoped>
.study-stats {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--el-border-color-light);
}

.stats-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  
  .stat-icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
  }
  
  .stat-content {
    .stat-value {
      font-size: 20px;
      font-weight: 700;
      line-height: 1.2;
    }
    
    .stat-label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
  
  &.today {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
    .stat-icon {
      background: rgba(64, 158, 255, 0.2);
      color: var(--el-color-primary);
    }
    .stat-value {
      color: var(--el-color-primary);
    }
  }
  
  &.due {
    background: linear-gradient(135deg, rgba(230, 162, 60, 0.1) 0%, rgba(230, 162, 60, 0.05) 100%);
    .stat-icon {
      background: rgba(230, 162, 60, 0.2);
      color: var(--el-color-warning);
    }
    .stat-value {
      color: var(--el-color-warning);
    }
  }
  
  &.new {
    background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(103, 194, 58, 0.05) 100%);
    .stat-icon {
      background: rgba(103, 194, 58, 0.2);
      color: var(--el-color-success);
    }
    .stat-value {
      color: var(--el-color-success);
    }
  }
  
  &.streak {
    background: linear-gradient(135deg, rgba(245, 108, 108, 0.1) 0%, rgba(245, 108, 108, 0.05) 100%);
    .stat-icon {
      background: rgba(245, 108, 108, 0.2);
      color: var(--el-color-danger);
    }
    .stat-value {
      color: var(--el-color-danger);
    }
  }
}

.mastery-section {
  .section-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    margin-bottom: 12px;
  }
}

.mastery-bar {
  display: flex;
  height: 24px;
  border-radius: 12px;
  overflow: hidden;
  background: var(--el-fill-color-light);
  
  .mastery-segment {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 12px;
    font-weight: 500;
    transition: width 0.3s ease;
    
    &.mastered {
      background: #67c23a;
    }
    
    &.learning {
      background: #e6a23c;
    }
    
    &.new {
      background: #909399;
    }
  }
}

.mastery-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 12px;
  
  .legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    
    .dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      
      &.mastered {
        background: #67c23a;
      }
      
      &.learning {
        background: #e6a23c;
      }
      
      &.new {
        background: #909399;
      }
    }
  }
}
</style>
