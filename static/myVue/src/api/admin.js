import request from '@/utils/request'

export const pageUsers = (params) => request.get('/admin/users', { params })
export const addUser = (payload) => request.post('/admin/users', payload)
export const updateUser = (payload) => request.put('/admin/users', payload)
export const deleteUser = (id) => request.delete(`/admin/users/${id}`)
export const getQuota = (id) => request.get(`/admin/users/${id}/quota`)
export const setQuota = (id, value, ttlHours) => request.patch(`/admin/users/${id}/quota`, { value, ttlHours })


