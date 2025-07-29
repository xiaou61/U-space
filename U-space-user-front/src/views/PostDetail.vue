<template>
  <div class="post-detail-page">
    <el-page-header @back="handleBack" content="帖子详情" class="page-header" />
    <el-skeleton v-if="loadingPost" animated :rows="4" />
    <div v-else>
      <h2>{{ post.title }}</h2>
      <div class="author-row">
        <img :src="post.userInfo?.avatar" class="avatar" />
        <span class="name">{{ post.userInfo?.name }}</span>
        <span class="time">{{ formatTime(post.createdAt) }}</span>
      </div>
      <p class="content">{{ post.content }}</p>
      <div class="img-list" v-if="post.images && post.images.length">
        <img v-for="(img,i) in post.images" :key="i" :src="img" />
      </div>
      <div class="post-meta">
        <span>浏览 {{ post.viewCount }}</span>
        <span class="like" @click="handleLikePost"><el-icon :style="{color: post.liked ? '#f56c6c' : '#909399'}"><StarFilled /></el-icon>{{ post.likeCount }}</span>
        <span>评论 {{ post.commentCount }}</span>
      </div>
    </div>

    <el-divider />
    <!-- 评论列表 -->
    <div v-infinite-scroll="loadMore" :infinite-scroll-disabled="loadingMore || pageNum>=totalPages" infinite-scroll-distance="100">
      <h3>评论</h3>
      <el-skeleton v-if="loadingComments" animated :rows="3" />
      <template v-else>
        <div v-for="c in comments" :key="c.id" class="comment">
          <img :src="c.userInfo?.avatar" class="c-avatar" />
          <div class="c-body">
            <div class="c-meta">
              <span class="c-name">{{ c.userInfo?.name }}</span>
              <span class="c-time">{{ formatTime(c.createdAt) }}</span>
            </div>
            <p class="c-content">{{ c.content }}</p>
            <div class="c-actions">
              <a class="reply-link" @click="setReply(c)">回复</a>
              <span class="c-like" @click="handleLikeComment(c)"><el-icon :style="{color: c.liked ? '#f56c6c' : '#909399'}"><StarFilled /></el-icon>{{ c.likeCount }}</span>
              <a v-if="c.isMine" class="reply-link" @click="handleDelete(c)">删除</a>
              <a v-if="c.replyCount" class="reply-link" @click="loadReplies(c)">
                {{ c.showReplies ? '收起' : '查看 ' + c.replyCount + ' 条回复' }}
              </a>
            </div>
            <!-- 二级回复 -->
            <div v-if="c.showReplies" class="reply-list">
              <div v-for="r in c.replies" :key="r.id" class="reply-item">
                <img :src="r.replyUserAvatar" class="r-avatar" />
                <span class="r-name">{{ r.replyUserName }}</span>
                <template v-if="r.replyToUserName">
                  <span class="reply-to"> 回复 </span>
                  <span class="r-name">{{ r.replyToUserName }}</span>
                </template>
                <span class="colon">:</span>
                <span class="r-content">{{ r.content }}</span>
                <span class="r-like" @click="handleLikeReply(r, c)"><el-icon :style="{color: r.liked ? '#f56c6c' : '#909399'}"><StarFilled /></el-icon>{{ r.likeCount }}</span>
                <a v-if="r.isMine" class="reply-link ml-4" @click="handleDelete(r, true, c)">删除</a>
                <a class="reply-link ml-4" @click="setReply(c, r)">回复</a>
              </div>
            </div>
          </div>
        </div>
        <div v-if="loadingMore" class="load-more">加载中...</div>
        <el-empty v-if="!comments.length && !loadingComments" description="暂无评论" />
      </template>
    </div>

    <!-- 发表评论栏 -->
    <div class="input-bar">
      <el-input v-model="commentText" placeholder="说点什么..." maxlength="200" />
      <el-button type="primary" :disabled="!commentText" @click="submitComment">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getCommentList, getReplyList, addComment, replyComment, listPosts, deleteComment, deleteReply, likeComment, likeReply, likePost, viewPost } from '../api/forum'
import { StarFilled } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const postId = route.params.id

const post = ref({})
const loadingPost = ref(true)

const comments = ref([]) // each item: {..., replies:[], repliesLoaded:false, showReplies:false}
const pageNum = ref(1)
const totalPages = ref(1)
const loadingComments = ref(true)
const loadingMore = ref(false)
const commentText = ref('')
const replyTarget = ref(null)

