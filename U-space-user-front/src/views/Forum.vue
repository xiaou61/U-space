<template>
  <div class="forum-page">
    <el-tabs v-model="activeCate" @tab-change="handleTabChange" stretch>
      <el-tab-pane label="推荐" name="recommend" />
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane
        v-for="c in categories"
        :key="c.id"
        :label="c.name"
        :name="c.id"
      />
    </el-tabs>

    <el-skeleton v-if="loading" animated :rows="4" style="margin-top:16px;" />

    <div
      v-infinite-scroll="loadMore"
      :infinite-scroll-disabled="loadingMore || pageNum >= totalPages"
      infinite-scroll-distance="100"
    >
    <el-card v-for="p in posts" :key="p.id" class="post-card" shadow="hover" @click="goDetail(p)">
      <div class="post-header">
        <div class="author">
          <img :src="p.userInfo?.avatar" class="avatar" />
          <span class="name">{{ p.userInfo?.name || '匿名' }}</span>
        </div>
        <span class="post-time">{{ formatTime(p.createdAt) }}</span>
      </div>
      <span class="post-title">{{ p.title }}</span>
      <p class="post-content">{{ p.content }}</p>
      <div v-if="p.images && p.images.length" class="img-list">
        <img v-for="(img, i) in p.images.slice(0,3)" :key="i" :src="img" />
      </div>
      <div class="post-meta">
        <span>浏览 {{ p.viewCount }}</span>
        <span class="like" @click.stop="handleLike(p)"><el-icon :style="{color: p.liked ? '#f56c6c' : '#909399'}"><StarFilled /></el-icon>{{ p.likeCount }}</span>
        <span>评论 {{ p.commentCount }}</span>
      </div>
    </el-card>
    <div v-if="loadingMore" class="load-more">加载中...</div>
    </div>

    <el-empty v-if="!loading && !posts.length" description="暂无帖子" />

    <!-- 浮动按钮：刷新 & 发布 -->
    <el-button class="fab refresh-btn" type="primary" circle @click="doRefresh">
      <el-icon><Refresh /></el-icon>
    </el-button>
    <el-button class="fab" type="primary" circle @click="postDialog.visible = true">
      <el-icon><Plus /></el-icon>
    </el-button>
    <el-button class="fab msg-btn" type="primary" circle @click="openNotifyDialog">
      <el-icon><Bell /></el-icon>
      <span v-if="hasUnreadNotify" class="dot" />
    </el-button>

    <!-- 消息通知弹窗 -->
    <el-dialog v-model="notifyDialog.visible" title="我的消息" width="90%" :close-on-click-modal="false">
      <el-scrollbar max-height="60vh">
        <div v-if="!notifyDialog.list.length" style="text-align:center;color:#909399;padding:24px;">暂无消息</div>
        <div v-for="(n,idx) in notifyDialog.list" :key="idx" class="notify-item" @click="goNotify(n)">
          <img :src="n.senderAvatar" class="n-avatar" />
          <div class="n-body">
            <p class="n-content">{{ formatNotify(n) }}</p>
            <span class="n-time">{{ formatTime(n.createdAt) }}</span>
          </div>
        </div>
      </el-scrollbar>
      <template #footer>
        <el-button @click="notifyDialog.visible=false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 发布帖子弹窗 -->
    <el-dialog v-model="postDialog.visible" title="发表帖子" width="90%" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="分类">
          <el-select v-model="postDialog.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="postDialog.title" placeholder="输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" rows="4" v-model="postDialog.content" placeholder="输入内容" />
        </el-form-item>
        <el-form-item label="图片上传">
          <el-upload
            class="upload-area"
            :show-file-list="false"
            :before-upload="handleFileUpload"
            multiple
            accept="image/*"
          >
            <el-button type="primary">选择图片</el-button>
          </el-upload>
          <div class="thumbs" v-if="postDialog.images.length">
            <div v-for="(url, idx) in postDialog.images" :key="idx" class="thumb">
              <img :src="url" />
              <el-icon class="del" @click="postDialog.images.splice(idx,1)"><Close /></el-icon>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="postDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="posting" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElLoading } from 'element-plus'
