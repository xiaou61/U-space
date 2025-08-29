<template>
  <div class="points-type-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      label-position="right"
    >
      <el-form-item label="类型名称" prop="typeName">
        <el-input
          v-model="form.typeName"
          placeholder="请输入积分类型名称"
          maxlength="50"
          show-word-limit
          clearable
        />
      </el-form-item>

      <el-form-item label="类型编码" prop="typeCode">
        <el-input
          v-model="form.typeCode"
          placeholder="请输入积分类型编码"
          maxlength="20"
          show-word-limit
          clearable
        />
        <div style="font-size: 12px; color: #999; margin-top: 4px;">
          用于系统内部识别，建议使用英文字母和数字
        </div>
      </el-form-item>

      <el-form-item label="类型描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入积分类型描述"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="图标URL" prop="iconUrl">
        <div class="icon-upload">
          <el-input
            v-model="form.iconUrl"
            placeholder="请输入图标URL或上传图标"
            clearable
          />
          <el-upload
            class="upload-demo"
            action="/uapi/file/upload"
            :show-file-list="false"
            :on-success="handleIconUploadSuccess"
            :before-upload="beforeIconUpload"
            accept="image/*"
            style="margin-top: 8px;"
          >
            <el-button size="small" type="primary">上传图标</el-button>
          </el-upload>
          <div v-if="form.iconUrl" class="icon-preview">
            <img :src="form.iconUrl" alt="图标预览" @error="handleImageError" />
          </div>
        </div>
      </el-form-item>

      <el-form-item label="排序顺序" prop="sortOrder">
        <el-input-number
          v-model="form.sortOrder"
          :min="1"
          :max="999"
          placeholder="请输入排序顺序"
          style="width: 200px;"
        />
        <div style="font-size: 12px; color: #999; margin-top: 4px;">
          数字越小排序越靠前
        </div>
      </el-form-item>

      <el-form-item label="是否启用" prop="isActive">
        <el-switch
          v-model="form.isActive"
          :active-value="1"
          :inactive-value="0"
          active-text="启用"
          inactive-text="禁用"
        />
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
        {{ isEdit ? '更新积分类型' : '创建积分类型' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  formData: {
    type: Object,
    default: () => ({})
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['submit', 'cancel'])

// 响应式数据
const formRef = ref()
const submitLoading = ref(false)

// 表单数据
const form = reactive({
  typeName: '',
  typeCode: '',
  description: '',
  iconUrl: '',
  sortOrder: 1,
  isActive: 1
})

// 表单验证规则
const rules = {
  typeName: [
    { required: true, message: '请输入积分类型名称', trigger: 'blur' },
    { min: 2, max: 50, message: '积分类型名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  typeCode: [
    { required: true, message: '请输入积分类型编码', trigger: 'blur' },
    { min: 2, max: 20, message: '积分类型编码长度在 2 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '编码只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过 200 个字符', trigger: 'blur' }
  ],
  iconUrl: [
    { pattern: /^https?:\/\/.+/, message: '请输入有效的图标URL', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序顺序', trigger: 'blur' },
    { type: 'number', min: 1, max: 999, message: '排序顺序范围 1-999', trigger: 'blur' }
  ],
  isActive: [
    { required: true, message: '请选择是否启用', trigger: 'change' }
  ]
}

// 监听 props 变化，更新表单数据
watch(() => props.formData, (newData) => {
  if (newData) {
    Object.assign(form, newData)
  }
}, { immediate: true, deep: true })

// 图标上传处理
const beforeIconUpload = (file) => {
  const isJPGorPNG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif' || file.type === 'image/svg+xml'
  const isLt1M = file.size / 1024 / 1024 < 1

  if (!isJPGorPNG) {
    ElMessage.error('上传图标只能是 JPG/PNG/GIF/SVG 格式!')
  }
  if (!isLt1M) {
    ElMessage.error('上传图标大小不能超过 1MB!')
  }
  return isJPGorPNG && isLt1M
}

const handleIconUploadSuccess = (response) => {
  if (response.code === 200) {
    form.iconUrl = response.data
    ElMessage.success('图标上传成功')
  } else {
    ElMessage.error(response.msg || '图标上传失败')
  }
}

const handleImageError = (e) => {
  e.target.style.display = 'none'
}

// 表单提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    emit('submit', { ...form })
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.points-type-form {
  padding: 20px 0;
}

.icon-upload {
  width: 100%;
}

.icon-preview {
  margin-top: 12px;
  width: 64px;
  height: 64px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

.form-footer {
  text-align: right;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.el-form-item {
  margin-bottom: 20px;
}

.upload-demo {
  display: inline-block;
}
</style> 