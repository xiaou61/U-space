<template>
  <div class="video-watch-page">
    <!-- 顶部导航 -->
    <header class="page-header">
      <el-icon @click="goBack" class="back-icon"><ArrowLeft /></el-icon>
      <h3>入学必看视频</h3>
      <div></div>
    </header>

    <!-- 视频列表 -->
    <div class="video-content">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="videoList.length > 0" class="video-list">
        <div 
          v-for="video in videoList" 
          :key="video.id" 
          class="video-item"
          @click="playVideo(video)"
        >
          <div class="video-cover">
            <!-- 观看状态标签 -->
            <div class="watch-status">
              <el-tag 
                v-if="video.isWatched" 
                type="success" 
                size="small"
                class="status-tag"
              >
                <el-icon><CircleCheck /></el-icon>
                已观看
              </el-tag>
              <el-tag 
                v-else 
                type="warning" 
                size="small"
                class="status-tag"
              >
                <el-icon><Clock /></el-icon>
                未观看
              </el-tag>
            </div>
            
            <el-image
              v-if="video.coverUrl"
              :src="video.coverUrl"
              fit="cover"
              class="cover-image"
            />
            <div v-else class="default-cover">
              <el-icon class="play-icon"><VideoPlay /></el-icon>
            </div>
            <div class="play-overlay">
              <el-icon class="play-btn"><VideoPlay /></el-icon>
            </div>
          </div>
          
          <div class="video-info">
            <h3 class="video-title">{{ video.title }}</h3>
            <p class="video-description">{{ video.description || '暂无简介' }}</p>
            <div class="video-meta">
              <span v-if="video.duration" class="duration">
                <el-icon><Clock /></el-icon>
                {{ formatDuration(video.duration) }}
              </span>
              <span class="upload-time">{{ formatDate(video.createAt) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="no-videos">
        <el-empty description="暂无必看视频">
          <el-button type="primary" @click="loadVideos">刷新</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, VideoPlay, Clock, CircleCheck } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getVideoList } from '../api/video'

const router = useRouter()
const loading = ref(false)
const videoList = ref([])

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 加载视频列表
const loadVideos = async () => {
  loading.value = true
  try {
    const res = await getVideoList()
    if (res.code === 200 && res.data) {
      videoList.value = res.data || []
    } else {
      ElMessage.error('加载视频列表失败')
    }
  } catch (error) {
    console.error('加载视频列表失败:', error)
    ElMessage.error('加载视频列表失败')
  } finally {
    loading.value = false
  }
}

// 播放视频
const playVideo = (video) => {
  router.push({
    name: 'VideoPlayer',
    params: { id: video.id },
    query: { title: video.title }
  })
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
  loadVideos()
})
</script>

<style scoped>
.video-watch-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6fa;
}

.page-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  z-index: 1000;
}

.page-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.back-icon {
  cursor: pointer;
  font-size: 1.2rem;
  color: #409eff;
}

.video-content {
  flex: 1;
  padding: 16px;
  padding-top: 64px;
  padding-bottom: 80px;
  overflow-y: auto;
}

.loading-container {
  padding: 20px;
}

.video-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.video-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.video-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.video-cover {
  position: relative;
  height: 200px;
  background: #f0f0f0;
  overflow: hidden;
}

.watch-status {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(4px);
  font-weight: 500;
  border: none !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.status-tag.el-tag--success {
  color: #67c23a !important;
}

.status-tag.el-tag--warning {
  color: #e6a23c !important;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.default-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.default-cover .play-icon {
  font-size: 3rem;
}

.play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.7);
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.video-item:hover .play-overlay {
  background: rgba(0, 0, 0, 0.8);
}

.play-btn {
  color: white;
  font-size: 1.5rem;
}

.video-info {
  padding: 16px;
}

.video-title {
  margin: 0 0 8px 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.video-description {
  margin: 0 0 12px 0;
  font-size: 0.9rem;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #999;
}

.duration {
  display: flex;
  align-items: center;
  gap: 4px;
}

.no-videos {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .video-cover {
    height: 160px;
  }
  
  .watch-status {
    top: 8px;
    right: 8px;
  }
  
  .status-tag {
    font-size: 0.75rem;
    padding: 2px 6px;
    gap: 2px;
  }
  
  .video-title {
    font-size: 1rem;
  }
  
  .video-description {
    font-size: 0.85rem;
  }
  
  .play-overlay {
    width: 50px;
    height: 50px;
  }
  
  .play-btn {
    font-size: 1.2rem;
  }
}
</style> 