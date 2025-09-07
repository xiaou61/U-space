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
          <el-button type="warning" @click="goToRandomQuestions" :icon="Refresh">
            随机抽题
          </el-button>
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
              <div class="card-author">
                <span v-if="questionSet.creatorName">创建者：{{ questionSet.creatorName }}</span>
              </div>
              <div class="card-date">
                <span>{{ formatDate(questionSet.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>

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
  Search, Star, Back, Edit, View, Collection, Refresh
} from '@element-plus/icons-vue'
import { useInterviewStore } from '@/stores/interview'

const router = useRouter()
const interviewStore = useInterviewStore()

// 响应式数据
const searchKeyword = ref('')
const currentCategoryId = ref(null)

// 从store获取数据
const loading = computed(() => interviewStore.questionSetsLoading)
const categoryList = computed(() => interviewStore.categories)
const questionSetList = computed(() => interviewStore.questionSets)
const total = computed(() => interviewStore.questionSetsTotal)

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
    await interviewStore.fetchCategories()
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  }
}

// 获取题单列表
const fetchQuestionSets = async () => {
  try {
    // 如果有搜索关键词，使用搜索接口，否则使用公开题单列表接口
    if (queryParams.keyword) {
      await interviewStore.searchQuestions(queryParams)
    } else {
      await interviewStore.fetchPublicQuestionSets({
        categoryId: currentCategoryId.value,
        page: queryParams.page,
        size: queryParams.size
      })
    }
  } catch (error) {
    ElMessage.error('获取题单列表失败')
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
  // 如果是搜索结果（题目），直接跳转到题目详情
  if (typeof questionSet.id === 'string' && questionSet.id.startsWith('q-') && questionSet.originalQuestion) {
    router.push(`/interview/questions/${questionSet.originalQuestion.id}`)
  } else {
    // 正常题单，跳转到题单详情
    router.push(`/interview/question-sets/${questionSet.id}`)
  }
}

// 跳转到收藏页面
const goToFavorites = () => {
  router.push('/interview/favorites')
}

// 返回首页
const goBack = () => {
  router.push('/')
}

// 跳转到随机抽题页面
const goToRandomQuestions = () => {
  router.push('/interview/random')
}

// 初始化
onMounted(async () => {
  await fetchCategories()
  await fetchQuestionSets()
})
</script>

<style scoped>
.interview-index {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.header-left p {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.category-nav, .search-section, .content-section {
  margin-bottom: 20px;
}

.category-card, .search-card, .content-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.search-card {
  padding: 8px;
}



/* 题单列表样式 */
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-count {
  color: #909399;
  font-size: 14px;
}

.question-sets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.question-set-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.question-set-card:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  flex: 1;
}

.card-badges {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
  margin-left: 12px;
}

.card-description {
  margin: 0 0 16px 0;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  color: #909399;
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .interview-index {
    padding: 10px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .question-sets-grid {
    grid-template-columns: 1fr;
  }
  
  .category-tabs {
    justify-content: center;
  }
}
</style> 