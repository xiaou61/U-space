import http from '../utils/request'
 
export const getSchoolInfo = () => http.post('/school/home/info') 