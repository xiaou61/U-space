import request from '../utils/request'

// 获取所有权限列表
export function getAllPermissions() {
  return request({
    url: '/admin/menu/list',
    method: 'get'
  })
}

// 获取权限树
export function getPermissionTree() {
  return request({
    url: '/admin/menu/tree',
    method: 'get'
  })
}

// 根据权限ID获取权限详情
export function getPermissionById(permissionId) {
  return request({
    url: `/admin/menu/${permissionId}`,
    method: 'get'
  })
}

// 根据父权限ID获取子权限列表
export function getChildPermissions(parentId = null) {
  return request({
    url: '/admin/menu/children',
    method: 'get',
    params: { parentId }
  })
}

// 添加权限
export function addPermission(data) {
  return request({
    url: '/admin/menu/add',
    method: 'post',
    data
  })
}

// 更新权限
export function updatePermission(data) {
  return request({
    url: '/admin/menu/update',
    method: 'put',
    data
  })
}

// 删除权限
export function deletePermission(permissionId) {
  return request({
    url: `/admin/menu/delete/${permissionId}`,
    method: 'delete'
  })
}

// 批量删除权限
export function batchDeletePermissions(permissionIds) {
  return request({
    url: '/admin/menu/batch-delete',
    method: 'delete',
    data: permissionIds
  })
}

// 更新权限排序
export function updatePermissionSort(permissionId, sortOrder) {
  return request({
    url: '/admin/menu/sort',
    method: 'put',
    data: { permissionId, sortOrder }
  })
} 