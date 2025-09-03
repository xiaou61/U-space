import request from '@/utils/request'

export const authApi = {
  // 用户登录
  login(data) {
    return request.post('/user/auth/login', data)
  },
  
  // 用户注册
  register(data) {
    return request.post('/user/auth/register', data)
  },
  
  // 刷新Token
  refresh() {
    return request.post('/user/auth/refresh')
  },
  
  // 用户登出
  logout() {
    return request.post('/user/auth/logout')
  },
  
  // 检查用户名是否可用
  checkUsername(username) {
    return request.get('/user/auth/check-username', { username })
  },
  
  // 检查邮箱是否可用
  checkEmail(email) {
    return request.get('/user/auth/check-email', { email })
  }
} 