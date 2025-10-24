<template>
  <div class="codepen-statistics">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card class="stat-card stat-card-blue">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">作品总数</div>
            <div class="stat-value">{{ statistics.totalPens || 0 }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card stat-card-green">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">创作者总数</div>
            <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card stat-card-orange">
          <div class="stat-icon">
            <el-icon><View /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总浏览量</div>
            <div class="stat-value">{{ formatNumber(statistics.totalViews) }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card stat-card-red">
          <div class="stat-icon">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总点赞量</div>
            <div class="stat-value">{{ formatNumber(statistics.totalLikes) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card class="stat-card stat-card-purple">
          <div class="stat-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总评论量</div>
            <div class="stat-value">{{ formatNumber(statistics.totalComments) }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card stat-card-cyan">
          <div class="stat-icon">
            <el-icon><Collection /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总收藏量</div>
            <div class="stat-value">{{ formatNumber(statistics.totalCollects) }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card stat-card-pink">
          <div class="stat-icon">
            <el-icon><CopyDocument /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总Fork量</div>
            <div class="stat-value">{{ formatNumber(statistics.totalForks) }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card stat-card-yellow">
          <div class="stat-icon">
            <el-icon><Plus /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">今日新增</div>
            <div class="stat-value">{{ statistics.todayNewPens || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据概览</span>
              <el-button icon="Refresh" @click="loadStatistics">刷新数据</el-button>
            </div>
          </template>

          <el-descriptions :column="3" border>
            <el-descriptions-item label="作品总数">
              {{ statistics.totalPens || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="今日新增">
              {{ statistics.todayNewPens || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="创作者总数">
              {{ statistics.totalUsers || 0 }}
            </el-descriptions-item>

            <el-descriptions-item label="总浏览量">
              {{ formatNumber(statistics.totalViews) }}
            </el-descriptions-item>
            <el-descriptions-item label="总点赞量">
              {{ formatNumber(statistics.totalLikes) }}
            </el-descriptions-item>
            <el-descriptions-item label="总评论量">
              {{ formatNumber(statistics.totalComments) }}
            </el-descriptions-item>

            <el-descriptions-item label="总收藏量">
              {{ formatNumber(statistics.totalCollects) }}
            </el-descriptions-item>
            <el-descriptions-item label="总Fork量">
              {{ formatNumber(statistics.totalForks) }}
            </el-descriptions-item>
            <el-descriptions-item label="平均互动率">
              {{ calculateEngagementRate() }}%
            </el-descriptions-item>
          </el-descriptions>

          <div style="margin-top: 30px">
            <h3 style="margin-bottom: 15px">互动数据分布</h3>
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="chart-item">
                  <div class="chart-label">平均浏览量</div>
                  <div class="chart-value">{{ calculateAverage(statistics.totalViews, statistics.totalPens) }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="chart-item">
                  <div class="chart-label">平均点赞量</div>
                  <div class="chart-value">{{ calculateAverage(statistics.totalLikes, statistics.totalPens) }}</div>
                </div>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 15px">
              <el-col :span="12">
                <div class="chart-item">
                  <div class="chart-label">平均评论量</div>
                  <div class="chart-value">{{ calculateAverage(statistics.totalComments, statistics.totalPens) }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="chart-item">
                  <div class="chart-label">平均收藏量</div>
                  <div class="chart-value">{{ calculateAverage(statistics.totalCollects, statistics.totalPens) }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document, User, View, Star, ChatDotRound,
  Collection, CopyDocument, Plus
} from '@element-plus/icons-vue'
import { codepenApi } from '@/api/codepen'

// 统计数据
const statistics = ref({
  totalPens: 0,
  totalUsers: 0,
  todayNewPens: 0,
  totalViews: 0,
  totalLikes: 0,
  totalComments: 0,
  totalCollects: 0,
  totalForks: 0
})

// 加载统计数据
const loadStatistics = async () => {
  try {
    const result = await codepenApi.getStatistics()
    statistics.value = result || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}

// 计算平均值
const calculateAverage = (total, count) => {
  if (!count || count === 0) return 0
  return Math.round(total / count)
}

// 计算互动率
const calculateEngagementRate = () => {
  const total = statistics.value.totalPens || 0
  if (total === 0) return 0
  
  const engagements = 
    (statistics.value.totalLikes || 0) +
    (statistics.value.totalComments || 0) +
    (statistics.value.totalCollects || 0)
  
  const views = statistics.value.totalViews || 1
  
  return ((engagements / views) * 100).toFixed(2)
}

// 初始化
onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.codepen-statistics {
  .stat-card {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 20px;
    cursor: default;

    .stat-icon {
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      font-size: 28px;
      color: #fff;
    }

    .stat-content {
      flex: 1;

      .stat-label {
        font-size: 14px;
        color: #666;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #333;
      }
    }

    &.stat-card-blue .stat-icon {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.stat-card-green .stat-icon {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    &.stat-card-orange .stat-icon {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    }

    &.stat-card-red .stat-icon {
      background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
    }

    &.stat-card-purple .stat-icon {
      background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
    }

    &.stat-card-cyan .stat-icon {
      background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
    }

    &.stat-card-pink .stat-icon {
      background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
    }

    &.stat-card-yellow .stat-icon {
      background: linear-gradient(135deg, #ff6e7f 0%, #bfe9ff 100%);
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .chart-item {
    background: #f5f7fa;
    padding: 20px;
    border-radius: 8px;
    text-align: center;

    .chart-label {
      font-size: 14px;
      color: #666;
      margin-bottom: 10px;
    }

    .chart-value {
      font-size: 24px;
      font-weight: 600;
      color: #333;
    }
  }
}
</style>

