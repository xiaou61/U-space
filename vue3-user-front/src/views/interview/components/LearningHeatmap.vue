<template>
  <div class="learning-heatmap-wrapper">
    <!-- 头部 -->
    <div class="heatmap-header">
      <div class="header-left">
        <div class="header-icon">✨</div>
        <div class="header-text">
          <h3 class="header-title">学习足迹</h3>
          <p class="header-subtitle">{{ selectedYear }} 年共学习 <strong>{{ heatmapData.totalDays || 0 }}</strong> 天</p>
        </div>
      </div>
      <el-select v-model="selectedYear" size="small" class="year-select">
        <el-option 
          v-for="year in yearOptions" 
          :key="year" 
          :label="year + '年'" 
          :value="year" 
        />
      </el-select>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">🔥</div>
        <div class="stat-info">
          <div class="stat-value">{{ heatmapData.currentStreak || 0 }}</div>
          <div class="stat-name">连续学习</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏆</div>
        <div class="stat-info">
          <div class="stat-value">{{ heatmapData.longestStreak || 0 }}</div>
          <div class="stat-name">最长连续</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📅</div>
        <div class="stat-info">
          <div class="stat-value">{{ currentMonthDays }}</div>
          <div class="stat-name">本月学习</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🎯</div>
        <div class="stat-info">
          <div class="stat-value">{{ heatmapData.totalDays || 0 }}</div>
          <div class="stat-name">总学习天</div>
        </div>
      </div>
    </div>

    <!-- 热力图 -->
    <div class="heatmap-box" v-loading="loading">
      <div class="heatmap-scroll">
        <div class="heatmap-inner">
        <!-- 月份标签 -->
        <div class="month-row">
          <div class="week-label-spacer"></div>
          <div class="month-labels">
            <span 
              v-for="m in monthPositions" 
              :key="m.month" 
              class="month-name"
              :style="{ left: m.left + 'px' }"
            >{{ m.name }}</span>
          </div>
        </div>

        <!-- 热力图主体 -->
        <div class="heatmap-main">
          <!-- 星期标签 -->
          <div class="week-labels">
            <span></span>
            <span>Mon</span>
            <span></span>
            <span>Wed</span>
            <span></span>
            <span>Fri</span>
            <span></span>
          </div>
          
          <!-- 格子区域 -->
          <div class="cells-container">
            <div 
              v-for="(week, wi) in weeksData" 
              :key="wi" 
              class="week-col"
            >
              <el-tooltip
                v-for="(day, di) in week"
                :key="di"
                :content="getTooltip(day)"
                placement="top"
                :disabled="!day.date"
              >
                <div 
                  class="cell"
                  :class="getCellClass(day)"
                ></div>
              </el-tooltip>
            </div>
          </div>
        </div>

      </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { interviewApi } from '@/api/interview'
import dayjs from 'dayjs'

const loading = ref(false)
const selectedYear = ref(dayjs().year())
const heatmapData = ref({
  totalDays: 0,
  currentStreak: 0,
  longestStreak: 0,
  monthStats: {},
  dailyData: []
})

// 年份选项
const yearOptions = computed(() => {
  const currentYear = dayjs().year()
  return [currentYear, currentYear - 1, currentYear - 2]
})

// 当月学习天数
const currentMonthDays = computed(() => {
  const currentMonth = dayjs().format('YYYY-MM')
  return heatmapData.value.monthStats?.[currentMonth] || 0
})

// 生成整年的周数据（简单直接的方式）
const weeksData = computed(() => {
  const year = selectedYear.value
  const weeks = []
  
  // 从1月1日开始
  let date = dayjs(`${year}-01-01`)
  const endDate = dayjs(`${year}-12-31`)
  
  // 找到第一周的开始（可能是去年的日期）
  const firstDayOfWeek = date.day() // 0=周日, 1=周一...
  // 调整为周一开始：周日=6, 周一=0, 周二=1...
  const offset = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1
  
  // 第一周
  let currentWeek = []
  
  // 填充第一周开始的空白
  for (let i = 0; i < offset; i++) {
    currentWeek.push({ date: null })
  }
  
  // 遍历整年
  while (date.isBefore(endDate) || date.isSame(endDate, 'day')) {
    currentWeek.push({
      date: date.format('YYYY-MM-DD')
    })
    
    // 如果当前周已经有7天，保存并开始新一周
    if (currentWeek.length === 7) {
      weeks.push(currentWeek)
      currentWeek = []
    }
    
    date = date.add(1, 'day')
  }
  
  // 保存最后一周（填充剩余空白）
  if (currentWeek.length > 0) {
    while (currentWeek.length < 7) {
      currentWeek.push({ date: null })
    }
    weeks.push(currentWeek)
  }
  
  return weeks
})

