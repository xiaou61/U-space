<template>
  <div class="deck-editor">
    <div class="page-header">
      <el-button :icon="ArrowLeft" text @click="goBack">返回</el-button>
      <h1 class="page-title">{{ isEdit ? '编辑卡组' : '创建卡组' }}</h1>
    </div>

    <el-card class="editor-card">
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules" 
        label-position="top"
        v-loading="loading"
      >
        <el-form-item label="卡组名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入卡组名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea"
            placeholder="请输入卡组描述"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面图片">
          <div class="cover-upload">
            <div class="cover-preview" v-if="form.coverImage">
              <img :src="form.coverImage" alt="封面预览" @error="handleImageError" />
              <div class="cover-actions">
                <el-button type="danger" size="small" @click="form.coverImage = ''">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
            <el-upload
              v-else
              class="cover-uploader"
              :auto-upload="false"
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
              :on-change="handleCoverChange"
              accept="image/*"
            >
              <div class="upload-placeholder">
                <el-icon class="upload-icon" :class="{ 'is-loading': coverUploading }">
                  <Loading v-if="coverUploading" />
                  <Plus v-else />
                </el-icon>
                <span class="upload-text">{{ coverUploading ? '上传中...' : '点击上传封面' }}</span>
                <span class="upload-hint">JPG/PNG/GIF，不超过2MB</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-input 
            v-model="form.tags" 
            placeholder="多个标签用逗号分隔，如：Java,面试,基础"
          />
        </el-form-item>

        <el-form-item label="公开设置" prop="isPublic">
          <el-switch 
            v-model="form.isPublic"
            active-text="公开"
            inactive-text="私有"
          />
          <span class="hint">公开的卡组可以被其他用户搜索和Fork</span>
        </el-form-item>

        <el-form-item class="form-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '创建卡组' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Delete, Loading } from '@element-plus/icons-vue'
import { flashcardApi } from '@/api/flashcard'
import { uploadSingle } from '@/api/upload'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)
const deckId = computed(() => route.params.id)

const form = ref({
  name: '',
  description: '',
  coverImage: '',
  tags: '',
  isPublic: false
})

const rules = {
  name: [
    { required: true, message: '请输入卡组名称', trigger: 'blur' },
    { max: 100, message: '名称不能超过100个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
  ]
}

// 加载卡组详情（编辑模式）
const loadDeckDetail = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const data = await flashcardApi.getDeckById(deckId.value)
    form.value = {
      name: data.name || '',
      description: data.description || '',
      coverImage: data.coverImage || '',
      tags: data.tags || '',
      isPublic: data.isPublic || false
    }
  } catch (error) {
    console.error('加载卡组失败:', error)
    ElMessage.error('加载卡组失败')
    router.push('/flashcard/my')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await flashcardApi.updateDeck({
        id: Number(deckId.value),
        ...form.value
      })
      ElMessage.success('卡组更新成功')
    } else {
      const newDeckId = await flashcardApi.createDeck(form.value)
      ElMessage.success('卡组创建成功')
      router.push(`/flashcard/deck/${newDeckId}`)
      return
    }
    router.push(`/flashcard/deck/${deckId.value}`)
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  if (isEdit.value) {
    router.push(`/flashcard/deck/${deckId.value}`)
  } else {
    router.push('/flashcard/my')
  }
}

const handleImageError = (e) => {
  e.target.style.display = 'none'
}

// 封面上传相关
const coverUploading = ref(false)

const beforeCoverUpload = (rawFile) => {
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'].includes(rawFile.type)
  const isLt2M = rawFile.size / 1024 / 1024 < 2

  if (!isValidType) {
    ElMessage.error('封面只能是 JPG、PNG、GIF 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('封面大小不能超过 2MB!')
    return false
  }
  return true
}

const handleCoverChange = async (uploadFile) => {
  if (!uploadFile.raw) return
  
  if (!beforeCoverUpload(uploadFile.raw)) return
  
  try {
    coverUploading.value = true
    const response = await uploadSingle(uploadFile.raw, 'flashcard', 'cover')
    form.value.coverImage = response.data?.accessUrl || response.accessUrl || response.url || response
    ElMessage.success('封面上传成功')
  } catch (error) {
    console.error('封面上传失败:', error)
    ElMessage.error('封面上传失败，请重试')
  } finally {
    coverUploading.value = false
  }
}

onMounted(() => {
  loadDeckDetail()
})
</script>

<style lang="scss" scoped>
.deck-editor {
  max-width: 700px;
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

.editor-card {
  border-radius: 12px;
  
  :deep(.el-card__body) {
    padding: 32px;
  }
}

.cover-upload {
  width: 100%;
}

.cover-preview {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  max-width: 300px;
  border: 1px solid var(--el-border-color);
  
  img {
    width: 100%;
    height: 180px;
    object-fit: cover;
    display: block;
  }
  
  .cover-actions {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 8px;
    background: linear-gradient(transparent, rgba(0,0,0,0.6));
    display: flex;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
  }
  
  &:hover .cover-actions {
    opacity: 1;
  }
}

.cover-uploader {
  :deep(.el-upload) {
    width: 300px;
    height: 180px;
    border: 2px dashed var(--el-border-color);
    border-radius: 12px;
    cursor: pointer;
    transition: border-color 0.3s;
    
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  .upload-icon {
    font-size: 32px;
    color: var(--el-text-color-placeholder);
    
    &.is-loading {
      animation: spin 1s linear infinite;
    }
  }
  
  .upload-text {
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }
  
  .upload-hint {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.hint {
  margin-left: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.form-actions {
  margin-top: 32px;
  margin-bottom: 0;
  
  :deep(.el-form-item__content) {
    justify-content: flex-end;
    gap: 12px;
  }
}

@media (max-width: 640px) {
  .deck-editor {
    padding: 16px;
  }
  
  .editor-card {
    :deep(.el-card__body) {
      padding: 20px;
    }
  }
}
</style>
