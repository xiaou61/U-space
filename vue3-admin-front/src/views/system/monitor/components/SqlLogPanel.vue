<template>
  <div class="sql-log-panel">
    <!-- 查询条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" :inline="true" label-width="80px">
        <el-form-item label="跟踪ID">
          <el-input 
            v-model="queryForm.traceId" 
            placeholder="请输入跟踪ID"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="用户名">
          <el-input 
            v-model="queryForm.username" 
            placeholder="请输入用户名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        
        <el-form-item label="SQL类型">
          <el-select 
            v-model="queryForm.sqlType" 
            placeholder="请选择SQL类型"
            clearable
            style="width: 120px"
          >
            <el-option label="SELECT" value="SELECT" />
            <el-option label="INSERT" value="INSERT" />
            <el-option label="UPDATE" value="UPDATE" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="执行状态">
          <el-select 
            v-model="queryForm.success" 
            placeholder="请选择执行状态"
            clearable
            style="width: 120px"
          >
            <el-option label="成功" :value="true" />
            <el-option label="失败" :value="false" />
          </el-select>
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

    <!-- SQL日志表格 -->
    <el-card class="table-card" shadow="never">
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
        <el-table-column prop="mapperMethod" label="Mapper方法" width="200" show-overflow-tooltip />
        <el-table-column prop="executionTime" label="执行时间(ms)" width="110" align="center">
          <template #default="{ row }">
            <span :class="getExecutionTimeClass(row.executionTime)">
              {{ row.executionTime }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="affectedRows" label="影响行数" width="90" align="center" />
        <el-table-column prop="success" label="执行状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">
              {{ row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="slowSql" label="慢SQL" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.slowSql" type="warning" size="small">慢SQL</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeTime" label="执行时间" width="180" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
  traceId: '',
  username: '',
  sqlType: '',
  success: null,
  timeRange: null,
  startTime: null,
  endTime: null
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

// 获取执行时间样式类
const getExecutionTimeClass = (time) => {
  if (time > 1000) return 'slow-sql'
  if (time > 500) return 'medium-sql'
  return 'fast-sql'
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
      endTime: queryForm.endTime
    }
    
    const data = await monitorApi.getSqlLogs(requestParams)
    
    // 安全的数据访问，防止undefined错误
    if (data && typeof data === 'object') {
      tableData.value = data.records || []
      total.value = data.total || 0
    } else {
      console.warn('SQL日志接口返回数据格式异常:', data)
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取SQL日志失败:', error)
    ElMessage.error('获取SQL日志失败')
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
    traceId: '',
    username: '',
    sqlType: '',
    success: null,
    timeRange: null,
    startTime: null,
    endTime: null
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
.sql-log-panel {
  height: 100%;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  min-height: 600px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
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
</style> 