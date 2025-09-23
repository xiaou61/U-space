<template>
  <div class="bug-store">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="back-button" @click="$router.push('/moyu-tools')">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回工具箱</span>
        </div>
        <div class="title-section">
          <h1 class="main-title">
            <el-icon class="title-icon"><Notification /></el-icon>
            Bug 商店
          </h1>
          <p class="subtitle">随机发现经典Bug，学习分析思路和解决方案</p>
        </div>
      </div>
    </div>

    <!-- Bug 展示区域 -->
    <div class="bug-display">
      <div class="bug-container">
        <!-- 获取Bug按钮 -->
        <div class="get-bug-section" v-if="!currentBug || !showBugDetail">
          <div class="get-bug-card">
            <div class="card-icon">
              <el-icon><MagicStick /></el-icon>
            </div>
            <h2 class="card-title">发现经典 Bug</h2>
            <p class="card-description">
              点击按钮随机获取一个经典Bug案例，学习问题分析和解决思路
            </p>
            <el-button 
              type="danger" 
              size="large" 
              class="get-bug-btn"
              :loading="loading"
              @click="getRandomBug"
            >
              <el-icon><Refresh /></el-icon>
              来一发Bug
            </el-button>
          </div>
        </div>

        <!-- Bug 详情展示 -->
        <div class="bug-detail-section" v-if="currentBug && showBugDetail">
          <div class="bug-card">
            <!-- Bug 头部信息 -->
            <div class="bug-header">
              <div class="bug-title-section">
                <h2 class="bug-title">{{ currentBug.title }}</h2>
                <div class="bug-meta">
                  <span class="difficulty-badge" :class="getDifficultyClass(currentBug.difficultyLevel)">
                    {{ currentBug.difficultyName }}
                  </span>
                  <div class="tech-tags" v-if="currentBug.techTags && currentBug.techTags.length > 0">
                    <span 
                      v-for="tag in currentBug.techTags" 
                      :key="tag" 
                      class="tech-tag"
                    >
                      {{ tag }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="action-buttons">
                <el-button 
                  type="primary" 
                  :icon="CopyDocument" 
                  @click="copyBugContent"
                  size="small"
                >
                  复制内容
                </el-button>
                <el-button 
                  type="success" 
                  :icon="Refresh" 
                  @click="getRandomBug"
                  :loading="loading"
                  size="small"
                >
                  换一个
                </el-button>
              </div>
            </div>

            <!-- Bug 内容 -->
            <div class="bug-content">
              <!-- 现象描述 -->
              <div class="content-section">
                <h3 class="section-title">
                  <el-icon><Warning /></el-icon>
                  现象描述
                </h3>
                <div class="section-content phenomenon">
                  <p>{{ currentBug.phenomenon }}</p>
                </div>
              </div>

              <!-- 原因分析 -->
              <div class="content-section">
                <h3 class="section-title">
                  <el-icon><Search /></el-icon>
                  原因分析
                </h3>
                <div class="section-content analysis">
                  <div class="formatted-content" v-html="formatContent(currentBug.causeAnalysis)"></div>
                </div>
              </div>

              <!-- 解决方案 -->
              <div class="content-section">
                <h3 class="section-title">
                  <el-icon><Tools /></el-icon>
                  解决方案
                </h3>
                <div class="section-content solution">
                  <div class="formatted-content" v-html="formatContent(currentBug.solution)"></div>
                </div>
              </div>
            </div>

            <!-- 底部操作 -->
            <div class="bug-footer">
              <div class="footer-left">
                <span class="tip-text">💡 提示：遇到类似问题时，记得按这个思路来分析哦！</span>
              </div>
              <div class="footer-right">
                <el-button 
                  type="primary" 
                  @click="getRandomBug"
                  :loading="loading"
                >
                  <el-icon><Refresh /></el-icon>
                  再来一个
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, 
  Notification, 
  MagicStick, 
  Refresh, 
  CopyDocument, 
  Warning, 
  Search, 
  Tools
} from '@element-plus/icons-vue'
import { getRandomBug as fetchRandomBug } from '@/api/moyu'

// 响应式数据
const loading = ref(false)
const currentBug = ref(null)
const showBugDetail = ref(false)

