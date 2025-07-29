import http from '../utils/request'
 
export const getClassList = () => {
  return http.post('/student/auth/list')
} 