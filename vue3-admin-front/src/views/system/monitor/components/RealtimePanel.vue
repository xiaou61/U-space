<template>
  <div class="realtime-panel">
    <!-- 控制面板 -->
    <el-card class="control-card" shadow="never">
      <div class="control-header">
        <div class="status-info">
          <el-tag :type="autoRefresh ? 'success' : 'info'" size="large">
            <el-icon><VideoPlay v-if="autoRefresh" /><VideoPause v-else /></el-icon>
            {{ autoRefresh ? '实时监控中' : '监控已暂停' }}
          </el-tag>
          <span class="last-update">最后更新: {{ lastUpdateTime }}</span>
        </div>
        
        <div class="control-actions">
          <el-input 
            v-model="traceIdFilter"
            placeholder="输入跟踪ID进行过滤"
            clearable
            style="width: 200px; margin-right: 10px"
            @change="fetchRealtimeData"
          />
          
          <el-button 
            :type="autoRefresh ? 'danger' : 'success'"
            @click="toggleAutoRefresh"
          >
            <el-icon><VideoPlay v-if="!autoRefresh" /><VideoPause v-else /></el-icon>
            {{ autoRefresh ? '暂停' : '开始' }}
          </el-button>
          
          <el-button @click="clearData">
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
          
          <el-dropdown @command="handleRefreshInterval">
            <el-button>
              刷新间隔: {{ refreshInterval / 1000 }}s
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="1000">1秒</el-dropdown-item>
                <el-dropdown-item command="3000">3秒</el-dropdown-item>
                <el-dropdown-item command="5000">5秒</el-dropdown-item>
                <el-dropdown-item command="10000">10秒</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-card>

    <!-- 实时统计 -->
    <div class="realtime-stats">
      <el-card class="stat-item">
        <div class="stat-content">
          <div class="stat-number">{{ realtimeData.length }}</div>
          <div class="stat-label">当前SQL数量</div>
        </div>
        <el-icon class="stat-icon"><DataLine /></el-icon>
      </el-card>
      
      <el-card class="stat-item">
        <div class="stat-content">
          <div class="stat-number">{{ currentSlowSqlCount }}</div>
          <div class="stat-label">慢SQL数量</div>
        </div>
        <el-icon class="stat-icon slow"><Warning /></el-icon>
      </el-card>
      
      <el-card class="stat-item">
        <div class="stat-content">
          <div class="stat-number">{{ currentAvgTime }}ms</div>
          <div class="stat-label">平均执行时间</div>
        </div>
        <el-icon class="stat-icon"><Timer /></el-icon>
      </el-card>
      
      <el-card class="stat-item">
        <div class="stat-content">
          <div class="stat-number">{{ currentErrorCount }}</div>
          <div class="stat-label">错误数量</div>
        </div>
        <el-icon class="stat-icon error"><CircleCloseFilled /></el-icon>
      </el-card>
    </div>

    <!-- 实时SQL列表 -->
    <el-card class="realtime-table" shadow="never">
      <template #header>
        <div class="table-header">
          <span>实时SQL执行记录</span>
          <div class="table-actions">
            <el-switch
              v-model="showOnlySlowSql"
              active-text="只显示慢SQL"
              @change="filterData"
            />
            <el-switch
              v-model="autoScroll"
              active-text="自动滚动"
              style="margin-left: 15px"
            />
          </div>
        </div>
      </template>
      
      <div class="table-container" ref="tableContainerRef">
        <el-table 
          :data="filteredData" 
          :loading="loading"
          stripe
          size="small"
          style="width: 100%"
          @row-click="handleRowClick"
          :max-height="400"
        >
          <el-table-column prop="executeTime" label="执行时间" width="180" fixed="left">
            <template #default="{ row }">
              <span class="time-stamp">{{ formatTime(row.executeTime) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="traceId" label="跟踪ID" width="120" show-overflow-tooltip />
          
          <el-table-column prop="username" label="用户" width="100" />
          
          <el-table-column prop="sqlType" label="类型" width="70" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getSqlTypeTagType(row.sqlType)"
                size="small"
              >
                {{ row.sqlType }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="executionTime" label="耗时(ms)" width="90" align="center">
            <template #default="{ row }">
              <span :class="getExecutionTimeClass(row.executionTime)">
                {{ row.executionTime }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column prop="affectedRows" label="影响行数" width="80" align="center" />
          
          <el-table-column prop="success" label="状态" width="70" align="center">
            <template #default="{ row }">
              <el-icon 
                :class="row.success ? 'success-icon' : 'error-icon'"
                :title="row.success ? '成功' : '失败'"
              >
                <CircleCheckFilled v-if="row.success" />
                <CircleCloseFilled v-else />
              </el-icon>
            </template>
          </el-table-column>
          
          <el-table-column label="标识" width="100" align="center">
            <template #default="{ row }">
              <div class="status-indicators">
                <el-tag v-if="row.slowSql" type="warning" size="small">慢</el-tag>
                <el-tag v-if="!row.success" type="danger" size="small">错</el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="mapperMethod" label="Mapper方法" min-width="200" show-overflow-tooltip />
          
          <el-table-column label="SQL预览" min-width="300">
            <template #default="{ row }">
              <div class="sql-preview" @click.stop="showSqlDetail(row)">
                {{ formatSqlPreview(row.sqlStatement) }}
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- SQL详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible"
      title="SQL执行详情"
      width="80%"
      destroy-on-close
    >
      <SqlDetailDialog 
        v-if="detailDialogVisible"
        :sql-log="selectedSqlLog"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  VideoPlay, VideoPause, Delete, ArrowDown, DataLine,
  Warning, Timer, CircleCloseFilled, CircleCheckFilled
} from '@element-plus/icons-vue'
import { monitorApi } from '@/api/monitor'
import SqlDetailDialog from './SqlDetailDialog.vue'

// 响应式数据
const loading = ref(false)
const realtimeData = ref([])
const filteredData = ref([])
const autoRefresh = ref(false)
const autoScroll = ref(true)
const showOnlySlowSql = ref(false)
const refreshInterval = ref(3000)
const lastUpdateTime = ref('')
const traceIdFilter = ref('')
const detailDialogVisible = ref(false)
const selectedSqlLog = ref(null)

// DOM引用
const tableContainerRef = ref(null)

// 定时器
let refreshTimer = null

// 计算属性
const currentSlowSqlCount = computed(() => {
  return realtimeData.value.filter(item => item.slowSql).length
})

const currentErrorCount = computed(() => {
  return realtimeData.value.filter(item => !item.success).length
})

const currentAvgTime = computed(() => {
  if (realtimeData.value.length === 0) return 0
  const total = realtimeData.value.reduce((sum, item) => sum + item.executionTime, 0)
  return Math.round(total / realtimeData.value.length)
})

// 获取实时数据
const fetchRealtimeData = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    const params = traceIdFilter.value ? { traceId: traceIdFilter.value } : {}
    const response = await monitorApi.getRealtimeData(params)
    
    // 安全的数据访问，防止undefined错误
    let newData = []
    if (response && typeof response === 'object') {
      newData = Array.isArray(response) ? response : (response.records || [])
    } else {
      console.warn('实时监控接口返回数据格式异常:', response)
    }
    const existingIds = new Set(realtimeData.value.map(item => item.id))
    const freshData = newData.filter(item => !existingIds.has(item.id))
    
    if (freshData.length > 0) {
      realtimeData.value = [...freshData, ...realtimeData.value].slice(0, 100) // 保留最新100条
      filterData()
      
      // 自动滚动到顶部
      if (autoScroll.value) {
        await nextTick()
        scrollToTop()
      }
    }
    
    lastUpdateTime.value = new Date().toLocaleTimeString()
  } catch (error) {
    console.error('获取实时数据失败:', error)
    // 静默处理错误，避免频繁提示
  } finally {
    loading.value = false
  }
}

// 过滤数据
const filterData = () => {
  if (showOnlySlowSql.value) {
    filteredData.value = realtimeData.value.filter(item => item.slowSql)
  } else {
    filteredData.value = [...realtimeData.value]
  }
}

// 滚动到顶部
const scrollToTop = () => {
  const container = tableContainerRef.value?.querySelector('.el-scrollbar__wrap')
  if (container) {
    container.scrollTop = 0
  }
}

// 切换自动刷新
const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  
  if (autoRefresh.value) {
    startAutoRefresh()
    ElMessage.success('实时监控已开启')
  } else {
    stopAutoRefresh()
    ElMessage.info('实时监控已暂停')
  }
}

