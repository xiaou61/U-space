<template>
  <div class="moments-page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索动态内容..."
        clearable
        @keyup.enter="handleSearch"
        @clear="handleClearSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch" :loading="searching">
            搜索
          </el-button>
        </template>
      </el-input>
    </el-card>

    <!-- 热门动态轮播 -->
    <el-card v-if="!isSearchMode && hotMoments.length > 0" class="hot-moments-card" v-loading="loadingHot">
      <div class="hot-header">
        <el-icon class="fire-icon"><StarFilled /></el-icon>
        <span class="hot-title">热门动态</span>
      </div>
      <el-carousel height="100px" :interval="5000" indicator-position="none">
        <el-carousel-item v-for="hot in hotMoments" :key="hot.id">
          <div class="hot-item" @click="() => {}">
            <div class="hot-content">
              <span class="hot-user">{{ hot.userNickname }}:</span>
              <span class="hot-text">{{ hot.content }}</span>
            </div>
            <div class="hot-stats">
              <span><el-icon><Star /></el-icon> {{ hot.likeCount }}</span>
              <span><el-icon><ChatDotRound /></el-icon> {{ hot.commentCount }}</span>
              <span><el-icon><View /></el-icon> {{ hot.viewCount }}</span>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>
    
    <!-- 发布动态区域 -->
    <el-card class="publish-card">
      <div class="publish-section">
        <el-input
          v-model="publishForm.content"
          type="textarea"
          placeholder="分享此刻的想法..."
          :rows="3"
          maxlength="100"
          show-word-limit
          @focus="showPublishDialog"
          readonly
        />
      </div>
    </el-card>

    <!-- 动态列表 -->
    <div v-loading="loading" class="moments-list">
      <el-empty v-if="!loading && momentList.length === 0" description="暂无动态，快来发布第一条吧！" />
      
      <div v-for="moment in momentList" :key="moment.id" class="moment-item">
        <el-card class="moment-card">
          <!-- 用户信息 -->
          <div class="user-header">
            <el-avatar 
              :size="45" 
              :src="moment.userAvatar" 
              class="clickable-avatar"
              @click="goToUserProfile(moment.userId)"
            >
              {{ moment.userNickname?.charAt(0) }}
            </el-avatar>
            <div class="user-info" @click="goToUserProfile(moment.userId)">
              <div class="user-name">{{ moment.userNickname }}</div>
              <div class="publish-time">{{ formatTime(moment.createTime) }}</div>
            </div>
            <el-dropdown v-if="moment.canDelete" trigger="click">
              <el-button type="text" size="small">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="deleteMoment(moment)">删除动态</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 动态内容 -->
          <div class="moment-content">
            <p class="content-text">{{ moment.content }}</p>
            
            <!-- 图片展示 -->
            <div v-if="moment.images && moment.images.length" class="images-grid">
              <div 
                v-for="(image, index) in moment.images" 
                :key="index" 
                class="image-item"
                @click="previewImage(moment.images, index)"
              >
                <el-image
                  :src="image"
                  fit="cover"
                  class="moment-image"
                  loading="lazy"
                />
              </div>
            </div>
          </div>

          <!-- 互动区域 -->
          <div class="interaction-section">
            <div class="interaction-stats">
              <span v-if="moment.likeCount > 0" class="stat-item">
                <el-icon><Star /></el-icon>
                {{ moment.likeCount }}
              </span>
              <span v-if="moment.commentCount > 0" class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ moment.commentCount }}
              </span>
              <span v-if="moment.viewCount > 0" class="stat-item">
                <el-icon><View /></el-icon>
                {{ moment.viewCount }}
              </span>
            </div>
            
            <div class="interaction-buttons">
              <el-button 
                :type="moment.isLiked ? 'primary' : ''" 
                text 
                @click="toggleLikeMoment(moment)"
                :loading="moment.liking"
              >
                <el-icon><Star /></el-icon>
                {{ moment.isLiked ? '已赞' : '点赞' }}
              </el-button>
              <el-button text @click="showCommentDialog(moment)">
                <el-icon><ChatDotRound /></el-icon>
                评论
              </el-button>
              <el-button 
                :type="moment.isFavorited ? 'warning' : ''" 
                text 
                @click="toggleFavoriteMoment(moment)"
                :loading="moment.favoriting"
              >
                <el-icon v-if="moment.isFavorited"><StarFilled /></el-icon>
                <el-icon v-else><Star /></el-icon>
                {{ moment.isFavorited ? '已收藏' : '收藏' }}
              </el-button>
            </div>

            <!-- 最新评论 -->
            <div v-if="moment.recentComments && moment.recentComments.length" class="recent-comments">
              <div 
                v-for="comment in moment.recentComments" 
                :key="comment.id" 
                class="comment-item"
              >
                <span class="comment-user">{{ comment.userNickname }}:</span>
                <span class="comment-content">{{ comment.content }}</span>
                <el-button 
                  v-if="comment.canDelete" 
                  type="danger" 
                  text 
                  size="small"
                  @click="deleteComment(comment, moment)"
                >
                  删除
                </el-button>
              </div>
              <el-button 
                v-if="moment.commentCount > moment.recentComments.length" 
                text 
                size="small" 
                @click="viewAllComments(moment)"
              >
                查看全部 {{ moment.commentCount }} 条评论
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more" ref="loadMoreRef">
        <div v-if="loadingMore" class="loading-text">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载中...
        </div>
        <div v-else class="more-text">下拉加载更多</div>
      </div>
      <div v-else-if="momentList.length > 0" class="no-more">
        没有更多了
      </div>
    </div>

    <!-- 发布动态对话框 -->
    <PublishDialog
      v-model="publishDialogVisible"
      @published="handlePublished"
    />

    <!-- 评论对话框 -->
    <CommentDialog
      v-model="commentDialogVisible"
      :moment="currentMoment"
      @commented="handleCommented"
    />

    <!-- 查看全部评论对话框 -->
    <AllCommentsDialog
      v-model="allCommentsDialogVisible"
      :moment="currentMoment"
      @comment-deleted="handleCommentDeleted"
    />

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="imageViewerVisible"
      :url-list="previewImages"
      :initial-index="previewIndex"
      @close="closeImageViewer"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElImageViewer } from 'element-plus'