import 'element-plus/es/components/infinite-scroll/style/css'
import { listCategories, listPosts, listRecommendPosts, addPost } from '../api/forum'
import { uploadPostImage } from '../api/forum'
import dayjs from 'dayjs'
import { Plus, Close, StarFilled, Refresh, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { likePost, listUserNotify, readUserNotify } from '../api/forum'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'

const categories = ref([])
const activeCate = ref('all')
const posts = ref([])
const loading = ref(true)
const pageNum = ref(1)
const totalPages = ref(1)
const loadingMore = ref(false)
const postDialog = ref({ visible: false, categoryId: '', title: '', content: '', images: [] })
const posting = ref(false)
const likedIds = ref(new Set(JSON.parse(localStorage.getItem('likedPostIds') || '[]')))
const route = useRoute()
const router = useRouter()
const notifyDialog = ref({ visible: false, list: [] })
const hasUnreadNotify = ref(false)

// listen to custom event for unread
const handleNotifyUnread = () => {
  hasUnreadNotify.value = true
}

const fetchCategories = async () => {
  try {
    const res = await listCategories()
    categories.value = (res.data || []).filter((c) => c.status === 1)
    if (!postDialog.value.categoryId && categories.value.length) {
      postDialog.value.categoryId = categories.value[0].id
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchPosts = async (isLoadMore = false) => {
  if (isLoadMore && (loadingMore.value || pageNum.value > totalPages.value)) return
  loading.value = !isLoadMore
  if (isLoadMore) loadingMore.value = true
  try {
    let newList = []
    if (activeCate.value === 'recommend') {
      // 推荐列表一次性返回，不分页
      const res = await listRecommendPosts()
      newList = res.data || []
      totalPages.value = 1
    } else {
      const res = await listPosts(activeCate.value === 'all' ? null : activeCate.value, pageNum.value, 10)
      const pageData = res.data || {}
      newList = pageData.list || pageData.records || []
      totalPages.value = Number(pageData.pages || 1)
    }
    const processed = newList.map((it)=>({...it, liked: likedIds.value.has(it.id)}))
    if (isLoadMore) {
      posts.value = posts.value.concat(processed)
    } else {
      posts.value = processed
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const handleTabChange = () => {
  pageNum.value = 1
  totalPages.value = 1
  fetchPosts()
}

const loadMore = () => {
  if (loadingMore.value || pageNum.value >= totalPages.value) return
  pageNum.value += 1
  fetchPosts(true)
}

const formatTime = (t) => (t ? dayjs(t).format('MM-DD HH:mm') : '')

const submitPost = async () => {
  if (!postDialog.value.title || !postDialog.value.content) {
    ElMessage.warning('请填写完整内容')
    return
  }
  posting.value = true
  try {
    await addPost({
      categoryId: postDialog.value.categoryId || null,
      title: postDialog.value.title,
      content: postDialog.value.content,
      images: postDialog.value.images,
    })
    ElMessage.success('发布成功')
    postDialog.value.visible = false
    postDialog.value.title = ''
    postDialog.value.content = ''
    postDialog.value.images = []
    fetchPosts()
  } catch (e) {
    console.error(e)
    ElMessage.error('发布失败')
  } finally {
    posting.value = false
  }
}

const handleFileUpload = async (file) => {
  try {
    const res = await uploadPostImage(file)
    const urls = Array.isArray(res.data) ? res.data : [res.data]
    urls.forEach((u) => {
      if (u) postDialog.value.images.push(u)
    })
    if (urls.length) ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败' + e.message)
  }
  return false // prevent auto upload
}

const handleLike = async (p) => {
  if (p.liking) return
  p.liking = true
  try {
    const res = await likePost(p.id)
    const msg = res.msg || '操作成功'
    ElMessage.success(msg)
    const isCancel = msg.includes('取消')
    if (isCancel) {
      p.liked = false
      p.likeCount = Math.max((p.likeCount || 1) - 1, 0)
    } else {
      p.liked = true
      p.likeCount = (p.likeCount || 0) + 1
    }
    if (p.liked) {
      likedIds.value.add(p.id)
    } else {
      likedIds.value.delete(p.id)
    }
    localStorage.setItem('likedPostIds', JSON.stringify(Array.from(likedIds.value)))
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    p.liking = false
  }
}

const openNotifyDialog = async () => {
  try {
    await readUserNotify()
    const res = await listUserNotify(1, 50)
    notifyDialog.value.list = (res.data?.list || res.data?.records || [])
  } catch (e) {
    console.error(e)
  }
  hasUnreadNotify.value = false
  notifyDialog.value.visible = true
}

const formatNotify = (n) => {
  if (n.type === 'like') {
    return `${n.senderName || '有人'} 点赞了你的帖子《${n.postTitle || ''}》`
  }
  // 其他类型占位
  return '你有新的消息'
}

const goNotify = (n) => {
  notifyDialog.value.visible = false
  if (n.type === 'like' && n.targetId) {
    router.push({ path: '/post/detail/' + n.targetId, query: { from: route.fullPath } })
  }
}

const getScrollEl = ()=> document.querySelector('.page-main')

const saveState = ()=>{
  const el = getScrollEl()
  sessionStorage.setItem('forumState', JSON.stringify({
    activeCate: activeCate.value,
    pageNum: pageNum.value,
    totalPages: totalPages.value,
    posts: posts.value,
    scrollY: el ? el.scrollTop : 0
  }))
}

const goDetail = (p)=>{
  router.push({ path: '/post/detail/'+p.id, query: { from: route.fullPath } })
}

const doRefresh = async () => {
  const el = getScrollEl()
  if(el) el.scrollTop = 0
  pageNum.value = 1
  totalPages.value = 1
  await fetchPosts()
}

// 自动在离开论坛页时保存状态（包括滚动位置）
onBeforeRouteLeave((_to,_from,next)=>{
  saveState()
  next()
})

onMounted(async () => {
  await fetchCategories()
  const saved = sessionStorage.getItem('forumState')
  if(saved){
    try{
      const s = JSON.parse(saved)
      activeCate.value = s.activeCate || 'all'
      pageNum.value = s.pageNum || 1
      totalPages.value = s.totalPages || 1
      posts.value = s.posts || []
      loading.value=false
      await nextTick()
      const el = getScrollEl()
      if(el) el.scrollTop = s.scrollY || 0
    }catch(e){
      console.error(e)
      fetchPosts()
    }
    sessionStorage.removeItem('forumState')
  }else{
    fetchPosts()
  }
  window.addEventListener('notify-unread', handleNotifyUnread)
})

onUnmounted(()=>{
  window.removeEventListener('notify-unread', handleNotifyUnread)
})
</script>

<style scoped>
.forum-page {
  padding: 16px;
}
.post-card {
  margin-top: 16px;
}
.post-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}
.post-title {
  font-weight: 600;
  font-size: 1rem;
}
.post-time {
  font-size: 0.8rem;
  color: #909399;
}
.post-content {
  margin: 6px 0;
  white-space: pre-wrap;
  line-height: 1.4;
}
.img-list img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  margin-right: 6px;
}
.post-meta {
  margin-top: 8px;
  display: flex;
  gap: 12px;
  font-size: 0.8rem;
  color: #606266;
}
.post-meta .like{
  cursor:pointer;
  display:flex;
  align-items:center;
}
.post-meta .like .el-icon{
  margin-right:2px;
  color:#f7ba2a;
}
.load-more {
  text-align: center;
  padding: 12px 0;
  color: #909399;
  font-size: 0.85rem;
}
.thumbs {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.thumb {
  position: relative;
}
.thumb img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}
.thumb .del {
  position: absolute;
  top: -6px;
  right: -6px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
  cursor: pointer;
}
.upload-area {
  margin-top: 8px;
}
.fab {
  position: fixed;
  bottom: 80px; /* above bottom nav */
  right: 24px;
  z-index: 999;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}
.author {
  display: flex;
  align-items: center;
  gap: 6px;
}
.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}
.author .name {
  font-size: 0.85rem;
  font-weight: 500;
}
.refresh-btn {
  right: 88px;
}
.msg-btn {
  right: 140px; /* Adjust position to be between refresh and post buttons */
  position: fixed;
}
.msg-btn .dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
}
.notify-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}
.notify-item:last-child {
  border-bottom: none;
}
.n-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
}
.n-body {
  flex: 1;
}
.n-content {
  font-size: 0.9rem;
  color: #303133;
  margin-bottom: 4px;
}
.n-time {
  font-size: 0.75rem;
  color: #909399;
}
</style> 