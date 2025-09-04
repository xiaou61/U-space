<template>
  <div class="post-management">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="title-section">
          <h2>帖子管理</h2>
          <p>管理社区帖子，支持置顶、下架、删除等操作</p>
        </div>
      </div>
    </el-card>

    <!-- 搜索和操作区 -->
    <el-card class="search-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入标题或内容关键词" 
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
          <el-input 
            v-model="searchForm.authorName" 
            placeholder="作者用户名" 
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-input 
            v-model="searchForm.category" 
            placeholder="分类标签" 
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="正常" :value="1" />
            <el-option label="下架" :value="2" />
            <el-option label="删除" :value="3" />
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

    <!-- 帖子表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="postList" 
        style="width: 100%"
        :row-key="row => row.id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="帖子标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-tag v-if="row.isTop === 1" type="danger" size="small" style="margin-right: 8px;">置顶</el-tag>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.category" type="primary" size="small">{{ row.category }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column label="数据统计" width="200" align="center">
          <template #default="{ row }">
            <div style="display: flex; flex-direction: column; gap: 4px;">
              <div>
                <el-tag type="info" size="small">浏览 {{ row.viewCount || 0 }}</el-tag>
                <el-tag type="success" size="small" style="margin-left: 4px;">赞 {{ row.likeCount || 0 }}</el-tag>
              </div>
              <div>
                <el-tag type="warning" size="small">评论 {{ row.commentCount || 0 }}</el-tag>
                <el-tag type="primary" size="small" style="margin-left: 4px;">收藏 {{ row.collectCount || 0 }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'"
              size="small"
            >
              {{ row.status === 1 ? '正常' : row.status === 2 ? '下架' : '删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button 
              v-if="row.status === 1 && row.isTop === 0" 
              type="warning" 
              size="small" 
              @click="handleTop(row)"
            >
              <el-icon><Top /></el-icon>
              置顶
            </el-button>
            <el-button 
              v-if="row.isTop === 1" 
              type="info" 
              size="small" 
              @click="handleCancelTop(row)"
            >
              <el-icon><Bottom /></el-icon>
              取消置顶
            </el-button>
            <el-button 
              v-if="row.status === 1" 
              type="warning" 
              size="small" 
              @click="handleDisable(row)"
            >
              <el-icon><Lock /></el-icon>
              下架
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :disabled="row.status === 3"
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

    <!-- 帖子详情对话框 -->
    <el-dialog 
      title="帖子详情" 
      v-model="detailVisible" 
      width="800px"
      :before-close="() => detailVisible = false"
    >
      <div v-if="currentPost" class="post-detail">
        <div class="detail-header">
          <h3>{{ currentPost.title }}</h3>
          <div class="meta-info">
            <span>作者：{{ currentPost.authorName }}</span>
            <span v-if="currentPost.category">分类：{{ currentPost.category }}</span>
            <span>创建时间：{{ currentPost.createTime }}</span>
          </div>
        </div>
        <div class="detail-content markdown-content" v-html="formatContent(currentPost.content)"></div>
      </div>
    </el-dialog>

    <!-- 置顶对话框 -->
    <el-dialog 
      title="置顶帖子" 
      v-model="topDialogVisible" 
      width="400px"
    >
      <el-form :model="topForm" label-width="100px">
        <el-form-item label="置顶时长" required>
          <el-input-number 
            v-model="topForm.duration" 
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
          <el-button @click="topDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleTopSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Top, Bottom, Lock, Delete } from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'
import { renderMarkdown } from '@/utils/markdown'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const detailVisible = ref(false)
const topDialogVisible = ref(false)
const postList = ref([])
const currentPost = ref(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  authorName: '',
  category: '',
  status: null,
  timeRange: null
})

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 置顶表单
const topForm = reactive({
  duration: 24
})

// 获取帖子列表
const fetchPosts = async () => {
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

    const data = await communityApi.getPostList(params)
    postList.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchPosts()
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.authorName = ''
  searchForm.category = ''
  searchForm.status = null
  searchForm.timeRange = null
  pagination.pageNum = 1
  fetchPosts()
}

// 查看详情
const handleView = async (row) => {
  try {
    const data = await communityApi.getPostById(row.id)
    currentPost.value = data
    detailVisible.value = true
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败')
  }
}

// 置顶帖子
const handleTop = (row) => {
  currentPost.value = row
  topForm.duration = 24
  topDialogVisible.value = true
}

// 提交置顶
const handleTopSubmit = async () => {
  try {
    submitLoading.value = true
    await communityApi.topPost(currentPost.value.id, {
      duration: topForm.duration
    })
    ElMessage.success('置顶成功')
    topDialogVisible.value = false
    await fetchPosts()
  } catch (error) {
    console.error('置顶失败:', error)
    ElMessage.error('置顶失败')
  } finally {
    submitLoading.value = false
  }
}

// 取消置顶
const handleCancelTop = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消置顶帖子 "${row.title}" 吗？`,
      '取消置顶确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await communityApi.cancelTop(row.id)
    ElMessage.success('取消置顶成功')
    await fetchPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消置顶失败:', error)
      ElMessage.error('取消置顶失败')
    }
  }
}

// 下架帖子
const handleDisable = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要下架帖子 "${row.title}" 吗？`,
      '下架确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await communityApi.disablePost(row.id)
    ElMessage.success('下架成功')
    await fetchPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
      ElMessage.error('下架失败')
    }
  }
}

// 删除帖子
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除帖子 "${row.title}" 吗？删除后无法恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await communityApi.deletePost(row.id)
    ElMessage.success('删除成功')
    await fetchPosts()
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
  fetchPosts()
}

const handlePageSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchPosts()
}

// 格式化帖子内容（渲染Markdown）
const formatContent = (content) => {
  if (!content) return ''
  return renderMarkdown(content)
}

// 页面挂载
onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.post-management {
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

.post-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-header {
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.meta-info {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.detail-content {
  line-height: 1.6;
  color: #606266;
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