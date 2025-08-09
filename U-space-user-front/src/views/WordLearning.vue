<template>
  <div class="word-learning-page">
    <!-- 顶部导航 -->
    <header class="page-header">
      <el-icon @click="goBack" class="back-icon"><ArrowLeft /></el-icon>
      <h3>单词学习</h3>
      <div></div>
    </header>

    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" class="word-tabs">
      <el-tab-pane label="学习模式" name="learning">
        <div class="learning-content">
          <div v-if="!loading && currentWord" class="word-card">
            <div class="word-info">
              <h2 class="word-text">{{ currentWord.word }}</h2>
              <p class="word-meaning">{{ currentWord.meaning }}</p>
              <div class="word-details">
                <span class="phonetic" v-if="currentWord.phonetic">{{ currentWord.phonetic }}</span>
                <span class="difficulty">难度: {{ currentWord.difficulty || '未知' }}</span>
              </div>
            </div>
            <div class="action-buttons">
              <el-button type="success" size="large" @click="handleWordSelection(true)">
                认识
              </el-button>
              <el-button type="warning" size="large" @click="handleWordSelection(false)">
                不认识
              </el-button>
            </div>
          </div>
          <div v-else-if="loading" class="loading-area">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else class="no-word">
            <el-empty description="暂无更多单词">
              <el-button type="primary" @click="loadWords">重新加载</el-button>
            </el-empty>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="学习记录" name="records">
        <div class="records-content">
          <div class="filter-tabs">
            <el-radio-group v-model="recordFilter" @change="resetAndLoadRecords">
              <el-radio-button label="">全部记录</el-radio-button>
              <el-radio-button label="known">倾向认识</el-radio-button>
              <el-radio-button label="unknown">倾向不认识</el-radio-button>
            </el-radio-group>
          </div>
          
          <div v-if="userRecords.length > 0" class="records-list">
            <div v-for="record in userRecords" :key="record.id" class="record-item">
              <div class="record-word">
                <h4>{{ record.word || '未知单词' }}</h4>
                <p>认识: {{ record.knowCount || 0 }}次 | 不认识: {{ record.notKnowCount || 0 }}次</p>
                <p class="last-study" v-if="record.lastStudyTime">最后学习: {{ formatTime(record.lastStudyTime) }}</p>
              </div>
              <div class="record-info">
                <el-tag :type="getRecordType(record)">
                  {{ getRecordStatus(record) }}
                </el-tag>
                <span class="record-time">{{ formatTime(record.updatedAt || record.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div v-else-if="!recordsLoading" class="no-records">
            <el-empty description="暂无学习记录" />
          </div>
          <div v-if="recordsLoading" class="loading-area">
            <el-skeleton :rows="3" animated />
          </div>
          
          <!-- 分页 -->
          <el-pagination
            v-if="recordsTotal > 0"
            v-model:current-page="recordsPage"
            v-model:page-size="recordsPageSize"
            :total="recordsTotal"
            layout="prev, pager, next"
            @current-change="handlePageChange"
            class="pagination"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getWordPage, selectWord, getUserWordPage, getUserWordPageKnown, getUserWordPageUnknown } from '../api/word'

const router = useRouter()

// 页面状态
const activeTab = ref('learning')
const loading = ref(false)
const recordsLoading = ref(false)

// 学习模式相关
const currentWord = ref(null)
const wordList = ref([])
const currentIndex = ref(0)

// 记录模式相关
const userRecords = ref([])
const recordsPage = ref(1)
const recordsPageSize = ref(10)
const recordsTotal = ref(0)
const recordFilter = ref('')

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'records') {
    loadUserRecords()
  }
})

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 加载单词列表
const loadWords = async () => {
  loading.value = true
  try {
    const res = await getWordPage({
      pageNumber: 1,
      pageSize: 20
    })
    if (res.code === 200 && res.data?.list) {
      wordList.value = res.data.list
      currentIndex.value = 0
      if (wordList.value.length > 0) {
        currentWord.value = wordList.value[0]
      }
    } else {
      ElMessage.error('加载单词失败')
    }
  } catch (error) {
    console.error('加载单词失败:', error)
    ElMessage.error('加载单词失败')
  } finally {
    loading.value = false
  }
}

