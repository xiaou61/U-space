<template>
  <div class="moments-page">

    
    <!-- 发布动态区域 -->
    <el-card class="publish-card">
      <div class="publish-section">
        <el-input
          v-model="publishForm.content"
          type="textarea"
          placeholder="分享此刻的想法..."
          :rows="3"
          maxlength="100"
          show-word-limit
          @focus="showPublishDialog"
          readonly
        />
      </div>
    </el-card>

    <!-- 动态列表 -->
    <div v-loading="loading" class="moments-list">
      <el-empty v-if="!loading && momentList.length === 0" description="暂无动态，快来发布第一条吧！" />
      
      <div v-for="moment in momentList" :key="moment.id" class="moment-item">
        <el-card class="moment-card">
          <!-- 用户信息 -->
          <div class="user-header">
            <el-avatar :size="45" :src="moment.userAvatar">
              {{ moment.userNickname?.charAt(0) }}
            </el-avatar>
            <div class="user-info">
              <div class="user-name">{{ moment.userNickname }}</div>
              <div class="publish-time">{{ moment.createTime }}</div>
            </div>
            <el-dropdown v-if="moment.canDelete" trigger="click">
              <el-button type="text" size="small">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="deleteMoment(moment)">删除动态</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 动态内容 -->
          <div class="moment-content">
            <p class="content-text">{{ moment.content }}</p>
            
            <!-- 图片展示 -->
            <div v-if="moment.images && moment.images.length" class="images-grid">
              <div 
                v-for="(image, index) in moment.images" 
                :key="index" 
                class="image-item"
                @click="previewImage(moment.images, index)"
              >
                <el-image
                  :src="image"
                  fit="cover"
                  class="moment-image"
                  loading="lazy"
                />
              </div>
            </div>
          </div>

          <!-- 互动区域 -->
          <div class="interaction-section">
            <div class="interaction-stats" v-if="moment.likeCount > 0 || moment.commentCount > 0">
              <span v-if="moment.likeCount > 0" class="like-count">
                <el-icon><Star /></el-icon>
                {{ moment.likeCount }} 人点赞
              </span>
              <span v-if="moment.commentCount > 0" class="comment-count">
                {{ moment.commentCount }} 条评论
              </span>
            </div>
            
            <div class="interaction-buttons">
                             <el-button 
                 :type="moment.isLiked ? 'primary' : ''" 
                 text 
                 @click="toggleLikeMoment(moment)"
                 :loading="moment.liking"
               >
                 <el-icon><Star /></el-icon>
                 {{ moment.isLiked ? '已赞' : '点赞' }}
               </el-button>
              <el-button text @click="showCommentDialog(moment)">
                <el-icon><ChatDotRound /></el-icon>
                评论
              </el-button>
            </div>

            <!-- 最新评论 -->
            <div v-if="moment.recentComments && moment.recentComments.length" class="recent-comments">
              <div 
                v-for="comment in moment.recentComments" 
                :key="comment.id" 
                class="comment-item"
              >
                <span class="comment-user">{{ comment.userNickname }}:</span>
                <span class="comment-content">{{ comment.content }}</span>
                <el-button 
                  v-if="comment.canDelete" 
                  type="danger" 
                  text 
                  size="small"
                  @click="deleteComment(comment, moment)"
                >
                  删除
                </el-button>
              </div>
              <el-button 
                v-if="moment.commentCount > moment.recentComments.length" 
                text 
                size="small" 
                @click="viewAllComments(moment)"
              >
                查看全部 {{ moment.commentCount }} 条评论
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <el-button @click="loadMore" :loading="loadingMore">
          加载更多
        </el-button>
      </div>
    </div>

    <!-- 发布动态对话框 -->
    <PublishDialog
      v-model="publishDialogVisible"
      @published="handlePublished"
    />

    <!-- 评论对话框 -->
    <CommentDialog
      v-model="commentDialogVisible"
      :moment="currentMoment"
      @commented="handleCommented"
    />

    <!-- 查看全部评论对话框 -->
    <AllCommentsDialog
      v-model="allCommentsDialogVisible"
      :moment="currentMoment"
      @comment-deleted="handleCommentDeleted"
    />

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="imageViewerVisible"
      :url-list="previewImages"
      :initial-index="previewIndex"
      @close="closeImageViewer"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElImageViewer } from 'element-plus'
import { Star, ChatDotRound, MoreFilled } from '@element-plus/icons-vue'
import { getMomentList, toggleLike, deleteMoment as deleteMomentApi, deleteComment as deleteCommentApi } from '@/api/moment'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import PublishDialog from './components/PublishDialog.vue'
import CommentDialog from './components/CommentDialog.vue'
import AllCommentsDialog from './components/AllCommentsDialog.vue'

const userStore = useUserStore()
const router = useRouter()

// 数据状态
const loading = ref(false)
const loadingMore = ref(false)
const momentList = ref([])
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)

// 发布相关
const publishForm = ref({
  content: ''
})
const publishDialogVisible = ref(false)

