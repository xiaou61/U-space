import request from '../utils/request'

export const pageStudent = (data) => request.post('/admin/student/page', data)
export const getUnauditedStudents = () => request.get('/admin/student/page/unaudited')
export const auditStudent = (id) => request.post('/admin/student/audit', null, { params: { id } }) 