// 月份标签位置
const monthPositions = computed(() => {
  const year = selectedYear.value
  const positions = []
  const cellSize = 14 // 格子大小
  const gap = 3 // 间隔
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
  
  for (let m = 0; m < 12; m++) {
    const firstOfMonth = dayjs(`${year}-${String(m + 1).padStart(2, '0')}-01`)
    const startOfYear = dayjs(`${year}-01-01`)
    const daysDiff = firstOfMonth.diff(startOfYear, 'day')
    
    // 计算第一天的偏移
    const firstDayOfWeek = startOfYear.day()
    const offset = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1
    
    // 计算在第几周
    const weekIndex = Math.floor((daysDiff + offset) / 7)
    const left = weekIndex * (cellSize + gap)
    
    positions.push({
      month: m,
      name: monthNames[m],
      left
    })
  }
  
  return positions
})

// 获取日期的学习数据
const getDayData = (dateStr) => {
  if (!dateStr || !heatmapData.value.dailyData) return null
  return heatmapData.value.dailyData.find(d => d.date === dateStr)
}

// 获取格子的CSS类
const getCellClass = (day) => {
  if (!day.date) return 'empty'
  
  const data = getDayData(day.date)
  const level = data?.level || 0
  const isToday = day.date === dayjs().format('YYYY-MM-DD')
  
  return {
    [`level-${level}`]: true,
    'today': isToday
  }
}

// 获取提示文本
const getTooltip = (day) => {
  if (!day.date) return ''
  
  const data = getDayData(day.date)
  const count = data?.count || 0
  const dateStr = dayjs(day.date).format('YYYY年M月D日')
  
  return count > 0 
    ? `${dateStr}：学习了 ${count} 道题` 
    : `${dateStr}：无学习记录`
}

// 获取数据
const fetchHeatmap = async () => {
  loading.value = true
  try {
    const res = await interviewApi.getHeatmap(selectedYear.value)
    if (res) {
      heatmapData.value = res
    }
  } catch (error) {
    console.error('获取热力图失败', error)
  } finally {
    loading.value = false
  }
}

watch(selectedYear, fetchHeatmap)
onMounted(fetchHeatmap)
</script>

<style scoped>
.learning-heatmap-wrapper {
  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 50%, #14b8a6 100%);
  border-radius: 16px;
  padding: 20px;
  color: #fff;
}

/* 头部 */
.heatmap-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 32px;
}

.header-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  opacity: 0.9;
}

.header-subtitle strong {
  font-size: 16px;
}

.year-select {
  width: 90px;
}

.year-select :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.2);
  box-shadow: none;
}

.year-select :deep(.el-input__inner),
.year-select :deep(.el-select__caret) {
  color: #fff;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.stat-card {
  background: rgba(255,255,255,0.15);
  border-radius: 10px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  font-size: 24px;
}

.stat-info {
  min-width: 0;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-name {
  font-size: 11px;
  opacity: 0.85;
  white-space: nowrap;
}

/* 热力图区域 */
.heatmap-box {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
}

.heatmap-scroll {
  overflow-x: auto;
}

.heatmap-inner {
  min-width: 900px;
}

/* 月份行 */
.month-row {
  display: flex;
  margin-bottom: 6px;
  height: 16px;
}

.week-label-spacer {
  width: 32px;
  flex-shrink: 0;
}

.month-labels {
  position: relative;
  flex: 1;
  height: 16px;
}

.month-name {
  position: absolute;
  font-size: 11px;
  color: #57606a;
  white-space: nowrap;
}

/* 热力图主体 */
.heatmap-main {
  display: flex;
}

.week-labels {
  width: 32px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.week-labels span {
  height: 14px;
  line-height: 14px;
  font-size: 10px;
  color: #57606a;
}

.cells-container {
  display: flex;
  gap: 3px;
}

.week-col {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.cell {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  background: #ebedf0;
}

.cell.empty {
  background: transparent;
}

.cell.today {
  outline: 2px solid #fb7185;
  outline-offset: -1px;
}

.cell.level-0 { background: #ebedf0; }
.cell.level-1 { background: #9be9a8; }
.cell.level-2 { background: #40c463; }
.cell.level-3 { background: #30a14e; }
.cell.level-4 { background: #216e39; }

.cell:not(.empty):hover {
  transform: scale(1.3);
  outline: 2px solid #333;
  outline-offset: 1px;
}

/* 图例 */
.legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.legend-label {
  font-size: 11px;
  color: #57606a;
}

.legend-cell {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-card {
    padding: 10px;
  }
  
  .stat-icon {
    font-size: 20px;
  }
  
  .stat-value {
    font-size: 16px;
  }
}
</style>
