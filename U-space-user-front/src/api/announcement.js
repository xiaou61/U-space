import http from '../utils/request'

// 获取公告列表
export const listAnnouncements = () => http.post('/bulletin/list') 