<template>
  <div class="login-box">
    <h2>登录</h2>
    <form @submit.prevent="onSubmit">
      <label>用户名</label>
      <input v-model="username" placeholder="请输入用户名" />
      <label>密码</label>
      <input v-model="password" type="password" placeholder="请输入密码" />
      <button type="submit" :disabled="loading">登录</button>
      <button type="button" @click="onRegister" :disabled="loading">注册</button>
    </form>
    <p class="tip">登录成功后会自动携带 Token 访问后端</p>
  </div>
  
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { login, register } from '@/api/user'
import { handleResponse, STATUS_CODES } from '@/utils/statusCode'

const username = ref('guest01')
const password = ref('123456')
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()

const onSubmit = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    const { data } = await login(username.value, password.value)
    const result = handleResponse(data, {
      showSuccess: false,
      showError: true,
      onSuccess: (responseData) => {
        auth.setToken(responseData.token)
        // 重定向到水泥厂管理页面
        router.push('/cement-plant')
      },
      onError: (code, message) => {
        // 特殊处理某些状态码
        if (code === STATUS_CODES.TOO_MANY_REQUESTS) {
          alert('登录失败次数过多，请15分钟后重试')
        } else if (code === STATUS_CODES.NOT_FOUND) {
          alert('用户名不存在')
        } else if (code === STATUS_CODES.UNAUTHORIZED) {
          alert('密码错误')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('Login error:', error)
    alert('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const onRegister = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    const { data } = await register(username.value, password.value)
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onError: (code, message) => {
        if (code === STATUS_CODES.CONFLICT) {
          alert('用户名或邮箱已被占用')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('Register error:', error)
    alert('注册失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.login-box{max-width:420px;margin:10vh auto;padding:24px;border:1px solid #eee;border-radius:8px}
label{display:block;margin-top:12px}
input{width:100%;padding:8px;margin-top:6px}
button{margin-top:16px;margin-right:8px}
.tip{color:#888;margin-top:12px}
</style>


