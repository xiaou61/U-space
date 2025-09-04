<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="title-section">
          <h2>用户管理</h2>
          <p>管理社区用户状态，支持封禁、解封、查看用户记录等操作</p>
        </div>
      </div>
    </el-card>

    <!-- 搜索和操作区 -->
    <el-card class="search-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input 
            v-model="searchForm.userName" 
            placeholder="请输入用户名" 
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.isBanned" placeholder="封禁状态" clearable @change="handleSearch">
            <el-option label="正常" :value="0" />
            <el-option label="已封禁" :value="1" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 15px;">
        <el-col :span="8">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleSearch"
          />
        </el-col>
      </el-row>
    </el-card>

    <!-- 用户表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="userList" 
        style="width: 100%"
        :row-key="row => row.id"
      >
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="userName" label="用户名" width="150" />
        <el-table-column label="活动统计" width="300" align="center">
          <template #default="{ row }">
            <div style="display: flex; flex-wrap: wrap; gap: 4px; justify-content: center;">
              <el-tag type="primary" size="small">发帖 {{ row.postCount || 0 }}</el-tag>
              <el-tag type="success" size="small">评论 {{ row.commentCount || 0 }}</el-tag>
              <el-tag type="warning" size="small">点赞 {{ row.likeCount || 0 }}</el-tag>
              <el-tag type="info" size="small">收藏 {{ row.collectCount || 0 }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="isBanned" label="封禁状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.isBanned === 0 ? 'success' : 'danger'"
              size="small"
            >
              {{ row.isBanned === 0 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="banReason" label="封禁原因" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.banReason">{{ row.banReason }}</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="banExpireTime" label="封禁到期时间" width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.banExpireTime">{{ row.banExpireTime }}</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewPosts(row)">
              <el-icon><Document /></el-icon>
              发帖记录
            </el-button>
            <el-button type="info" size="small" @click="handleViewComments(row)">
              <el-icon><ChatDotRound /></el-icon>
              评论记录
            </el-button>
            <el-button 
              v-if="row.isBanned === 0" 
              type="danger" 
              size="small" 
              @click="handleBan(row)"
            >
              <el-icon><Lock /></el-icon>
              封禁
            </el-button>
            <el-button 
              v-if="row.isBanned === 1" 
              type="success" 
              size="small" 
              @click="handleUnban(row)"
            >
              <el-icon><Unlock /></el-icon>
              解封
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 封禁用户对话框 -->
    <el-dialog 
      title="封禁用户" 
      v-model="banDialogVisible" 
      width="500px"
    >
      <el-form :model="banForm" :rules="banRules" ref="banFormRef" label-width="100px">
        <el-form-item label="用户" prop="userName">
          <el-input v-model="banForm.userName" disabled />
        </el-form-item>
        <el-form-item label="封禁原因" prop="reason">
          <el-input 
            v-model="banForm.reason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入封禁原因" 
            maxlength="200" 
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="封禁时长" prop="duration">
          <el-input-number 
            v-model="banForm.duration" 
            :min="1" 
            :max="8760" 
            placeholder="小时"
            style="width: 200px;"
          />
          <span style="margin-left: 8px; color: #909399;">小时</span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="banDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="handleBanSubmit" :loading="submitLoading">
            确定封禁
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 用户记录对话框 -->
    <el-dialog 
      :title="recordDialogTitle" 
      v-model="recordDialogVisible" 
      width="1000px"
      :before-close="() => recordDialogVisible = false"
    >
      <div v-if="recordType === 'posts'" class="record-content">
        <el-table :data="userPosts" style="width: 100%" max-height="400">
          <el-table-column prop="id" label="帖子ID" width="80" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="viewCount" label="浏览" width="80" />
          <el-table-column prop="likeCount" label="点赞" width="80" />
          <el-table-column prop="commentCount" label="评论" width="80" />
          <el-table-column prop="createTime" label="发布时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag 
                :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'"
                size="small"
              >
                {{ row.status === 1 ? '正常' : row.status === 2 ? '下架' : '删除' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-if="recordType === 'comments'" class="record-content">
        <el-table :data="userComments" style="width: 100%" max-height="400">
          <el-table-column prop="id" label="评论ID" width="80" />
          <el-table-column prop="postId" label="帖子ID" width="80" />
          <el-table-column prop="content" label="评论内容" min-width="300" show-overflow-tooltip />
          <el-table-column prop="likeCount" label="点赞" width="80" />
          <el-table-column prop="createTime" label="评论时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag 
                :type="row.status === 1 ? 'success' : 'danger'"
                size="small"
              >
                {{ row.status === 1 ? '正常' : '删除' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Document, ChatDotRound, Lock, Unlock } from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const banDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const userList = ref([])
const userPosts = ref([])
const userComments = ref([])
const currentUser = ref(null)
const recordType = ref('')
const banFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  userName: '',
  isBanned: null,
  timeRange: null
})

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 封禁表单
const banForm = reactive({
  userName: '',
  reason: '',
  duration: 24
})

// 封禁表单验证规则
const banRules = {
  reason: [
    { required: true, message: '请输入封禁原因', trigger: 'blur' },
    { max: 200, message: '封禁原因长度不能超过200字符', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入封禁时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 8760, message: '封禁时长必须在1-8760小时之间', trigger: 'blur' }
  ]
}

// 计算记录对话框标题
const recordDialogTitle = computed(() => {
  if (!currentUser.value) return ''
  return recordType.value === 'posts' 
    ? `${currentUser.value.userName} 的发帖记录` 
    : `${currentUser.value.userName} 的评论记录`
})

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    
    // 处理时间范围
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      params.startTime = searchForm.timeRange[0]
      params.endTime = searchForm.timeRange[1]
    }
    delete params.timeRange

    const data = await communityApi.getUserStatusList(params)
    userList.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchUsers()
}

// 重置搜索
const handleReset = () => {
  searchForm.userName = ''
  searchForm.isBanned = null
  searchForm.timeRange = null
  pagination.pageNum = 1
  fetchUsers()
}

// 查看发帖记录
const handleViewPosts = async (row) => {
  try {
    currentUser.value = row
    recordType.value = 'posts'
    const data = await communityApi.getUserPosts(row.userId)
    userPosts.value = data || []
    recordDialogVisible.value = true
  } catch (error) {
    console.error('获取用户发帖记录失败:', error)
    ElMessage.error('获取用户发帖记录失败')
  }
}

// 查看评论记录
const handleViewComments = async (row) => {
  try {
    currentUser.value = row
    recordType.value = 'comments'
    const data = await communityApi.getUserComments(row.userId)
    userComments.value = data || []
    recordDialogVisible.value = true
  } catch (error) {
    console.error('获取用户评论记录失败:', error)
    ElMessage.error('获取用户评论记录失败')
  }
}

// 封禁用户
const handleBan = (row) => {
  currentUser.value = row
  banForm.userName = row.userName
  banForm.reason = ''
  banForm.duration = 24
  banDialogVisible.value = true
}

// 提交封禁
const handleBanSubmit = async () => {
  const formElement = banFormRef.value
  if (!formElement) return

  try {
    await formElement.validate()
    submitLoading.value = true

    await communityApi.banUser(currentUser.value.userId, {
      reason: banForm.reason,
      duration: banForm.duration
    })
    
    ElMessage.success('封禁成功')
    banDialogVisible.value = false
    await fetchUsers()
  } catch (error) {
    if (error?.name !== 'ValidationError') {
      console.error('封禁用户失败:', error)
      ElMessage.error('封禁用户失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 解封用户
const handleUnban = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要解封用户 "${row.userName}" 吗？`,
      '解封确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await communityApi.unbanUser(row.userId)
    ElMessage.success('解封成功')
    await fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解封用户失败:', error)
      ElMessage.error('解封用户失败')
    }
  }
}

// 分页变化
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchUsers()
}

const handlePageSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchUsers()
}

// 页面挂载
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
}

.title-section p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.record-content {
  max-height: 500px;
  overflow-y: auto;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style> 