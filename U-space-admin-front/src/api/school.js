import request from '../utils/request'
 
export const getSchoolInfo = () => request.post('/school/home/info')
export const updateSchoolInfo = (data) => request.post('/school/home/update', data) 