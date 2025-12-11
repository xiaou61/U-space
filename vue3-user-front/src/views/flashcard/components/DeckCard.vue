<template>
  <div class="deck-card" :class="{ 'official': isOfficial }">
    <div class="deck-header">
      <div class="deck-icon">{{ getDeckIcon(deck.category) }}</div>
      <div class="deck-title">
        <h4>{{ deck.name }}</h4>
        <span class="deck-category">{{ deck.categoryName || '自定义' }}</span>
      </div>
      <el-dropdown v-if="!isOfficial" trigger="click" @command="handleAction">
        <el-icon class="more-btn"><MoreFilled /></el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="add-card">
              <el-icon><Plus /></el-icon>添加卡片
            </el-dropdown-item>
            <el-dropdown-item command="edit">
              <el-icon><Edit /></el-icon>编辑
            </el-dropdown-item>
            <el-dropdown-item command="delete" divided>
              <el-icon><Delete /></el-icon>删除
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <span v-if="isOfficial" class="official-badge">官方</span>
    </div>
    
    <p class="deck-desc" v-if="deck.description">{{ deck.description }}</p>
    
    <div class="deck-stats">
      <div class="stat-item">
        <span class="stat-value">{{ deck.cardCount || 0 }}</span>
        <span class="stat-label">总卡片</span>
      </div>
      <div class="stat-item">
        <span class="stat-value new">{{ deck.newCount || 0 }}</span>
        <span class="stat-label">新卡片</span>
      </div>
      <div class="stat-item">
        <span class="stat-value review">{{ deck.reviewCount || 0 }}</span>
        <span class="stat-label">待复习</span>
      </div>
      <div class="stat-item">
        <span class="stat-value mastered">{{ deck.masteredCount || 0 }}</span>
        <span class="stat-label">已掌握</span>
      </div>
    </div>
    
    <div class="deck-progress" v-if="deck.cardCount > 0">
      <el-progress 
        :percentage="masteryPercent" 
        :stroke-width="6"
        :show-text="false"
        color="#67c23a"
      />
      <span class="progress-text">掌握 {{ masteryPercent }}%</span>
    </div>
    
    <div class="deck-footer">
      <button 
        class="study-btn"
        :disabled="(deck.newCount || 0) + (deck.reviewCount || 0) === 0"
        @click="$emit('study', deck)"
      >
        <el-icon><VideoPlay /></el-icon>
        {{ (deck.newCount || 0) + (deck.reviewCount || 0) > 0 ? '开始学习' : '已完成' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { MoreFilled, Plus, Edit, Delete, VideoPlay } from '@element-plus/icons-vue'

const props = defineProps({
  deck: {
    type: Object,
    required: true
  },
  isOfficial: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['study', 'edit', 'delete', 'add-card'])

// 掌握百分比
const masteryPercent = computed(() => {
  if (!props.deck.cardCount) return 0
  return Math.round((props.deck.masteredCount / props.deck.cardCount) * 100)
})

// 获取卡组图标
const getDeckIcon = (category) => {
  const iconMap = {
    'java': '☕',
    'spring': '🌿',
    'mysql': '🐬',
    'redis': '🔴',
    'network': '🌐',
    'os': '💻',
    'algorithm': '🧮',
    'frontend': '🎨',
    'default': '📚'
  }
  return iconMap[category] || iconMap['default']
}

// 处理操作
const handleAction = (command) => {
  switch (command) {
    case 'edit':
      emit('edit', props.deck)
      break
    case 'delete':
      emit('delete', props.deck)
      break
    case 'add-card':
      emit('add-card', props.deck)
      break
  }
}
</script>

<style lang="scss" scoped>
.deck-card {
  background: #f8f9fc;
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s;
  border: 2px solid transparent;
  
  &:hover {
    border-color: #667eea;
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  }
  
  &.official {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
    border-color: rgba(102, 126, 234, 0.3);
  }
}

.deck-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.deck-icon {
  font-size: 32px;
  line-height: 1;
}

.deck-title {
  flex: 1;
  
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 4px 0;
    color: #333;
  }
  
  .deck-category {
    font-size: 12px;
    color: #999;
  }
}

.more-btn {
  cursor: pointer;
  color: #999;
  font-size: 18px;
  padding: 4px;
  
  &:hover {
    color: #409eff;
  }
}

.official-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.deck-desc {
  font-size: 13px;
  color: #666;
  margin: 0 0 16px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.deck-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
  
  .stat-value {
    display: block;
    font-size: 18px;
    font-weight: bold;
    color: #333;
    
    &.new { color: #409eff; }
    &.review { color: #e6a23c; }
    &.mastered { color: #67c23a; }
  }
  
  .stat-label {
    font-size: 11px;
    color: #999;
  }
}

.deck-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  
  .el-progress {
    flex: 1;
  }
  
  .progress-text {
    font-size: 12px;
    color: #67c23a;
    font-weight: 500;
    white-space: nowrap;
  }
}

.deck-footer {
  display: flex;
  justify-content: center;
}

.study-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }
  
  &:disabled {
    background: #c0c4cc;
    cursor: not-allowed;
  }
}
</style>
