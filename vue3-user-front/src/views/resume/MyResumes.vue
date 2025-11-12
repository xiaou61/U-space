<template>
  <div class="resume-manage-page">
    <el-card class="header-card" shadow="never">
      <div class="header-top">
        <div>
          <h2>在线简历工作台</h2>
          <p class="sub-title">在这里管理、导出、分享你的所有简历</p>
        </div>
        <div class="header-actions">
          <el-button @click="goTemplate">浏览模板</el-button>
          <el-button type="primary" @click="goEditor">
            <el-icon><Plus /></el-icon>
            新建简历
          </el-button>
        </div>
      </div>
      <div class="stats-row">
        <div class="stat-item">
          <p class="label">总简历数</p>
          <p class="value">{{ pagination.total }}</p>
        </div>
        <div class="stat-item">
          <p class="label">最近导出</p>
          <p class="value">{{ lastExportTime || '--' }}</p>
        </div>
        <div class="stat-item">
          <p class="label">最近更新</p>
          <p class="value">{{ lastUpdateTime || '--' }}</p>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <div class="table-tools">
        <el-input
          v-model="query.keyword"
          placeholder="输入关键词搜索简历"
          clearable
          :prefix-icon="Search"
          @clear="handleSearch"
        />
        <el-select v-model="query.status" placeholder="状态" clearable @change="handleSearch">
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
        </el-select>
        <el-select v-model="query.visibility" placeholder="可见性" clearable @change="handleSearch">
          <el-option label="私密" :value="0" />
          <el-option label="公开" :value="1" />
        </el-select>
        <el-button type="primary" plain @click="handleSearch">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table
        :data="resumeList"
        v-loading="loading"
        border
        class="resume-table"
        empty-text="暂无简历，点击右上角新建"
      >
        <el-table-column prop="resumeName" label="简历名称" min-width="200">
          <template #default="scope">
            <div class="name-cell">
              <span class="name">{{ scope.row.resumeName }}</span>
              <el-tag size="small" type="info">v{{ scope.row.version }}</el-tag>
            </div>
            <p class="summary">{{ scope.row.summary || '暂无简介' }}</p>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可见性" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.visibility === 1 ? 'success' : 'warning'">
              {{ scope.row.visibility === 1 ? '公开' : '私密' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="200" />
        <el-table-column label="操作" fixed="right" width="360">
          <template #default="scope">
            <el-button text type="primary" @click="handlePreview(scope.row)">预览</el-button>
            <el-button text type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button text type="primary" @click="openExportDialog(scope.row)">导出</el-button>
            <el-button text type="primary" @click="handleShare(scope.row)">分享</el-button>
            <el-button text type="primary" @click="handleAnalytics(scope.row)">统计</el-button>
            <el-popconfirm title="确认删除该简历？" @confirm="handleDelete(scope.row)">
              <template #reference>
                <el-button text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="pagination.total > pagination.size">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 预览抽屉 -->
    <el-drawer v-model="previewVisible" size="40%" :title="previewData?.resume?.resumeName || '简历预览'">
      <div v-if="previewData">
        <p class="preview-summary">{{ previewData.resume?.summary }}</p>
        <el-timeline>
          <el-timeline-item
            v-for="section in previewData.sections"
            :key="section.id || section.title"
            :timestamp="section.sectionType"
          >
            <h4>{{ section.title }}</h4>
            <pre>{{ section.content }}</pre>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>

    <!-- 导出对话框 -->
    <el-dialog v-model="exportDialogVisible" title="导出简历" width="420px">
      <el-form :model="exportForm" label-width="80px">
        <el-form-item label="格式">
          <el-select v-model="exportForm.format">
            <el-option label="PDF" value="PDF" />
            <el-option label="Word" value="WORD" />
            <el-option label="HTML" value="HTML" />
          </el-select>
        </el-form-item>
        <el-form-item label="主题">
          <el-input v-model="exportForm.theme" placeholder="用于标记导出主题，可选" />
        </el-form-item>
        <el-form-item label="水印">
          <el-switch v-model="exportForm.watermark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="exportLoading" @click="submitExport">立即导出</el-button>
      </template>
    </el-dialog>

    <!-- 分享信息 -->
    <el-dialog v-model="shareDialogVisible" title="分享链接" width="420px">
      <el-descriptions :column="1" border v-if="shareData">
        <el-descriptions-item label="访问链接">
          <el-input v-model="shareData.shareUrl" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="分享口令">
          {{ shareData.shareCode || '无需口令' }}
        </el-descriptions-item>
        <el-descriptions-item label="到期时间">
          {{ shareData.expireTime }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="shareDialogVisible = false">知道了</el-button>
      </template>
    </el-dialog>

    <!-- 数据统计 -->
    <el-dialog v-model="analyticsDialogVisible" title="访问统计" width="480px">
      <el-row :gutter="16">
        <el-col :span="12">
          <div class="metric-card">
            <p class="metric-label">浏览次数</p>
            <p class="metric-value">{{ analyticsData?.viewCount ?? 0 }}</p>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="metric-card">
            <p class="metric-label">导出次数</p>
            <p class="metric-value">{{ analyticsData?.exportCount ?? 0 }}</p>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="metric-card">
            <p class="metric-label">分享次数</p>
            <p class="metric-value">{{ analyticsData?.shareCount ?? 0 }}</p>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="metric-card">
            <p class="metric-label">访客数</p>
            <p class="metric-value">{{ analyticsData?.uniqueVisitors ?? 0 }}</p>
          </div>
        </el-col>
      </el-row>
      <p class="metric-footer">
        最近访问：{{ analyticsData?.lastAccessTime || '暂无记录' }}
      </p>
      <template #footer>
        <el-button type="primary" @click="analyticsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { resumeApi } from '@/api/resume'

const router = useRouter()
const loading = ref(false)
const resumeList = ref([])
const lastExportTime = ref('')
const lastUpdateTime = ref('')

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const query = reactive({
  keyword: '',
  status: '',
  visibility: ''
})

const previewVisible = ref(false)
const previewData = ref(null)

const exportDialogVisible = ref(false)
const exportResumeId = ref(null)
const exportLoading = ref(false)
const exportForm = reactive({
  format: 'PDF',
  theme: '',
  watermark: true
})

const shareDialogVisible = ref(false)
const shareData = ref(null)

const analyticsDialogVisible = ref(false)
const analyticsData = ref(null)

const fetchResumes = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: query.keyword || undefined,
      status: query.status !== '' ? query.status : undefined,
      visibility: query.visibility !== '' ? query.visibility : undefined
    }
    const result = await resumeApi.getMyResumes(params)
    resumeList.value = result?.records || []
    pagination.total = result?.total || resumeList.value.length
    lastUpdateTime.value = resumeList.value[0]?.updateTime || ''
  } catch (error) {
    ElMessage.error('加载简历列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchResumes()
}

const resetQuery = () => {
  query.keyword = ''
  query.status = ''
  query.visibility = ''
  pagination.page = 1
  fetchResumes()
}

const handlePageChange = (page) => {
  pagination.page = page
  fetchResumes()
}

const goEditor = () => {
  router.push('/resume/editor')
}

const goTemplate = () => {
  router.push('/resume/templates')
}

const handleEdit = (row) => {
  router.push(`/resume/editor/${row.id}`)
}

const handlePreview = async (row) => {
  try {
    previewData.value = await resumeApi.previewResume(row.id)
    previewVisible.value = true
  } catch (error) {
    ElMessage.error('获取预览失败')
  }
}

const openExportDialog = (row) => {
  exportResumeId.value = row.id
  exportForm.theme = row.resumeName
  exportDialogVisible.value = true
}

const submitExport = async () => {
  if (!exportResumeId.value) return
  exportLoading.value = true
  try {
    const result = await resumeApi.exportResume(exportResumeId.value, exportForm)
    lastExportTime.value = new Date().toLocaleString()
    exportDialogVisible.value = false
    ElMessage.success('导出成功，正在为你打开文件')
    if (result?.downloadUrl) {
      window.open(result.downloadUrl, '_blank')
    }
  } catch (error) {
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exportLoading.value = false
  }
}

const handleShare = async (row) => {
  try {
    shareData.value = await resumeApi.createShareLink(row.id)
    shareDialogVisible.value = true
  } catch (error) {
    ElMessage.error('创建分享链接失败')
  }
}

const handleAnalytics = async (row) => {
  try {
    analyticsData.value = await resumeApi.getAnalytics(row.id)
    analyticsDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取统计信息失败')
  }
}

const handleDelete = async (row) => {
  try {
    await resumeApi.deleteResume(row.id)
    ElMessage.success('删除成功')
    fetchResumes()
  } catch (error) {
    ElMessage.error('删除失败，请稍后重试')
  }
}

const statusTagType = (status) => {
  return status === 1 ? 'success' : 'info'
}

const formatStatus = (status) => {
  return status === 1 ? '已发布' : '草稿'
}

onMounted(() => {
  fetchResumes()
})
</script>

<style scoped>
.resume-manage-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.header-card {
  margin-bottom: 24px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-top h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2d3d;
}

.sub-title {
  margin: 4px 0 0;
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.stat-item {
  padding: 16px;
  border-radius: 12px;
  background: #f7f9fc;
}

.stat-item .label {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.stat-item .value {
  margin: 6px 0 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2d3d;
}

.table-tools {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.resume-table .name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.resume-table .name {
  font-weight: 600;
  color: #1d2129;
}

.resume-table .summary {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 16px;
  text-align: right;
}

.preview-summary {
  color: #606266;
  margin-bottom: 16px;
}

.metric-card {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 10px;
  text-align: center;
}

.metric-label {
  margin: 0;
  color: #909399;
}

.metric-value {
  margin: 8px 0 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2d3d;
}

.metric-footer {
  margin-top: 16px;
  color: #909399;
  text-align: center;
}

@media (max-width: 768px) {
  .header-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .table-tools {
    flex-direction: column;
  }
  
  .table-tools > * {
    width: 100%;
  }
}
</style>
