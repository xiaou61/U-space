<template>
  <div class="vote-activity-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>投票活动管理</span>
          <el-button type="primary" @click="openAddActivityDialog">
            <el-icon><Plus /></el-icon>
            添加活动
          </el-button>
        </div>
      </template>

      <!-- 活动列表 -->
      <el-table :data="activityList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="活动ID" width="250" />
        <el-table-column prop="title" label="活动名称" width="200" />
        <el-table-column prop="description" label="活动描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '进行中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="选项数量" width="120">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-tag type="info" size="small">{{ scope.row.options?.length || 0 }}个选项</el-tag>
              <el-button size="small" @click="viewOptions(scope.row)">查看选项</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editActivity(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="warning" @click="manageOptions(scope.row)">
              管理选项
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="offlineActivity(scope.row)"
              :disabled="scope.row.status === 0"
            >
              下架
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 添加/编辑活动对话框 -->
    <el-dialog 
      :title="isEditActivity ? '编辑活动' : '添加活动'"
      v-model="activityDialogVisible" 
      width="600px"
    >
      <el-form :model="activityForm" :rules="activityRules" ref="activityFormRef" label-width="100px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="activityForm.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动描述" prop="activityDesc">
          <el-input 
            v-model="activityForm.activityDesc" 
            type="textarea"
            :rows="3"
            placeholder="请输入活动描述"
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="activityForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="activityForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="activityForm.status">
            <el-radio :label="1">进行中</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="activityDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveActivity" :loading="saveLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 管理选项对话框 -->
    <el-dialog 
      :title="`管理投票选项 - ${currentActivity?.title || currentActivity?.activityName || '未知活动'}`"
      v-model="optionsDialogVisible" 
      width="800px"
    >
      <div style="margin-bottom: 20px">
        <el-button type="primary" @click="openAddOptionDialog">
          <el-icon><Plus /></el-icon>
          添加选项
        </el-button>
      </div>
      
      <el-table :data="currentOptions" stripe style="width: 100%">
        <el-table-column prop="optionName" label="选项名称" width="200" />
        <el-table-column prop="optionDesc" label="选项描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="voteCount" label="票数" width="100" />
        <el-table-column prop="imageUrl" label="图片" width="100">
          <template #default="scope">
            <el-image 
              v-if="scope.row.imageUrl"
              :src="scope.row.imageUrl"
              style="width: 50px; height: 50px"
              fit="cover"
            />
            <span v-else>无图片</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="editOption(scope.row)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteOption(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加/编辑选项对话框 -->
    <el-dialog 
      :title="isEditOption ? '编辑选项' : '添加选项'"
      v-model="optionDialogVisible" 
      width="500px"
    >
      <el-form :model="optionForm" :rules="optionRules" ref="optionFormRef" label-width="100px">
        <el-form-item label="选项名称" prop="optionName">
          <el-input v-model="optionForm.optionName" placeholder="请输入选项名称" />
        </el-form-item>
        <el-form-item label="选项描述" prop="optionDesc">
          <el-input 
            v-model="optionForm.optionDesc" 
            type="textarea"
            :rows="2"
            placeholder="请输入选项描述"
          />
        </el-form-item>
        <el-form-item label="选项图片" prop="imageUrl">
          <div class="upload-container">
            <el-upload
              class="image-uploader"
              :action="''"
              :http-request="handleImageUpload"
              :show-file-list="false"
              :before-upload="beforeImageUpload"
              accept="image/*"
            >
              <el-image 
                v-if="optionForm.imageUrl" 
                :src="optionForm.imageUrl" 
                class="uploaded-image"
                fit="cover"
              />
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">点击上传图片</div>
              </div>
            </el-upload>
            <div class="upload-actions" v-if="optionForm.imageUrl">
              <el-button size="small" @click="optionForm.imageUrl = ''" type="danger">
                删除图片
              </el-button>
            </div>
            <div class="upload-tips">
              <p>支持jpg、png、gif格式，文件大小不超过2MB</p>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="optionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveOption" :loading="saveOptionLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getVoteActivityList, 
  editVoteActivity, 
  offlineVoteActivity,
  editVoteOption,
  deleteVoteOption
} from '../api/vote'
import { uploadFile } from '../api/file'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const saveOptionLoading = ref(false)
const activityList = ref([])

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 活动相关数据
const activityDialogVisible = ref(false)
const isEditActivity = ref(false)
const activityFormRef = ref()
const activityForm = reactive({
  id: null,
  activityName: '',
  activityDesc: '',
  startTime: '',
  endTime: '',
  status: 1
})

