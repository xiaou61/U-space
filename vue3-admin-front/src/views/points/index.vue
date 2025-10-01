<template>
  <div class="points-management">
    <!-- 页面标题和统计信息 -->
    <div class="page-header">
      <div class="header-title">
        <h2>积分管理</h2>
        <p>管理系统积分发放和用户积分数据</p>
      </div>
      <div class="header-stats" v-if="statistics">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-number">{{ formatNumber(statistics.totalPointsIssued) }}</div>
                <div class="stat-label">总发放积分</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-number active">{{ formatNumber(statistics.checkinPointsSum) }}</div>
                <div class="stat-label">打卡积分</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-number warning">{{ formatNumber(statistics.adminGrantPointsSum) }}</div>
                <div class="stat-label">后台发放</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-number">{{ statistics.activeUserCount || 0 }}</div>
                <div class="stat-label">活跃用户</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 快捷操作区域 -->
    <el-row :gutter="16" class="action-cards">
      <el-col :span="6">
        <el-card class="action-card" @click="$router.push('/points/grant')">
          <div class="action-item">
            <el-icon class="action-icon"><Plus /></el-icon>
            <div class="action-title">发放积分</div>
            <div class="action-desc">为指定用户发放积分奖励</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="action-card" @click="$router.push('/points/users')">
          <div class="action-item">
            <el-icon class="action-icon"><Trophy /></el-icon>
            <div class="action-title">积分排行</div>
            <div class="action-desc">查看用户积分排行榜</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="action-card" @click="$router.push('/points/details')">
          <div class="action-item">
            <el-icon class="action-icon"><Document /></el-icon>
            <div class="action-title">积分明细</div>
            <div class="action-desc">查看所有积分变动记录</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="action-card" @click="showBatchGrantDialog = true">
          <div class="action-item">
            <el-icon class="action-icon"><UserFilled /></el-icon>
            <div class="action-title">批量发放</div>
            <div class="action-desc">批量为多个用户发放积分</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 积分排行榜 -->
    <el-card class="ranking-card" v-if="statistics && statistics.userRankings">
      <template #header>
        <div class="card-header">
          <span>积分排行榜 TOP 10</span>
          <el-button text @click="$router.push('/points/users')">查看全部</el-button>
        </div>
      </template>
      
      <div class="ranking-list">
        <div 
          v-for="(user, index) in statistics.userRankings.slice(0, 10)" 
          :key="user.userId"
          class="ranking-item"
          :class="{ 'top-three': index < 3 }"
        >
          <div class="rank-number" :class="`rank-${index + 1}`">
            {{ index + 1 }}
          </div>
          <div class="user-info">
            <div class="user-name">{{ user.userName }}</div>
            <div class="user-days">连续{{ user.continuousDays }}天</div>
          </div>
          <div class="points-info">
            <div class="points">{{ formatNumber(user.totalPoints) }}积分</div>
            <div class="yuan">≈{{ user.balanceYuan }}元</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 批量发放积分对话框 -->
    <el-dialog
      v-model="showBatchGrantDialog"
      title="批量发放积分"
      width="500px"
    >
      <el-form :model="batchGrantForm" :rules="batchGrantRules" ref="batchGrantFormRef" label-width="100px">
        <el-form-item label="用户ID" prop="userIds">
          <el-input
            v-model="batchGrantForm.userIdsText"
            type="textarea"
            :rows="3"
            placeholder="请输入用户ID，多个用户用英文逗号分隔，例如：123,456,789"
          />
          <div class="form-tip">请输入要发放积分的用户ID，用英文逗号分隔</div>
        </el-form-item>
        <el-form-item label="积分数量" prop="points">
          <el-input-number
            v-model="batchGrantForm.points"
            :min="1"
            :max="10000"
            controls-position="right"
            placeholder="请输入积分数量"
          />
        </el-form-item>
        <el-form-item label="发放原因" prop="reason">
          <el-input
            v-model="batchGrantForm.reason"
            placeholder="请输入发放原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showBatchGrantDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBatchGrant" :loading="batchGrantLoading">
          确认发放
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Trophy, Document, UserFilled } from '@element-plus/icons-vue'
import { pointsApi } from '@/api/points'

