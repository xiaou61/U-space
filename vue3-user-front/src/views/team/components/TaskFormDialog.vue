<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="isEdit ? '编辑任务' : '创建任务'"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="任务名称" prop="taskName">
        <el-input 
          v-model="form.taskName" 
          placeholder="请输入任务名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="任务描述" prop="taskDesc">
        <el-input 
          v-model="form.taskDesc" 
          type="textarea"
          :rows="3"
          placeholder="请输入任务描述（选填）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="任务类型" prop="taskType">
        <el-radio-group v-model="form.taskType">
          <el-radio :value="1">每日任务</el-radio>
          <el-radio :value="2">每周任务</el-radio>
          <el-radio :value="3">单次任务</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="任务时间" v-if="form.taskType === 3">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      
      <el-form-item label="排序权重" prop="sortOrder">
        <el-input-number 
          v-model="form.sortOrder" 
          :min="0" 
          :max="999"
          placeholder="数字越大越靠前"
        />
        <span class="form-tip">数字越大越靠前</span>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ isEdit ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import teamApi from '@/api/team'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  teamId: { type: [String, Number], required: true },
  taskData: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'success'])

const isEdit = computed(() => !!props.taskData)

const formRef = ref()
const submitting = ref(false)
const dateRange = ref([])

const form = ref({
  taskName: '',
  taskDesc: '',
  taskType: 1,
  startDate: null,
  endDate: null,
  sortOrder: 0
})

const rules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  taskType: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ]
}

// 监听弹窗打开
watch(() => props.modelValue, (val) => {
  if (val) {
    if (props.taskData) {
      // 编辑模式
      form.value = { ...props.taskData }
      if (props.taskData.startDate && props.taskData.endDate) {
        dateRange.value = [props.taskData.startDate, props.taskData.endDate]
      }
    } else {
      // 创建模式
      resetForm()
    }
  }
})

// 监听日期范围
watch(dateRange, (val) => {
  if (val && val.length === 2) {
    form.value.startDate = val[0]
    form.value.endDate = val[1]
  } else {
    form.value.startDate = null
    form.value.endDate = null
  }
})

const resetForm = () => {
  form.value = {
    taskName: '',
    taskDesc: '',
    taskType: 1,
    startDate: null,
    endDate: null,
    sortOrder: 0
  }
  dateRange.value = []
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const data = { ...form.value }
    
    if (isEdit.value) {
      await teamApi.updateTask(props.teamId, props.taskData.id, data)
      ElMessage.success('更新成功')
    } else {
      await teamApi.createTask(props.teamId, data)
      ElMessage.success('创建成功')
    }
    
    emit('update:modelValue', false)
    emit('success')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}
</style>
