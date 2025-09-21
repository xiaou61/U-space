<template>
  <div class="version-history">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">📅 版本更新历史</h1>
        <p class="page-subtitle">了解我们的产品发展历程和功能演进轨迹</p>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <div class="search-section">
      <el-card shadow="never">
        <el-row :gutter="20" align="middle">
          <el-col :xs="24" :sm="8" :md="6">
            <el-input
              v-model="searchForm.keyword"
              placeholder="🔍 搜索版本号、标题或描述"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <el-select
              v-model="searchForm.updateType"
              placeholder="更新类型"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部类型" :value="null" />
              <el-option label="🚀 重大更新" :value="1" />
              <el-option label="✨ 功能更新" :value="2" />
              <el-option label="🐛 修复更新" :value="3" />
              <el-option label="📋 其他" :value="4" />
            </el-select>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <el-select
              v-model="searchForm.isFeatured"
              placeholder="筛选类型"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部版本" :value="null" />
              <el-option label="💎 重点推荐" :value="1" />
              <el-option label="📝 普通版本" :value="0" />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="4" :md="3">
            <el-button type="primary" @click="handleSearch" :loading="loading">
              搜索
            </el-button>
          </el-col>
          <el-col :xs="24" :sm="12" :md="7">
            <div class="stats-info">
              <el-icon><InfoFilled /></el-icon>
              <span>共 {{ pagination.total }} 个版本</span>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

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
  Search, InfoFilled, Calendar, View, Document, Share
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

// 搜索表单
const searchForm = reactive({
  keyword: '',
  updateType: null,
  isFeatured: null
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
      ...searchForm,
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

// 搜索
const handleSearch = () => {
  loadVersionList()
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
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  color: white;
}

.page-title {
  font-size: 2.5rem;
  margin: 0 0 10px 0;
  font-weight: 700;
}

.page-subtitle {
  font-size: 1.1rem;
  margin: 0;
  opacity: 0.9;
}

.search-section {
  margin-bottom: 30px;
}

.stats-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.loading-container,
.empty-container {
  padding: 60px 20px;
}

.timeline-container {
  position: relative;
  padding-left: 40px;
}

.timeline-container::before {
  content: '';
  position: absolute;
  left: 15px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #e6f7ff 0%, #1890ff 50%, #e6f7ff 100%);
}

.timeline-item {
  position: relative;
  margin-bottom: 40px;
  display: flex;
  align-items: flex-start;
}

.timeline-item.featured .version-card {
  border: 2px solid #faad14;
  background: linear-gradient(145deg, #fff7e6 0%, #ffffff 100%);
}

.timeline-marker {
  position: absolute;
  left: -32px;
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
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
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

/* 响应式设计 */
@media (max-width: 768px) {
  .version-history {
    padding: 10px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .timeline-container {
    padding-left: 25px;
  }
  
  .timeline-marker {
    left: -20px;
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
  .page-header {
    padding: 30px 15px;
    margin: 0 -10px 30px;
    border-radius: 0;
  }
  
  .version-number-line {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style> 