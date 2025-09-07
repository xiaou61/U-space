<template>
  <div class="sql-monitor">
    <el-card class="overview-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">SQL监控总览</span>
          <div class="header-actions">
            <el-button type="primary" @click="refreshAll" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新全部
            </el-button>
            <el-button @click="clearAllData">
              <el-icon><Delete /></el-icon>
              清理数据
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalSqlCount || 0 }}</div>
            <div class="stat-label">总SQL执行数</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon slow">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.slowSqlCount || 0 }}</div>
            <div class="stat-label">慢SQL数量</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon error">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.errorSqlCount || 0 }}</div>
            <div class="stat-label">错误SQL数量</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon avg">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.averageExecutionTime || 0 }}ms</div>
            <div class="stat-label">平均执行时间</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="content-row">
      <!-- 左侧主要内容 -->
      <el-col :span="16">
        <el-tabs v-model="activeTab" type="border-card" class="main-tabs">
          <!-- SQL调用树 -->
          <el-tab-pane label="SQL调用树" name="sqlTree">
            <div class="tab-content">
                            <div class="operations-bar">
                <div class="left-operations">
                  <el-button type="primary" @click="loadSqlTree">刷新调用树</el-button>
                  <el-button @click="clearSessions">清空会话</el-button>
                  <el-select 
                    v-model="selectedSession" 
                    placeholder="选择会话" 
                    style="width: 400px" 
                    @change="loadSessionTree"
                  >
                    <el-option 
                      v-for="(session, key) in (isSearching ? filteredSessions : allSessions)" 
                      :key="key" 
                      :label="getSessionLabel(key, session)" 
                      :value="key" 
                    />
                  </el-select>
                </div>
                <div class="right-operations">
                  <el-input 
                    v-model="sqlSearch" 
                    placeholder="搜索SQL记录（服务名、方法名、用户ID等）"
                    clearable
                    style="width: 350px; margin-right: 10px"
                    @keyup.enter="performSearch"
                    @clear="clearSearch"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                    <template #append>
                      <el-button @click="performSearch" :loading="sqlTreeLoading">
                        搜索
                      </el-button>
                    </template>
                  </el-input>
                  <el-button @click="fitTreeToScreen">适应屏幕</el-button>
                </div>
              </div>
              
              <!-- 搜索结果提示 -->
              <div v-if="isSearching" class="search-info">
                <el-alert
                  :title="`搜索结果：找到 ${Object.keys(filteredSessions).length} 个匹配的会话（关键词：${sqlSearch}）`"
                  type="info"
                  :closable="false"
                  style="margin-bottom: 15px"
                >
                  <template #default>
                    <el-button link type="primary" @click="clearSearch">
                      清空搜索，查看所有会话
                    </el-button>
                  </template>
                </el-alert>
              </div>
              
              <div class="tree-visualization-container">
                <div 
                  v-loading="sqlTreeLoading" 
                  id="sql-tree-container" 
                  class="tree-container"
                  @wheel.prevent="handleZoom"
                ></div>
                
                <!-- 树形图例 -->
                <div class="tree-legend">
                  <div class="legend-item">
                    <div class="legend-color select-color"></div>
                    <span>SELECT</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color insert-color"></div>
                    <span>INSERT</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color update-color"></div>
                    <span>UPDATE</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color delete-color"></div>
                    <span>DELETE</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color slow-color"></div>
                    <span>慢查询</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color error-color"></div>
                    <span>错误</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <!-- 慢SQL分析 -->
          <el-tab-pane label="慢SQL分析" name="slowSql">
            <div class="tab-content">
              <div class="operations-bar">
                <el-button type="primary" @click="loadSlowSqlAnalysis">刷新慢SQL</el-button>
                <el-input 
                  v-model="slowSqlSearch" 
                  placeholder="搜索SQL语句"
                  style="width: 250px"
                  @keyup.enter="filterSlowSql"
                >
                  <template #append>
                    <el-button @click="filterSlowSql">
                      <el-icon><Search /></el-icon>
                    </el-button>
                  </template>
                </el-input>
              </div>
              
              <el-table
                v-loading="slowSqlLoading"
                :data="filteredSlowSqlData"
                style="width: 100%"
                max-height="500"
              >
                <el-table-column prop="sql" label="SQL语句" show-overflow-tooltip />
                <el-table-column prop="executionTime" label="执行时间(ms)" width="120" sortable>
                  <template #default="scope">
                    <el-tag type="warning">{{ scope.row.executionTime }}ms</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="参数" show-overflow-tooltip width="200">
                  <template #default="scope">
                    {{ Array.isArray(scope.row.parameters) ? scope.row.parameters.join(', ') : scope.row.parameters || '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="startTime" label="执行时间" width="180" />
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button link type="primary" @click="viewSqlDetail(scope.row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <!-- 错误SQL分析 -->
          <el-tab-pane label="错误SQL分析" name="errorSql">
            <div class="tab-content">
              <div class="operations-bar">
                <el-button type="primary" @click="loadErrorSqlAnalysis">刷新错误SQL</el-button>
                <el-input 
                  v-model="errorSqlSearch" 
                  placeholder="搜索SQL语句或错误信息"
                  style="width: 250px"
                  @keyup.enter="filterErrorSql"
                >
                  <template #append>
                    <el-button @click="filterErrorSql">
                      <el-icon><Search /></el-icon>
                    </el-button>
                  </template>
                </el-input>
              </div>
              
              <el-table
                v-loading="errorSqlLoading"
                :data="filteredErrorSqlData"
                style="width: 100%"
                max-height="500"
              >
                <el-table-column prop="sql" label="SQL语句" show-overflow-tooltip />
                <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip width="200" />
                <el-table-column prop="executionTime" label="执行时间(ms)" width="120" />
                <el-table-column prop="startTime" label="错误时间" width="180" />
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button link type="primary" @click="viewSqlDetail(scope.row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
      
      <!-- 右侧配置面板 -->
      <el-col :span="8">
        <el-card class="config-card">
          <template #header>
            <span>系统配置</span>
          </template>
          
          <el-form :model="config" label-width="120px" size="small">
            <el-form-item label="启用追踪">
              <el-switch 
                v-model="config.traceEnabled" 
                @change="updateConfig"
                active-text="开启"
                inactive-text="关闭"
              />
            </el-form-item>
            
            <el-form-item label="慢SQL阈值">
              <el-input-number 
                v-model="config.slowSqlThreshold" 
                :min="100" 
                :max="10000" 
                :step="100"
                @change="updateConfig"
                style="width: 100%"
              />
              <div class="form-tip">单位: 毫秒</div>
            </el-form-item>
            
            <el-form-item label="最大会话数">
              <el-input-number 
                v-model="config.maxSessionCount" 
                :min="10" 
                :max="1000" 
                :step="10"
                @change="updateConfig"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="系统状态">
              <el-tag :type="systemHealth.status === 'UP' ? 'success' : 'danger'">
                {{ systemHealth.status === 'UP' ? '运行正常' : '异常' }}
              </el-tag>
            </el-form-item>
            
            <el-form-item label="当前会话数">
              <span>{{ statistics.currentSessionCount || 0 }}</span>
            </el-form-item>
          </el-form>
          
          <el-divider />
          
          <div class="config-actions">
            <el-button type="warning" @click="resetStatistics" style="width: 100%; margin-bottom: 10px;">
              <el-icon><RefreshRight /></el-icon>
              重置统计
            </el-button>
            
            <el-button @click="cleanupExpiredSessions" style="width: 100%; margin-bottom: 10px;">
              <el-icon><Delete /></el-icon>
              清理过期会话
            </el-button>
            
            <el-button type="success" @click="exportData" style="width: 100%;">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- SQL详情对话框 -->
    <el-dialog
      v-model="sqlDetailVisible"
      title="SQL详情"
      width="80%"
      :before-close="closeSqlDetail"
    >
      <div v-if="currentSqlDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="执行时间">
            <el-tag :type="getExecutionTimeType(currentSqlDetail.executionTime)">
              {{ currentSqlDetail.executionTime }}ms
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ currentSqlDetail.startTime }}</el-descriptions-item>
                     <el-descriptions-item label="状态">
             <el-tag v-if="currentSqlDetail.errorMessage" type="danger">错误</el-tag>
             <el-tag v-else-if="currentSqlDetail.slowSql" type="warning">慢查询</el-tag>
             <el-tag v-else type="success">正常</el-tag>
           </el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">SQL语句</el-divider>
        <el-input
          v-model="currentSqlDetail.sql"
          type="textarea"
          :rows="6"
          readonly
          class="sql-textarea"
        />
        
        <el-divider content-position="left">参数</el-divider>
        <el-input
          :value="Array.isArray(currentSqlDetail.parameters) ? JSON.stringify(currentSqlDetail.parameters, null, 2) : currentSqlDetail.parameters || '无参数'"
          type="textarea"
          :rows="3"
          readonly
          placeholder="无参数"
        />
        
        <el-divider v-if="currentSqlDetail.errorMessage" content-position="left">错误信息</el-divider>
        <el-alert
          v-if="currentSqlDetail.errorMessage"
          :title="currentSqlDetail.errorMessage"
          type="error"
          :closable="false"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, Delete, Search, Download, RefreshRight, 
  DataAnalysis, Warning, CircleClose, Timer 
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as d3 from 'd3'

