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

/**
 * 时薪计算器 API
 */

/**
 * 获取时薪计算器数据
 */
export const getSalaryCalculatorData = () => {
  return request.get('/moyu/salary-calculator/data')
}

/**
 * 获取用户薪资配置
 */
export const getSalaryConfig = () => {
  return request.get('/moyu/salary-calculator/config')
}

/**
 * 保存或更新薪资配置
 * @param {Object} config - 薪资配置
 */
export const saveOrUpdateSalaryConfig = (config) => {
  return request.post('/moyu/salary-calculator/config', config)
}

/**
 * 删除薪资配置
 */
export const deleteSalaryConfig = () => {
  return request.delete('/moyu/salary-calculator/config')
}

/**
 * 工作时间操作（开始/结束工作）
 * @param {Object} action - 操作信息
 */
export const handleWorkTime = (action) => {
  return request.post('/moyu/salary-calculator/work-time', action)
}