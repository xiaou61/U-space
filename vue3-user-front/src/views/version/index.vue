<template>
  <div class="version-history">
    <!-- 版本时间轴 -->
    <div class="timeline-section">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="versionList.length === 0" class="empty-container">
        <el-empty description="暂无版本历史记录" />
      </div>
      
      <div v-else class="timeline-container">
        <div 
          v-for="(version, index) in versionList" 
          :key="version.id"
          class="timeline-item"
          :class="{ 'featured': version.isFeatured === 1 }"
        >
          <div class="timeline-marker">
            <div 
              class="marker-dot" 
              :class="getUpdateTypeClass(version.updateType)"
            >
              <span class="marker-emoji">{{ getUpdateTypeIcon(version.updateType) }}</span>
            </div>
          </div>
          
          <div class="timeline-content">
            <el-card 
              class="version-card" 
              shadow="hover"
              :body-style="{ padding: '24px' }"
            >
              <!-- 版本头部信息 -->
              <div class="version-header">
                <div class="version-info">
                  <div class="version-number-line">
                    <el-tag 
                      type="primary" 
                      size="large" 
                      class="version-tag"
                    >
                      {{ version.versionNumber }}
                    </el-tag>
                    <el-tag 
                      v-if="version.isFeatured === 1"
                      type="warning" 
                      size="small"
                      class="featured-tag"
                    >
                      💎 重点推荐
                    </el-tag>
                  </div>
                  <h3 class="version-title">{{ version.title }}</h3>
                  <div class="version-meta">
                    <el-tag 
                      :type="getUpdateTypeTagType(version.updateType)" 
                      size="small"
                      class="type-tag"
                    >
                      <span class="type-emoji">{{ getUpdateTypeIcon(version.updateType) }}</span> {{ version.updateTypeName }}
                    </el-tag>
                    <span class="release-time">
                      <el-icon><Calendar /></el-icon>
                      {{ formatReleaseTime(version.releaseTime) }}
                    </span>
                    <span class="view-count">
                      <el-icon><View /></el-icon>
                      {{ version.viewCount || 0 }} 次查看
                    </span>
                  </div>
                </div>
              </div>

              <!-- 版本描述 -->
              <div v-if="version.description" class="version-description">
                <p>{{ version.description }}</p>
              </div>

              <!-- 版本操作 -->
              <div class="version-actions">
                <el-button 
                  v-if="version.prdUrl" 
                  type="primary" 
                  @click="handleViewPrd(version)"
                  class="action-button"
                >
                  <el-icon><Document /></el-icon>
                  查看PRD详情
                </el-button>
                <el-button 
                  type="info" 
                  @click="handleShare(version)"
                  class="action-button"
                >
                  <el-icon><Share /></el-icon>
                  分享
                </el-button>
              </div>

              <!-- 版本索引指示器 -->
              <div class="version-index">
                #{{ pagination.total - (pagination.pageNum - 1) * pagination.pageSize - index }}
              </div>
            </el-card>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more-container">
        <el-button 
          type="primary" 
          size="large"
          @click="loadMore"
          :loading="loadingMore"
          class="load-more-btn"
        >
          📄 加载更多版本
        </el-button>
      </div>

      <!-- 回到顶部 -->
      <el-backtop :right="40" :bottom="40" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Calendar, View, Document, Share
} from '@element-plus/icons-vue'
import { versionApi } from '@/api/version'

// 响应式数据
const loading = ref(false)
const loadingMore = ref(false)
const versionList = ref([])

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})



// 计算属性
const hasMore = computed(() => {
  const totalPages = Math.ceil(pagination.total / pagination.pageSize)
  return pagination.pageNum < totalPages
})

// 生命周期
onMounted(() => {
  loadVersionList()
})

