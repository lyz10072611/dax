<template>
  <div class="login-box">
    <h2>登录</h2>
    <form @submit.prevent="onSubmit">
      <label>用户名</label>
      <input v-model="username" placeholder="请输入用户名" />
      <label>密码</label>
      <input v-model="password" type="password" placeholder="请输入密码" />
      <button type="submit">登录</button>
      <button type="button" @click="onRegister">注册</button>
    </form>
    <p class="tip">登录成功后会自动携带 Token 访问后端</p>
  </div>
  
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { login, register } from '@/api/user'

const username = ref('guest01')
const password = ref('123456')
const router = useRouter()
const auth = useAuthStore()

const onSubmit = async () => {
  const { data } = await login(username.value, password.value)
  if (data.code === 0) { auth.setToken(data.data); router.push('/') }
  else alert(data.message || '登录失败')
}
const onRegister = async () => {
  const { data } = await register(username.value, password.value)
  if (data.code === 0) alert('注册成功')
  else alert(data.message || '注册失败')
}
</script>
<style scoped>
.login-box{max-width:420px;margin:10vh auto;padding:24px;border:1px solid #eee;border-radius:8px}
label{display:block;margin-top:12px}
input{width:100%;padding:8px;margin-top:6px}
button{margin-top:16px;margin-right:8px}
.tip{color:#888;margin-top:12px}
</style>