import { Star, ChatDotRound, MoreFilled, StarFilled, View, Search, Loading } from '@element-plus/icons-vue'
import { 
  getMomentList, 
  toggleLike, 
  deleteMoment as deleteMomentApi, 
  deleteComment as deleteCommentApi,
  toggleFavorite,
  getHotMoments,
  searchMoments 
} from '@/api/moment'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { formatRelativeTime } from '@/utils/timeUtil'
import PublishDialog from './components/PublishDialog.vue'
import CommentDialog from './components/CommentDialog.vue'
import AllCommentsDialog from './components/AllCommentsDialog.vue'

const userStore = useUserStore()
const router = useRouter()

// 数据状态
const loading = ref(false)
const loadingMore = ref(false)
const momentList = ref([])
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)

// 热门动态
const hotMoments = ref([])
const loadingHot = ref(false)

// 搜索相关
const searchKeyword = ref('')
const isSearchMode = ref(false)
const searching = ref(false)

// 发布相关
const publishForm = ref({
  content: ''
})
const publishDialogVisible = ref(false)

// 评论相关
const commentDialogVisible = ref(false)
const allCommentsDialogVisible = ref(false)
const currentMoment = ref(null)

// 图片预览
const imageViewerVisible = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)

// 无限滚动
const loadMoreRef = ref(null)
let observer = null

// 加载热门动态
const loadHotMoments = async () => {
  loadingHot.value = true
  try {
    const result = await getHotMoments({ limit: 3 })
    hotMoments.value = result || []
  } catch (error) {
    console.error('加载热门动态失败', error)
  } finally {
    loadingHot.value = false
  }
}

