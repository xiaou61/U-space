<template>
  <div class="my-pens-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">我的作品</h1>
      <div class="header-actions">
        <el-button 
          type="primary" 
          icon="Plus" 
          @click="createNewPen"
        >
          创建作品
        </el-button>
        <el-button 
          icon="TrendCharts" 
          @click="showIncomeStats"
        >
          收益统计
        </el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部作品" name="all">
        <div class="pen-list" v-loading="loading">
          <div class="pen-grid">
            <PenCard
              v-for="pen in penList"
              :key="pen.id"
              :pen="pen"
              @click="viewPen(pen.id)"
            />
          </div>

          <el-empty
            v-if="!loading && penList.length === 0"
            description="还没有作品，快去创建吧"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="草稿箱" name="draft">
        <div class="pen-list" v-loading="loading">
          <div class="pen-grid">
            <PenCard
              v-for="pen in draftList"
              :key="pen.id"
              :pen="pen"
              @click="editPen(pen.id)"
            />
          </div>

          <el-empty
            v-if="!loading && draftList.length === 0"
            description="暂无草稿"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="付费作品" name="paid">
        <div class="pen-list" v-loading="loading">
          <div class="pen-grid">
            <div
              v-for="pen in paidPens"
              :key="pen.id"
              class="paid-pen-card"
              @click="viewPen(pen.id)"
            >
              <PenCard :pen="pen" />
              <div class="income-badge">
                收益: {{ pen.totalIncome || 0 }} 积分
              </div>
            </div>
          </div>

          <el-empty
            v-if="!loading && paidPens.length === 0"
            description="暂无付费作品"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 收益统计对话框 -->
    <el-dialog
      v-model="showIncomeDialog"
      title="收益统计"
      width="600px"
    >
      <div class="income-stats" v-loading="loadingStats">
        <div class="stat-card">
          <div class="stat-label">总收益</div>
          <div class="stat-value">{{ incomeStats.totalIncome || 0 }} 积分</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">付费作品数</div>
          <div class="stat-value">{{ incomeStats.paidPensCount || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">总Fork次数</div>
          <div class="stat-value">{{ incomeStats.totalForkCount || 0 }}</div>
        </div>

        <!-- 详细收益列表 -->
        <div class="income-details" v-if="incomeStats.details && incomeStats.details.length > 0">
          <h3>作品收益详情</h3>
          <el-table :data="incomeStats.details" style="width: 100%">
            <el-table-column prop="title" label="作品标题" />
            <el-table-column prop="forkPrice" label="Fork价格" width="100">
              <template #default="{ row }">
                {{ row.forkPrice }} 积分
              </template>
            </el-table-column>
            <el-table-column prop="forkCount" label="Fork次数" width="100" />
            <el-table-column prop="totalIncome" label="收益" width="100">
              <template #default="{ row }">
                {{ row.totalIncome }} 积分
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { codepenApi } from '@/api/codepen'
import PenCard from './components/PenCard.vue'

const router = useRouter()

// 标签页
const activeTab = ref('all')
const loading = ref(false)

// 作品列表
const allPens = ref([])
const draftList = ref([])

// 计算付费作品
const paidPens = computed(() => {
  return allPens.value.filter(pen => !pen.isFree)
})

// 当前显示的列表
const penList = computed(() => {
  if (activeTab.value === 'draft') {
    return draftList.value
  } else if (activeTab.value === 'paid') {
    return paidPens.value
  }
  return allPens.value
})

// 收益统计
const showIncomeDialog = ref(false)
const loadingStats = ref(false)
const incomeStats = ref({})

// 创建新作品
const createNewPen = () => {
  router.push('/codepen/editor')
}

// 查看作品
const viewPen = (id) => {
  router.push(`/codepen/${id}`)
}

// 编辑作品
const editPen = (id) => {
  router.push(`/codepen/editor/${id}`)
}

// 标签页切换
const handleTabChange = (tab) => {
  if (tab === 'draft' && draftList.value.length === 0) {
    loadDrafts()
  }
}

// 加载全部作品
const loadAllPens = async () => {
  try {
    loading.value = true
    allPens.value = await codepenApi.getMyPens({ status: 1 })
  } catch (error) {
    console.error('加载作品失败:', error)
    ElMessage.error('加载作品失败')
  } finally {
    loading.value = false
  }
}

// 加载草稿
const loadDrafts = async () => {
  try {
    loading.value = true
    draftList.value = await codepenApi.getMyDrafts()
  } catch (error) {
    console.error('加载草稿失败:', error)
  } finally {
    loading.value = false
  }
}

// 显示收益统计
const showIncomeStats = async () => {
  showIncomeDialog.value = true
  try {
    loadingStats.value = true
    incomeStats.value = await codepenApi.getIncomeStats()
  } catch (error) {
    console.error('加载收益统计失败:', error)
  } finally {
    loadingStats.value = false
  }
}

// 初始化
onMounted(() => {
  loadAllPens()
})
</script>

<style scoped lang="scss">
.my-pens-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;

    .page-title {
      font-size: 32px;
      font-weight: 600;
      color: #333;
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .pen-list {
    min-height: 400px;

    .pen-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      margin-bottom: 30px;

      .paid-pen-card {
        position: relative;
        cursor: pointer;

        .income-badge {
          position: absolute;
          top: 10px;
          right: 10px;
          background: rgba(255, 193, 7, 0.95);
          color: #333;
          padding: 5px 12px;
          border-radius: 20px;
          font-size: 12px;
          font-weight: 600;
          z-index: 10;
        }
      }
    }
  }

  .income-stats {
    .stat-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 15px;
      text-align: center;

      .stat-label {
        font-size: 14px;
        opacity: 0.9;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 32px;
        font-weight: 600;
      }
    }

    .income-details {
      margin-top: 20px;

      h3 {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 15px;
      }
    }
  }
}
</style>

