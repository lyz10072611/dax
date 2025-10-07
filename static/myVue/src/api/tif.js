import request from '@/utils/request'

// TIF文件管理API
export const uploadTifFile = (file, plantId, dataType) => {
  const formData = new FormData()
  formData.append('file', file)
  if (plantId) formData.append('plantId', plantId)
  if (dataType) formData.append('dataType', dataType)
  
  return request.post('/geo/upload-tif', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const getTifFiles = (fileType, status) => {
  return request.get('/geo/files', {
    params: { fileType, status }
  })
}

export const getTifInfo = (fileId) => {
  return request.get(`/geo/tif-info/${fileId}`)
}

export const processTifFile = (fileId) => {
  return request.post(`/geo/process-tif/${fileId}`)
}

export const getTileUrl = (fileId, z, x, y) => {
  return `${request.defaults.baseURL}/geo/tile/${fileId}/${z}/${x}/${y}`
}

export const downloadTifFile = (fileId) => {
  return request.get(`/data-download/tif/${fileId}`, {
    responseType: 'blob'
  })
}

export const batchDownloadTifFiles = (fileIds) => {
  return request.post('/data-download/batch-download', fileIds, {
    responseType: 'blob'
  })
}

export const getDownloadStatus = () => {
  return request.get('/data-download/download-status')
}

export const getDownloadHistory = (pageNum = 1, pageSize = 10) => {
  return request.get('/data-download/download-history', {
    params: { pageNum, pageSize }
  })
}

export const submitAsyncDownload = (fileIds) => {
  return request.post('/data-download/async-download', fileIds)
}

export const getAsyncDownloadStatus = (taskId) => {
  return request.get(`/data-download/async-download/${taskId}/status`)
}

export const downloadAsyncResult = (taskId) => {
  return request.get(`/data-download/async-download/${taskId}/result`, {
    responseType: 'blob'
  })
}
