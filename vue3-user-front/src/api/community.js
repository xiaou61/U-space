import request from '@/utils/request'

export const communityApi = {
  // 获取帖子列表
  getPostList(params) {
    return request.post('/community/posts/list', params)
  },

  // 获取帖子详情
  getPostDetail(id) {
    return request.get(`/community/posts/${id}`)
  },

  // 创建帖子
  createPost(data) {
    return request.post('/community/posts', data)
  },

  // 点赞帖子
  likePost(id) {
    return request.post(`/community/posts/${id}/like`)
  },

  // 取消点赞帖子
  unlikePost(id) {
    return request.delete(`/community/posts/${id}/like`)
  },

  // 收藏帖子
  collectPost(id) {
    return request.post(`/community/posts/${id}/collect`)
  },

  // 取消收藏帖子
  uncollectPost(id) {
    return request.delete(`/community/posts/${id}/collect`)
  },

  // 获取帖子评论
  getPostComments(id, params) {
    return request.post(`/community/posts/${id}/comments`, params)
  },

  // 创建评论
  createComment(postId, data) {
    return request.post(`/community/posts/${postId}/comments/create`, data)
  },

  // 点赞评论
  likeComment(id) {
    return request.post(`/community/comments/${id}/like`)
  },

  // 获取我的帖子
  getMyPosts(params) {
    return request.post('/community/user/posts', params)
  },

  // 获取我的收藏
  getMyCollections(params) {
    return request.post('/community/user/collections', params)
  },

  // 别名，与后端方法名保持一致
  getUserPosts(params) {
    return this.getMyPosts(params)
  },

  // 别名，与后端方法名保持一致  
  getUserCollections(params) {
    return this.getMyCollections(params)
  },

  // 获取启用的分类列表
  getEnabledCategories() {
    return request.get('/community/categories')
  },

  // 社区初始化
  init() {
    return request.get('/community/init')
  }
} 