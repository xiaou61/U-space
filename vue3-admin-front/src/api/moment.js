import request from '@/utils/request'

/**
 * 朋友圈管理相关API
 */

// 获取动态列表（管理端）
export function getAdminMomentList(data) {
  return request.post('/admin/moments/list', data)
}

// 批量删除动态
export function batchDeleteMoments(data) {
  return request.post('/admin/moments/batch-delete', data)
}

// 获取评论列表（管理端）
export function getAdminCommentList(data) {
  return request.post('/admin/moments/comments/list', data)
}

// 删除评论（管理端）
export function deleteComment(id) {
  return request.delete(`/admin/moments/comments/${id}`)
}

// 获取统计数据
export function getMomentStatistics(data) {
  return request.post('/admin/moments/statistics', data)
} 