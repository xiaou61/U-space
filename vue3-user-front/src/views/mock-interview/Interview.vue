<template>
  <div class="mock-interview-session">
    <!-- 顶部状态栏 -->
    <div class="session-header">
      <div class="header-left">
        <el-tag type="primary" size="large">{{ session.direction }}</el-tag>
        <span class="progress-text">
          第 {{ currentQuestion.questionOrder || 0 }} / {{ currentQuestion.totalQuestions || session.questionCount || 0 }} 题
        </span>
      </div>
      <div class="header-center">
        <el-progress 
          :percentage="progressPercent" 
          :stroke-width="8"
          :show-text="false"
          class="progress-bar"
        />
      </div>
      <div class="header-right">
        <el-button type="danger" plain @click="confirmEndInterview">
          结束面试
        </el-button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="session-main" v-loading="loading">
      <!-- 面试完成提示 -->
      <div v-if="isFinished" class="finished-section">
        <el-result
          icon="success"
          title="面试完成"
          sub-title="恭喜你完成了本次模拟面试！"
        >
          <template #extra>
            <el-button type="primary" size="large" @click="viewReport">
              查看面试报告
            </el-button>
          </template>
        </el-result>
      </div>

      <!-- 面试进行中 -->
      <template v-else>
        <!-- 题目区域 -->
        <el-card shadow="never" class="question-card">
          <template #header>
            <div class="question-header">
              <div class="question-info">
                <el-tag 
                  :type="currentQuestion.questionType === 1 ? 'primary' : 'warning'"
                  size="small"
                >
                  {{ currentQuestion.questionType === 1 ? '主题' : '追问' }}
                </el-tag>
                <span class="question-order">问题 {{ currentQuestion.questionOrder }}</span>
              </div>
              <div class="question-tags" v-if="currentQuestion.knowledgePoints?.length">
                <el-tag 
                  v-for="(point, index) in currentQuestion.knowledgePoints" 
                  :key="index"
                  type="info"
                  size="small"
                >
                  {{ point }}
                </el-tag>
              </div>
            </div>
          </template>

          <div class="question-content">
            <p class="question-text">{{ currentQuestion.questionContent }}</p>
          </div>
        </el-card>

        <!-- 答题区域 -->
        <el-card shadow="never" class="answer-card">
          <template #header>
            <span class="card-title">你的回答</span>
          </template>

          <el-input
            v-model="userAnswer"
            type="textarea"
            :rows="8"
            placeholder="请在此输入你的回答..."
            :disabled="submitting || !!feedback"
            maxlength="2000"
            show-word-limit
          />

          <div class="answer-actions" v-if="!feedback">
            <el-button @click="skipQuestion" :loading="skipping">
              跳过此题
            </el-button>
            <el-button 
              type="primary" 
              @click="submitAnswer"
              :loading="submitting"
              :disabled="!userAnswer.trim()"
            >
              提交答案
            </el-button>
          </div>
        </el-card>

        <!-- AI反馈区域 -->
        <el-card v-if="feedback" shadow="never" class="feedback-card">
          <template #header>
            <div class="feedback-header">
              <span class="card-title">
                <el-icon><ChatDotRound /></el-icon>
                AI 面试官评价
              </span>
              <div class="score-badge" :class="getScoreClass(feedback.score)">
                {{ feedback.score }} 分
              </div>
            </div>
          </template>

          <!-- 评价详情 -->
          <div class="feedback-content">
            <!-- 优点 -->
            <div class="feedback-section" v-if="feedback.strengths?.length">
              <h4 class="section-title success">
                <el-icon><CircleCheckFilled /></el-icon>
                回答优点
              </h4>
              <ul class="feedback-list">
                <li v-for="(item, index) in feedback.strengths" :key="index">
                  {{ item }}
                </li>
              </ul>
            </div>

            <!-- 改进点 -->
            <div class="feedback-section" v-if="feedback.improvements?.length">
              <h4 class="section-title warning">
                <el-icon><WarningFilled /></el-icon>
                需要改进
              </h4>
              <ul class="feedback-list">
                <li v-for="(item, index) in feedback.improvements" :key="index">
                  {{ item }}
                </li>
              </ul>
            </div>

            <!-- 考察知识点 -->
            <div class="feedback-section" v-if="feedback.referencePoints?.length">
              <h4 class="section-title info">
                <el-icon><Reading /></el-icon>
                考察知识点
              </h4>
              <div class="reference-tags">
                <el-tag 
                  v-for="(point, index) in feedback.referencePoints" 
                  :key="index"
                  type="info"
                >
                  {{ point }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 下一步操作 -->
          <div class="feedback-actions">
            <!-- 有追问时显示追问按钮 -->
            <el-button 
              v-if="feedback.nextAction === 'followUp' && feedback.followUpQuestion"
              type="warning"
              @click="handleFollowUp"
            >
              回答追问
            </el-button>
            <!-- 用户主动请求追问按钮（当没有自动追问时显示） -->
            <el-button 
              v-if="feedback.nextAction !== 'followUp' && !requestingFollowUp"
              type="info"
              plain
              @click="requestFollowUp"
              :loading="requestingFollowUp"
            >
              请求追问
            </el-button>
            <!-- 没有追问时显示下一题按钮 -->
            <el-button 
              v-if="feedback.hasNext && feedback.nextAction !== 'followUp'"
              type="primary"
              @click="nextQuestion"
            >
              下一题
            </el-button>
            <!-- 没有更多题目时显示完成面试 -->
            <el-button 
              v-if="!feedback.hasNext && feedback.nextAction !== 'followUp'"
              type="success"
              @click="finishInterview"
            >
              完成面试
            </el-button>
          </div>
        </el-card>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ChatDotRound, CircleCheckFilled, WarningFilled, Reading 
} from '@element-plus/icons-vue'
import { mockInterviewApi } from '@/api/mockInterview'

