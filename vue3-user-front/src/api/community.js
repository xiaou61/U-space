import request from '@/utils/request'

// 获取帖子列表
export function getPostList(params) {
  return request.get('/community/posts', params)
}

// 获取帖子详情
export function getPostDetail(id) {
  return request.get(`/community/posts/${id}`)
}

// 创建帖子
export function createPost(data) {
  return request.post('/community/posts', data)
}

// 点赞帖子
export function likePost(id) {
  return request.post(`/community/posts/${id}/like`)
}

// 收藏帖子
export function collectPost(id) {
  return request.post(`/community/posts/${id}/collect`)
}

// 获取帖子评论
export function getPostComments(id, params) {
  return request.get(`/community/posts/${id}/comments`, params)
}

// 创建评论
export function createComment(data) {
  return request.post('/community/comments', data)
}

// 点赞评论
export function likeComment(id) {
  return request.post(`/community/comments/${id}/like`)
}

// 获取我的帖子
export function getMyPosts(params) {
  return request.get('/community/posts/my', params)
}

// 获取我的收藏
export function getMyCollections(params) {
  return request.get('/community/posts/collections', params)
}

// 获取启用的分类列表（新增）
export function getEnabledCategories() {
  return request.get('/community/categories')
}

export const communityApi = {
  getPostList,
  getPostDetail,
  createPost,
  likePost,
  collectPost,
  getPostComments,
  createComment,
  likeComment,
  getMyPosts,
  getMyCollections,
  getEnabledCategories
} 