<template>
  <div class="grant-points-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      label-position="right"
    >
      <el-form-item label="积分类型" prop="pointsTypeId">
        <el-select
          v-model="form.pointsTypeId"
          placeholder="请选择积分类型"
          style="width: 100%"
          :loading="pointsTypesLoading"
        >
          <el-option
            v-for="item in pointsTypes"
            :key="item.id"
            :label="item.typeName"
            :value="item.id"
          >
            <div>
              <span style="font-weight: bold;">{{ item.typeName }}</span>
              <div style="font-size: 12px; color: #999;">{{ item.description }}</div>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="积分数量" prop="pointsAmount">
        <el-input-number
          v-model="form.pointsAmount"
          :min="1"
          :max="10000"
          placeholder="请输入积分数量"
          style="width: 100%"
        />
        <div style="font-size: 12px; color: #999; margin-top: 4px;">
          将为每个用户发放相同数量的积分
        </div>
      </el-form-item>

      <el-form-item label="用户列表" prop="userIds">
        <div class="user-input-section">
          <!-- 单个用户添加 -->
          <div class="single-user-input">
            <el-input
              v-model="singleUserId"
              placeholder="输入用户ID"
              style="width: 200px;"
              @keyup.enter="addSingleUser"
            />
            <el-button @click="addSingleUser" :disabled="!singleUserId">添加用户</el-button>
          </div>

          <!-- 批量用户添加 -->
          <div class="batch-user-input">
            <el-input
              v-model="batchUserIds"
              type="textarea"
              placeholder="批量输入用户ID，每行一个或用逗号分隔"
              :rows="4"
              style="width: 100%;"
            />
            <el-button @click="addBatchUsers" style="margin-top: 8px;" :disabled="!batchUserIds">批量添加</el-button>
          </div>

          <!-- 已选用户列表 -->
          <div class="selected-users" v-if="form.userIds.length > 0">
            <div class="users-header">
              <span>已选择用户 ({{ form.userIds.length }} 人)</span>
              <el-button size="small" @click="clearAllUsers">清空所有</el-button>
            </div>
            <div class="users-list">
              <el-tag
                v-for="userId in form.userIds"
                :key="userId"
                closable
                @close="removeUser(userId)"
                style="margin: 4px;"
              >
                {{ userId }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="备注信息" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          placeholder="请输入发放备注"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 发放预览 -->
      <el-form-item label="发放预览" v-if="form.userIds.length > 0 && form.pointsAmount > 0">
        <el-alert
          :title="`将为 ${form.userIds.length} 个用户各发放 ${form.pointsAmount} 积分，总计 ${form.userIds.length * form.pointsAmount} 积分`"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleSubmit" 
        :loading="submitLoading"
        :disabled="form.userIds.length === 0"
      >
        确认发放积分
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPointsTypes } from '../api/points'

// Emits
const emit = defineEmits(['submit', 'cancel'])

// 响应式数据
const formRef = ref()
const submitLoading = ref(false)
const pointsTypesLoading = ref(false)
const pointsTypes = ref([])
const singleUserId = ref('')
const batchUserIds = ref('')

// 表单数据
const form = reactive({
  pointsTypeId: null,
  pointsAmount: 1,
  userIds: [],
  remark: ''
})

// 表单验证规则
const rules = {
  pointsTypeId: [
    { required: true, message: '请选择积分类型', trigger: 'change' }
  ],
  pointsAmount: [
    { required: true, message: '请输入积分数量', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '积分数量范围 1-10000', trigger: 'blur' }
  ],
  userIds: [
    { required: true, message: '请添加至少一个用户', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请添加至少一个用户'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  remark: [
    { max: 200, message: '备注信息不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 获取积分类型列表
const fetchPointsTypes = async () => {
  pointsTypesLoading.value = true
  try {
    const res = await getPointsTypes()
    pointsTypes.value = res.data?.filter(item => item.isActive === 1) || []
  } catch (error) {
    console.error('获取积分类型失败:', error)
    ElMessage.error('获取积分类型失败')
  } finally {
    pointsTypesLoading.value = false
  }
}

onMounted(fetchPointsTypes)

// 添加单个用户
const addSingleUser = () => {
  const userId = singleUserId.value.trim()
  if (!userId) {
    ElMessage.warning('请输入用户ID')
    return
  }
  
  if (form.userIds.includes(userId)) {
    ElMessage.warning('该用户已添加')
    return
  }
  
  form.userIds.push(userId)
  singleUserId.value = ''
  ElMessage.success('用户添加成功')
}

// 批量添加用户
const addBatchUsers = () => {
  const userIdsText = batchUserIds.value.trim()
  if (!userIdsText) {
    ElMessage.warning('请输入用户ID')
    return
  }
  
  // 支持换行符和逗号分隔
  const userIds = userIdsText
    .split(/[\n,，]/)
    .map(id => id.trim())
    .filter(id => id.length > 0)
  
  if (userIds.length === 0) {
    ElMessage.warning('未找到有效的用户ID')
    return
  }
  
  let addedCount = 0
  let duplicateCount = 0
  
  userIds.forEach(userId => {
    if (!form.userIds.includes(userId)) {
      form.userIds.push(userId)
      addedCount++
    } else {
      duplicateCount++
    }
  })
  
  batchUserIds.value = ''
  
  let message = `成功添加 ${addedCount} 个用户`
  if (duplicateCount > 0) {
    message += `，跳过 ${duplicateCount} 个重复用户`
  }
  ElMessage.success(message)
}

// 移除用户
const removeUser = (userId) => {
  const index = form.userIds.indexOf(userId)
  if (index > -1) {
    form.userIds.splice(index, 1)
  }
}

// 清空所有用户
const clearAllUsers = () => {
  form.userIds = []
  ElMessage.success('已清空所有用户')
}

// 表单提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (form.userIds.length === 0) {
      ElMessage.warning('请添加至少一个用户')
      return
    }
    
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
.grant-points-form {
  padding: 20px 0;
}

.user-input-section {
  width: 100%;
}

.single-user-input {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.batch-user-input {
  margin-bottom: 16px;
}

.selected-users {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  background-color: #fafafa;
}

.users-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 500;
  color: #606266;
}

.users-list {
  max-height: 150px;
  overflow-y: auto;
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
</style> 