// 选项相关数据
const optionsDialogVisible = ref(false)
const optionDialogVisible = ref(false)
const isEditOption = ref(false)
const currentActivity = ref(null)
const currentOptions = ref([])
const optionFormRef = ref()
const optionForm = reactive({
  id: null,
  optionName: '',
  optionDesc: '',
  imageUrl: ''
})

// 表单验证规则
const activityRules = {
  activityName: [
    { required: true, message: '请输入活动名称', trigger: 'blur' }
  ],
  activityDesc: [
    { required: true, message: '请输入活动描述', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

const optionRules = {
  optionName: [
    { required: true, message: '请输入选项名称', trigger: 'blur' }
  ]
}

// 页面加载时获取数据
onMounted(() => {
  loadActivityList()
})

// 加载活动列表
const loadActivityList = async () => {
  loading.value = true
  try {
    const response = await getVoteActivityList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    if (response.code === 200) {
      activityList.value = response.data.list || response.data.records
      pagination.total = parseInt(response.data.total) || 0
    } else {
      ElMessage.error(response.message || '获取活动列表失败')
    }
  } catch (error) {
    ElMessage.error('获取活动列表失败')
    console.error('Error loading activity list:', error)
  } finally {
    loading.value = false
  }
}

// 分页相关方法
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1  // 改变页大小时重置到第一页
  loadActivityList()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadActivityList()
}

// 打开添加活动对话框
const openAddActivityDialog = () => {
  isEditActivity.value = false
  resetActivityForm()
  activityDialogVisible.value = true
}

// 编辑活动
const editActivity = (row) => {
  isEditActivity.value = true
  activityForm.id = row.id
  activityForm.activityName = row.title || row.activityName        // 兼容字段名
  activityForm.activityDesc = row.description || row.activityDesc  // 兼容字段名
  // 将字符串日期转换为Date对象
  activityForm.startTime = row.startTime ? new Date(row.startTime.replace(' ', 'T')) : null
  activityForm.endTime = row.endTime ? new Date(row.endTime.replace(' ', 'T')) : null
  activityForm.status = row.status
  activityDialogVisible.value = true
}

// 保存活动
const saveActivity = async () => {
  if (!activityFormRef.value) return
  
  await activityFormRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        // 转换日期格式为后端期望的格式 "yyyy-MM-dd HH:mm:ss"
        const formatDateTime = (date) => {
          if (!date) return null
          const d = new Date(date)
          const year = d.getFullYear()
          const month = String(d.getMonth() + 1).padStart(2, '0')
          const day = String(d.getDate()).padStart(2, '0')
          const hours = String(d.getHours()).padStart(2, '0')
          const minutes = String(d.getMinutes()).padStart(2, '0')
          const seconds = String(d.getSeconds()).padStart(2, '0')
          return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
        }

        const formData = {
          id: activityForm.id,
          title: activityForm.activityName,        // 映射字段名
          description: activityForm.activityDesc,  // 映射字段名
          startTime: formatDateTime(activityForm.startTime),
          endTime: formatDateTime(activityForm.endTime),
          status: activityForm.status
        }

        const response = await editVoteActivity(formData)
        if (response.code === 200) {
          ElMessage.success(response.message || '保存成功')
          activityDialogVisible.value = false
          loadActivityList()
        } else {
          ElMessage.error(response.message || '保存失败')
        }
      } catch (error) {
        ElMessage.error('保存失败')
        console.error('Error saving activity:', error)
      } finally {
        saveLoading.value = false
      }
    }
  })
}

