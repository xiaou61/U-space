<template>
  <div class="sensitive-whitelist-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-card shadow="never">
        <el-form :model="queryForm" inline>
          <el-form-item label="白名单词汇">
            <el-input
              v-model="queryForm.word"
              placeholder="请输入词汇"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="分类">
            <el-input
              v-model="queryForm.category"
              placeholder="请输入分类"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="作用范围">
            <el-select
              v-model="queryForm.scope"
              placeholder="请选择范围"
              clearable
              style="width: 130px"
            >
              <el-option label="全局" value="global" />
              <el-option label="模块级" value="module" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="queryForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
            >
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetQuery">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <el-card shadow="never">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" @click="handleRefreshCache">
              <el-icon><Refresh /></el-icon>
              刷新缓存
            </el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="白名单词汇" min-width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="作用范围" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.scope === 'global' ? 'success' : 'info'"
              size="small"
            >
              {{ row.scope === 'global' ? '全局' : '模块级' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="moduleName" label="模块名称" width="120" />
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
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="词汇" prop="word">
          <el-input
            v-model="form.word"
            placeholder="请输入白名单词汇"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input
            v-model="form.category"
            placeholder="请输入分类，如：专业术语、成语、人名等"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="作用范围" prop="scope">
          <el-radio-group v-model="form.scope">
            <el-radio value="global">全局</el-radio>
            <el-radio value="module">模块级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="form.scope === 'module'"
          label="模块名称"
          prop="moduleName"
        >
          <el-select
            v-model="form.moduleName"
            placeholder="请选择模块"
            style="width: 100%"
          >
            <el-option label="社区模块" value="community" />
            <el-option label="面试模块" value="interview" />
            <el-option label="朋友圈模块" value="moment" />
            <el-option label="博客模块" value="blog" />
          </el-select>
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
import {
  Search,
  Refresh,
  Plus,
  Delete
} from '@element-plus/icons-vue'
import {
  listWhitelist,
  addWhitelist,
  updateWhitelist,
  deleteWhitelist,
  deleteWhitelistBatch,
  refreshWhitelistCache
} from '@/api/sensitive'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const tableData = ref([])
const selectedRows = ref([])
const total = ref(0)
const formRef = ref()

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  word: '',
  category: '',
  scope: null,
  moduleName: '',
  status: null
})

// 编辑表单
const form = reactive({
  id: null,
  word: '',
  category: '',
  scope: 'global',
  moduleName: '',
  status: 1
})

// 表单验证规则
const formRules = {
  word: [
    { required: true, message: '请输入白名单词汇', trigger: 'blur' },
    { min: 1, max: 255, message: '长度在 1 到 255 个字符', trigger: 'blur' }
  ],
  scope: [
    { required: true, message: '请选择作用范围', trigger: 'change' }
  ],
  moduleName: [
    { required: true, message: '请选择模块名称', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 方法
const handleQuery = async () => {
  loading.value = true
  try {
    const response = await listWhitelist(queryForm)
    tableData.value = response.records
    total.value = response.total
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  queryForm.word = ''
  queryForm.category = ''
  queryForm.scope = null
  queryForm.moduleName = ''
  queryForm.status = null
  handleQuery()
}

const handleAdd = () => {
  dialogTitle.value = '新增白名单'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑白名单'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  try {
    if (form.id) {
      await updateWhitelist(form)
      ElMessage.success('更新成功')
    } else {
      await addWhitelist(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleQuery()
  } catch (error) {
    ElMessage.error(form.id ? '更新失败' : '新增失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个白名单词汇吗？', '提示', {
      type: 'warning'
    })
    await deleteWhitelist(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除选中的白名单词汇吗？', '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await deleteWhitelistBatch(ids)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleRefreshCache = async () => {
  try {
    await refreshWhitelistCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    ElMessage.error('缓存刷新失败')
  }
}

const resetForm = () => {
  form.id = null
  form.word = ''
  form.category = ''
  form.scope = 'global'
  form.moduleName = ''
  form.status = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 生命周期
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.sensitive-whitelist-page {
  padding: 20px;
}

.search-bar {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>
