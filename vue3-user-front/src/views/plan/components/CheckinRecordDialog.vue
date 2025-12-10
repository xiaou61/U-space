<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    :title="`${planName} - 打卡记录`"
    width="650px"
    :close-on-click-modal="true"
  >
    <div class="record-content">
      <!-- 统计摘要 -->
      <div class="stats-summary">
        <div class="stat-card">
          <div class="stat-value">{{ totalRecords }}</div>
          <div class="stat-label">累计打卡</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ completionRate }}%</div>
          <div class="stat-label">达标率</div>
        </div>
      </div>

      <!-- 记录列表 -->
      <div class="record-list" v-loading="loading">
        <div v-if="records.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无打卡记录" />
        </div>
        
        <div 
          v-for="record in records" 
          :key="record.id"
          class="record-item"
        >
          <div class="record-date">
            <div class="date-day">{{ formatDay(record.checkinDate) }}</div>
            <div class="date-weekday">{{ formatWeekday(record.checkinDate) }}</div>
          </div>
          
          <div class="record-main">
            <div class="record-value">
              <span class="value">{{ record.actualValue }}</span>
              <span class="target">/ {{ record.targetValue }} {{ targetUnit }}</span>
              <el-tag 
                size="small" 
                :type="record.completionRate >= 100 ? 'success' : 'warning'"
              >
                {{ record.completionRate }}%
              </el-tag>
            </div>
            <div class="record-remark" v-if="record.remark">
              "{{ record.remark }}"
            </div>
            <div class="record-time">
              {{ formatTime(record.checkinTime) }}
            </div>
          </div>

          <div class="record-status">
            <el-icon v-if="record.completionRate >= 100" class="success"><SuccessFilled /></el-icon>
            <el-icon v-else class="partial"><WarningFilled /></el-icon>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div class="load-more" v-if="hasMore">
        <el-button @click="loadMore" :loading="loading" text>
          加载更多
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { SuccessFilled, WarningFilled } from '@element-plus/icons-vue'
import planApi from '@/api/plan'

const props = defineProps({
  modelValue: Boolean,
  planId: Number,
  planName: String
})

const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const records = ref([])
const targetUnit = ref('')

// 计算属性
const totalRecords = computed(() => records.value.length)
const hasMore = computed(() => false) // 后端返回全部记录，不需要分页
const completionRate = computed(() => {
  if (records.value.length === 0) return 0
  const completed = records.value.filter(r => r.completionRate >= 100).length
  return Math.round((completed / records.value.length) * 100)
})

// 监听弹窗打开
watch(() => props.modelValue, (val) => {
  if (val && props.planId) {
    records.value = []
    loadRecords()
  }
})

// 加载打卡记录
const loadRecords = async () => {
  loading.value = true
  try {
    const response = await planApi.getCheckinRecords(props.planId)
    records.value = response || []
    
    // 获取目标单位
    if (records.value.length > 0 && !targetUnit.value) {
      targetUnit.value = records.value[0].targetUnit || ''
    }
  } catch (error) {
    console.error('加载打卡记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载更多（保留以防模板报错）
const loadMore = () => {
  // 后端返回全部记录，不需要分页
}

// 格式化日期 - 日
const formatDay = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.getDate()
}

// 格式化日期 - 星期
const formatWeekday = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const month = date.getMonth() + 1
  return `${month}月 周${weekdays[date.getDay()]}`
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}
</script>

<style lang="scss" scoped>
.record-content {
  min-height: 300px;
}

.stats-summary {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  background: #409eff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  color: white;
  
  .stat-value {
    font-size: 28px;
    font-weight: bold;
  }
  
  .stat-label {
    font-size: 13px;
    opacity: 0.9;
    margin-top: 4px;
  }
}

.record-list {
  max-height: 400px;
  overflow-y: auto;
}

.empty-state {
  padding: 40px 0;
}

.record-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 12px;
  margin-bottom: 12px;
  transition: all 0.3s;
  
  &:hover {
    background: #f0f2f8;
  }
  
  &:last-child {
    margin-bottom: 0;
  }
}

.record-date {
  width: 60px;
  text-align: center;
  
  .date-day {
    font-size: 24px;
    font-weight: bold;
    color: #409eff;
    line-height: 1;
  }
  
  .date-weekday {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}

.record-main {
  flex: 1;
  
  .record-value {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
    
    .value {
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }
    
    .target {
      font-size: 14px;
      color: #999;
    }
  }
  
  .record-remark {
    font-size: 13px;
    color: #666;
    font-style: italic;
    margin-bottom: 4px;
    line-height: 1.4;
  }
  
  .record-time {
    font-size: 12px;
    color: #999;
  }
}

.record-status {
  .el-icon {
    font-size: 24px;
    
    &.success {
      color: #67c23a;
    }
    
    &.partial {
      color: #e6a23c;
    }
  }
}

.load-more {
  text-align: center;
  padding: 16px 0 8px;
}
</style>
