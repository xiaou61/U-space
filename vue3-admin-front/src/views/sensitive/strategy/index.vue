<template>
  <div class="sensitive-strategy-page">
    <!-- 操作栏 -->
    <div class="toolbar">
      <el-card shadow="never">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button type="warning" @click="handleRefreshCache">
              <el-icon><Refresh /></el-icon>
              刷新缓存
            </el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 策略配置表格 - 按模块分组 -->
    <el-card v-loading="loading" shadow="never">
      <div v-for="module in modules" :key="module.value" class="module-section">
        <h3>{{ module.label }}</h3>
        <el-table :data="getModuleStrategies(module.value)" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="风险等级" width="120">
            <template #default="{ row }">
              <el-tag
                :type="getLevelTagType(row.level)"
                size="small"
              >
                {{ getLevelText(row.level) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理动作" width="120">
            <template #default="{ row }">
              <el-tag
                :type="getActionTagType(row.action)"
                size="small"
              >
                {{ row.action }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="通知管理员" width="120">
            <template #default="{ row }">
              <el-tag :type="row.notifyAdmin ? 'success' : 'info'" size="small">
                {{ row.notifyAdmin ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="限制用户" width="120">
            <template #default="{ row }">
              <el-tag :type="row.limitUser ? 'danger' : 'info'" size="small">
                {{ row.limitUser ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="limitDuration" label="限制时长(分钟)" width="140" />
          <el-table-column prop="description" label="策略描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag
                :type="row.status === 1 ? 'success' : 'danger'"
                size="small"
              >
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                type="warning"
                size="small"
                @click="handleReset(row)"
              >
                重置
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑策略"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="策略名称">
          <el-input v-model="form.strategyName" disabled />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="form.module" disabled />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-tag :type="getLevelTagType(form.level)" size="small">
            {{ getLevelText(form.level) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="处理动作" prop="action">
          <el-select v-model="form.action" placeholder="请选择处理动作" style="width: 100%">
            <el-option label="替换" value="replace" />
            <el-option label="拒绝" value="reject" />
            <el-option label="警告" value="warn" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知管理员" prop="notifyAdmin">
          <el-radio-group v-model="form.notifyAdmin">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="限制用户" prop="limitUser">
          <el-radio-group v-model="form.limitUser">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="form.limitUser === 1"
          label="限制时长(分钟)"
          prop="limitDuration"
        >
          <el-input-number
            v-model="form.limitDuration"
            :min="1"
            :max="43200"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="策略描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  listStrategy,
  updateStrategy,
  resetStrategy,
  refreshStrategyCache
} from '@/api/sensitive'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const formRef = ref()

// 模块列表
const modules = [
  { label: '社区模块 (community)', value: 'community' },
  { label: '面试模块 (interview)', value: 'interview' },
  { label: '朋友圈模块 (moment)', value: 'moment' },
  { label: '博客模块 (blog)', value: 'blog' }
]

// 编辑表单
const form = reactive({
  id: null,
  strategyName: '',
  module: '',
  level: null,
  action: '',
  notifyAdmin: 0,
  limitUser: 0,
  limitDuration: null,
  description: '',
  status: 1
})

// 表单验证规则
const formRules = {
  action: [
    { required: true, message: '请选择处理动作', trigger: 'change' }
  ],
  notifyAdmin: [
    { required: true, message: '请选择是否通知管理员', trigger: 'change' }
  ],
  limitUser: [
    { required: true, message: '请选择是否限制用户', trigger: 'change' }
  ],
  limitDuration: [
    { required: true, message: '请输入限制时长', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 方法
const handleQuery = async () => {
  loading.value = true
  try {
    const response = await listStrategy({
      pageNum: 1,
      pageSize: 100 // 获取所有策略
    })
    tableData.value = response.records
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const getModuleStrategies = (module) => {
  return tableData.value.filter(item => item.module === module)
}

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  try {
    await updateStrategy(form)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    handleQuery()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleReset = async (row) => {
  try {
    await ElMessageBox.confirm('确定要重置这个策略为默认值吗？', '提示', {
      type: 'warning'
    })
    await resetStrategy(row.id)
    ElMessage.success('重置成功')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

const handleRefreshCache = async () => {
  try {
    await refreshStrategyCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    ElMessage.error('缓存刷新失败')
  }
}

const resetForm = () => {
  form.id = null
  form.strategyName = ''
  form.module = ''
  form.level = null
  form.action = ''
  form.notifyAdmin = 0
  form.limitUser = 0
  form.limitDuration = null
  form.description = ''
  form.status = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 工具方法
const getLevelText = (level) => {
  const levelMap = { 1: '低风险', 2: '中风险', 3: '高风险' }
  return levelMap[level] || '未知'
}

const getLevelTagType = (level) => {
  const typeMap = { 1: 'success', 2: 'warning', 3: 'danger' }
  return typeMap[level] || 'info'
}

const getActionTagType = (action) => {
  const typeMap = { 
    replace: 'info', 
    reject: 'danger', 
    warn: 'warning' 
  }
  return typeMap[action] || 'info'
}

// 生命周期
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.sensitive-strategy-page {
  padding: 20px;
}

.toolbar {
  margin-bottom: 16px;
}

.module-section {
  margin-bottom: 30px;
}

.module-section h3 {
  margin-bottom: 16px;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}
</style>
