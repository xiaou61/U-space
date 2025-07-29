import http from '../utils/request'
 
// 获取我的资料列表
export const listMaterials = (groupId) =>
  http.post('/student/material/list', null, { params: { groupId } }) 