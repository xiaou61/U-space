<template>
  <div class="moments-list">
    <el-card>
      <!-- 搜索筛选栏 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="filterForm.userNickname"
              placeholder="请输入用户昵称"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
              <el-option label="全部" value="" />
              <el-option label="正常" :value="1" />
              <el-option label="删除" :value="0" />
              <el-option label="审核中" :value="2" />
            </el-select>
          </el-col>
          <el-col :span="5">
            <el-date-picker
              v-model="filterForm.createTime"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 操作栏 -->
      <div class="operation-section">
        <el-button
          type="danger"
          :disabled="!selectedIds.length"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除 ({{ selectedIds.length }})
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 动态列表 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userNickname" label="用户" width="120" />
        <el-table-column label="动态内容" min-width="200">
          <template #default="{ row }">
            <div class="moment-content">
              <p class="content-text">{{ row.content }}</p>
              <div v-if="row.images && row.images.length" class="images-preview">
                <el-image
                  v-for="(image, index) in row.images.slice(0, 3)"
                  :key="index"
                  :src="image"
                  :preview-src-list="row.images"
                  :initial-index="index"
                  class="preview-image"
                  fit="cover"
                />
                <span v-if="row.images.length > 3" class="more-images">
                  +{{ row.images.length - 3 }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞数" width="80" align="center" />
        <el-table-column prop="commentCount" label="评论数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              text
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
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Search, Delete, Refresh } from '@element-plus/icons-vue'
import { getAdminMomentList, batchDeleteMoments } from '@/api/moment'

// 数据状态
const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])

// 筛选表单
const filterForm = reactive({
  userNickname: '',
  status: '',
  createTime: null
})

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 状态映射
const getStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    0: '已删除',
    1: '正常',
    2: '审核中'
  }
  return map[status] || '未知'
}

// 加载列表数据
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      userNickname: filterForm.userNickname || undefined,
      status: filterForm.status !== '' ? filterForm.status : undefined,
      startDate: filterForm.createTime?.[0] || undefined,
      endDate: filterForm.createTime?.[1] || undefined
    }

    const result = await getAdminMomentList(params)
    tableData.value = result.records || []
    pagination.total = result.total
  } catch (error) {
    ElMessage.error('加载列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadList()
}

// 刷新
const handleRefresh = () => {
  Object.assign(filterForm, {
    userNickname: '',
    status: '',
    createTime: null
  })
  pagination.currentPage = 1
  loadList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 删除单个动态
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.userNickname}"的这条动态吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await batchDeleteMoments([row.id])
    ElMessage.success('删除成功')
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量删除选中的 ${selectedIds.value.length} 条动态吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await batchDeleteMoments(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败：' + error.message)
    }
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.moments-list {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.operation-section {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.moment-content {
  max-width: 100%;
}

.content-text {
  margin: 0 0 10px 0;
  line-height: 1.5;
  word-wrap: break-word;
}

.images-preview {
  display: flex;
  gap: 5px;
  align-items: center;
}

.preview-image {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
}

.more-images {
  color: #999;
  font-size: 12px;
  margin-left: 5px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-table .el-table__cell) {
  padding: 12px 0;
}
</style> 