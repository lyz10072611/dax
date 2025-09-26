import request from '@/utils/request'

export const login = (username, password) => request.post('/user/login', null, { params: { username, password } })
export const register = (username, password) => request.post('/user/register', null, { params: { username, password } })
export const userInfo = () => request.get('/user/userInfo')
export const updateMe = (payload) => request.put('/user/update', payload)
export const updatePwd = (payload) => request.patch('/user/updatePwd', payload)


