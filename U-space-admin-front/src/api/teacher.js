import request from '../utils/request'
 
export const addTeacher = (data) => request.post('/admin/teacher/add', data)
export const deleteTeacher = (id) => request.delete('/admin/teacher/delete', { params: { id } })
export const pageTeacher = (data) => request.post('/admin/teacher/page', data) 