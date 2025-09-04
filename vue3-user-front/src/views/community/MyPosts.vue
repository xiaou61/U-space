<template>
  <div class="my-posts-page">
    <!-- 返回按钮 -->
    <div class="back-section">
      <el-button @click="goBack" :icon="Back">
        返回社区
      </el-button>
    </div>

    <!-- 我的帖子 -->
    <div class="content-section">
      <el-card shadow="never" class="content-card">
        <template #header>
          <div class="content-header">
            <span>我的帖子</span>
            <span class="total-count" v-if="total > 0">共 {{ total }} 个帖子</span>
          </div>
        </template>

        <div v-loading="loading" class="posts-list">
          <div 
            v-for="post in postList" 
            :key="post.id"
            class="post-card"
            @click="goToPostDetail(post)"
          >
            <div class="post-header">
              <div class="post-meta">
                <span class="post-date">发表于 {{ formatDate(post.createTime) }}</span>
                <el-tag v-if="post.category" type="info" size="small" class="category-tag">
                  {{ post.category }}
                </el-tag>
                <el-tag 
                  :type="post.status === 1 ? 'success' : post.status === 0 ? 'warning' : 'danger'" 
                  size="small"
                >
                  {{ getStatusText(post.status) }}
                </el-tag>
              </div>
            </div>
            
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-content">{{ post.content }}</p>
            
            <div class="post-stats">
              <div class="stat-item">
                <el-icon><View /></el-icon>
                <span>{{ post.viewCount || 0 }} 浏览</span>
              </div>
                             <div class="stat-item">
                 <el-icon><StarFilled /></el-icon>
                <span>{{ post.likeCount || 0 }} 点赞</span>
              </div>
              <div class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ post.commentCount || 0 }} 评论</span>
              </div>
              <div class="stat-item">
                <el-icon><Star /></el-icon>
                <span>{{ post.collectCount || 0 }} 收藏</span>
              </div>
            </div>
            
            <div class="post-actions" @click.stop>
              <el-button size="small" @click="goToPostDetail(post)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="!loading && postList.length === 0" class="empty-state">
          <el-empty description="暂无发表的帖子">
            <el-button type="primary" @click="goToCommunity">去发表帖子</el-button>
          </el-empty>
        </div>

        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination 
            v-model:current-page="queryParams.page" 
            v-model:page-size="queryParams.size"
            :page-sizes="[10, 20, 30, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Back, View, StarFilled, ChatDotRound, Star
} from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const postList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 获取帖子状态文本
const getStatusText = (status) => {
  switch (status) {
    case 1:
      return '已发布'
    case 0:
      return '待审核'
    case -1:
      return '已删除'
    default:
      return '未知'
  }
}

// 获取我的帖子列表
const fetchMyPosts = async () => {
  loading.value = true
  try {
    const response = await communityApi.getUserPosts(queryParams)
    postList.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error('获取我的帖子失败')
  } finally {
    loading.value = false
  }
}

// 分页大小改变
const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.page = 1
  fetchMyPosts()
}

// 当前页改变
const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchMyPosts()
}

// 跳转到帖子详情
const goToPostDetail = (post) => {
  router.push(`/community/posts/${post.id}`)
}

// 返回社区
const goBack = () => {
  router.push('/community')
}

// 跳转到社区
const goToCommunity = () => {
  router.push('/community')
}

// 初始化
onMounted(() => {
  fetchMyPosts()
})
</script>

<style scoped>
.my-posts-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.back-section {
  margin-bottom: 20px;
}

.content-section {
  margin-bottom: 20px;
}

.content-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-count {
  color: #909399;
  font-size: 14px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.post-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.post-card:hover {
  border-color: #00b894;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 185, 148, 0.15);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #909399;
  font-size: 14px;
}

.collect-date {
  color: #f6ad55;
  font-size: 13px;
}

.post-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.post-content {
  margin: 0 0 16px 0;
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-stats {
  display: flex;
  justify-content: flex-start;
  gap: 24px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.post-actions {
  display: flex;
  gap: 12px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-posts-page {
    padding: 10px;
  }
  
  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .post-stats {
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .post-actions {
    flex-wrap: wrap;
  }
}
</style> 