// 响应式数据
const statistics = ref(null)
const showBatchGrantDialog = ref(false)
const batchGrantLoading = ref(false)

// 批量发放表单
const batchGrantForm = reactive({
  userIdsText: '',
  points: null,
  reason: ''
})

const batchGrantFormRef = ref(null)

// 表单验证规则
const batchGrantRules = {
  userIdsText: [
    { required: true, message: '请输入用户ID', trigger: 'blur' }
  ],
  points: [
    { required: true, message: '请输入积分数量', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '积分数量必须在1-10000之间', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入发放原因', trigger: 'blur' },
    { max: 200, message: '发放原因不能超过200个字符', trigger: 'blur' }
  ]
}

// 格式化数字显示
const formatNumber = (num) => {
  if (!num) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const data = await pointsApi.getPointsStatistics()
    statistics.value = data
  } catch (error) {
    console.error('加载积分统计数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 处理批量发放积分
const handleBatchGrant = async () => {
  if (!batchGrantFormRef.value) return
  
  try {
    await batchGrantFormRef.value.validate()
    
    // 解析用户ID
    const userIds = batchGrantForm.userIdsText
      .split(',')
      .map(id => parseInt(id.trim()))
      .filter(id => !isNaN(id))
    
    if (userIds.length === 0) {
      ElMessage.warning('请输入有效的用户ID')
      return
    }
    
    await ElMessageBox.confirm(
      `确认为${userIds.length}个用户发放${batchGrantForm.points}积分吗？`,
      '确认发放',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    batchGrantLoading.value = true
    
    const requestData = {
      userIds: userIds,
      points: batchGrantForm.points,
      reason: batchGrantForm.reason
    }
    
    const result = await pointsApi.batchGrantPoints(requestData)
    
    ElMessage.success(
      `批量发放完成！成功：${result.successCount}人，失败：${result.failCount}人`
    )
    
    // 重置表单并关闭对话框
    batchGrantFormRef.value.resetFields()
    Object.assign(batchGrantForm, {
      userIdsText: '',
      points: null,
      reason: ''
    })
    showBatchGrantDialog.value = false
    
    // 重新加载统计数据
    await loadStatistics()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量发放积分失败:', error)
      ElMessage.error(error.message || '批量发放积分失败')
    }
  } finally {
    batchGrantLoading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.points-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.header-title h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-title p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-stats {
  margin-top: 20px;
}

.stat-card {
  text-align: center;
  cursor: default;
}

.stat-item {
  padding: 10px 0;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-number.active {
  color: #67C23A;
}

.stat-number.warning {
  color: #E6A23C;
}

.stat-number.disabled {
  color: #F56C6C;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.action-cards {
  margin: 20px 0;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
}

.action-item {
  text-align: center;
  padding: 20px 10px;
}

.action-icon {
  font-size: 32px;
  color: #409EFF;
  margin-bottom: 12px;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.action-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.ranking-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ranking-list {
  max-height: 400px;
  overflow-y: auto;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.ranking-item:last-child {
  border-bottom: none;
}

.ranking-item.top-three .rank-number {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  margin-right: 12px;
}

.rank-number.rank-1 {
  background: linear-gradient(135deg, #FFD700, #FFA500);
}

.rank-number.rank-2 {
  background: linear-gradient(135deg, #C0C0C0, #A8A8A8);
}

.rank-number.rank-3 {
  background: linear-gradient(135deg, #CD7F32, #B8860B);
}

.user-info {
  flex: 1;
  margin-right: 12px;
}

.user-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.user-days {
  font-size: 12px;
  color: #67C23A;
}

.points-info {
  text-align: right;
}

.points {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.yuan {
  font-size: 12px;
  color: #909399;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
