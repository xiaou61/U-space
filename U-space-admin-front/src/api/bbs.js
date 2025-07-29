import request from '../utils/request'

export const listBbsAdmins = () => request.get('/admin/bbs/list')
export const addBbsAdmin = (username, password) => request.post('/admin/bbs/add', null, { params: { username, password } })
export const deleteBbsAdmin = (id) => request.post('/admin/bbs/delete', null, { params: { id } }) 