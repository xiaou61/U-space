<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    :title="isEdit ? '编辑计划' : '创建计划'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form 
      ref="formRef" 
      :model="form" 
      :rules="rules" 
      label-width="100px"
      label-position="top"
    >
      <el-form-item label="计划名称" prop="planName">
        <el-input 
          v-model="form.planName" 
          placeholder="给你的计划起个名字吧"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="计划描述" prop="planDesc">
        <el-input 
          v-model="form.planDesc" 
          type="textarea"
          placeholder="简单描述一下你的计划（选填）"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="计划类型" prop="planType">
            <el-select v-model="form.planType" placeholder="选择类型" style="width: 100%">
              <el-option label="学习计划" :value="1">
                <span>📚 学习计划</span>
              </el-option>
              <el-option label="运动计划" :value="2">
                <span>🏃 运动计划</span>
              </el-option>
              <el-option label="阅读计划" :value="3">
                <span>📖 阅读计划</span>
              </el-option>
              <el-option label="编程计划" :value="4">
                <span>💻 编程计划</span>
              </el-option>
              <el-option label="其他计划" :value="99">
                <span>📋 其他计划</span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标类型" prop="targetType">
            <el-select v-model="form.targetType" placeholder="选择目标类型" style="width: 100%">
              <el-option label="每日目标" :value="1" />
              <el-option label="累计目标" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="目标数值" prop="targetValue">
            <el-input-number 
              v-model="form.targetValue" 
              :min="1" 
              :max="9999"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标单位" prop="targetUnit">
            <el-input 
              v-model="form.targetUnit" 
              placeholder="如：道题、分钟、页"
              maxlength="10"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="开始日期" prop="startDate">
            <el-date-picker
              v-model="form.startDate"
              type="date"
              placeholder="选择开始日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="form.endDate"
              type="date"
              placeholder="不选则为长期计划"
              style="width: 100%"
              value-format="YYYY-MM-DD"
              :disabled-date="disabledEndDate"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="每日开始时间">
            <el-time-picker
              v-model="form.dailyStartTime"
              placeholder="选择时间（选填）"
              style="width: 100%"
              format="HH:mm"
              value-format="HH:mm"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="每日截止时间">
            <el-time-picker
              v-model="form.dailyEndTime"
              placeholder="选择时间（选填）"
              style="width: 100%"
              format="HH:mm"
              value-format="HH:mm"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="重复设置">
        <el-radio-group v-model="form.repeatType">
          <el-radio :label="1">每天</el-radio>
          <el-radio :label="2">工作日</el-radio>
          <el-radio :label="3">自定义</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.repeatType === 3" label="选择重复日">
        <el-checkbox-group v-model="repeatDaysArray">
          <el-checkbox label="1">周一</el-checkbox>
          <el-checkbox label="2">周二</el-checkbox>
          <el-checkbox label="3">周三</el-checkbox>
          <el-checkbox label="4">周四</el-checkbox>
          <el-checkbox label="5">周五</el-checkbox>
          <el-checkbox label="6">周六</el-checkbox>
          <el-checkbox label="7">周日</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-divider content-position="left">提醒设置</el-divider>

      <el-form-item>
        <el-switch v-model="form.remindEnabled" active-text="开启提醒" />
      </el-form-item>

      <template v-if="form.remindEnabled">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始前提醒">
              <el-select v-model="form.remindBefore" placeholder="选择提醒时间" style="width: 100%">
                <el-option label="不提醒" :value="0" />
                <el-option label="5分钟前" :value="5" />
                <el-option label="10分钟前" :value="10" />
                <el-option label="15分钟前" :value="15" />
                <el-option label="30分钟前" :value="30" />
                <el-option label="1小时前" :value="60" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止前提醒">
              <el-select v-model="form.remindDeadline" placeholder="选择提醒时间" style="width: 100%">
                <el-option label="不提醒" :value="0" />
                <el-option label="5分钟前" :value="5" />
                <el-option label="10分钟前" :value="10" />
                <el-option label="15分钟前" :value="15" />
                <el-option label="30分钟前" :value="30" />
                <el-option label="1小时前" :value="60" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </template>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ isEdit ? '保存修改' : '创建计划' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import planApi from '@/api/plan'

const props = defineProps({
  modelValue: Boolean,
  planData: Object
})

const emit = defineEmits(['update:modelValue', 'success'])

// 是否编辑模式
const isEdit = computed(() => !!props.planData?.id)

// 表单引用
const formRef = ref(null)
const submitting = ref(false)

// 重复日数组（用于自定义模式）
const repeatDaysArray = ref([])

// 表单数据
const form = ref({
  planName: '',
  planDesc: '',
  planType: 4, // 默认编程计划
  targetType: 1, // 默认每日目标
  targetValue: 1,
  targetUnit: '道题',
  startDate: new Date().toISOString().split('T')[0],
  endDate: null,
  dailyStartTime: null,
  dailyEndTime: null,
  repeatType: 1, // 默认每天
  repeatDays: null,
  remindEnabled: true,
  remindBefore: 10,
  remindDeadline: 30
})

// 表单校验规则
const rules = {
  planName: [
    { required: true, message: '请输入计划名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在2到50个字符', trigger: 'blur' }
  ],
  planType: [
    { required: true, message: '请选择计划类型', trigger: 'change' }
  ],
  targetType: [
    { required: true, message: '请选择目标类型', trigger: 'change' }
  ],
  targetValue: [
    { required: true, message: '请输入目标数值', trigger: 'blur' }
  ],
  targetUnit: [
    { required: true, message: '请输入目标单位', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ]
}

// 监听弹窗打开，初始化表单数据
watch(() => props.modelValue, (val) => {
  if (val) {
    if (props.planData) {
      // 编辑模式，填充数据
      form.value = { ...props.planData }
      if (props.planData.repeatDays) {
        repeatDaysArray.value = props.planData.repeatDays.split(',')
      } else {
        repeatDaysArray.value = []
      }
    } else {
      // 创建模式，重置表单
      resetForm()
    }
  }
})

// 监听重复日数组变化
watch(repeatDaysArray, (val) => {
  form.value.repeatDays = val.length > 0 ? val.join(',') : null
})

// 禁用结束日期（不能早于开始日期）
const disabledEndDate = (date) => {
  if (!form.value.startDate) return false
  return date < new Date(form.value.startDate)
}

// 重置表单
const resetForm = () => {
  form.value = {
    planName: '',
    planDesc: '',
    planType: 4,
    targetType: 1,
    targetValue: 1,
    targetUnit: '道题',
    startDate: new Date().toISOString().split('T')[0],
    endDate: null,
    dailyStartTime: null,
    dailyEndTime: null,
    repeatType: 1,
    repeatDays: null,
    remindEnabled: true,
    remindBefore: 10,
    remindDeadline: 30
  }
  repeatDaysArray.value = []
  formRef.value?.resetFields()
}

// 关闭弹窗
const handleClose = () => {
  emit('update:modelValue', false)
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const submitData = { ...form.value }
    // 处理布尔值转数字
    submitData.remindEnabled = submitData.remindEnabled ? 1 : 0
    
    if (isEdit.value) {
      await planApi.updatePlan(props.planData.id, submitData)
      ElMessage.success('计划修改成功')
    } else {
      await planApi.createPlan(submitData)
      ElMessage.success('计划创建成功')
    }
    
    emit('success')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
:deep(.el-dialog__body) {
  padding-top: 10px;
  max-height: 65vh;
  overflow-y: auto;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

:deep(.el-divider__text) {
  color: #666;
  font-size: 14px;
}
</style>