// 加载版本列表
const loadVersionList = async (isLoadMore = false) => {
  try {
    if (isLoadMore) {
      loadingMore.value = true
    } else {
      loading.value = true
      pagination.pageNum = 1
      versionList.value = []
    }
    
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }

    const data = await versionApi.getVersionTimeline(params)
    
    if (isLoadMore) {
      versionList.value.push(...(data.records || []))
    } else {
      versionList.value = data.records || []
    }
    
    pagination.total = data.total || 0
  } catch (error) {
    console.error('加载版本列表失败:', error)
    ElMessage.error('加载版本列表失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}



// 加载更多
const loadMore = () => {
  pagination.pageNum++
  loadVersionList(true)
}

// 查看PRD详情
const handleViewPrd = (version) => {
  if (version.prdUrl) {
    // 记录查看次数
    versionApi.recordViewCount(version.id).catch(console.error)
    // 打开PRD链接
    window.open(version.prdUrl, '_blank')
  }
}

// 分享版本
const handleShare = async (version) => {
  const shareData = {
    title: `${version.versionNumber} - ${version.title}`,
    text: version.description || '查看最新版本更新',
    url: window.location.href
  }

  try {
    if (navigator.share) {
      await navigator.share(shareData)
    } else {
      // 降级到复制链接
      await navigator.clipboard.writeText(window.location.href)
      ElMessage.success('链接已复制到剪贴板')
    }
  } catch (error) {
    console.error('分享失败:', error)
    // 尝试复制链接作为备选方案
    try {
      await navigator.clipboard.writeText(window.location.href)
      ElMessage.success('链接已复制到剪贴板')
    } catch (clipboardError) {
      ElMessage.warning('分享功能暂时不可用')
    }
  }
}

// 格式化发布时间
const formatReleaseTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 如果是今天
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    if (hours < 1) {
      const minutes = Math.floor(diff / (60 * 1000))
      return `${minutes} 分钟前`
    }
    return `${hours} 小时前`
  }
  
  // 如果是一周内
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000))
    return `${days} 天前`
  }
  
  // 其他情况显示完整日期
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 获取更新类型图标
const getUpdateTypeIcon = (type) => {
  const iconMap = {
    1: '🚀', // 重大更新
    2: '✨', // 功能更新  
    3: '🐛', // 修复更新
    4: '📋'  // 其他
  }
  return iconMap[type] || '📋'
}

// 获取更新类型CSS类
const getUpdateTypeClass = (type) => {
  const classMap = {
    1: 'major-update',    // 重大更新
    2: 'feature-update',  // 功能更新
    3: 'fix-update',      // 修复更新
    4: 'other-update'     // 其他
  }
  return classMap[type] || 'other-update'
}

// 获取更新类型标签样式
const getUpdateTypeTagType = (type) => {
  const tagMap = {
    1: 'danger',   // 重大更新
    2: 'primary',  // 功能更新
    3: 'warning',  // 修复更新
    4: 'info'      // 其他
  }
  return tagMap[type] || 'info'
}
</script>

