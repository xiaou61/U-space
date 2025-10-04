<template>
  <div class="risk-users">
    <el-card shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="风险等级">
          <el-select v-model="queryForm.riskLevel" placeholder="请选择" clearable>
            <el-option label="正常" :value="0" />
            <el-option label="低风险" :value="1" />
            <el-option label="中风险" :value="2" />
            <el-option label="高风险" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="黑名单">
          <el-select v-model="queryForm.isBlacklist" placeholder="请选择" clearable>
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRiskUsers">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="userList" stripe border v-loading="loading">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskLevelType(row.riskLevel)" size="small">
              {{ getRiskLevelName(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="todayDrawCount" label="今日抽奖" width="100" align="center" />
        <el-table-column prop="totalDrawCount" label="累计抽奖" width="100" align="center" />
        <el-table-column prop="todayWinCount" label="今日中奖" width="100" align="center" />
        <el-table-column prop="totalWinCount" label="累计中奖" width="100" align="center" />
        <el-table-column prop="continuousNoWin" label="连续未中奖" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'high-continuous': row.continuousNoWin >= 20 }">
              {{ row.continuousNoWin }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="黑名单" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isBlacklist === 1" type="danger" size="small">是</el-tag>
            <el-tag v-else type="success" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              size="small" 
              @click="handleEvaluateRisk(row)">
              评估风险
            </el-button>
            <el-button 
              link 
              type="warning" 
              size="small" 
              @click="handleDetectAbnormal(row)">
              检测异常
            </el-button>
            <el-button 
              link 
              type="danger" 
              size="small" 
              @click="handleResetLimit(row)">
              重置限制
            </el-button>
            <el-button 
              link 
              :type="row.isBlacklist === 1 ? 'success' : 'danger'" 
              size="small" 
              @click="handleToggleBlacklist(row)">
              {{ row.isBlacklist === 1 ? '移除黑名单' : '加入黑名单' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRiskUsers"
        @current-change="loadRiskUsers"
        style="margin-top: 20px; justify-content: flex-end" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { lotteryAdminApi } from '@/api/lotteryAdmin'

const loading = ref(false)
const userList = ref([])
const total = ref(0)

const queryForm = reactive({
  riskLevel: null,
  isBlacklist: null,
  minContinuousNoWin: null,
  page: 1,
  size: 20
})

const loadRiskUsers = async () => {
  loading.value = true
  try {
    const res = await lotteryAdminApi.getRiskUserList(queryForm)
    userList.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(queryForm, {
    riskLevel: null,
    isBlacklist: null,
    minContinuousNoWin: null,
    page: 1,
    size: 20
  })
  loadRiskUsers()
}

const handleEvaluateRisk = async (row) => {
  try {
    const riskLevel = await lotteryAdminApi.evaluateRiskLevel(row.userId)
    ElMessage.success(`评估完成，风险等级：${getRiskLevelName(riskLevel)}`)
    loadRiskUsers()
  } catch (error) {
    ElMessage.error(error.message || '评估失败')
  }
}

const handleDetectAbnormal = async (row) => {
  try {
    const hasAbnormal = await lotteryAdminApi.detectAbnormalBehavior(row.userId)
    if (hasAbnormal) {
      ElMessage.warning('检测到异常行为！')
    } else {
      ElMessage.success('行为正常')
    }
  } catch (error) {
    ElMessage.error(error.message || '检测失败')
  }
}

const handleResetLimit = async (row) => {
  try {
    await ElMessageBox.confirm('确认要重置该用户的抽奖限制吗？', '提示', {
      type: 'warning'
    })
    await lotteryAdminApi.resetUserLimit(row.userId)
    ElMessage.success('重置成功')
    loadRiskUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleToggleBlacklist = async (row) => {
  try {
    const isBlacklist = row.isBlacklist === 1 ? false : true
    await ElMessageBox.confirm(
      `确认要${isBlacklist ? '加入' : '移除'}黑名单吗？`,
      '提示',
      { type: 'warning' }
    )
    await lotteryAdminApi.setUserBlacklist(row.userId, isBlacklist)
    ElMessage.success(isBlacklist ? '已加入黑名单' : '已移除黑名单')
    loadRiskUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const getRiskLevelName = (level) => {
  const names = ['正常', '低风险', '中风险', '高风险']
  return names[level] || '未知'
}

const getRiskLevelType = (level) => {
  if (level === 0) return 'success'
  if (level === 1) return 'info'
  if (level === 2) return 'warning'
  return 'danger'
}

onMounted(() => {
  loadRiskUsers()
})
</script>

<style scoped lang="scss">
.risk-users {
  .high-continuous {
    color: #f56c6c;
    font-weight: bold;
  }
  
  :deep(.el-pagination) {
    display: flex;
  }
}
</style>

