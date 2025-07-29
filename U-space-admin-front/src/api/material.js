import request from '../utils/request'
 
export const addMaterial = (data) => request.post('/teacher/material/add', data)
export const deleteMaterial = (id) => request.post('/teacher/material/delete', null, { params: { id } })
export const pageMaterials = (data) => request.post('/teacher/material/listpage', data) 