// 开始自动刷新
const startAutoRefresh = () => {
  stopAutoRefresh() // 先停止之前的定时器
  refreshTimer = setInterval(fetchRealtimeData, refreshInterval.value)
  fetchRealtimeData() // 立即执行一次
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 处理刷新间隔变更
const handleRefreshInterval = (interval) => {
  refreshInterval.value = parseInt(interval)
  if (autoRefresh.value) {
    startAutoRefresh() // 重新开始定时器
  }
}

// 清空数据
const clearData = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前实时数据吗？', '确认清空', {
      type: 'warning'
    })
    
    realtimeData.value = []
    filteredData.value = []
    ElMessage.success('数据已清空')
  } catch (error) {
    // 用户取消
  }
}

// 获取SQL类型标签类型
const getSqlTypeTagType = (sqlType) => {
  const typeMap = {
    'SELECT': 'info',
    'INSERT': 'success',
    'UPDATE': 'warning',
    'DELETE': 'danger'
  }
  return typeMap[sqlType] || 'info'
}

// 获取执行时间样式类
const getExecutionTimeClass = (time) => {
  if (time > 1000) return 'slow-sql'
  if (time > 500) return 'medium-sql'
  return 'fast-sql'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleTimeString()
}

// 格式化SQL预览
const formatSqlPreview = (sql) => {
  if (!sql) return ''
  const preview = sql.replace(/\s+/g, ' ').trim()
  return preview.length > 80 ? preview.substring(0, 80) + '...' : preview
}

