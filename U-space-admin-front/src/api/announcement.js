import request from '../utils/request'

export const addAnnouncement = (data) => request.post('/bulletin/add', data)

export const listAnnouncements = () => request.post('/bulletin/list')

export const deleteAnnouncements = (ids) => request.post('/bulletin/delete', ids) 