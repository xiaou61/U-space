<template>
  <div class="user-profile-page">
    <!-- 用户信息卡片 -->
    <el-card class="user-info-card" v-loading="loadingUserInfo">
      <div class="user-header">
        <el-avatar :size="80" :src="userInfo.avatar">
          {{ userInfo.nickname?.charAt(0) }}
        </el-avatar>
        <div class="user-details">
          <h2 class="user-name">{{ userInfo.nickname }}</h2>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.totalMoments || 0 }}</span>
              <span class="stat-label">动态</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.totalLikes || 0 }}</span>
              <span class="stat-label">获赞</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.totalComments || 0 }}</span>
              <span class="stat-label">评论</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 动态列表 -->
    <div class="moments-section">
      <div class="section-title">
        <span>Ta的动态</span>
      </div>

      <div v-loading="loading" class="moments-list">
        <el-empty v-if="!loading && momentList.length === 0" description="暂无动态" />
        
        <div v-for="moment in momentList" :key="moment.id" class="moment-item">
          <el-card class="moment-card">
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

            <!-- 互动信息 -->
            <div class="interaction-footer">
              <span class="time-text">{{ formatTime(moment.createTime) }}</span>
              <div class="interaction-stats">
                <span v-if="moment.likeCount > 0" class="stat-badge">
                  <el-icon><Star /></el-icon>
                  {{ moment.likeCount }}
                </span>
                <span v-if="moment.commentCount > 0" class="stat-badge">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ moment.commentCount }}
                </span>
                <span v-if="moment.viewCount > 0" class="stat-badge">
                  <el-icon><View /></el-icon>
                  {{ moment.viewCount }}
                </span>
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
    </div>

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
import { useRoute } from 'vue-router'
import { ElMessage, ElImageViewer } from 'element-plus'
import { Star, ChatDotRound, View } from '@element-plus/icons-vue'
import { getUserMomentList, getUserMomentInfo } from '@/api/moment'
import { formatRelativeTime } from '@/utils/timeUtil'

const route = useRoute()

// 用户ID
const userId = ref(null)

// 用户信息
const userInfo = ref({})
const loadingUserInfo = ref(false)

// 动态列表
const loading = ref(false)
const loadingMore = ref(false)
const momentList = ref([])
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(20)

// 图片预览
const imageViewerVisible = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)

// 加载用户信息
const loadUserInfo = async () => {
  loadingUserInfo.value = true
  try {
    const result = await getUserMomentInfo({ userId: userId.value })
    userInfo.value = result
  } catch (error) {
    ElMessage.error('加载用户信息失败：' + error.message)
  } finally {
    loadingUserInfo.value = false
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
      userId: userId.value,
      pageNum: page,
      pageSize: pageSize.value
    }

    const result = await getUserMomentList(params)
    const newMoments = result.records || []

    if (page === 1) {
      momentList.value = newMoments
    } else {
      momentList.value.push(...newMoments)
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
  loadMomentList(currentPage.value)
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

onMounted(() => {
  // 从路由获取用户ID
  userId.value = Number(route.params.userId || route.query.userId)
  
  if (!userId.value) {
    ElMessage.error('用户ID不存在')
    return
  }
  
  loadUserInfo()
  loadMomentList()
})
</script>

<style scoped>
.user-profile-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

/* 用户信息卡片 */
.user-info-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-details {
  flex: 1;
}

.user-name {
  margin: 0 0 15px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.user-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 动态列表 */
.moments-section {
  margin-top: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding: 15px 0;
  border-bottom: 2px solid #409eff;
  margin-bottom: 20px;
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
}

.moment-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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

.interaction-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.time-text {
  font-size: 12px;
  color: #c0c4cc;
}

.interaction-stats {
  display: flex;
  gap: 15px;
}

.stat-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.load-more {
  text-align: center;
  padding: 20px;
}
</style>

