import request from '@/utils/request'

export const authApi = {
  // 用户登录
  login(data) {
    return request.post('/auth/login', data)
  },
  
  // 用户登出
  logout() {
    return request.post('/auth/logout')
  },
  
  // 刷新Token
  refreshToken() {
    return request.post('/auth/refresh')
  },
  
  // 获取用户信息
  getUserInfo() {
    return request.get('/auth/info')
  },
  
  // 获取登录日志（分页）
  getLoginLogs(params) {
    return request.get('/auth/login-logs', params)
  },
  
  // 获取登录日志详情
  getLoginLogById(id) {
    return request.get(`/auth/login-logs/${id}`)
  },
  
  // 清空登录日志
  clearLoginLogs() {
    return request.delete('/auth/login-logs')
  },
  
  // 更新个人信息
  updateProfile(data) {
    return request.put('/auth/profile', data)
  },
  
  // 修改密码
  changePassword(data) {
    return request.put('/auth/password', data)
  },
} 