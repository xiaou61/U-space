import request from '../utils/request'

export const listCategories = () => request.get('/admin/post-categeteory/list')
export const addCategory = (data) => request.post('/admin/post-categeteory/add', data)
export const updateCategory = (id, data) => request.post('/admin/post-categeteory/update', data, { params: { id } })
export const deleteCategory = (id) => request.post('/admin/post-categeteory/delete', id, { headers: { 'Content-Type': 'application/json' } }) 