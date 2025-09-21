import request from '@/utils/request'

/**
 * 版本历史管理API
 */

// 获取版本历史列表（分页）
export function getVersionList(params) {
  return request.post('/admin/version/list', params)
}

// 获取版本历史详情
export function getVersionDetail(id) {
  return request.get(`/admin/version/${id}`)
}

// 创建版本历史记录
export function createVersion(data) {
  return request.post('/admin/version/create', data)
}

// 更新版本历史记录
export function updateVersion(data) {
  return request.post('/admin/version/update', data)
}

// 删除版本历史记录
export function deleteVersion(id) {
  return request.post('/admin/version/delete', id)
}

// 发布版本
export function publishVersion(id) {
  return request.post('/admin/version/publish', id)
}

// 隐藏版本
export function hideVersion(id) {
  return request.post('/admin/version/hide', id)
}

// 取消发布版本（设置为草稿）
export function unpublishVersion(id) {
  return request.post('/admin/version/unpublish', id)
}

// 批量发布版本
export function batchPublishVersions(ids) {
  return request.post('/admin/version/batch/publish', ids)
}

// 批量隐藏版本
export function batchHideVersions(ids) {
  return request.post('/admin/version/batch/hide', ids)
}

// 批量删除版本
export function batchDeleteVersions(ids) {
  return request.post('/admin/version/batch/delete', ids)
}

// 检查版本号是否已存在
export function checkVersionNumberExists(versionNumber, excludeId = null) {
  const params = excludeId ? { excludeId } : {}
  return request.get(`/admin/version/check-version/${encodeURIComponent(versionNumber)}`, params)
}

// 版本管理API导出对象
export const versionApi = {
  getVersionList,
  getVersionDetail,
  createVersion,
  updateVersion,
  deleteVersion,
  publishVersion,
  hideVersion,
  unpublishVersion,
  batchPublishVersions,
  batchHideVersions,
  batchDeleteVersions,
  checkVersionNumberExists
} 