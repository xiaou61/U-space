import request from '../utils/request'

export const addHomework = (data) => request.post('/teacher/homework/add', data)
export const listHomework = (groupId) => request.post('/teacher/homework/list', null, { params: { groupId } })
export const detailHomework = (id) => request.post('/teacher/homework/homework-detail', null, { params: { id } })
export const listSubmissions = (id) => request.post('/teacher/homework/detail', null, { params: { id } })
export const getSubmission = (id) => request.post('/teacher/homework/homework', null, { params: { id } })
export const approveSubmission = (id, data) => request.post('/teacher/homework/approve', data, { params: { id } }) 