import request from '../utils/request'

export const addSignin = (data) => request.post('/teacher/signin/add', data)

export const listSignins = (data) => request.post('/teacher/signin/list', data)

export const detailSignin = (id) => request.post('/teacher/signin/detail', null, { params: { id } }) 