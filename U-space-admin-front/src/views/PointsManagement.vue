<template>
  <div class="points-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>积分管理</h2>
      <p>管理系统中的积分类型、发放记录和用户积分余额</p>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 积分类型管理 -->
      <el-tab-pane label="积分类型管理" name="types">
        <div class="tab-content">
          <!-- 操作栏 -->
          <div class="action-bar">
            <el-button type="primary" @click="openCreateTypeDialog">
              <el-icon><Plus /></el-icon>
              创建积分类型
            </el-button>
            
            <div class="search-box">
              <el-input
                v-model="typeSearchForm.typeName"
                placeholder="搜索积分类型名称"
                clearable
                @clear="fetchPointsTypes"
                @keyup.enter="fetchPointsTypes"
                style="width: 200px;"
              >
                <template #append>
                  <el-button @click="fetchPointsTypes">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>
          </div>

          <!-- 积分类型列表 -->
          <el-table
            :data="pointsTypes"
            v-loading="typesLoading"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="80" />
            
            <el-table-column label="积分类型" min-width="200">
              <template #default="{ row }">
                <div style="display: flex; align-items: center;">
                  <img 
                    v-if="row.iconUrl" 
                    :src="row.iconUrl" 
                    style="width: 32px; height: 32px; border-radius: 4px; margin-right: 8px;"
                    @error="handleImageError"
                  />
                  <div>
                    <div style="font-weight: 500;">{{ row.typeName }}</div>
                    <div style="font-size: 12px; color: #999;">{{ row.typeCode }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="description" label="描述" min-width="200" />
            
            <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
            
            <el-table-column prop="isActive" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-switch
                  v-model="row.isActive"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleToggleTypeStatus(row)"
                />
              </template>
            </el-table-column>
            
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template #default="{ row }">
                <span style="font-size: 12px;">{{ formatDateTime(row.createTime) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openEditTypeDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteType(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 积分记录查询 -->
      <el-tab-pane label="积分记录" name="records">
        <div class="tab-content">
          <!-- 搜索表单 -->
          <el-form :model="recordSearchForm" inline class="search-form">
            <el-form-item label="用户ID">
              <el-input 
                v-model="recordSearchForm.userId" 
                placeholder="请输入用户ID" 
                clearable 
                style="width: 200px;"
              />
            </el-form-item>
            <el-form-item label="积分类型">
              <el-select 
                v-model="recordSearchForm.pointsTypeId" 
                placeholder="请选择积分类型" 
                clearable 
                style="width: 200px;"
              >
                <el-option
                  v-for="type in pointsTypes"
                  :key="type.id"
                  :label="type.typeName"
                  :value="type.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="操作类型">
              <el-select 
                v-model="recordSearchForm.operationType" 
                placeholder="请选择操作类型" 
                clearable 
                style="width: 120px;"
              >
                <el-option label="获得" :value="1" />
                <el-option label="扣除" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="发放状态">
              <el-select 
                v-model="recordSearchForm.status" 
                placeholder="请选择状态" 
                clearable 
                style="width: 120px;"
              >
                <el-option label="待发放" :value="0" />
                <el-option label="已发放" :value="1" />
                <el-option label="发放失败" :value="2" />
                <el-option label="已撤销" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchPointsRecords">查询</el-button>
              <el-button @click="resetRecordSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 操作按钮 -->
          <div class="action-bar">
            <el-button type="primary" @click="openGrantPointsDialog">
              <el-icon><Money /></el-icon>
              手动发放积分
            </el-button>
          </div>

          <!-- 积分记录列表 -->
          <el-table
            :data="pointsRecords"
            v-loading="recordsLoading"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="id" label="记录ID" width="100" />
            
            <el-table-column prop="userId" label="用户ID" width="120" />
            
            <el-table-column prop="pointsTypeName" label="积分类型" width="120">
              <template #default="{ row }">
                {{ getPointsTypeName(row.pointsTypeId) }}
              </template>
            </el-table-column>
            
            <el-table-column label="积分数量" width="120" align="center">
              <template #default="{ row }">
                <span 
                  :style="{ color: row.operationType === 1 ? '#67c23a' : '#f56c6c' }"
                  style="font-weight: 500;"
                >
                  {{ row.operationType === 1 ? '+' : '-' }}{{ row.pointsAmount }}
                </span>
              </template>
            </el-table-column>
            
            <el-table-column prop="operationType" label="操作类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.operationType === 1 ? 'success' : 'danger'">
                  {{ row.operationType === 1 ? '获得' : '扣除' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="发放状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getRecordStatusTagType(row.status)">
                  {{ getRecordStatusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="activityId" label="活动ID" width="100">
              <template #default="{ row }">
                <span v-if="row.activityId">{{ row.activityId }}</span>
                <span v-else style="color: #c0c4cc;">-</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="grantTime" label="发放时间" width="160">
              <template #default="{ row }">
                <span style="font-size: 12px;">{{ formatDateTime(row.grantTime) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template #default="{ row }">
                <span style="font-size: 12px;">{{ formatDateTime(row.createTime) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button 
                  size="small" 
                  type="danger" 
                  @click="handleRevokeRecord(row.id)"
                  :disabled="row.status !== 1"
                >
                  撤销
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
            v-model:current-page="recordPagination.pageNum"
            v-model:page-size="recordPagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="recordPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleRecordSizeChange"
            @current-change="handleRecordCurrentChange"
            style="margin-top: 20px; text-align: right;"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建/编辑积分类型对话框 -->
    <el-dialog
      :title="typeDialogTitle"
      v-model="typeDialogVisible"
      width="50%"
      :close-on-click-modal="false"
    >
      <PointsTypeForm
        :form-data="currentPointsType"
        :is-edit="isEditType"
        @submit="handleTypeFormSubmit"
        @cancel="typeDialogVisible = false"
      />
    </el-dialog>

    <!-- 手动发放积分对话框 -->
    <el-dialog
      title="手动发放积分"
      v-model="grantDialogVisible"
      width="50%"
      :close-on-click-modal="false"
    >
      <GrantPointsForm
        @submit="handleGrantPointsSubmit"
        @cancel="grantDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Money } from '@element-plus/icons-vue'
import {
  getPointsTypes,
  createPointsType,
  updatePointsType,
  deletePointsType,
  togglePointsTypeStatus,
  getPointsRecords,
  grantPointsManually,
  revokePointsRecord
} from '../api/points'
import PointsTypeForm from '../components/PointsTypeForm.vue'
import GrantPointsForm from '../components/GrantPointsForm.vue'

// 响应式数据
const activeTab = ref('types')
const typesLoading = ref(false)
const recordsLoading = ref(false)
const pointsTypes = ref([])
const pointsRecords = ref([])

// 积分类型搜索
const typeSearchForm = reactive({
  typeName: ''
})

// 积分记录搜索
const recordSearchForm = reactive({
  userId: '',
  pointsTypeId: null,
  operationType: null,
  status: null
})

// 积分记录分页
const recordPagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 对话框控制
const typeDialogVisible = ref(false)
const grantDialogVisible = ref(false)
const isEditType = ref(false)
const currentPointsType = ref({})

// 计算属性
const typeDialogTitle = computed(() => isEditType.value ? '编辑积分类型' : '创建积分类型')

// 获取积分类型列表
const fetchPointsTypes = async () => {
  typesLoading.value = true
  try {
    const res = await getPointsTypes()
    pointsTypes.value = res.data || []
  } catch (error) {
    console.error('获取积分类型失败:', error)
  } finally {
    typesLoading.value = false
  }
}

// 获取积分记录列表
const fetchPointsRecords = async () => {
  recordsLoading.value = true
  try {
    const params = {
      pageNum: recordPagination.pageNum,
      pageSize: recordPagination.pageSize,
      ...recordSearchForm
    }
    const res = await getPointsRecords(params)
    if (res.data) {
      pointsRecords.value = res.data.records || []
      recordPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取积分记录失败:', error)
  } finally {
    recordsLoading.value = false
  }
}

onMounted(() => {
  fetchPointsTypes()
  fetchPointsRecords()
})

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'records') {
    fetchPointsRecords()
  }
}

// 打开创建积分类型对话框
const openCreateTypeDialog = () => {
  isEditType.value = false
  currentPointsType.value = {
    typeName: '',
    typeCode: '',
    description: '',
    iconUrl: '',
    sortOrder: 1
  }
  typeDialogVisible.value = true
}

// 打开编辑积分类型对话框
const openEditTypeDialog = (row) => {
  isEditType.value = true
  currentPointsType.value = { ...row }
  typeDialogVisible.value = true
}

// 积分类型表单提交
const handleTypeFormSubmit = async (formData) => {
  try {
    if (isEditType.value) {
      await updatePointsType(currentPointsType.value.id, formData)
      ElMessage.success('更新积分类型成功')
    } else {
      await createPointsType(formData)
      ElMessage.success('创建积分类型成功')
    }
    typeDialogVisible.value = false
    fetchPointsTypes()
  } catch (error) {
    console.error('提交表单失败:', error)
  }
}

// 切换积分类型状态
const handleToggleTypeStatus = async (row) => {
  try {
    await togglePointsTypeStatus(row.id, row.isActive)
    ElMessage.success(row.isActive ? '积分类型已启用' : '积分类型已禁用')
  } catch (error) {
    console.error('切换状态失败:', error)
    // 恢复状态
    row.isActive = row.isActive === 1 ? 0 : 1
  }
}

// 删除积分类型
const handleDeleteType = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此积分类型吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePointsType(id)
    ElMessage.success('删除成功')
    fetchPointsTypes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除积分类型失败:', error)
    }
  }
}

// 打开手动发放积分对话框
const openGrantPointsDialog = () => {
  grantDialogVisible.value = true
}

// 手动发放积分提交
const handleGrantPointsSubmit = async (formData) => {
  try {
    await grantPointsManually(formData)
    ElMessage.success('积分发放成功')
    grantDialogVisible.value = false
    fetchPointsRecords()
  } catch (error) {
    console.error('发放积分失败:', error)
  }
}

// 撤销积分记录
const handleRevokeRecord = async (recordId) => {
  try {
    await ElMessageBox.confirm('确定要撤销此积分记录吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await revokePointsRecord(recordId)
    ElMessage.success('撤销成功')
    fetchPointsRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销积分记录失败:', error)
    }
  }
}

// 重置记录搜索
const resetRecordSearch = () => {
  Object.assign(recordSearchForm, {
    userId: '',
    pointsTypeId: null,
    operationType: null,
    status: null
  })
  recordPagination.pageNum = 1
  fetchPointsRecords()
}

// 记录分页事件
const handleRecordSizeChange = () => {
  recordPagination.pageNum = 1
  fetchPointsRecords()
}

const handleRecordCurrentChange = () => {
  fetchPointsRecords()
}

// 工具函数
const getPointsTypeName = (typeId) => {
  const type = pointsTypes.value.find(t => t.id === typeId)
  return type ? type.typeName : '未知类型'
}

const getRecordStatusName = (status) => {
  const statusMap = { 0: '待发放', 1: '已发放', 2: '发放失败', 3: '已撤销' }
  return statusMap[status] || '未知'
}

const getRecordStatusTagType = (status) => {
  const typeMap = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }
  return typeMap[status] || ''
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  return dateTimeStr.replace('T', ' ').substring(0, 19)
}

const handleImageError = (e) => {
  e.target.style.display = 'none'
}
</script>

<style scoped>
.points-management {
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

.tab-content {
  padding: 20px 0;
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

.search-form {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.el-table {
  border-radius: 4px;
  overflow: hidden;
}
</style> 