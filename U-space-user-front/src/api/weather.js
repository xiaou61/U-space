import http from '../utils/request'
 
export const getWeather = () => http.get('/school/home/weather') 