import { defineStore } from 'pinia'
import { ref } from 'vue'
import { communityApi } from '@/api/community'

export const useCommunityStore = defineStore('community', () => {
  // ========== 状态 ==========
  
  // 用户社区状态
  const userStatus = ref(null)
  
  // 帖子相关状态
  const posts = ref([])
  const postsTotal = ref(0)
  const postsLoading = ref(false)
  
  // 我的收藏
  const collections = ref([])
  const collectionsTotal = ref(0)
  const collectionsLoading = ref(false)
  
  // 我的帖子
  const myPosts = ref([])
  const myPostsTotal = ref(0)
  const myPostsLoading = ref(false)
  
  // 我的评论
  const myComments = ref([])
  const myCommentsTotal = ref(0)
  const myCommentsLoading = ref(false)
  
  // ========== Actions ==========
  
  // 初始化社区
  const initCommunity = async () => {
    try {
      userStatus.value = await communityApi.init()
      return userStatus.value
    } catch (error) {
      console.error('社区初始化失败:', error)
      throw error
    }
  }
  
  // 获取当前用户社区状态
  const fetchUserStatus = async () => {
    try {
      userStatus.value = await communityApi.getCurrentUserStatus()
      return userStatus.value
    } catch (error) {
      console.error('获取用户社区状态失败:', error)
      throw error
    }
  }
  
  // 获取帖子列表
  const fetchPosts = async (params) => {
    postsLoading.value = true
    try {
      const response = await communityApi.getPostList(params)
      posts.value = response.list || []
      postsTotal.value = response.total || 0
      return response
    } catch (error) {
      console.error('获取帖子列表失败:', error)
      throw error
    } finally {
      postsLoading.value = false
    }
  }
  
  // 获取我的收藏
  const fetchCollections = async (params) => {
    collectionsLoading.value = true
    try {
      const response = await communityApi.getUserCollections(params)
      collections.value = response.list || []
      collectionsTotal.value = response.total || 0
      return response
    } catch (error) {
      console.error('获取收藏列表失败:', error)
      throw error
    } finally {
      collectionsLoading.value = false
    }
  }
  
  // 获取我的帖子
  const fetchMyPosts = async (params) => {
    myPostsLoading.value = true
    try {
      const response = await communityApi.getUserPosts(params)
      myPosts.value = response.list || []
      myPostsTotal.value = response.total || 0
      return response
    } catch (error) {
      console.error('获取我的帖子失败:', error)
      throw error
    } finally {
      myPostsLoading.value = false
    }
  }
  
  // 获取我的评论
  const fetchMyComments = async (params) => {
    myCommentsLoading.value = true
    try {
      const response = await communityApi.getUserComments(params)
      myComments.value = response.list || []
      myCommentsTotal.value = response.total || 0
      return response
    } catch (error) {
      console.error('获取我的评论失败:', error)
      throw error
    } finally {
      myCommentsLoading.value = false
    }
  }
  
  // 创建帖子
  const createPost = async (postData) => {
    try {
      await communityApi.createPost(postData)
      // 创建成功后可以刷新帖子列表
      return true
    } catch (error) {
      console.error('创建帖子失败:', error)
      throw error
    }
  }
  
  // 点赞帖子
  const likePost = async (postId) => {
    try {
      await communityApi.likePost(postId)
      return true
    } catch (error) {
      console.error('点赞帖子失败:', error)
      throw error
    }
  }
  
  // 取消点赞帖子
  const unlikePost = async (postId) => {
    try {
      await communityApi.unlikePost(postId)
      return true
    } catch (error) {
      console.error('取消点赞失败:', error)
      throw error
    }
  }
  
  // 收藏帖子
  const collectPost = async (postId) => {
    try {
      await communityApi.collectPost(postId)
      return true
    } catch (error) {
      console.error('收藏帖子失败:', error)
      throw error
    }
  }
  
  // 取消收藏帖子
  const uncollectPost = async (postId) => {
    try {
      await communityApi.uncollectPost(postId)
      return true
    } catch (error) {
      console.error('取消收藏失败:', error)
      throw error
    }
  }
  
  // 重置状态
  const resetState = () => {
    userStatus.value = null
    posts.value = []
    postsTotal.value = 0
    collections.value = []
    collectionsTotal.value = 0
    myPosts.value = []
    myPostsTotal.value = 0
    myComments.value = []
    myCommentsTotal.value = 0
  }
  
  return {
    // 状态
    userStatus,
    posts,
    postsTotal,
    postsLoading,
    collections,
    collectionsTotal,
    collectionsLoading,
    myPosts,
    myPostsTotal,
    myPostsLoading,
    myComments,
    myCommentsTotal,
    myCommentsLoading,
    
    // 方法
    initCommunity,
    fetchUserStatus,
    fetchPosts,
    fetchCollections,
    fetchMyPosts,
    fetchMyComments,
    createPost,
    likePost,
    unlikePost,
    collectPost,
    uncollectPost,
    resetState
  }
}) 