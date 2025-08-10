import request from '../utils/request'

// 获取管理员用户列表
export function getAdminUserList() {
  return request({
    url: '/admin/user/list',
    method: 'get'
  })
}

// 添加管理员用户
export function addAdminUser(data) {
  return request({
    url: '/admin/user/add',
    method: 'post',
    data
  })
}

// 更新管理员用户信息
export function updateAdminUser(data) {
  return request({
    url: '/admin/user/update',
    method: 'put',
    data
  })
}

// 删除管理员用户
export function deleteAdminUser(userId) {
  return request({
    url: `/admin/user/delete/${userId}`,
    method: 'delete'
  })
}

// 重置用户密码
export function resetPassword(userId, newPassword) {
  return request({
    url: '/admin/user/reset-password',
    method: 'put',
    data: { userId, newPassword }
  })
} 