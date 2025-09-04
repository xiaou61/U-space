<template>
  <div class="category-management">
    <el-card shadow="never">
      <!-- 页面头部 -->
      <template #header>
        <div class="card-header">
          <h3>分类管理</h3>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            创建分类
          </el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input 
              v-model="searchForm.keyword" 
              placeholder="搜索分类名称或描述..."
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select 
              v-model="searchForm.status" 
              placeholder="状态筛选"
              clearable
              style="width: 100%"
            >
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-col>
        </el-row>
      </div>
      
      <!-- 数据表格 -->
      <el-table 
        :data="categoryList" 
        :loading="loading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="postCount" label="帖子数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="creatorName" label="创建者" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editCategory(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger"
              @click="deleteCategory(row)"
              :disabled="row.postCount > 0"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchCategoryList"
          @current-change="fetchCategoryList"
        />
      </div>
    </el-card>
    
    <!-- 创建/编辑分类对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="editingCategory ? '编辑分类' : '创建分类'"
      width="500px"
      @close="resetForm"
    >
      <el-form 
        ref="formRef"
        :model="categoryForm" 
        :rules="formRules" 
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input 
            v-model="categoryForm.name"
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input 
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number 
            v-model="categoryForm.sortOrder"
            :min="0"
            :max="999"
            placeholder="数字越小越靠前"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="editingCategory" label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ editingCategory ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Refresh, Edit, Delete 
} from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const editingCategory = ref(null)
const categoryList = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: null
})

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 分类表单
const categoryForm = reactive({
  name: '',
  description: '',
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 50, message: '分类名称长度不能超过50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '分类描述长度不能超过200个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序顺序', trigger: 'blur' },
    { type: 'number', min: 0, max: 999, message: '排序顺序范围为0-999', trigger: 'blur' }
  ]
}

const formRef = ref(null)

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    
    const response = await communityApi.getCategoryList(params)
    
    categoryList.value = response.records || []
    pagination.total = response.total || 0
    
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchCategoryList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.pageNum = 1
  fetchCategoryList()
}

// 编辑分类
const editCategory = (row) => {
  editingCategory.value = row
  categoryForm.name = row.name
  categoryForm.description = row.description
  categoryForm.sortOrder = row.sortOrder
  categoryForm.status = row.status
  showCreateDialog.value = true
}

// 切换状态
const toggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '禁用' : '启用'}分类"${row.name}"吗？`,
      '状态切换确认',
      { type: 'warning' }
    )
    
    await communityApi.toggleCategoryStatus(row.id)
    ElMessage.success(`${row.status === 1 ? '禁用' : '启用'}成功`)
    fetchCategoryList()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态切换失败:', error)
      ElMessage.error('状态切换失败')
    }
  }
}

// 删除分类
const deleteCategory = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${row.name}"吗？该操作不可撤销！`,
      '删除确认',
      { type: 'warning' }
    )
    
    await communityApi.deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchCategoryList()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除分类失败')
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (editingCategory.value) {
      // 编辑
      await communityApi.updateCategory(editingCategory.value.id, categoryForm)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await communityApi.createCategory(categoryForm)
      ElMessage.success('创建成功')
    }
    
    showCreateDialog.value = false
    fetchCategoryList()
    
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  editingCategory.value = null
  categoryForm.name = ''
  categoryForm.description = ''
  categoryForm.sortOrder = 0
  categoryForm.status = 1
  
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 组件挂载
onMounted(() => {
  fetchCategoryList()
})
</script>

<style scoped>
.category-management {
  margin: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #303133;
}

.search-section {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: center;
}
</style> 