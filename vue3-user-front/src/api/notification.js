import request from '@/utils/request'

/**
 * 获取消息列表
 * @param {Object} data 查询参数
 */
export const getMessages = (data) => {
  return request.post('/notification/list', data)
}

/**
 * 获取未读消息数量
 */
export const getUnreadCount = () => {
  return request.get('/notification/unread-count')
}

/**
 * 获取消息详情
 * @param {Number} id 消息ID
 */
export const getMessageDetail = (id) => {
  return request.get(`/notification/${id}`)
}

/**
 * 标记消息已读
 * @param {Object} data 标记已读参数
 */
export const markAsRead = (data) => {
  return request.post('/notification/mark-read', data)
}

/**
 * 删除消息
 * @param {Object} data 删除参数
 */
export const deleteMessage = (data) => {
  return request.post('/notification/delete', data)
}

/**
 * 全部标记已读
 */
export const markAllAsRead = () => {
  return request.post('/notification/mark-all-read')
} 