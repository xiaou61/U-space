import request from '@/utils/request'

/**
 * 发布系统公告
 * @param {Object} data 公告数据
 */
export const publishAnnouncement = (data) => {
  return request.post('/admin/notification/announcement', data)
}

/**
 * 获取消息统计信息
 * @param {Object} data 统计查询参数
 */
export const getStatistics = (data) => {
  return request.post('/admin/notification/statistics', data)
}

/**
 * 管理端查询所有消息列表
 * @param {Object} data 查询参数
 */
export const getAllMessages = (data) => {
  return request.post('/admin/notification/list', data)
}

/**
 * 批量发送个人消息
 * @param {Object} data 批量发送数据
 */
export const batchSendMessage = (data) => {
  return request.post('/admin/notification/batch-send', data)
}

/**
 * 删除消息（管理员操作）
 * @param {Number} id 消息ID
 */
export const deleteMessage = (id) => {
  return request.post(`/admin/notification/delete/${id}`)
}

/**
 * 获取所有模板列表
 */
export const getTemplates = () => {
  return request.get('/admin/notification/templates')
}

/**
 * 创建消息模板
 * @param {Object} data 模板数据
 */
export const createTemplate = (data) => {
  return request.post('/admin/notification/templates', data)
}

/**
 * 更新消息模板
 * @param {Number} id 模板ID
 * @param {Object} data 模板数据
 */
export const updateTemplate = (id, data) => {
  return request.put(`/admin/notification/templates/${id}`, data)
}

/**
 * 删除消息模板
 * @param {Number} id 模板ID
 */
export const deleteTemplate = (id) => {
  return request.delete(`/admin/notification/templates/${id}`)
} 