const router = useRouter()
const route = useRoute()

// 状态
const loading = ref(false)
const submitting = ref(false)
const skipping = ref(false)
const isFinished = ref(false)
const requestingFollowUp = ref(false)

// 会话信息 - 后端状态: 0-进行中 1-已完成 2-已中断
const session = reactive({
  sessionId: null,
  direction: '',
  directionName: '',
  questionCount: 0,
  status: 0
})

// 当前题目 - 匹配 InterviewQuestionResponse
const currentQuestion = reactive({
  qaId: null,
  questionOrder: 0,
  totalQuestions: 0,
  questionContent: '',
  questionType: 1,
  knowledgePoints: [],
  estimatedTime: 0,
  finished: false
})

// 用户答案
const userAnswer = ref('')

// AI反馈
const feedback = ref(null)

// 进度百分比
const progressPercent = computed(() => {
  const total = currentQuestion.totalQuestions || session.questionCount
  if (!total) return 0
  return Math.round((currentQuestion.questionOrder / total) * 100)
})

// 获取分数样式
const getScoreClass = (score) => {
  if (score >= 8) return 'score-high'
  if (score >= 6) return 'score-medium'
  return 'score-low'
}

// 初始化会话
const initSession = async () => {
  const sessionId = route.query.sessionId
  if (!sessionId) {
    ElMessage.error('会话不存在')
    router.push('/mock-interview')
    return
  }

  session.sessionId = sessionId
  loading.value = true

try {
    // 获取会话状态
    const statusData = await mockInterviewApi.getSessionStatus(sessionId)
    if (statusData) {
      Object.assign(session, statusData)
    }

    // 开始面试，获取第一题
    const startData = await mockInterviewApi.startInterview(sessionId)
    if (startData) {
      updateCurrentQuestion(startData)
    }
  } catch (error) {
    console.error('初始化会话失败', error)
    ElMessage.error('初始化面试失败')
  } finally {
    loading.value = false
  }
}

// 更新当前题目
const updateCurrentQuestion = (data) => {
  if (data.finished) {
    isFinished.value = true
    return
  }
  
  currentQuestion.qaId = data.qaId
  currentQuestion.questionOrder = data.questionOrder
  currentQuestion.totalQuestions = data.totalQuestions || currentQuestion.totalQuestions
  currentQuestion.questionContent = data.questionContent
  currentQuestion.questionType = data.questionType || 1
  currentQuestion.knowledgePoints = data.knowledgePoints || []
  
  // 重置状态
  userAnswer.value = ''
  feedback.value = null
}

// 提交答案
const submitAnswer = async () => {
  if (!userAnswer.value.trim()) {
    ElMessage.warning('请输入你的回答')
    return
  }

  submitting.value = true

  try {
    const data = await mockInterviewApi.submitAnswer({
      sessionId: session.sessionId,
      qaId: currentQuestion.qaId,
      answer: userAnswer.value
    })

    // 显示AI反馈 - 后端返回结构为 AnswerFeedbackResponse
    feedback.value = {
      score: data.score,
      strengths: data.feedback?.strengths || [],
      improvements: data.feedback?.improvements || [],
      referencePoints: [],  // 后端没有此字段
      nextAction: data.nextAction,
      hasNext: data.hasNext,
      followUpQuestion: data.followUpQuestion
    }
  } catch (error) {
    console.error('提交答案失败', error)
    ElMessage.error('提交答案失败')
  } finally {
    submitting.value = false
  }
}

