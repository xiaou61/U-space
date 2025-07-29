import request from '../utils/request'

export const pageOperLog = (data) => request.post('/monitor/operlog/list', data)
export const exportOperLogExcel = (beginTime, endTime) =>
  request.post('/monitor/operlog/export', { beginTime, endTime }, { responseType: 'blob' }) 