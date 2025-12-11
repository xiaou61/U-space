<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="从题库生成闪卡"
    width="700px"
    :close-on-click-modal="false"
  >
    <div class="generate-dialog">
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索面试题..."
          clearable
          @keyup.enter="searchQuestions"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="searchQuestions">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 分类筛选 -->
      <div class="filter-area">
        <el-select 
          v-model="selectedCategory" 
          placeholder="选择分类" 
          clearable
          @change="loadQuestions"
        >
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
        <el-select 
          v-model="selectedQuestionSet" 
          placeholder="选择题单" 
          clearable
          @change="loadQuestions"
        >
          <el-option
            v-for="qs in questionSets"
            :key="qs.id"
            :label="qs.title"
            :value="qs.id"
          />
        </el-select>
      </div>

      <!-- 题目列表 -->
      <div class="question-list">
        <div v-if="loading" class="loading-state">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        
        <div v-else-if="questions.length === 0" class="empty-state">
          <p>暂无题目，请选择分类或搜索</p>
        </div>
        
        <div v-else class="question-items">
          <div
            v-for="question in questions"
            :key="question.id"
            class="question-item"
            :class="{ 'selected': isSelected(question.id) }"
            @click="toggleSelect(question)"
          >
            <el-checkbox
              :model-value="isSelected(question.id)"
              @click.stop
              @change="toggleSelect(question)"
            />
            <div class="question-content">
              <div class="question-title">{{ question.title }}</div>
              <div class="question-meta">
                <span class="meta-tag">{{ question.categoryName }}</span>
                <span class="meta-difficulty" :class="getDifficultyClass(question.difficulty)">
                  {{ getDifficultyText(question.difficulty) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 已选择的题目 -->
      <div class="selected-area" v-if="selectedQuestions.length > 0">
        <div class="selected-header">
          <span>已选择 {{ selectedQuestions.length }} 道题目</span>
          <el-button type="text" @click="clearSelection">清空</el-button>
        </div>
        <div class="selected-tags">
          <el-tag
            v-for="q in selectedQuestions"
            :key="q.id"
            closable
            @close="removeSelection(q.id)"
          >
            {{ q.title.substring(0, 20) }}{{ q.title.length > 20 ? '...' : '' }}
          </el-tag>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button 
        type="primary" 
        :loading="generating"
        :disabled="selectedQuestions.length === 0"
        @click="handleGenerate"
      >
        生成闪卡 ({{ selectedQuestions.length }})
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Loading } from '@element-plus/icons-vue'
import flashcardApi from '@/api/flashcard'
import { interviewApi } from '@/api/interview'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  deckId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

// 搜索和筛选
const searchKeyword = ref('')
const selectedCategory = ref(null)
const selectedQuestionSet = ref(null)

// 数据
const categories = ref([])
const questionSets = ref([])
const questions = ref([])
const loading = ref(false)
const generating = ref(false)

// 已选择的题目
const selectedQuestions = ref([])

// 监听弹窗打开
watch(() => props.modelValue, (val) => {
  if (val) {
    loadCategories()
    loadQuestionSets()
    selectedQuestions.value = []
  }
})

// 加载分类
const loadCategories = async () => {
  try {
    const response = await interviewApi.getEnabledCategories()
    categories.value = response || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载题单
const loadQuestionSets = async () => {
  try {
    const response = await interviewApi.getPublicQuestionSets({ pageSize: 100 })
    questionSets.value = response?.records || []
  } catch (error) {
    console.error('加载题单失败:', error)
  }
}

// 加载题目
const loadQuestions = async () => {
  if (!selectedQuestionSet.value) {
    questions.value = []
    return
  }
  
  loading.value = true
  try {
    const response = await interviewApi.getQuestionsBySetId(selectedQuestionSet.value)
    questions.value = response || []
  } catch (error) {
    console.error('加载题目失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索题目
const searchQuestions = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  loading.value = true
  try {
    const response = await interviewApi.searchQuestions({
      keyword: searchKeyword.value,
      pageSize: 50
    })
    questions.value = response?.records || []
  } catch (error) {
    console.error('搜索题目失败:', error)
  } finally {
    loading.value = false
  }
}

// 判断是否选中
const isSelected = (id) => {
  return selectedQuestions.value.some(q => q.id === id)
}

// 切换选中
const toggleSelect = (question) => {
  const index = selectedQuestions.value.findIndex(q => q.id === question.id)
  if (index >= 0) {
    selectedQuestions.value.splice(index, 1)
  } else {
    selectedQuestions.value.push(question)
  }
}

// 移除选中
const removeSelection = (id) => {
  const index = selectedQuestions.value.findIndex(q => q.id === id)
  if (index >= 0) {
    selectedQuestions.value.splice(index, 1)
  }
}

// 清空选中
const clearSelection = () => {
  selectedQuestions.value = []
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const map = { 1: '简单', 2: '中等', 3: '困难' }
  return map[difficulty] || '未知'
}

// 获取难度样式
const getDifficultyClass = (difficulty) => {
  const map = { 1: 'easy', 2: 'medium', 3: 'hard' }
  return map[difficulty] || ''
}

// 关闭弹窗
const handleClose = () => {
  emit('update:modelValue', false)
  searchKeyword.value = ''
  selectedCategory.value = null
  selectedQuestionSet.value = null
  questions.value = []
  selectedQuestions.value = []
}

// 生成闪卡
const handleGenerate = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请至少选择一道题目')
    return
  }
  
  generating.value = true
  try {
    await flashcardApi.generateFromInterview({
      deckId: props.deckId,
      questionIds: selectedQuestions.value.map(q => q.id)
    })
    
    ElMessage.success(`成功生成 ${selectedQuestions.value.length} 张闪卡`)
    emit('success')
    handleClose()
  } catch (error) {
    console.error('生成闪卡失败:', error)
    ElMessage.error(error.message || '生成失败')
  } finally {
    generating.value = false
  }
}
</script>

<style lang="scss" scoped>
.generate-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-area {
  .el-input {
    width: 100%;
  }
}

.filter-area {
  display: flex;
  gap: 12px;
  
  .el-select {
    flex: 1;
  }
}

.question-list {
  border: 1px solid #eee;
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.loading-state, .empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #999;
  gap: 8px;
}

.question-items {
  padding: 8px;
}

.question-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: #f5f7fa;
  }
  
  &.selected {
    background: #ecf5ff;
    border-color: #409eff;
  }
}

.question-content {
  flex: 1;
  
  .question-title {
    font-size: 14px;
    color: #333;
    margin-bottom: 6px;
    line-height: 1.5;
  }
  
  .question-meta {
    display: flex;
    gap: 8px;
    
    .meta-tag {
      font-size: 12px;
      color: #999;
      background: #f5f5f5;
      padding: 2px 8px;
      border-radius: 4px;
    }
    
    .meta-difficulty {
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 4px;
      
      &.easy { background: #f0f9eb; color: #67c23a; }
      &.medium { background: #fdf6ec; color: #e6a23c; }
      &.hard { background: #fef0f0; color: #f56c6c; }
    }
  }
}

.selected-area {
  background: #f8f9fc;
  border-radius: 8px;
  padding: 12px;
  
  .selected-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 13px;
    color: #666;
  }
  
  .selected-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .el-tag {
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}
</style>
