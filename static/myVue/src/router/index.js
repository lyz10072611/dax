import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
    { 
      path: '/', 
      component: () => import('@/components/AppLayout.vue'),
      children: [
        { path: '', redirect: '/cement-plant' },
        { path: 'cement-plant', component: () => import('@/views/CementPlantView.vue') },
        { path: 'map', component: () => import('@/views/MapView.vue') },
        { path: 'tif-management', component: () => import('@/views/TifManagementView.vue') },
        { path: 'pollution', component: () => import('@/views/PollutionView.vue') },
        { path: 'me', component: () => import('@/views/UserCenter.vue') },
        { path: 'files', component: () => import('@/views/FilesView.vue') },
        { path: 'admin/users', component: () => import('@/views/AdminUsers.vue') },
        { path: 'pollution/async', component: () => import('@/views/AsyncDownloadView.vue') },
      ]
    },
  ],
})

router.beforeEach((to, _from, next) => {
  // 登录页面直接放行
  if (to.path === '/login') return next()
  
  try {
    const auth = useAuthStore()
    if (!auth.token) {
      // 没有token，重定向到登录页
      next('/login')
      return
    }
  } catch (error) {
    console.error('路由守卫错误:', error)
    next('/login')
    return
  }
  
  next()
})

export default router
