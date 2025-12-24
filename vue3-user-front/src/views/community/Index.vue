<template>
  <div class="community-container">
    <!-- 主体内容区域 - 三栏布局 -->
    <div class="community-main">
      <!-- 左侧边栏 -->
      <aside class="sidebar sidebar-left">
        <!-- 快捷操作 -->
        <div class="sidebar-card quick-actions">
          <button class="action-btn primary" @click="showCreateDialog">
            <el-icon><Edit /></el-icon>
            <span>发表帖子</span>
          </button>
          <button class="action-btn" @click="goToMyPosts">
            <el-icon><Document /></el-icon>
            <span>我的帖子</span>
          </button>
          <button class="action-btn" @click="goToMyCollection">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </button>
        </div>

        <!-- 分类导航 -->
        <div class="sidebar-card categories-nav">
          <div class="card-header">
            <el-icon><Menu /></el-icon>
            <span>分类导航</span>
          </div>
          <ul class="nav-list">
            <li 
              class="nav-item" 
              :class="{ active: selectedCategoryId === null }"
              @click="selectCategory(null)"
            >
              <span class="nav-icon">📚</span>
              <span class="nav-text">全部帖子</span>
              <span class="nav-count">{{ total }}</span>
            </li>
            <li 
              v-for="category in categoryList" 
              :key="category.id"
              class="nav-item"
              :class="{ active: selectedCategoryId === category.id }"
              @click="selectCategory(category.id)"
            >
              <span class="nav-icon">{{ getCategoryIcon(category.name) }}</span>
              <span class="nav-text">{{ category.name }}</span>
              <span class="nav-count">{{ category.postCount || 0 }}</span>
            </li>
          </ul>
        </div>

        <!-- 热门标签 -->
        <div class="sidebar-card hot-tags-card" v-if="tagList.length > 0">
          <div class="card-header">
            <el-icon><PriceTag /></el-icon>
            <span>热门标签</span>
          </div>
          <div class="tags-cloud">
            <span 
              v-for="tag in tagList.slice(0, 15)" 
              :key="tag.id"
              class="cloud-tag"
              :class="{ active: selectedTagId === tag.id }"
              @click="selectTag(tag.id)"
            >
              # {{ tag.name }}
            </span>
          </div>
        </div>
      </aside>

      <!-- 中间内容区 -->
      <main class="main-content">
        <!-- 搜索栏 + 排序选项 -->
        <div class="content-header-card">
          <!-- 搜索框 -->
          <div class="search-bar">
            <div class="search-wrapper">
              <el-icon class="search-icon"><Search /></el-icon>
              <input 
                v-model="searchKeyword" 
                type="text" 
                class="search-input" 
                placeholder="搜索帖子、标签、作者..."
                @keyup.enter="handleSearch"
                @focus="showHotKeywords = true"
              />
              <button class="search-btn" @click="handleSearch">搜索</button>
            </div>
            <!-- 热门搜索词 -->
            <div v-if="showHotKeywords && hotKeywords.length > 0" class="hot-keywords-dropdown">
              <span class="hot-label">🔥 热门搜索</span>
              <div class="hot-tags">
                <span 
                  v-for="(keyword, index) in hotKeywords" 
                  :key="index"
                  class="hot-tag"
                  @click="selectHotKeyword(keyword)"
                >
                  {{ keyword }}
                </span>
              </div>
            </div>
          </div>
          
          <!-- 排序选项 -->
          <div class="content-tabs">
            <div class="tabs-left">
              <button 
                class="tab-btn" 
                :class="{ active: queryParams.sortBy === 'time' }"
                @click="queryParams.sortBy = 'time'; handleSortChange()"
              >
                <el-icon><Clock /></el-icon>
                最新
              </button>
              <button 
                class="tab-btn" 
                :class="{ active: queryParams.sortBy === 'hot' }"
                @click="queryParams.sortBy = 'hot'; handleSortChange()"
              >
                <el-icon><TrendCharts /></el-icon>
                最热
              </button>
            </div>
            <div class="tabs-right">
              <span class="posts-count">共 {{ total }} 篇帖子</span>
            </div>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div v-loading="loading" class="posts-feed">
          <article 
            v-for="post in postList" 
            :key="post.id"
            class="post-item"
            @click="goToPostDetail(post)"
          >
            <!-- 作者信息 -->
            <div class="post-author-info">
              <div class="author-avatar" @click.stop="goToUserProfile(post.authorId)">
                {{ post.authorName?.charAt(0) || '匿' }}
              </div>
              <div class="author-details">
                <span class="author-name" @click.stop="goToUserProfile(post.authorId)">
                  {{ post.authorName }}
                </span>
                <span class="post-time">{{ formatRelativeTime(post.createTime) }}</span>
              </div>
              <span v-if="post.categoryName" class="post-category">
                {{ post.categoryName }}
              </span>
            </div>

            <!-- 帖子主体 -->
            <div class="post-body">
              <h2 class="post-title">{{ post.title }}</h2>
              
              <!-- AI摘要 -->
              <div v-if="post.aiSummary" class="ai-summary">
                <span class="ai-badge">🤖 AI摘要</span>
                <p>{{ post.aiSummary }}</p>
              </div>
              
              <p class="post-excerpt">{{ post.content }}</p>
              
              <!-- 帖子标签 -->
              <div v-if="post.tags && post.tags.length > 0" class="post-tags-inline">
                <span 
                  v-for="tag in post.tags" 
                  :key="tag.id"
                  class="inline-tag"
                  @click.stop="selectTag(tag.id)"
                >
                  # {{ tag.name }}
                </span>
              </div>
            </div>

            <!-- 帖子底部互动区 -->
            <div class="post-footer">
              <div class="post-stats-row">
                <span class="stat">
                  <el-icon><View /></el-icon>
                  {{ post.viewCount || 0 }}
                </span>
                <span class="stat like-stat" :class="{ active: post.isLiked }" @click.stop="toggleLike(post)">
                  <el-icon><Pointer /></el-icon>
                  {{ post.likeCount || 0 }}
                </span>
                <span class="stat">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ post.commentCount || 0 }}
                </span>
                <span class="stat" :class="{ active: post.isCollected }" @click.stop="toggleCollect(post)">
                  <el-icon><Star /></el-icon>
                  {{ post.collectCount || 0 }}
                </span>
              </div>
            </div>
          </article>

          <!-- 空状态 -->
          <div v-if="!loading && postList.length === 0" class="empty-state">
            <div class="empty-icon">📭</div>
            <p class="empty-text">暂无帖子</p>
            <button class="empty-btn" @click="showCreateDialog">发表第一篇帖子</button>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination 
            v-model:current-page="queryParams.pageNum" 
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 30, 50]"
            :total="total"
            layout="prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </main>

      <!-- 右侧边栏 -->
      <aside class="sidebar sidebar-right">
        <!-- 热门榜单 -->
        <div class="sidebar-card hot-ranking" v-if="hotPosts.length > 0">
          <div class="card-header">
            <el-icon><TrendCharts /></el-icon>
            <span>🔥 热门榜单</span>
          </div>
          <ul class="ranking-list">
            <li 
              v-for="(post, index) in hotPosts" 
              :key="post.id"
              class="ranking-item"
              @click="goToPostDetail(post)"
            >
              <span class="rank-number" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
              <div class="rank-content">
                <p class="rank-title">{{ post.title }}</p>
                <span class="rank-heat">🔥 {{ post.hotScore || 0 }}</span>
              </div>
            </li>
          </ul>
        </div>

        <!-- 社区公告 -->
        <div class="sidebar-card community-notice">
          <div class="card-header">
            <el-icon><Bell /></el-icon>
            <span>社区公告</span>
          </div>
          <div class="notice-content">
            <p>🎉 欢迎来到 Code Nest 社区！</p>
            <p>📝 发帖前请遵守社区规范</p>
            <p>💡 优质内容将获得推荐</p>
          </div>
        </div>

        <!-- 社区数据 -->
        <div class="sidebar-card community-stats">
          <div class="card-header">
            <el-icon><DataLine /></el-icon>
            <span>社区数据</span>
          </div>
          <div class="stats-grid">
            <div class="stat-box">
              <span class="stat-number">{{ total }}</span>
              <span class="stat-label">帖子总数</span>
            </div>
            <div class="stat-box">
              <span class="stat-number">{{ categoryList.length }}</span>
              <span class="stat-label">分类数</span>
            </div>
            <div class="stat-box">
              <span class="stat-number">{{ tagList.length }}</span>
              <span class="stat-label">标签数</span>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, Star, Edit, View, ChatDotRound, Document, TrendCharts, MagicStick,
  Menu, PriceTag, Clock, Bell, DataLine, Pointer
} from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'

