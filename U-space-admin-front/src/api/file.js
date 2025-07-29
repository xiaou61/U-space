import request from '../utils/request'
 
export const uploadFile = (formData) => request.post('/file-upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const uploadBatch = (formData) => request.post('/file-upload/batch', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const pageFiles = (data) => request.post('/file-upload/list', data) 