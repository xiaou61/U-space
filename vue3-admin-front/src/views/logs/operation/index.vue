<template>
  <div class="operation-logs-container">
    <h1 style="margin-bottom: 24px;">操作日志</h1>
    
    <!-- 查询表单 -->
    <el-card style="margin-bottom: 20px;">
      <el-form :model="queryForm" inline>
        <el-form-item label="操作模块:">
          <el-input 
            v-model="queryForm.module" 
            placeholder="请输入操作模块" 
            clearable 
            style="width: 160px;"
          />
        </el-form-item>
        
        <el-form-item label="操作类型:">
          <el-select 
            v-model="queryForm.operationType" 
            placeholder="请选择操作类型" 
            clearable 
            style="width: 160px;"
          >
            <el-option label="查询" value="SELECT" />
            <el-option label="新增" value="INSERT" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="授权" value="GRANT" />
            <el-option label="导出" value="EXPORT" />
            <el-option label="导入" value="IMPORT" />
            <el-option label="强退" value="FORCE" />
            <el-option label="生成代码" value="GENCODE" />
            <el-option label="清空数据" value="CLEAN" />
            <el-option label="其它" value="OTHER" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="操作人:">
          <el-input 
            v-model="queryForm.operatorName" 
            placeholder="请输入操作人" 
            clearable 
            style="width: 160px;"
          />
        </el-form-item>
        
        <el-form-item label="操作状态:">
          <el-select 
            v-model="queryForm.status" 
            placeholder="请选择操作状态" 
            clearable 
            style="width: 120px;"
          >
            <el-option label="成功" :value="0" />
            <el-option label="失败" :value="1" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">
            查询
          </el-button>
          <el-button @click="handleReset" icon="Refresh">
            重置
          </el-button>
          <el-button 
            type="danger" 
            @click="handleBatchDelete" 
            :disabled="selectedRows.length === 0"
            icon="Delete"
          >
            批量删除
          </el-button>
          <el-button type="warning" @click="showCleanDialog = true" icon="DeleteFilled">
            清理日志
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card>
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="operationTypeText" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTagType(row.operationType)">
              {{ row.operationTypeText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" width="150" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operatorIp" label="操作IP" width="140" />
        <el-table-column prop="statusText" label="操作状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" />
        <el-table-column prop="operationTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.operationTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleView(row)"
              icon="View"
            >
              详情
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div style="margin-top: 20px; text-align: right;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="showDetail" title="操作日志详情" width="800px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="日志ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="操作ID">{{ currentRow.operationId }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentRow.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTagType(currentRow.operationType)">
            {{ currentRow.operationTypeText }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentRow.description }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentRow.method }}</el-descriptions-item>
        <el-descriptions-item label="请求URI">{{ currentRow.requestUri }}</el-descriptions-item>
        <el-descriptions-item label="HTTP方法">{{ currentRow.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentRow.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentRow.operatorIp }}</el-descriptions-item>
        <el-descriptions-item label="操作地点">{{ currentRow.operationLocation }}</el-descriptions-item>
        <el-descriptions-item label="浏览器">{{ currentRow.browser }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ currentRow.os }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentRow.status === 0 ? 'success' : 'danger'">
            {{ currentRow.statusText }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentRow.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ formatDateTime(currentRow.operationTime) }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2" v-if="currentRow.requestParams">
          <el-input 
            :model-value="currentRow.requestParams" 
            type="textarea" 
            :rows="3" 
            readonly
          />
        </el-descriptions-item>
        <el-descriptions-item label="响应数据" :span="2" v-if="currentRow.responseData">
          <el-input 
            :model-value="currentRow.responseData" 
            type="textarea" 
            :rows="3" 
            readonly
          />
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentRow.errorMsg">
          <el-input 
            :model-value="currentRow.errorMsg" 
            type="textarea" 
            :rows="2" 
            readonly
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <!-- 清理日志对话框 -->
    <el-dialog v-model="showCleanDialog" title="清理操作日志" width="400px">
      <el-form :model="cleanForm" label-width="120px">
        <el-form-item label="清理类型:">
          <el-radio-group v-model="cleanForm.type">
            <el-radio label="all">清空所有日志</el-radio>
            <el-radio label="days">按天数清理</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          label="保留天数:" 
          v-if="cleanForm.type === 'days'"
        >
          <el-input-number 
            v-model="cleanForm.days" 
            :min="1" 
            :max="365" 
            placeholder="请输入保留天数"
          />
          <div style="color: #999; font-size: 12px; margin-top: 5px;">
            将删除 {{ cleanForm.days }} 天前的日志
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCleanDialog = false">取消</el-button>
        <el-button type="danger" @click="handleClean">确定清理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logApi } from '@/api/log'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const showDetail = ref(false)
