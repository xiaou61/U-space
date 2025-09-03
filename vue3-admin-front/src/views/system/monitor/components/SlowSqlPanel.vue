<template>
  <div class="slow-sql-panel">
    <!-- 查询条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" :inline="true" label-width="80px">
        <el-form-item label="用户名">
          <el-input 
            v-model="queryForm.username" 
            placeholder="请输入用户名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        
        <el-form-item label="最小执行时间">
          <el-input-number
            v-model="queryForm.minExecutionTime"
            :min="1000"
            :step="500"
            placeholder="毫秒"
            style="width: 150px"
          />
        </el-form-item>
        
        <el-form-item label="执行时间">
          <el-date-picker
            v-model="queryForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 350px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 慢SQL统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ totalSlowSql }}</div>
          <div class="stat-label">慢SQL总数</div>
        </div>
        <el-icon class="stat-icon slow"><Warning /></el-icon>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ avgExecutionTime }}ms</div>
          <div class="stat-label">平均执行时间</div>
        </div>
        <el-icon class="stat-icon avg"><Timer /></el-icon>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ maxExecutionTime }}ms</div>
          <div class="stat-label">最大执行时间</div>
        </div>
        <el-icon class="stat-icon max"><TrendCharts /></el-icon>
      </el-card>
    </div>

    <!-- 慢SQL表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>慢SQL列表</span>
          <el-button 
            type="danger" 
            size="small" 
            icon="Download"
            @click="exportSlowSql"
            :disabled="tableData.length === 0"
          >
            导出
          </el-button>
        </div>
      </template>
      
      <el-table 
        :data="tableData" 
        :loading="loading"
        stripe
        border
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="traceId" label="跟踪ID" width="120" show-overflow-tooltip />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="mapperMethod" label="Mapper方法" width="200" show-overflow-tooltip />
        <el-table-column prop="executionTime" label="执行时间(ms)" width="120" align="center" sortable>
          <template #default="{ row }">
            <el-tag type="danger" size="small">
              {{ row.executionTime }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="affectedRows" label="影响行数" width="90" align="center" />
        <el-table-column prop="sqlType" label="SQL类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getSqlTypeTagType(row.sqlType)"
              size="small"
            >
              {{ row.sqlType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeTime" label="执行时间" width="180" />
        <el-table-column label="SQL预览" min-width="300">
          <template #default="{ row }">
            <div class="sql-preview">
              {{ formatSqlPreview(row.sqlStatement) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleViewDetail(row)"
              link
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- SQL详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible"
      title="慢SQL执行详情"
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Timer, TrendCharts, Download } from '@element-plus/icons-vue'
import { monitorApi } from '@/api/monitor'
import SqlDetailDialog from './SqlDetailDialog.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const selectedSqlLog = ref(null)

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 20,
  username: '',
  minExecutionTime: 1000,
  timeRange: null,
  startTime: null,
  endTime: null,
  slowSqlOnly: true // 只查询慢SQL
})

// 计算统计数据
const totalSlowSql = computed(() => tableData.value.length)

const avgExecutionTime = computed(() => {
  if (tableData.value.length === 0) return 0
  const total = tableData.value.reduce((sum, item) => sum + item.executionTime, 0)
  return Math.round(total / tableData.value.length)
})

const maxExecutionTime = computed(() => {
  if (tableData.value.length === 0) return 0
  return Math.max(...tableData.value.map(item => item.executionTime))
})

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

// 格式化SQL预览
const formatSqlPreview = (sql) => {
  if (!sql) return ''
  const preview = sql.replace(/\s+/g, ' ').trim()
  return preview.length > 100 ? preview.substring(0, 100) + '...' : preview
}

// 查询数据
const fetchData = async () => {
  loading.value = true
  try {
    // 处理时间范围
    if (queryForm.timeRange && queryForm.timeRange.length === 2) {
      queryForm.startTime = queryForm.timeRange[0]
      queryForm.endTime = queryForm.timeRange[1]
    } else {
      queryForm.startTime = null
      queryForm.endTime = null
    }

    // 构建请求参数，排除不需要的字段
    const requestParams = {
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      traceId: queryForm.traceId,
      username: queryForm.username,
      sqlType: queryForm.sqlType,
      success: queryForm.success,
      startTime: queryForm.startTime,
      endTime: queryForm.endTime,
      slowSqlOnly: true  // 慢SQL查询标识
    }
    
    const data = await monitorApi.getSlowSqlLogs(requestParams)
    
    // 安全的数据访问，防止undefined错误
    if (data && typeof data === 'object') {
      tableData.value = data.records || []
      total.value = data.total || 0
    } else {
      console.warn('慢SQL日志接口返回数据格式异常:', data)
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取慢SQL日志失败:', error)
    ElMessage.error('获取慢SQL日志失败')
  } finally {
    loading.value = false
  }
}

// 查询处理
const handleQuery = () => {
  queryForm.pageNum = 1
  fetchData()
}

// 页码变更处理
const handlePageChange = () => {
  fetchData()
}

// 页面大小变更处理
const handlePageSizeChange = () => {
  queryForm.pageNum = 1
  fetchData()
}

// 重置处理
const handleReset = () => {
  Object.assign(queryForm, {
    pageNum: 1,
    pageSize: 20,
    username: '',
    minExecutionTime: 1000,
    timeRange: null,
    startTime: null,
    endTime: null,
    slowSqlOnly: true
  })
  fetchData()
}

// 行点击处理
const handleRowClick = (row) => {
  handleViewDetail(row)
}

// 查看详情
const handleViewDetail = (row) => {
  selectedSqlLog.value = row
  detailDialogVisible.value = true
}

// 导出慢SQL
const exportSlowSql = () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  
  // 构建CSV内容
  const headers = ['跟踪ID', '用户名', 'Mapper方法', '执行时间(ms)', '影响行数', 'SQL类型', '执行时间', 'SQL语句']
  const csvContent = [
    headers.join(','),
    ...tableData.value.map(row => [
      row.traceId,
      row.username || '',
      row.mapperMethod || '',
      row.executionTime,
      row.affectedRows,
      row.sqlType,
      row.executeTime,
      `"${row.sqlStatement?.replace(/"/g, '""') || ''}"`
    ].join(','))
  ].join('\n')
  
  // 下载文件
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `slow_sql_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('慢SQL数据导出成功')
}

// 刷新数据
const refresh = async () => {
  await fetchData()
}

// 暴露方法给父组件
defineExpose({
  refresh
})

// 组件挂载后获取数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.slow-sql-panel {
  height: 100%;
}

.filter-card {
  margin-bottom: 20px;
}

.stats-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card .el-card__body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
}

.stat-content {
  text-align: left;
}

.stat-value {
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
  font-size: 36px;
}

.stat-icon.slow {
  color: #F56C6C;
}

.stat-icon.avg {
  color: #E6A23C;
}

.stat-icon.max {
  color: #909399;
}

.table-card {
  min-height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sql-preview {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row):hover {
  background-color: #f5f7fa;
}
</style> 