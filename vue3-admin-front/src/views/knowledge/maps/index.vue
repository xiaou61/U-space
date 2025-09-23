<template>
  <div class="knowledge-map-container">
    <div class="page-header">
      <h2>知识图谱管理</h2>
      <p>管理和创建知识图谱，用于组织面试相关知识点</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" :icon="Plus" @click="handleCreate">
          新建图谱
        </el-button>
        <el-button 
          type="danger" 
          :icon="Delete" 
          :disabled="multipleSelection.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </div>
      
      <div class="toolbar-right">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索图谱标题或描述"
          :prefix-icon="Search"
          style="width: 250px; margin-right: 16px;"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="searchForm.status"
          placeholder="状态筛选"
          style="width: 120px; margin-right: 16px;"
          @change="handleSearch"
        >
          <el-option label="全部" :value="null" />
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已隐藏" :value="2" />
        </el-select>
        <el-button :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table 
      v-loading="loading"
      :data="mapList"
      @selection-change="handleSelectionChange"
      style="margin-top: 16px;"
    >
      <el-table-column type="selection" width="55" />
      
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <div class="cover-container">
            <img 
              v-if="row.coverImage" 
              :src="row.coverImage" 
              class="cover-image"
              alt="封面"
            />
            <div v-else class="cover-placeholder">
              <el-icon><Picture /></el-icon>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="标题" min-width="200">
        <template #default="{ row }">
          <div class="title-container">
            <h4 class="map-title">{{ row.title }}</h4>
            <p class="map-description">{{ row.description || '暂无描述' }}</p>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="统计信息" width="120">
        <template #default="{ row }">
          <div class="stats-container">
            <div class="stat-item">
              <el-icon><Connection /></el-icon>
              <span>{{ row.nodeCount || 0 }} 个节点</span>
            </div>
            <div class="stat-item">
              <el-icon><View /></el-icon>
              <span>{{ row.viewCount || 0 }} 次查看</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag 
            :type="getStatusTagType(row.status)"
            size="small"
          >
            {{ row.statusDesc }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button 
              size="small" 
              :icon="Edit" 
              @click="handleEditStructure(row)"
              title="编辑结构"
            >
              编辑结构
            </el-button>
            <el-button 
              size="small" 
              :icon="Setting" 
              @click="handleEditInfo(row)"
              title="编辑信息"
            >
              编辑信息
            </el-button>
          </el-button-group>
          
          <el-dropdown @command="(command) => handleDropdownCommand(command, row)">
            <el-button size="small" :icon="More" style="margin-left: 8px;">
              更多
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item 
                  v-if="row.status !== 1" 
                  command="publish"
                  :icon="Upload"
                >
                  发布
                </el-dropdown-item>
                <el-dropdown-item 
                  v-if="row.status === 1" 
                  command="hide"
                  :icon="Hide"
                >
                  隐藏
                </el-dropdown-item>
                <el-dropdown-item command="copy" :icon="CopyDocument">
                  复制
                </el-dropdown-item>
                <el-dropdown-item 
                  command="delete" 
                  :icon="Delete"
                  style="color: #f56c6c;"
                >
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建/编辑图谱信息对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="mapFormRef"
        :model="mapForm"
        :rules="mapFormRules"
        label-width="100px"
      >
        <el-form-item label="图谱标题" prop="title">
          <el-input
            v-model="mapForm.title"
            placeholder="请输入图谱标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="图谱描述" prop="description">
          <el-input
            v-model="mapForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入图谱描述"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="封面图片" prop="coverImage">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept="image/*"
            drag
            style="width: 100%;"
          >
            <div v-if="mapForm.coverImage" class="cover-preview">
              <img :src="mapForm.coverImage" alt="封面预览" />
              <div class="cover-overlay">
                <el-button type="primary" :icon="Upload" size="small">
                  重新上传
                </el-button>
                <el-button type="danger" :icon="Delete" size="small" @click.stop="removeCoverImage">
                  删除
                </el-button>
              </div>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon class="el-icon--upload" size="48"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将图片拖到此处，或<em>点击上传</em>
              </div>
              <div class="el-upload__tip">
                支持 JPG、PNG、GIF 格式，文件大小不超过 2MB
              </div>
            </div>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="排序权重" prop="sortOrder">
          <el-input-number
            v-model="mapForm.sortOrder"
            :min="0"
            :max="9999"
            controls-position="right"
          />
          <span class="form-tip">数值越大排序越靠前</span>
        </el-form-item>
        
        <el-form-item v-if="!isEdit" label="初始状态" prop="status">
          <el-radio-group v-model="mapForm.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">直接发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Delete, Search, RefreshLeft, Edit, Setting, More,
  Upload, Hide, CopyDocument, Picture, Connection, View, UploadFilled
} from '@element-plus/icons-vue'
import {
  getKnowledgeMapList,
  createKnowledgeMap,
  updateKnowledgeMap,
  publishKnowledgeMap,
  hideKnowledgeMap,
  deleteKnowledgeMap,
  deleteBatchKnowledgeMaps,
  copyKnowledgeMap
} from '@/api/knowledge'
import { fileAPI } from '@/api/filestorage'
import router from '@/router'

// 响应式数据
const loading = ref(false)
const mapList = ref([])
const multipleSelection = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: null
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentMapId = ref(null)

// 上传相关
const uploadRef = ref()
const uploading = ref(false)

const dialogTitle = computed(() => {
  return isEdit.value ? '编辑图谱信息' : '创建知识图谱'
})

// 表单数据
const mapForm = reactive({
  title: '',
  description: '',
  coverImage: '',
  sortOrder: 0,
  status: 0
})

