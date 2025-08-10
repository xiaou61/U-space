import request from '../utils/request'

// 获取投票活动列表（分页）
export function getVoteActivityList(pageReqDto) {
  return request({
    url: '/admin/vote/list',
    method: 'post',
    data: pageReqDto
  })
}

// 新增或修改投票活动
export function editVoteActivity(data) {
  return request({
    url: '/admin/vote/edit',
    method: 'post',
    data
  })
}

// 下架投票活动
export function offlineVoteActivity(id) {
  return request({
    url: '/admin/vote/offline',
    method: 'post',
    params: { id }
  })
}

// 新增或修改投票选项
export function editVoteOption(activityId, data) {
  return request({
    url: '/admin/vote/addOption',
    method: 'post',
    params: { activityId },
    data
  })
}

// 删除投票选项
export function deleteVoteOption(optionId) {
  return request({
    url: '/admin/vote/deleteOption',
    method: 'post',
    params: { optionId }
  })
} 