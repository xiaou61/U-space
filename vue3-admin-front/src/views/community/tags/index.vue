<template>
  <div class="tags-page">
    <div class="header">
      <h2>标签管理</h2>
      <el-button type="primary" @click="showAddDialog" :icon="Plus">
        新建标签
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="标签名称">
          <el-input v-model="searchForm.keyword" placeholder="请输入标签名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 标签列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tagList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" min-width="150">
          <template #default="{ row }">
            <el-tag type="success" effect="plain"># {{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="标签描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="useCount" label="使用次数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.useCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)" :icon="Edit">
              编辑
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'" 
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)" 
              :icon="Delete"
              :disabled="row.useCount > 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageParams.pageNum"
          v-model:page-size="pageParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="标签描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            placeholder="请输入标签描述" 
            :rows="3"
            maxlength="200" 
            show-word-limit 
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const tagList = ref([])
const total = ref(0)
const formRef = ref(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: null
})

// 分页参数
const pageParams = reactive({
  pageNum: 1,
  pageSize: 10
})

// 表单数据
const form = reactive({
  id: null,
  name: '',
  description: '',
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const rules = reactive({
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 2, max: 20, message: '标签名称长度为2-20个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '标签描述最多200个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
})

// 获取标签列表
const fetchTagList = async () => {
  loading.value = true
  try {
    const params = {
      ...pageParams,
      ...searchForm
    }
    const response = await communityApi.getTagList(params)
    tagList.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error('获取标签列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageParams.pageNum = 1
  fetchTagList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  pageParams.pageNum = 1
  fetchTagList()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pageParams.pageSize = size
  pageParams.pageNum = 1
  fetchTagList()
}

// 当前页改变
const handleCurrentChange = (page) => {
  pageParams.pageNum = page
  fetchTagList()
}

// 显示新增对话框
const showAddDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新建标签'
  resetForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑标签'
  form.id = row.id
  form.name = row.name
  form.description = row.description || ''
  form.sortOrder = row.sortOrder
  form.status = row.status
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.name = ''
  form.description = ''
  form.sortOrder = 0
  form.status = 1
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await communityApi.updateTag(form.id, form)
      ElMessage.success('更新标签成功')
    } else {
      await communityApi.createTag(form)
      ElMessage.success('创建标签成功')
    }
    
    dialogVisible.value = false
    fetchTagList()
  } catch (error) {
    if (error !== false) { // 不是表单验证失败
      ElMessage.error(isEdit.value ? '更新标签失败' : '创建标签失败')
    }
  } finally {
    submitting.value = false
  }
}

// 切换状态
const toggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认${row.status === 1 ? '禁用' : '启用'}标签「${row.name}」吗？`,
      '确认操作',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await communityApi.toggleTagStatus(row.id)
    ElMessage.success('操作成功')
    fetchTagList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除标签
const handleDelete = async (row) => {
  if (row.useCount > 0) {
    ElMessage.warning('该标签已被使用，无法删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认删除标签「${row.name}」吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await communityApi.deleteTag(row.id)
    ElMessage.success('删除成功')
    
    // 如果删除的是当前页最后一条数据，且不是第一页，则回到上一页
    if (tagList.value.length === 1 && pageParams.pageNum > 1) {
      pageParams.pageNum--
    }
    
    fetchTagList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchTagList()
})
</script>

<style scoped>
.tags-page {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

