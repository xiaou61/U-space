<template>
  <div class="questionset-management">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="title-section">
          <h2>题单管理</h2>
          <p>管理官方面试题单，支持增删改查和分类管理</p>
        </div>
        <div class="action-section">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增题单
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 搜索和过滤区 -->
    <el-card class="search-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input 
            v-model="queryForm.title" 
            placeholder="请输入题单标题" 
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="5">
          <el-select v-model="queryForm.categoryId" placeholder="请选择分类" clearable @change="handleSearch">
            <el-option 
              v-for="category in categoryList" 
              :key="category.id" 
              :label="category.name" 
              :value="category.id" 
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="草稿" :value="0" />
            <el-option label="发布" :value="1" />
            <el-option label="下线" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="queryForm.type" placeholder="请选择类型" clearable @change="handleSearch">
            <el-option label="官方" :value="1" />
            <el-option label="用户创建" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="5">
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

    <!-- 题单表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="questionSetList" 
        style="width: 100%"
        :row-key="row => row.id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="题单标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'info'">
              {{ row.type === 1 ? '官方' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.questionCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
        <el-table-column prop="favoriteCount" label="收藏量" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 1 ? 'success' : (row.status === 0 ? 'warning' : 'danger')"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建者" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="handleViewQuestions(row)">
              <el-icon><View /></el-icon>
              题目
            </el-button>
            <el-button type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination 
          v-model:current-page="queryForm.pageNum" 
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="700px" 
      @close="resetForm"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="题单标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入题单标题" maxlength="200" />
        </el-form-item>
        <el-form-item label="题单描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入题单描述" 
            maxlength="2000" 
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option 
              v-for="category in categoryList.filter(c => c.status === 1)" 
              :key="category.id" 
              :label="category.name" 
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">官方题单</el-radio>
            <el-radio :label="2">用户题单</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type === 2" label="可见性" prop="visibility">
          <el-radio-group v-model="form.visibility">
            <el-radio :label="1">公开</el-radio>
            <el-radio :label="2">私有</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">发布</el-radio>
            <el-radio :label="2">下线</el-radio>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Refresh, Edit, Delete, View
} from '@element-plus/icons-vue'
import { interviewApi } from '@/api/interview'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const questionSetList = ref([])
const categoryList = ref([])
const total = ref(0)
const formRef = ref(null)

// 查询表单
const queryForm = reactive({
  title: '',
  categoryId: null,
  type: null,
  status: null,
  pageNum: 1,
  pageSize: 10
})

// 编辑表单
const form = reactive({
  id: null,
  title: '',
  description: '',
  categoryId: null,
  type: 1,
  visibility: 1,
  status: 1
})



// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入题单标题', trigger: 'blur' },
    { max: 200, message: '题单标题长度不能超过200字符', trigger: 'blur' }
  ],
  description: [
    { max: 2000, message: '题单描述长度不能超过2000字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}



// 计算对话框标题
const dialogTitle = computed(() => {
  return form.id ? '编辑题单' : '新增题单'
})

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '草稿',
    1: '发布',
    2: '下线'
  }
  return statusMap[status] || '未知'
}

// 获取题单列表
const fetchQuestionSets = async () => {
  loading.value = true
  try {
    const data = await interviewApi.getQuestionSets(queryForm)
    questionSetList.value = data?.records || []
    total.value = data?.total || 0
  } catch (error) {
    console.error('获取题单列表失败:', error)
    ElMessage.error('获取题单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const data = await interviewApi.getAllCategories()
    categoryList.value = data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 搜索
const handleSearch = () => {
  queryForm.pageNum = 1
  fetchQuestionSets()
}

// 重置搜索
const handleReset = () => {
  queryForm.title = ''
  queryForm.categoryId = null
  queryForm.type = null
  queryForm.status = null
  queryForm.pageNum = 1
  fetchQuestionSets()
}

// 分页大小改变
const handleSizeChange = (size) => {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  fetchQuestionSets()
}

// 当前页改变
const handleCurrentChange = (page) => {
  queryForm.pageNum = page
  fetchQuestionSets()
}

// 新增题单
const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑题单
const handleEdit = (row) => {
  form.id = row.id
  form.title = row.title
  form.description = row.description || ''
  form.categoryId = row.categoryId
  form.type = row.type
  form.visibility = row.visibility || 1
  form.status = row.status
  dialogVisible.value = true
}

// 删除题单
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除题单 "${row.title}" 吗？删除后将无法恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await interviewApi.deleteQuestionSet(row.id)
    ElMessage.success('删除成功')
    await fetchQuestionSets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除题单失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 查看题目
const handleViewQuestions = (row) => {
  router.push(`/interview/questions?questionSetId=${row.id}&title=${encodeURIComponent(row.title)}`)
}



// 提交表单
const handleSubmit = async () => {
  const formElement = formRef.value
  if (!formElement) return

  try {
    await formElement.validate()
    submitLoading.value = true

    const data = {
      title: form.title,
      description: form.description,
      categoryId: form.categoryId,
      type: form.type,
      visibility: form.type === 2 ? form.visibility : 1,
      status: form.status
    }

    if (form.id) {
      // 编辑
      await interviewApi.updateQuestionSet(form.id, data)
      ElMessage.success('编辑成功')
    } else {
      // 新增
      await interviewApi.createQuestionSet(data)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    await fetchQuestionSets()
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
  form.title = ''
  form.description = ''
  form.categoryId = null
  form.type = 1
  form.visibility = 1
  form.status = 1
}



// 页面挂载
onMounted(() => {
  fetchCategories()
  fetchQuestionSets()
})
</script>

<style scoped>
.questionset-management {
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

.action-section {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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
  
  .action-section {
    align-self: stretch;
    justify-content: flex-end;
  }
}
</style> 