import request from '../utils/request'

// 获取当前用户的历史对话列表
export function listChatHistory() {
  return request({
    url: '/ai/chat/list',
    method: 'get',
  })
} 