const router = useRouter()

// 响应式数据
const searchKeyword = ref('')
const selectedCategoryId = ref(null)
const selectedTagId = ref(null)
const loading = ref(false)
const postList = ref([])
const categoryList = ref([])
const tagList = ref([])
const hotPosts = ref([])
const hotKeywords = ref([])
const showHotKeywords = ref(false)
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  categoryId: null,
  tagId: null,
  keyword: null,
  sortBy: 'time'
})



// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 格式化相对时间
const formatRelativeTime = (dateStr) => {
  if (!dateStr) return ''
  const now = new Date()
  const date = new Date(dateStr)
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return formatDate(dateStr)
}

// 获取分类图标
const getCategoryIcon = (name) => {
  const icons = {
    '技术分享': '💻',
    '求助问答': '❓',
    '项目展示': '🚀',
    '学习笔记': '📝',
    '职场交流': '💼',
    '闲聊灌水': '💬',
    '资源分享': '📦',
    '面试经验': '🎯'
  }
  return icons[name] || '📁'
}

// 点击外部关闭热门搜索
const handleClickOutside = (e) => {
  if (!e.target.closest('.search-bar')) {
    showHotKeywords.value = false
  }
}

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

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

// 选择分类
const selectCategory = (categoryId) => {
  selectedCategoryId.value = categoryId
  queryParams.categoryId = categoryId
  queryParams.pageNum = 1
  fetchPostList()
}

