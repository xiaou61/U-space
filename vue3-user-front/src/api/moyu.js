import request from '@/utils/request'

/**
 * 摸鱼工具 API
 */

/**
 * 获取热榜分类信息
 */
export const getHotTopicCategories = () => {
  return request.get('/moyu/hot-topic/categories')
}

/**
 * 获取指定平台热榜数据
 * @param {string} platform - 平台代码
 */
export const getHotTopicData = (platform) => {
  return request.get(`/moyu/hot-topic/data/${platform}`)
}

/**
 * 获取所有平台热榜数据
 */
export const getAllHotTopicData = () => {
  return request.get('/moyu/hot-topic/data/all')
}

/**
 * 手动刷新热榜数据
 */
export const refreshHotTopicData = () => {
  return request.post('/moyu/hot-topic/refresh')
}
