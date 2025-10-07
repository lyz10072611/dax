import request from '@/utils/request'

// 水泥厂管理API
export const getCementPlants = () => {
  return request.get('/cement-plant/list')
}

export const getCementPlantById = (plantId) => {
  return request.get(`/cement-plant/${plantId}`)
}

export const addCementPlant = (plantData) => {
  return request.post('/cement-plant', plantData)
}

export const updateCementPlant = (plantData) => {
  return request.put('/cement-plant', plantData)
}

export const deleteCementPlant = (plantId) => {
  return request.delete(`/cement-plant/${plantId}`)
}

export const searchCementPlants = (params) => {
  return request.get('/cement-plant/page', { params })
}

export const getCementPlantsByLocation = (minLng, maxLng, minLat, maxLat) => {
  return request.get('/cement-plant/location-range', {
    params: { minLng, maxLng, minLat, maxLat }
  })
}

// 水泥厂识别记录API
export const getIdentifications = () => {
  return request.get('/cement-plant/identification/list')
}

export const getIdentificationById = (identificationId) => {
  return request.get(`/cement-plant/identification/${identificationId}`)
}

export const addIdentification = (identificationData) => {
  return request.post('/cement-plant/identification', identificationData)
}

export const updateIdentification = (identificationData) => {
  return request.put('/cement-plant/identification', identificationData)
}

export const deleteIdentification = (identificationId) => {
  return request.delete(`/cement-plant/identification/${identificationId}`)
}

export const searchIdentifications = (params) => {
  return request.get('/cement-plant/identification/page', { params })
}

// 统计API
export const getActivePlantsCount = () => {
  return request.get('/cement-plant/stats/active-count')
}

export const getIdentificationCountByPlantId = (plantId) => {
  return request.get(`/cement-plant/stats/identification-count/${plantId}`)
}

export const getIdentificationCountByTimeRange = (startTime, endTime) => {
  return request.get('/cement-plant/stats/identification-count-by-time', {
    params: { startTime, endTime }
  })
}
