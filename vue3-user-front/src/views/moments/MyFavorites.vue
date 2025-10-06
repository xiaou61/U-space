<template>
  <div class="my-favorites-page">
    <el-page-header @back="goBack" content="我的收藏" class="page-header" />

    <div v-loading="loading" class="favorites-list">
      <el-empty v-if="!loading && favoriteList.length === 0" description="暂无收藏" />
      
      <div v-for="moment in favoriteList" :key="moment.id" class="moment-item">
        <el-card class="moment-card">
          <!-- 用户信息 -->
          <div class="user-header">
            <el-avatar :size="40" :src="moment.userAvatar">
              {{ moment.userNickname?.charAt(0) }}
            </el-avatar>
            <div class="user-info">
              <div class="user-name">{{ moment.userNickname }}</div>
              <div class="publish-time">{{ formatTime(moment.createTime) }}</div>
            </div>
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
                type="warning" 
                text 
                @click="toggleFavoriteMoment(moment)"
                :loading="moment.favoriting"
              >
                <el-icon><StarFilled /></el-icon>
                取消收藏
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <el-button @click="loadMore" :loading="loadingMore">
          加载更多
        </el-button>
      </div>
    </div>

    <!-- 评论对话框 -->
    <CommentDialog
      v-model="commentDialogVisible"
      :moment="currentMoment"
      @commented="handleCommented"
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElImageViewer } from 'element-plus'
import { Star, ChatDotRound, View, StarFilled } from '@element-plus/icons-vue'
import { getMyFavorites, toggleLike, toggleFavorite } from '@/api/moment'
import { formatRelativeTime } from '@/utils/timeUtil'
import CommentDialog from './components/CommentDialog.vue'

const router = useRouter()

// 数据状态
const loading = ref(false)
const loadingMore = ref(false)
const favoriteList = ref([])
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(20)

// 评论相关
const commentDialogVisible = ref(false)
const currentMoment = ref(null)

// 图片预览
const imageViewerVisible = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)

// 加载收藏列表
const loadFavoriteList = async (page = 1) => {
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

    const result = await getMyFavorites(params)
    const newMoments = result.records.map(moment => ({
      ...moment,
      images: moment.images || [],
      liking: false,
      favoriting: false
    }))

    if (page === 1) {
      favoriteList.value = newMoments
    } else {
      favoriteList.value.push(...newMoments)
    }

    hasMore.value = newMoments.length === pageSize.value
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
  loadFavoriteList(currentPage.value)
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

// 取消收藏
const toggleFavoriteMoment = async (moment) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    moment.favoriting = true
    await toggleFavorite(moment.id)
    
    // 从列表中移除
    const index = favoriteList.value.findIndex(item => item.id === moment.id)
    if (index > -1) {
      favoriteList.value.splice(index, 1)
    }
    
    ElMessage.success('取消收藏成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + error.message)
    }
  } finally {
    moment.favoriting = false
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

// 相对时间格式化
const formatTime = (time) => {
  return formatRelativeTime(time)
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadFavoriteList()
})
</script>

<style scoped>
.my-favorites-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.favorites-list {
  min-height: 400px;
}

.moment-item {
  margin-bottom: 20px;
}

.moment-card {
  transition: all 0.3s;
  border-radius: 12px;
}

.moment-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  flex: 1;
  margin-left: 12px;
}

.user-name {
  font-weight: 500;
  color: #303133;
  font-size: 15px;
}

.publish-time {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.moment-content {
  margin-bottom: 15px;
}

.content-text {
  margin: 0 0 15px 0;
  line-height: 1.6;
  color: #606266;
}

.images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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
  margin-bottom: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.interaction-buttons {
  display: flex;
  gap: 20px;
}

.load-more {
  text-align: center;
  padding: 20px;
}
</style>