const likedCommentIds = ref(new Set(JSON.parse(localStorage.getItem('likedCommentIds') || '[]')))
const likedReplyIds = ref(new Set(JSON.parse(localStorage.getItem('likedReplyIds') || '[]')))
const likedPostIds = ref(new Set(JSON.parse(localStorage.getItem('likedPostIds') || '[]')))

const fetchPost = async () => {
  loadingPost.value = true
  // reuse listPosts to get single post
  const res = await listPosts(null,1,10) // placeholder, assume backend provides /post/detail ideally
  const all = res.data.list || []
  post.value = all.find((p)=>p.id===postId) || {}
  post.value.liked = likedPostIds.value.has(postId)
  loadingPost.value = false
}

const fetchComments = async (isMore=false)=>{
  if(isMore && (loadingMore.value || pageNum.value>totalPages.value)) return
  if(isMore) loadingMore.value=true
  else loadingComments.value=true
  const res = await getCommentList(postId,pageNum.value,10)
  const page=res.data||{}
  const newList=(page.list||page.records||[]).map(c=>({
    ...c,
    userInfo: { avatar: c.avatar, name: c.name },
    replies: [],
    repliesLoaded: false,
    showReplies: false,
    liked: likedCommentIds.value.has(c.id),
  }))
  if(isMore) comments.value=comments.value.concat(newList)
  else comments.value=newList
  totalPages.value=Number(page.pages||1)
  loadingComments.value=false
  loadingMore.value=false
}

const loadReplies = async (c)=>{
  if(c.repliesLoaded){
    c.showReplies=!c.showReplies
    return
  }
  c.loadingReplies=true
  const res= await getReplyList(c.id,1,20)
  const page=res.data||{}
  c.replies=(page.list||page.records||[]).map(r=>({...r, liked: likedReplyIds.value.has(r.id)}))
  c.repliesLoaded=true
  c.showReplies=true
  c.loadingReplies=false
}

const loadMore=()=>{
 pageNum.value+=1
 fetchComments(true)
}

const setReply = (rootComment, replyItem = null)=>{
  if(replyItem){
    // 回复某条回复：commentId 仍为一级评论ID，replyUserId 为被回复用户ID
    replyTarget.value = { id: rootComment.id, userId: replyItem.userId }
    commentText.value = `@${replyItem.replyUserName || ''} `
  }else{
    // 回复一级评论
    replyTarget.value = rootComment
    commentText.value = `@${rootComment.userInfo?.name || ''} `
  }
}

const submitComment= async ()=>{
 if(!commentText.value.trim()) return
 try{
  if(replyTarget.value){
     await replyComment({
       postId,
       commentId: replyTarget.value.id,
       content: commentText.value.trim(),
       replyUserId: replyTarget.value.userId,
     })
  }else{
     await addComment({postId,content:commentText.value.trim()})
  }
  replyTarget.value=null
  ElMessage.success('评论成功')
  commentText.value=''
  pageNum.value=1
  fetchComments()
 }catch(e){ElMessage.error('评论失败')}
}

const formatTime=t=> t?dayjs(t).format('MM-DD HH:mm'):''

const handleDelete = async (item, isReply=false, root=null)=>{
  try{
    await ElMessageBox.confirm('确定删除此条评论?','提示',{type:'warning'})
    if(isReply){
      await deleteReply(item.id)
    }else{
      await deleteComment(item.id)
    }
    ElMessage.success('已删除')
    if(isReply && root){
      root.replies = root.replies.filter(r=>r.id!==item.id)
      root.replyCount = Math.max((root.replyCount||1)-1,0)
    }else{
      comments.value = comments.value.filter(c=>c.id!==item.id)
    }
  }catch(e){ if(e!== 'cancel' && e!== 'close') ElMessage.error('删除失败') }
}

const handleLikeComment = async (c)=>{
  if(c.liking) return
  c.liking = true
  try{
    const res = await likeComment(c.id)
    const msg = res.msg || '操作成功'
    const isCancel = msg.includes('取消')
    if(isCancel){
      c.liked = false
      c.likeCount = Math.max((c.likeCount || 1) - 1, 0)
      likedCommentIds.value.delete(c.id)
    }else{
      c.liked = true
      c.likeCount = (c.likeCount || 0) + 1
      likedCommentIds.value.add(c.id)
    }
    localStorage.setItem('likedCommentIds', JSON.stringify(Array.from(likedCommentIds.value)))
  }catch(e){
    ElMessage.error('操作失败')
  }finally{
    c.liking = false
  }
}

