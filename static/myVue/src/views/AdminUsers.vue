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
import { handleResponse, STATUS_CODES } from '@/utils/statusCode'

const pageNum = ref(1)
const pageSize = ref(10)
const username = ref('')
const items = ref([])

const load = async (p=1) => {
  pageNum.value = p
  try {
    const { data } = await pageUsers({ pageNum: pageNum.value, pageSize: pageSize.value, username: username.value })
    const result = handleResponse(data, {
      showError: true,
      onSuccess: (responseData) => {
        items.value = responseData.items || responseData
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.FORBIDDEN) {
          alert('权限不足，只有管理员可以访问')
        } else if (code === STATUS_CODES.UNAUTHORIZED) {
          alert('请先登录')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('加载用户列表失败:', error)
    alert('加载用户列表失败，请稍后重试')
  }
}

onMounted(() => load(1))

const onAdd = async () => {
  const u = { 
    username: prompt('用户名?') || '', 
    password: prompt('密码?') || '123456', 
    roleId: Number(prompt('roleId? 1=admin,2=user,3=guest', '2') || 2) 
  }
  
  if (!u.username) return
  
  try {
    const { data } = await addUser(u)
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onSuccess: () => {
        load(pageNum.value)
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.FORBIDDEN) {
          alert('权限不足，只有管理员可以添加用户')
        } else if (code === STATUS_CODES.CONFLICT) {
          alert('用户名已存在')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('添加用户失败:', error)
    alert('添加用户失败，请稍后重试')
  }
}

const onEdit = async (u) => {
  const payload = { 
    ...u, 
    nickname: prompt('昵称?', u.nickname||'') || '', 
    email: prompt('邮箱?', u.email||'') || '', 
    roleId: Number(prompt('roleId?', String(u.roleId||2)) || u.roleId) 
  }
  
  try {
    const { data } = await updateUser(payload)
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onSuccess: () => {
        load(pageNum.value)
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.FORBIDDEN) {
          alert('权限不足，只有管理员可以编辑用户')
        } else if (code === STATUS_CODES.NOT_FOUND) {
          alert('用户不存在')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('更新用户失败:', error)
    alert('更新用户失败，请稍后重试')
  }
}

const onDel = async (id) => { 
  if (!confirm('确认删除?')) return
  
  try {
    const { data } = await deleteUser(id)
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onSuccess: () => {
        load(pageNum.value)
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.FORBIDDEN) {
          alert('权限不足，只有管理员可以删除用户')
        } else if (code === STATUS_CODES.NOT_FOUND) {
          alert('用户不存在')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('删除用户失败:', error)
    alert('删除用户失败，请稍后重试')
  }
}

const onQuota = async (id) => {
  try {
    const q = await getQuota(id)
    const result = handleResponse(q.data, {
      showError: true,
      onSuccess: (responseData) => {
        const cur = responseData || {}
        const value = Number(prompt('配额值?', cur.value ?? 500))
        const ttlHours = Number(prompt('过期时间(小时)?', Math.ceil((cur.ttlSeconds||86400)/3600)))
        
        if (isNaN(value) || isNaN(ttlHours)) return
        
        setQuota(id, value, ttlHours).then(async (response) => {
          const result = handleResponse(response.data, {
            showSuccess: true,
            showError: true,
            onError: (code, message) => {
              if (code === STATUS_CODES.FORBIDDEN) {
                alert('权限不足，只有管理员可以设置配额')
              } else if (code === STATUS_CODES.BAD_REQUEST) {
                alert('参数不合法')
              } else {
                alert(message)
              }
            }
          })
        })
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.FORBIDDEN) {
          alert('权限不足，只有管理员可以查看配额')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('获取配额失败:', error)
    alert('获取配额失败，请稍后重试')
  }
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