// 下架活动
const offlineActivity = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要下架活动"${row.title || row.activityName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await offlineVoteActivity(row.id)
    if (response.code === 200) {
      ElMessage.success('下架成功')
      loadActivityList()
    } else {
      ElMessage.error(response.message || '下架失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('下架失败')
      console.error('Error offline activity:', error)
    }
  }
}

// 查看选项
const viewOptions = (row) => {
  currentActivity.value = row
  currentOptions.value = row.options || []
  optionsDialogVisible.value = true
}

// 管理选项
const manageOptions = (row) => {
  currentActivity.value = row
  currentOptions.value = row.options || []
  optionsDialogVisible.value = true
}

// 打开添加选项对话框
const openAddOptionDialog = () => {
  isEditOption.value = false
  resetOptionForm()
  optionDialogVisible.value = true
}

// 编辑选项
const editOption = (row) => {
  isEditOption.value = true
  optionForm.id = row.id
  optionForm.optionName = row.optionName
  optionForm.optionDesc = row.optionDesc
  optionForm.imageUrl = row.imageUrl
  optionDialogVisible.value = true
}

// 保存选项
const saveOption = async () => {
  if (!optionFormRef.value) return
  
  await optionFormRef.value.validate(async (valid) => {
    if (valid) {
      saveOptionLoading.value = true
      try {
        const response = await editVoteOption(currentActivity.value.id, optionForm)
        if (response.code === 200) {
          ElMessage.success(response.message || '保存成功')
          optionDialogVisible.value = false
          
          // 立即更新当前选项列表和活动列表
          await loadActivityList()
          
          // 重新获取当前活动的最新选项数据
          const updatedActivity = activityList.value.find(item => item.id === currentActivity.value.id)
          if (updatedActivity) {
            currentActivity.value = updatedActivity
            currentOptions.value = updatedActivity.options || []
          }
        } else {
          ElMessage.error(response.message || '保存失败')
        }
      } catch (error) {
        ElMessage.error('保存失败')
        console.error('Error saving option:', error)
      } finally {
        saveOptionLoading.value = false
      }
    }
  })
}

// 删除选项
const deleteOption = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选项"${row.optionName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await deleteVoteOption(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      
      // 立即更新当前选项列表和活动列表
      await loadActivityList()
      
      // 重新获取当前活动的最新选项数据
      const updatedActivity = activityList.value.find(item => item.id === currentActivity.value.id)
      if (updatedActivity) {
        currentActivity.value = updatedActivity
        currentOptions.value = updatedActivity.options || []
      }
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('Error deleting option:', error)
    }
  }
}

// 重置表单
const resetActivityForm = () => {
  activityForm.id = null
  activityForm.activityName = ''
  activityForm.activityDesc = ''
  activityForm.startTime = ''
  activityForm.endTime = ''
  activityForm.status = 1
}

const resetOptionForm = () => {
  optionForm.id = null
  optionForm.optionName = ''
  optionForm.optionDesc = ''
  optionForm.imageUrl = ''
}

// 图片上传相关方法
const beforeImageUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isValidType) {
    ElMessage.error('只能上传 JPG/PNG/GIF 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleImageUpload = async (options) => {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response = await uploadFile(formData)
    if (response.code === 200) {
      optionForm.imageUrl = response.data
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(response.message || '图片上传失败')
    }
  } catch (error) {
    ElMessage.error('图片上传失败')
    console.error('Error uploading image:', error)
  }
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  // 处理字符串格式的日期 "2025-08-04 00:00:00"
  const date = new Date(datetime.replace(' ', 'T'))
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vote-activity-management {
  padding: 20px;
}

/* 图片上传组件样式 */
.upload-container {
  width: 100%;
}

.image-uploader {
  display: block;
}

.uploaded-image {
  width: 120px;
  height: 120px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-placeholder:hover {
  border-color: #409eff;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.upload-text {
  color: #8c939d;
  font-size: 14px;
}

.upload-actions {
  margin-top: 10px;
}

.upload-tips {
  margin-top: 8px;
}

.upload-tips p {
  color: #909399;
  font-size: 12px;
  margin: 0;
}
</style> 