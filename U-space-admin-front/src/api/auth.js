import request from '../utils/request'

export const login = (data) => request.post('/admin/auth/login', data)

export const getInfo = () => request.get('/admin/auth/info')

export const logout = () => request.get('/admin/auth/logout')

export const updatePassword = (oldPassword, newPassword) =>
  request.post('/admin/auth/updatePassword', null, {
    params: { oldPassword, newPassword }
  })

export const getRole = () => request.get('/admin/auth/role') 