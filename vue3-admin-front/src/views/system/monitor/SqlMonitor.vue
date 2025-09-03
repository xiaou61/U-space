<template>
  <div class="sql-monitor">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>SQL监控</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              icon="Refresh" 
              @click="refreshCurrentTab"
              :loading="loading"
            >
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- SQL日志监控 -->
        <el-tab-pane label="SQL日志" name="logs">
          <SqlLogPanel ref="sqlLogPanelRef" />
        </el-tab-pane>

        <!-- 慢SQL监控 -->
        <el-tab-pane label="慢SQL监控" name="slowSql">
          <SlowSqlPanel ref="slowSqlPanelRef" />
        </el-tab-pane>

        <!-- 统计信息 -->
        <el-tab-pane label="统计信息" name="statistics">
          <StatisticsPanel ref="statisticsPanelRef" />
        </el-tab-pane>

        <!-- 实时监控 -->
        <el-tab-pane label="实时监控" name="realtime">
          <RealtimePanel ref="realtimePanelRef" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import SqlLogPanel from './components/SqlLogPanel.vue'
import SlowSqlPanel from './components/SlowSqlPanel.vue'
import StatisticsPanel from './components/StatisticsPanel.vue'
import RealtimePanel from './components/RealtimePanel.vue'

// 响应式数据
const activeTab = ref('logs')
const loading = ref(false)

// 子组件引用
const sqlLogPanelRef = ref(null)
const slowSqlPanelRef = ref(null)
const statisticsPanelRef = ref(null)
const realtimePanelRef = ref(null)

// 刷新当前标签页
const refreshCurrentTab = async () => {
  loading.value = true
  try {
    await nextTick()
    
    switch (activeTab.value) {
      case 'logs':
        await sqlLogPanelRef.value?.refresh()
        break
      case 'slowSql':
        await slowSqlPanelRef.value?.refresh()
        break
      case 'statistics':
        await statisticsPanelRef.value?.refresh()
        break
      case 'realtime':
        await realtimePanelRef.value?.refresh()
        break
    }
    
    ElMessage.success('刷新成功')
  } catch (error) {
    console.error('刷新失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.sql-monitor {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

:deep(.el-tabs--border-card) {
  border: none;
  box-shadow: none;
}

:deep(.el-tabs__header) {
  margin: 0 0 15px 0;
}

:deep(.el-tab-pane) {
  padding: 0;
}
</style> 