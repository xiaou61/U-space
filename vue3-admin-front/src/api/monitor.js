import request from '@/utils/request'

export const monitorApi = {
  // ============ SQL监控日志 ============
  
  // 分页查询SQL监控日志
  getSqlLogs(data) {
    return request.post('/monitor/sql/logs', data)
  },
  
  // 查询慢SQL列表
  getSlowSqlLogs(data) {
    return request.post('/monitor/sql/slow-sql', data)
  },
  
  // 查询实时监控数据
  getRealtimeData(params) {
    return request.get('/monitor/sql/realtime', params)
  },
  
  // ============ 统计信息 ============
  
  // 查询SQL统计信息
  getStatistics(params) {
    return request.get('/monitor/sql/statistics', params)
  },
  
  // 查询SQL执行频次统计
  getFrequencyStatistics(params) {
    return request.get('/monitor/sql/frequency', params)
  },
  
  // ============ 数据清理 ============
  
  // 清理过期数据
  deleteExpiredData(retentionDays = 30) {
    return request.delete('/monitor/sql/expired', { 
      params: { retentionDays }
    })
  },
  
  // 清空所有监控数据
  clearAllData() {
    return request.delete('/monitor/sql/all')
  }
} 