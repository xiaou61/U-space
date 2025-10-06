<template>
  <div class="sensitive-version-page">
    <!-- 最新版本信息 -->
    <div class="latest-version">
      <el-card shadow="never">
        <div class="version-info">
          <el-icon><InfoFilled /></el-icon>
          <span>当前版本：</span>
          <el-tag type="success" size="large">{{ latestVersion }}</el-tag>
        </div>
      </el-card>
    </div>

    <!-- 版本历史列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="versionNo" label="版本号" width="150" />
        <el-table-column label="变更类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getChangeTypeTagType(row.changeType)" size="small">
              {{ getChangeTypeText(row.changeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeCount" label="变更数量" width="120" align="right" />
        <el-table-column prop="changeDetail" label="变更详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleView(row)"
            >
              详情
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="handleRollback(row)"
            >
              回滚
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="版本详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="版本号">
          {{ versionDetail.versionNo }}
        </el-descriptions-item>
        <el-descriptions-item label="变更类型">
          <el-tag :type="getChangeTypeTagType(versionDetail.changeType)" size="small">
            {{ getChangeTypeText(versionDetail.changeType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="变更数量">
          {{ versionDetail.changeCount }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ versionDetail.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="变更详情" :span="2">
          <div class="change-detail">
            {{ versionDetail.changeDetail }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ versionDetail.remark || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import {
  listVersions,
  getVersionById,
  rollbackVersion,
  getLatestVersion
} from '@/api/sensitive'

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const latestVersion = ref('v1.0.0')
const versionDetail = ref({})

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10
})

// 方法
const handleQuery = async () => {
  loading.value = true
  try {
    const response = await listVersions(queryForm)
    tableData.value = response.records
    total.value = response.total
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const loadLatestVersion = async () => {
  try {
    const version = await getLatestVersion()
    latestVersion.value = version || 'v1.0.0'
  } catch (error) {
    console.error('获取最新版本失败', error)
  }
}

const handleView = async (row) => {
  try {
    const detail = await getVersionById(row.id)
    versionDetail.value = detail
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取版本详情失败')
  }
}

const handleRollback = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要回滚到版本 ${row.versionNo} 吗？此操作将恢复到该版本的词库状态。`,
      '回滚确认',
      {
        type: 'warning',
        confirmButtonText: '确定回滚',
        cancelButtonText: '取消'
      }
    )
    
    await rollbackVersion(row.id, { operatorId: 1 })
    ElMessage.success('回滚成功')
    handleQuery()
    loadLatestVersion()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('回滚失败')
    }
  }
}

// 工具方法
const getChangeTypeText = (type) => {
  const typeMap = {
    add: '新增',
    update: '更新',
    delete: '删除',
    import: '导入'
  }
  return typeMap[type] || '未知'
}

const getChangeTypeTagType = (type) => {
  const typeMap = {
    add: 'success',
    update: 'warning',
    delete: 'danger',
    import: 'info'
  }
  return typeMap[type] || 'info'
}

// 生命周期
onMounted(() => {
  handleQuery()
  loadLatestVersion()
})
</script>

<style scoped>
.sensitive-version-page {
  padding: 20px;
}

.latest-version {
  margin-bottom: 16px;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
}

.version-info .el-icon {
  font-size: 20px;
  color: #409EFF;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.change-detail {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
