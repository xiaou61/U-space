import request from '@/utils/request'

export const userApi = {
  // 获取当前用户信息
  getCurrentUserInfo() {
    return request.get('/user/profile')
  },
  
  // 更新用户信息
  updateUserInfo(data) {
    return request.put('/user/profile', data)
  },
  
  // 修改密码
  changePassword(data) {
    return request.put('/user/password', data)
  },

  // 上传头像
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/user/avatar/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
} 