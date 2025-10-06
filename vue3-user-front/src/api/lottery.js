import request from '@/utils/request'

export const lotteryApi = {
  // 执行抽奖
  draw(data) {
    return request.post('/user/lottery/draw', data)
  },
  
  // 获取奖品列表
  getPrizeList() {
    return request.get('/user/lottery/prizes')
  },
  
  // 获取用户抽奖记录
  getDrawRecords(data) {
    return request.post('/user/lottery/records', data)
  },
  
  // 获取用户抽奖统计
  getStatistics() {
    return request.get('/user/lottery/statistics')
  },
  
  // 获取抽奖规则
  getRules() {
    return request.get('/user/lottery/rules')
  },
  
  // 获取今日剩余抽奖次数
  getRemainingCount() {
    return request.get('/user/lottery/remaining-count')
  }
}

