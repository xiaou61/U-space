import request from '@/utils/request'

export const codepenApi = {
  // ========== 作品管理 ==========
  
  // 创建/保存作品（首次发布奖励10积分）
  createPen(data) {
    return request.post('/user/code-pen/create', data)
  },

  // 保存作品
  savePen(data) {
    return request.post('/user/code-pen/save', data)
  },

  // 更新作品
  updatePen(data) {
    return request.post('/user/code-pen/update', data)
  },

  // 删除作品
  deletePen(id) {
    return request.delete(`/user/code-pen/${id}`)
  },

  // 获取作品详情
  getPenDetail(id) {
    return request.get(`/user/code-pen/${id}`)
  },

  // 我的作品列表
  getMyPens(params) {
    return request.post('/user/code-pen/my-list', params)
  },

  // 我的草稿列表
  getMyDrafts() {
    return request.post('/user/code-pen/draft-list')
  },

  // 检查Fork价格和用户积分
  checkForkPrice(penId) {
    return request.post('/user/code-pen/check-fork-price', { penId })
  },

  // Fork作品（免费或付费）
  forkPen(penId) {
    return request.post('/user/code-pen/fork', { penId })
  },

  // 查看收益统计
  getIncomeStats() {
    return request.post('/user/code-pen/income-stats')
  },

  // ========== 作品广场 ==========
  
  // 获取作品列表（代码广场）
  getPenList(params) {
    return request.post('/user/code-pen/list', params)
  },

  // 获取推荐作品列表
  getRecommendPens() {
    return request.post('/user/code-pen/recommend-list')
  },

  // 获取热门作品
  getHotPens() {
    return request.get('/user/code-pen/hot')
  },

  // 搜索作品
  searchPens(params) {
    return request.post('/user/code-pen/search', params)
  },

  // 按标签获取作品
  getPensByTag(params) {
    return request.post('/user/code-pen/by-tag', params)
  },

  // 按分类获取作品
  getPensByCategory(params) {
    return request.post('/user/code-pen/by-category', params)
  },

  // 获取指定用户的作品
  getPensByUser(params) {
    return request.post('/user/code-pen/by-user', params)
  },

  // ========== 互动功能 ==========
  
  // 点赞作品
  likePen(penId) {
    return request.post('/user/code-pen/like', { penId })
  },

  // 取消点赞
  unlikePen(penId) {
    return request.post('/user/code-pen/unlike', { penId })
  },

  // 收藏作品
  collectPen(penId) {
    return request.post('/user/code-pen/collect', { penId })
  },

  // 取消收藏
  uncollectPen(penId) {
    return request.post('/user/code-pen/uncollect', { penId })
  },

  // 增加浏览数
  incrementView(penId) {
    return request.post('/user/code-pen/view', { penId })
  },

  // 发表评论
  createComment(data) {
    return request.post('/user/code-pen/comment', data)
  },

  // 删除评论
  deleteComment(id) {
    return request.delete(`/user/code-pen/comment/${id}`)
  },

  // 获取评论列表
  getComments(penId) {
    return request.post('/user/code-pen/comment-list', { penId })
  },

  // ========== 收藏夹管理 ==========
  
  // 创建收藏夹
  createFolder(data) {
    return request.post('/user/code-pen/folder/create', data)
  },

  // 更新收藏夹
  updateFolder(data) {
    return request.post('/user/code-pen/folder/update', data)
  },

  // 删除收藏夹
  deleteFolder(id) {
    return request.delete(`/user/code-pen/folder/${id}`)
  },

  // 我的收藏夹列表
  getFolders() {
    return request.post('/user/code-pen/folder/list')
  },

  // 收藏夹内容列表
  getFolderItems(folderId) {
    return request.post('/user/code-pen/folder/items', { id: folderId })
  },

  // ========== 模板和标签 ==========
  
  // 获取系统模板列表
  getTemplates() {
    return request.get('/user/code-pen/templates')
  },

  // 获取模板详情
  getTemplateDetail(id) {
    return request.get(`/user/code-pen/template/${id}`)
  },

  // 获取所有标签
  getTags() {
    return request.get('/user/code-pen/tags')
  },

  // 获取热门标签
  getHotTags() {
    return request.get('/user/code-pen/tags/hot')
  }
}

