<template>
  <div class="team-card" @click="goToDetail">
    <!-- 小组头像 -->
    <div class="team-avatar">
      <img v-if="team.teamAvatar" :src="team.teamAvatar" :alt="team.teamName" />
      <span v-else class="avatar-text">{{ team.teamName?.charAt(0) || '组' }}</span>
      <span class="team-type-badge" :class="getTypeClass(team.teamType)">
        {{ getTypeText(team.teamType) }}
      </span>
    </div>

    <!-- 小组信息 -->
    <div class="team-info">
      <div class="team-header">
        <h3 class="team-name">{{ team.teamName }}</h3>
        <span v-if="team.status === 2" class="status-tag full">已满员</span>
      </div>
      
      <p class="team-desc">{{ team.teamDesc || '暂无简介' }}</p>
      
      <!-- 标签 -->
      <div class="team-tags" v-if="team.tags">
        <span 
          v-for="(tag, index) in parseTags(team.tags)" 
          :key="index" 
          class="tag"
        >
          {{ tag }}
        </span>
      </div>

      <!-- 统计数据 -->
      <div class="team-stats">
        <div class="stat-item">
          <el-icon><User /></el-icon>
          <span>{{ team.currentMembers }}/{{ team.maxMembers }}</span>
        </div>
        <div class="stat-item">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatDate(team.createTime) }}</span>
        </div>
        <div class="stat-item" v-if="team.totalCheckins">
          <el-icon><Check /></el-icon>
          <span>{{ team.totalCheckins }}次打卡</span>
        </div>
      </div>
    </div>

    <!-- 操作区域 -->
    <div class="team-action" @click.stop>
      <template v-if="!team.joined">
        <el-button 
          v-if="team.joinType === 1 && team.status !== 2" 
          type="primary" 
          size="small"
          @click="handleJoin"
          :loading="joining"
        >
          加入
        </el-button>
        <el-button 
          v-else-if="team.joinType === 2 && team.status !== 2" 
          type="primary" 
          size="small"
          plain
          @click="handleApply"
          :loading="joining"
        >
          申请
        </el-button>
        <el-button 
          v-else-if="team.joinType === 3" 
          type="info" 
          size="small"
          plain
          disabled
        >
          仅邀请
        </el-button>
        <el-button 
          v-else-if="team.status === 2" 
          type="info" 
          size="small"
          plain
          disabled
        >
          已满员
        </el-button>
      </template>
      <template v-else>
        <span class="joined-badge">
          <el-icon><SuccessFilled /></el-icon>
          已加入
        </span>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Calendar, Check, SuccessFilled } from '@element-plus/icons-vue'
import teamApi from '@/api/team'

const props = defineProps({
  team: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['refresh', 'apply'])

const router = useRouter()
const joining = ref(false)

// 跳转到详情
const goToDetail = () => {
  router.push(`/team/${props.team.id}`)
}

// 直接加入（公开小组）
const handleJoin = async () => {
  joining.value = true
  try {
    await teamApi.applyJoin(props.team.id)
    ElMessage.success('加入成功')
    emit('refresh')
  } catch (error) {
    console.error('加入失败:', error)
  } finally {
    joining.value = false
  }
}

// 申请加入
const handleApply = () => {
  emit('apply', props.team)
}

// 解析标签
const parseTags = (tags) => {
  if (!tags) return []
  return tags.split(',').filter(t => t.trim()).slice(0, 3)
}

// 获取类型文本
const getTypeText = (type) => {
  const typeMap = {
    1: '目标型',
    2: '学习型',
    3: '打卡型'
  }
  return typeMap[type] || '学习型'
}

// 获取类型样式类
const getTypeClass = (type) => {
  const classMap = {
    1: 'type-goal',
    2: 'type-study',
    3: 'type-checkin'
  }
  return classMap[type] || 'type-study'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}
</script>

<style lang="scss" scoped>
.team-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #eee;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.team-avatar {
  position: relative;
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .avatar-text {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    font-size: 24px;
    font-weight: bold;
    color: white;
  }
  
  .team-type-badge {
    position: absolute;
    bottom: -2px;
    left: 50%;
    transform: translateX(-50%);
    padding: 2px 8px;
    font-size: 10px;
    border-radius: 8px;
    white-space: nowrap;
    
    &.type-goal {
      background: #e8f4fd;
      color: #409eff;
    }
    &.type-study {
      background: #f0f9eb;
      color: #67c23a;
    }
    &.type-checkin {
      background: #fdf2e9;
      color: #e6a23c;
    }
  }
}

.team-info {
  flex: 1;
  min-width: 0;
}

.team-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.team-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 4px;
  
  &.full {
    background: #fef0f0;
    color: #f56c6c;
  }
}

.team-desc {
  font-size: 13px;
  color: #666;
  margin: 0 0 10px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.team-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
  
  .tag {
    padding: 2px 8px;
    font-size: 12px;
    background: #f5f7fa;
    color: #909399;
    border-radius: 4px;
  }
}

.team-stats {
  display: flex;
  gap: 16px;
  
  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #999;
    
    .el-icon {
      font-size: 14px;
    }
  }
}

.team-action {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  
  .joined-badge {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #67c23a;
    font-size: 13px;
    
    .el-icon {
      font-size: 16px;
    }
  }
}

@media (max-width: 600px) {
  .team-card {
    flex-direction: column;
    
    .team-avatar {
      width: 48px;
      height: 48px;
      
      .avatar-text {
        font-size: 18px;
      }
    }
    
    .team-action {
      align-self: flex-end;
    }
  }
}
</style>
