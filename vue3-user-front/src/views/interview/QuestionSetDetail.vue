<template>
  <div class="question-set-detail">
    <!-- 返回导航 -->
    <div class="nav-bar">
      <el-button @click="goBack" :icon="Back" text>
        返回题库
      </el-button>
    </div>

    <!-- 题单信息 -->
    <el-card v-loading="loading" shadow="never" class="set-info-card">
      <div class="set-header">
        <div class="set-main-info">
          <h1 class="set-title">{{ questionSet.title }}</h1>
          <p class="set-description">{{ questionSet.description || '暂无描述' }}</p>
          
          <div class="set-meta">
            <div class="meta-item">
              <el-tag :type="questionSet.type === 1 ? 'success' : 'info'" size="small">
                {{ questionSet.type === 1 ? '官方题单' : '用户题单' }}
              </el-tag>
              <el-tag type="warning" size="small" v-if="questionSet.categoryName">
                {{ questionSet.categoryName }}
              </el-tag>
              <el-tag 
                :type="questionSet.status === 1 ? 'success' : 'danger'" 
                size="small"
              >
                {{ questionSet.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </div>
            
            <div class="meta-stats">
              <span class="stat">
                <el-icon><Edit /></el-icon>
                {{ questionSet.questionCount || 0 }} 题
              </span>
              <span class="stat">
                <el-icon><View /></el-icon>
                {{ questionSet.viewCount || 0 }} 浏览
              </span>
              <span class="stat">
                <el-icon><Star /></el-icon>
                {{ questionSet.favoriteCount || 0 }} 收藏
              </span>
            </div>
          </div>
        </div>
        
        <div class="set-actions">
          <el-button 
            :type="isFavorited ? 'danger' : 'primary'" 
            :icon="Star"
            @click="toggleFavorite"
            :loading="favoriteLoading"
          >
            {{ isFavorited ? '取消收藏' : '收藏题单' }}
          </el-button>
          <el-button 
            type="success" 
            :icon="VideoPlay"
            @click="startLearning"
            :disabled="questionList.length === 0"
          >
            开始学习
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 题目列表 -->
    <el-card shadow="never" class="questions-card">
      <template #header>
        <div class="questions-header">
          <span>题目列表</span>
          <span class="question-count">共 {{ questionList.length }} 题</span>
        </div>
      </template>

      <div v-loading="questionsLoading" class="questions-list">
        <div 
          v-for="(question, index) in questionList" 
          :key="question.id"
          class="question-item"
          @click="goToQuestion(question)"
        >
          <div class="question-index">{{ index + 1 }}</div>
          <div class="question-content">
            <h4 class="question-title">{{ question.title }}</h4>
            <div class="question-meta">
              <span class="view-count">
                <el-icon><View /></el-icon>
                {{ question.viewCount || 0 }} 浏览
              </span>
              <span class="favorite-count">
                <el-icon><Star /></el-icon>
                {{ question.favoriteCount || 0 }} 收藏
              </span>
              <span class="create-time">
                创建于 {{ formatDate(question.createTime) }}
              </span>
            </div>
          </div>
          <div class="question-action">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-if="!questionsLoading && questionList.length === 0" 
        description="该题单暂无题目"
        :image-size="100"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Back, Edit, View, Star, VideoPlay, ArrowRight
} from '@element-plus/icons-vue'
import { interviewApi } from '@/api/interview'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const questionsLoading = ref(false)
const favoriteLoading = ref(false)
const questionSet = ref({})
const questionList = ref([])
const isFavorited = ref(false)

// 获取题单ID
const questionSetId = ref(parseInt(route.params.id))

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 获取题单详情
const fetchQuestionSet = async () => {
  loading.value = true
  try {
    const data = await interviewApi.getQuestionSetById(questionSetId.value)
    questionSet.value = data || {}
  } catch (error) {
    console.error('获取题单详情失败:', error)
    ElMessage.error('获取题单详情失败')
  } finally {
    loading.value = false
  }
}

// 获取题目列表
const fetchQuestions = async () => {
  questionsLoading.value = true
  try {
    const data = await interviewApi.getQuestionsBySetId(questionSetId.value)
    questionList.value = data || []
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败')
  } finally {
    questionsLoading.value = false
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  try {
    const favorited = await interviewApi.isFavorited(2, questionSetId.value) // 2表示题单类型
    isFavorited.value = favorited || false
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  favoriteLoading.value = true
  try {
    if (isFavorited.value) {
      await interviewApi.removeFavorite(2, questionSetId.value)
      ElMessage.success('取消收藏成功')
      isFavorited.value = false
    } else {
      await interviewApi.addFavorite(2, questionSetId.value)
      ElMessage.success('收藏成功')
      isFavorited.value = true
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('收藏操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

// 开始学习（跳转到第一题）
const startLearning = () => {
  if (questionList.value.length === 0) {
    ElMessage.warning('该题单暂无题目')
    return
  }
  const firstQuestion = questionList.value[0]
  router.push(`/interview/questions/${questionSetId.value}/${firstQuestion.id}`)
}

// 跳转到指定题目
const goToQuestion = (question) => {
  router.push(`/interview/questions/${questionSetId.value}/${question.id}`)
}

// 返回题库
const goBack = () => {
  router.push('/interview')
}

// 页面挂载
onMounted(() => {
  fetchQuestionSet()
  fetchQuestions()
  checkFavoriteStatus()
})
</script>

<style scoped>
.question-set-detail {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.nav-bar {
  margin-bottom: 20px;
}

.set-info-card {
  background: rgba(255, 255, 255, 0.95);
  border: none;
  margin-bottom: 20px;
}

.set-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.set-main-info {
  flex: 1;
}

.set-title {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 32px;
  font-weight: bold;
  line-height: 1.3;
}

.set-description {
  margin: 0 0 20px 0;
  color: #606266;
  font-size: 16px;
  line-height: 1.6;
}

.set-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meta-item {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.meta-stats {
  display: flex;
  gap: 20px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.set-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
}

.questions-card {
  background: rgba(255, 255, 255, 0.95);
  border: none;
}

.questions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.question-count {
  color: #909399;
  font-size: 14px;
  font-weight: normal;
}

.questions-list {
  margin-top: 16px;
}

.question-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  margin-bottom: 8px;
}

.question-item:hover {
  background: #f8f9fa;
  border-color: #409EFF;
  transform: translateX(4px);
}

.question-index {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #67C23A);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  margin-right: 16px;
  flex-shrink: 0;
}

.question-content {
  flex: 1;
}

.question-title {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
}

.question-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.question-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.question-action {
  color: #C0C4CC;
  font-size: 18px;
  margin-left: 12px;
}

@media (max-width: 768px) {
  .question-set-detail {
    padding: 12px;
  }
  
  .set-header {
    flex-direction: column;
  }
  
  .set-actions {
    flex-direction: row;
    align-self: stretch;
  }
  
  .set-title {
    font-size: 24px;
  }
  
  .question-item {
    padding: 12px;
  }
  
  .question-index {
    width: 32px;
    height: 32px;
    font-size: 14px;
    margin-right: 12px;
  }
  
  .question-title {
    font-size: 15px;
  }
  
  .question-meta {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style> 