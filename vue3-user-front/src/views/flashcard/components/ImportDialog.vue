<template>
  <div class="import-dialog">
    <div class="import-header">
      <h3>从面试题库导入</h3>
      <p class="hint">选择面试题导入为闪卡，题目标题作为正面，答案作为背面</p>
    </div>

    <div class="search-section">
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索题目..."
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <div class="question-list" v-loading="loading">
      <el-checkbox-group v-model="selectedIds">
        <div 
          v-for="question in questionList" 
          :key="question.id"
          class="question-item"
        >
          <el-checkbox :value="question.id">
            <span class="question-title">{{ question.title }}</span>
          </el-checkbox>
        </div>
      </el-checkbox-group>

      <el-empty v-if="!loading && questionList.length === 0" description="暂无题目" :image-size="60" />
    </div>

    <div class="import-footer">
      <span class="selected-count">已选 {{ selectedIds.length }} 题</span>
      <el-button type="primary" @click="handleImport" :loading="importing" :disabled="selectedIds.length === 0">
        导入为闪卡
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { interviewApi } from '@/api/interview'
import { flashcardApi } from '@/api/flashcard'

const props = defineProps({
  deckId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['imported'])

const loading = ref(false)
const importing = ref(false)
const searchKeyword = ref('')
const questionList = ref([])
const selectedIds = ref([])

let searchTimer = null

// 加载题目列表
const loadQuestions = async () => {
  loading.value = true
  try {
    // 获取题单列表，然后获取题目
    const sets = await interviewApi.getPublicQuestionSets({ page: 1, size: 5 })
    const questions = []
    
    for (const set of (sets.content || sets || []).slice(0, 3)) {
      const setQuestions = await interviewApi.getQuestionsBySetId(set.id)
      questions.push(...(setQuestions || []).slice(0, 10))
    }
    
    questionList.value = questions
  } catch (error) {
    console.error('加载题目失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    // TODO: 实现搜索
  }, 300)
}

// 导入
const handleImport = async () => {
  if (selectedIds.value.length === 0) return

  importing.value = true
  try {
    const count = await flashcardApi.importFromQuestionBank({
      deckId: props.deckId,
      questionIds: selectedIds.value
    })
    ElMessage.success(`成功导入 ${count} 张闪卡`)
    selectedIds.value = []
    emit('imported', count)
  } catch (error) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  loadQuestions()
})
</script>

<style lang="scss" scoped>
.import-dialog {
  min-height: 400px;
}

.import-header {
  margin-bottom: 20px;
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 8px 0;
    color: var(--el-text-color-primary);
  }
  
  .hint {
    margin: 0;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.search-section {
  margin-bottom: 16px;
}

.question-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 8px;
  margin-bottom: 16px;
}

.question-item {
  padding: 12px;
  border-radius: 6px;
  transition: background 0.2s;
  
  &:hover {
    background: var(--el-fill-color-light);
  }
  
  .question-title {
    font-size: 14px;
    color: var(--el-text-color-primary);
  }
}

.import-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .selected-count {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}
</style>
