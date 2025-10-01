import request from '@/utils/request'

/**
 * 聊天室相关API
 */

// 获取历史消息
export function getChatHistory(data) {
  return request.post('/user/chat/history', data)
}

// 获取在线人数
export function getOnlineCount() {
  return request.post('/user/chat/online-count')
}

// 获取在线用户列表
export function getOnlineUsers() {
  return request.post('/user/chat/online-users')
}

// 撤回消息
export function recallMessage(data) {
  return request.post('/user/chat/message/recall', data)
}
