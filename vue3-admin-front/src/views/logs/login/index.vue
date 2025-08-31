<template>
  <div class="login-logs-container">
    <h1 style="margin-bottom: 24px;">登录日志</h1>
    
    <!-- 查询表单 -->
    <el-card style="margin-bottom: 20px;">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户名:">
          <el-input 
            v-model="queryForm.username" 
            placeholder="请输入用户名" 
            clearable 
            style="width: 200px;"
          />
        </el-form-item>
        
        <el-form-item label="IP地址:">
          <el-input 
            v-model="queryForm.loginIp" 
            placeholder="请输入IP地址" 
            clearable 
            style="width: 200px;"
          />
        </el-form-item>
        
        <el-form-item label="登录状态:">
          <el-select 
            v-model="queryForm.loginStatus" 
            placeholder="请选择登录状态" 
            clearable 
            style="width: 200px;"
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
          <el-button type="danger" @click="handleClear" icon="Delete">
            清空日志
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
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="loginIp" label="登录IP" width="140" />
        <el-table-column prop="loginLocation" label="登录地点" width="120" />
        <el-table-column prop="browser" label="浏览器" width="120" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column prop="loginStatusText" label="登录状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.loginStatus === 0 ? 'success' : 'danger'">
              {{ row.loginStatusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginMessage" label="登录消息" width="150" />
        <el-table-column prop="loginTime" label="登录时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.loginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleView(row)"
              icon="View"
            >
              详情
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
    <el-dialog v-model="showDetail" title="登录日志详情" width="600px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="日志ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="管理员ID">{{ currentRow.adminId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentRow.username }}</el-descriptions-item>
        <el-descriptions-item label="登录IP">{{ currentRow.loginIp }}</el-descriptions-item>
        <el-descriptions-item label="登录地点">{{ currentRow.loginLocation }}</el-descriptions-item>
        <el-descriptions-item label="浏览器">{{ currentRow.browser }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ currentRow.os }}</el-descriptions-item>
        <el-descriptions-item label="登录状态">
          <el-tag :type="currentRow.loginStatus === 0 ? 'success' : 'danger'">
            {{ currentRow.loginStatusText }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="登录消息" :span="2">{{ currentRow.loginMessage }}</el-descriptions-item>
        <el-descriptions-item label="登录时间" :span="2">{{ formatDateTime(currentRow.loginTime) }}</el-descriptions-item>
      </el-descriptions>
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
const currentRow = ref(null)

// 查询表单
const queryForm = reactive({
  username: '',
  loginIp: '',
  loginStatus: null,
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

// 页面加载时获取数据
onMounted(() => {
  getLoginLogs()
})

// 获取登录日志数据
const getLoginLogs = async () => {
  try {
    loading.value = true
    
    const params = {
      ...queryForm,
      pageNum: pagination.page,
      pageSize: pagination.size,
    }
    
    const result = await logApi.getLoginLogs(params)
    
    tableData.value = result.records || []
    pagination.total = result.total || 0
    
  } catch (error) {
    console.error('获取登录日志失败:', error)
    ElMessage.error('获取登录日志失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  pagination.page = 1
  getLoginLogs()
}

// 重置
const handleReset = () => {
  Object.assign(queryForm, {
    username: '',
    loginIp: '',
    loginStatus: null,
  })
  pagination.page = 1
  getLoginLogs()
}

// 清空日志
const handleClear = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有登录日志吗？此操作不可恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await logApi.clearLoginLogs()
    
    ElMessage.success('登录日志已清空')
    getLoginLogs()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空登录日志失败:', error)
      ElMessage.error('清空登录日志失败')
    }
  }
}

// 查看详情
const handleView = async (row) => {
  try {
    const result = await logApi.getLoginLogById(row.id)
    currentRow.value = result
    showDetail.value = true
  } catch (error) {
    console.error('获取登录日志详情失败:', error)
    ElMessage.error('获取登录日志详情失败')
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
  getLoginLogs()
}

// 页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  getLoginLogs()
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