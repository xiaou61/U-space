import request from '@/utils/request'

export const codepenApi = {
  // ========== 作品管理 ==========
  
  // 获取作品列表（管理端）
  getPenList(params) {
    return request.post('/admin/code-pen/list', params)
  },

  // 获取作品详情
  getPenDetail(id) {
    return request.get(`/admin/code-pen/${id}`)
  },

  // 更新作品状态
  updatePenStatus(data) {
    return request.post('/admin/code-pen/update-status', data)
  },

  // 设置推荐
  setRecommend(data) {
    return request.post('/admin/code-pen/recommend', data)
  },

  // 取消推荐
  cancelRecommend(penId) {
    return request.post('/admin/code-pen/cancel-recommend', { penId })
  },

  // 删除作品（管理端）
  deletePen(id) {
    return request.delete(`/admin/code-pen/${id}`)
  },

  // ========== 模板管理 ==========
  
  // 创建系统模板
  createTemplate(data) {
    return request.post('/admin/code-pen/template/create', data)
  },

  // 更新模板
  updateTemplate(data) {
    return request.post('/admin/code-pen/template/update', data)
  },

  // 删除模板
  deleteTemplate(id) {
    return request.delete(`/admin/code-pen/template/${id}`)
  },

  // 模板列表
  getTemplateList() {
    return request.post('/admin/code-pen/template/list')
  },

  // ========== 标签管理 ==========
  
  // 创建标签
  createTag(data) {
    return request.post('/admin/code-pen/tag/create', data)
  },

  // 更新标签
  updateTag(data) {
    return request.post('/admin/code-pen/tag/update', data)
  },

  // 删除标签
  deleteTag(id) {
    return request.delete(`/admin/code-pen/tag/${id}`)
  },

  // 合并标签
  mergeTags(data) {
    return request.post('/admin/code-pen/tag/merge', data)
  },

  // 标签列表
  getTagList() {
    return request.post('/admin/code-pen/tag/list')
  },

  // ========== 评论管理 ==========
  
  // 评论列表
  getCommentList(penId) {
    return request.post('/admin/code-pen/comment/list', { penId })
  },

  // 隐藏评论
  hideComment(id) {
    return request.post('/admin/code-pen/comment/hide', { id })
  },

  // 删除评论
  deleteComment(id) {
    return request.delete(`/admin/code-pen/comment/${id}`)
  },

  // ========== 统计数据 ==========
  
  // 获取统计数据
  getStatistics() {
    return request.get('/admin/code-pen/statistics')
  }
}

