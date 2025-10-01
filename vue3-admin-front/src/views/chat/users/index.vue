<template>
  <div class="online-users-management">
    <el-card>
      <!-- 头部信息 -->
      <div class="header-info">
        <el-statistic title="当前在线人数" :value="onlineCount">
          <template #prefix>
            <el-icon color="#67c23a"><User /></el-icon>
          </template>
        </el-statistic>
        <el-button type="primary" @click="handleRefresh" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 在线用户列表 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        row-key="userId"
        style="margin-top: 20px;"
      >
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="userNickname" label="用户昵称" width="150" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.userAvatar">
              {{ row.userNickname?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
        <el-table-column prop="deviceInfo" label="设备信息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="connectTime" label="连接时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="warning"
              size="small"
              text
              @click="handleBanUser(row)"
            >
              禁言
            </el-button>
            <el-button
              type="danger"
              size="small"
              text
              @click="handleKickUser(row)"
            >
              踢出
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="!loading && tableData.length === 0" description="暂无在线用户" />
    </el-card>

    <!-- 禁言对话框 -->
    <el-dialog v-model="banDialogVisible" title="禁言用户" width="500px">
      <el-form :model="banForm" label-width="100px">
        <el-form-item label="用户昵称">
          <el-input :value="currentUser?.userNickname" disabled />
        </el-form-item>
        <el-form-item label="禁言时长">
          <el-radio-group v-model="banForm.banDuration">
            <el-radio :label="3600">1小时</el-radio>
            <el-radio :label="86400">1天</el-radio>
            <el-radio :label="604800">7天</el-radio>
            <el-radio :label="0">永久</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="禁言原因">
          <el-input
            v-model="banForm.banReason"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入禁言原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="banning" @click="confirmBan">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Refresh } from '@element-plus/icons-vue'
import { getAdminOnlineUsers, kickUser, banUser } from '@/api/chat'

// 状态
const loading = ref(false)
const banning = ref(false)
const tableData = ref([])
const banDialogVisible = ref(false)
const currentUser = ref(null)

// 禁言表单
const banForm = reactive({
  userId: null,
  banDuration: 3600,
  banReason: ''
})

// 在线人数
const onlineCount = computed(() => tableData.value.length)

// 初始化
onMounted(() => {
  loadData()
  // 每30秒自动刷新
  setInterval(() => {
    loadData()
  }, 30000)
})

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const res = await getAdminOnlineUsers()
    tableData.value = res || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新
const handleRefresh = () => {
  loadData()
}

// 禁言用户
const handleBanUser = (row) => {
  currentUser.value = row
  banForm.userId = row.userId
  banForm.banDuration = 3600
  banForm.banReason = ''
  banDialogVisible.value = true
}

// 确认禁言
const confirmBan = async () => {
  if (!banForm.banReason.trim()) {
    ElMessage.warning('请输入禁言原因')
    return
  }
  
  try {
    banning.value = true
    await banUser({
      userId: banForm.userId,
      banDuration: banForm.banDuration,
      banReason: banForm.banReason
    })
    ElMessage.success('禁言成功')
    banDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '禁言失败')
  } finally {
    banning.value = false
  }
}

// 踢出用户
const handleKickUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要踢出用户 "${row.userNickname}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await kickUser(row.userId)
    ElMessage.success('已踢出用户')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '踢出失败')
    }
  }
}
</script>

<style scoped lang="scss">
.online-users-management {
  padding: 20px;
}

.header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}
</style>
