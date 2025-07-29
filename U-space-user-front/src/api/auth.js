import http from '../utils/request'
import { securePost } from '../utils/secureRequest'

export const register = (data) => {
  return http.post('/student/auth/register', data)
}

export const login = (data) => {
  // 学生登录接口使用新的 securePost (AES/CBC + HMAC-SHA256)
  return securePost('/student/auth/login', data)
}

export const getInfo = () => {
  return http.get('/student/auth/info')
} 