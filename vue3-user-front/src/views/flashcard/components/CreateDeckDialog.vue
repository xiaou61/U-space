<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="isEdit ? '编辑卡组' : '新建卡组'"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="卡组名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入卡组名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="分类" prop="category">
        <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%;">
          <el-option label="Java基础" value="java" />
          <el-option label="Spring框架" value="spring" />
          <el-option label="MySQL数据库" value="mysql" />
          <el-option label="Redis缓存" value="redis" />
          <el-option label="计算机网络" value="network" />
          <el-option label="操作系统" value="os" />
          <el-option label="数据结构与算法" value="algorithm" />
          <el-option label="前端技术" value="frontend" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入卡组描述（可选）"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import flashcardApi from '@/api/flashcard'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  deckData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const loading = ref(false)

// 是否编辑模式
const isEdit = computed(() => !!props.deckData)

// 表单数据
const form = ref({
  name: '',
  category: '',
  description: ''
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入卡组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

// 监听弹窗打开
watch(() => props.modelValue, (val) => {
  if (val) {
    if (props.deckData) {
      // 编辑模式，填充数据
      form.value = {
        name: props.deckData.name || '',
        category: props.deckData.category || '',
        description: props.deckData.description || ''
      }
    } else {
      // 新建模式，重置表单
      form.value = {
        name: '',
        category: '',
        description: ''
      }
    }
  }
})

// 关闭弹窗
const handleClose = () => {
  emit('update:modelValue', false)
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    loading.value = true
    
    if (isEdit.value) {
      await flashcardApi.updateDeck(props.deckData.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await flashcardApi.createDeck(form.value)
      ElMessage.success('创建成功')
    }
    
    emit('success')
    handleClose()
  } catch (error) {
    if (error !== 'cancel' && error !== false) {
      console.error('保存卡组失败:', error)
      ElMessage.error(error.message || '保存失败')
    }
  } finally {
    loading.value = false
  }
}
</script>
