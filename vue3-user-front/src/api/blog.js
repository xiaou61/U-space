import request from '@/utils/request'

// ========== 博客配置相关 ==========

/**
 * 开通博客（扣除50积分）
 */
export function openBlog() {
  return request.post('/user/blog/open')
}

/**
 * 检查博客开通状态
 */
export function checkBlogStatus() {
  return request.get('/user/blog/check-status')
}

/**
 * 获取博客配置
 */
export function getBlogConfig(userId) {
  return request.get(`/user/blog/config/${userId}`)
}

/**
 * 更新博客配置
 */
export function updateBlogConfig(data) {
  return request.post('/user/blog/config/update', data)
}

// ========== 文章管理相关 ==========

/**
 * 创建文章草稿
 */
export function createArticle(data) {
  return request.post('/user/blog/article/create', data)
}

/**
 * 发布文章（扣除20积分）
 */
export function publishArticle(data) {
  return request.post('/user/blog/article/publish', data)
}

/**
 * 更新文章
 */
export function updateArticle(id, data) {
  return request.post(`/user/blog/article/update/${id}`, data)
}

/**
 * 删除文章
 */
export function deleteArticle(id) {
  return request.delete(`/user/blog/article/${id}`)
}

/**
 * 获取文章详情
 */
export function getArticleDetail(id) {
  return request.get(`/user/blog/article/${id}`)
}

/**
 * 获取用户的文章列表
 */
export function getUserArticleList(data) {
  return request.post('/user/blog/article/list', data)
}

/**
 * 获取我的文章列表
 */
export function getMyArticleList(data) {
  return request.post('/user/blog/article/my-list', data)
}

/**
 * 获取我的草稿列表
 */
export function getMyDraftList(pageNum = 1, pageSize = 10) {
  return request.post('/user/blog/article/draft-list', null, {
    params: { pageNum, pageSize }
  })
}

/**
 * 按分类获取文章
 */
export function getArticlesByCategory(categoryId, pageNum = 1, pageSize = 10) {
  return request.post('/user/blog/article/by-category', null, {
    params: { categoryId, pageNum, pageSize }
  })
}

// ========== 分类标签相关 ==========

/**
 * 获取所有分类
 */
export function getAllCategories() {
  return request.get('/user/blog/categories')
}

/**
 * 获取所有标签
 */
export function getAllTags() {
  return request.get('/user/blog/tags')
}

/**
 * 获取热门标签
 */
export function getHotTags(limit = 10) {
  return request.get('/user/blog/tags/hot', { limit })
}