// 选择标签
const selectTag = (tagId) => {
  selectedTagId.value = tagId
  queryParams.tagId = tagId
  queryParams.pageNum = 1
  showHotKeywords.value = false
  fetchPostList()
}

// 搜索
const handleSearch = () => {
  queryParams.keyword = searchKeyword.value || null
  queryParams.pageNum = 1
  showHotKeywords.value = false
  fetchPostList()
}

// 排序改变
const handleSortChange = () => {
  queryParams.pageNum = 1
  fetchPostList()
}

// 选择热门搜索词
const selectHotKeyword = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchPostList()
}

// 当前页改变
const handleCurrentChange = (page) => {
  queryParams.pageNum = page
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

// 跳转到用户主页
const goToUserProfile = (userId) => {
  if (userId) {
    router.push(`/community/users/${userId}`)
  }
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

// 加载标签列表
const loadTags = async () => {
  try {
    const response = await communityApi.getTags()
    tagList.value = response || []
  } catch (error) {
    console.error('加载标签列表失败:', error)
  }
}

// 加载热门帖子
const loadHotPosts = async () => {
  try {
    const response = await communityApi.getHotPosts(5)
    hotPosts.value = response || []
  } catch (error) {
    console.error('加载热门帖子失败:', error)
  }
}

// 加载热门搜索词
const loadHotKeywords = async () => {
  try {
    const response = await communityApi.getHotKeywords(10)
    hotKeywords.value = response || []
  } catch (error) {
    console.error('加载热门搜索词失败:', error)
  }
}

// 初始化
onMounted(async () => {
  await loadCategories()
  await loadTags()
  await loadHotPosts()
  await loadHotKeywords()
  await initCommunity()
  await fetchPostList()
  document.addEventListener('click', handleClickOutside)
})
</script>

<style scoped>
/* ========== 全局容器 ========== */
.community-container {
  min-height: 100vh;
  background: #f4f5f5;
}

/* ========== 主体三栏布局 ========== */
.community-main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  display: grid;
  grid-template-columns: 240px 1fr 280px;
  gap: 24px;
}

/* ========== 侧边栏通用 ========== */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* ========== 左侧边栏 ========== */
/* 快捷操作 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f5f5;
  color: #666;
}

.action-btn:hover {
  background: #eee;
  transform: translateY(-1px);
}

.action-btn.primary {
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  color: white;
}

.action-btn.primary:hover {
  box-shadow: 0 4px 15px rgba(0, 184, 148, 0.4);
}

/* 分类导航 */
.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 4px;
}

.nav-item:hover {
  background: #f5f5f5;
}