const mapFormRef = ref()

// 表单验证规则
const mapFormRules = {
  title: [
    { required: true, message: '请输入图谱标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
  ],
  description: [
    { max: 2000, message: '描述长度不能超过2000个字符', trigger: 'blur' }
  ],
  coverImage: [
    { max: 500, message: 'URL长度不能超过500个字符', trigger: 'blur' }
  ]
}

// 方法
const fetchMapList = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      ...searchForm
    }
    
    const data = await getKnowledgeMapList(params)
    mapList.value = data.records || []
    pagination.total = data.total || 0
    pagination.page = data.pageNum || 1
  } catch (error) {
    ElMessage.error('获取图谱列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchMapList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.page = 1
  fetchMapList()
}

const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchMapList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchMapList()
}

const getStatusTagType = (status) => {
  const typeMap = {
    0: 'info',     // 草稿
    1: 'success',  // 已发布
    2: 'warning'   // 已隐藏
  }
  return typeMap[status] || 'info'
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 创建图谱
const handleCreate = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑结构 - 跳转到可视化编辑器
const handleEditStructure = (row) => {
  router.push(`/knowledge/maps/${row.id}/edit`)
}

// 编辑图谱信息
const handleEditInfo = (row) => {
  resetForm()
  isEdit.value = true
  currentMapId.value = row.id
  
  // 填充表单数据
  Object.assign(mapForm, {
    title: row.title,
    description: row.description,
    coverImage: row.coverImage,
    sortOrder: row.sortOrder
  })
  
  dialogVisible.value = true
}

// 下拉菜单操作
const handleDropdownCommand = async (command, row) => {
  switch (command) {
    case 'publish':
      await handlePublish(row)
      break
    case 'hide':
      await handleHide(row)
      break
    case 'copy':
      await handleCopy(row)
      break
    case 'delete':
      await handleDelete(row)
      break
  }
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确认发布此知识图谱？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await publishKnowledgeMap(row.id)
    ElMessage.success('发布成功')
    fetchMapList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

const handleHide = async (row) => {
  try {
    await ElMessageBox.confirm('确认隐藏此知识图谱？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await hideKnowledgeMap(row.id)
    ElMessage.success('隐藏成功')
    fetchMapList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('隐藏失败')
    }
  }
}

const handleCopy = async (row) => {
  try {
    const { value: title } = await ElMessageBox.prompt('请输入复制后的图谱名称', '复制知识图谱', {
      confirmButtonText: '确认复制',
      cancelButtonText: '取消',
      inputValue: `${row.title} - 副本`,
      inputPlaceholder: '图谱名称',
      inputValidator: (value) => {
        if (!value || !value.trim()) {
          return '请输入图谱名称'
        }
        if (value.length > 200) {
          return '名称长度不能超过200个字符'
        }
        return true
      }
    })
    
    await copyKnowledgeMap(row.id, title.trim())
    ElMessage.success('图谱复制成功')
    
    // 刷新列表
    handleSearch()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('复制失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除此知识图谱？删除后无法恢复！', '危险操作', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await deleteKnowledgeMap(row.id)
    ElMessage.success('删除成功')
    fetchMapList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${multipleSelection.value.length} 个知识图谱？删除后无法恢复！`,
      '批量删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    const ids = multipleSelection.value.map(item => item.id)
    await deleteBatchKnowledgeMaps(ids)
    ElMessage.success('批量删除成功')
    fetchMapList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    await mapFormRef.value.validate()
    
    if (isEdit.value) {
      await updateKnowledgeMap(currentMapId.value, mapForm)
      ElMessage.success('更新成功')
    } else {
      await createKnowledgeMap(mapForm)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchMapList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  }
}

const handleDialogClose = () => {
  resetForm()
}

// 文件上传相关方法
const handleFileChange = async (file) => {
  if (!file) return
  
  // 文件类型验证
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF 格式的图片')
    return
  }
  
  // 文件大小验证 (2MB)
  const maxSize = 2 * 1024 * 1024
  if (file.raw.size > maxSize) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }
  
  try {
    uploading.value = true
    
    // 上传文件
    const result = await fileAPI.uploadSingle(file.raw, 'knowledge', 'cover')
    
    // 设置封面图片URL
    mapForm.coverImage = result.url
    
    ElMessage.success('封面上传成功')
  } catch (error) {
    ElMessage.error('上传失败：' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

const removeCoverImage = () => {
  mapForm.coverImage = ''
}

const resetForm = () => {
  Object.assign(mapForm, {
    title: '',
    description: '',
    coverImage: '',
    sortOrder: 0,
    status: 0
  })
  currentMapId.value = null
  mapFormRef.value?.resetFields()
  
  // 清空上传组件
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 生命周期
onMounted(() => {
  fetchMapList()
})
</script>

<style scoped>
.knowledge-map-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.cover-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #c0c4cc;
}

.title-container {
  padding: 4px 0;
}

.map-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.map-description {
  margin: 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.stats-container {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.stat-item .el-icon {
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 上传组件样式 */
.upload-placeholder {
  padding: 40px 20px;
  text-align: center;
  color: #909399;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-placeholder:hover {
  border-color: #409eff;
}

:deep(.el-upload__text) {
  margin-top: 16px;
  font-size: 14px;
}

:deep(.el-upload__text em) {
  color: #409eff;
  font-style: normal;
}

:deep(.el-upload__tip) {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.cover-preview {
  position: relative;
  margin-top: 8px;
  cursor: pointer;
}

.cover-preview:hover .cover-overlay {
  opacity: 1;
}

.cover-preview img {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  display: block;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
  border-radius: 4px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}
</style> 