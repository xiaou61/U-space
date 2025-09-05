<template>
  <div class="collections-page">
    <!-- 返回按钮 -->
    <div class="back-section">
      <el-button @click="goBack" :icon="Back">
        返回社区
      </el-button>
    </div>

    <!-- 我的收藏 -->
    <div class="content-section">
      <el-card shadow="never" class="content-card">
        <template #header>
          <div class="content-header">
            <span>我的收藏</span>
            <span class="total-count" v-if="total > 0">共 {{ total }} 个收藏</span>
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
                <span class="post-author">{{ post.authorName }}</span>
                <span class="post-date">{{ formatDate(post.createTime) }}</span>
                <el-tag v-if="post.categoryName" type="info" size="small" class="category-tag">
                  {{ post.categoryName }}
                </el-tag>
              </div>
              <div class="collect-date">
                收藏于 {{ formatDate(post.collectTime) }}
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
            </div>
            
            <div class="post-actions" @click.stop>
              <el-button size="small" @click="goToPostDetail(post)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button 
                type="danger" 
                plain
                size="small" 
                @click="uncollectPost(post)"
              >
                <el-icon><Star /></el-icon>
                取消收藏
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="!loading && postList.length === 0" class="empty-state">
          <el-empty description="暂无收藏的帖子">
            <el-button type="primary" @click="goToCommunity">去社区逛逛</el-button>
          </el-empty>
        </div>

        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination 
            v-model:current-page="queryParams.pageNum" 
            v-model:page-size="queryParams.pageSize"
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
import { ElMessage, ElMessageBox } from 'element-plus'
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
  pageNum: 1,
  pageSize: 10
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 获取收藏列表
const fetchCollections = async () => {
  loading.value = true
  try {
    const response = await communityApi.getUserCollections(queryParams)
    postList.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 分页大小改变
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchCollections()
}

// 当前页改变
const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  fetchCollections()
}

// 取消收藏帖子
const uncollectPost = async (post) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏这个帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await communityApi.uncollectPost(post.id)
    ElMessage.success('取消收藏成功')
    
    // 从列表中移除
    const index = postList.value.findIndex(p => p.id === post.id)
    if (index > -1) {
      postList.value.splice(index, 1)
      total.value = Math.max(0, total.value - 1)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
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
  fetchCollections()
})
</script>

<style scoped>
.collections-page {
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

.post-author {
  color: #00b894;
  font-weight: 500;
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
  .collections-page {
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