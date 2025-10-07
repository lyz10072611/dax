<template>
  <div class="app-layout">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="navbar-brand">
        <h1>水泥厂管理系统</h1>
      </div>
      <nav class="navbar-nav">
        <router-link to="/cement-plant" class="nav-link">水泥厂管理</router-link>
        <router-link to="/map" class="nav-link">地图查看</router-link>
        <router-link to="/tif-management" class="nav-link">TIF管理</router-link>
        <router-link to="/pollution" class="nav-link">污染数据</router-link>
        <router-link to="/files" class="nav-link">文件管理</router-link>
        <router-link to="/me" class="nav-link">个人中心</router-link>
        <router-link to="/admin/users" class="nav-link" v-if="isAdmin">用户管理</router-link>
      </nav>
      <div class="navbar-user">
        <span class="user-info">{{ userInfo.username }}</span>
        <button @click="logout" class="logout-btn">退出</button>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 底部信息 -->
    <footer class="footer">
      <p>&copy; 2025 水泥厂管理系统 - 基于Spring Boot + Vue.js</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userInfo as getUserInfo } from '@/api/user'

const router = useRouter()
const auth = useAuthStore()
const userInfo = ref({ username: '加载中...' })
const isAdmin = ref(false)

onMounted(async () => {
  // 只有在有token的情况下才获取用户信息
  if (auth.token) {
    try {
      const { data } = await getUserInfo()
      if (data.code === 200) {
        userInfo.value = data.data
        isAdmin.value = data.data.roleCode === 0
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果获取用户信息失败，清除token并重定向到登录页
      auth.logout()
      router.push('/login')
    }
  } else {
    // 没有token，重定向到登录页
    router.push('/login')
  }
})

const logout = () => {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.navbar-brand h1 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
}

.navbar-nav {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 5px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.nav-link.router-link-active {
  background-color: rgba(255, 255, 255, 0.3);
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  font-size: 0.9rem;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.5rem 1rem;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.main-content {
  flex: 1;
  padding: 2rem;
  background-color: #f5f5f5;
}

.footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 1rem;
  font-size: 0.9rem;
}
</style>
