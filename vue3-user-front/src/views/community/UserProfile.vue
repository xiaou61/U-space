<template>
  <div class="user-profile-page">
    <!-- 返回按钮 -->
    <div class="back-section">
      <el-button @click="goBack" :icon="Back">
        返回社区
      </el-button>
    </div>

    <!-- 用户信息卡片 -->
    <div v-if="userProfile" class="profile-section">
      <el-card shadow="never" class="profile-card">
        <div class="profile-header">
          <el-avatar :size="80" :src="userProfile.avatar || defaultAvatar">
            {{ userProfile.username?.charAt(0) }}
          </el-avatar>
          <div class="profile-info">
            <h2 class="username">{{ userProfile.username }}</h2>
            <p v-if="userProfile.bio" class="bio">{{ userProfile.bio }}</p>
            <p v-else class="bio">这个人很懒，什么都没写~</p>
          </div>
        </div>

        <!-- 统计数据 -->
        <div class="stats-section">
          <div class="stat-item">
            <div class="stat-value">{{ userProfile.postCount || 0 }}</div>
            <div class="stat-label">发帖数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userProfile.commentCount || 0 }}</div>
            <div class="stat-label">评论数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userProfile.likeCount || 0 }}</div>
            <div class="stat-label">获赞数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userProfile.collectCount || 0 }}</div>
            <div class="stat-label">收藏数</div>
          </div>
        </div>

        <!-- 活跃标签 -->
        <div v-if="userProfile.activeTags && userProfile.activeTags.length > 0" class="active-tags-section">
          <div class="section-title">
            <el-icon><Discount /></el-icon>
            <span>活跃标签</span>
          </div>
          <div class="tags-list">
            <el-tag 
              v-for="tag in userProfile.activeTags" 
              :key="tag.id"
              type="success"
              effect="plain"
              size="default"
              class="tag-item"
            >
              # {{ tag.name }} ({{ tag.count || 0 }})
            </el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 用户帖子列表 -->
    <div class="posts-section">
      <el-card shadow="never" class="posts-card">
        <template #header>
          <div class="posts-header">
            <div>
              <el-icon style="margin-right: 8px"><Document /></el-icon>
              <span>TA的帖子</span>
            </div>
            <span class="posts-count" v-if="postsTotal > 0">共 {{ postsTotal }} 篇</span>
          </div>
        </template>

        <div v-loading="postsLoading" class="posts-list">
          <div 
            v-for="post in postsList" 
            :key="post.id"
            class="post-item"
            @click="goToPostDetail(post)"
          >
            <div class="post-header">
              <h3 class="post-title">{{ post.title }}</h3>
              <span class="post-date">{{ formatDate(post.createTime) }}</span>
            </div>
            
            <!-- AI摘要 -->
            <div v-if="post.aiSummary" class="post-summary">
              <el-icon style="color: #409eff"><MagicStick /></el-icon>
              <span class="summary-text">{{ post.aiSummary }}</span>
            </div>
            
            <p class="post-content">{{ post.content }}</p>
            
            <!-- 帖子标签 -->
            <div v-if="post.tags && post.tags.length > 0" class="post-tags">
              <el-tag 
                v-for="tag in post.tags" 
                :key="tag.id"
                size="small"
                type="success"
                effect="plain"
              >
                # {{ tag.name }}
              </el-tag>
            </div>
            
            <div class="post-stats">
              <div class="stat">
                <el-icon><View /></el-icon>
                <span>{{ post.viewCount || 0 }}</span>
              </div>
              <div class="stat">
                <el-icon><StarFilled /></el-icon>
                <span>{{ post.likeCount || 0 }}</span>
              </div>
              <div class="stat">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ post.commentCount || 0 }}</span>
              </div>
              <div class="stat">
                <el-icon><Star /></el-icon>
                <span>{{ post.collectCount || 0 }}</span>
              </div>
            </div>
          </div>

          <el-empty v-if="!postsLoading && postsList.length === 0" description="暂无帖子" />
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="postsTotal > 0">
          <el-pagination 
            v-model:current-page="postsQueryParams.pageNum" 
            v-model:page-size="postsQueryParams.pageSize"
            :page-sizes="[10, 20, 30]"
            :total="postsTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="handlePostsSizeChange"
            @current-change="handlePostsCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Back, Document, View, StarFilled, Star, ChatDotRound, Discount, MagicStick
} from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'

const route = useRoute()
const router = useRouter()

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 响应式数据
const userProfile = ref(null)
const postsList = ref([])
const postsTotal = ref(0)
const postsLoading = ref(false)

// 帖子查询参数
const postsQueryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }
  // 小于1小时
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  }
  // 小于1天
  if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  }
  // 小于7天
  if (diff < 604800000) {
    return Math.floor(diff / 86400000) + '天前'
  }
  
  return date.toLocaleDateString('zh-CN')
}

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    const userId = route.params.userId
    userProfile.value = await communityApi.getUserProfile(userId)
  } catch (error) {
    ElMessage.error('获取用户信息失败')
    goBack()
  }
}

// 获取用户帖子列表
const fetchUserPosts = async () => {
  postsLoading.value = true
  try {
    const userId = route.params.userId
    const response = await communityApi.getUserPostList(userId, postsQueryParams)
    postsList.value = response.records || []
    postsTotal.value = response.total || 0
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
  } finally {
    postsLoading.value = false
  }
}

// 跳转到帖子详情
const goToPostDetail = (post) => {
  router.push(`/community/posts/${post.id}`)
}

// 分页大小改变
const handlePostsSizeChange = (size) => {
  postsQueryParams.pageSize = size
  postsQueryParams.pageNum = 1
  fetchUserPosts()
}

// 当前页改变
const handlePostsCurrentChange = (page) => {
  postsQueryParams.pageNum = page
  fetchUserPosts()
}

// 返回社区
const goBack = () => {
  router.push('/community')
}

// 初始化
onMounted(async () => {
  await fetchUserProfile()
  await fetchUserPosts()
})
</script>

<style scoped>
.user-profile-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.back-section {
  margin-bottom: 20px;
}

.profile-section, .posts-section {
  margin-bottom: 20px;
}

.profile-card, .posts-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 用户信息区域 */
.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.profile-info {
  flex: 1;
}

.username {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.bio {
  margin: 0;
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
}

/* 统计数据区域 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 24px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4f8 100%);
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 185, 148, 0.2);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #00b894;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

/* 活跃标签区域 */
.active-tags-section {
  padding-top: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-item {
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

/* 帖子列表区域 */
.posts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.posts-count {
  color: #909399;
  font-size: 14px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.post-item {
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s;
}

.post-item:hover {
  border-color: #00b894;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 185, 148, 0.15);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 16px;
}

.post-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  line-height: 1.4;
}

.post-date {
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
}

/* AI摘要样式 */
.post-summary {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.summary-text {
  flex: 1;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}

.post-content {
  margin: 0 0 12px 0;
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.post-stats {
  display: flex;
  gap: 20px;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-profile-page {
    padding: 10px;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .post-header {
    flex-direction: column;
    gap: 8px;
  }
  
  .post-stats {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>

