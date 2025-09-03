<template>
  <div class="question-detail">
    <!-- 顶部导航 -->
    <div class="nav-bar">
      <div class="nav-left">
        <el-button @click="goBack" :icon="Back" text>
          返回题单
        </el-button>
        <el-divider direction="vertical" />
        <span class="breadcrumb">
          {{ questionSet.title }} / 第 {{ currentIndex + 1 }} 题
        </span>
      </div>
      <div class="nav-right">
        <div class="mode-toggle">
          <el-switch
            v-model="isStudyMode"
            :active-icon="Reading"
            :inactive-icon="EditPen"
            active-text="背题模式"
            inactive-text="做题模式"
            inline-prompt
            @change="handleModeChange"
            style="margin-right: 12px;"
          />
        </div>
        <el-button 
          :type="isFavorited ? 'danger' : 'primary'" 
          :icon="Star"
          @click="toggleFavorite"
          :loading="favoriteLoading"
          size="small"
        >
          {{ isFavorited ? '取消收藏' : '收藏' }}
        </el-button>
      </div>
    </div>

    <!-- 题目内容 -->
    <div class="question-content-wrapper">
      <el-card v-loading="loading" shadow="never" class="question-card">
        <div class="question-header">
          <h1 class="question-title">{{ question.title }}</h1>
          <div class="question-meta">
            <span class="view-count">
              <el-icon><View /></el-icon>
              {{ question.viewCount || 0 }} 浏览
            </span>
            <span class="favorite-count">
              <el-icon><Star /></el-icon>
              {{ question.favoriteCount || 0 }} 收藏
            </span>
          </div>
        </div>

        <div class="question-content">
          <div class="answer-section" v-if="shouldShowAnswer">
            <h3>参考答案</h3>
            <div class="markdown-content" v-html="renderedAnswer"></div>
          </div>

          <div class="action-buttons" v-if="!isStudyMode">
            <el-button 
              v-if="!showAnswer" 
              type="primary" 
              @click="showAnswer = true"
              :icon="View"
            >
              查看答案
            </el-button>
            <el-button 
              v-else 
              type="info" 
              @click="showAnswer = false"
              :icon="Hide"
            >
              隐藏答案
            </el-button>
          </div>
          
          <div class="mode-tip" v-if="isStudyMode">
            <el-alert
              title="背题模式"
              description="当前为背题模式，答案已自动显示，适合复习和记忆"
              type="success"
              :closable="false"
              show-icon
            />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <el-card shadow="never" class="nav-card">
        <div class="nav-content">
          <el-button 
            @click="goToPrevQuestion" 
            :disabled="!hasPrev"
            :icon="ArrowLeft"
          >
            上一题
          </el-button>
          
          <div class="progress-info">
            <span>{{ currentIndex + 1 }} / {{ totalQuestions }}</span>
            <el-progress 
              :percentage="progressPercentage" 
              :stroke-width="8"
              style="width: 200px; margin: 0 20px;"
            />
          </div>
          
          <el-button 
            @click="goToNextQuestion" 
            :disabled="!hasNext"
            :icon="ArrowRight"
          >
            下一题
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Back, View, Hide, Star, ArrowLeft, ArrowRight, Reading, EditPen
} from '@element-plus/icons-vue'
import { renderMarkdown } from '@/utils/markdown'
import { useInterviewStore } from '@/stores/interview'

const route = useRoute()
const router = useRouter()
const interviewStore = useInterviewStore()

// 响应式数据
const favoriteLoading = ref(false)
const showAnswer = ref(false)
const isStudyMode = ref(false) // false: 做题模式, true: 背题模式

// 从store获取数据
const loading = computed(() => interviewStore.currentQuestionLoading)
const question = computed(() => interviewStore.currentQuestion)
const questionSet = computed(() => interviewStore.currentQuestionSet)
const questionList = computed(() => interviewStore.questions)

// 收藏状态
const isFavorited = computed(() => {
  const statusKey = `3-${questionId.value}` // 3表示题目类型
  return interviewStore.favoriteStatus.get(statusKey) || false
})

// 路由参数
const setId = ref(parseInt(route.params.setId))
const questionId = ref(parseInt(route.params.questionId))

// 计算属性
const currentIndex = computed(() => {
  return questionList.value.findIndex(q => q.id === questionId.value)
})

const totalQuestions = computed(() => questionList.value.length)

const progressPercentage = computed(() => {
  if (totalQuestions.value === 0) return 0
  return Math.round(((currentIndex.value + 1) / totalQuestions.value) * 100)
})

const hasPrev = computed(() => currentIndex.value > 0)
const hasNext = computed(() => currentIndex.value < totalQuestions.value - 1)

// 是否显示答案（根据模式决定）
const shouldShowAnswer = computed(() => {
  return isStudyMode.value || showAnswer.value
})

// 渲染Markdown内容
const renderedAnswer = computed(() => {
  if (!question.value.answer) return ''
  return renderMarkdown(question.value.answer)
})