// 获取随机Bug
const getRandomBug = async () => {
  try {
    loading.value = true
    showBugDetail.value = false
    
    const response = await fetchRandomBug()
    
    if (response) {
      currentBug.value = response
      showBugDetail.value = true
    } else {
      ElMessage.warning('暂无可用的Bug内容，请稍后再试')
    }
  } catch (error) {
    console.error('获取随机Bug失败:', error)
    ElMessage.error(error.message || '获取Bug失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 获取难度等级样式类
const getDifficultyClass = (level) => {
  const classes = {
    1: 'difficulty-easy',
    2: 'difficulty-medium', 
    3: 'difficulty-hard',
    4: 'difficulty-expert'
  }
  return classes[level] || 'difficulty-easy'
}

// 格式化内容（处理换行）
const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br/>')
}

// 复制Bug内容
const copyBugContent = async () => {
  if (!currentBug.value) return
  
  const bug = currentBug.value
  const content = `
【Bug标题】${bug.title}

【现象描述】
${bug.phenomenon}

【原因分析】
${bug.causeAnalysis}

【解决方案】
${bug.solution}

【技术标签】${bug.techTags ? bug.techTags.join(', ') : ''}
【难度等级】${bug.difficultyName}
  `.trim()
  
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('内容已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动选择复制')
  }
}
</script>

<style scoped>
.bug-store {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow-x: hidden;
}

.bug-store::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.bug-store > * {
  position: relative;
  z-index: 1;
}

/* 页面头部 */
.page-header {
  padding: 40px 20px;
  text-align: center;
  color: white;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
}

.back-button {
  position: absolute;
  left: 0;
  top: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: color 0.3s ease;
  font-size: 14px;
}

.back-button:hover {
  color: white;
}

.main-title {
  font-size: 3rem;
  font-weight: 700;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: linear-gradient(45deg, #fff, #ffe0e6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 2.5rem;
  color: #ff6b6b;
}

.subtitle {
  font-size: 1.25rem;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
}

/* Bug 展示区域 */
.bug-display {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px 60px;
}

.bug-container {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

/* 获取Bug区域 */
.get-bug-section {
  padding: 60px 40px;
  text-align: center;
}

.get-bug-card .card-icon {
  font-size: 4rem;
  color: #ef4444;
  margin-bottom: 24px;
}

.get-bug-card .card-title {
  font-size: 2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.get-bug-card .card-description {
  font-size: 1.1rem;
  color: #6b7280;
  margin: 0 0 32px 0;
  line-height: 1.6;
}

.get-bug-btn {
  font-size: 1.2rem;
  height: 56px;
  padding: 0 32px;
  border-radius: 28px;
  font-weight: 600;
}

/* Bug 详情区域 */
.bug-detail-section {
  padding: 0;
}

.bug-card {
  background: white;
}

.bug-header {
  padding: 32px 40px 24px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
}

.bug-title-section {
  flex: 1;
}

.bug-title {
  font-size: 1.8rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.bug-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.difficulty-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  color: white;
}

.difficulty-easy { background: linear-gradient(135deg, #10b981, #059669); }
.difficulty-medium { background: linear-gradient(135deg, #f59e0b, #d97706); }
.difficulty-hard { background: linear-gradient(135deg, #ef4444, #dc2626); }
.difficulty-expert { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }

.tech-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tech-tag {
  padding: 4px 12px;
  background: rgba(107, 114, 128, 0.1);
  border-radius: 12px;
  font-size: 0.8rem;
  color: #374151;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

/* Bug 内容 */
.bug-content {
  padding: 0 40px;
}

.content-section {
  margin-bottom: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #e5e7eb;
}

.section-content {
  padding: 16px 24px;
  border-radius: 12px;
  line-height: 1.7;
}

.phenomenon {
  background: rgba(239, 68, 68, 0.05);
  border: 1px solid rgba(239, 68, 68, 0.1);
  color: #374151;
}

.analysis {
  background: rgba(59, 130, 246, 0.05);
  border: 1px solid rgba(59, 130, 246, 0.1);
  color: #374151;
}

.solution {
  background: rgba(16, 185, 129, 0.05);
  border: 1px solid rgba(16, 185, 129, 0.1);
  color: #374151;
}

.formatted-content {
  font-size: 1rem;
}

/* Bug 底部 */
.bug-footer {
  padding: 24px 40px 32px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.footer-left .tip-text {
  color: #6b7280;
  font-size: 0.9rem;
  font-style: italic;
}

.footer-right {
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-title {
    font-size: 2.2rem;
    flex-direction: column;
    gap: 12px;
  }
  
  .bug-display {
    padding: 0 15px 40px;
  }
  
  .get-bug-section {
    padding: 40px 20px;
  }
  
  .bug-header {
    padding: 24px 20px 16px;
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .action-buttons {
    justify-content: center;
  }
  
  .bug-content {
    padding: 0 20px;
  }
  
  .bug-footer {
    padding: 20px;
    flex-direction: column;
    align-items: stretch;
    text-align: center;
    gap: 16px;
  }
  
  .back-button {
    position: static;
    justify-content: center;
    margin-bottom: 20px;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 1.8rem;
  }
  
  .get-bug-card .card-title {
    font-size: 1.6rem;
  }
  
  .bug-title {
    font-size: 1.4rem;
  }
  
  .get-bug-btn {
    font-size: 1rem;
    height: 48px;
    padding: 0 24px;
  }
}
</style>
