<template>
  <div class="category-management">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="title-section">
          <h2>分类管理</h2>
          <p>管理面试题分类，支持增删改查和状态控制</p>
        </div>
        <div class="action-section">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增分类
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 搜索和操作区 -->
    <el-card class="search-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入分类名称" 
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="10">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 分类表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="categoryList" 
        style="width: 100%"
        :row-key="row => row.id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="description" label="分类描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column prop="questionSetCount" label="题单数量" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.questionSetCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              :active-value="1" 
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :disabled="row.questionSetCount > 0"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="600px" 
      @close="resetForm"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入分类描述" 
            maxlength="500" 
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序序号" prop="sortOrder">
          <el-input-number 
            v-model="form.sortOrder" 
            :min="0" 
            :max="9999" 
            placeholder="请输入排序序号" 
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { interviewApi } from '@/api/interview'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const categoryList = ref([])
const formRef = ref(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: null
})

// 编辑表单
const form = reactive({
  id: null,
  name: '',
  description: '',
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 100, message: '分类名称长度不能超过100字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '分类描述长度不能超过500字符', trigger: 'blur' }
  ],
  sortOrder: [
    { type: 'number', message: '排序序号必须为数字', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 计算对话框标题
const dialogTitle = computed(() => {
  return form.id ? '编辑分类' : '新增分类'
})

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true
  try {
    const data = await interviewApi.getAllCategories()
    
    // 根据搜索条件过滤
    let filteredData = data || []
    if (searchForm.name) {
      filteredData = filteredData.filter(item => 
        item.name.includes(searchForm.name)
      )
    }
    if (searchForm.status !== null) {
      filteredData = filteredData.filter(item => 
        item.status === searchForm.status
      )
    }
    
    // 按排序序号排序
    categoryList.value = filteredData.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  fetchCategories()
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.status = null
  fetchCategories()
}

// 新增分类
const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  form.id = row.id
  form.name = row.name
  form.description = row.description || ''
  form.sortOrder = row.sortOrder || 0
  form.status = row.status
  dialogVisible.value = true
}

// 删除分类
const handleDelete = async (row) => {
  if (row.questionSetCount > 0) {
    ElMessage.warning('该分类下还有题单，不能删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分类 "${row.name}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await interviewApi.deleteCategory(row.id)
    ElMessage.success('删除成功')
    await fetchCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 状态切换
const handleStatusChange = async (row) => {
  try {
    await interviewApi.updateCategory(row.id, {
      name: row.name,
      description: row.description,
      sortOrder: row.sortOrder,
      status: row.status
    })
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('状态更新失败')
    // 回滚状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 提交表单
const handleSubmit = async () => {
  const formElement = formRef.value
  if (!formElement) return

  try {
    await formElement.validate()
    submitLoading.value = true

    const data = {
      name: form.name,
      description: form.description,
      sortOrder: form.sortOrder,
      status: form.status
    }

    if (form.id) {
      // 编辑
      await interviewApi.updateCategory(form.id, data)
      ElMessage.success('编辑成功')
    } else {
      // 新增
      await interviewApi.createCategory(data)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    await fetchCategories()
  } catch (error) {
    if (error?.name !== 'ValidationError') {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.id = null
  form.name = ''
  form.description = ''
  form.sortOrder = 0
  form.status = 1
}

// 页面挂载
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
}

.title-section p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style> 