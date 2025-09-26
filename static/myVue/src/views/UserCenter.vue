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
      <button @click="onSave">保存</button>
    </div>
    <h3>修改密码</h3>
    <input v-model="oldPwd" placeholder="旧密码"/>
    <input v-model="newPwd" placeholder="新密码"/>
    <input v-model="rePwd" placeholder="确认新密码"/>
    <button @click="onPwd">修改密码</button>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { userInfo, updateMe, updatePwd } from '@/api/user'

const user = ref(null)
const oldPwd = ref('')
const newPwd = ref('')
const rePwd = ref('')

onMounted(async () => {
  const { data } = await userInfo()
  if (data.code === 0) user.value = data.data
})

const onSave = async () => {
  const { data } = await updateMe(user.value)
  alert(data.code === 0 ? '已保存' : data.message)
}
const onPwd = async () => {
  const { data } = await updatePwd({ oldPwd: oldPwd.value, newPwd: newPwd.value, rePwd: rePwd.value })
  alert(data.code === 0 ? '已修改' : data.message)
}
</script>
<style scoped>
.box{max-width:720px;margin:24px auto}
input{margin:6px 0;padding:6px}
button{margin-top:8px}
</style>