// 响应式数据
const loading = ref(false)
const activeTab = ref('sqlTree')

// 统计数据
const statistics = ref({})
const systemHealth = ref({})

// SQL调用树
const sqlTreeLoading = ref(false)
const sqlTreeData = ref([])
const allSessions = ref({})
const filteredSessions = ref({})
const selectedSession = ref('')
const sqlSearch = ref('')
const isSearching = ref(false)
let treeVisualizer = null

// 慢SQL分析
const slowSqlLoading = ref(false)
const slowSqlData = ref([])
const slowSqlSearch = ref('')

// 错误SQL分析
const errorSqlLoading = ref(false)
const errorSqlData = ref([])
const errorSqlSearch = ref('')

// 系统配置
const config = ref({
  traceEnabled: true,
  slowSqlThreshold: 1000,
  maxSessionCount: 100
})

// SQL详情对话框
const sqlDetailVisible = ref(false)
const currentSqlDetail = ref(null)

// 计算属性
const filteredSlowSqlData = computed(() => {
  if (!slowSqlSearch.value) return slowSqlData.value
  return slowSqlData.value.filter(item => 
    item.sql?.toLowerCase().includes(slowSqlSearch.value.toLowerCase())
  )
})

const filteredErrorSqlData = computed(() => {
  if (!errorSqlSearch.value) return errorSqlData.value
  return errorSqlData.value.filter(item => 
    item.sql?.toLowerCase().includes(errorSqlSearch.value.toLowerCase()) ||
    item.errorMessage?.toLowerCase().includes(errorSqlSearch.value.toLowerCase())
  )
})

