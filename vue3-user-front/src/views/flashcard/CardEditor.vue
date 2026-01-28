<template>
  <div class="card-editor">
    <div class="page-header">
      <el-button :icon="ArrowLeft" text @click="goBack">返回</el-button>
      <h1 class="page-title">管理闪卡</h1>
    </div>

    <div class="editor-tabs">
      <el-radio-group v-model="activeTab">
        <el-radio-button value="single">单张添加</el-radio-button>
        <el-radio-button value="batch">批量添加</el-radio-button>
        <el-radio-button value="import">从题库导入</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 单张添加 -->
    <el-card v-if="activeTab === 'single'" class="editor-card">
      <el-form ref="singleFormRef" :model="singleForm" :rules="singleRules" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="正面内容" prop="frontContent">
              <el-input 
                v-model="singleForm.frontContent" 
                type="textarea"
                placeholder="输入卡片正面内容（问题）"
                :rows="6"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="背面内容" prop="backContent">
              <el-input 
                v-model="singleForm.backContent" 
                type="textarea"
                placeholder="输入卡片背面内容（答案）"
                :rows="6"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="内容类型">
              <el-select v-model="singleForm.contentType" style="width: 100%">
                <el-option :value="1" label="纯文本" />
                <el-option :value="2" label="Markdown" />
                <el-option :value="3" label="代码" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-input v-model="singleForm.tags" placeholder="多个标签用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="handleSingleSubmit" :loading="submitting">
            添加闪卡
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量添加 -->
    <el-card v-if="activeTab === 'batch'" class="editor-card">
      <div class="batch-hint">
        <el-alert type="info" :closable="false">
          每行一张卡片，使用 <code>|||</code> 分隔正面和背面内容。<br>
          例如：什么是闭包？|||闭包是指有权访问另一个函数作用域中的变量的函数。
        </el-alert>
      </div>

      <el-form ref="batchFormRef" :model="batchForm" label-position="top">
        <el-form-item label="批量内容">
          <el-input 
            v-model="batchForm.content" 
            type="textarea"
            placeholder="每行一张卡片，使用 ||| 分隔正面和背面"
            :rows="12"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="内容类型">
              <el-select v-model="batchForm.contentType" style="width: 100%">
                <el-option :value="1" label="纯文本" />
                <el-option :value="2" label="Markdown" />
                <el-option :value="3" label="代码" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-input v-model="batchForm.tags" placeholder="应用到所有卡片" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="handleBatchSubmit" :loading="submitting">
            批量添加 ({{ parsedCards.length }} 张)
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 从题库导入 -->
    <el-card v-if="activeTab === 'import'" class="editor-card">
      <ImportDialog 
        :deck-id="deckId" 
        @imported="handleImported"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { flashcardApi } from '@/api/flashcard'
import ImportDialog from './components/ImportDialog.vue'

const router = useRouter()
const route = useRoute()

const deckId = computed(() => Number(route.params.deckId))
const activeTab = ref('single')
const submitting = ref(false)

// 单张表单
const singleFormRef = ref(null)
const singleForm = ref({
  frontContent: '',
  backContent: '',
  contentType: 1,
  tags: ''
})

const singleRules = {
  frontContent: [{ required: true, message: '请输入正面内容', trigger: 'blur' }],
  backContent: [{ required: true, message: '请输入背面内容', trigger: 'blur' }]
}

// 批量表单
const batchForm = ref({
  content: '',
  contentType: 1,
  tags: ''
})

// 解析批量内容
const parsedCards = computed(() => {
  if (!batchForm.value.content) return []
  return batchForm.value.content
    .split('\n')
    .filter(line => line.trim() && line.includes('|||'))
    .map(line => {
      const [front, back] = line.split('|||').map(s => s.trim())
      return { frontContent: front, backContent: back }
    })
})

// 单张提交
const handleSingleSubmit = async () => {
  const valid = await singleFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await flashcardApi.createCard({
      deckId: deckId.value,
      ...singleForm.value
    })
    ElMessage.success('闪卡添加成功')
    singleForm.value = {
      frontContent: '',
      backContent: '',
      contentType: 1,
      tags: ''
    }
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  } finally {
    submitting.value = false
  }
}

// 批量提交
const handleBatchSubmit = async () => {
  if (parsedCards.value.length === 0) {
    ElMessage.warning('没有可添加的卡片')
    return
  }

  submitting.value = true
  try {
    const cards = parsedCards.value.map(card => ({
      ...card,
      contentType: batchForm.value.contentType,
      tags: batchForm.value.tags
    }))

    const count = await flashcardApi.batchCreateCards({
      deckId: deckId.value,
      cards
    })
    ElMessage.success(`成功添加 ${count} 张闪卡`)
    batchForm.value.content = ''
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  } finally {
    submitting.value = false
  }
}

// 导入完成
const handleImported = (count) => {
  ElMessage.success(`成功导入 ${count} 张闪卡`)
}

const goBack = () => {
  router.push(`/flashcard/deck/${deckId.value}`)
}
</script>

<style lang="scss" scoped>
.card-editor {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
    color: var(--el-text-color-primary);
  }
}

.editor-tabs {
  margin-bottom: 20px;
}

.editor-card {
  border-radius: 12px;
  
  :deep(.el-card__body) {
    padding: 24px;
  }
}

.batch-hint {
  margin-bottom: 20px;
  
  code {
    background: var(--el-fill-color);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: monospace;
  }
}

@media (max-width: 768px) {
  .card-editor {
    padding: 16px;
  }
  
  .el-col {
    margin-bottom: 0;
  }
}
</style>