// 加载动态列表
const loadMomentList = async (page = 1) => {
  if (page === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const params = {
      pageNum: page,
      pageSize: pageSize.value
    }

    const result = await getMomentList(params)
    const newMoments = result.records.map(moment => ({
      ...moment,
      images: moment.images || [],
      liking: false,
      favoriting: false
    }))

    if (page === 1) {
      momentList.value = newMoments
    } else {
      momentList.value.push(...newMoments)
    }

    hasMore.value = result.records.length === pageSize.value
  } catch (error) {
    ElMessage.error('加载失败：' + error.message)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}



// 加载更多
const loadMore = () => {
  currentPage.value++
  if (isSearchMode.value) {
    // 搜索模式下加载更多
    loadMoreSearch()
  } else {
    loadMomentList(currentPage.value)
  }
}

// 搜索模式加载更多
const loadMoreSearch = async () => {
  loadingMore.value = true
  try {
    const params = {
      keyword: searchKeyword.value.trim(),
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    const result = await searchMoments(params)
    const newMoments = result.records.map(moment => ({
      ...moment,
      images: moment.images || [],
      liking: false,
      favoriting: false
    }))
    
    momentList.value.push(...newMoments)
    hasMore.value = newMoments.length === pageSize.value
  } catch (error) {
    ElMessage.error('加载失败：' + error.message)
  } finally {
    loadingMore.value = false
  }
}

// 显示发布对话框
const showPublishDialog = () => {
  publishDialogVisible.value = true
}

// 处理发布成功
const handlePublished = () => {
  publishDialogVisible.value = false
  currentPage.value = 1
  loadMomentList(1)
}

// 点赞切换
const toggleLikeMoment = async (moment) => {
  moment.liking = true
  try {
    const isLiked = await toggleLike(moment.id)
    moment.isLiked = isLiked
    moment.likeCount += isLiked ? 1 : -1
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    moment.liking = false
  }
}

// 删除动态
const deleteMoment = async (moment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条动态吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteMomentApi(moment.id)
    ElMessage.success('删除成功')
    
    // 从列表中移除
    const index = momentList.value.findIndex(item => item.id === moment.id)
    if (index > -1) {
      momentList.value.splice(index, 1)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 显示评论对话框
const showCommentDialog = (moment) => {
  currentMoment.value = moment
  commentDialogVisible.value = true
}

// 处理评论成功
const handleCommented = (comment) => {
  const moment = currentMoment.value
  if (moment) {
    moment.commentCount++
    if (!moment.recentComments) {
      moment.recentComments = []
    }
    moment.recentComments.unshift(comment)
    // 保持最新评论只显示3条
    if (moment.recentComments.length > 3) {
      moment.recentComments = moment.recentComments.slice(0, 3)
    }
  }
}

// 删除评论
const deleteComment = async (comment, moment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteCommentApi(comment.id)
    ElMessage.success('删除成功')
    
    // 从最新评论中移除
    const commentIndex = moment.recentComments.findIndex(item => item.id === comment.id)
    if (commentIndex > -1) {
      moment.recentComments.splice(commentIndex, 1)
      moment.commentCount--
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 查看全部评论
const viewAllComments = (moment) => {
  currentMoment.value = moment
  allCommentsDialogVisible.value = true
}

// 处理评论删除（从全部评论对话框触发）
const handleCommentDeleted = (commentId) => {
  const moment = currentMoment.value
  if (moment) {
    moment.commentCount--
    // 如果删除的评论在最新评论中，也要移除
    const index = moment.recentComments.findIndex(item => item.id === commentId)
    if (index > -1) {
      moment.recentComments.splice(index, 1)
    }
  }
}

// 图片预览
const previewImage = (images, index) => {
  previewImages.value = images
  previewIndex.value = index
  imageViewerVisible.value = true
}

const closeImageViewer = () => {
  imageViewerVisible.value = false
}

// 收藏切换
const toggleFavoriteMoment = async (moment) => {
  moment.favoriting = true
  try {
    const isFavorited = await toggleFavorite(moment.id)
    moment.isFavorited = isFavorited
    moment.favoriteCount += isFavorited ? 1 : -1
    ElMessage.success(isFavorited ? '收藏成功' : '取消收藏')
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    moment.favoriting = false
  }
}

// 搜索动态
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  searching.value = true
  isSearchMode.value = true
  currentPage.value = 1
  
  try {
    const params = {
      keyword: searchKeyword.value.trim(),
      pageNum: 1,
      pageSize: pageSize.value
    }
    
    const result = await searchMoments(params)
    const newMoments = result.records.map(moment => ({
      ...moment,
      images: moment.images || [],
      liking: false,
      favoriting: false
    }))
    
    momentList.value = newMoments
    hasMore.value = newMoments.length === pageSize.value
  } catch (error) {
    ElMessage.error('搜索失败：' + error.message)
  } finally {
    searching.value = false
  }
}

// 清空搜索
const handleClearSearch = () => {
  isSearchMode.value = false
  searchKeyword.value = ''
  currentPage.value = 1
  loadMomentList(1)
}

// 跳转到用户主页
const goToUserProfile = (userId) => {
  router.push(`/moments/user/${userId}`)
}

// 相对时间格式化
const formatTime = (time) => {
  return formatRelativeTime(time)
}

// 初始化无限滚动
const initInfiniteScroll = () => {
  if (!loadMoreRef.value) return
  
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      // 当"加载更多"元素进入视口且有更多数据且不在加载中
      if (entry.isIntersecting && hasMore.value && !loadingMore.value) {
        loadMore()
      }
    })
  }, {
    rootMargin: '100px' // 提前100px触发加载
  })
  
  observer.observe(loadMoreRef.value)
}

onMounted(() => {
  loadHotMoments()
  loadMomentList()
  
  // 延迟初始化无限滚动，等待DOM渲染
  nextTick(() => {
    setTimeout(() => {
      initInfiniteScroll()
    }, 1000)
  })
})

onUnmounted(() => {
  // 清理observer
  if (observer) {
    observer.disconnect()
  }
})
</script>

<style scoped>
.moments-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

/* 搜索栏样式 */
.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

/* 热门动态样式 */
.hot-moments-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
}