// 获取执行时间标签类型
const getExecutionTimeType = (time) => {
  if (time > config.value.slowSqlThreshold) return 'danger'
  if (time > config.value.slowSqlThreshold * 0.5) return 'warning'
  return 'success'
}

// 格式化显示SQL数量统计
const formatSqlCount = (sqlArray) => {
  if (!Array.isArray(sqlArray)) return '0'
  return sqlArray.length.toString()
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const res = await request.get('/api/sql-tree/statistics')
    // 根据实际返回数据结构调整字段映射
    statistics.value = {
      totalSqlCount: res?.totalSqlCount || res?.executionCount || 0,
      slowSqlCount: res?.slowSqlCount || 0,
      errorSqlCount: res?.errorSqlCount || res?.errorCount || 0,
      averageExecutionTime: res?.averageExecutionTime || res?.avgExecutionTime || 0,
      currentSessionCount: res?.currentSessionCount || 0
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 加载系统健康状态
const loadSystemHealth = async () => {
  try {
    const res = await request.get('/api/sql-tree/health')
    systemHealth.value = res || {}
    if (res.traceEnabled !== undefined) config.value.traceEnabled = res.traceEnabled
    if (res.slowSqlThreshold !== undefined) config.value.slowSqlThreshold = res.slowSqlThreshold
    if (res.maxSessionCount !== undefined) config.value.maxSessionCount = res.maxSessionCount
  } catch (error) {
    console.error('加载系统健康状态失败:', error)
  }
}



// 加载SQL调用树
const loadSqlTree = async () => {
  sqlTreeLoading.value = true
  try {
    // 加载所有会话数据
    const sessionsRes = await request.get('/api/sql-tree/sessions')
    if (sessionsRes && typeof sessionsRes === 'object') {
      allSessions.value = sessionsRes
      
      // 如果没有选择会话，默认选择第一个
      if (!selectedSession.value && Object.keys(sessionsRes).length > 0) {
        selectedSession.value = Object.keys(sessionsRes)[0]
      }
      
      // 渲染当前选择的会话
      if (selectedSession.value && sessionsRes[selectedSession.value]) {
        sqlTreeData.value = sessionsRes[selectedSession.value]
        await nextTick()
        renderSqlTree()
      }
    }
  } catch (error) {
    console.error('加载SQL调用树失败:', error)
    ElMessage.error('加载SQL调用树失败')
  } finally {
    sqlTreeLoading.value = false
  }
}

// 加载会话调用树
const loadSessionTree = async () => {
  const currentSessions = isSearching.value ? filteredSessions.value : allSessions.value
  if (currentSessions[selectedSession.value]) {
    sqlTreeData.value = currentSessions[selectedSession.value]
    await nextTick()
    renderSqlTree()
  }
}

// 加载慢SQL分析
const loadSlowSqlAnalysis = async () => {
  slowSqlLoading.value = true
  try {
    const res = await request.get('/api/sql-tree/analysis/slow-sql')
    slowSqlData.value = res || []
    
    // 如果接口返回空数据，从所有会话中筛选慢SQL
    if ((!res || res.length === 0) && Object.keys(allSessions.value).length > 0) {
      const slowSqlFromSessions = []
      Object.entries(allSessions.value).forEach(([sessionKey, sqlArray]) => {
        if (Array.isArray(sqlArray)) {
          const slowSqls = sqlArray.filter(sql => sql.slowSql || sql.executionTime > config.value.slowSqlThreshold)
          slowSqlFromSessions.push(...slowSqls)
        }
      })
      slowSqlData.value = slowSqlFromSessions.sort((a, b) => b.executionTime - a.executionTime)
    }
  } catch (error) {
    console.error('加载慢SQL分析失败:', error)
    ElMessage.error('加载慢SQL分析失败')
  } finally {
    slowSqlLoading.value = false
  }
}

// 加载错误SQL分析
const loadErrorSqlAnalysis = async () => {
  errorSqlLoading.value = true
  try {
    const res = await request.get('/api/sql-tree/analysis/error-sql')
    errorSqlData.value = res || []
    
    // 如果接口返回空数据，从所有会话中筛选错误SQL
    if ((!res || res.length === 0) && Object.keys(allSessions.value).length > 0) {
      const errorSqlFromSessions = []
      Object.entries(allSessions.value).forEach(([sessionKey, sqlArray]) => {
        if (Array.isArray(sqlArray)) {
          const errorSqls = sqlArray.filter(sql => sql.errorMessage)
          errorSqlFromSessions.push(...errorSqls)
        }
      })
      errorSqlData.value = errorSqlFromSessions.sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
    }
  } catch (error) {
    console.error('加载错误SQL分析失败:', error)
    ElMessage.error('加载错误SQL分析失败')
  } finally {
    errorSqlLoading.value = false
  }
}

// 更新配置
const updateConfig = async () => {
  try {
    await request.post('/api/sql-tree/config', config.value)
    ElMessage.success('配置更新成功')
    await loadSystemHealth()
  } catch (error) {
    console.error('更新配置失败:', error)
    ElMessage.error('更新配置失败')
  }
}

// 清空所有数据
const clearAllData = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有SQL监控数据吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.post('/api/sql-tree/clear')
    ElMessage.success('数据清理成功')
    await refreshAll()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理数据失败:', error)
      ElMessage.error('清理数据失败')
    }
  }
}

