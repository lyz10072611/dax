import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { STATUS_CODES, handleStatusCode } from './statusCode'

axios.defaults.withCredentials = true;
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8085',
  timeout: 20000
})

request.interceptors.request.use(cfg => {
  try {
    const auth = useAuthStore()
    if (auth.token) cfg.headers.Authorization = auth.token
  } catch {}
  return cfg
})

request.interceptors.response.use(
  resp => {
    // 检查响应数据中的状态码
    if (resp.data && typeof resp.data === 'object' && 'code' in resp.data) {
      const { code } = resp.data
      
      // 处理特定的状态码
      if (code === STATUS_CODES.UNAUTHORIZED) {
        try { 
          useAuthStore().logout()
          // 使用Vue Router重定向到登录页
          window.location.href = '/myVue/index.html#/login'
        } catch {}
      }
    }
    
    return resp
  },
  err => {
    console.error('请求错误:', err)
    
    // 处理HTTP状态码错误
    if (err?.response?.status === 401) {
      try { 
        useAuthStore().logout()
        // 使用Vue Router重定向到登录页
        window.location.href = '/myVue/index.html#/login'
      } catch {}
    }
    
    return Promise.reject(err)
  }
)

export default request


