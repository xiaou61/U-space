import request from '../utils/request'

// 角色管理相关API
export function getRoleList() {
  return request({
    url: '/admin/role/list',
    method: 'get'
  })
}

export function addRole(data) {
  return request({
    url: '/admin/role/add',
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: '/admin/role/update',
    method: 'put',
    data
  })
}

export function deleteRole(roleCode) {
  return request({
    url: '/admin/role/delete',
    method: 'delete',
    params: { roleCode }
  })
}

// 权限管理相关API
export function getMenuPermissions() {
  return request({
    url: '/admin/permission/menu',
    method: 'get'
  })
}

export function getRolePermissions(roleCode) {
  return request({
    url: '/admin/role/permissions',
    method: 'get',
    params: { roleCode }
  })
}

export function updateRolePermissions(roleCode, permissions) {
  return request({
    url: '/admin/role/permissions',
    method: 'put',
    data: { roleCode, permissions }
  })
}

// 用户角色管理相关API
export function getUserList() {
  return request({
    url: '/admin/user/list',
    method: 'get'
  })
}

export function getUserRoles(userId) {
  return request({
    url: '/admin/user/roles',
    method: 'get',
    params: { userId }
  })
}

export function updateUserRoles(userId, roles) {
  return request({
    url: '/admin/user/roles',
    method: 'put',
    data: { userId, roles }
  })
}

// 获取所有用户及其角色信息
export function getUsersWithRoles() {
  return request({
    url: '/admin/user/with-roles',
    method: 'get'
  })
} 