// 评论相关
const commentDialogVisible = ref(false)
const allCommentsDialogVisible = ref(false)
const currentMoment = ref(null)

// 图片预览
const imageViewerVisible = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)

// 加载动态列表
const loadMomentList = async (page = 1) => {
  if (page === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const params = {
      pageNum: page,
      pageSize: pageSize.value
    }

    const result = await getMomentList(params)
    const newMoments = result.records.map(moment => ({
      ...moment,
      images: moment.images || [],
      liking: false
    }))

    if (page === 1) {
      momentList.value = newMoments
    } else {
      momentList.value.push(...newMoments)
    }

    hasMore.value = result.records.length === pageSize.value
  } catch (error) {
    ElMessage.error('加载失败：' + error.message)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}



// 加载更多
const loadMore = () => {
  currentPage.value++
  loadMomentList(currentPage.value)
}

// 显示发布对话框
const showPublishDialog = () => {
  publishDialogVisible.value = true
}

// 处理发布成功
const handlePublished = () => {
  publishDialogVisible.value = false
  currentPage.value = 1
  loadMomentList(1)
}

// 点赞切换
const toggleLikeMoment = async (moment) => {
  moment.liking = true
  try {
    const isLiked = await toggleLike(moment.id)
    moment.isLiked = isLiked
    moment.likeCount += isLiked ? 1 : -1
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    moment.liking = false
  }
}

// 删除动态
const deleteMoment = async (moment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条动态吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteMomentApi(moment.id)
    ElMessage.success('删除成功')
    
    // 从列表中移除
    const index = momentList.value.findIndex(item => item.id === moment.id)
    if (index > -1) {
      momentList.value.splice(index, 1)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 显示评论对话框
const showCommentDialog = (moment) => {
  currentMoment.value = moment
  commentDialogVisible.value = true
}

// 处理评论成功
const handleCommented = (comment) => {
  const moment = currentMoment.value
  if (moment) {
    moment.commentCount++
    if (!moment.recentComments) {
      moment.recentComments = []
    }
    moment.recentComments.unshift(comment)
    // 保持最新评论只显示3条
    if (moment.recentComments.length > 3) {
      moment.recentComments = moment.recentComments.slice(0, 3)
    }
  }
}

// 删除评论
const deleteComment = async (comment, moment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteCommentApi(comment.id)
    ElMessage.success('删除成功')
    
    // 从最新评论中移除
    const commentIndex = moment.recentComments.findIndex(item => item.id === comment.id)
    if (commentIndex > -1) {
      moment.recentComments.splice(commentIndex, 1)
      moment.commentCount--
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 查看全部评论
const viewAllComments = (moment) => {
  currentMoment.value = moment
  allCommentsDialogVisible.value = true
}

// 处理评论删除（从全部评论对话框触发）
const handleCommentDeleted = (commentId) => {
  const moment = currentMoment.value
  if (moment) {
    moment.commentCount--
    // 如果删除的评论在最新评论中，也要移除
    const index = moment.recentComments.findIndex(item => item.id === commentId)
    if (index > -1) {
      moment.recentComments.splice(index, 1)
    }
  }
}

// 图片预览
const previewImage = (images, index) => {
  previewImages.value = images
  previewIndex.value = index
  imageViewerVisible.value = true
}

const closeImageViewer = () => {
  imageViewerVisible.value = false
}



onMounted(() => {
  loadMomentList()
})
</script>

<style scoped>
.moments-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}



.publish-card {
  margin-top: 20px;
  margin-bottom: 20px;
}

.publish-section {
  cursor: pointer;
}

.moments-list {
  min-height: 400px;
}

.moment-item {
  margin-bottom: 20px;
}

.moment-card {
  transition: box-shadow 0.2s;
}

.moment-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  flex: 1;
  margin-left: 12px;
}

.user-name {
  font-weight: 500;
  color: #333;
  font-size: 16px;
}

.publish-time {
  color: #999;
  font-size: 12px;
  margin-top: 2px;
}

.moment-content {
  margin-bottom: 15px;
}

.content-text {
  margin: 0 0 15px 0;
  line-height: 1.6;
  color: #333;
}

.images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 15px;
  max-width: 360px;
}

.image-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  width: 116px;
  height: 116px;
}

.moment-image {
  width: 100%;
  height: 100%;
}

.interaction-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.interaction-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #999;
}

.like-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

.interaction-buttons {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.recent-comments {
  background: #f7f7f7;
  border-radius: 8px;
  padding: 10px;
  margin-top: 10px;
}

.comment-item {
  margin-bottom: 8px;
  font-size: 14px;
  line-height: 1.4;
}

.comment-item:last-child {
  margin-bottom: 0;
}

.comment-user {
  font-weight: 500;
  color: #1890ff;
  margin-right: 5px;
}

.comment-content {
  color: #333;
}

.load-more {
  text-align: center;
  padding: 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-textarea__inner) {
  resize: none;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .images-grid {
    max-width: 300px;
  }
  
  .image-item {
    width: 96px;
    height: 96px;
  }
}
</style> 