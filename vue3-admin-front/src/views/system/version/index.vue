<template>
  <div class="version-management">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="title-section">
          <h2>版本更新历史管理</h2>
          <p>管理网站版本更新历史，记录产品迭代轨迹</p>
        </div>
      </div>
    </el-card>

    <!-- 搜索和操作区 -->
    <el-card class="search-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入版本号或标题关键词" 
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select 
            v-model="searchForm.updateType" 
            placeholder="更新类型" 
            clearable
            @change="handleSearch"
          >
            <el-option label="重大更新" :value="1" />
            <el-option label="功能更新" :value="2" />
            <el-option label="修复更新" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select 
            v-model="searchForm.status" 
            placeholder="状态筛选" 
            clearable 
            @change="handleSearch"
          >
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已隐藏" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select 
            v-model="searchForm.isFeatured" 
            placeholder="推荐筛选" 
            clearable
            @change="handleSearch"
          >
            <el-option label="普通版本" :value="0" />
            <el-option label="重点推荐" :value="1" />
          </el-select>
        </el-col>
        <el-col :span="6">
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
      
      <el-row :gutter="20" style="margin-top: 15px;">
        <el-col :span="8">
          <el-date-picker
            v-model="searchForm.releaseTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="发布开始时间"
            end-placeholder="发布结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleSearch"
          />
        </el-col>
        <el-col :span="16" style="text-align: right;">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增版本
          </el-button>
          <el-button 
            type="success" 
            :disabled="multipleSelection.length === 0"
            @click="handleBatchPublish"
          >
            <el-icon><VideoPlay /></el-icon>
            批量发布
          </el-button>
          <el-button 
            type="warning" 
            :disabled="multipleSelection.length === 0"
            @click="handleBatchHide"
          >
            <el-icon><Hide /></el-icon>
            批量隐藏
          </el-button>
          <el-button 
            type="danger" 
            :disabled="multipleSelection.length === 0"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 版本历史表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="versionList" 
        style="width: 100%"
        :row-key="row => row.id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="versionNumber" label="版本号" width="120">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.versionNumber }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="更新标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-tag 
                v-if="row.isFeatured === 1" 
                type="warning" 
                size="small" 
                style="margin-right: 8px;"
              >
                推荐
              </el-tag>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updateTypeName" label="更新类型" width="120">
          <template #default="{ row }">
            <el-tag 
              :type="getUpdateTypeTag(row.updateType)" 
              size="small"
            >
              {{ row.updateTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="简要描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="prdUrl" label="PRD文档" width="120">
          <template #default="{ row }">
            <el-button 
              v-if="row.prdUrl" 
              type="primary" 
              link 
              size="small"
              @click="openPrdLink(row.prdUrl)"
            >
              <el-icon><Link /></el-icon>
              查看PRD
            </el-button>
            <span v-else class="text-gray-400">未关联</span>
          </template>
        </el-table-column>
        <el-table-column prop="releaseTime" label="发布时间" width="160" />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusTag(row.status)" 
              size="small"
            >
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="查看次数" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              link 
              size="small" 
              @click="handlePublish(row.id)"
            >
              <el-icon><VideoPlay /></el-icon>
              发布
            </el-button>
            <el-button 
              v-if="row.status === 1" 
              type="warning" 
              link 
              size="small" 
              @click="handleHide(row.id)"
            >
              <el-icon><Hide /></el-icon>
              隐藏
            </el-button>
            <el-button 
              v-if="row.status === 2" 
              type="success" 
              link 
              size="small" 
              @click="handlePublish(row.id)"
            >
              <el-icon><VideoPlay /></el-icon>
              发布
            </el-button>
            <el-button 
              v-if="row.status !== 0" 
              type="info" 
              link 
              size="small" 
              @click="handleUnpublish(row.id)"
            >
              <el-icon><Document /></el-icon>
              草稿
            </el-button>
            <el-popconfirm 
              title="确定要删除这个版本记录吗？" 
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button type="danger" link size="small">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="版本号" prop="versionNumber">
              <el-input 
                v-model="form.versionNumber" 
                placeholder="如：v1.3.0"
                @blur="checkVersionNumber"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="更新类型" prop="updateType">
              <el-select v-model="form.updateType" placeholder="请选择更新类型" style="width: 100%">
                <el-option label="重大更新" :value="1" />
                <el-option label="功能更新" :value="2" />
                <el-option label="修复更新" :value="3" />
                <el-option label="其他" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="更新标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="请输入更新标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="简要描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入版本更新的简要描述（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="PRD文档链接">
          <el-input 
            v-model="form.prdUrl" 
            placeholder="请输入PRD文档链接（选填）"
            maxlength="500"
          >
            <template #append>
              <el-button 
                v-if="form.prdUrl" 
                @click="openPrdLink(form.prdUrl)"
              >
                预览
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发布时间" prop="releaseTime">
              <el-date-picker
                v-model="form.releaseTime"
                type="datetime"
                placeholder="请选择发布时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="草稿" :value="0" />
                <el-option label="已发布" :value="1" />
                <el-option label="已隐藏" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序权重">
              <el-input-number 
                v-model="form.sortOrder" 
                :min="0" 
                :max="9999" 
                controls-position="right"
                style="width: 100%"
              />
              <div class="form-tip">数字越大越靠前显示</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重点推荐">
              <el-switch 
                v-model="form.isFeatured"
                :active-value="1"
                :inactive-value="0"
                active-text="是"
                inactive-text="否"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ editMode ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Plus, VideoPlay, Hide, Delete, Edit, Link, Document 
} from '@element-plus/icons-vue'
import { 
  getVersionList, 
  createVersion, 
  updateVersion, 
  deleteVersion,
  publishVersion,
  hideVersion,
  unpublishVersion,
  batchPublishVersions,
  batchHideVersions,
  batchDeleteVersions,
  checkVersionNumberExists
} from '@/api/version'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const versionList = ref([])
const multipleSelection = ref([])

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单 - 确保初始化
const searchForm = reactive({
  keyword: '',
  updateType: '',
  status: '',
  isFeatured: '',
  releaseTimeRange: null
})

// 弹窗相关
const dialogVisible = ref(false)
const editMode = ref(false)
const dialogTitle = ref('新增版本')
const formRef = ref()

// 表单数据
const form = reactive({
  id: null,
  versionNumber: '',
  title: '',
  updateType: null,
  description: '',
  prdUrl: '',
  releaseTime: '',
  status: 0,
  sortOrder: 0,
  isFeatured: 0
})

// 表单验证规则
const rules = {
  versionNumber: [
    { required: true, message: '请输入版本号', trigger: 'blur' },
    { max: 50, message: '版本号长度不能超过50个字符', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入更新标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
  ],
  updateType: [
    { required: true, message: '请选择更新类型', trigger: 'change', type: 'number' }
  ],
  releaseTime: [
    { required: true, message: '请选择发布时间', trigger: 'change' }
  ]
}

// 生命周期
onMounted(() => {
  loadVersionList()
})

// 加载版本列表
const loadVersionList = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    
    // 只添加有值的搜索条件
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.updateType !== '' && searchForm.updateType !== null && searchForm.updateType !== undefined) {
      params.updateType = searchForm.updateType
    }
    if (searchForm.status !== '' && searchForm.status !== null && searchForm.status !== undefined) {
      params.status = searchForm.status
    }
    if (searchForm.isFeatured !== '' && searchForm.isFeatured !== null && searchForm.isFeatured !== undefined) {
      params.isFeatured = searchForm.isFeatured
    }
    
    // 处理时间范围
    if (searchForm.releaseTimeRange && searchForm.releaseTimeRange.length === 2) {
      params.releaseTimeStart = searchForm.releaseTimeRange[0]
      params.releaseTimeEnd = searchForm.releaseTimeRange[1]
    }

    const data = await getVersionList(params)
    versionList.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    console.error('加载版本列表失败:', error)
    ElMessage.error('加载版本列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadVersionList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    updateType: '',
    status: '',
    isFeatured: '',
    releaseTimeRange: null
  })
  handleSearch()
}

