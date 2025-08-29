<template>
  <div class="participants-view">
    <!-- 统计信息 -->
    <div class="stats-bar">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ participants.length }}</div>
              <div class="stat-label">总参与人数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value success">{{ getSuccessCount() }}</div>
              <div class="stat-label">成功参与</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value warning">{{ getCancelledCount() }}</div>
              <div class="stat-label">已取消</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value danger">{{ getViolationCount() }}</div>
              <div class="stat-label">违规取消</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="left-actions">
        <el-button 
          type="success" 
          @click="handleBatchGrantPoints"
          :disabled="!activityId || participants.length === 0"
        >
          <el-icon><Money /></el-icon>
          为所有参与者发放积分
        </el-button>
        <el-button @click="exportParticipants">
          <el-icon><Download /></el-icon>
          导出参与者列表
        </el-button>
      </div>
      
      <div class="right-actions">
        <!-- 状态筛选 -->
        <el-select 
          v-model="statusFilter" 
          placeholder="参与状态" 
          clearable 
          style="width: 120px;"
          @change="applyFilters"
        >
          <el-option label="参与成功" :value="0" />
          <el-option label="已取消" :value="1" />
          <el-option label="违规取消" :value="2" />
        </el-select>

        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户"
          clearable
          style="width: 200px;"
          @input="applyFilters"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 参与者列表 -->
    <el-table
      :data="filteredParticipants"
      v-loading="loading"
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      
      <el-table-column label="排名" width="80" align="center">
        <template #default="{ row }">
          <div class="rank-badge">
            <el-tag 
              v-if="row.participantRank <= 3 && row.status === 0" 
              :type="getRankTagType(row.participantRank)"
              effect="dark"
            >
              第{{ row.participantRank }}名
            </el-tag>
            <span v-else-if="row.status === 0">{{ row.participantRank }}</span>
            <span v-else style="color: #999;">-</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="用户信息" min-width="200">
        <template #default="{ row }">
          <div class="user-info">
            <div class="user-avatar">
              <el-avatar 
                :src="row.userAvatar" 
                :size="40"
                @error="handleAvatarError"
              >
                {{ (row.userName || row.userId).substring(0, 1) }}
              </el-avatar>
            </div>
            <div class="user-details">
              <div class="user-name">{{ row.userName || row.userId }}</div>
              <div class="user-id">ID: {{ row.userId }}</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="participateTime" label="参与时间" width="160">
        <template #default="{ row }">
          <div>
            <div style="font-weight: 500;">{{ formatDate(row.participateTime) }}</div>
            <div style="font-size: 12px; color: #999;">{{ formatTime(row.participateTime) }}</div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="status" label="参与状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="remark" label="备注信息" min-width="150">
        <template #default="{ row }">
          <span v-if="row.remark" style="color: #666;">{{ row.remark }}</span>
          <span v-else style="color: #c0c4cc;">-</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button 
            size="small" 
            type="primary" 
            @click="viewUserDetail(row)"
          >
            查看详情
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

    <!-- 用户详情对话框 -->
    <el-dialog
      title="用户详情"
      v-model="userDetailVisible"
      width="50%"
    >
      <div v-if="currentUser" class="user-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentUser.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="参与时间">{{ formatDateTime(currentUser.participateTime) }}</el-descriptions-item>
          <el-descriptions-item label="参与状态">
            <el-tag :type="getStatusTagType(currentUser.status)">
              {{ getStatusName(currentUser.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="参与排名" v-if="currentUser.status === 0">
            第{{ currentUser.participantRank }}名
          </el-descriptions-item>
          <el-descriptions-item label="备注信息" :span="2">
            {{ currentUser.remark || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Money, Download } from '@element-plus/icons-vue'
import { getActivityParticipants } from '../api/activity'
import { batchGrantPointsForActivity } from '../api/points'

// Props
const props = defineProps({
  activityId: {
    type: [Number, String],
    required: true
  }
})

// 响应式数据
const loading = ref(false)
const participants = ref([])
const filteredParticipants = ref([])
const multipleSelection = ref([])
const userDetailVisible = ref(false)
const currentUser = ref(null)

// 筛选条件
const statusFilter = ref(null)
const searchKeyword = ref('')

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 获取参与者列表
const fetchParticipants = async () => {
  if (!props.activityId) return
  
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await getActivityParticipants(props.activityId, params)
    if (res.data) {
      participants.value = res.data.records || []
      pagination.total = res.data.total || 0
      applyFilters()
    }
  } catch (error) {
    console.error('获取参与者列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchParticipants)

// 应用筛选条件
const applyFilters = () => {
  let filtered = [...participants.value]
  
  // 状态筛选
  if (statusFilter.value !== null) {
    filtered = filtered.filter(item => item.status === statusFilter.value)
  }
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(item => 
      (item.userName && item.userName.toLowerCase().includes(keyword)) ||
      (item.userId && item.userId.toLowerCase().includes(keyword))
    )
  }
  
  filteredParticipants.value = filtered
}

// 分页事件
const handleSizeChange = () => {
  pagination.pageNum = 1
  fetchParticipants()
}

const handleCurrentChange = () => {
  fetchParticipants()
}

// 表格选择事件
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

// 统计函数
const getSuccessCount = () => {
  return participants.value.filter(item => item.status === 0).length
}

const getCancelledCount = () => {
  return participants.value.filter(item => item.status === 1).length
}

const getViolationCount = () => {
  return participants.value.filter(item => item.status === 2).length
}

// 批量发放积分
const handleBatchGrantPoints = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要为此活动的所有成功参与者发放积分吗？',
      '批量发放积分',
      {
        confirmButtonText: '确定发放',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await batchGrantPointsForActivity(props.activityId)
    ElMessage.success('积分发放成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量发放积分失败:', error)
    }
  }
}

// 导出参与者列表
const exportParticipants = () => {
  // 这里可以实现导出功能
  ElMessage.info('导出功能开发中...')
}

// 查看用户详情
const viewUserDetail = (row) => {
  currentUser.value = row
  userDetailVisible.value = true
}

// 工具函数
const getStatusName = (status) => {
  const statusMap = { 0: '参与成功', 1: '已取消', 2: '违规取消' }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status) => {
  const typeMap = { 0: 'success', 1: 'warning', 2: 'danger' }
  return typeMap[status] || ''
}

const getRankTagType = (rank) => {
  if (rank === 1) return 'danger'
  if (rank === 2) return 'warning'
  if (rank === 3) return 'success'
  return ''
}

const formatDate = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  return dateTimeStr.split(' ')[0]
}

const formatTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  return dateTimeStr.split(' ')[1]
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  return dateTimeStr.replace('T', ' ').substring(0, 19)
}

const handleAvatarError = () => {
  // 头像加载失败时的处理
}
</script>

<style scoped>
.participants-view {
  padding: 20px 0;
}

.stats-bar {
  margin-bottom: 20px;
}

.stat-card {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 4px;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.left-actions,
.right-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  margin-right: 12px;
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 500;
  margin-bottom: 2px;
}

.user-id {
  font-size: 12px;
  color: #999;
}

.rank-badge {
  display: flex;
  justify-content: center;
}

.user-detail {
  padding: 20px 0;
}

.el-table {
  border-radius: 4px;
  overflow: hidden;
}
</style> 