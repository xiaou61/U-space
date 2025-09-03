<template>
  <div class="statistics-panel">
    <!-- 统计筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" :inline="true" label-width="80px">
        <el-form-item label="统计日期">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 300px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="Search" @click="fetchStatistics">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 概览统计卡片 -->
    <div class="overview-stats">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ totalExecutions }}</div>
          <div class="stat-label">总执行次数</div>
          <div class="stat-trend">
            <el-icon><TrendCharts /></el-icon>
            今日: {{ todayExecutions }}
          </div>
        </div>
        <el-icon class="stat-icon total"><DataAnalysis /></el-icon>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ slowSqlCount }}</div>
          <div class="stat-label">慢SQL数量</div>
          <div class="stat-trend">
            <el-icon class="trend-down"><Bottom /></el-icon>
            占比: {{ slowSqlRatio }}%
          </div>
        </div>
        <el-icon class="stat-icon slow"><Warning /></el-icon>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ avgExecutionTime }}ms</div>
          <div class="stat-label">平均执行时间</div>
          <div class="stat-trend">
            <el-icon><Timer /></el-icon>
            最大: {{ maxExecutionTime }}ms
          </div>
        </div>
        <el-icon class="stat-icon avg"><Stopwatch /></el-icon>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ errorCount }}</div>
          <div class="stat-label">执行失败次数</div>
          <div class="stat-trend">
            <el-icon class="trend-down"><Bottom /></el-icon>
            成功率: {{ successRate }}%
          </div>
        </div>
        <el-icon class="stat-icon error"><CircleCloseFilled /></el-icon>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- SQL类型分布 -->
      <el-card class="chart-card" shadow="never">
        <template #header>
          <span>SQL类型分布</span>
        </template>
        <div ref="sqlTypeChartRef" class="chart-container"></div>
      </el-card>

      <!-- 执行时间趋势 -->
      <el-card class="chart-card" shadow="never">
        <template #header>
          <span>执行时间趋势</span>
        </template>
        <div ref="executionTrendChartRef" class="chart-container"></div>
      </el-card>
    </div>

    <!-- 热点SQL统计表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>热点SQL统计 (执行频次Top20)</span>
          <el-button size="small" @click="refreshFrequencyStats">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table 
        :data="frequencyStats" 
        :loading="frequencyLoading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column type="index" label="排名" width="60" align="center" />
        <el-table-column label="SQL模板" prop="sqlTemplate" width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="sql-preview">{{ formatSqlPreview(row.sqlTemplate) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="SQL类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getSqlTypeTagType(getSqlType(row.sqlTemplate))" size="small">
              {{ getSqlType(row.sqlTemplate) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executionCount" label="执行次数" width="100" align="center" sortable />
        <el-table-column prop="avgExecutionTime" label="平均时间(ms)" width="120" align="center" sortable>
          <template #default="{ row }">
            <span :class="getExecutionTimeClass(row.avgExecutionTime)">
              {{ row.avgExecutionTime }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="maxExecutionTime" label="最大时间(ms)" width="120" align="center" sortable>
          <template #default="{ row }">
            <span :class="getExecutionTimeClass(row.maxExecutionTime)">
              {{ row.maxExecutionTime }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="slowSqlCount" label="慢SQL次数" width="100" align="center" />
        <el-table-column prop="errorCount" label="错误次数" width="90" align="center" />
        <el-table-column label="最后执行时间" prop="lastExecuteTime" width="150" align="center">
          <template #default="{ row }">
            <span>{{ new Date(row.lastExecuteTime).toLocaleString() }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  TrendCharts, DataAnalysis, Warning, Timer, 
  Stopwatch, CircleCloseFilled, Bottom, Refresh 
} from '@element-plus/icons-vue'
import { monitorApi } from '@/api/monitor'
import * as echarts from 'echarts'

// 响应式数据
const statisticsData = ref([])
const frequencyStats = ref([])
const frequencyLoading = ref(false)

// Chart实例
const sqlTypeChartRef = ref(null)
const executionTrendChartRef = ref(null)
let sqlTypeChart = null
let executionTrendChart = null

// 查询表单
const queryForm = reactive({
  dateRange: null,
  startDate: null,
  endDate: null
})

// 计算统计数据
const totalExecutions = computed(() => {
  return statisticsData.value.reduce((sum, item) => sum + (item.executionCount || 0), 0)
})

const slowSqlCount = computed(() => {
  return statisticsData.value.reduce((sum, item) => sum + (item.slowSqlCount || 0), 0)
})

const errorCount = computed(() => {
  return statisticsData.value.reduce((sum, item) => sum + (item.errorCount || 0), 0)
})

const todayExecutions = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  const todayData = statisticsData.value.find(item => item.statisticsDate === today)
  return todayData ? todayData.executionCount : 0
})

const slowSqlRatio = computed(() => {
  if (totalExecutions.value === 0) return 0
  return ((slowSqlCount.value / totalExecutions.value) * 100).toFixed(1)
})

const successRate = computed(() => {
  if (totalExecutions.value === 0) return 100
  return (((totalExecutions.value - errorCount.value) / totalExecutions.value) * 100).toFixed(1)
})

const avgExecutionTime = computed(() => {
  if (frequencyStats.value.length === 0) return 0
  const total = frequencyStats.value.reduce((sum, item) => sum + (item.avgExecutionTime || 0), 0)
  return Math.round(total / frequencyStats.value.length)
})

const maxExecutionTime = computed(() => {
  if (frequencyStats.value.length === 0) return 0
  return Math.max(...frequencyStats.value.map(item => item.maxExecutionTime || 0))
})

// 获取SQL类型标签类型
const getSqlTypeTagType = (sqlType) => {
  const typeMap = {
    'SELECT': 'info',
    'INSERT': 'success',
    'UPDATE': 'warning',
    'DELETE': 'danger'
  }
  return typeMap[sqlType] || 'info'
}

// 从SQL模板推断SQL类型
const getSqlType = (sqlTemplate) => {
  if (!sqlTemplate) return 'UNKNOWN'
  const template = sqlTemplate.toUpperCase()
  if (template.includes('SELECT')) return 'SELECT'
  if (template.includes('INSERT')) return 'INSERT'  
  if (template.includes('UPDATE')) return 'UPDATE'
  if (template.includes('DELETE')) return 'DELETE'
  return 'UNKNOWN'
}

// 获取执行时间样式类
const getExecutionTimeClass = (time) => {
  if (time > 1000) return 'slow-sql'
  if (time > 500) return 'medium-sql'
  return 'fast-sql'
}

// 格式化SQL预览
const formatSqlPreview = (sql) => {
  if (!sql) return ''
  const preview = sql.replace(/\s+/g, ' ').trim()
  return preview.length > 100 ? preview.substring(0, 100) + '...' : preview
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    // 处理日期范围
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      queryForm.startDate = queryForm.dateRange[0]
      queryForm.endDate = queryForm.dateRange[1]
    }

    const data = await monitorApi.getStatistics({
      startDate: queryForm.startDate,
      endDate: queryForm.endDate
    })
    
    // 安全的数据访问，防止undefined错误
    if (data && typeof data === 'object') {
      statisticsData.value = Array.isArray(data) ? data : (data.records || [])
    } else {
      console.warn('统计数据接口返回数据格式异常:', data)
      statisticsData.value = []
    }
    
    console.log('统计数据获取成功:', statisticsData.value)
    
    // 延迟更新图表，确保图表已初始化
    setTimeout(() => {
      updateExecutionTrendChart()
    }, 100)
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取频次统计
const refreshFrequencyStats = async () => {
  frequencyLoading.value = true
  try {
    const data = await monitorApi.getFrequencyStatistics({
      date: queryForm.startDate,
      limit: 20
    })
    
    // 安全的数据访问，防止undefined错误
    if (data && typeof data === 'object') {
      frequencyStats.value = Array.isArray(data) ? data : (data.records || [])
    } else {
      console.warn('频次统计接口返回数据格式异常:', data)
      frequencyStats.value = []
    }
    
    console.log('频次统计数据获取成功:', frequencyStats.value)
    
    // 延迟更新图表，确保图表已初始化
    setTimeout(() => {
      updateSqlTypeChart()
    }, 100)
    
  } catch (error) {
    console.error('获取频次统计失败:', error)
    ElMessage.error('获取频次统计失败')
  } finally {
    frequencyLoading.value = false
  }
}

// 初始化图表
const initCharts = async () => {
  // 等待DOM完全渲染
  await nextTick()
  
  // 重试机制确保DOM元素准备就绪
  let retryCount = 0
  const maxRetries = 10
  
  const initSqlTypeChart = () => {
    if (sqlTypeChartRef.value && sqlTypeChartRef.value.clientWidth > 0) {
      if (sqlTypeChart) {
        sqlTypeChart.dispose()
      }
      sqlTypeChart = echarts.init(sqlTypeChartRef.value)
      return true
    }
    return false
  }
  
  const initExecutionTrendChart = () => {
    if (executionTrendChartRef.value && executionTrendChartRef.value.clientWidth > 0) {
      if (executionTrendChart) {
        executionTrendChart.dispose()
      }
      executionTrendChart = echarts.init(executionTrendChartRef.value)
      return true
    }
    return false
  }
  
  // 带重试的初始化
  const tryInit = () => {
    const sqlTypeInitialized = initSqlTypeChart()
    const executionTrendInitialized = initExecutionTrendChart()
    
    if (sqlTypeInitialized && executionTrendInitialized) {
      console.log('图表初始化成功')
      return true
    }
    
    retryCount++
    if (retryCount < maxRetries) {
      console.log(`图表初始化重试 ${retryCount}/${maxRetries}`)
      setTimeout(tryInit, 100)
    } else {
      console.error('图表初始化失败，已达到最大重试次数')
    }
    return false
  }
  
  tryInit()
}

// 更新图表
const updateCharts = () => {
  // 确保图表已初始化
  if (!sqlTypeChart || !executionTrendChart) {
    console.warn('图表未初始化，等待初始化完成后再更新')
    setTimeout(() => {
      if (sqlTypeChart && executionTrendChart) {
        updateCharts()
      }
    }, 200)
    return
  }
  
  updateSqlTypeChart()
  updateExecutionTrendChart()
}

// 更新SQL类型分布图
const updateSqlTypeChart = () => {
  if (!sqlTypeChart) {
    console.warn('SQL类型图表未初始化')
    return
  }
  
  // 由于后端SqlStatistics没有sqlType字段，我们根据sqlTemplate推断SQL类型
  const sqlTypeData = {}
  
  if (frequencyStats.value.length === 0) {
    // 没有数据时显示提示
    const option = {
      title: {
        text: 'SQL类型分布',
        left: 'center',
        textStyle: {
          fontSize: 14,
          color: '#303133'
        }
      },
      graphic: {
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: '暂无数据',
          fontSize: 16,
          fill: '#999'
        }
      }
    }
    sqlTypeChart.setOption(option, true)
    return
  }
  
  frequencyStats.value.forEach(item => {
    const template = item.sqlTemplate || ''
    let type = 'UNKNOWN'
    if (template.toUpperCase().includes('SELECT')) type = 'SELECT'
    else if (template.toUpperCase().includes('INSERT')) type = 'INSERT'
    else if (template.toUpperCase().includes('UPDATE')) type = 'UPDATE'
    else if (template.toUpperCase().includes('DELETE')) type = 'DELETE'
    
    sqlTypeData[type] = (sqlTypeData[type] || 0) + (item.executionCount || 0)
  })
  
  const pieData = Object.entries(sqlTypeData).map(([name, value]) => ({ name, value }))
  
  const option = {
    title: {
      text: 'SQL类型分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        color: '#303133'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: pieData.map(item => item.name)
    },
    series: [
      {
        name: 'SQL类型',
        type: 'pie',
        radius: '50%',
        data: pieData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  sqlTypeChart.setOption(option, true)
}

// 更新执行时间趋势图
const updateExecutionTrendChart = () => {
  if (!executionTrendChart) {
    console.warn('执行时间趋势图表未初始化')
    return
  }
  
  if (statisticsData.value.length === 0) {
    // 没有数据时显示提示
    const option = {
      title: {
        text: '执行时间趋势',
        left: 'center',
        textStyle: {
          fontSize: 14,
          color: '#303133'
        }
      },
      graphic: {
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: '暂无数据',
          fontSize: 16,
          fill: '#999'
        }
      }
    }
    executionTrendChart.setOption(option, true)
    return
  }
  
  const dates = statisticsData.value.map(item => item.statisticsDate).sort()
  const executions = statisticsData.value.map(item => item.executionCount || 0)
  const slowSqls = statisticsData.value.map(item => item.slowSqlCount || 0)
  
  const option = {
    title: {
      text: '执行时间趋势',
      left: 'center',
      textStyle: {
        fontSize: 14,
        color: '#303133'
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['总执行次数', '慢SQL次数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '总执行次数',
        type: 'line',
        stack: 'Total',
        data: executions,
        smooth: true,
        lineStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.1)'
        }
      },
      {
        name: '慢SQL次数',
        type: 'line',
        stack: 'Total',
        data: slowSqls,
        smooth: true,
        lineStyle: {
          color: '#F56C6C'
        },
        areaStyle: {
          color: 'rgba(245, 108, 108, 0.1)'
        }
      }
    ]
  }
  
  executionTrendChart.setOption(option, true)
}

// 重置处理
const handleReset = () => {
  queryForm.dateRange = null
  queryForm.startDate = null
  queryForm.endDate = null
  fetchStatistics()
}

// 刷新数据
const refresh = async () => {
  await Promise.all([
    fetchStatistics(),
    refreshFrequencyStats()
  ])
}

// 暴露方法给父组件
defineExpose({
  refresh
})

// 组件挂载后初始化
onMounted(async () => {
  console.log('StatisticsPanel 组件开始挂载')
  
  // 等待DOM渲染完成
  await nextTick()
  
  // 初始化图表
  await initCharts()
  
  // 延迟获取数据，确保图表初始化完成
  setTimeout(async () => {
    await refresh()
  }, 300)
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    sqlTypeChart?.resize()
    executionTrendChart?.resize()
  })
})
</script>

<style scoped>
.statistics-panel {
  height: 100%;
}

.filter-card {
  margin-bottom: 20px;
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-card .el-card__body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
}

.stat-content {
  text-align: left;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin: 8px 0 4px 0;
}

.stat-trend {
  font-size: 12px;
  color: #67C23A;
  display: flex;
  align-items: center;
  gap: 4px;
}

.trend-down {
  color: #F56C6C;
}

.stat-icon {
  font-size: 40px;
  opacity: 0.8;
}

.stat-icon.total { color: #409EFF; }
.stat-icon.slow { color: #F56C6C; }
.stat-icon.avg { color: #E6A23C; }
.stat-icon.error { color: #909399; }

.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.chart-card {
  min-height: 400px;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.table-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sql-preview {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.slow-sql {
  color: #F56C6C;
  font-weight: bold;
}

.medium-sql {
  color: #E6A23C;
  font-weight: bold;
}

.fast-sql {
  color: #67C23A;
}

@media (max-width: 1200px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
}
</style> 