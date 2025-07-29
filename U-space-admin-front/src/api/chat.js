import request from '../utils/request'

// 获取所有对话消息（仅管理员）
export const listAllChats = () => request.get('/ai/chat/list/all') 