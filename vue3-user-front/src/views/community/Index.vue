<template>
  <div class="community-index">
    <!-- 头部导航 -->
    <div class="header">
      <div class="header-content">
        <div class="header-left">
          <h2>技术社区</h2>
          <p>分享经验，交流技术，共同成长</p>
        </div>
        <div class="header-right">
          <el-button type="success" @click="showCreateDialog" :icon="Edit">
            发表帖子
          </el-button>
          <el-button type="primary" @click="goToMyCollection" :icon="Star">
            我的收藏
          </el-button>
          <el-button type="info" @click="goToMyPosts" :icon="Document">
            我的帖子
          </el-button>
          <el-button @click="goBack" :icon="Back">
            返回首页
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-card shadow="never" class="search-card">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索帖子..." 
              size="large"
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
            <el-select 
              v-model="searchCategory" 
              placeholder="按分类筛选..." 
              size="large"
              clearable
              @change="handleSearch"
              style="width: 100%"
            >
              <template #prefix>
                <el-icon><Flag /></el-icon>
              </template>
              <el-option
                v-for="category in categoryList"
                :key="category.id"
                :label="category.name"
                :value="category.name"
              >
                <span>{{ category.name }}</span>
                <span style="color: #8492a6; font-size: 13px; margin-left: 8px">{{ category.description }}</span>
              </el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" size="large" @click="handleSearch" style="width: 100%">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 帖子列表 -->
    <div class="content-section">
      <el-card shadow="never" class="content-card">
        <template #header>
          <div class="content-header">
            <span>社区帖子</span>
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
                <span class="post-author">{{ post.authorName }}</span>
                <span class="post-date">{{ formatDate(post.createTime) }}</span>
                <el-tag v-if="post.category" type="info" size="small" class="category-tag">
                  {{ post.category }}
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
              <div class="stat-item" :class="{ 'liked': post.isLiked }">
                <el-icon><StarFilled /></el-icon>
                <span>{{ post.likeCount || 0 }} 点赞</span>
              </div>
              <div class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ post.commentCount || 0 }} 评论</span>
              </div>
              <div class="stat-item" :class="{ 'collected': post.isCollected }">
                <el-icon><Star /></el-icon>
                <span>{{ post.collectCount || 0 }} 收藏</span>
              </div>
            </div>
            
            <div class="post-actions" @click.stop>
              <el-button 
                :type="post.isLiked ? 'primary' : ''" 
                :plain="!post.isLiked"
                size="small" 
                @click="toggleLike(post)"
              >
                <el-icon><StarFilled /></el-icon>
                {{ post.isLiked ? '已赞' : '点赞' }}
              </el-button>
              <el-button 
                :type="post.isCollected ? 'warning' : ''" 
                :plain="!post.isCollected"
                size="small" 
                @click="toggleCollect(post)"
              >
                <el-icon><Star /></el-icon>
                {{ post.isCollected ? '已收藏' : '收藏' }}
              </el-button>
              <el-button size="small" @click="goToPostDetail(post)">
                <el-icon><ChatDotRound /></el-icon>
                评论
              </el-button>
            </div>
          </div>
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, Star, Back, Edit, View, StarFilled, ChatDotRound, Document, Flag
} from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'

const router = useRouter()

// 响应式数据
const searchKeyword = ref('')
const searchCategory = ref('')
const loading = ref(false)
const postList = ref([])
const categoryList = ref([])
const total = ref(0)


// 查询参数
const queryParams = reactive({
  keyword: '',
  page: 1,
  size: 10
})



// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 初始化社区
const initCommunity = async () => {
  try {
    await communityApi.init()
  } catch (error) {
    console.error('社区初始化失败:', error)
  }
}

// 获取帖子列表
const fetchPostList = async () => {
  loading.value = true
  try {
    const response = await communityApi.getPostList(queryParams)
    postList.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.keyword = searchKeyword.value
  queryParams.category = searchCategory.value
  queryParams.page = 1
  fetchPostList()
}

// 分页大小改变
const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.page = 1
  fetchPostList()
}

// 当前页改变
const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchPostList()
}

// 跳转到创建帖子页面
const showCreateDialog = () => {
  router.push('/community/create')
}



// 切换点赞状态
const toggleLike = async (post) => {
  try {
    if (post.isLiked) {
      await communityApi.unlikePost(post.id)
      post.likeCount = Math.max(0, post.likeCount - 1)
      post.isLiked = false
      ElMessage.success('取消点赞成功')
    } else {
      await communityApi.likePost(post.id)
      post.likeCount = post.likeCount + 1
      post.isLiked = true
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 切换收藏状态
const toggleCollect = async (post) => {
  try {
    if (post.isCollected) {
      await communityApi.uncollectPost(post.id)
      post.collectCount = Math.max(0, post.collectCount - 1)
      post.isCollected = false
      ElMessage.success('取消收藏成功')
    } else {
      await communityApi.collectPost(post.id)
      post.collectCount = post.collectCount + 1
      post.isCollected = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 跳转到帖子详情
const goToPostDetail = (post) => {
  router.push(`/community/posts/${post.id}`)
}

// 跳转到我的收藏
const goToMyCollection = () => {
  router.push('/community/collections')
}

// 跳转到我的帖子
const goToMyPosts = () => {
  router.push('/community/my-posts')
}

// 返回首页
const goBack = () => {
  router.push('/')
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await communityApi.getEnabledCategories()
    categoryList.value = response || []
  } catch (error) {
    console.error('加载分类列表失败:', error)
  }
}

// 初始化
onMounted(async () => {
  await loadCategories()
  await initCommunity()
  await fetchPostList()
})
</script>

<style scoped>
.community-index {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  color: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 185, 148, 0.3);
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.header-left p {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.search-section, .content-section {
  margin-bottom: 20px;
}

.search-card, .content-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.search-card {
  padding: 8px;
}

/* 帖子列表样式 */
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
  transition: color 0.3s;
}

.stat-item.liked {
  color: #f56565;
}

.stat-item.collected {
  color: #f6ad55;
}

.post-actions {
  display: flex;
  gap: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .community-index {
    padding: 10px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .header-right {
    flex-wrap: wrap;
    justify-content: center;
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