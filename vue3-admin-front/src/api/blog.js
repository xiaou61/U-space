import request from '@/utils/request'

// ========== 博客管理 ==========

/**
 * 获取博客统计数据
 */
export function getStatistics() {
  return request.get('/admin/blog/statistics')
}

// ========== 文章管理 ==========

/**
 * 获取文章列表（管理端）
 */
export function getArticleList(data) {
  return request.post('/admin/blog/article/list', data)
}

/**
 * 置顶文章
 */
export function topArticle(id, duration) {
  return request.post('/admin/blog/article/top', null, {
    params: { id, duration }
  })
}

/**
 * 取消置顶
 */
export function cancelTop(id) {
  return request.post('/admin/blog/article/cancel-top', null, {
    params: { id }
  })
}

/**
 * 更新文章状态
 */
export function updateArticleStatus(id, status) {
  return request.post('/admin/blog/article/update-status', null, {
    params: { id, status }
  })
}

/**
 * 删除文章
 */
export function deleteArticle(id) {
  return request.delete(`/admin/blog/article/${id}`)
}

// ========== 分类管理 ==========

/**
 * 获取分类列表
 */
export function getCategoryList(params) {
  return request.get('/admin/blog/category/list', params)
}

/**
 * 创建分类
 */
export function createCategory(data) {
  return request.post('/admin/blog/category/create', data)
}

/**
 * 更新分类
 */
export function updateCategory(id, data) {
  return request.post('/admin/blog/category/update', data, {
    params: { id }
  })
}

/**
 * 删除分类
 */
export function deleteCategory(id) {
  return request.delete(`/admin/blog/category/${id}`)
}

// ========== 标签管理 ==========

/**
 * 获取标签列表
 */
export function getTagList(params) {
  return request.get('/admin/blog/tag/list', params)
}

/**
 * 合并标签
 */
export function mergeTags(sourceTagId, targetTagId) {
  return request.post('/admin/blog/tag/merge', null, {
    params: { sourceTagId, targetTagId }
  })
}

/**
 * 删除标签
 */
export function deleteTag(id) {
  return request.delete(`/admin/blog/tag/${id}`)
}