// 清空会话
const clearSessions = async () => {
  try {
    await request.post('/api/sql-tree/clear')
    ElMessage.success('会话清理成功')
    await loadSqlTree()
  } catch (error) {
    console.error('清理会话失败:', error)
    ElMessage.error('清理会话失败')
  }
}

// 重置统计
const resetStatistics = async () => {
  try {
    await request.post('/api/sql-tree/reset-statistics')
    ElMessage.success('统计信息重置成功')
    await loadStatistics()
  } catch (error) {
    console.error('重置统计失败:', error)
    ElMessage.error('重置统计失败')
  }
}

// 清理过期会话
const cleanupExpiredSessions = async () => {
  try {
    await request.post('/api/sql-tree/cleanup', { maxAgeMinutes: 60 })
    ElMessage.success('过期会话清理成功')
    await loadSqlTree()
  } catch (error) {
    console.error('清理过期会话失败:', error)
    ElMessage.error('清理过期会话失败')
  }
}

// 导出数据
const exportData = async () => {
  try {
    const res = await request.get('/api/sql-tree/export')
    const blob = new Blob([JSON.stringify(res, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `sql-monitor-${new Date().toISOString().split('T')[0]}.json`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('数据导出成功')
  } catch (error) {
    console.error('导出数据失败:', error)
    ElMessage.error('导出数据失败')
  }
}

// 过滤慢SQL
const filterSlowSql = () => {
  // 计算属性会自动更新
}

// 过滤错误SQL
const filterErrorSql = () => {
  // 计算属性会自动更新
}

// 查看SQL详情
const viewSqlDetail = (sql) => {
  currentSqlDetail.value = sql
  sqlDetailVisible.value = true
}

// 关闭SQL详情
const closeSqlDetail = () => {
  sqlDetailVisible.value = false
  currentSqlDetail.value = null
}

// 刷新所有数据
const refreshAll = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadStatistics(),
      loadSystemHealth(),
      loadSqlTree(),
      loadSlowSqlAnalysis(),
      loadErrorSqlAnalysis()
    ])
    ElMessage.success('刷新成功')
  } catch (error) {
    console.error('刷新失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    loading.value = false
  }
}

