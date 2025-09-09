<template>
  <div class="system-settings">
    <div class="header">
      <h2>系统设置</h2>
      <div class="header-actions">
        <el-button type="primary" @click="saveSettings" :loading="saving">
          <el-icon><Check /></el-icon>
          保存设置
        </el-button>
        <el-button @click="loadSettings">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 系统概览 -->
    <div class="summary-section" v-if="systemSummary">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>系统概览</span>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="summary-item">
              <div class="summary-value">{{ systemSummary.maxFileSize || 0 }}MB</div>
              <div class="summary-label">最大文件大小</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="summary-item">
              <div class="summary-value">{{ systemSummary.moduleStorageQuota || 0 }}GB</div>
              <div class="summary-label">模块存储配额</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="summary-item">
              <div class="summary-value">{{ systemSummary.tempLinkExpireHours || 0 }}小时</div>
              <div class="summary-label">临时链接有效期</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="summary-item">
              <div class="summary-value">{{ systemSummary.allowedFileTypesCount || 0 }}种</div>
              <div class="summary-label">允许文件类型</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <el-row :gutter="20">
      <!-- 上传限制设置 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>上传限制设置</span>
            </div>
          </template>
          <el-form :model="uploadSettings" label-width="150px">
            <el-form-item label="最大文件大小">
              <el-input-number
                v-model="uploadSettings.maxFileSize"
                :min="1"
                :max="1024"
                :step="1"
                controls-position="right"
              />
              <span class="unit-text">MB</span>
            </el-form-item>
            <el-form-item label="单次上传文件数">
              <el-input-number
                v-model="uploadSettings.maxFilesPerUpload"
                :min="1"
                :max="100"
                :step="1"
                controls-position="right"
              />
              <span class="unit-text">个</span>
            </el-form-item>
            <el-form-item label="模块存储配额">
              <el-input-number
                v-model="uploadSettings.moduleStorageQuota"
                :min="1"
                :max="10000"
                :step="1"
                controls-position="right"
              />
              <span class="unit-text">GB</span>
            </el-form-item>
            <el-form-item label="启用重复检测">
              <el-switch
                v-model="uploadSettings.enableDuplicateCheck"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
            <el-form-item label="自动压缩图片">
              <el-switch
                v-model="uploadSettings.autoCompressImage"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 安全设置 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>安全设置</span>
            </div>
          </template>
          <el-form :model="securitySettings" label-width="150px">
            <el-form-item label="默认访问权限">
              <el-radio-group v-model="securitySettings.defaultAccessLevel">
                <el-radio value="private">私有</el-radio>
                <el-radio value="public">公开</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="临时链接有效期">
              <el-input-number
                v-model="securitySettings.tempLinkExpireHours"
                :min="1"
                :max="168"
                :step="1"
                controls-position="right"
              />
              <span class="unit-text">小时</span>
            </el-form-item>
            <el-form-item label="启用病毒扫描">
              <el-switch
                v-model="securitySettings.enableVirusScan"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
            <el-form-item label="记录访问日志">
              <el-switch
                v-model="securitySettings.enableAccessLog"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
            <el-form-item label="IP访问限制">
              <el-switch
                v-model="securitySettings.enableIpRestriction"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 存储设置 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>存储设置</span>
            </div>
          </template>
          <el-form :model="storageSettings" label-width="150px">
            <el-form-item label="启用自动备份">
              <el-switch
                v-model="storageSettings.autoBackupEnabled"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
            <el-form-item label="备份存储类型">
              <el-select v-model="storageSettings.backupStorageType" placeholder="请选择备份存储">
                <el-option label="本地存储" value="LOCAL" />
                <el-option label="阿里云OSS" value="OSS" />
                <el-option label="腾讯云COS" value="COS" />
                <el-option label="七牛云KODO" value="KODO" />
                <el-option label="华为云OBS" value="OBS" />
              </el-select>
            </el-form-item>
            <el-form-item label="本地备份保留天数">
              <el-input-number
                v-model="storageSettings.localBackupRetentionDays"
                :min="1"
                :max="365"
                :step="1"
                controls-position="right"
              />
              <span class="unit-text">天</span>
            </el-form-item>
            <el-form-item label="启用分片上传">
              <el-switch
                v-model="storageSettings.enableChunkUpload"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
            <el-form-item label="分片大小">
              <el-input-number
                v-model="storageSettings.chunkSize"
                :min="1"
                :max="100"
                :step="1"
                controls-position="right"
                :disabled="!storageSettings.enableChunkUpload"
              />
              <span class="unit-text">MB</span>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 性能设置 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>性能设置</span>
            </div>
          </template>
          <el-form :model="performanceSettings" label-width="150px">
            <el-form-item label="启用缓存">
              <el-switch
                v-model="performanceSettings.enableCache"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
            <el-form-item label="缓存过期时间">
              <el-input-number
                v-model="performanceSettings.cacheExpireMinutes"
                :min="5"
                :max="1440"
                :step="5"
                controls-position="right"
                :disabled="!performanceSettings.enableCache"
              />
              <span class="unit-text">分钟</span>
            </el-form-item>
            <el-form-item label="并发上传数">
              <el-input-number
                v-model="performanceSettings.maxConcurrentUploads"
                :min="1"
                :max="10"
                :step="1"
                controls-position="right"
              />
            </el-form-item>
            <el-form-item label="缩略图生成">
              <el-switch
                v-model="performanceSettings.enableThumbnail"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
            <el-form-item label="缩略图大小">
              <el-select 
                v-model="performanceSettings.thumbnailSize" 
                :disabled="!performanceSettings.enableThumbnail"
              >
                <el-option label="128x128" value="128" />
                <el-option label="256x256" value="256" />
                <el-option label="512x512" value="512" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 文件类型白名单 -->
    <div class="file-types-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>文件类型白名单</span>
            <el-button type="primary" size="small" @click="showAddFileTypeDialog">
              添加类型
            </el-button>
          </div>
        </template>
        
        <div class="file-types-grid">
          <div class="file-type-category" v-for="(types, category) in fileTypesByCategory" :key="category">
            <h4>{{ getCategoryName(category) }}</h4>
            <div class="file-type-list">
              <el-tag
                v-for="type in types"
                :key="type"
                closable
                @close="removeFileType(type)"
                type="info"
                class="file-type-tag"
              >
                {{ type }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 添加文件类型对话框 -->
    <el-dialog v-model="fileTypeDialogVisible" title="添加文件类型" width="500px">
      <el-form :model="fileTypeForm" label-width="100px">
        <el-form-item label="文件类型">
          <el-input v-model="fileTypeForm.extension" placeholder="如: jpg, pdf, doc" />
          <div class="form-tip">请输入文件扩展名，不包含点号</div>
        </el-form-item>
        <el-form-item label="MIME类型">
          <el-input v-model="fileTypeForm.mimeType" placeholder="如: image/jpeg, application/pdf" />
          <div class="form-tip">可选，用于更精确的类型检查</div>
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="fileTypeForm.category" placeholder="请选择类别">
            <el-option label="图片" value="image" />
            <el-option label="文档" value="document" />
            <el-option label="视频" value="video" />
            <el-option label="音频" value="audio" />
            <el-option label="压缩包" value="archive" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="fileTypeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addFileType">添加</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Refresh } from '@element-plus/icons-vue'
