<template>
  <div class="activity-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>活动管理</h2>
      <p>管理系统中的所有活动，包括创建、编辑、状态控制和参与者管理</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        创建活动
      </el-button>
      <el-button @click="handleBatchGrantPoints">
        <el-icon><Money /></el-icon>
        批量发放积分
      </el-button>
      <el-button @click="handleBatchUpdateStatus">
        <el-icon><Refresh /></el-icon>
        批量更新状态
      </el-button>
      
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchForm.title"
          placeholder="搜索活动标题"
          clearable
          @clear="fetchData"
          @keyup.enter="fetchData"
          style="width: 200px;"
        >
          <template #append>
            <el-button @click="fetchData">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 活动列表表格 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      
      <el-table-column prop="id" label="ID" width="80" />
      
      <el-table-column prop="title" label="活动标题" min-width="150">
        <template #default="{ row }">
          <div style="display: flex; align-items: center;">
            <img 
              v-if="row.coverImage" 
              :src="row.coverImage" 
              style="width: 40px; height: 40px; border-radius: 4px; margin-right: 8px;"
              @error="handleImageError"
            />
            <div>
              <div style="font-weight: 500;">{{ row.title }}</div>
              <div style="font-size: 12px; color: #999;">{{ row.description }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="activityTypeName" label="活动类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getActivityTypeTagType(row.activityType)">
            {{ row.activityTypeName || getActivityTypeName(row.activityType) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="参与情况" width="120">
        <template #default="{ row }">
          <div>
            <div style="font-size: 14px; font-weight: 500;">
              {{ row.currentParticipants || 0 }} / {{ row.maxParticipants }}
            </div>
            <el-progress 
              :percentage="getParticipationPercentage(row)" 
              :stroke-width="6"
              :show-text="false"
              style="margin-top: 4px;"
            />
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="pointsAmount" label="奖励积分" width="100">
        <template #default="{ row }">
          <el-tag type="warning" v-if="row.pointsAmount > 0">
            {{ row.pointsAmount }} 分
          </el-tag>
          <span v-else style="color: #999;">无奖励</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="status" label="活动状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ row.statusName || getStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="活动时间" width="180">
        <template #default="{ row }">
          <div style="font-size: 12px;">
            <div>开始：{{ formatDateTime(row.startTime) }}</div>
            <div>结束：{{ formatDateTime(row.endTime) }}</div>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="createTime" label="创建时间" width="120">
        <template #default="{ row }">
          <span style="font-size: 12px;">{{ formatDateTime(row.createTime) }}</span>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="info" @click="viewParticipants(row)">参与者</el-button>
          
          <el-dropdown @command="handleStatusCommand">
            <el-button size="small" type="warning">
              状态 <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item 
                  :command="{action: 'enable', id: row.id}" 
                  :disabled="row.status !== 4"
                >
                  启用活动
                </el-dropdown-item>
                <el-dropdown-item 
                  :command="{action: 'disable', id: row.id}"
                  :disabled="row.status === 3 || row.status === 4"
                >
                  禁用活动
                </el-dropdown-item>
                <el-dropdown-item 
                  :command="{action: 'cancel', id: row.id}"
                  :disabled="row.status === 3"
                >
                  取消活动
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <el-button 
            size="small" 
            type="success" 
            @click="handleGrantPoints(row)"
            :disabled="row.status !== 2 || row.pointsAmount <= 0"
          >
            发放积分
          </el-button>
          
          <el-button 
            size="small" 
            type="danger" 
            @click="handleDelete(row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      style="margin-top: 20px; text-align: right;"
    />

    <!-- 创建/编辑活动对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="60%"
      :close-on-click-modal="false"
    >
      <ActivityForm
        :form-data="currentActivity"
        :is-edit="isEdit"
        @submit="handleFormSubmit"
        @cancel="dialogVisible = false"
      />
    </el-dialog>

    <!-- 参与者查看对话框 -->
    <el-dialog
      title="活动参与者"
      v-model="participantsDialogVisible"
      width="70%"
    >
      <ParticipantsView
        :activity-id="currentActivityId"
        v-if="participantsDialogVisible"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Money, Refresh, ArrowDown } from '@element-plus/icons-vue'
import {
  getActivityPage,
  createActivity,
  updateActivity,
  deleteActivity,
  enableActivity,
  disableActivity,
  cancelActivity,
  autoGrantPoints,
  batchGrantPoints,
  batchUpdateStatus
} from '../api/activity'
import ActivityForm from '../components/ActivityForm.vue'
import ParticipantsView from '../components/ParticipantsView.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const multipleSelection = ref([])

// 搜索表单
const searchForm = reactive({
  title: ''
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 对话框控制
const dialogVisible = ref(false)
const participantsDialogVisible = ref(false)
const isEdit = ref(false)
const currentActivity = ref({})
const currentActivityId = ref(null)

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑活动' : '创建活动')

// 获取活动列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const res = await getActivityPage(params)
    if (res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 页面挂载时获取数据
onMounted(fetchData)

// 表格选择变化
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

// 分页事件
const handleSizeChange = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleCurrentChange = () => {
  fetchData()
}

// 打开创建对话框
const openCreateDialog = () => {
  isEdit.value = false
  currentActivity.value = {
    title: '',
    description: '',
    coverImage: '',
    maxParticipants: 100,
    pointsTypeId: null,
    pointsAmount: 0,
    activityType: 1,
    startTime: '',
    endTime: '',
    rules: ''
  }
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (row) => {
  isEdit.value = true
  currentActivity.value = { ...row }
  dialogVisible.value = true
}

// 表单提交处理
const handleFormSubmit = async (formData) => {
  try {
    if (isEdit.value) {
      await updateActivity(currentActivity.value.id, formData)
      ElMessage.success('更新活动成功')
    } else {
      await createActivity(formData)
      ElMessage.success('创建活动成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交表单失败:', error)
  }
}

// 删除活动
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此活动吗？删除后不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteActivity(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除活动失败:', error)
    }
  }
}

// 状态操作处理
const handleStatusCommand = async (command) => {
  const { action, id } = command
  try {
    let message = ''
    switch (action) {
      case 'enable':
        await enableActivity(id)
        message = '活动已启用'
        break
      case 'disable':
        await disableActivity(id)
        message = '活动已禁用'
        break
      case 'cancel':
        await ElMessageBox.confirm('确定要取消此活动吗？取消后不可恢复！', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await cancelActivity(id)
        message = '活动已取消'
        break
    }
    ElMessage.success(message)
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态操作失败:', error)
    }
  }
}

// 发放积分
const handleGrantPoints = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要为活动"${row.title}"的所有参与者发放积分吗？`,
      '确认发放积分',
      {
        confirmButtonText: '确定发放',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await autoGrantPoints(row.id)
    ElMessage.success('积分发放成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发放积分失败:', error)
    }
  }
}

// 批量发放积分
const handleBatchGrantPoints = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要批量处理所有已结束活动的积分发放吗？',
      '批量发放积分',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await batchGrantPoints()
    ElMessage.success(res.data || '批量积分发放任务已触发')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量发放积分失败:', error)
    }
  }
}

// 批量更新状态
const handleBatchUpdateStatus = async () => {
  try {
    const res = await batchUpdateStatus()
    ElMessage.success(res.data || '批量状态更新任务已触发')
    fetchData()
  } catch (error) {
    console.error('批量更新状态失败:', error)
  }
}

// 查看参与者
const viewParticipants = (row) => {
  currentActivityId.value = row.id
  participantsDialogVisible.value = true
}

// 工具函数
const getActivityTypeName = (type) => {
  const types = { 1: '抢夺型', 2: '签到型', 3: '任务型' }
  return types[type] || '未知'
}

const getActivityTypeTagType = (type) => {
  const types = { 1: 'danger', 2: 'success', 3: 'warning' }
  return types[type] || ''
}

const getStatusName = (status) => {
  const statuses = { 0: '待开始', 1: '进行中', 2: '已结束', 3: '已取消', 4: '已禁用' }
  return statuses[status] || '未知'
}

const getStatusTagType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' }
  return types[status] || ''
}

const getParticipationPercentage = (row) => {
  if (!row.maxParticipants || row.maxParticipants === 0) return 0
  return Math.round((row.currentParticipants || 0) / row.maxParticipants * 100)
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  return dateTimeStr.replace('T', ' ').substring(0, 16)
}

const handleImageError = (e) => {
  e.target.style.display = 'none'
}
</script>

<style scoped>
.activity-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.el-table {
  border-radius: 4px;
  overflow: hidden;
}

.el-progress {
  width: 80px;
}
</style> 