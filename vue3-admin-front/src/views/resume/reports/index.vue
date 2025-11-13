<template>
  <div class="resume-report-page">
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div>
          <h2>健康巡检</h2>
          <p>聚焦异常简历，提前发现数据问题</p>
        </div>
        <el-button type="primary" @click="fetchReports">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="reports" v-loading="loading" border row-key="resumeId">
        <el-table-column prop="resumeId" label="简历ID" width="100" />
        <el-table-column prop="resumeName" label="简历名称" min-width="200" />
        <el-table-column prop="issue" label="问题描述" min-width="260" show-overflow-tooltip />
        <el-table-column label="严重级别" width="140">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)">
              {{ severityLabel(row.severity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detectedAt" label="检测时间" width="180" />
        <el-table-column label="快捷操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="gotoResume(row.resumeId)">查看简历</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { resumeApi } from '@/api/resume'

const router = useRouter()
const loading = ref(false)
const reports = ref([])

const fetchReports = async () => {
  loading.value = true
  try {
    reports.value = await resumeApi.getHealthReports() || []
  } catch (error) {
    ElMessage.error('获取巡检数据失败')
  } finally {
    loading.value = false
  }
}

const severityLabel = (level) => {
  return {
    2: '高',
    1: '中',
    0: '低'
  }[level] || '低'
}

const severityType = (level) => {
  return {
    2: 'danger',
    1: 'warning',
    0: 'info'
  }[level] || 'info'
}

const gotoResume = (resumeId) => {
  router.push({
    path: '/resume',
    query: { focusId: resumeId }
  })
}

onMounted(() => {
  fetchReports()
})
</script>

<style scoped>
.resume-report-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.header-card {
  margin-bottom: 16px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