<style scoped>
.version-history {
  padding: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.version-history::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(24, 144, 255, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(24, 144, 255, 0.03) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.version-history > * {
  position: relative;
  z-index: 1;
}



.timeline-section {
  padding: 20px 0;
}

.loading-container,
.empty-container {
  padding: 60px 20px;
}

.timeline-container {
  position: relative;
  padding: 60px 20px;
  max-width: 1000px;
  margin: 0 auto;
  padding-left: 60px;
  opacity: 1;
  animation: container-fade-in 1.2s ease-out 0.3s both;
}

@keyframes container-fade-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.timeline-container::before {
  content: '';
  position: absolute;
  left: 35px;
  top: 60px;
  bottom: 60px;
  width: 4px;
  background: linear-gradient(180deg, 
    transparent 0%, 
    #e6f7ff 8%, 
    #1890ff 20%, 
    #40a9ff 50%, 
    #1890ff 80%, 
    #e6f7ff 92%, 
    transparent 100%
  );
  border-radius: 3px;
  box-shadow: 
    0 0 15px rgba(24, 144, 255, 0.4),
    inset 0 0 3px rgba(255, 255, 255, 0.5);
}

.timeline-item {
  position: relative;
  margin-bottom: 40px;
  display: flex;
  align-items: flex-start;
  opacity: 1;
  transform: translateY(0);
}

.timeline-item.featured .version-card {
  border: 2px solid #faad14;
  background: linear-gradient(145deg, #fff7e6 0%, #ffffff 100%);
}

.timeline-marker {
  position: absolute;
  left: -52px;
  z-index: 2;
}

.marker-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.marker-dot::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  transform: scale(0);
  transition: transform 0.3s ease;
}

.marker-dot:hover::before {
  transform: scale(1);
}

.marker-emoji {
  font-size: 16px;
  line-height: 1;
}

.marker-dot.major-update {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
}

.marker-dot.feature-update {
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
}

.marker-dot.fix-update {
  background: linear-gradient(135deg, #feca57, #ff9ff3);
}

.marker-dot.other-update {
  background: linear-gradient(135deg, #a8e6cf, #88d8c0);
}

.timeline-content {
  flex: 1;
  margin-left: 20px;
}

.version-card {
  position: relative;
  transition: all 0.3s ease;
  border-radius: 16px;
  overflow: hidden;
}

.version-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  z-index: 2;
}

.version-card:hover .marker-dot {
  transform: scale(1.3);
  animation: none;
  box-shadow: 0 0 0 8px rgba(24, 144, 255, 0.2);
}

.version-header {
  margin-bottom: 16px;
}

.version-number-line {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.version-tag {
  font-size: 16px;
  font-weight: 600;
  padding: 8px 16px;
}

.featured-tag {
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.version-title {
  font-size: 1.5rem;
  margin: 0 0 12px 0;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1.4;
}

.version-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.type-tag {
  font-weight: 500;
}

.type-emoji {
  font-size: 12px;
  margin-right: 4px;
}

.release-time,
.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
}

.version-description {
  margin: 20px 0;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border-left: 4px solid #1890ff;
}

.version-description p {
  margin: 0;
  color: #555;
  line-height: 1.6;
  font-size: 15px;
}

.version-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.action-button {
  border-radius: 8px;
  font-weight: 500;
}

.version-index {
  position: absolute;
  top: 16px;
  right: 16px;
  background: rgba(0, 0, 0, 0.05);
  color: #666;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.load-more-container {
  text-align: center;
  margin: 40px 0;
  padding: 20px;
}

.load-more-btn {
  padding: 16px 32px;
  font-size: 16px;
  border-radius: 50px;
  box-shadow: 0 4px 15px rgba(24, 144, 255, 0.3);
}

/* 滚动动画效果 */
@keyframes timeline-fade-in {
  from {
    opacity: 0;
    transform: translateY(50px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes line-draw {
  from {
    height: 0;
  }
  to {
    height: 100%;
  }
}

@keyframes marker-pulse {
  from {
    transform: scale(0.8);
    opacity: 0.8;
  }
  to {
    transform: scale(1.1);
    opacity: 1;
  }
}

@keyframes card-float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

/* 支持现代浏览器的动画 */
@supports (animation-timeline: view()) {
  @media (prefers-reduced-motion: no-preference) {
    .timeline-item {
      opacity: 1;
      animation: timeline-fade-in linear;
      animation-timeline: view();
      animation-range: entry 0% entry 70%;
    }
    
    .marker-dot {
      animation: marker-pulse ease-in-out;
      animation-timeline: view();
      animation-range: entry 0% entry 50%;
    }
  }
  
  @media (prefers-reduced-motion: reduce) {
    .timeline-item {
      opacity: 1;
      transform: none;
      animation: none;
    }
    
    .marker-dot {
      animation: none;
    }
    
    .timeline-container {
      animation: none;
      opacity: 1;
    }
  }
}

/* 降级方案：不支持view()的浏览器 */
@supports not (animation-timeline: view()) {
  .timeline-item {
    opacity: 1;
    transform: none;
    animation: none;
  }
  
  .timeline-item:nth-child(even) {
    animation: timeline-fade-in 0.8s ease-out 0.1s both;
  }
  
  .timeline-item:nth-child(odd) {
    animation: timeline-fade-in 0.8s ease-out 0.3s both;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .version-history {
    padding: 0;
  }
  
  .timeline-container {
    padding: 40px 15px;
    padding-left: 45px;
  }
  
  .timeline-container::before {
    left: 20px;
    width: 3px;
  }
  
  .timeline-marker {
    left: -37px;
  }
  
  .marker-dot {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }
  
  .timeline-content {
    margin-left: 15px;
  }
  
  .version-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .version-actions {
    flex-direction: column;
  }
  
  .action-button {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .version-number-line {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .timeline-item {
    opacity: 1;
    transform: none;
  }
  
  .version-card:hover {
    transform: translateY(-4px) scale(1.01);
  }
}
</style> 