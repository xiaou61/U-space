<template>
  <div class="video-management">
    <div class="header">
      <h2>入学必看视频管理</h2>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon>
        添加视频
      </el-button>
    </div>

    <!-- 视频列表 -->
    <el-card>
      <el-table 
        :data="tableData" 
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无视频数据"
      >
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.coverUrl"
              :src="row.coverUrl"
              :preview-src-list="[row.coverUrl]"
              style="width: 80px; height: 60px"
              fit="cover"
              preview-teleported
            />
            <span v-else style="color: #999">无封面</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="视频标题" min-width="180" />
        <el-table-column label="视频时长" width="100">
          <template #default="{ row }">
            <span v-if="row.duration">{{ formatDuration(row.duration) }}</span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="视频简介" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary" 
              size="small"
              @click="previewVideo(row)"
            >
              预览
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
    </el-card>

    <!-- 添加视频对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="'添加入学必看视频'"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" label-width="100px" ref="formRef">
        <el-form-item 
          label="视频标题" 
          prop="title"
          :rules="[{ required: true, message: '请输入视频标题', trigger: 'blur' }]"
        >
          <el-input v-model="form.title" placeholder="请输入视频标题" />
        </el-form-item>
        
        <el-form-item 
          label="视频文件" 
          prop="url"
        >
          <el-upload
            class="upload-demo"
            :auto-upload="false"
            :on-change="handleVideoChange"
            :on-remove="handleVideoRemove"
            :before-upload="beforeVideoUpload"
            :show-file-list="true"
            accept="video/*"
            :limit="1"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              选择视频文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 mp4, avi, mov 等格式，文件大小不超过100MB
              </div>
            </template>
          </el-upload>
          <div v-if="form.url" style="margin-top: 10px">
            <el-tag type="success">视频已上传</el-tag>
          </div>
        </el-form-item>

        <el-form-item label="封面图片">
          <el-upload
            class="upload-demo"
            :auto-upload="false"
            :on-change="handleCoverChange"
            :on-remove="handleCoverRemove"
            :before-upload="beforeImageUpload"
            :show-file-list="true"
            accept="image/*"
            :limit="1"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              选择封面图片
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 jpg, png 格式，文件大小不超过5MB
              </div>
            </template>
          </el-upload>
          <div v-if="form.coverUrl" style="margin-top: 10px">
            <el-image
              :src="form.coverUrl"
              style="width: 100px; height: 80px"
              fit="cover"
            />
          </div>
        </el-form-item>

        <el-form-item label="视频简介">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入视频简介"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 视频预览对话框 -->
    <el-dialog 
      v-model="previewVisible" 
      :title="currentVideo?.title"
      width="800px"
    >
      <div v-if="currentVideo" style="text-align: center;">
        <video 
          :src="currentVideo.url" 
          controls 
          style="width: 100%; max-height: 400px;"
          preload="metadata"
        >
          您的浏览器不支持视频播放
        </video>
        <div style="margin-top: 15px; text-align: left;">
          <p><strong>简介：</strong>{{ currentVideo.description || '暂无简介' }}</p>
          <p><strong>时长：</strong>{{ currentVideo.duration ? formatDuration(currentVideo.duration) : '未知' }}</p>
          <p><strong>创建时间：</strong>{{ formatDate(currentVideo.createAt) }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { addVideo, deleteVideo, listVideos } from '../api/video'
import { uploadFile } from '../api/file'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const previewVisible = ref(false)
const currentVideo = ref(null)
const formRef = ref()
const videoFile = ref(null)
const coverFile = ref(null)

// 表单数据
const form = reactive({
  title: '',
  url: '',
  coverUrl: '',
  description: ''
})

// 获取视频列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listVideos()
    if (res.data) {
      tableData.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('获取视频列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

// 打开添加对话框
const openAdd = () => {
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    title: '',
    url: '',
    coverUrl: '',
    description: ''
  })
  videoFile.value = null
  coverFile.value = null
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 视频上传前检查
const beforeVideoUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isLt100M = file.size / 1024 / 1024 < 100

  if (!isVideo) {
    ElMessage.error('请上传视频文件!')
    return false
  }
  if (!isLt100M) {
    ElMessage.error('上传视频大小不能超过 100MB!')
    return false
  }
  return true
}

// 图片上传前检查
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('请上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 视频文件选择
const handleVideoChange = (file, fileList) => {
  videoFile.value = file.raw
}

// 视频文件移除
const handleVideoRemove = () => {
  videoFile.value = null
  form.url = ''
}

// 封面文件选择
const handleCoverChange = (file, fileList) => {
  coverFile.value = file.raw
}

// 封面文件移除
const handleCoverRemove = () => {
  coverFile.value = null
  form.coverUrl = ''
}

// 上传视频文件
const uploadVideoFile = async () => {
  if (!videoFile.value) return true
  
  const formData = new FormData()
  formData.append('file', videoFile.value)
  
  try {
    const res = await uploadFile(formData)
    if (res.code === 200) {
      form.url = res.data
      return true
    } else {
      ElMessage.error('视频上传失败')
      return false
    }
  } catch (error) {
    ElMessage.error('视频上传失败')
    return false
  }
}

// 上传封面文件
const uploadCoverFile = async () => {
  if (!coverFile.value) return true
  
  const formData = new FormData()
  formData.append('file', coverFile.value)
  
  try {
    const res = await uploadFile(formData)
    if (res.code === 200) {
      form.coverUrl = res.data
      return true
    } else {
      ElMessage.error('封面上传失败')
      return false
    }
  } catch (error) {
    ElMessage.error('封面上传失败')
    return false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 检查是否选择了视频文件
    if (!videoFile.value && !form.url) {
      ElMessage.error('请选择视频文件')
      return
    }
    
    submitting.value = true
    
    // 上传视频文件
    if (!await uploadVideoFile()) {
      submitting.value = false
      return
    }

    // 上传封面文件
    if (!await uploadCoverFile()) {
      submitting.value = false
      return
    }

    const res = await addVideo(form)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error) {
    console.error('添加视频失败:', error)
    ElMessage.error('添加失败')
  } finally {
    submitting.value = false
  }
}

// 删除视频
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除视频"${row.title}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await deleteVideo(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 预览视频
const previewVideo = (row) => {
  currentVideo.value = row
  previewVisible.value = true
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.video-management {
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
  color: #303133;
}

.dialog-footer {
  text-align: right;
}

.upload-demo {
  width: 100%;
}

:deep(.el-upload) {
  width: 100%;
}

:deep(.el-upload .el-button) {
  width: auto;
}
</style> 