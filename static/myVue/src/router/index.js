import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
    { path: '/', redirect: '/pollution' },
    { path: '/me', component: () => import('@/views/UserCenter.vue') },
    { path: '/files', component: () => import('@/views/FilesView.vue') },
    { path: '/pollution', component: () => import('@/views/PollutionView.vue') },
    { path: '/admin/users', component: () => import('@/views/AdminUsers.vue') },
    { path: '/pollution/async', component: () => import('@/views/AsyncDownloadView.vue') },
  ],
})

router.beforeEach((to, _from, next) => {
  if (to.path === '/login') return next()
  try {
    const auth = useAuthStore()
    if (!auth.token) return next('/login')
  } catch {}
  next()
})

export default router