// 页面初始化
onMounted(() => {
  refreshAll()
})

// 页面销毁时清理
onUnmounted(() => {
  if (treeVisualizer) {
    // 清理D3.js创建的所有元素
    if (treeVisualizer.tooltip) {
      treeVisualizer.tooltip.remove()
    }
    if (treeVisualizer.svg) {
      treeVisualizer.svg.selectAll('*').remove()
    }
    // 清理容器
    const container = document.getElementById('sql-tree-container')
    if (container) {
      container.innerHTML = ''
    }
    treeVisualizer = null
  }
})

// SQL树形可视化类
class SqlTreeVisualizer {
  constructor(containerId) {
    this.containerId = containerId
    this.width = 1000
    this.height = 600
    this.margin = { top: 20, right: 150, bottom: 20, left: 150 }
    
    this.initializeSvg()
  }
  
  initializeSvg() {
    const container = d3.select(`#${this.containerId}`)
    container.selectAll('*').remove()
    
    // 清理旧的tooltip
    d3.selectAll('.sql-tooltip').remove()
    
    this.svg = container
      .append('svg')
      .attr('width', this.width)
      .attr('height', this.height)
    
    this.g = this.svg.append('g')
      .attr('transform', `translate(${this.margin.left},${this.margin.top})`)
    
    // 添加缩放功能
    this.zoom = d3.zoom()
      .scaleExtent([0.1, 3])
      .on('zoom', (event) => {
        if (this.g) {
          this.g.attr('transform', event.transform)
        }
      })
    
    this.svg.call(this.zoom)
    
    // 配置树布局
    this.tree = d3.tree()
      .size([this.height - this.margin.top - this.margin.bottom, 
             this.width - this.margin.left - this.margin.right])
             
    // 初始化工具提示
    this.tooltip = d3.select('body').append('div')
      .attr('class', 'sql-tooltip')
      .style('position', 'absolute')
      .style('visibility', 'hidden')
      .style('background', 'rgba(0, 0, 0, 0.8)')
      .style('color', 'white')
      .style('padding', '10px')
      .style('border-radius', '5px')
      .style('font-size', '12px')
      .style('z-index', '1000')
      .style('pointer-events', 'none')
  }
  
