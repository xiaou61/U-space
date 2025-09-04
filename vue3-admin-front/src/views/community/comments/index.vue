<template>
  <div class="comment-management">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="title-section">
          <h2>评论管理</h2>
          <p>管理社区评论，支持查看、删除等操作</p>
        </div>
      </div>
    </el-card>

    <!-- 搜索和操作区 -->
    <el-card class="search-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input 
            v-model="searchForm.postId" 
            placeholder="请输入帖子ID" 
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-input 
            v-model="searchForm.authorName" 
            placeholder="评论者用户名" 
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="正常" :value="1" />
            <el-option label="删除" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="8">
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

    <!-- 评论表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="commentList" 
        style="width: 100%"
        :row-key="row => row.id"
      >
        <el-table-column prop="id" label="评论ID" width="100" />
        <el-table-column prop="postId" label="帖子ID" width="100" />
        <el-table-column prop="content" label="评论内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="comment-content">
              <div v-if="row.parentId > 0" style="color: #909399; font-size: 12px; margin-bottom: 4px;">
                <el-icon><Back /></el-icon>
                回复评论
              </div>
              <div>{{ row.content }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="评论者" width="120" />
        <el-table-column prop="likeCount" label="点赞数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.likeCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === 1 ? '正常' : '删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :disabled="row.status === 2"
            >
              <el-icon><Delete /></el-icon>
              删除
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

    <!-- 评论详情对话框 -->
    <el-dialog 
      title="评论详情" 
      v-model="detailVisible" 
      width="800px"
      :before-close="() => detailVisible = false"
    >
      <div v-if="currentComment" class="comment-detail">
        <div class="detail-header">
          <h4>评论信息</h4>
          <div class="meta-info">
            <span>评论者：{{ currentComment.authorName }}</span>
            <span>帖子ID：{{ currentComment.postId }}</span>
            <span v-if="currentComment.parentId > 0">父评论ID：{{ currentComment.parentId }}</span>
            <span>评论时间：{{ currentComment.createTime }}</span>
          </div>
        </div>
        <div class="detail-content">
          <h5>评论内容：</h5>
          <div class="content-box">
            <pre style="white-space: pre-wrap; font-family: inherit;">{{ currentComment.content }}</pre>
          </div>
          <div class="stats">
            <el-tag type="success">点赞数：{{ currentComment.likeCount || 0 }}</el-tag>
            <el-tag :type="currentComment.status === 1 ? 'success' : 'danger'" style="margin-left: 8px;">
              状态：{{ currentComment.status === 1 ? '正常' : '删除' }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Delete, Back } from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'

// 响应式数据
const loading = ref(false)
const detailVisible = ref(false)
const commentList = ref([])
const currentComment = ref(null)

// 搜索表单
const searchForm = reactive({
  postId: '',
  authorName: '',
  status: null,
  timeRange: null
})

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 获取评论列表
const fetchComments = async () => {
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

    // 处理帖子ID为数字
    if (params.postId) {
      params.postId = parseInt(params.postId) || null
    }

    const data = await communityApi.getCommentList(params)
    commentList.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchComments()
}

// 重置搜索
const handleReset = () => {
  searchForm.postId = ''
  searchForm.authorName = ''
  searchForm.status = null
  searchForm.timeRange = null
  pagination.pageNum = 1
  fetchComments()
}

// 查看详情
const handleView = async (row) => {
  try {
    const data = await communityApi.getCommentById(row.id)
    currentComment.value = data
    detailVisible.value = true
  } catch (error) {
    console.error('获取评论详情失败:', error)
    ElMessage.error('获取评论详情失败')
  }
}

// 删除评论
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条评论吗？删除后无法恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await communityApi.deleteComment(row.id)
    ElMessage.success('删除成功')
    await fetchComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 分页变化
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchComments()
}

const handlePageSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchComments()
}

// 页面挂载
onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-management {
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

.comment-content {
  max-width: 300px;
}

.comment-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-header {
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.detail-header h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.meta-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.detail-content h5 {
  margin: 0 0 10px 0;
  color: #303133;
}

.content-box {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 15px;
  line-height: 1.6;
  color: #606266;
}

.stats {
  margin-top: 15px;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .meta-info {
    flex-direction: column;
    gap: 8px;
  }
}
</style> 