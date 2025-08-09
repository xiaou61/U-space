import request from '../utils/request'

export function login(data) {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data
  })
}

export function getInfo() {
  return request({
    url: '/admin/auth/info',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/admin/auth/logout',
    method: 'get'
  })
}

export function updatePassword(oldPassword, newPassword) {
  return request({
    url: '/admin/auth/updatePassword',
    method: 'post',
    data: { oldPassword, newPassword }
  })
}

export function getRole() {
  return request({
    url: '/admin/auth/role',
    method: 'get'
  })
}

// 获取当前用户的权限列表
export function getUserPermissions() {
  return request({
    url: '/admin/user/permissions',
    method: 'get'
  })
} 