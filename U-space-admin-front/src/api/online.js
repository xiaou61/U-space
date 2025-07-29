import request from '../utils/request'

export const listOnlineUsers = () => request.get('/online/users')
export const getOnlineCount = () => request.get('/online/count')
export const offlineUser = (userId) => request.post(`/online/offline/${userId}`) 