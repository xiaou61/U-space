<template>
  <div class="interview-index">
    <!-- 头部导航 -->
    <div class="header">
      <div class="header-content">
        <div class="header-left">
          <h2>面试题库</h2>
          <p>精选面试题，助力求职成功</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="goToFavorites" :icon="Star">
            我的收藏
          </el-button>
          <el-button @click="goBack" :icon="Back">
            返回首页
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <el-card shadow="never" class="category-card">
        <div class="category-tabs">
          <el-button 
            :type="currentCategoryId === null ? 'primary' : ''"
            :plain="currentCategoryId !== null"
            @click="selectCategory(null)"
          >
            全部分类
          </el-button>
          <el-button 
            v-for="category in categoryList" 
            :key="category.id"
            :type="currentCategoryId === category.id ? 'primary' : ''"
            :plain="currentCategoryId !== category.id"
            @click="selectCategory(category.id)"
          >
            {{ category.name }}
            <el-tag v-if="category.questionSetCount > 0" type="info" size="small" style="margin-left: 6px;">
              {{ category.questionSetCount }}
            </el-tag>
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-card shadow="never" class="search-card">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索题单或题目..." 
              size="large"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="8">
            <el-button type="primary" size="large" @click="handleSearch" style="width: 100%">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 题单列表 -->
    <div class="content-section">
      <el-card shadow="never" class="content-card">
        <template #header>
          <div class="content-header">
            <span>题单列表</span>
            <span class="total-count" v-if="total > 0">共 {{ total }} 个题单</span>
          </div>
        </template>

        <div v-loading="loading" class="question-sets-grid">
          <div 
            v-for="questionSet in questionSetList" 
            :key="questionSet.id"
            class="question-set-card"
            @click="goToQuestionSet(questionSet)"
          >
            <div class="card-header">
              <h3 class="card-title">{{ questionSet.title }}</h3>
              <div class="card-badges">
                <el-tag :type="questionSet.type === 1 ? 'success' : 'info'" size="small">
                  {{ questionSet.type === 1 ? '官方' : '用户' }}
                </el-tag>
                <el-tag type="warning" size="small" v-if="questionSet.categoryName">
                  {{ questionSet.categoryName }}
                </el-tag>
              </div>
            </div>
            
            <p class="card-description">{{ questionSet.description || '暂无描述' }}</p>
            
            <div class="card-stats">
              <div class="stat-item">
                <el-icon><Edit /></el-icon>
                <span>{{ questionSet.questionCount || 0 }} 题</span>
              </div>
              <div class="stat-item">
                <el-icon><View /></el-icon>
                <span>{{ questionSet.viewCount || 0 }} 浏览</span>
              </div>
              <div class="stat-item">
                <el-icon><Star /></el-icon>
                <span>{{ questionSet.favoriteCount || 0 }} 收藏</span>
              </div>
            </div>
            
            <div class="card-footer">
              <span class="creator">{{ questionSet.creatorName }}</span>
              <span class="create-time">{{ formatDate(questionSet.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty 
          v-if="!loading && questionSetList.length === 0" 
          description="暂无题单数据"
          :image-size="120"
        />

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination 
            v-model:current-page="queryParams.page" 
            v-model:page-size="queryParams.size"
            :page-sizes="[10, 20, 30, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, Star, Back, Edit, View, Collection
} from '@element-plus/icons-vue'
import { interviewApi } from '@/api/interview'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const categoryList = ref([])
const questionSetList = ref([])
const searchKeyword = ref('')
const currentCategoryId = ref(null)
const total = ref(0)

// 查询参数
const queryParams = reactive({
  keyword: '',
  page: 1,
  size: 12
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const data = await interviewApi.getEnabledCategories()
    categoryList.value = data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 获取题单列表
const fetchQuestionSets = async () => {
  loading.value = true
  try {
    // 如果有搜索关键词，使用搜索接口，否则使用公开题单列表接口
    let data
    if (queryParams.keyword) {
      data = await interviewApi.searchQuestionSets(queryParams)
    } else {
      data = await interviewApi.getPublicQuestionSets({
        categoryId: currentCategoryId.value,
        page: queryParams.page,
        size: queryParams.size
      })
    }
    questionSetList.value = data?.records || []
    total.value = data?.total || 0
  } catch (error) {
    console.error('获取题单列表失败:', error)
    ElMessage.error('获取题单列表失败')
  } finally {
    loading.value = false
  }
}

// 选择分类
const selectCategory = (categoryId) => {
  currentCategoryId.value = categoryId
  queryParams.page = 1
  // 这里可以根据分类ID进一步筛选，但现在使用搜索接口
  fetchQuestionSets()
}

// 搜索
const handleSearch = () => {
  queryParams.keyword = searchKeyword.value
  queryParams.page = 1
  fetchQuestionSets()
}

// 分页大小改变
const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.page = 1
  fetchQuestionSets()
}

// 当前页改变
const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchQuestionSets()
}

// 跳转到题单详情
const goToQuestionSet = (questionSet) => {
  router.push(`/interview/question-sets/${questionSet.id}`)
}

// 跳转到收藏页面
const goToFavorites = () => {
  router.push('/interview/favorites')
}

// 返回首页
const goBack = () => {
  router.push('/')
}

// 页面挂载
onMounted(() => {
  fetchCategories()
  fetchQuestionSets()
})
</script>

<style scoped>
.interview-index {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e4e7ed;
  padding: 20px 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 28px;
  font-weight: bold;
}

.header-left p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.category-nav {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.category-card {
  background: rgba(255, 255, 255, 0.9);
  border: none;
}

.category-tabs {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-section {
  padding: 0 20px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-card {
  background: rgba(255, 255, 255, 0.9);
  border: none;
}

.content-section {
  padding: 0 20px 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.content-card {
  background: rgba(255, 255, 255, 0.95);
  border: none;
  min-height: 500px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.total-count {
  color: #909399;
  font-size: 14px;
  font-weight: normal;
}

.question-sets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.question-set-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
  position: relative;
  overflow: hidden;
}

.question-set-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #409EFF, #67C23A, #E6A23C);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.question-set-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.question-set-card:hover::before {
  opacity: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-title {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: bold;
  line-height: 1.4;
  flex: 1;
  margin-right: 12px;
}

.card-badges {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

.card-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  min-height: 40px;
}

.card-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 0;
  border-top: 1px solid #f0f2f5;
  border-bottom: 1px solid #f0f2f5;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 13px;
}

.stat-item .el-icon {
  font-size: 14px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #C0C4CC;
}

.creator {
  font-weight: 500;
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-right {
    align-self: stretch;
    justify-content: flex-end;
  }
  
  .category-tabs {
    justify-content: center;
  }
  
  .question-sets-grid {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .card-badges {
    flex-direction: row;
    align-self: flex-start;
  }
}
</style> 