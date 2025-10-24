<template>
  <div class="pen-card" @click="handleClick">
    <!-- 预览图 -->
    <div class="pen-preview">
      <div class="preview-placeholder">
        <el-icon><Document /></el-icon>
        <span>{{ pen.title }}</span>
      </div>
      
      <!-- 付费标识 -->
      <div class="price-badge" v-if="!pen.isFree">
        <el-icon><Lock /></el-icon>
        {{ pen.forkPrice }} 积分
      </div>
    </div>

    <!-- 作品信息 -->
    <div class="pen-info">
      <h3 class="pen-title">{{ pen.title }}</h3>
      <p class="pen-description" v-if="pen.description">
        {{ pen.description }}
      </p>

      <!-- 标签 -->
      <div class="pen-tags" v-if="pen.tags && pen.tags.length > 0">
        <el-tag
          v-for="(tag, index) in pen.tags.slice(0, 3)"
          :key="index"
          size="small"
          type="info"
        >
          {{ tag }}
        </el-tag>
      </div>

      <!-- 作者信息 -->
      <div class="pen-author">
        <el-avatar :size="24" :src="pen.userAvatar">
          {{ pen.userNickname?.charAt(0) }}
        </el-avatar>
        <span class="author-name">{{ pen.userNickname }}</span>
      </div>

      <!-- 统计信息 -->
      <div class="pen-stats">
        <span class="stat-item">
          <el-icon><View /></el-icon>
          {{ formatNumber(pen.viewCount) }}
        </span>
        <span class="stat-item">
          <el-icon><Star /></el-icon>
          {{ formatNumber(pen.likeCount) }}
        </span>
        <span class="stat-item">
          <el-icon><Collection /></el-icon>
          {{ formatNumber(pen.collectCount) }}
        </span>
        <span class="stat-item" v-if="pen.forkCount > 0">
          <el-icon><CopyDocument /></el-icon>
          {{ formatNumber(pen.forkCount) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Document, Lock, View, Star, Collection, CopyDocument } from '@element-plus/icons-vue'

const props = defineProps({
  pen: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click', props.pen)
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
</script>

<style scoped lang="scss">
.pen-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  }

  .pen-preview {
    position: relative;
    height: 200px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    overflow: hidden;

    .preview-placeholder {
      text-align: center;
      padding: 20px;

      .el-icon {
        font-size: 48px;
        margin-bottom: 10px;
      }

      span {
        display: block;
        font-size: 16px;
        font-weight: 500;
      }
    }

    .price-badge {
      position: absolute;
      top: 10px;
      right: 10px;
      background: rgba(255, 193, 7, 0.95);
      color: #333;
      padding: 5px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .pen-info {
    padding: 15px;

    .pen-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin: 0 0 8px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .pen-description {
      font-size: 14px;
      color: #666;
      margin: 0 0 12px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      line-height: 1.5;
      max-height: 3em;
    }

    .pen-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-bottom: 12px;
    }

    .pen-author {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 1px solid #f0f0f0;

      .author-name {
        font-size: 14px;
        color: #666;
      }
    }

    .pen-stats {
      display: flex;
      gap: 15px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #999;

        .el-icon {
          font-size: 16px;
        }
      }
    }
  }
}
</style>

