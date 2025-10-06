<template>
  <el-dialog
    v-model="visible"
    title="发表评论"
    width="500px"
    :before-close="handleClose"
  >
    <div v-if="moment" class="moment-preview">
      <div class="user-info">
        <el-avatar :size="40" :src="moment.userAvatar">
          {{ moment.userNickname?.charAt(0) }}
        </el-avatar>
        <div class="user-detail">
          <div class="user-name">{{ moment.userNickname }}</div>
          <div class="publish-time">{{ moment.createTime }}</div>
        </div>
      </div>
      <div class="moment-content">
        {{ moment.content }}
      </div>
    </div>

    <el-divider />

    <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
      <el-form-item prop="content">
        <el-input
          ref="textareaRef"
          v-model="form.content"
          type="textarea"
          placeholder="写下你的评论..."
          :rows="4"
          maxlength="200"
          show-word-limit
          autofocus
        />
      </el-form-item>

      <!-- 表情选择器 -->
      <el-form-item>
        <EmojiPicker @select="insertEmoji" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleComment" :loading="commenting">
        发表评论
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { publishComment } from '@/api/moment'
import EmojiPicker from '@/components/EmojiPicker.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  moment: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'commented'])

// 响应式数据
const formRef = ref(null)
const textareaRef = ref(null)
const commenting = ref(false)

// 表单数据
const form = reactive({
  content: ''
})

// 表单验证规则
const rules = {
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 1, max: 200, message: '评论长度应在1-200字符之间', trigger: 'blur' }
  ]
}

// 对话框可见性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 监听对话框打开，重置表单
watch(visible, (newVal) => {
  if (newVal) {
    resetForm()
  }
})

// 重置表单
const resetForm = () => {
  form.content = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 发表评论
const handleComment = async () => {
  if (!formRef.value || !props.moment) return

  try {
    await formRef.value.validate()
    
    commenting.value = true
    
    const data = {
      momentId: props.moment.id,
      content: form.content
    }
    
    const commentId = await publishComment(data)
    
    ElMessage.success('评论发表成功！')
    
    // 构造评论对象返回给父组件
    const comment = {
      id: commentId,
      content: form.content,
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
      canDelete: true // 自己的评论可以删除
    }
    
    emit('commented', comment)
    visible.value = false
  } catch (error) {
    if (error.message) {
      ElMessage.error('评论发表失败：' + error.message)
    }
  } finally {
    commenting.value = false
  }
}

// 插入表情
const insertEmoji = (emoji) => {
  const textarea = textareaRef.value?.$refs?.textarea
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const text = form.content
    
    form.content = text.substring(0, start) + emoji + text.substring(end)
    
    // 恢复光标位置
    setTimeout(() => {
      textarea.selectionStart = textarea.selectionEnd = start + emoji.length
      textarea.focus()
    }, 10)
  } else {
    form.content += emoji
  }
}
</script>

<style scoped>
.moment-preview {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.user-detail {
  margin-left: 10px;
}

.user-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.publish-time {
  color: #999;
  font-size: 12px;
  margin-top: 2px;
}

.moment-content {
  color: #333;
  line-height: 1.5;
  font-size: 14px;
}

:deep(.el-divider) {
  margin: 15px 0;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style> 