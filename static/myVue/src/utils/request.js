import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
axios.defaults.withCredentials = true;
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8083',
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
  resp => resp,
  err => {
    if (err?.response?.status === 401) {
      try { useAuthStore().logout() } catch {}
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default request


