<template>
  <div class="blog-home-container">
    <!-- 博客头部 -->
    <div v-if="blogConfig" class="blog-header">
      <div class="header-bg" :style="{ backgroundImage: blogConfig.blogCover ? `url(${blogConfig.blogCover})` : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }">
        <div class="header-overlay"></div>
        <div class="header-content">
          <el-avatar :size="100" :src="blogConfig.blogAvatar" class="avatar" />
          <h1 class="blog-name">{{ blogConfig.blogName }}</h1>
          <p class="blog-desc">{{ blogConfig.blogDescription }}</p>
          <div class="stats">
            <span>📝 {{ blogConfig.totalArticles }} 篇文章</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 未开通提示 -->
    <div v-else class="not-found">
      <el-empty description="该用户还未开通博客" />
    </div>

    <!-- 文章列表 -->
    <div v-if="blogConfig" class="blog-content">
      <el-row :gutter="20">
        <!-- 左侧文章列表 -->
        <el-col :span="16">
          <div v-if="articleList.length > 0" class="article-list">
            <div v-for="article in articleList" :key="article.id" class="article-card" @click="viewArticle(article.id)">
              <div v-if="article.coverImage" class="article-cover">
                <img :src="article.coverImage" :alt="article.title" />
                <el-tag v-if="article.isTop === 1" type="warning" class="top-tag">置顶</el-tag>
              </div>
              <div class="article-info">
                <h3 class="article-title">
                  <el-tag v-if="article.isTop === 1 && !article.coverImage" type="warning" size="small">置顶</el-tag>
                  {{ article.title }}
                </h3>
                <p class="article-summary">{{ article.summary }}</p>
                <div class="article-meta">
                  <el-tag size="small" type="primary">{{ article.categoryName }}</el-tag>
                  <el-tag v-for="tag in article.tags" :key="tag" size="small" type="info" class="ml-1">{{ tag }}</el-tag>
                  <span class="time">{{ article.publishTime }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无文章" />

          <!-- 分页 -->
          <div v-if="total > 0" class="pagination">
            <el-pagination
              v-model:current-page="queryParams.pageNum"
              v-model:page-size="queryParams.pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="getList"
            />
          </div>
        </el-col>

        <!-- 右侧边栏 -->
        <el-col :span="8">
          <el-card class="sidebar-card">
            <template #header>
              <div class="card-header">
                <span>分类</span>
              </div>
            </template>
            <div class="category-list">
              <div
                v-for="category in categories"
                :key="category.id"
                class="category-item"
                :class="{ active: queryParams.categoryId === category.id }"
                @click="filterByCategory(category.id)"
              >
                <span>{{ category.categoryName }}</span>
                <span class="count">{{ category.articleCount || 0 }}</span>
              </div>
              <div class="category-item" :class="{ active: !queryParams.categoryId }" @click="filterByCategory(null)">
                <span>全部</span>
              </div>
            </div>
          </el-card>

          <el-card v-if="blogConfig.personalTags && blogConfig.personalTags.length > 0" class="sidebar-card mt-3">
            <template #header>
              <div class="card-header">
                <span>个人标签</span>
              </div>
            </template>
            <div class="tags">
              <el-tag v-for="tag in blogConfig.personalTags" :key="tag" type="info">{{ tag }}</el-tag>
            </div>
          </el-card>

          <el-card v-if="blogConfig.socialLinks" class="sidebar-card mt-3">
            <template #header>
              <div class="card-header">
                <span>社交链接</span>
              </div>
            </template>
            <div class="social-links">
              <a v-for="(link, name) in blogConfig.socialLinks" :key="name" :href="link" target="_blank" class="social-link">
                {{ name }}
              </a>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBlogConfig, getUserArticleList, getAllCategories } from '@/api/blog'

const router = useRouter()
const route = useRoute()

const blogConfig = ref(null)
const articleList = ref([])
const categories = ref([])
const total = ref(0)

const queryParams = reactive({
  userId: null,
  categoryId: null,
  pageNum: 1,
  pageSize: 10
})

const loadBlogConfig = async (userId) => {
  try {
    blogConfig.value = await getBlogConfig(userId)
    queryParams.userId = userId
  } catch (error) {
    console.error('加载博客配置失败', error)
  }
}

const loadCategories = async () => {
  try {
    categories.value = await getAllCategories()
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const getList = async () => {
  if (!queryParams.userId) return
  
  try {
    const res = await getUserArticleList(queryParams)
    articleList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取文章列表失败')
  }
}

const filterByCategory = (categoryId) => {
  queryParams.categoryId = categoryId
  queryParams.pageNum = 1
  getList()
}

const viewArticle = (articleId) => {
  router.push(`/blog/${queryParams.userId}/article/${articleId}`)
}

onMounted(() => {
  const userId = route.params.userId
  if (userId) {
    loadBlogConfig(userId)
    loadCategories()
    getList()
  }
})
</script>

<style scoped>
.blog-home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.blog-header {
  margin-bottom: 30px;
}

.header-bg {
  position: relative;
  height: 300px;
  background-size: cover;
  background-position: center;
}

.header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
}

.header-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  text-align: center;
}

.avatar {
  border: 4px solid white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.blog-name {
  margin: 20px 0 10px;
  font-size: 32px;
  font-weight: bold;
}

.blog-desc {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.stats {
  margin-top: 15px;
}

.stats span {
  margin: 0 15px;
  font-size: 14px;
}

.not-found {
  padding: 100px 20px;
  text-align: center;
}

.blog-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.article-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.article-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.top-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.article-info {
  padding: 20px;
}

.article-title {
  margin: 0 0 15px 0;
  font-size: 20px;
  color: #333;
}

.article-summary {
  margin: 0 0 15px 0;
  color: #666;
  line-height: 1.6;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.ml-1 {
  margin-left: 5px;
}

.time {
  margin-left: auto;
  color: #999;
  font-size: 14px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.sidebar-card {
  margin-bottom: 20px;
}

.mt-3 {
  margin-top: 20px;
}

.card-header {
  font-weight: bold;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.category-item:hover {
  background: #f5f7fa;
}

.category-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.count {
  color: #999;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.social-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.social-link {
  color: #409eff;
  text-decoration: none;
}

.social-link:hover {
  text-decoration: underline;
}
</style>

