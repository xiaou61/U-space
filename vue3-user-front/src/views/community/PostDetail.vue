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
            <span class="post-author" @click.stop="goToUserProfile(postDetail.authorId)">
              {{ postDetail.authorName }}
            </span>
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
        
        <!-- AI一键分析 -->
        <div class="post-summary-section">
          <el-card shadow="never" class="summary-card">
            <template #header>
              <div class="summary-header">
                <el-icon style="color: #409eff"><MagicStick /></el-icon>
                <span style="margin-left: 8px; font-weight: 600">AI智能分析</span>
                <el-button 
                  v-if="!postDetail.aiSummary && !aiSummaryLoading"
                  size="default" 
                  type="primary"
                  @click="generateAISummary"
                  style="margin-left: auto"
                  :icon="MagicStick"
                >
                  🤖 AI一键分析
                </el-button>
                <el-button 
                  v-else-if="postDetail.aiSummary"
                  size="small" 
                  type="success"
                  plain
                  @click="generateAISummary"
                  style="margin-left: auto"
                  :loading="aiSummaryLoading"
                >
                  重新分析
                </el-button>
              </div>
            </template>
            
            <!-- 未生成状态 -->
            <div v-if="!postDetail.aiSummary && !aiSummaryLoading" class="summary-empty">
              <div class="empty-icon">🤖</div>
              <div class="empty-text">点击「AI一键分析」按钮，让AI为您智能分析这篇帖子</div>
              <div class="empty-tips">✨ AI将为您提供：核心内容提炼、关键技术点总结</div>
            </div>
            
            <!-- 生成中状态 -->
            <div v-else-if="aiSummaryLoading" class="summary-loading">
              <el-icon class="is-loading" style="font-size: 32px; color: #409eff"><Loading /></el-icon>
              <div class="loading-text">AI正在智能分析帖子内容...</div>
              <div class="loading-tips">这可能需要几秒钟，请稍候</div>
            </div>
            
            <!-- 已生成状态 -->
            <div v-else-if="postDetail.aiSummary" class="summary-content">
              <div class="summary-label">
                <el-icon><Document /></el-icon>
                <span>AI分析结果</span>
              </div>
              <div class="summary-text">{{ postDetail.aiSummary }}</div>
              
              <!-- 关键词标签 -->
              <div v-if="aiKeywords && aiKeywords.length > 0" class="keywords-section">
                <div class="keywords-label">
                  <el-icon><CollectionTag /></el-icon>
                  <span>关键词</span>
                </div>
                <div class="keywords-tags">
                  <el-tag 
                    v-for="(keyword, index) in aiKeywords" 
                    :key="index"
                    type="info"
                    effect="plain"
                    size="small"
                    style="margin-right: 8px; margin-bottom: 8px"
                  >
                    {{ keyword }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </div>
        
        <div class="post-content markdown-content" v-html="formatContent(postDetail.content)"></div>
        
        <!-- 帖子标签 -->
        <div v-if="postDetail.tags && postDetail.tags.length > 0" class="post-tags-section">
          <span class="tags-label">标签：</span>
          <el-tag 
            v-for="tag in postDetail.tags" 
            :key="tag.id"
            size="default"
            type="success"
            effect="plain"
            class="post-tag-item"
          >
            # {{ tag.name }}
          </el-tag>
        </div>
        
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
                <span class="comment-author" @click.stop="goToUserProfile(comment.authorId)">
                  {{ comment.authorName }}
                </span>
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
                <el-button 
                  size="small" 
                  @click="showReplyBox(comment)"
                >
                  <el-icon><ChatDotRound /></el-icon>
                  回复
                </el-button>
              </div>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
            
            <!-- 回复框 -->
            <div v-if="replyingComment && replyingComment.id === comment.id" class="reply-box">
              <el-input 
                v-model="replyForm.content" 
                type="textarea"
                :placeholder="`回复 @${comment.authorName}...`"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
              <div class="reply-actions">
                <el-button size="small" @click="cancelReply">取消</el-button>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleReply(comment)"
                  :loading="replyLoading"
                >
                  发送回复
                </el-button>
              </div>
            </div>
            
            <!-- 二级回复列表 -->
            <div v-if="comment.replyCount > 0" class="replies-section">
              <div class="replies-header" @click="toggleReplies(comment)">
                <el-icon><ArrowDown /></el-icon>
                <span>查看 {{ comment.replyCount }} 条回复</span>
              </div>
              
              <!-- 展开的回复列表 -->
              <div v-if="expandedComments[comment.id]" class="replies-list">
                <div 
                  v-for="reply in comment.replies || []" 
                  :key="reply.id"
                  class="reply-item"
                >
                  <div class="reply-header">
                    <div class="reply-meta">
                      <span class="reply-author" @click.stop="goToUserProfile(reply.authorId)">
                        {{ reply.authorName }}
                      </span>
                      <span v-if="reply.replyToUserName" class="reply-to">
                        回复 <span class="reply-to-name" @click.stop="goToUserProfile(reply.replyToUserId)">
                          @{{ reply.replyToUserName }}
                        </span>
                      </span>
                      <span class="reply-date">{{ formatDate(reply.createTime) }}</span>
                    </div>
                    <div class="reply-actions">
                      <el-button 
                        :type="reply.isLiked ? 'primary' : ''" 
                        :plain="!reply.isLiked"
                        size="small" 
                        @click="toggleCommentLike(reply)"
                      >
                        <el-icon><StarFilled /></el-icon>
                        {{ reply.likeCount || 0 }}
                      </el-button>
                      <el-button 
                        size="small" 
                        @click="showReplyBox(reply, comment)"
                      >
                        <el-icon><ChatDotRound /></el-icon>
                        回复
                      </el-button>
                    </div>
                  </div>
                  <div class="reply-content">{{ reply.content }}</div>
                </div>
                
                <!-- 查看更多回复 -->
                <div v-if="comment.replyCount > (comment.replies || []).length" class="load-more-replies">
                  <el-button 
                    text 
                    size="small"
                    :loading="loadingReplies[comment.id]"
                    @click="loadMoreReplies(comment)"
                  >
                    加载更多回复...
                  </el-button>
                </div>
              </div>
            </div>
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
  Back, StarFilled, Star, View, ChatDotRound, MagicStick, Loading, ArrowDown, Document, CollectionTag
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
const aiSummaryLoading = ref(false)
const aiKeywords = ref([]) // AI提取的关键词

// 二级评论相关
const replyingComment = ref(null) // 当前正在回复的评论
const parentComment = ref(null) // 父评论（用于二级回复）
const replyLoading = ref(false)
const expandedComments = ref({}) // 展开的评论ID集合
const loadingReplies = ref({}) // 正在加载回复的评论ID集合

// 评论查询参数
const commentQueryParams = reactive({
  page: 1,
  size: 10
})

// 评论表单
const commentForm = reactive({
  content: ''
})

// 回复表单
const replyForm = reactive({
  content: '',
  replyToUserId: null,
  replyToCommentId: null
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

// 生成AI摘要
const generateAISummary = async () => {
  aiSummaryLoading.value = true
  try {
    // 如果已经有摘要，说明是重新分析，传true强制刷新
    const forceRefresh = !!postDetail.value.aiSummary
    const result = await communityApi.generateSummary(postDetail.value.id, forceRefresh)
    
    // 更新关键词
    if (result.keywords && Array.isArray(result.keywords)) {
      aiKeywords.value = result.keywords
    }
    
    // 重新获取帖子详情以获取AI摘要
    await fetchPostDetail()
    ElMessage.success('AI摘要生成成功！')
  } catch (error) {
    ElMessage.error('生成AI摘要失败')
  } finally {
    aiSummaryLoading.value = false
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

// 显示回复框
const showReplyBox = (comment, parent = null) => {
  replyingComment.value = comment
  parentComment.value = parent
  replyForm.content = ''
  replyForm.replyToUserId = comment.authorId
  replyForm.replyToCommentId = comment.id
}

// 取消回复
const cancelReply = () => {
  replyingComment.value = null
  parentComment.value = null
  replyForm.content = ''
  replyForm.replyToUserId = null
  replyForm.replyToCommentId = null
}

// 发送回复
const handleReply = async (comment) => {
  if (!replyForm.content.trim()) {
    ElMessage.error('请输入回复内容')
    return
  }
  
  if (replyForm.content.length < 3 || replyForm.content.length > 500) {
    ElMessage.error('回复内容为3-500个字符')
    return
  }
  
  replyLoading.value = true
  try {
    await communityApi.replyComment(comment.id, replyForm)
    ElMessage.success('回复成功')
    
    // 清空回复表单
    cancelReply()
    
    // 如果该评论的回复已展开，则刷新回复列表
    const targetComment = parentComment.value || comment
    if (expandedComments.value[targetComment.id]) {
      await loadReplies(targetComment)
    } else {
      // 否则增加回复计数
      targetComment.replyCount = (targetComment.replyCount || 0) + 1
    }
    
    // 更新帖子评论数
    postDetail.value.commentCount = postDetail.value.commentCount + 1
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    replyLoading.value = false
  }
}

// 切换回复列表展开/折叠
const toggleReplies = async (comment) => {
  if (expandedComments.value[comment.id]) {
    expandedComments.value[comment.id] = false
  } else {
    expandedComments.value[comment.id] = true
    if (!comment.replies || comment.replies.length === 0) {
      await loadReplies(comment)
    }
  }
}

// 加载回复列表
const loadReplies = async (comment) => {
  loadingReplies.value[comment.id] = true
  try {
    const response = await communityApi.getCommentReplies(comment.id, {
      pageNum: 1,
      pageSize: 10
    })
    comment.replies = response.records || []
    comment.replyCount = response.total || 0
  } catch (error) {
    ElMessage.error('加载回复列表失败')
  } finally {
    loadingReplies.value[comment.id] = false
  }
}

// 加载更多回复
const loadMoreReplies = async (comment) => {
  loadingReplies.value[comment.id] = true
  try {
    const currentPage = Math.floor((comment.replies || []).length / 10) + 1
    const response = await communityApi.getCommentReplies(comment.id, {
      pageNum: currentPage + 1,
      pageSize: 10
    })
    comment.replies = [...(comment.replies || []), ...(response.records || [])]
  } catch (error) {
    ElMessage.error('加载回复失败')
  } finally {
    loadingReplies.value[comment.id] = false
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

// 跳转到用户主页
const goToUserProfile = (userId) => {
  if (userId) {
    router.push(`/community/users/${userId}`)
  }
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
  cursor: pointer;
  transition: all 0.3s;
}

.post-author:hover {
  color: #00d4a0;
  text-decoration: underline;
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

/* AI摘要样式 */
.post-summary-section {
  margin: 20px 0;
}

.summary-card {
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transition: all 0.3s;
}

.summary-card:hover {
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.25);
}

.summary-header {
  display: flex;
  align-items: center;
  font-weight: 600;
}

/* 未生成状态 */
.summary-empty {
  text-align: center;
  padding: 40px 20px;
  background: white;
  border-radius: 8px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.empty-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 12px;
  font-weight: 500;
}

.empty-tips {
  font-size: 14px;
  color: #909399;
}

/* 生成中状态 */
.summary-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 20px;
  background: white;
  border-radius: 8px;
}

.loading-text {
  font-size: 16px;
  color: #409eff;
  font-weight: 500;
}

.loading-tips {
  font-size: 13px;
  color: #909399;
}

/* 已生成状态 */
.summary-content {
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.summary-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #409eff;
  font-size: 14px;
}

.summary-text {
  color: #303133;
  line-height: 1.8;
  font-size: 15px;
  text-indent: 2em;
}

.keywords-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
}

.keywords-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}

.keywords-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 帖子标签样式 */
.post-tags-section {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.tags-label {
  font-weight: 500;
  color: #606266;
}

.post-tag-item {
  cursor: pointer;
  transition: all 0.3s;
}

.post-tag-item:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 6px rgba(103, 194, 58, 0.3);
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
  cursor: pointer;
  transition: all 0.3s;
}

.comment-author:hover {
  color: #00d4a0;
  text-decoration: underline;
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

/* 回复框样式 */
.reply-box {
  margin-top: 12px;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

/* 二级回复样式 */
.replies-section {
  margin-top: 12px;
  padding-left: 16px;
  border-left: 2px solid #e4e7ed;
}

.replies-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px;
  cursor: pointer;
  color: #409eff;
  font-size: 14px;
  transition: all 0.3s;
}

.replies-header:hover {
  color: #66b1ff;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.replies-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-item {
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.reply-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 12px;
}

.reply-author {
  color: #00b894;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.reply-author:hover {
  color: #00d4a0;
  text-decoration: underline;
}

.reply-to {
  color: #909399;
}

.reply-to-name {
  color: #409eff;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.reply-to-name:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.reply-date {
  color: #c0c4cc;
}

.reply-actions {
  display: flex;
  gap: 6px;
}

.reply-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  font-size: 14px;
}

.load-more-replies {
  text-align: center;
  padding: 8px;
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