  render(sessions) {
    try {
      if (!this.g) return
      
      this.g.selectAll('*').remove()
      
      if (!sessions || Object.keys(sessions).length === 0) {
        this.showEmptyState('暂无SQL调用数据')
        return
      }
      
      // 选择第一个会话进行展示，或者使用指定的会话
      const sessionKey = Object.keys(sessions)[0]
      const rootNodes = sessions[sessionKey]
      
      if (!rootNodes || rootNodes.length === 0) {
        this.showEmptyState('搜索无结果')
        return
      }
      
      // 如果只有一个根节点，直接渲染
      if (rootNodes.length === 1) {
        this.renderTree(rootNodes[0])
      } else {
        // 如果有多个根节点，创建一个虚拟根节点
        const virtualRoot = {
          nodeId: 'virtual-root',
          sql: '会话根节点',
          sqlType: 'SESSION',
          serviceName: 'Session',
          methodName: 'root',
          executionTime: 0,
          children: rootNodes
        }
        this.renderTree(virtualRoot)
      }
    } catch (error) {
      console.error('渲染SQL树失败:', error)
    }
  }
  
  renderTree(rootNode) {
    // 构建D3层次结构
    const root = d3.hierarchy(rootNode, d => d.children && d.children.length > 0 ? d.children : null)
    
    // 计算节点位置
    this.tree(root)
    
    // 绘制连接线
    this.g.selectAll('.link')
      .data(root.links())
      .join('path')
      .attr('class', 'link')
      .attr('d', d3.linkHorizontal()
        .x(d => d.y)
        .y(d => d.x))
      .style('fill', 'none')
      .style('stroke', '#94a3b8')
      .style('stroke-width', '2px')
      .style('stroke-opacity', 0.6)
    
    // 绘制节点组
    const nodes = this.g.selectAll('.node')
      .data(root.descendants())
      .join('g')
      .attr('class', 'node')
      .attr('transform', d => `translate(${d.y},${d.x})`)
      .style('cursor', 'pointer')
    
    // 绘制节点圆圈
    nodes.append('circle')
      .attr('r', 12)
      .style('fill', d => this.getNodeColor(d.data))
      .style('stroke', '#1e293b')
      .style('stroke-width', '2px')
    
    // 添加节点文本
    nodes.append('text')
      .attr('dy', '.35em')
      .attr('x', d => d.children ? -20 : 20)
      .style('text-anchor', d => d.children ? 'end' : 'start')
      .style('font-size', '11px')
      .style('font-weight', '500')
      .style('fill', '#1e293b')
      .text(d => this.getNodeLabel(d.data))
    
    // 添加交互事件
    nodes
      .on('mouseover', (event, d) => this.showTooltip(event, d.data))
      .on('mouseout', () => this.hideTooltip())
      .on('click', (event, d) => {
        event.stopPropagation()
        viewSqlDetail(d.data)
      })
  }
  