import { systemAPI } from '@/api/filestorage'

// 响应式数据
const saving = ref(false)
const systemSummary = ref({})
const allowedFileTypes = ref([])
const fileTypeDialogVisible = ref(false)

// 设置表单
const uploadSettings = reactive({
  maxFileSize: 100,
  maxFilesPerUpload: 10,
  moduleStorageQuota: 1000,
  enableDuplicateCheck: true,
  autoCompressImage: true
})

const securitySettings = reactive({
  defaultAccessLevel: 'private',
  tempLinkExpireHours: 24,
  enableVirusScan: false,
  enableAccessLog: true,
  enableIpRestriction: false
})

const storageSettings = reactive({
  autoBackupEnabled: true,
  backupStorageType: 'LOCAL',
  localBackupRetentionDays: 30,
  enableChunkUpload: false,
  chunkSize: 5
})

const performanceSettings = reactive({
  enableCache: true,
  cacheExpireMinutes: 60,
  maxConcurrentUploads: 3,
  enableThumbnail: true,
  thumbnailSize: '256'
})

const fileTypeForm = reactive({
  extension: '',
  mimeType: '',
  category: ''
})

// 文件类型分类
const fileTypeCategories = {
  image: { name: '图片', extensions: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'] },
  document: { name: '文档', extensions: ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt'] },
  video: { name: '视频', extensions: ['mp4', 'avi', 'mkv', 'mov', 'wmv', 'flv', 'webm'] },
  audio: { name: '音频', extensions: ['mp3', 'wav', 'flac', 'aac', 'ogg', 'wma'] },
  archive: { name: '压缩包', extensions: ['zip', 'rar', '7z', 'tar', 'gz'] },
  other: { name: '其他', extensions: [] }
}

// 计算属性
const fileTypesByCategory = computed(() => {
  const result = {}
  
  Object.keys(fileTypeCategories).forEach(category => {
    result[category] = allowedFileTypes.value.filter(type => {
      if (category === 'other') {
        // 其他类别包含不在预定义类别中的文件类型
        return !Object.values(fileTypeCategories)
          .some(cat => cat.extensions.includes(type))
      }
      return fileTypeCategories[category].extensions.includes(type)
    })
  })
  
  return result
})

// 生命周期
onMounted(() => {
  loadSettings()
  loadSystemSummary()
  loadFileTypes()
})

// 方法
const loadSettings = async () => {
  try {
    const data = await systemAPI.getSystemSettings()
    
    // 解析设置数据
    Object.keys(data).forEach(key => {
      const value = data[key]
      
      // 上传设置
      if (key === 'maxFileSize') uploadSettings.maxFileSize = parseInt(value)
      if (key === 'maxFilesPerUpload') uploadSettings.maxFilesPerUpload = parseInt(value)
      if (key === 'moduleStorageQuota') uploadSettings.moduleStorageQuota = parseInt(value)
      if (key === 'enableDuplicateCheck') uploadSettings.enableDuplicateCheck = value === 'true'
      if (key === 'autoCompressImage') uploadSettings.autoCompressImage = value === 'true'
      
      // 安全设置
      if (key === 'defaultAccessLevel') securitySettings.defaultAccessLevel = value
      if (key === 'tempLinkExpireHours') securitySettings.tempLinkExpireHours = parseInt(value)
      if (key === 'enableVirusScan') securitySettings.enableVirusScan = value === 'true'
      if (key === 'enableAccessLog') securitySettings.enableAccessLog = value === 'true'
      if (key === 'enableIpRestriction') securitySettings.enableIpRestriction = value === 'true'
      
      // 存储设置
      if (key === 'autoBackupEnabled') storageSettings.autoBackupEnabled = value === 'true'
      if (key === 'backupStorageType') storageSettings.backupStorageType = value
      if (key === 'localBackupRetentionDays') storageSettings.localBackupRetentionDays = parseInt(value)
      if (key === 'enableChunkUpload') storageSettings.enableChunkUpload = value === 'true'
      if (key === 'chunkSize') storageSettings.chunkSize = parseInt(value)
      
      // 性能设置
      if (key === 'enableCache') performanceSettings.enableCache = value === 'true'
      if (key === 'cacheExpireMinutes') performanceSettings.cacheExpireMinutes = parseInt(value)
      if (key === 'maxConcurrentUploads') performanceSettings.maxConcurrentUploads = parseInt(value)
      if (key === 'enableThumbnail') performanceSettings.enableThumbnail = value === 'true'
      if (key === 'thumbnailSize') performanceSettings.thumbnailSize = value
    })
  } catch (error) {
    console.error('获取系统设置失败:', error)
  }
}

const loadSystemSummary = async () => {
  try {
    const data = await systemAPI.getSystemSummary()
    systemSummary.value = data
  } catch (error) {
    console.error('获取系统概览失败:', error)
  }
}

const loadFileTypes = async () => {
  try {
    const data = await systemAPI.getFileTypes()
    allowedFileTypes.value = data
  } catch (error) {
    console.error('获取文件类型失败:', error)
  }
}

const saveSettings = async () => {
  saving.value = true
  try {
    // 合并所有设置
    const allSettings = {
      // 上传设置
      maxFileSize: uploadSettings.maxFileSize.toString(),
      maxFilesPerUpload: uploadSettings.maxFilesPerUpload.toString(),
      moduleStorageQuota: uploadSettings.moduleStorageQuota.toString(),
      enableDuplicateCheck: uploadSettings.enableDuplicateCheck.toString(),
      autoCompressImage: uploadSettings.autoCompressImage.toString(),
      
      // 安全设置
      defaultAccessLevel: securitySettings.defaultAccessLevel,
      tempLinkExpireHours: securitySettings.tempLinkExpireHours.toString(),
      enableVirusScan: securitySettings.enableVirusScan.toString(),
      enableAccessLog: securitySettings.enableAccessLog.toString(),
      enableIpRestriction: securitySettings.enableIpRestriction.toString(),
      
      // 存储设置
      autoBackupEnabled: storageSettings.autoBackupEnabled.toString(),
      backupStorageType: storageSettings.backupStorageType,
      localBackupRetentionDays: storageSettings.localBackupRetentionDays.toString(),
      enableChunkUpload: storageSettings.enableChunkUpload.toString(),
      chunkSize: storageSettings.chunkSize.toString(),
      
      // 性能设置
      enableCache: performanceSettings.enableCache.toString(),
      cacheExpireMinutes: performanceSettings.cacheExpireMinutes.toString(),
      maxConcurrentUploads: performanceSettings.maxConcurrentUploads.toString(),
      enableThumbnail: performanceSettings.enableThumbnail.toString(),
      thumbnailSize: performanceSettings.thumbnailSize
    }
    
    await systemAPI.updateSystemSettings(allSettings)
    ElMessage.success('系统设置保存成功')
    
    // 刷新概览数据
    loadSystemSummary()
  } catch (error) {
    ElMessage.error('保存系统设置失败：' + error.message)
  } finally {
    saving.value = false
  }
}

const showAddFileTypeDialog = () => {
  Object.assign(fileTypeForm, {
    extension: '',
    mimeType: '',
    category: ''
  })
  fileTypeDialogVisible.value = true
}

const addFileType = async () => {
  if (!fileTypeForm.extension.trim()) {
    ElMessage.warning('请输入文件扩展名')
    return
  }
  
  const extension = fileTypeForm.extension.toLowerCase().trim()
  
  if (allowedFileTypes.value.includes(extension)) {
    ElMessage.warning('该文件类型已存在')
    return
  }
  
  try {
    const newFileTypes = [...allowedFileTypes.value, extension]
    await systemAPI.updateFileTypes(newFileTypes)
    allowedFileTypes.value = newFileTypes
    fileTypeDialogVisible.value = false
    ElMessage.success('添加文件类型成功')
  } catch (error) {
    ElMessage.error('添加文件类型失败：' + error.message)
  }
}

const removeFileType = async (extension) => {
  try {
    await ElMessageBox.confirm('确定要删除此文件类型吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const newFileTypes = allowedFileTypes.value.filter(type => type !== extension)
    await systemAPI.updateFileTypes(newFileTypes)
    allowedFileTypes.value = newFileTypes
    ElMessage.success('删除文件类型成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除文件类型失败：' + error.message)
    }
  }
}

const getCategoryName = (category) => {
  return fileTypeCategories[category]?.name || category
}
</script>

<style scoped>
.system-settings {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.summary-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-item {
  text-align: center;
  padding: 10px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.summary-label {
  font-size: 14px;
  color: #909399;
}

.unit-text {
  margin-left: 10px;
  color: #909399;
}

.file-types-section {
  margin-top: 20px;
}

.file-types-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.file-type-category h4 {
  margin: 0 0 10px 0;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 5px;
}

.file-type-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.file-type-tag {
  margin: 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.dialog-footer {
  text-align: right;
}
</style> 