<template>
  <div class="chat-messages-management">
    <el-card>
      <!-- 搜索筛选栏 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="filterForm.userId"
              placeholder="请输入用户ID"
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
              v-model="filterForm.keyword"
              placeholder="搜索消息内容"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="8">
            <el-date-picker
              v-model="filterForm.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
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
        <el-button type="warning" @click="showAnnouncementDialog">
          <el-icon><Bell /></el-icon>
          发送公告
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 消息列表 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="消息ID" width="100" />
        <el-table-column prop="userNickname" label="发送者" width="120" />
        <el-table-column label="消息类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getMessageTypeTag(row.messageType)">
              {{ getMessageTypeText(row.messageType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="消息内容" min-width="300">
          <template #default="{ row }">
            <div class="message-content">
              <!-- 文本消息 -->
              <p v-if="row.messageType === 1" class="text-message">{{ row.content }}</p>
              <!-- 图片消息 -->
              <el-image 
                v-else-if="row.messageType === 2"
                :src="row.imageUrl"
                :preview-src-list="[row.imageUrl]"
                class="preview-image"
                fit="cover"
              />
              <!-- 系统消息 -->
              <p v-else-if="row.messageType === 3" class="system-message">{{ row.content }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="deviceInfo" label="设备信息" width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发送时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
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
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 发送公告对话框 -->
    <el-dialog v-model="announcementVisible" title="发送系统公告" width="500px">
      <el-form :model="announcementForm" label-width="80px">
        <el-form-item label="公告内容">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="5"
            maxlength="200"
            show-word-limit
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="announcementVisible = false">取消</el-button>
        <el-button type="primary" :loading="sendingAnnouncement" @click="handleSendAnnouncement">
          发送
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Search, Delete, Refresh, Bell } from '@element-plus/icons-vue'
import { getAdminMessageList, deleteMessage, batchDeleteMessages, sendAnnouncement } from '@/api/chat'

// 筛选表单
const filterForm = reactive({
  userId: null,
  keyword: '',
  timeRange: [],
  startTime: '',
  endTime: ''
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 状态
const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])
const announcementVisible = ref(false)
const sendingAnnouncement = ref(false)
const announcementForm = reactive({
  content: ''
})

// 初始化
onMounted(() => {
  loadData()
})

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    
    // 处理时间范围
    if (filterForm.timeRange && filterForm.timeRange.length === 2) {
      filterForm.startTime = filterForm.timeRange[0]
      filterForm.endTime = filterForm.timeRange[1]
    } else {
      filterForm.startTime = ''
      filterForm.endTime = ''
    }
    
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      userId: filterForm.userId || null,
      keyword: filterForm.keyword || null,
      startTime: filterForm.startTime || null,
      endTime: filterForm.endTime || null
    }
    
    const res = await getAdminMessageList(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

// 刷新
const handleRefresh = () => {
  filterForm.userId = null
  filterForm.keyword = ''
  filterForm.timeRange = []
  pagination.pageNum = 1
  loadData()
}

// 选择改变
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteMessage(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条消息吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await batchDeleteMessages(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除失败')
    }
  }
}

// 显示公告对话框
const showAnnouncementDialog = () => {
  announcementForm.content = ''
  announcementVisible.value = true
}

// 发送公告
const handleSendAnnouncement = async () => {
  if (!announcementForm.content.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }
  
  try {
    sendingAnnouncement.value = true
    await sendAnnouncement({ content: announcementForm.content })
    ElMessage.success('公告发送成功')
    announcementVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '发送公告失败')
  } finally {
    sendingAnnouncement.value = false
  }
}

// 分页改变
const handlePageChange = () => {
  loadData()
}

const handleSizeChange = () => {
  pagination.pageNum = 1
  loadData()
}

// 获取消息类型标签
const getMessageTypeTag = (type) => {
  const map = {
    1: '',
    2: 'success',
    3: 'warning'
  }
  return map[type] || ''
}

// 获取消息类型文本
const getMessageTypeText = (type) => {
  const map = {
    1: '文本',
    2: '图片',
    3: '系统'
  }
  return map[type] || '未知'
}
</script>

<style scoped lang="scss">
.chat-messages-management {
  padding: 20px;
}

.filter-section {
  margin-bottom: 16px;
}

.operation-section {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.message-content {
  .text-message {
    margin: 0;
    word-break: break-word;
    white-space: pre-wrap;
  }
  
  .system-message {
    margin: 0;
    color: #e6a23c;
    font-weight: 500;
  }
  
  .preview-image {
    width: 100px;
    height: 100px;
    border-radius: 4px;
    cursor: pointer;
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