  getNodeColor(data) {
    if (data.errorMessage) {
      return '#ef4444' // 错误：红色
    }
    if (data.slowSql || data.executionTime > 1000) {
      return '#f59e0b' // 慢SQL：橙色
    }
    switch (data.sqlType) {
      case 'SELECT':
        return '#10b981' // 查询：绿色
      case 'INSERT':
        return '#3b82f6' // 插入：蓝色
      case 'UPDATE':
        return '#8b5cf6' // 更新：紫色
      case 'DELETE':
        return '#ef4444' // 删除：红色
      case 'SESSION':
        return '#94a3b8' // 会话根节点：灰色
      default:
        return '#6b7280' // 默认：灰色
    }
  }
  
  getNodeLabel(data) {
    if (data.sqlType === 'SESSION') {
      return `${data.serviceName || 'Session'}`
    }
    const time = data.executionTime || 0
    return `${data.sqlType} (${time}ms)`
  }
  
  showTooltip(event, data) {
    if (!this.tooltip) return
    
    try {
      const tooltipContent = `
        <div style="font-weight: bold; margin-bottom: 5px;">${data.sqlType} 操作</div>
        <div>执行时间: ${data.executionTime || 0}ms</div>
        <div>影响行数: ${data.affectedRows || 0}</div>
        <div>服务: ${data.serviceName || 'Unknown'}</div>
        <div>方法: ${data.methodName || 'unknown'}</div>
        ${data.errorMessage ? `<div style="color: #ef4444;">错误: ${data.errorMessage}</div>` : ''}
      `
      
      this.tooltip
        .style('visibility', 'visible')
        .html(tooltipContent)
        .style('left', (event.pageX + 10) + 'px')
        .style('top', (event.pageY - 10) + 'px')
    } catch (error) {
      console.error('显示tooltip失败:', error)
    }
  }
  
  hideTooltip() {
    if (this.tooltip) {
      this.tooltip.style('visibility', 'hidden')
    }
  }
  
  showEmptyState(message = '暂无SQL调用数据') {
    this.g.append('text')
      .attr('x', (this.width - this.margin.left - this.margin.right) / 2)
      .attr('y', (this.height - this.margin.top - this.margin.bottom) / 2)
      .attr('text-anchor', 'middle')
      .style('font-size', '18px')
      .style('fill', '#6b7280')
      .text(message)
  }
  
  fitToScreen() {
    const bounds = this.g.node().getBBox()
    const fullWidth = this.width
    const fullHeight = this.height
    const width = bounds.width
    const height = bounds.height
    const midX = bounds.x + width / 2
    const midY = bounds.y + height / 2
    
    if (width === 0 || height === 0) return
    
    const scale = Math.min(0.8 * fullWidth / width, 0.8 * fullHeight / height)
    const translate = [fullWidth / 2 - scale * midX, fullHeight / 2 - scale * midY]
    
    this.svg.transition()
      .duration(750)
      .call(this.zoom.transform, d3.zoomIdentity.translate(translate[0], translate[1]).scale(scale))
  }
}