// 跳过题目
const skipQuestion = async () => {
  try {
    await ElMessageBox.confirm('确定要跳过这道题吗？跳过后将得0分。', '提示', {
      type: 'warning'
    })

skipping.value = true

    const data = await mockInterviewApi.skipQuestion(session.sessionId, currentQuestion.qaId)
    
    if (data) {
      updateCurrentQuestion(data)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('跳过题目失败', error)
      ElMessage.error('操作失败')
    }
  } finally {
    skipping.value = false
  }
}

// 处理追问（自动追问）
const handleFollowUp = () => {
  if (feedback.value?.followUpQuestion) {
    // 更新为追问题目
    currentQuestion.questionContent = feedback.value.followUpQuestion.questionContent
    currentQuestion.qaId = feedback.value.followUpQuestion.qaId
    currentQuestion.questionType = 2 // 追问类型
    
    userAnswer.value = ''
    feedback.value = null
  }
}

// 用户主动请求追问
const requestFollowUp = async () => {
  requestingFollowUp.value = true
  
  try {
    const data = await mockInterviewApi.requestFollowUp(session.sessionId, currentQuestion.qaId)
    
    if (data) {
      // 更新为追问题目
      currentQuestion.questionContent = data.questionContent
      currentQuestion.qaId = data.qaId
      currentQuestion.questionType = 2 // 追问类型
      
      userAnswer.value = ''
      feedback.value = null
      
      ElMessage.success('已生成追问，请回答')
    }
  } catch (error) {
    console.error('请求追问失败', error)
    ElMessage.error(error.message || '请求追问失败')
  } finally {
    requestingFollowUp.value = false
  }
}

// 下一题
const nextQuestion = async () => {
  loading.value = true

try {
    const data = await mockInterviewApi.getNextQuestion(session.sessionId)
    
    if (data) {
      updateCurrentQuestion(data)
    }
  } catch (error) {
    console.error('获取下一题失败', error)
    ElMessage.error('获取下一题失败')
  } finally {
    loading.value = false
  }
}

// 完成面试
const finishInterview = async () => {
  loading.value = true

try {
    await mockInterviewApi.endInterview(session.sessionId)
    
    isFinished.value = true
  } catch (error) {
    console.error('完成面试失败', error)
    ElMessage.error('完成面试失败')
  } finally {
    loading.value = false
  }
}

// 确认结束面试
const confirmEndInterview = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要提前结束面试吗？未完成的题目将不计入评分。',
      '结束面试',
      { type: 'warning' }
    )
    
    await finishInterview()
  } catch (error) {
    // 取消操作
  }
}

// 查看报告
const viewReport = () => {
  router.push({
    path: '/mock-interview/report',
    query: { sessionId: session.sessionId }
  })
}

onMounted(() => {
  initSession()
})
</script>

<style scoped>
.mock-interview-session {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 顶部状态栏 */
.session-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.progress-text {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.header-center {
  flex: 1;
  max-width: 400px;
  margin: 0 32px;
}

.progress-bar {
  width: 100%;
}

/* 主内容区 */
.session-main {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
}

/* 完成提示 */
.finished-section {
  padding: 60px 0;
}

/* 题目卡片 */
.question-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-order {
  font-weight: 600;
  color: #303133;
}

.question-tags {
  display: flex;
  gap: 8px;
}

.question-content {
  padding: 20px 0;
}

.question-text {
  font-size: 18px;
  line-height: 1.8;
  color: #303133;
  margin: 0;
}

/* 答题卡片 */
.answer-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.answer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

/* 反馈卡片 */
.feedback-card {
  border-radius: 12px;
  border: 2px solid #409eff;
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.feedback-header .card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 600;
  color: white;
}

.score-high {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.score-medium {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
}

.score-low {
  background: linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%);
}

.feedback-content {
  padding: 16px 0;
}

.feedback-section {
  margin-bottom: 20px;
}

.feedback-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  margin: 0 0 12px 0;
}

.section-title.success {
  color: #67c23a;
}

.section-title.warning {
  color: #e6a23c;
}

.section-title.info {
  color: #409eff;
}

.feedback-list {
  margin: 0;
  padding-left: 24px;
  color: #606266;
  line-height: 1.8;
}

.reference-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feedback-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