const handleLikeReply = async (r, root)=>{
  if(r.liking) return
  r.liking = true
  try{
    const res = await likeReply(r.id)
    const msg = res.msg || '操作成功'
    const isCancel = msg.includes('取消')
    if(isCancel){
      r.liked = false
      r.likeCount = Math.max((r.likeCount || 1)-1,0)
      likedReplyIds.value.delete(r.id)
    }else{
      r.liked = true
      r.likeCount = (r.likeCount || 0)+1
      likedReplyIds.value.add(r.id)
    }
    localStorage.setItem('likedReplyIds', JSON.stringify(Array.from(likedReplyIds.value)))
  }catch(e){
    ElMessage.error('操作失败')
  }finally{
    r.liking = false
  }
}

const handleLikePost = async () => {
  if(post.value.liking) return
  post.value.liking = true
  try {
    const res = await likePost(postId)
    const msg = res.msg || '操作成功'
    const isCancel = msg.includes('取消')
    if(isCancel){
      post.value.liked = false
      post.value.likeCount = Math.max((post.value.likeCount || 1) - 1, 0)
      likedPostIds.value.delete(postId)
    }else{
      post.value.liked = true
      post.value.likeCount = (post.value.likeCount || 0) + 1
      likedPostIds.value.add(postId)
    }
    localStorage.setItem('likedPostIds', JSON.stringify(Array.from(likedPostIds.value)))
  }catch(e){
    ElMessage.error('操作失败')
  }finally{
    post.value.liking = false
  }
}

const handleViewPost = async () => {
  try {
    await viewPost(postId)
    post.value.viewCount = (post.value.viewCount || 0) + 1
  } catch (e) {
    console.error(e)
  }
}

const handleBack = ()=>{
  if(window.history.length > 1){
    router.back()
  }else if(route.query.from){
    router.push(route.query.from)
  }else{
    router.push('/forum')
  }
}

onMounted(async ()=>{
  await fetchPost()
  await handleViewPost()
  fetchComments()
})
</script>

<style scoped>
.post-detail-page{padding:16px;}
.author-row{display:flex;align-items:center;gap:8px;margin:8px 0;}
.avatar{width:32px;height:32px;border-radius:50%;object-fit:cover;}
.img-list img{width:90px;height:90px;border-radius:6px;object-fit:cover;margin-right:6px;margin-top:6px;}
.comment{display:flex;gap:8px;margin-top:12px;}
.c-avatar{width:28px;height:28px;border-radius:50%;object-fit:cover;}
.c-body{flex:1;}
.c-meta{display:flex;gap:8px;font-size:.8rem;color:#909399;}
.c-name{font-weight:600;color:#333;}
.c-content{white-space:pre-wrap;margin:4px 0;}
.load-more{text-align:center;color:#909399;font-size:.85rem;padding:8px;}
.input-bar{position:fixed;bottom:56px;left:0;right:0;display:flex;gap:8px;padding:8px 16px;background:#fff;border-top:1px solid #e0e0e0;}
.reply-link{font-size:0.75rem;color:#409eff;cursor:pointer;margin-top:2px;display:inline-block;}
.c-actions{display:flex;gap:12px;margin-top:2px;align-items:center;}
.reply-list{margin-left:32px;margin-top:6px;}
.reply-item{display:flex;align-items:center;gap:4px;font-size:.85rem;color:#555;margin-top:4px;}
.r-name{font-weight:600;color:#333;}
.r-avatar{width:22px;height:22px;border-radius:50%;object-fit:cover;margin-right:6px;}
.reply-item{display:flex;align-items:center;gap:4px;font-size:.85rem;color:#555;margin-top:4px;}
.reply-to{font-size:0.75rem;color:#909399;margin:0 4px;}
.colon{font-size:0.75rem;color:#909399;margin:0 4px;}
.post-meta{display:flex;gap:16px;color:#909399;font-size:.85rem;margin-top:8px;align-items:center;}
.post-meta .like,.c-like,.r-like{display:flex;align-items:center;gap:4px;color:#f56c6c;font-size:.8rem;cursor:pointer;}
.page-header{margin-bottom:8px;}
</style> 