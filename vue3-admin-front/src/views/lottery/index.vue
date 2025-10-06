<template>
  <div class="lottery-management">
    <el-card class="header-card">
      <div class="header-content">
        <h2>
          <el-icon style="margin-right: 8px"><TrophyBase /></el-icon>
          抽奖系统管理
        </h2>
        <div class="header-actions">
          <el-button 
            type="primary" 
            :icon="Refresh" 
            @click="refreshCache"
            :loading="cacheLoading">
            刷新缓存
          </el-button>
          <el-dropdown @command="handleEmergencyAction">
            <el-button type="danger">
              应急操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="circuit-break">
                  <el-icon><Warning /></el-icon>
                  手动熔断
                </el-dropdown-item>
                <el-dropdown-item command="resume">
                  <el-icon><Check /></el-icon>
                  恢复服务
                </el-dropdown-item>
                <el-dropdown-item command="enable-degradation">
                  <el-icon><Bottom /></el-icon>
                  启用降级
                </el-dropdown-item>
                <el-dropdown-item command="disable-degradation">
                  <el-icon><Top /></el-icon>
                  禁用降级
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" type="border-card" class="main-tabs">
      <el-tab-pane label="奖品配置" name="config">
        <prize-config ref="prizeConfigRef" />
      </el-tab-pane>
      
      <el-tab-pane label="实时监控" name="monitor">
        <realtime-monitor ref="realtimeMonitorRef" />
      </el-tab-pane>
      
      <el-tab-pane label="抽奖记录" name="records">
        <draw-records ref="drawRecordsRef" />
      </el-tab-pane>
      
      <el-tab-pane label="数据统计" name="statistics">
        <data-statistics ref="dataStatisticsRef" />
      </el-tab-pane>
      
      <el-tab-pane label="调整历史" name="history">
        <adjust-history ref="adjustHistoryRef" />
      </el-tab-pane>
      
      <el-tab-pane label="风险用户" name="risk">
        <risk-users ref="riskUsersRef" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, 
  ArrowDown,
  Warning,
  Check,
  Bottom,
  Top,
  TrophyBase
} from '@element-plus/icons-vue'
import { lotteryAdminApi } from '@/api/lotteryAdmin'

import PrizeConfig from './components/PrizeConfig.vue'
import RealtimeMonitor from './components/RealtimeMonitor.vue'
import DrawRecords from './components/DrawRecords.vue'
import DataStatistics from './components/DataStatistics.vue'
import AdjustHistory from './components/AdjustHistory.vue'
import RiskUsers from './components/RiskUsers.vue'

const activeTab = ref('config')
const cacheLoading = ref(false)

// 刷新缓存
const refreshCache = async () => {
  cacheLoading.value = true
  try {
    await lotteryAdminApi.refreshCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    ElMessage.error(error.message || '刷新缓存失败')
  } finally {
    cacheLoading.value = false
  }
}

// 应急操作
const handleEmergencyAction = async (command) => {
  try {
    if (command === 'circuit-break') {
      const { value } = await ElMessageBox.prompt('请输入熔断原因', '手动熔断', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '请输入熔断原因'
      })
      await lotteryAdminApi.manualCircuitBreak(value)
      ElMessage.success('熔断成功')
    } else if (command === 'resume') {
      await ElMessageBox.confirm('确认要恢复服务吗？', '恢复服务', {
        type: 'warning'
      })
      await lotteryAdminApi.resumeService()
      ElMessage.success('服务已恢复')
    } else if (command === 'enable-degradation') {
      await ElMessageBox.confirm('确认要启用降级模式吗？', '启用降级', {
        type: 'warning'
      })
      await lotteryAdminApi.enableDegradation()
      ElMessage.success('降级模式已启用')
    } else if (command === 'disable-degradation') {
      await lotteryAdminApi.disableDegradation()
      ElMessage.success('降级模式已禁用')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}
</script>

<style scoped lang="scss">
.lottery-management {
  padding: 20px;
  
  .header-card {
    margin-bottom: 20px;
    
    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h2 {
        margin: 0;
        display: flex;
        align-items: center;
        font-size: 24px;
        color: #303133;
      }
      
      .header-actions {
        display: flex;
        gap: 10px;
      }
    }
  }
  
  .main-tabs {
    :deep(.el-tabs__content) {
      padding: 20px;
    }
  }
}
</style>

