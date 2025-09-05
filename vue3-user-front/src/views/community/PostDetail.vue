<template>
  <div class="post-detail">
    <!-- 返回按钮 -->
    <div class="back-section">
      <el-button @click="goBack" :icon="Back">
        返回社区
      </el-button>
    </div>

    <!-- 帖子详情 -->
    <div v-if="postDetail" class="post-section">
      <el-card shadow="never" class="post-card">
        <div class="post-header">
          <div class="post-meta">
            <span class="post-author">{{ postDetail.authorName }}</span>
            <span class="post-date">{{ formatDate(postDetail.createTime) }}</span>
            <el-tag v-if="postDetail.categoryName" type="info" size="small" class="category-tag">
              {{ postDetail.categoryName }}
            </el-tag>
          </div>
          <div class="post-actions">
            <el-button 
              :type="postDetail.isLiked ? 'primary' : ''" 
              :plain="!postDetail.isLiked"
              size="small" 
              @click="toggleLike"
            >
                                 <el-icon><StarFilled /></el-icon>
              {{ postDetail.isLiked ? '已赞' : '点赞' }}
            </el-button>
            <el-button 
              :type="postDetail.isCollected ? 'warning' : ''" 
              :plain="!postDetail.isCollected"
              size="small" 
              @click="toggleCollect"
            >
              <el-icon><Star /></el-icon>
              {{ postDetail.isCollected ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
        
        <h1 class="post-title">{{ postDetail.title }}</h1>
        <div class="post-content markdown-content" v-html="formatContent(postDetail.content)"></div>
        
        <div class="post-stats">
          <div class="stat-item">
            <el-icon><View /></el-icon>
            <span>{{ postDetail.viewCount || 0 }} 浏览</span>
          </div>
                     <div class="stat-item" :class="{ 'liked': postDetail.isLiked }">
             <el-icon><StarFilled /></el-icon>
            <span>{{ postDetail.likeCount || 0 }} 点赞</span>
          </div>
          <div class="stat-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ postDetail.commentCount || 0 }} 评论</span>
          </div>
          <div class="stat-item" :class="{ 'collected': postDetail.isCollected }">
            <el-icon><Star /></el-icon>
            <span>{{ postDetail.collectCount || 0 }} 收藏</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 评论区 -->
    <div class="comment-section">
      <el-card shadow="never" class="comment-card">
        <template #header>
          <div class="comment-header">
            <span>评论区</span>
            <span class="comment-count" v-if="commentTotal > 0">共 {{ commentTotal }} 条评论</span>
          </div>
        </template>

        <!-- 发表评论 -->
        <div class="create-comment">
          <el-form :model="commentForm" :rules="commentRules" ref="commentFormRef">
            <el-form-item prop="content">
              <el-input 
                v-model="commentForm.content" 
                type="textarea" 
                placeholder="请输入评论内容..." 
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCreateComment" :loading="commentLoading">
                发表评论
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 评论列表 -->
        <div v-loading="commentsLoading" class="comments-list">
          <div 
            v-for="comment in commentList" 
            :key="comment.id"
            class="comment-item"
          >
            <div class="comment-header">
              <div class="comment-meta">
                <span class="comment-author">{{ comment.authorName }}</span>
                <span class="comment-date">{{ formatDate(comment.createTime) }}</span>
              </div>
              <div class="comment-actions">
                <el-button 
                  :type="comment.isLiked ? 'primary' : ''" 
                  :plain="!comment.isLiked"
                  size="small" 
                  @click="toggleCommentLike(comment)"
                >
                  <el-icon><StarFilled /></el-icon>
                  {{ comment.likeCount || 0 }}
                </el-button>
              </div>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>

        <!-- 评论分页 -->
        <div class="pagination-wrapper" v-if="commentTotal > 0">
          <el-pagination 
            v-model:current-page="commentQueryParams.page" 
            v-model:page-size="commentQueryParams.size"
            :page-sizes="[10, 20, 30]"
            :total="commentTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="handleCommentSizeChange"
            @current-change="handleCommentCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Back, StarFilled, Star, View, ChatDotRound
} from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const router = useRouter()

// 响应式数据
const postDetail = ref(null)
const commentList = ref([])
const commentTotal = ref(0)
const commentsLoading = ref(false)
const commentLoading = ref(false)

// 评论查询参数
const commentQueryParams = reactive({
  page: 1,
  size: 10
})

// 评论表单
const commentForm = reactive({
  content: ''
})

const commentFormRef = ref(null)

// 评论验证规则
const commentRules = reactive({
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 3, max: 500, message: '评论内容为3-500个字符', trigger: 'blur' }
  ]
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 格式化帖子内容（渲染Markdown）
const formatContent = (content) => {
  if (!content) return ''
  return renderMarkdown(content)
}

// 获取帖子详情
const fetchPostDetail = async () => {
  try {
    const postId = route.params.id
    postDetail.value = await communityApi.getPostDetail(postId)
  } catch (error) {
    ElMessage.error('获取帖子详情失败')
    goBack()
  }
}

// 获取评论列表
const fetchComments = async () => {
  commentsLoading.value = true
  try {
    const postId = route.params.id
    const response = await communityApi.getPostComments(postId, commentQueryParams)
    commentList.value = response.records || []
    commentTotal.value = response.total || 0
  } catch (error) {
    ElMessage.error('获取评论列表失败')
  } finally {
    commentsLoading.value = false
  }
}

// 切换帖子点赞状态
const toggleLike = async () => {
  try {
    if (postDetail.value.isLiked) {
      await communityApi.unlikePost(postDetail.value.id)
      postDetail.value.likeCount = Math.max(0, postDetail.value.likeCount - 1)
      postDetail.value.isLiked = false
      ElMessage.success('取消点赞成功')
    } else {
      await communityApi.likePost(postDetail.value.id)
      postDetail.value.likeCount = postDetail.value.likeCount + 1
      postDetail.value.isLiked = true
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 切换帖子收藏状态
const toggleCollect = async () => {
  try {
    if (postDetail.value.isCollected) {
      await communityApi.uncollectPost(postDetail.value.id)
      postDetail.value.collectCount = Math.max(0, postDetail.value.collectCount - 1)
      postDetail.value.isCollected = false
      ElMessage.success('取消收藏成功')
    } else {
      await communityApi.collectPost(postDetail.value.id)
      postDetail.value.collectCount = postDetail.value.collectCount + 1
      postDetail.value.isCollected = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 发表评论
const handleCreateComment = async () => {
  try {
    await commentFormRef.value.validate()
    commentLoading.value = true
    
    const postId = route.params.id
    await communityApi.createComment(postId, commentForm)
    ElMessage.success('评论成功')
    
    // 清空表单
    commentForm.content = ''
    commentFormRef.value?.clearValidate()
    
    // 刷新评论列表
    commentQueryParams.page = 1
    await fetchComments()
    
    // 更新帖子评论数
    postDetail.value.commentCount = postDetail.value.commentCount + 1
  } catch (error) {
    if (error !== false) { // 不是表单验证失败
      ElMessage.error('评论失败')
    }
  } finally {
    commentLoading.value = false
  }
}

// 切换评论点赞状态
const toggleCommentLike = async (comment) => {
  try {
    if (comment.isLiked) {
      await communityApi.unlikeComment(comment.id)
      comment.likeCount = Math.max(0, comment.likeCount - 1)
      comment.isLiked = false
    } else {
      await communityApi.likeComment(comment.id)
      comment.likeCount = comment.likeCount + 1
      comment.isLiked = true
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 评论分页大小改变
const handleCommentSizeChange = (size) => {
  commentQueryParams.size = size
  commentQueryParams.page = 1
  fetchComments()
}

// 评论当前页改变
const handleCommentCurrentChange = (page) => {
  commentQueryParams.page = page
  fetchComments()
}

// 返回社区
const goBack = () => {
  router.push('/community')
}

// 初始化
onMounted(async () => {
  await fetchPostDetail()
  await fetchComments()
})
</script>

<style scoped>
.post-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.back-section {
  margin-bottom: 20px;
}

.post-section {
  margin-bottom: 20px;
}

.post-card, .comment-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #909399;
  font-size: 14px;
}

.post-author {
  color: #00b894;
  font-weight: 500;
  font-size: 16px;
}

.post-actions {
  display: flex;
  gap: 12px;
}

.post-title {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.post-content {
  margin: 0 0 24px 0;
}

.post-stats {
  display: flex;
  justify-content: flex-start;
  gap: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
  transition: color 0.3s;
}

.stat-item.liked {
  color: #f56565;
}

.stat-item.collected {
  color: #f6ad55;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-count {
  color: #909399;
  font-size: 14px;
}

.create-comment {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.comment-item {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #909399;
  font-size: 13px;
}

.comment-author {
  color: #00b894;
  font-weight: 500;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .post-detail {
    padding: 10px;
  }
  
  .post-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .post-actions {
    align-self: stretch;
    justify-content: space-between;
  }
  
  .post-stats {
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .comment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style> 