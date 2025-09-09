<template>
  <div class="moment-comments">
    <el-card>
      <!-- 搜索筛选栏 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="filterForm.userNickname"
              placeholder="请输入评论用户昵称"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="filterForm.momentId"
              placeholder="请输入动态ID"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Document /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="filterForm.contentKeyword"
              placeholder="请输入评论内容关键词"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="6">
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
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 评论列表 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        row-key="id"
      >
        <el-table-column prop="id" label="评论ID" width="100" />
        <el-table-column prop="momentId" label="动态ID" width="100" />
        <el-table-column prop="userNickname" label="评论用户" width="120" />
        <el-table-column label="评论内容" min-width="250">
          <template #default="{ row }">
            <div class="comment-content">
              {{ row.content }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属动态" min-width="200">
          <template #default="{ row }">
            <div class="moment-preview" v-if="row.momentContentSummary">
              <p class="moment-text">{{ row.momentContentSummary }}</p>
            </div>
            <span v-else class="no-data">动态已删除</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="180" />
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
import { User, Document, Search, Refresh } from '@element-plus/icons-vue'
import { getAdminCommentList, deleteComment } from '@/api/moment'

// 数据状态
const loading = ref(false)
const tableData = ref([])

// 筛选表单
const filterForm = reactive({
  userNickname: '',
  momentId: '',
  contentKeyword: '',
  createTime: null
})

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 加载列表数据
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      userNickname: filterForm.userNickname || undefined,
      momentId: filterForm.momentId || undefined,
      contentKeyword: filterForm.contentKeyword || undefined,
      startDate: filterForm.createTime?.[0] || undefined,
      endDate: filterForm.createTime?.[1] || undefined
    }

    const result = await getAdminCommentList(params)
    tableData.value = result.records || []
    pagination.total = result.total || 0
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
    momentId: '',
    contentKeyword: '',
    createTime: null
  })
  pagination.currentPage = 1
  loadList()
}

// 删除评论
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.userNickname}"的这条评论吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await deleteComment(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.moment-comments {
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

.comment-content {
  line-height: 1.5;
  word-wrap: break-word;
  max-width: 100%;
}

.moment-preview {
  font-size: 12px;
  color: #666;
}

.moment-text {
  margin: 0 0 5px 0;
  line-height: 1.4;
}

.moment-author {
  margin: 0;
  color: #999;
}

.no-data {
  color: #ccc;
  font-style: italic;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-table .el-table__cell) {
  padding: 12px 0;
}
</style> 