// 表格多选
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadVersionList()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  loadVersionList()
}

// 新增版本
const handleCreate = () => {
  editMode.value = false
  dialogTitle.value = '新增版本'
  resetForm()
  dialogVisible.value = true
}

// 编辑版本
const handleEdit = (row) => {
  editMode.value = true
  dialogTitle.value = '编辑版本'
  Object.assign(form, {
    ...row,
    updateType: row.updateType ? Number(row.updateType) : null,
    status: Number(row.status) || 0,
    isFeatured: Number(row.isFeatured) || 0,
    sortOrder: Number(row.sortOrder) || 0
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    versionNumber: '',
    title: '',
    updateType: null,
    description: '',
    prdUrl: '',
    releaseTime: '',
    status: 0,
    sortOrder: 0,
    isFeatured: 0
  })
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 检查版本号是否存在
const checkVersionNumber = async () => {
  if (!form.versionNumber) return
  
  try {
    const exists = await checkVersionNumberExists(form.versionNumber, form.id)
    if (exists) {
      ElMessage.warning('该版本号已存在，请使用其他版本号')
      return false
    }
    return true
  } catch (error) {
    console.error('检查版本号失败:', error)
    return false
  }
}

// 提交表单
const handleSubmit = () => {
  if (!formRef.value) return
  
  formRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 检查版本号唯一性
    const versionValid = await checkVersionNumber()
    if (!versionValid) return
    
    try {
      submitLoading.value = true
      
      if (editMode.value) {
        await updateVersion(form)
        ElMessage.success('更新版本成功')
      } else {
        await createVersion(form)
        ElMessage.success('创建版本成功')
      }
      
      dialogVisible.value = false
      loadVersionList()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(editMode.value ? '更新版本失败' : '创建版本失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 关闭弹窗
const handleDialogClose = () => {
  dialogVisible.value = false
  resetForm()
}

// 发布版本
const handlePublish = async (id) => {
  try {
    await publishVersion(id)
    ElMessage.success('发布成功')
    loadVersionList()
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败')
  }
}

// 隐藏版本
const handleHide = async (id) => {
  try {
    await hideVersion(id)
    ElMessage.success('隐藏成功')
    loadVersionList()
  } catch (error) {
    console.error('隐藏失败:', error)
    ElMessage.error('隐藏失败')
  }
}

// 取消发布
const handleUnpublish = async (id) => {
  try {
    await unpublishVersion(id)
    ElMessage.success('已设置为草稿')
    loadVersionList()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除版本
const handleDelete = async (id) => {
  try {
    await deleteVersion(id)
    ElMessage.success('删除成功')
    loadVersionList()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 批量发布
const handleBatchPublish = async () => {
  if (multipleSelection.value.length === 0) return
  
  ElMessageBox.confirm(
    `确定要发布选中的 ${multipleSelection.value.length} 个版本吗？`,
    '批量发布',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = multipleSelection.value.map(item => item.id)
      await batchPublishVersions(ids)
      ElMessage.success('批量发布成功')
      loadVersionList()
    } catch (error) {
      console.error('批量发布失败:', error)
      ElMessage.error('批量发布失败')
    }
  })
}

// 批量隐藏
const handleBatchHide = async () => {
  if (multipleSelection.value.length === 0) return
  
  ElMessageBox.confirm(
    `确定要隐藏选中的 ${multipleSelection.value.length} 个版本吗？`,
    '批量隐藏',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = multipleSelection.value.map(item => item.id)
      await batchHideVersions(ids)
      ElMessage.success('批量隐藏成功')
      loadVersionList()
    } catch (error) {
      console.error('批量隐藏失败:', error)
      ElMessage.error('批量隐藏失败')
    }
  })
}

// 批量删除
const handleBatchDelete = async () => {
  if (multipleSelection.value.length === 0) return
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${multipleSelection.value.length} 个版本吗？此操作不可恢复！`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(async () => {
    try {
      const ids = multipleSelection.value.map(item => item.id)
      await batchDeleteVersions(ids)
      ElMessage.success('批量删除成功')
      loadVersionList()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  })
}

// 打开PRD链接
const openPrdLink = (url) => {
  if (url) {
    window.open(url, '_blank')
  }
}

// 获取更新类型标签样式
const getUpdateTypeTag = (type) => {
  const tagMap = {
    1: 'danger',   // 重大更新
    2: 'primary',  // 功能更新
    3: 'warning',  // 修复更新
    4: 'info'      // 其他
  }
  return tagMap[type] || 'info'
}

// 获取状态标签样式
const getStatusTag = (status) => {
  const tagMap = {
    0: 'info',     // 草稿
    1: 'success',  // 已发布
    2: 'warning'   // 已隐藏
  }
  return tagMap[status] || 'info'
}
</script>

<style scoped>
.version-management {
  padding: 20px;
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
  font-size: 24px;
  font-weight: 600;
}

.title-section p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.text-gray-400 {
  color: #9ca3af;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.dialog-footer {
  text-align: right;
}
</style> 