// 获取题单信息
const fetchQuestionSet = async () => {
  try {
    await interviewStore.fetchQuestionSetById(setId.value)
  } catch (error) {
    console.error('获取题单信息失败:', error)
  }
}

// 获取题目列表（用于导航）
const fetchQuestionList = async () => {
  try {
    await interviewStore.fetchQuestionsBySetId(setId.value)
  } catch (error) {
    console.error('获取题目列表失败:', error)
  }
}

// 获取题目详情
const fetchQuestion = async () => {
  try {
    await interviewStore.fetchQuestionById(setId.value, questionId.value)
    showAnswer.value = false // 重置答案显示状态
  } catch (error) {
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  try {
    await interviewStore.checkFavoriteStatus(3, questionId.value) // 3表示题目类型
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  favoriteLoading.value = true
  try {
    if (isFavorited.value) {
      await interviewStore.removeFavorite(3, questionId.value)
      ElMessage.success('取消收藏成功')
    } else {
      await interviewStore.addFavorite(3, questionId.value)
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('收藏操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

// 上一题
const goToPrevQuestion = async () => {
  try {
    const data = await interviewStore.fetchPrevQuestion(setId.value, questionId.value)
    if (data) {
      router.push(`/interview/questions/${setId.value}/${data.id}`)
    } else {
      ElMessage.info('已经是第一题了')
    }
  } catch (error) {
    ElMessage.error('获取上一题失败')
  }
}

// 下一题
const goToNextQuestion = async () => {
  try {
    const data = await interviewStore.fetchNextQuestion(setId.value, questionId.value)
    if (data) {
      router.push(`/interview/questions/${setId.value}/${data.id}`)
    } else {
      ElMessage.info('已经是最后一题了')
    }
  } catch (error) {
    ElMessage.error('获取下一题失败')
  }
}

// 返回题单详情
const goBack = () => {
  router.push(`/interview/question-sets/${setId.value}`)
}

// 模式切换处理
const handleModeChange = (value) => {
  isStudyMode.value = value
  // 保存用户偏好到localStorage
  localStorage.setItem('question-mode', value ? 'study' : 'practice')
  
  // 如果切换到做题模式，重置答案显示状态
  if (!value) {
    showAnswer.value = false
  }
}

// 初始化模式状态
const initMode = () => {
  const savedMode = localStorage.getItem('question-mode')
  isStudyMode.value = savedMode === 'study'
}

// 监听路由变化
watch(() => route.params.questionId, (newQuestionId) => {
  if (newQuestionId) {
    questionId.value = parseInt(newQuestionId)
    fetchQuestion()
    checkFavoriteStatus()
    // 如果是做题模式，重置答案显示状态
    if (!isStudyMode.value) {
      showAnswer.value = false
    }
  }
})

// 页面挂载
onMounted(() => {
  // 初始化模式状态
  initMode()
  
  // 加载页面数据
  fetchQuestionSet()
  fetchQuestionList()
  fetchQuestion()
  checkFavoriteStatus()
})
</script>

<style scoped>
.question-detail {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  flex-direction: column;
}

.nav-bar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e4e7ed;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb {
  color: #606266;
  font-size: 14px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mode-toggle {
  display: flex;
  align-items: center;
}

.question-content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.question-card {
  max-width: 900px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border: none;
}

.question-header {
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 20px;
  margin-bottom: 30px;
}

.question-title {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 28px;
  font-weight: bold;
  line-height: 1.4;
}

.question-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.question-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.question-content {
  line-height: 1.8;
}

.answer-section {
  margin-bottom: 32px;
}

.answer-section h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.answer-content {
  background: rgba(240, 249, 255, 0.5);
  border: 2px solid #409EFF;
  border-radius: 12px;
  padding: 24px;
  margin-top: 16px;
}

.action-buttons {
  margin-top: 30px;
  text-align: center;
}

.mode-tip {
  margin-top: 24px;
}

.bottom-nav {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid #e4e7ed;
  padding: 16px 20px;
  position: sticky;
  bottom: 0;
  z-index: 100;
}

.nav-card {
  max-width: 900px;
  margin: 0 auto;
  background: transparent;
  border: none;
  box-shadow: none;
}

.nav-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 768px) {
  .question-detail {
    padding: 0;
  }
  
  .nav-bar {
    padding: 8px 12px;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .nav-left {
    width: 100%;
  }
  
  .nav-right {
    align-self: flex-end;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .mode-toggle {
    order: -1;
    width: 100%;
    margin-bottom: 8px;
  }
  
  .question-content-wrapper {
    padding: 12px;
  }
  
  .question-title {
    font-size: 22px;
  }
  
  .question-meta {
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .markdown-content {
    padding: 16px;
    font-size: 14px;
  }
  
  .nav-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .progress-info {
    order: -1;
  }
  
  .progress-info .el-progress {
    width: 250px !important;
  }
}
</style> 