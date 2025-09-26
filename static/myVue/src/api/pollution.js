import request from '@/utils/request'

export const pagePollution = (params) => request.get('/pollution', { params })
export const getById = (id) => request.get(`/pollution/${id}`)

export const download = async (ids) => {
  const url = new URL('/pollution/download', import.meta.env.VITE_API_BASE || 'http://localhost:8083')
  ids.forEach(id => url.searchParams.append('ids', String(id)))
  const resp = await fetch(url.toString(), { headers: { Authorization: localStorage.getItem('token') || '' }})
  if (resp.status === 429) throw new Error('每日下载额度不足')
  if (!resp.ok) throw new Error('下载失败')
  return await resp.blob()
}

export const enqueue = (ids) => request.post('/pollution/async/enqueue', ids)
export const taskStatus = (taskId) => request.get(`/pollution/async/status/${taskId}`)
export const taskResult = async (taskId) => {
  const url = `${import.meta.env.VITE_API_BASE || 'http://localhost:8083'}/pollution/async/result/${taskId}`
  const resp = await fetch(url, { headers: { Authorization: localStorage.getItem('token') || '' }})
  if (!resp.ok) throw new Error('结果获取失败')
  return await resp.blob()
}


