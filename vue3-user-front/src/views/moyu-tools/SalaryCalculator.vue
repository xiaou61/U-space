<template>
  <div class="salary-calculator">
    <!-- 头部标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="main-title">
            <el-icon class="title-icon"><Money /></el-icon>
            打工人时薪计算器
          </h1>
          <p class="subtitle">实时计算工作收入，让每分钟的努力都有意义</p>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-section">
      <div class="content-container">
        <!-- 薪资配置提示 -->
        <div v-if="!hasConfig" class="config-prompt">
          <div class="prompt-card">
            <div class="prompt-icon">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="prompt-content">
              <h3>首次使用需要配置薪资信息</h3>
              <p>请先配置您的月薪和工作时间，以便准确计算收入</p>
              <el-button type="primary" size="large" @click="showConfigDialog = true">
                <el-icon><Plus /></el-icon>
                立即配置
              </el-button>
            </div>
          </div>
        </div>

        <!-- 计算器主界面 -->
        <div v-else class="calculator-dashboard">
          <!-- 顶部统计卡片 -->
          <div class="stats-grid">
            <!-- 基础薪资信息 -->
            <div class="stat-card salary-info">
              <div class="card-header">
                <div class="card-title">
                  <el-icon><CreditCard /></el-icon>
                  薪资信息
                </div>
                <el-button text @click="showConfigDialog = true">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
              </div>
              <div class="card-content">
                <div class="salary-display">
                  <div class="salary-item">
                    <span class="label">月薪</span>
                    <span class="value">¥{{ formatNumber(calculatorData.monthlySalary) }}</span>
                  </div>
                  <div class="salary-item">
                    <span class="label">时薪</span>
                    <span class="value highlight">¥{{ formatNumber(calculatorData.hourlyRate) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 今日收入 -->
            <div class="stat-card today-earnings">
              <div class="card-header">
                <div class="card-title">
                  <el-icon><Calendar /></el-icon>
                  今日收入
                </div>
                <div class="work-status" :class="getWorkStatusClass()">
                  {{ getWorkStatusText() }}
                </div>
              </div>
              <div class="card-content">
                <div class="earnings-display">
                  <div class="main-value realtime-earnings">
                    ¥{{ formatNumber(displayEarnings) }}
                  </div>
                  <div class="sub-info">
                    已工作 {{ formatNumber(displayWorkHours) }} 小时
                  </div>
                  <div v-if="calculatorData.workStatus === 1" class="realtime-tip">
                    <el-icon class="spinning"><Timer /></el-icon>
                    实时计算中...
                  </div>
                  <div v-else-if="calculatorData.workStatus === 2" class="paused-tip">
                    <el-icon><VideoPause /></el-icon>
                    工作暂停中
                  </div>
                </div>
              </div>
            </div>

            <!-- 工作控制 -->
            <div class="stat-card work-control">
              <div class="card-header">
                <div class="card-title">
                  <el-icon><Timer /></el-icon>
                  工作控制
                </div>
              </div>
              <div class="card-content">
                <div class="control-buttons">
                  <!-- 未开始状态 -->
                  <el-button 
                    v-if="calculatorData.workStatus === 0"
                    type="success" 
                    size="large"
                    @click="startWork"
                    :loading="actionLoading"
                  >
                    <el-icon><VideoPlay /></el-icon>
                    开始工作
                  </el-button>
                  
                  <!-- 工作中状态 -->
                  <div v-else-if="calculatorData.workStatus === 1" class="working-controls">
                    <el-button 
                      type="warning" 
                      size="large"
                      @click="pauseWork"
                      :loading="actionLoading"
                    >
                      <el-icon><VideoPause /></el-icon>
                      暂停工作
                    </el-button>
                    <el-button 
                      type="danger" 
                      size="large"
                      @click="endWork"
                      :loading="actionLoading"
                    >
                      <el-icon><SwitchButton /></el-icon>
                      结束工作
                    </el-button>
                  </div>
                  
                  <!-- 暂停中状态 -->
                  <div v-else-if="calculatorData.workStatus === 2" class="paused-controls">
                    <el-button 
                      type="primary" 
                      size="large"
                      @click="resumeWork"
                      :loading="actionLoading"
                    >
                      <el-icon><VideoPlay /></el-icon>
                      恢复工作
                    </el-button>
                    <el-button 
                      type="danger" 
                      size="large"
                      @click="endWork"
                      :loading="actionLoading"
                    >
                      <el-icon><SwitchButton /></el-icon>
                      结束工作
                    </el-button>
                  </div>
                  
                  <!-- 已完成状态 -->
                  <el-button 
                    v-else
                    type="info" 
                    size="large"
                    disabled
                  >
                    <el-icon><Select /></el-icon>
                    今日完成
                  </el-button>
                </div>
                <div v-if="calculatorData.todayStartTime" class="work-time-info">
                  开始时间：{{ formatDateTime(calculatorData.todayStartTime) }}
                </div>
              </div>
            </div>
          </div>

          <!-- 统计图表区域 -->
          <div class="charts-section">
            <div class="chart-grid">
              <!-- 本周统计 -->
              <div class="chart-card">
                <div class="chart-header">
                  <h3>本周统计</h3>
                </div>
                <div class="chart-content">
                  <div class="stat-item">
                    <div class="stat-label">工作时长</div>
                    <div class="stat-value">{{ formatNumber(calculatorData.weekWorkHours) }} 小时</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-label">收入金额</div>
                    <div class="stat-value earnings">¥{{ formatNumber(calculatorData.weekEarnings) }}</div>
                  </div>
                </div>
              </div>

              <!-- 本月统计 -->
              <div class="chart-card">
                <div class="chart-header">
                  <h3>本月统计</h3>
                </div>
                <div class="chart-content">
                  <div class="stat-item">
                    <div class="stat-label">工作时长</div>
                    <div class="stat-value">{{ formatNumber(calculatorData.monthWorkHours) }} 小时</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-label">收入金额</div>
                    <div class="stat-value earnings">¥{{ formatNumber(calculatorData.monthEarnings) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 薪资配置对话框 -->
    <el-dialog 
      v-model="showConfigDialog" 
      title="薪资配置" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="configFormRef"
        :model="configForm" 
        :rules="configRules" 
        label-width="120px"
      >
        <el-form-item label="月薪" prop="monthlySalary">
          <el-input-number
            v-model="configForm.monthlySalary"
            :min="1000"
            :max="999999.99"
            :precision="2"
            :step="100"
            placeholder="请输入月薪"
            style="width: 100%"
          />
          <div class="form-tip">单位：人民币元</div>
        </el-form-item>
        
        <el-form-item label="每月工作天数" prop="workDaysPerMonth">
          <el-input-number
            v-model="configForm.workDaysPerMonth"
            :min="1"
            :max="31"
            :step="1"
            placeholder="请输入每月工作天数"
            style="width: 100%"
          />
          <div class="form-tip">一般为22天</div>
        </el-form-item>
        
        <el-form-item label="每日工作小时" prop="workHoursPerDay">
          <el-input-number
            v-model="configForm.workHoursPerDay"
            :min="0.5"
            :max="24"
            :precision="2"
            :step="0.5"
            placeholder="请输入每日工作小时数"
            style="width: 100%"
          />
          <div class="form-tip">一般为8小时</div>
        </el-form-item>

        <!-- 计算预览 -->
        <el-form-item v-if="configForm.monthlySalary && configForm.workDaysPerMonth && configForm.workHoursPerDay">
          <div class="preview-box">
            <div class="preview-title">预计时薪</div>
            <div class="preview-value">
              ¥{{ calculateHourlyRate() }}
            </div>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showConfigDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveConfig"
            :loading="saveLoading"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Money, Setting, Plus, CreditCard, Edit, Calendar, Timer, 
  VideoPlay, VideoPause, Select, SwitchButton
} from '@element-plus/icons-vue'
import { 
  getSalaryCalculatorData, 
  getSalaryConfig, 
  saveOrUpdateSalaryConfig, 
  handleWorkTime 
} from '@/api/moyu'

// 响应式数据
const loading = ref(true)
const actionLoading = ref(false)
const saveLoading = ref(false)
const showConfigDialog = ref(false)
const hasConfig = ref(false)

// 实时计时器相关
const realtimeTimer = ref(null)
const realtimeWorkHours = ref(0)
const realtimeEarnings = ref(0)

// 计算器数据
const calculatorData = reactive({
  monthlySalary: 0,
  workDaysPerMonth: 22,
  workHoursPerDay: 8,
  hourlyRate: 0,
  todayWorkHours: 0,
  todayEarnings: 0,
  todayStartTime: null,
  workStatus: 0, // 0-未开始，1-进行中，2-已完成
  weekWorkHours: 0,
  weekEarnings: 0,
  monthWorkHours: 0,
  monthEarnings: 0
})

// 配置表单
const configForm = reactive({
  monthlySalary: null,
  workDaysPerMonth: 22,
  workHoursPerDay: 8
})

// 表单引用
const configFormRef = ref()

// 计算属性：显示的工作时长（实时或静态）
const displayWorkHours = computed(() => {
  if (calculatorData.workStatus === 1) {
    // 工作中状态显示实时计算的时长
    return realtimeWorkHours.value
  } else {
    // 其他状态显示静态数据
    return calculatorData.todayWorkHours || 0
  }
})

// 计算属性：显示的收入（实时或静态）
const displayEarnings = computed(() => {
  if (calculatorData.workStatus === 1) {
    // 工作中状态显示实时计算的收入
    return realtimeEarnings.value
  } else {
    // 其他状态显示静态数据
    return calculatorData.todayEarnings || 0
  }
})

// 表单验证规则
const configRules = {
  monthlySalary: [
    { required: true, message: '请输入月薪', trigger: 'blur' },
    { type: 'number', min: 1000, max: 999999.99, message: '月薪应在1000-999999.99之间', trigger: 'blur' }
  ],
  workDaysPerMonth: [
    { required: true, message: '请输入每月工作天数', trigger: 'blur' },
    { type: 'number', min: 1, max: 31, message: '工作天数应在1-31之间', trigger: 'blur' }
  ],
  workHoursPerDay: [
    { required: true, message: '请输入每日工作小时数', trigger: 'blur' },
    { type: 'number', min: 0.5, max: 24, message: '工作小时数应在0.5-24之间', trigger: 'blur' }
  ]
}

// 计算时薪预览
const calculateHourlyRate = () => {
  if (!configForm.monthlySalary || !configForm.workDaysPerMonth || !configForm.workHoursPerDay) {
    return '0.00'
  }
  const rate = configForm.monthlySalary / (configForm.workDaysPerMonth * configForm.workHoursPerDay)
  return rate.toFixed(2)
}

// 获取工作状态样式类
const getWorkStatusClass = () => {
  switch (calculatorData.workStatus) {
    case 0: return 'status-idle'
    case 1: return 'status-working'
    case 2: return 'status-paused'
    case 3: return 'status-completed'
    default: return 'status-idle'
  }
}

// 获取工作状态文本
const getWorkStatusText = () => {
  switch (calculatorData.workStatus) {
    case 0: return '待开始'
    case 1: return '工作中'
    case 2: return '暂停中'
    case 3: return '已完成'
    default: return '待开始'
  }
}

// 格式化数字显示
const formatNumber = (value) => {
  if (!value && value !== 0) return '0.00'
  return Number(value).toFixed(2)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit', 
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载计算器数据
const loadCalculatorData = async () => {
  try {
    loading.value = true
    const data = await getSalaryCalculatorData()
    
    if (data && data.monthlySalary) {
      // 有配置数据
      Object.assign(calculatorData, data)
      hasConfig.value = true
    } else {
      // 无配置数据
      hasConfig.value = false
    }
  } catch (error) {
    console.error('加载计算器数据失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
    } else {
      ElMessage.error('加载数据失败')
    }
  } finally {
    loading.value = false
  }
}

// 加载配置数据
const loadConfigData = async () => {
  try {
    const data = await getSalaryConfig()
    if (data) {
      Object.assign(configForm, data)
    }
  } catch (error) {
    console.error('加载配置数据失败:', error)
  }
}

// 保存配置
const saveConfig = async () => {
  if (!configFormRef.value) return
  
  try {
    await configFormRef.value.validate()
    saveLoading.value = true
    
    await saveOrUpdateSalaryConfig(configForm)
    ElMessage.success('配置保存成功')
    showConfigDialog.value = false
    
    // 重新加载数据
    await loadCalculatorData()
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      console.error('保存配置失败:', error)
      ElMessage.error('保存配置失败')
    }
  } finally {
    saveLoading.value = false
  }
}

// 开始工作
const startWork = async () => {
  try {
    actionLoading.value = true
    const data = await handleWorkTime({
      action: 'START',
      remark: '开始今日工作'
    })
    
    Object.assign(calculatorData, data)
    ElMessage.success('开始工作记录')
    
    // 开始实时计时
    startRealTimeTimer()
  } catch (error) {
    console.error('开始工作失败:', error)
    ElMessage.error('开始工作失败')
  } finally {
    actionLoading.value = false
  }
}

// 暂停工作
const pauseWork = async () => {
  try {
    actionLoading.value = true
    const data = await handleWorkTime({
      action: 'PAUSE',
      remark: '暂停工作'
    })
    
    Object.assign(calculatorData, data)
    ElMessage.success('工作已暂停')
    
    // 停止实时计时
    stopRealTimeTimer()
  } catch (error) {
    console.error('暂停工作失败:', error)
    ElMessage.error('暂停工作失败')
  } finally {
    actionLoading.value = false
  }
}

// 恢复工作
const resumeWork = async () => {
  try {
    actionLoading.value = true
    const data = await handleWorkTime({
      action: 'RESUME',
      remark: '恢复工作'
    })
    
    Object.assign(calculatorData, data)
    ElMessage.success('工作已恢复')
    
    // 恢复实时计时
    startRealTimeTimer()
  } catch (error) {
    console.error('恢复工作失败:', error)
    ElMessage.error('恢复工作失败')
  } finally {
    actionLoading.value = false
  }
}

// 结束工作
const endWork = async () => {
  try {
    await ElMessageBox.confirm(
      '确定结束今日工作吗？',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    actionLoading.value = true
    const data = await handleWorkTime({
      action: 'END',
      remark: '结束今日工作'
    })
    
    Object.assign(calculatorData, data)
    ElMessage.success('工作记录已完成')
    
    // 停止实时计时
    stopRealTimeTimer()
  } catch (error) {
    if (error === 'cancel') return
    console.error('结束工作失败:', error)
    ElMessage.error('结束工作失败')
  } finally {
    actionLoading.value = false
  }
}

// 实时计时器方法
const startRealTimeTimer = () => {
  if (realtimeTimer.value) return // 避免重复启动
  
  realtimeTimer.value = setInterval(() => {
    if (!calculatorData.todayStartTime || !calculatorData.hourlyRate) return
    
    const now = new Date()
    const startTime = new Date(calculatorData.todayStartTime)
    
    // 计算已工作的总分钟数
    let totalMinutes = Math.floor((now - startTime) / (1000 * 60))
    
    // 减去已知的暂停时长（如果有的话）
    const pauseMinutes = calculatorData.totalPauseMinutes || 0
    totalMinutes = Math.max(0, totalMinutes - pauseMinutes)
    
    // 转换为小时数
    const workHours = totalMinutes / 60
    realtimeWorkHours.value = workHours
    
    // 计算实时收入
    const earnings = workHours * calculatorData.hourlyRate
    realtimeEarnings.value = earnings
  }, 1000) // 每秒更新一次
}

const stopRealTimeTimer = () => {
  if (realtimeTimer.value) {
    clearInterval(realtimeTimer.value)
    realtimeTimer.value = null
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  await loadCalculatorData()
  if (!hasConfig.value) {
    await loadConfigData()
  }
  
  // 如果当前状态是工作中，启动实时计时器
  if (calculatorData.workStatus === 1) {
    startRealTimeTimer()
  }
})

// 组件卸载时清理计时器
onUnmounted(() => {
  stopRealTimeTimer()
})
</script>

<style scoped>
.salary-calculator {
  min-height: 100vh;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  position: relative;
}

/* 头部区域 */
.page-header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 40px 20px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  text-align: center;
}

.title-section {
  color: white;
}

.main-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  font-size: 2.2rem;
  color: #fbbf24;
}

.subtitle {
  font-size: 1rem;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
}

/* 内容区域 */
.content-section {
  padding: 30px 20px 60px;
}

.content-container {
  max-width: 1400px;
  margin: 0 auto;
}

/* 配置提示 */
.config-prompt {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.prompt-card {
  background: white;
  border-radius: 20px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.prompt-icon {
  font-size: 4rem;
  color: #10b981;
  margin-bottom: 24px;
}

.prompt-content h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.prompt-content p {
  color: #6b7280;
  margin: 0 0 32px 0;
  line-height: 1.6;
}

/* 计算器仪表板 */
.calculator-dashboard {
  animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
}

.card-content {
  flex: 1;
}

/* 薪资信息卡片 */
.salary-display {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.salary-item {
  text-align: center;
  flex: 1;
}

.salary-item .label {
  display: block;
  font-size: 0.9rem;
  color: #6b7280;
  margin-bottom: 8px;
}

.salary-item .value {
  display: block;
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
}

.salary-item .value.highlight {
  color: #10b981;
}

/* 今日收入卡片 */
.work-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-idle {
  background: #f3f4f6;
  color: #6b7280;
}

.status-working {
  background: #dcfdf7;
  color: #059669;
}

.status-paused {
  background: #fef3c7;
  color: #d97706;
}

.status-completed {
  background: #dbeafe;
  color: #2563eb;
}

.earnings-display {
  text-align: center;
}

.main-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: #10b981;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.realtime-earnings {
  animation: pulse 2s ease-in-out infinite alternate;
}

@keyframes pulse {
  from {
    color: #10b981;
  }
  to {
    color: #059669;
  }
}

.sub-info {
  color: #6b7280;
  font-size: 0.9rem;
}

.realtime-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 8px;
  color: #059669;
  font-size: 0.8rem;
  font-weight: 500;
}

.paused-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 8px;
  color: #d97706;
  font-size: 0.8rem;
  font-weight: 500;
}

.spinning {
  animation: spin 2s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 工作控制卡片 */
.control-buttons {
  text-align: center;
  margin-bottom: 16px;
}

.control-buttons .el-button {
  min-width: 140px;
}

.working-controls,
.paused-controls {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.working-controls .el-button,
.paused-controls .el-button {
  flex: 1;
  min-width: 120px;
  max-width: 160px;
}

.work-time-info {
  text-align: center;
  color: #6b7280;
  font-size: 0.85rem;
}

/* 图表区域 */
.charts-section {
  margin-top: 30px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.chart-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.chart-header {
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.chart-content {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
}

.stat-value.earnings {
  color: #10b981;
}

/* 对话框样式 */
.form-tip {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: 4px;
}

.preview-box {
  background: #f0fdf4;
  border: 1px solid #10b981;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.preview-title {
  font-size: 0.9rem;
  color: #059669;
  margin-bottom: 8px;
}

.preview-value {
  font-size: 1.5rem;
  font-weight: 600;
  color: #10b981;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-title {
    font-size: 2rem;
    flex-direction: column;
    gap: 8px;
  }

  .title-icon {
    font-size: 1.8rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .chart-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .salary-display,
  .chart-content {
    flex-direction: column;
    gap: 16px;
  }

  .prompt-card {
    padding: 40px 24px;
    margin: 0 15px;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 30px 15px;
  }

  .content-section {
    padding: 20px 15px 40px;
  }

  .stat-card,
  .chart-card {
    padding: 20px;
  }

  .main-value {
    font-size: 2rem;
  }
}
</style>
