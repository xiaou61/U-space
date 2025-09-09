import request from '@/utils/request'

/**
 * 朋友圈相关API
 */

// 发布动态
export function publishMoment(data) {
  return request.post('/user/moments/publish', data)
}

// 获取动态列表
export function getMomentList(data) {
  return request.post('/user/moments/list', data)
}

// 删除动态
export function deleteMoment(id) {
  return request.delete(`/user/moments/${id}`)
}

// 点赞/取消点赞
export function toggleLike(momentId) {
  return request.post(`/user/moments/${momentId}/like`)
}

// 发布评论
export function publishComment(data) {
  return request.post('/user/moments/comment', data)
}

// 删除评论
export function deleteComment(id) {
  return request.delete(`/user/moments/comments/${id}`)
}

// 获取动态的完整评论列表
export function getMomentComments(data) {
  return request.post('/user/moments/comments', data)
} 