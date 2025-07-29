import request from '../utils/request'

export const addClass = (data) => request.post('/admin/class/add', data)
export const deleteClass = (id) => request.delete('/admin/class/delete', { params: { id } })
export const updateClass = (id, data) => request.put('/admin/class/update', data, { params: { id } })
export const pageClass = (data) => request.post('/admin/class/page', data)
export const importClassExcel = (formData) => request.post('/admin/class/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const exportClassExcel = () => request.get('/admin/class/export', { responseType: 'blob' })
export const downloadClassTemplate = () => request.get('/admin/class/import/template', { responseType: 'blob' }) 