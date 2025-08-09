<template>
  <div class="video-player-page">
    <!-- 顶部导航 -->
    <header class="player-header">
      <el-icon @click="handleBack" class="back-icon"><ArrowLeft /></el-icon>
      <h3>{{ videoTitle }}</h3>
      <div></div>
    </header>

    <!-- 视频播放器容器 -->
    <div class="video-container" ref="containerRef">
      <video 
        ref="videoRef"
        :src="videoUrl"
        class="video-player"
        controls
        controlsList="nodownload noremoteplayback"
        disablePictureInPicture
        @loadedmetadata="onVideoLoaded"
        @timeupdate="onTimeUpdate"
        @ended="onVideoEnded"
        @pause="onVideoPause"
        @play="onVideoPlay"
        @seeking="onSeeking"
        @seeked="onSeeked"
        preload="metadata"
      >
        您的浏览器不支持视频播放
      </video>

      <!-- 防快进提示 -->
      <div v-if="showSeekWarning" class="seek-warning">
        <el-alert
          title="请完整观看视频，不能快进"
          type="warning"
          :closable="false"
          center
        />
      </div>

      <!-- 全屏按钮 -->
      <div class="fullscreen-btn" @click="toggleFullscreen">
        <el-icon v-if="!isFullscreen"><FullScreen /></el-icon>
        <el-icon v-else><Aim /></el-icon>
      </div>

      <!-- 观看进度提示 -->
      <div class="progress-info">
        <div class="watch-progress">
          观看进度: {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
        </div>
        <div v-if="nearEnd" class="near-end-tip">
          <el-tag type="success">即将完成观看！</el-tag>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <el-loading :visible="true" text="加载视频中..." />
    </div>

    <!-- 完成观看提示 -->
    <el-dialog
      v-model="showCompleteDialog"
      title="观看完成"
      width="300px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div style="text-align: center;">
        <el-icon style="font-size: 48px; color: #67c23a; margin-bottom: 16px;">
          <CircleCheck />
        </el-icon>
        <p>恭喜您完成了此视频的观看！</p>
      </div>
      <template #footer>
        <div style="text-align: center;">
          <el-button type="primary" @click="goBackToList">返回列表</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, CircleCheck, FullScreen, Aim } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getVideoList, recordVideoWatch } from '../api/video'

const router = useRouter()
const route = useRoute()

const videoRef = ref()
const containerRef = ref()
const loading = ref(true)
const videoUrl = ref('')
const videoTitle = ref('')
const duration = ref(0)
const currentTime = ref(0)
const maxWatchedTime = ref(0) // 用户观看过的最大时间点
const showSeekWarning = ref(false)
const nearEnd = ref(false)
const hasRecorded = ref(false) // 是否已记录完成状态
const showCompleteDialog = ref(false)
const videoId = ref('')
const isFullscreen = ref(false) // 是否全屏状态

// 获取视频信息
const loadVideoInfo = async () => {
  try {
    const res = await getVideoList()
    if (res.code === 200 && res.data) {
      const video = res.data.find(v => v.id === route.params.id)
      if (video) {
        videoUrl.value = video.url
        videoTitle.value = video.title
        videoId.value = video.id
      } else {
        ElMessage.error('视频不存在')
        router.go(-1)
      }
    }
  } catch (error) {
    ElMessage.error('加载视频失败')
    router.go(-1)
  } finally {
    loading.value = false
  }
}

// 视频加载完成
const onVideoLoaded = () => {
  if (videoRef.value) {
    duration.value = videoRef.value.duration
    loading.value = false
  }
}

// 时间更新
const onTimeUpdate = () => {
  if (videoRef.value) {
    currentTime.value = videoRef.value.currentTime
    
    // 更新用户观看过的最大时间点
    if (currentTime.value > maxWatchedTime.value) {
      maxWatchedTime.value = currentTime.value
    }
    
    // 检查是否接近结尾（最后30秒）
    const remaining = duration.value - currentTime.value
    if (remaining <= 30 && remaining > 0 && !hasRecorded.value) {
      nearEnd.value = true
      recordWatchComplete()
    }
  }
}

// 视频播放结束
const onVideoEnded = () => {
  if (!hasRecorded.value) {
    recordWatchComplete()
  }
  showCompleteDialog.value = true
}

// 暂停视频
const onVideoPause = () => {
  // 可以在这里添加暂停相关逻辑
}

// 播放视频
const onVideoPlay = () => {
  // 可以在这里添加播放相关逻辑
}

// 用户试图快进
const onSeeking = () => {
  if (videoRef.value) {
    const seekTime = videoRef.value.currentTime
    
    // 如果用户试图快进到未观看的部分，阻止并回退
    if (seekTime > maxWatchedTime.value + 2) { // 允许2秒的缓冲
      showSeekWarning.value = true
      setTimeout(() => {
        showSeekWarning.value = false
      }, 3000)
    }
  }
}

// 快进操作完成
const onSeeked = () => {
  if (videoRef.value) {
    const seekTime = videoRef.value.currentTime
    
    // 如果快进到了未观看的部分，强制回退到最大观看时间点
    if (seekTime > maxWatchedTime.value + 2) {
      videoRef.value.currentTime = maxWatchedTime.value
      ElMessage.warning('请按顺序观看视频，不能快进')
    }
  }
}

// 记录观看完成
const recordWatchComplete = async () => {
  if (hasRecorded.value) return
  
  try {
    hasRecorded.value = true
    const res = await recordVideoWatch({
      videoId: videoId.value,
      isFinished: 1
    })
    
    if (res.code === 200) {
      console.log('观看记录已保存')
    }
  } catch (error) {
    console.error('保存观看记录失败:', error)
    hasRecorded.value = false
  }
}

