import request from '@/utils/request'

// 获取帖子列表
export function getPostList(params) {
  return request.get('/admin/community/posts', params)
}

// 获取帖子详情
export function getPostDetail(id) {
  return request.get(`/admin/community/posts/${id}`)
}

// 删除帖子
export function deletePost(id) {
  return request.delete(`/admin/community/posts/${id}`)
}

// 置顶帖子
export function togglePostTop(id) {
  return request.put(`/admin/community/posts/${id}/top`)
}

// 获取评论列表
export function getCommentList(params) {
  return request.get('/admin/community/comments', params)
}

// 删除评论
export function deleteComment(id) {
  return request.delete(`/admin/community/comments/${id}`)
}

// 获取用户状态列表
export function getUserStatusList(params) {
  return request.get('/admin/community/users', params)
}

// 封禁用户
export function banUser(id, data) {
  return request.put(`/admin/community/users/${id}/ban`, data)
}

// 解封用户
export function unbanUser(id) {
  return request.put(`/admin/community/users/${id}/unban`)
}

// ============ 分类管理接口（新增）============

// 获取分类列表
export function getCategoryList(params) {
  return request.get('/admin/community/categories', params)
}

// 获取分类详情
export function getCategoryDetail(id) {
  return request.get(`/admin/community/categories/${id}`)
}

// 创建分类
export function createCategory(data) {
  return request.post('/admin/community/categories', data)
}

// 更新分类
export function updateCategory(id, data) {
  return request.put(`/admin/community/categories/${id}`, data)
}

// 删除分类
export function deleteCategory(id) {
  return request.delete(`/admin/community/categories/${id}`)
}

// 切换分类状态
export function toggleCategoryStatus(id) {
  return request.put(`/admin/community/categories/${id}/status`)
}

export const communityApi = {
  // 帖子相关
  getPostList,
  getPostDetail,
  deletePost,
  togglePostTop,
  
  // 评论相关
  getCommentList,
  deleteComment,
  
  // 用户相关
  getUserStatusList,
  banUser,
  unbanUser,
  
  // 分类相关
  getCategoryList,
  getCategoryDetail,
  createCategory,
  updateCategory,
  deleteCategory,
  toggleCategoryStatus
} 