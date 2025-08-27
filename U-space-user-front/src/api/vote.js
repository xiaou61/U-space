import http from '../utils/request'

// 获取所有有效的投票活动
export const getActiveVoteActivities = () => {
  return http.get('/vote/activities')
}

// 用户投票
export const submitVote = (activityId, optionId) => {
  return http.post('/vote/submit', null, {
    params: {
      activityId,
      optionId
    }
  })
}

// 检查用户是否已投票
export const checkUserVoted = (activityId) => {
  return http.get(`/vote/check/${activityId}`)
} 