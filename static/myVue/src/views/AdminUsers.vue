<template>
  <div class="box">
    <h2>用户管理(管理员)</h2>
    <div class="filters">
      <input v-model="username" placeholder="用户名"/>
      <button @click="load(1)">查询</button>
      <button @click="onAdd">新增</button>
    </div>
    <table class="tbl">
      <thead><tr><th>ID</th><th>用户名</th><th>昵称</th><th>邮箱</th><th>roleId</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="u in items" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.username }}</td>
          <td>{{ u.nickname }}</td>
          <td>{{ u.email }}</td>
          <td>{{ u.roleId }}</td>
          <td>
            <button @click="onEdit(u)">编辑</button>
            <button @click="onDel(u.id)">删除</button>
            <button @click="onQuota(u.id)">配额</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="pager">
      <button :disabled="pageNum<=1" @click="load(pageNum-1)">上一页</button>
      <span>{{ pageNum }}</span>
      <button :disabled="items.length<pageSize" @click="load(pageNum+1)">下一页</button>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { pageUsers, addUser, updateUser, deleteUser, getQuota, setQuota } from '@/api/admin'

const pageNum = ref(1)
const pageSize = ref(10)
const username = ref('')
const items = ref([])

const load = async (p=1) => {
  pageNum.value = p
  const { data } = await pageUsers({ pageNum: pageNum.value, pageSize: pageSize.value, username: username.value })
  if (data.code === 0) items.value = data.data.items || data.data
}
onMounted(() => load(1))

const onAdd = async () => {
  const u = { username: prompt('用户名?') || '', password: prompt('密码?') || '123456', roleId: Number(prompt('roleId? 1=admin,2=user,3=guest', '2') || 2) }
  const { data } = await addUser(u)
  alert(data.code === 0 ? '已新增' : data.message); load(pageNum.value)
}
const onEdit = async (u) => {
  const payload = { ...u, nickname: prompt('昵称?', u.nickname||'') || '', email: prompt('邮箱?', u.email||'') || '', roleId: Number(prompt('roleId?', String(u.roleId||2)) || u.roleId) }
  const { data } = await updateUser(payload)
  alert(data.code === 0 ? '已更新' : data.message); load(pageNum.value)
}
const onDel = async (id) => { if (!confirm('确认删除?')) return; const { data } = await deleteUser(id); alert(data.code===0?'已删除':data.message); load(pageNum.value) }
const onQuota = async (id) => {
  const q = await getQuota(id); const cur = q.data?.data || {}
  const value = Number(prompt('配额值?', cur.value ?? 500))
  const ttlHours = Number(prompt('过期时间(小时)?', Math.ceil((cur.ttlSeconds||86400)/3600)))
  const { data } = await setQuota(id, value, ttlHours)
  alert(data.code===0?'已设置':data.message)
}
</script>
<style scoped>
.box{max-width:1000px;margin:24px auto}
.tbl{width:100%;border-collapse:collapse;margin-top:12px}
th,td{border:1px solid #eee;padding:8px;text-align:left}
.pager{margin-top:12px}
input{padding:6px}
button{margin-left:6px}
</style>