// 处理返回
const handleBack = () => {
  if (videoRef.value && !videoRef.value.paused) {
    ElMessageBox.confirm(
      '视频正在播放，确定要离开吗？离开后需要重新观看。',
      '确认离开',
      {
        confirmButtonText: '确定离开',
        cancelButtonText: '继续观看',
        type: 'warning',
      }
    ).then(() => {
      goBackToList()
    })
  } else {
    goBackToList()
  }
}

// 返回列表
const goBackToList = () => {
  router.push('/video-watch')
}

// 格式化时间
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '00:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = Math.floor(seconds % 60)
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 全屏切换功能
const toggleFullscreen = () => {
  if (!containerRef.value) return
  
  if (!isFullscreen.value) {
    // 进入全屏
    if (containerRef.value.requestFullscreen) {
      containerRef.value.requestFullscreen()
    } else if (containerRef.value.webkitRequestFullscreen) {
      containerRef.value.webkitRequestFullscreen()
    } else if (containerRef.value.mozRequestFullScreen) {
      containerRef.value.mozRequestFullScreen()
    } else if (containerRef.value.msRequestFullscreen) {
      containerRef.value.msRequestFullscreen()
    }
  } else {
    // 退出全屏
    if (document.exitFullscreen) {
      document.exitFullscreen()
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen()
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen()
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen()
    }
  }
}

// 监听全屏状态变化
const onFullscreenChange = () => {
  isFullscreen.value = !!(
    document.fullscreenElement ||
    document.webkitFullscreenElement ||
    document.mozFullScreenElement ||
    document.msFullscreenElement
  )
}

// 页面初始化
onMounted(async () => {
  videoTitle.value = route.query.title || '视频播放'
  await loadVideoInfo()
  
  // 禁用右键菜单
  document.addEventListener('contextmenu', (e) => e.preventDefault())
  
  // 添加全屏状态监听
  document.addEventListener('fullscreenchange', onFullscreenChange)
  document.addEventListener('webkitfullscreenchange', onFullscreenChange)
  document.addEventListener('mozfullscreenchange', onFullscreenChange)
  document.addEventListener('msfullscreenchange', onFullscreenChange)
  
  // 禁用某些键盘快捷键
  document.addEventListener('keydown', (e) => {
    // 禁用 F12, Ctrl+Shift+I 等开发者工具快捷键
    if (
      e.key === 'F12' ||
      (e.ctrlKey && e.shiftKey && e.key === 'I') ||
      (e.ctrlKey && e.shiftKey && e.key === 'J') ||
      (e.ctrlKey && e.key === 'u')
    ) {
      e.preventDefault()
    }
    
    // 禁用空格键暂停（可选）
    if (e.key === ' ' && e.target === document.body) {
      e.preventDefault()
    }
  })
})

onUnmounted(() => {
  // 清理事件监听器
  document.removeEventListener('contextmenu', (e) => e.preventDefault())
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  document.removeEventListener('webkitfullscreenchange', onFullscreenChange)
  document.removeEventListener('mozfullscreenchange', onFullscreenChange)
  document.removeEventListener('msfullscreenchange', onFullscreenChange)
})
</script>

<style scoped>
.video-player-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #000;
  position: relative;
}

.player-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  z-index: 1000;
}

.player-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: white;
}

.back-icon {
  cursor: pointer;
  font-size: 1.2rem;
  color: white;
}

.video-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding-top: 48px;
}

.video-player {
  max-width: 100%;
  max-height: 100%;
  width: 100%;
  height: auto;
}

.seek-warning {
  position: absolute;
  top: 70px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.fullscreen-btn {
  position: absolute;
  top: 70px;
  left: 16px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
  z-index: 100;
}

.fullscreen-btn:hover {
  background: rgba(0, 0, 0, 0.9);
}

.fullscreen-btn .el-icon {
  font-size: 1.2rem;
}

.progress-info {
  position: absolute;
  top: 70px;
  right: 16px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 0.9rem;
}

.near-end-tip {
  margin-top: 8px;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

/* 隐藏视频控制栏的某些控件 */
.video-player::-webkit-media-controls-timeline {
  /* 可以通过CSS隐藏进度条，但这可能影响用户体验 */
}

/* 响应式设计 */
@media (max-width: 768px) {
  .video-player {
    height: 100%;
    object-fit: contain;
  }
  
  .fullscreen-btn {
    top: 60px;
    left: 8px;
    padding: 6px;
  }
  
  .fullscreen-btn .el-icon {
    font-size: 1rem;
  }
  
  .progress-info {
    top: 60px;
    right: 8px;
    font-size: 0.8rem;
    padding: 6px 8px;
  }
}

/* 全屏时的样式 */
.video-player-page:fullscreen {
  background: #000;
}

.video-player-page:fullscreen .player-header {
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
}

.video-player-page:fullscreen .video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.video-player-page:fullscreen .fullscreen-btn {
  top: 60px;
}

.video-player-page:fullscreen .progress-info {
  top: 60px;
}

/* WebKit 浏览器全屏样式 */
.video-player-page:-webkit-full-screen {
  background: #000;
}

.video-player-page:-webkit-full-screen .video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* Mozilla 浏览器全屏样式 */
.video-player-page:-moz-full-screen {
  background: #000;
}

.video-player-page:-moz-full-screen .video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

@media (orientation: landscape) and (max-width: 1024px) {
  .player-header {
    height: 40px;
  }
  
  .video-container {
    padding-top: 40px;
  }
  
  .fullscreen-btn {
    top: 52px;
    left: 12px;
    padding: 6px;
  }
  
  .progress-info {
    top: 52px;
    right: 12px;
    font-size: 0.8rem;
    padding: 6px 8px;
  }
}
</style> 