// 处理单词选择
const handleWordSelection = async (isKnow) => {
  if (!currentWord.value) return
  
  try {
    const res = await selectWord({
      wordId: currentWord.value.id,
      type: isKnow ? 1 : 2  // 1为认识，2为不认识
    })
    
    if (res.code === 200) {
      ElMessage.success(isKnow ? '已标记为认识' : '已标记为不认识')
      
      // 显示下一个单词
      currentIndex.value++
      if (currentIndex.value < wordList.value.length) {
        currentWord.value = wordList.value[currentIndex.value]
      } else {
        // 没有更多单词了，重新加载
        await loadWords()
      }
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('选择单词失败:', error)
    ElMessage.error('操作失败')
  }
}

// 加载用户记录
const loadUserRecords = async () => {
  recordsLoading.value = true
  try {
    const params = {
      pageNumber: recordsPage.value,
      pageSize: recordsPageSize.value
    }
    
    let res
    if (recordFilter.value === 'known') {
      res = await getUserWordPageKnown(params)
    } else if (recordFilter.value === 'unknown') {
      res = await getUserWordPageUnknown(params)
    } else {
      res = await getUserWordPage(params)
    }
    
    if (res.code === 200 && res.data) {
      userRecords.value = res.data.list || []
      recordsTotal.value = res.data.total || 0
    } else {
      ElMessage.error('加载记录失败')
    }
  } catch (error) {
    console.error('加载记录失败:', error)
    ElMessage.error('加载记录失败')
  } finally {
    recordsLoading.value = false
  }
}

// 重置筛选并重新加载
const resetAndLoadRecords = () => {
  recordsPage.value = 1
  loadUserRecords()
}

// 分页变化时重新加载
const handlePageChange = () => {
  loadUserRecords()
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
}

// 获取记录类型
const getRecordType = (record) => {
  const knowCount = record.knowCount || 0
  const notKnowCount = record.notKnowCount || 0
  
  if (knowCount > notKnowCount) {
    return 'success'  // 倾向于认识
  } else if (notKnowCount > knowCount) {
    return 'warning'  // 倾向于不认识
  }
  return 'info'  // 相等或都为0
}

// 获取记录状态
const getRecordStatus = (record) => {
  const knowCount = record.knowCount || 0
  const notKnowCount = record.notKnowCount || 0
  
  if (knowCount > notKnowCount) {
    return `倾向认识 (${knowCount}:${notKnowCount})`
  } else if (notKnowCount > knowCount) {
    return `倾向不认识 (${knowCount}:${notKnowCount})`
  } else if (knowCount === 0 && notKnowCount === 0) {
    return '未学习'
  } else {
    return `学习中 (${knowCount}:${notKnowCount})`
  }
}

// 页面初始化
onMounted(() => {
  loadWords()
  // 如果默认就是记录标签页，也要加载记录数据
  if (activeTab.value === 'records') {
    loadUserRecords()
  }
})
</script>

<style scoped>
.word-learning-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6fa;
}

.page-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  z-index: 1000;
}

.page-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.back-icon {
  cursor: pointer;
  font-size: 1.2rem;
  color: #409eff;
}

.word-tabs {
  flex: 1;
  padding-top: 48px;
  padding-bottom: 60px;
}

.learning-content, .records-content {
  padding: 16px;
}

.word-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.word-info {
  margin-bottom: 32px;
}

.word-text {
  font-size: 2.5rem;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px 0;
}

.word-meaning {
  font-size: 1.2rem;
  color: #666;
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.word-details {
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 0.9rem;
  color: #999;
}

.phonetic {
  font-style: italic;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.action-buttons .el-button {
  flex: 1;
  max-width: 120px;
  height: 48px;
  font-size: 1rem;
}

.loading-area {
  padding: 20px;
}

.no-word {
  text-align: center;
  padding: 40px 20px;
}

.filter-tabs {
  margin-bottom: 16px;
  text-align: center;
  padding: 0 16px;
}

.filter-tabs .el-radio-group {
  background: white;
  border-radius: 8px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.records-list {
  margin-bottom: 20px;
}

.record-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.record-word h4 {
  margin: 0 0 4px 0;
  font-size: 1.1rem;
  color: #333;
}

.record-word p {
  margin: 0;
  font-size: 0.9rem;
  color: #666;
}

.last-study {
  font-size: 0.8rem !important;
  color: #999 !important;
  margin-top: 4px !important;
}

.record-info {
  text-align: right;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-end;
}

.record-time {
  font-size: 0.8rem;
  color: #999;
}

.no-records {
  text-align: center;
  padding: 40px 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .word-text {
    font-size: 2rem;
  }
  
  .word-meaning {
    font-size: 1rem;
  }
  
  .word-details {
    flex-direction: column;
    gap: 8px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    max-width: none;
  }
  
  .record-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .record-info {
    align-items: flex-start;
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
  }
}
</style> 