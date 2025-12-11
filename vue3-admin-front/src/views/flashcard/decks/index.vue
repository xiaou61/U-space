<template>
  <div class="flashcard-decks-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>卡组管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建卡组
      </el-button>
    </div>

    <!-- 卡组列表 -->
    <el-card>
      <el-table :data="deckList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="卡组名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="cardCount" label="闪卡数量" width="100" align="center" />
        <el-table-column prop="isOfficial" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.userId === 0 ? 'warning' : 'info'" size="small">
              {{ row.userId === 0 ? '官方' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isPublic" label="公开" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isPublic === 1 ? 'success' : 'info'" size="small">
              {{ row.isPublic === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link size="small" @click="handleGenerateFromList(row)">
              <el-icon><Document /></el-icon>
              从题库生成
            </el-button>
            <el-button type="primary" link size="small" @click="handleViewCards(row)">
              <el-icon><View /></el-icon>
              查看闪卡
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑卡组弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑卡组' : '新建卡组'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入卡组名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入卡组描述"
          />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <el-input v-model="formData.coverImage" placeholder="请输入封面图URL" />
        </el-form-item>
        <el-form-item label="是否公开" prop="isPublic">
          <el-switch
            v-model="formData.isPublic"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 闪卡列表弹窗 -->
    <el-dialog
      v-model="cardsDialogVisible"
      :title="`${currentDeck?.name || ''} - 闪卡列表`"
      width="900px"
    >
      <div class="cards-toolbar">
        <el-button type="primary" size="small" @click="handleCreateCard">
          <el-icon><Plus /></el-icon>
          手动添加
        </el-button>
        <el-button type="success" size="small" @click="handleGenerateFromQuestions">
          <el-icon><Document /></el-icon>
          从题库生成
        </el-button>
      </div>
      <el-table :data="cardList" v-loading="cardsLoading" stripe max-height="400">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="frontContent" label="正面(问题)" min-width="200" show-overflow-tooltip />
        <el-table-column prop="backContent" label="背面(答案)" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sourceType" label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.sourceType === 1 ? 'success' : 'info'" size="small">
              {{ row.sourceType === 1 ? '题库' : '手动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditCard(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDeleteCard(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 从题库生成闪卡弹窗 -->
    <el-dialog
      v-model="generateDialogVisible"
      title="从题库生成闪卡"
      width="800px"
    >
      <!-- 筛选区域 -->
      <div class="filter-area">
        <el-select 
          v-model="selectedQuestionSet" 
          placeholder="选择题单" 
          clearable
          filterable
          style="width: 300px;"
          @change="loadQuestionsBySet"
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
      <el-table 
        :data="questionList" 
        v-loading="questionsLoading" 
        stripe 
        max-height="350"
        @selection-change="handleSelectionChange"
        style="margin-top: 16px;"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="题目标题" min-width="300" show-overflow-tooltip />
        <el-table-column prop="difficulty" label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getDifficultyType(row.difficulty)" size="small">
              {{ getDifficultyText(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 已选择提示 -->
      <div class="selected-tip" v-if="selectedQuestions.length > 0">
        已选择 {{ selectedQuestions.length }} 道题目
      </div>

      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="generating"
          :disabled="selectedQuestions.length === 0"
          @click="handleConfirmGenerate"
        >
          生成闪卡 ({{ selectedQuestions.length }})
        </el-button>
      </template>
    </el-dialog>

    <!-- 新建/编辑闪卡弹窗 -->
    <el-dialog
      v-model="cardDialogVisible"
      :title="isEditCard ? '编辑闪卡' : '新建闪卡'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="cardFormRef" :model="cardFormData" :rules="cardFormRules" label-width="100px">
        <el-form-item label="正面(问题)" prop="frontContent">
          <el-input
            v-model="cardFormData.frontContent"
            type="textarea"
            :rows="4"
            placeholder="请输入问题内容"
          />
        </el-form-item>
        <el-form-item label="背面(答案)" prop="backContent">
          <el-input
            v-model="cardFormData.backContent"
            type="textarea"
            :rows="6"
            placeholder="请输入答案内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cardDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="cardSubmitting" @click="handleSubmitCard">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View, Document } from '@element-plus/icons-vue'
import { flashcardApi } from '@/api/flashcard'
import { interviewApi } from '@/api/interview'

// 卡组列表
const deckList = ref([])
const loading = ref(false)

// 卡组表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const submitting = ref(false)
const currentDeckId = ref(null)
const formData = reactive({
  name: '',
  description: '',
  coverImage: '',
  isPublic: 0
})
const formRules = {
  name: [{ required: true, message: '请输入卡组名称', trigger: 'blur' }]
}

// 闪卡列表
const cardsDialogVisible = ref(false)
const currentDeck = ref(null)
const cardList = ref([])
const cardsLoading = ref(false)

// 从题库生成
const generateDialogVisible = ref(false)
const questionSets = ref([])
const selectedQuestionSet = ref(null)
const questionList = ref([])
const questionsLoading = ref(false)
const selectedQuestions = ref([])
const generating = ref(false)

// 闪卡表单
const cardDialogVisible = ref(false)
const isEditCard = ref(false)
const cardFormRef = ref()
const cardSubmitting = ref(false)
const currentCardId = ref(null)
const cardFormData = reactive({
  frontContent: '',
  backContent: ''
})
const cardFormRules = {
  frontContent: [{ required: true, message: '请输入正面内容', trigger: 'blur' }],
  backContent: [{ required: true, message: '请输入背面内容', trigger: 'blur' }]
}

// 加载卡组列表
const loadDecks = async () => {
  loading.value = true
  try {
    const response = await flashcardApi.getOfficialDecks()
    deckList.value = response || []
  } catch (error) {
    console.error('加载卡组失败:', error)
    ElMessage.error('加载卡组失败')
  } finally {
    loading.value = false
  }
}

// 新建卡组
const handleCreate = () => {
  isEdit.value = false
  currentDeckId.value = null
  Object.assign(formData, { name: '', description: '', coverImage: '', isPublic: 0 })
  dialogVisible.value = true
}

// 编辑卡组
const handleEdit = (row) => {
  isEdit.value = true
  currentDeckId.value = row.id
  Object.assign(formData, {
    name: row.name,
    description: row.description,
    coverImage: row.coverImage,
    isPublic: row.isPublic
  })
  dialogVisible.value = true
}

// 提交卡组表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await flashcardApi.updateDeck(currentDeckId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await flashcardApi.createDeck(formData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadDecks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

// 删除卡组
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除卡组"${row.name}"吗？删除后将无法恢复。`, '警告', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await flashcardApi.deleteDeck(row.id)
    ElMessage.success('删除成功')
    loadDecks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 查看闪卡列表
const handleViewCards = async (row) => {
  currentDeck.value = row
  cardsDialogVisible.value = true
  await loadCards(row.id)
}

// 加载闪卡列表
const loadCards = async (deckId) => {
  cardsLoading.value = true
  try {
    const response = await flashcardApi.getCardsByDeck(deckId)
    cardList.value = response || []
  } catch (error) {
    console.error('加载闪卡失败:', error)
    ElMessage.error('加载闪卡失败')
  } finally {
    cardsLoading.value = false
  }
}

// 从卡组列表直接生成
const handleGenerateFromList = async (row) => {
  currentDeck.value = row
  generateDialogVisible.value = true
  selectedQuestionSet.value = null
  questionList.value = []
  selectedQuestions.value = []
  await loadQuestionSets()
}

// 从闪卡列表弹窗生成
const handleGenerateFromQuestions = async () => {
  generateDialogVisible.value = true
  selectedQuestionSet.value = null
  questionList.value = []
  selectedQuestions.value = []
  await loadQuestionSets()
}

// 加载题单列表
const loadQuestionSets = async () => {
  try {
    const response = await interviewApi.getQuestionSets({ pageNum: 1, pageSize: 100 })
    questionSets.value = response?.records || []
  } catch (error) {
    console.error('加载题单失败:', error)
  }
}

// 根据题单加载题目
const loadQuestionsBySet = async () => {
  if (!selectedQuestionSet.value) {
    questionList.value = []
    return
  }
  questionsLoading.value = true
  try {
    const response = await interviewApi.getQuestionsBySetId(selectedQuestionSet.value)
    questionList.value = response || []
  } catch (error) {
    console.error('加载题目失败:', error)
  } finally {
    questionsLoading.value = false
  }
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

// 确认生成
const handleConfirmGenerate = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请至少选择一道题目')
    return
  }
  generating.value = true
  try {
    await flashcardApi.generateFromCategory({
      deckId: currentDeck.value.id,
      questionIds: selectedQuestions.value.map(q => q.id)
    })
    ElMessage.success(`成功生成 ${selectedQuestions.value.length} 张闪卡`)
    generateDialogVisible.value = false
    loadCards(currentDeck.value.id)
    loadDecks()
  } catch (error) {
    console.error('生成失败:', error)
    ElMessage.error(error.message || '生成失败')
  } finally {
    generating.value = false
  }
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const map = { 1: '简单', 2: '中等', 3: '困难' }
  return map[difficulty] || '未知'
}

// 获取难度样式
const getDifficultyType = (difficulty) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[difficulty] || 'info'
}

// 新建闪卡
const handleCreateCard = () => {
  isEditCard.value = false
  currentCardId.value = null
  Object.assign(cardFormData, { frontContent: '', backContent: '' })
  cardDialogVisible.value = true
}

// 编辑闪卡
const handleEditCard = (row) => {
  isEditCard.value = true
  currentCardId.value = row.id
  Object.assign(cardFormData, {
    frontContent: row.frontContent,
    backContent: row.backContent
  })
  cardDialogVisible.value = true
}

// 提交闪卡表单
const handleSubmitCard = async () => {
  try {
    await cardFormRef.value.validate()
    cardSubmitting.value = true
    
    if (isEditCard.value) {
      await flashcardApi.updateCard(currentCardId.value, cardFormData)
      ElMessage.success('更新成功')
    } else {
      await flashcardApi.createCard({
        ...cardFormData,
        deckId: currentDeck.value.id
      })
      ElMessage.success('创建成功')
    }
    
    cardDialogVisible.value = false
    loadCards(currentDeck.value.id)
    loadDecks() // 刷新卡组列表以更新数量
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    cardSubmitting.value = false
  }
}

// 删除闪卡
const handleDeleteCard = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这张闪卡吗？', '警告', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await flashcardApi.deleteCard(row.id)
    ElMessage.success('删除成功')
    loadCards(currentDeck.value.id)
    loadDecks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadDecks()
})
</script>

<style lang="scss" scoped>
.flashcard-decks-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.cards-toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.filter-area {
  margin-bottom: 8px;
}

.selected-tip {
  margin-top: 12px;
  padding: 8px 12px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 14px;
}
</style>
