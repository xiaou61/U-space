<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    title="任务打卡"
    width="450px"
    :close-on-click-modal="false"
  >
    <div class="checkin-content" v-if="task">
      <!-- 任务信息 -->
      <div class="task-info">
        <div class="task-name">{{ task.planName }}</div>
        <div class="task-target">
          今日目标: <strong>{{ task.targetValue }} {{ task.targetUnit }}</strong>
        </div>
      </div>

      <!-- 打卡表单 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="今日完成量" prop="actualValue">
          <div class="value-input">
            <el-input-number 
              v-model="form.actualValue" 
              :min="0"
              :max="9999"
              size="large"
            />
            <span class="unit">{{ task.targetUnit }}</span>
          </div>
          <div class="quick-buttons">
            <el-button 
              v-for="percent in [50, 75, 100, 120]" 
              :key="percent"
              size="small"
              @click="setQuickValue(percent)"
            >
              {{ percent }}%
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="打卡心得（选填）">
          <el-input
            v-model="form.remark"
            type="textarea"
            placeholder="记录今天的感受或收获..."
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <!-- 打卡进度 -->
      <div class="progress-section">
        <div class="progress-label">
          <span>完成进度</span>
          <span class="progress-percent">{{ completionPercent }}%</span>
        </div>
        <el-progress 
          :percentage="completionPercent" 
          :color="progressColor"
          :stroke-width="12"
        />
      </div>

      <!-- 连续打卡提示 -->
      <div class="streak-tip" v-if="task.currentStreak > 0">
        🔥 已连续打卡 <strong>{{ task.currentStreak }}</strong> 天，继续加油！
      </div>
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleCheckin" 
        :loading="submitting"
        :disabled="!form.actualValue || form.actualValue <= 0"
      >
        <el-icon v-if="!submitting"><Check /></el-icon>
        确认打卡
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import planApi from '@/api/plan'

const props = defineProps({
  modelValue: Boolean,
  task: Object
})

const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const submitting = ref(false)

const form = ref({
  actualValue: 0,
  remark: ''
})

const rules = {
  actualValue: [
    { required: true, message: '请输入完成量', trigger: 'blur' }
  ]
}

// 监听弹窗打开
watch(() => props.modelValue, (val) => {
  if (val && props.task) {
    // 默认填充目标值
    form.value.actualValue = props.task.targetValue
    form.value.remark = ''
  }
})

// 计算完成百分比
const completionPercent = computed(() => {
  if (!props.task?.targetValue) return 0
  const percent = Math.round((form.value.actualValue / props.task.targetValue) * 100)
  return Math.min(percent, 100)
})

// 进度条颜色
const progressColor = computed(() => {
  const percent = completionPercent.value
  if (percent >= 100) return '#67c23a'
  if (percent >= 80) return '#409eff'
  if (percent >= 50) return '#e6a23c'
  return '#f56c6c'
})

// 快速设置百分比值
const setQuickValue = (percent) => {
  if (props.task?.targetValue) {
    form.value.actualValue = Math.round(props.task.targetValue * percent / 100)
  }
}

// 提交打卡
const handleCheckin = async () => {
  try {
    await formRef.value.validate()
    
    if (form.value.actualValue <= 0) {
      ElMessage.warning('完成量必须大于0')
      return
    }
    
    submitting.value = true
    
    const response = await planApi.checkin({
      planId: props.task.planId,
      actualValue: form.value.actualValue,
      remark: form.value.remark
    })
    
    // 显示打卡成功信息
    let message = '🎉 打卡成功！'
    if (response.currentStreak > 1) {
      message += ` 已连续打卡 ${response.currentStreak} 天`
    }
    
    ElMessage.success(message)
    emit('success')
    emit('update:modelValue', false)
    
  } catch (error) {
    console.error('打卡失败:', error)
    ElMessage.error(error.message || '打卡失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.checkin-content {
  padding: 10px 0;
}

.task-info {
  text-align: center;
  padding: 20px;
  background: #409eff;
  border-radius: 12px;
  color: white;
  margin-bottom: 24px;
  
  .task-name {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 8px;
  }
  
  .task-target {
    font-size: 14px;
    opacity: 0.9;
    
    strong {
      font-size: 20px;
    }
  }
}

.value-input {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .el-input-number {
    flex: 1;
  }
  
  .unit {
    font-size: 16px;
    color: #666;
  }
}

.quick-buttons {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  
  .el-button {
    flex: 1;
  }
}

.progress-section {
  margin: 24px 0;
  
  .progress-label {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
    color: #666;
    
    .progress-percent {
      font-weight: 600;
      color: #333;
    }
  }
}

.streak-tip {
  text-align: center;
  padding: 12px;
  background: #fff7e6;
  border-radius: 8px;
  color: #d48806;
  font-size: 14px;
  
  strong {
    color: #fa8c16;
    font-size: 18px;
    margin: 0 2px;
  }
}
</style>