.nav-item.active {
  background: linear-gradient(135deg, #00b89415 0%, #00a08515 100%);
  color: #00b894;
}

.nav-icon {
  font-size: 16px;
}

.nav-text {
  flex: 1;
  font-size: 14px;
}

.nav-count {
  font-size: 12px;
  color: #999;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

.nav-item.active .nav-count {
  background: #00b894;
  color: white;
}

/* 热门标签云 */
.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cloud-tag {
  padding: 6px 12px;
  background: #f5f5f5;
  border-radius: 20px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.cloud-tag:hover {
  background: #00b894;
  color: white;
}

.cloud-tag.active {
  background: #00b894;
  color: white;
}

/* ========== 主内容区 ========== */
.main-content {
  min-width: 0;
}

/* 内容顶部卡片 */
.content-header-card {
  background: white;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 搜索栏 */
.search-bar {
  position: relative;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.search-wrapper {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 8px 8px 8px 16px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.search-wrapper:focus-within {
  border-color: #00b894;
  background: white;
  box-shadow: 0 0 0 3px rgba(0, 184, 148, 0.1);
}

.search-icon {
  font-size: 18px;
  color: #999;
  margin-right: 10px;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #333;
  background: transparent;
}

.search-input::placeholder {
  color: #aaa;
}

.search-btn {
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.search-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 184, 148, 0.35);
}

/* 热门搜索下拉 */
.hot-keywords-dropdown {
  position: absolute;
  top: 50px;
  left: 0;
  right: 0;
  background: white;
  border-radius: 8px;
  padding: 14px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  border: 1px solid #e4e7ed;
  z-index: 100;
}

.hot-label {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 600;
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.hot-tag {
  padding: 5px 12px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.hot-tag:hover {
  background: #00b894;
  color: white;
}

/* 排序选项卡 */
.content-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tabs-left {
  display: flex;
  gap: 8px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
}

.tab-btn:hover {
  background: #f5f5f5;
}

.tab-btn.active {
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  color: white;
}

.posts-count {
  font-size: 13px;
  color: #999;
}

/* 帖子信息流 */
.posts-feed {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.post-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

/* 作者信息 */
.post-author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.3s;
}

.author-avatar:hover {
  transform: scale(1.05);
}

.author-details {
  flex: 1;
}

.author-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
}

.author-name:hover {
  color: #00b894;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-category {
  padding: 4px 12px;
  background: #f0f0f0;
  border-radius: 20px;
  font-size: 12px;
  color: #666;
}

/* 帖子主体 */
.post-body {
  margin-bottom: 14px;
}

.post-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  transition: color 0.3s;
}

.post-item:hover .post-title {
  color: #00b894;
}

/* AI摘要 */
.ai-summary {
  background: linear-gradient(135deg, #e8f8f5 0%, #d1f2eb 100%);
  border-radius: 8px;
  padding: 12px 14px;
  margin-bottom: 12px;
  border-left: 3px solid #00b894;
}

.ai-badge {
  display: inline-block;
  font-size: 11px;
  color: #00b894;
  font-weight: 600;
  margin-bottom: 6px;
}

.ai-summary p {
  margin: 0;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.post-excerpt {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 帖子标签 */
.post-tags-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.inline-tag {
  padding: 4px 10px;
  background: #e8f8f5;
  border-radius: 4px;
  font-size: 12px;
  color: #00b894;
  cursor: pointer;
  transition: all 0.3s;
}

.inline-tag:hover {
  background: #00b894;
  color: white;
}

/* 帖子底部 */
.post-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 14px;
}

.post-stats-row {
  display: flex;
  gap: 24px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.stat:hover {
  color: #00b894;
}

.stat.active {
  color: #f56c6c;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.empty-text {
  color: #999;
  font-size: 15px;
  margin-bottom: 20px;
}

.empty-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.empty-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 184, 148, 0.4);
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px;
  background: white;
  border-radius: 12px;
}

/* ========== 右侧边栏 ========== */
/* 热门榜单 */
.ranking-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.ranking-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 1px solid #f5f5f5;
}

.ranking-item:last-child {
  border-bottom: none;
}

.ranking-item:hover {
  padding-left: 8px;
}

.rank-number {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  background: #f0f0f0;
  color: #999;
  flex-shrink: 0;
}

.rank-number.rank-1 {
  background: linear-gradient(135deg, #ff6b6b, #ee5a6f);
  color: white;
}

.rank-number.rank-2 {
  background: linear-gradient(135deg, #ffa94d, #ff922b);
  color: white;
}

.rank-number.rank-3 {
  background: linear-gradient(135deg, #ffd43b, #fab005);
  color: white;
}

.rank-content {
  flex: 1;
  min-width: 0;
}

.rank-title {
  margin: 0 0 4px 0;
  font-size: 13px;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ranking-item:hover .rank-title {
  color: #00b894;
}

.rank-heat {
  font-size: 11px;
  color: #f56c6c;
}

/* 社区公告 */
.notice-content {
  font-size: 13px;
  color: #666;
  line-height: 2;
}

.notice-content p {
  margin: 0;
}

/* 社区数据 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-box {
  text-align: center;
  padding: 12px 8px;
  background: #f9f9f9;
  border-radius: 8px;
}

.stat-number {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #00b894;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 11px;
  color: #999;
}

/* ========== 响应式设计 ========== */
@media (max-width: 1200px) {
  .community-main {
    grid-template-columns: 200px 1fr 240px;
    gap: 16px;
  }
}

@media (max-width: 992px) {
  .community-main {
    grid-template-columns: 1fr 240px;
  }
  
  .sidebar-left {
    display: none;
  }
}

@media (max-width: 768px) {
  .community-main {
    grid-template-columns: 1fr;
    padding: 16px;
  }
  
  .sidebar-right {
    display: none;
  }
  
  .banner-title {
    font-size: 28px;
  }
  
  .banner-subtitle {
    font-size: 14px;
  }
  
  .search-wrapper {
    padding: 4px 4px 4px 16px;
  }
  
  .search-btn {
    padding: 10px 20px;
    font-size: 14px;
  }
  
  .post-stats-row {
    gap: 16px;
  }
}
</style>
