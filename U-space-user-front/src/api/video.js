import request from '../utils/request'

// 获取必看视频列表（包含观看状态）
export function getVideoList() {
  return request({
    url: '/video-watch/list',
    method: 'post'
  })
}

// 记录视频观看状态
export function recordVideoWatch(data) {
  return request({
    url: '/video-watch/record',
    method: 'post',
    data
  })
} 