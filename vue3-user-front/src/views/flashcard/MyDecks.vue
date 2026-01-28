<template>
  <div class="my-decks">
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" text @click="$router.push('/flashcard')">返回</el-button>
        <h1 class="page-title">我的卡组</h1>
        <el-badge :value="deckList.length" class="deck-count" />
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="goToCreate">创建卡组</el-button>
      </div>
    </div>

    <div v-loading="loading" class="deck-list">
      <DeckCard 
        v-for="deck in deckList" 
        :key="deck.id"
        :deck="deck"
        :show-progress="true"
        @click="goToDeckDetail(deck)"
      />
    </div>

    <el-empty 
      v-if="!loading && deckList.length === 0" 
      description="还没有卡组，创建一个吧"
      :image-size="150"
    >
      <el-button type="primary" @click="goToCreate">创建第一个卡组</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { flashcardApi } from '@/api/flashcard'
import DeckCard from './components/DeckCard.vue'

const router = useRouter()
const loading = ref(false)
const deckList = ref([])

const loadMyDecks = async () => {
  loading.value = true
  try {
    const data = await flashcardApi.getMyDecks()
    deckList.value = data || []
  } catch (error) {
    console.error('加载卡组失败:', error)
    ElMessage.error('加载卡组失败')
  } finally {
    loading.value = false
  }
}

const goToCreate = () => {
  router.push('/flashcard/deck/create')
}

const goToDeckDetail = (deck) => {
  router.push(`/flashcard/deck/${deck.id}`)
}

onMounted(() => {
  loadMyDecks()
})
</script>

<style lang="scss" scoped>
.my-decks {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      margin: 0;
      color: var(--el-text-color-primary);
    }
    
    .deck-count {
      :deep(.el-badge__content) {
        font-size: 12px;
      }
    }
  }
}

.deck-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 200px;
}

@media (max-width: 640px) {
  .my-decks {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
    
    .header-right {
      width: 100%;
      
      .el-button {
        width: 100%;
      }
    }
  }
  
  .deck-list {
    grid-template-columns: 1fr;
  }
}
</style>
