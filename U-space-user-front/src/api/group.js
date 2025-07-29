import http from '../utils/request'

// 获取我的群组列表
export const listGroups = () => http.post('/student/group/list')

// 加入群组
export const joinGroup = (id) => http.post('/student/group/join', null, {
  params: { id },
}) 