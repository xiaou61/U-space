<template>
  <div v-if="modelValue" class="dialog-overlay" @click="closeDialog">
    <div class="dialog-content" @click.stop>
      <div class="dialog-header">
        <h3>积分明细</h3>
        <button class="close-btn" @click="closeDialog">✕</button>
      </div>
      
      <!-- 筛选条件 -->
      <div class="filter-section">
        <div class="filter-row">
          <select v-model="queryForm.pointsType" @change="loadDetailList">
            <option value="">全部类型</option>
            <option value="CHECK_IN">每日打卡</option>
            <option value="ADMIN_GRANT">管理员发放</option>
          </select>
          
          <input 
            type="date" 
            v-model="queryForm.startTime" 
            @change="loadDetailList"
            placeholder="开始日期"
          >
          
          <input 
            type="date" 
            v-model="queryForm.endTime" 
            @change="loadDetailList"
            placeholder="结束日期"
          >
        </div>
      </div>
      
      <div class="dialog-body">
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner">⏳</div>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="detailList.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <p>暂无积分明细记录</p>
        </div>
        
        <div v-else class="detail-list">
          <div 
            v-for="detail in detailList" 
            :key="detail.id"
            class="detail-item"
          >
            <div class="detail-left">
              <div class="detail-type">{{ getPointsTypeText(detail.pointsType) }}</div>
              <div class="detail-desc">{{ detail.description }}</div>
              <div class="detail-time">{{ formatDateTime(detail.createTime) }}</div>
              <div v-if="detail.continuousDays > 0" class="continuous-days">
                连续打卡 {{ detail.continuousDays }} 天
              </div>
            </div>
            <div class="detail-right">
              <div class="points-change" :class="{ 'positive': detail.pointsChange > 0 }">
                {{ detail.pointsChange > 0 ? '+' : '' }}{{ detail.pointsChange }}
              </div>
              <div class="balance-after">余额: {{ detail.balanceAfter }}</div>
            </div>
          </div>
        </div>
        
        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination">
          <button 
            class="page-btn" 
            :disabled="currentPage === 1"
            @click="changePage(currentPage - 1)"
          >
            上一页
          </button>
          
          <span class="page-info">
            {{ currentPage }} / {{ totalPages }}
          </span>
          
          <button 
            class="page-btn" 
            :disabled="currentPage === totalPages"
            @click="changePage(currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import pointsApi from '@/api/points'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

// 响应式数据
const detailList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const queryForm = ref({
  pointsType: '',
  startTime: '',
  endTime: ''
})

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 监听弹窗打开
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    loadDetailList()
  }
})

// 加载积分明细列表
const loadDetailList = async () => {
  loading.value = true
  
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...queryForm.value
    }
    
    const response = await pointsApi.getPointsDetailList(params)
    detailList.value = response.records || []
    total.value = response.total || 0
    
  } catch (error) {
    console.error('加载积分明细失败:', error)
    detailList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 切换页码
const changePage = (page) => {
  currentPage.value = page
  loadDetailList()
}

// 关闭弹窗
const closeDialog = () => {
  emit('update:modelValue', false)
  // 重置数据
  currentPage.value = 1
  queryForm.value = {
    pointsType: '',
    startTime: '',
    endTime: ''
  }
}

// 获取积分类型文本
const getPointsTypeText = (type) => {
  const typeMap = {
    'CHECK_IN': '每日打卡',
    'ADMIN_GRANT': '管理员发放'
  }
  return typeMap[type] || type
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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

.filter-section {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.filter-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
  
  select, input {
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 14px;
    background: white;
    
    &:focus {
      outline: none;
      border-color: #667eea;
    }
  }
}

.dialog-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.loading-state, .empty-state {
  flex: 1;
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
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
    opacity: 0.5;
  }
  
  p {
    margin: 0;
    font-size: 14px;
  }
}

.detail-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

.detail-left {
  flex: 1;
  
  .detail-type {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    margin-bottom: 4px;
  }
  
  .detail-desc {
    font-size: 13px;
    color: #666;
    margin-bottom: 4px;
  }
  
  .detail-time {
    font-size: 12px;
    color: #999;
    margin-bottom: 4px;
  }
  
  .continuous-days {
    font-size: 12px;
    color: #67c23a;
    background: rgba(103, 194, 58, 0.1);
    padding: 2px 6px;
    border-radius: 4px;
    display: inline-block;
  }
}

.detail-right {
  text-align: right;
  margin-left: 16px;
  
  .points-change {
    font-size: 16px;
    font-weight: 600;
    color: #f56c6c;
    margin-bottom: 4px;
    
    &.positive {
      color: #67c23a;
    }
  }
  
  .balance-after {
    font-size: 12px;
    color: #999;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  
  .page-btn {
    padding: 8px 16px;
    border: 1px solid #ddd;
    border-radius: 6px;
    background: white;
    color: #333;
    cursor: pointer;
    font-size: 14px;
    
    &:hover:not(:disabled) {
      background: #f5f5f5;
    }
    
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
  
  .page-info {
    font-size: 14px;
    color: #666;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
