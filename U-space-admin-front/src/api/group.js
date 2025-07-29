import request from '../utils/request'
export const addGroup       = (data) => request.post('/teacher/group/add', data)
export const deleteGroup    = (id)   => request.post('/teacher/group/delete', null, { params: { id } })
export const listGroups     = ()     => request.post('/teacher/group/list')
export const generateId     = (groupId) => request.post('/teacher/group/generateId', null, { params: { groupId } }) 
export const listGroupMembers = (groupId) => request.post('/teacher/group/member', null, { params: { groupId } }) 