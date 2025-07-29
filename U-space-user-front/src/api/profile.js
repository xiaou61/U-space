import http from '../utils/request'

// 获取个人信息
export const getSelfInfo = () => http.get('/student/auth/info')

// 上传头像文件，返回 URL
export const uploadAvatar = (file) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/student/auth/uoloadavatar', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 更新头像记录，将上传得到的 url 写入用户信息
export const updateAvatar = (avatarUrl) => http.post('/student/auth/avatar', null, {
  params: { avatar: avatarUrl },
})

// 修改密码
export const updatePassword = (oldPassword, newPassword) => http.post('/student/auth/password', null, {
  params: { oldPassword, newPassword },
})

// 用户登出
export const logout = () => http.post('/student/auth/logout') 