const showCleanDialog = ref(false)
const currentRow = ref(null)

// 查询表单
const queryForm = reactive({
  module: '',
  operationType: '',
  operatorName: '',
  status: null,
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

// 清理表单
const cleanForm = reactive({
  type: 'days',
  days: 30,
})

// 页面加载时获取数据
onMounted(() => {
  getOperationLogs()
})

// 获取操作日志数据
const getOperationLogs = async () => {
  try {
    loading.value = true
    
    const params = {
      ...queryForm,
      pageNum: pagination.page,
      pageSize: pagination.size,
    }
    
    const result = await logApi.getOperationLogs(params)
    
    tableData.value = result.records || []
    pagination.total = result.total || 0
    
  } catch (error) {
    console.error('获取操作日志失败:', error)
    ElMessage.error('获取操作日志失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  pagination.page = 1
  getOperationLogs()
}

// 重置
const handleReset = () => {
  Object.assign(queryForm, {
    module: '',
    operationType: '',
    operatorName: '',
    status: null,
  })
  pagination.page = 1
  getOperationLogs()
}

// 查看详情
const handleView = async (row) => {
  try {
    const result = await logApi.getOperationLogById(row.id)
    currentRow.value = result
    showDetail.value = true
  } catch (error) {
    console.error('获取操作日志详情失败:', error)
    ElMessage.error('获取操作日志详情失败')
  }
}

// 删除单条记录
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条操作日志吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await logApi.deleteOperationLogs([row.id])
    
    ElMessage.success('删除成功')
    getOperationLogs()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除操作日志失败:', error)
      ElMessage.error('删除操作日志失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条操作日志吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    await logApi.deleteOperationLogs(ids)
    
    ElMessage.success('批量删除成功')
    getOperationLogs()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除操作日志失败:', error)
      ElMessage.error('批量删除操作日志失败')
    }
  }
}

// 清理日志
const handleClean = async () => {
  try {
    let confirmText = ''
    if (cleanForm.type === 'all') {
      confirmText = '确定要清空所有操作日志吗？此操作不可恢复！'
    } else {
      confirmText = `确定要清理 ${cleanForm.days} 天前的操作日志吗？此操作不可恢复！`
    }
    
    await ElMessageBox.confirm(confirmText, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    if (cleanForm.type === 'all') {
      await logApi.clearOperationLogs()
    } else {
      await logApi.cleanOperationLogs(cleanForm.days)
    }
    
    ElMessage.success('清理成功')
    showCleanDialog.value = false
    getOperationLogs()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理操作日志失败:', error)
      ElMessage.error('清理操作日志失败')
    }
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  getOperationLogs()
}

// 页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  getOperationLogs()
}

// 获取操作类型标签颜色
const getOperationTypeTagType = (operationType) => {
  const typeMap = {
    'SELECT': '',
    'INSERT': 'success',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'GRANT': 'info',
    'EXPORT': 'primary',
    'IMPORT': 'primary',
    'FORCE': 'danger',
    'GENCODE': 'success',
    'CLEAN': 'danger',
    'OTHER': ''
  }
  return typeMap[operationType] || ''
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '—'
  return new Date(dateTime).toLocaleString()
}
</script>

<style scoped>
/* 移除多余的padding，使用布局统一的样式 */
</style> 