<template>
  <div class="system-monitor">
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="title-section">
          <h2>系统监控</h2>
          <p>实时监控系统运行状态和性能数据</p>
        </div>
        <div class="status-section">
          <el-tag type="success" size="large">
            <el-icon><CircleCheckFilled /></el-icon>
            系统运行正常
          </el-tag>
        </div>
      </div>
    </el-card>

    <div class="monitor-grid">
      <!-- SQL监控卡片 -->
      <el-card class="monitor-card sql-monitor" shadow="hover" @click="goToSqlMonitor">
        <div class="card-content">
          <div class="card-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="card-info">
            <h3>SQL监控</h3>
            <p>监控SQL执行性能、慢查询和错误统计</p>
            <div class="card-stats">
              <div class="stat-item">
                <span class="stat-label">今日SQL执行：</span>
                <span class="stat-value">{{ sqlStats.todayCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">慢SQL数量：</span>
                <span class="stat-value slow">{{ sqlStats.slowCount }}</span>
              </div>
            </div>
          </div>
          <div class="card-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </el-card>

      <!-- 系统性能监控卡片 (预留) -->
      <el-card class="monitor-card performance-monitor disabled" shadow="hover">
        <div class="card-content">
          <div class="card-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="card-info">
            <h3>性能监控</h3>
            <p>监控系统CPU、内存、磁盘等性能指标</p>
            <div class="card-stats">
              <div class="stat-item">
                <span class="stat-label">CPU使用率：</span>
                <span class="stat-value">65%</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">内存使用率：</span>
                <span class="stat-value">42%</span>
              </div>
            </div>
            <el-tag type="info" size="small" style="margin-top: 10px">即将推出</el-tag>
          </div>
          <div class="card-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </el-card>

      <!-- 接口监控卡片 (预留) -->
      <el-card class="monitor-card api-monitor disabled" shadow="hover">
        <div class="card-content">
          <div class="card-icon">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="card-info">
            <h3>接口监控</h3>
            <p>监控API接口响应时间、成功率和异常</p>
            <div class="card-stats">
              <div class="stat-item">
                <span class="stat-label">今日请求：</span>
                <span class="stat-value">12,456</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">成功率：</span>
                <span class="stat-value success">99.8%</span>
              </div>
            </div>
            <el-tag type="info" size="small" style="margin-top: 10px">即将推出</el-tag>
          </div>
          <div class="card-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </el-card>

      <!-- 异常监控卡片 (预留) -->
      <el-card class="monitor-card error-monitor disabled" shadow="hover">
        <div class="card-content">
          <div class="card-icon">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="card-info">
            <h3>异常监控</h3>
            <p>监控系统异常、错误日志和告警信息</p>
            <div class="card-stats">
              <div class="stat-item">
                <span class="stat-label">今日异常：</span>
                <span class="stat-value error">23</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">待处理：</span>
                <span class="stat-value warning">5</span>
              </div>
            </div>
            <el-tag type="info" size="small" style="margin-top: 10px">即将推出</el-tag>
          </div>
          <div class="card-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 快速访问 -->
    <el-card class="quick-access-card" shadow="never">
      <template #header>
        <span>快速访问</span>
      </template>
      
      <div class="quick-buttons">
        <el-button type="primary" @click="goToSqlMonitor">
          <el-icon><DataAnalysis /></el-icon>
          SQL监控
        </el-button>
        
        <el-button type="success" disabled>
          <el-icon><Monitor /></el-icon>
          性能监控
        </el-button>
        
        <el-button type="warning" disabled>
          <el-icon><Connection /></el-icon>
          接口监控
        </el-button>
        
        <el-button type="danger" disabled>
          <el-icon><Warning /></el-icon>
          异常监控
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  CircleCheckFilled, DataAnalysis, Monitor, Connection, 
  Warning, ArrowRight 
} from '@element-plus/icons-vue'
import { monitorApi } from '@/api/monitor'

const router = useRouter()

// SQL监控统计数据
const sqlStats = ref({
  todayCount: 0,
  slowCount: 0
})

// 获取SQL监控统计
const fetchSqlStats = async () => {
  try {
    // 获取今天的统计数据
    const today = new Date().toISOString().slice(0, 10)
    const { data } = await monitorApi.getStatistics({
      startDate: today,
      endDate: today
    })
    
    if (data && data.length > 0) {
      sqlStats.value.todayCount = data[0].totalCount || 0
      sqlStats.value.slowCount = data[0].slowSqlCount || 0
    }
  } catch (error) {
    console.error('获取SQL统计失败:', error)
    ElMessage.error('获取SQL统计数据失败')
    // 初始化为0
    sqlStats.value = {
      todayCount: 0,
      slowCount: 0
    }
  }
}

// 跳转到SQL监控
const goToSqlMonitor = () => {
  router.push('/system/monitor/sql')
}

// 组件挂载后获取数据
onMounted(() => {
  fetchSqlStats()
})
</script>

<style scoped>
.system-monitor {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header-card {
  margin-bottom: 30px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 28px;
}

.title-section p {
  margin: 0;
  color: #909399;
  font-size: 16px;
}

.monitor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.monitor-card {
  transition: all 0.3s ease;
  cursor: pointer;
  height: 180px;
}

.monitor-card:not(.disabled):hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.monitor-card.disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 20px;
}

.card-icon {
  font-size: 48px;
  color: #409EFF;
  flex-shrink: 0;
}

.sql-monitor .card-icon {
  color: #409EFF;
}

.performance-monitor .card-icon {
  color: #67C23A;
}

.api-monitor .card-icon {
  color: #E6A23C;
}

.error-monitor .card-icon {
  color: #F56C6C;
}

.card-info {
  flex: 1;
}

.card-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.card-info p {
  margin: 0 0 15px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.card-stats {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-item {
  font-size: 13px;
}

.stat-label {
  color: #909399;
}

.stat-value {
  font-weight: bold;
  color: #303133;
}

.stat-value.slow {
  color: #F56C6C;
}

.stat-value.success {
  color: #67C23A;
}

.stat-value.error {
  color: #F56C6C;
}

.stat-value.warning {
  color: #E6A23C;
}

.card-arrow {
  font-size: 24px;
  color: #C0C4CC;
  flex-shrink: 0;
}

.monitor-card:not(.disabled):hover .card-arrow {
  color: #409EFF;
  transform: translateX(4px);
}

.quick-access-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.quick-access-card :deep(.el-card__header) {
  background: transparent;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: bold;
}

.quick-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.quick-buttons .el-button {
  border-radius: 20px;
  padding: 12px 24px;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .monitor-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-buttons {
    justify-content: center;
  }
}
</style> 