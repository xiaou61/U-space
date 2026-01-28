<template>
  <div class="flashcard-index">
    <!-- 左侧边栏 -->
    <aside class="sidebar">
      <!-- 搜索框 -->
      <div class="sidebar-section search-section">
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索卡组..." 
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>

      <!-- 快捷功能 -->
      <div class="sidebar-section quick-actions">
        <div class="section-title">
          <el-icon><Lightning /></el-icon>
          <span>快捷功能</span>
        </div>
        <div class="action-buttons">
          <div class="action-btn" @click="goToStudy">
            <el-icon class="action-icon study-icon"><Reading /></el-icon>
            <span>今日学习</span>
            <el-badge v-if="stats?.todayDueCount" :value="stats.todayDueCount" class="due-badge" />
          </div>
          <div class="action-btn" @click="goToMyDecks">
            <el-icon class="action-icon deck-icon"><Collection /></el-icon>
            <span>我的卡组</span>
          </div>
          <div class="action-btn" @click="goToCreate">
            <el-icon class="action-icon create-icon"><Plus /></el-icon>
            <span>创建卡组</span>
          </div>
        </div>
      </div>

      <!-- 学习统计 -->
      <StudyStats :stats="stats" v-if="isLoggedIn" />
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 学习热力图 -->
      <Heatmap :data="heatmapData" class="heatmap-section" v-if="isLoggedIn" />

      <!-- 内容头部 -->
      <div class="content-header">
        <div class="header-left">
          <h2 class="page-title">公开卡组</h2>
          <span class="total-badge" v-if="deckList.length > 0">{{ deckList.length }} 个卡组</span>
        </div>
        <div class="header-right">
          <el-input 
            v-model="filterTags" 
            placeholder="按标签筛选" 
            style="width: 160px"
            clearable
            @change="handleSearch"
          >
            <template #prefix>
              <el-icon><PriceTag /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 卡组网格 -->
      <div v-loading="loading" class="deck-grid">
        <DeckCard 
          v-for="deck in deckList" 
          :key="deck.id"
          :deck="deck"
          :show-progress="isLoggedIn"
          @click="goToDeckDetail(deck)"
        />
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-if="!loading && deckList.length === 0" 
        description="暂无公开卡组"
        :image-size="120"
      >
        <el-button type="primary" @click="goToCreate">创建第一个卡组</el-button>
      </el-empty>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Lightning, Reading, Collection, Plus, PriceTag } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { flashcardApi } from '@/api/flashcard'
import DeckCard from './components/DeckCard.vue'
import Heatmap from './components/Heatmap.vue'
import StudyStats from './components/StudyStats.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const searchKeyword = ref('')
const filterTags = ref('')
const deckList = ref([])
const stats = ref(null)
const heatmapData = ref([])

const isLoggedIn = computed(() => userStore.isLoggedIn)

// 加载公开卡组列表
const loadPublicDecks = async () => {
  loading.value = true
  try {
    const data = await flashcardApi.getPublicDecks(searchKeyword.value, filterTags.value)
    deckList.value = data || []
  } catch (error) {
    console.error('加载卡组失败:', error)
    ElMessage.error('加载卡组失败')
  } finally {
    loading.value = false
  }
}

// 加载学习统计
const loadStats = async () => {
  if (!isLoggedIn.value) return
  try {
    stats.value = await flashcardApi.getStudyStats()
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

// 加载热力图数据
const loadHeatmap = async () => {
  if (!isLoggedIn.value) return
  try {
    const data = await flashcardApi.getHeatmap(365)
    heatmapData.value = data?.data || []
  } catch (error) {
    console.error('加载热力图失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  loadPublicDecks()
}

// 导航
const goToStudy = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/flashcard/study')
}

const goToMyDecks = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/flashcard/my')
}

const goToCreate = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/flashcard/deck/create')
}

const goToDeckDetail = (deck) => {
  router.push(`/flashcard/deck/${deck.id}`)
}

onMounted(() => {
  loadPublicDecks()
  loadStats()
  loadHeatmap()
})
</script>

<style lang="scss" scoped>
.flashcard-index {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 60px);
}

.sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-section {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
}

.quick-actions {
  .action-buttons {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .action-btn {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;
    
    &:hover {
      background: var(--el-fill-color-light);
    }
    
    .action-icon {
      width: 32px;
      height: 32px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
    }
    
    .study-icon {
      background: rgba(64, 158, 255, 0.1);
      color: var(--el-color-primary);
    }
    
    .deck-icon {
      background: rgba(103, 194, 58, 0.1);
      color: var(--el-color-success);
    }
    
    .create-icon {
      background: rgba(230, 162, 60, 0.1);
      color: var(--el-color-warning);
    }
    
    span {
      font-size: 14px;
      color: var(--el-text-color-primary);
    }
    
    .due-badge {
      position: absolute;
      right: 12px;
    }
  }
}

.main-content {
  flex: 1;
  min-width: 0;
}

.heatmap-section {
  margin-bottom: 24px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .page-title {
      font-size: 20px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin: 0;
    }
    
    .total-badge {
      padding: 4px 12px;
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      border-radius: 12px;
      font-size: 12px;
    }
  }
}

.deck-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 200px;
}

@media (max-width: 900px) {
  .flashcard-index {
    flex-direction: column;
    padding: 16px;
  }
  
  .sidebar {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
    
    .sidebar-section {
      flex: 1;
      min-width: 280px;
    }
  }
  
  .deck-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}
</style>