// 执行搜索
const performSearch = async () => {
  if (!sqlSearch.value || sqlSearch.value.trim() === '') {
    clearSearch()
    return
  }
  
  sqlTreeLoading.value = true
  try {
    const response = await request.post('/api/sql-tree/sessions/search', {
      searchTerm: sqlSearch.value.trim()
    })
    
    filteredSessions.value = response || {}
    isSearching.value = true
    
    // 如果有搜索结果，选择第一个会话
    const sessionKeys = Object.keys(filteredSessions.value)
    if (sessionKeys.length > 0) {
      selectedSession.value = sessionKeys[0]
      await loadSessionTree()
      ElMessage.success(`找到 ${sessionKeys.length} 个匹配的会话`)
    } else {
      selectedSession.value = ''
      sqlTreeData.value = []
      renderSqlTree()
      ElMessage.info('未找到匹配的会话')
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    sqlTreeLoading.value = false
  }
}

// 清空搜索
const clearSearch = async () => {
  sqlSearch.value = ''
  isSearching.value = false
  filteredSessions.value = {}
  
  // 恢复显示所有会话
  if (Object.keys(allSessions.value).length > 0) {
    const firstSessionKey = Object.keys(allSessions.value)[0]
    selectedSession.value = firstSessionKey
    await loadSessionTree()
  }
}

// 渲染SQL树
const renderSqlTree = () => {
  try {
    // 确保容器存在
    const container = document.getElementById('sql-tree-container')
    if (!container) return
    
    if (!treeVisualizer) {
      treeVisualizer = new SqlTreeVisualizer('sql-tree-container')
    }
    
    const currentSessions = isSearching.value ? filteredSessions.value : allSessions.value
    if (selectedSession.value && currentSessions[selectedSession.value]) {
      const sessionData = { [selectedSession.value]: currentSessions[selectedSession.value] }
      treeVisualizer.render(sessionData)
    }
  } catch (error) {
    console.error('渲染SQL树失败:', error)
  }
}

// 获取会话标签（包含用户信息）
const getSessionLabel = (sessionKey, sessionData) => {
  if (!sessionData || sessionData.length === 0) {
    return `${sessionKey} (0条SQL)`
  }
  
  // 获取第一个SQL节点的用户信息
  const firstSql = sessionData[0]
  const userInfo = firstSql.userId ? 
    `用户${firstSql.userId}(${firstSql.username || 'unknown'})` : 
    '未知用户'
    
  return `${sessionKey} - ${userInfo} (${sessionData.length}条SQL)`
}



// 适应屏幕
const fitTreeToScreen = () => {
  if (treeVisualizer) {
    treeVisualizer.fitToScreen()
  }
}

// 处理缩放
const handleZoom = (event) => {
  // 缩放功能由D3.js处理
}

// 监听数据变化
watch(sqlTreeData, (newData) => {
  if (newData && newData.length > 0) {
    console.log(`加载了 ${newData.length} 条SQL记录`)
  }
})
</script>

<style scoped>
.sql-monitor {
  padding: 20px;
}

.overview-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 10px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
}

.stat-icon.total {
  background: #e3f2fd;
  color: #1976d2;
}

.stat-icon.slow {
  background: #fff3e0;
  color: #f57c00;
}

.stat-icon.error {
  background: #ffebee;
  color: #d32f2f;
}

.stat-icon.avg {
  background: #e8f5e8;
  color: #388e3c;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.content-row {
  margin-top: 20px;
}

.main-tabs {
  height: calc(100vh - 300px);
}

.config-card {
  height: calc(100vh - 300px);
  overflow-y: auto;
}

.tab-content {
  padding: 20px 0;
}

.operations-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.left-operations {
  display: flex;
  align-items: center;
  gap: 15px;
}



.right-operations {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.config-actions {
  margin-top: 20px;
}

.sql-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.tree-visualization-container {
  position: relative;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}

.tree-container {
  width: 100%;
  height: 600px;
  overflow: hidden;
}

.tree-legend {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 10px;
  font-size: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.select-color {
  background-color: #10b981;
}

.insert-color {
  background-color: #3b82f6;
}

.update-color {
  background-color: #8b5cf6;
}

.delete-color {
  background-color: #ef4444;
}

.slow-color {
  background-color: #f59e0b;
}

.error-color {
  background-color: #ef4444;
}

:global(.sql-tooltip) {
  pointer-events: none;
  transition: opacity 0.2s;
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

:deep(.el-table__body-wrapper) {
  max-height: 500px;
}
</style> 