// 行点击处理
const handleRowClick = (row) => {
  showSqlDetail(row)
}

// 显示SQL详情
const showSqlDetail = (row) => {
  selectedSqlLog.value = row
  detailDialogVisible.value = true
}

// 刷新数据
const refresh = async () => {
  await fetchRealtimeData()
}

// 暴露方法给父组件
defineExpose({
  refresh
})

// 组件挂载
onMounted(() => {
  fetchRealtimeData()
})

// 组件卸载
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.realtime-panel {
  height: 100%;
}

.control-card {
  margin-bottom: 20px;
}

.control-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.last-update {
  font-size: 14px;
  color: #909399;
}

.control-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.realtime-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  transition: all 0.3s;
  cursor: pointer;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-item .el-card__body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
}

.stat-content {
  text-align: left;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-icon {
  font-size: 32px;
  color: #409EFF;
}

.stat-icon.slow {
  color: #F56C6C;
}

.stat-icon.error {
  color: #909399;
}

.realtime-table {
  flex: 1;
  min-height: 500px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-actions {
  display: flex;
  align-items: center;
}

.table-container {
  max-height: 450px;
  overflow-y: auto;
}

.time-stamp {
  font-family: monospace;
  font-size: 12px;
  color: #606266;
}

.success-icon {
  color: #67C23A;
}

.error-icon {
  color: #F56C6C;
}

.status-indicators {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.sql-preview {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  line-height: 1.4;
}

.sql-preview:hover {
  color: #409EFF;
  text-decoration: underline;
}

.slow-sql {
  color: #F56C6C;
  font-weight: bold;
}

.medium-sql {
  color: #E6A23C;
  font-weight: bold;
}

.fast-sql {
  color: #67C23A;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row):hover {
  background-color: #f5f7fa;
}

:deep(.el-table__row.current-row) {
  background-color: #ecf5ff;
}

/* 新数据动画效果 */
.new-data-enter-active {
  transition: all 0.5s ease;
}

.new-data-enter-from {
  opacity: 0;
  background-color: #e6f7ff;
}

.new-data-enter-to {
  opacity: 1;
  background-color: transparent;
}
</style> 