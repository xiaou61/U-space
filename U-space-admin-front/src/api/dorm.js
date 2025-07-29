import request from '../utils/request'

// ===== 宿舍楼 =====
export const listBuildings = () => request.get('/admin/dorm/listBuilding')
export const addBuilding    = (data) => request.post('/admin/dorm/addBuilding', data)
export const removeBuilding = (id)   => request.post('/admin/dorm/removeBuilding', null, { params: { id } })

// ===== 宿舍房间 =====
export const listRooms   = (buildingId) => request.get('/admin/dorm/listRoom',   { params: { buildingId } })
export const addRoom     = (data)       => request.post('/admin/dorm/addRoom',   data)
export const removeRoom  = (id)         => request.post('/admin/dorm/removeRoom', null, { params: { id } })

// ===== 宿舍床位 =====
export const listBeds  = (roomId) => request.get('/admin/dorm/listBed', { params: { roomId } })
export const addBed    = (data)   => request.post('/admin/dorm/addBed',   data)
export const removeBed = (id)     => request.post('/admin/dorm/removeBed', null, { params: { id } }) 