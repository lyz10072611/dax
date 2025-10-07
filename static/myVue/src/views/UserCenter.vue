<template>
  <div class="box">
    <h2>个人中心</h2>
    <div v-if="user">
      <div>ID: {{ user.id }}</div>
      <div>用户名: {{ user.username }}</div>
      <div>
        昵称: <input v-model="user.nickname"/>
      </div>
      <div>
        邮箱: <input v-model="user.email"/>
      </div>
      <div>
        头像URL: <input v-model="user.userPic"/>
      </div>
      <button @click="onSave" :disabled="loading">保存</button>
    </div>
    <h3>修改密码</h3>
    <input v-model="oldPwd" placeholder="旧密码"/>
    <input v-model="newPwd" placeholder="新密码"/>
    <input v-model="rePwd" placeholder="确认新密码"/>
    <button @click="onPwd" :disabled="loading">修改密码</button>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { userInfo, updateMe, updatePwd } from '@/api/user'
import { handleResponse, STATUS_CODES } from '@/utils/statusCode'

const user = ref(null)
const oldPwd = ref('')
const newPwd = ref('')
const rePwd = ref('')
const loading = ref(false)

onMounted(async () => {
  try {
    const { data } = await userInfo()
    const result = handleResponse(data, {
      showError: true,
      onSuccess: (responseData) => {
        user.value = responseData
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.UNAUTHORIZED) {
          alert('请先登录')
        } else if (code === STATUS_CODES.NOT_FOUND) {
          alert('用户不存在')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('获取用户信息失败:', error)
    alert('获取用户信息失败，请稍后重试')
  }
})

const onSave = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    const { data } = await updateMe(user.value)
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onError: (code, message) => {
        if (code === STATUS_CODES.UNAUTHORIZED) {
          alert('请先登录')
        } else if (code === STATUS_CODES.BAD_REQUEST) {
          alert('输入信息有误，请检查')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const onPwd = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    const { data } = await updatePwd({ 
      oldPwd: oldPwd.value, 
      newPwd: newPwd.value, 
      rePwd: rePwd.value 
    })
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onSuccess: () => {
        // 清空密码输入框
        oldPwd.value = ''
        newPwd.value = ''
        rePwd.value = ''
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.UNAUTHORIZED) {
          alert('请先登录')
        } else if (code === STATUS_CODES.BAD_REQUEST) {
          alert('密码输入有误，请检查')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('修改密码失败:', error)
    alert('修改密码失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.box{max-width:720px;margin:24px auto}
input{margin:6px 0;padding:6px}
button{margin-top:8px}
</style>


