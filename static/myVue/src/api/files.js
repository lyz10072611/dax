import request from '@/utils/request'

export const listFiles = () => request.get('/files')
export const fileInfo = (name) => request.get(`/files/${name}`)
export const uploadFile = (file) => {
  const fd = new FormData(); fd.append('file', file)
  return request.post('/files', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const replaceFile = (name, file) => {
  const fd = new FormData(); fd.append('file', file)
  return request.put(`/files/${name}`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const deleteFile = (name) => request.delete(`/files/${name}`)


