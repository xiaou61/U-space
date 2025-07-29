import http from '../utils/request'

// Check whether current user needs to fill in extra info before dorm selection
export const isNeedInputInfo = () => http.get('/dorm/isNeedInputInfo')

// Submit additional info (gender, idCard)
export const inputInfo = (data) => http.post('/dorm/inputInfo', data)

// List available dorms for current user
export const listDorms = () => http.get('/dorm/list') 