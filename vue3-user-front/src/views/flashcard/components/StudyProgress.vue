<template>
  <div class="study-progress">
    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
    </div>
    <div class="progress-info">
      <div class="progress-left">
        <span class="current">{{ current }}</span>
        <span class="separator">/</span>
        <span class="total">{{ total }}</span>
      </div>
      <div class="progress-right">
        <div class="stat-item new" v-if="newCount > 0">
          <span class="dot"></span>
          <span>新卡 {{ newCount }}</span>
        </div>
        <div class="stat-item due" v-if="dueCount > 0">
          <span class="dot"></span>
          <span>复习 {{ dueCount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  current: {
    type: Number,
    default: 0
  },
  total: {
    type: Number,
    default: 0
  },
  newCount: {
    type: Number,
    default: 0
  },
  dueCount: {
    type: Number,
    default: 0
  }
})

const progressPercent = computed(() => {
  if (props.total === 0) return 0
  return Math.round(props.current / props.total * 100)
})
</script>

<style lang="scss" scoped>
.study-progress {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 16px 20px;
  border: 1px solid var(--el-border-color-light);
}

.progress-bar {
  height: 8px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
  
  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, var(--el-color-primary), var(--el-color-success));
    border-radius: 4px;
    transition: width 0.3s ease;
  }
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-left {
  font-size: 16px;
  color: var(--el-text-color-primary);
  
  .current {
    font-weight: 700;
    color: var(--el-color-primary);
  }
  
  .separator {
    margin: 0 4px;
    color: var(--el-text-color-secondary);
  }
  
  .total {
    color: var(--el-text-color-secondary);
  }
}

.progress-right {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  
  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
  
  &.new {
    color: var(--el-color-success);
    .dot {
      background: var(--el-color-success);
    }
  }
  
  &.due {
    color: var(--el-color-warning);
    .dot {
      background: var(--el-color-warning);
    }
  }
}
</style>