.hot-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.fire-icon {
  font-size: 20px;
  color: #ffd700;
  margin-right: 8px;
}

.hot-title {
  font-size: 16px;
  font-weight: 600;
}

.hot-item {
  cursor: pointer;
  padding: 10px 15px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  transition: all 0.3s;
}

.hot-item:hover {
  background: rgba(255, 255, 255, 0.2);
}

.hot-content {
  margin-bottom: 8px;
  font-size: 14px;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-user {
  font-weight: 600;
  margin-right: 5px;
}

.hot-stats {
  display: flex;
  gap: 15px;
  font-size: 12px;
  opacity: 0.9;
}

.hot-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.publish-card {
  margin-bottom: 20px;
}

.publish-section {
  cursor: pointer;
}

.moments-list {
  min-height: 400px;
}

.moment-item {
  margin-bottom: 20px;
}

.moment-card {
  transition: all 0.3s;
  border-radius: 12px;
  overflow: hidden;
}

.moment-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  flex: 1;
  margin-left: 12px;
  cursor: pointer;
}

.user-info:hover .user-name {
  color: #409eff;
}

.clickable-avatar {
  cursor: pointer;
  transition: transform 0.2s;
}

.clickable-avatar:hover {
  transform: scale(1.05);
}

.user-name {
  font-weight: 500;
  color: #303133;
  font-size: 16px;
  transition: color 0.2s;
}

.publish-time {
  color: #999;
  font-size: 12px;
  margin-top: 2px;
}

.moment-content {
  margin-bottom: 15px;
}

.content-text {
  margin: 0 0 15px 0;
  line-height: 1.6;
  color: #333;
}

.images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 15px;
  max-width: 360px;
}

.image-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  width: 116px;
  height: 116px;
}

.moment-image {
  width: 100%;
  height: 100%;
}

.interaction-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.interaction-stats {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
  color: #909399;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.interaction-buttons {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.recent-comments {
  background: #f7f7f7;
  border-radius: 8px;
  padding: 10px;
  margin-top: 10px;
}

.comment-item {
  margin-bottom: 8px;
  font-size: 14px;
  line-height: 1.4;
}

.comment-item:last-child {
  margin-bottom: 0;
}

.comment-user {
  font-weight: 500;
  color: #1890ff;
  margin-right: 5px;
}

.comment-content {
  color: #333;
}

.load-more {
  text-align: center;
  padding: 30px 20px;
}

.loading-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.more-text {
  color: #c0c4cc;
  font-size: 13px;
}

.no-more {
  text-align: center;
  padding: 20px;
  color: #c0c4cc;
  font-size: 13px;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-textarea__inner) {
  resize: none;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .images-grid {
    max-width: 300px;
  }
  
  .image-item {
    width: 96px;
    height: 96px;
  }
}
</style> 