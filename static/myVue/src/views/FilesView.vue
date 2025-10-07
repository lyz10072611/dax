<template>
  <div class="box">
    <h2>文件管理</h2>
    <div class="ops">
      <input type="file" @change="onPick">
      <button @click="onUpload" :disabled="loading">上传(管理员)</button>
    </div>
    <table class="tbl">
      <thead><tr><th>文件名</th><th>大小</th><th>修改时间</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="f in files" :key="f.name">
          <td>{{ f.name }}</td>
          <td>{{ f.size }}</td>
          <td>{{ new Date(f.lastModified).toLocaleString() }}</td>
          <td>
            <input type="file" @change="e=>onReplace(f.name,e)">
            <button @click="onDelete(f.name)" :disabled="loading">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { listFiles, uploadFile, replaceFile, deleteFile } from '@/api/files'
import { handleResponse, STATUS_CODES } from '@/utils/statusCode'

const files = ref([])
const picked = ref(null)
const loading = ref(false)

const load = async () => {
  try {
    const { data } = await listFiles()
    const result = handleResponse(data, {
      showError: true,
      onSuccess: (responseData) => {
        files.value = responseData
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.UNAUTHORIZED) {
          alert('请先登录')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('加载文件列表失败:', error)
    alert('加载文件列表失败，请稍后重试')
  }
}

onMounted(load)

const onPick = (e) => { picked.value = e.target.files?.[0] || null }

const onUpload = async () => {
  if (!picked.value) return alert('请选择文件')
  if (loading.value) return
  
  loading.value = true
  try {
    const { data } = await uploadFile(picked.value)
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onSuccess: async () => {
        await load()
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.FORBIDDEN) {
          alert('权限不足，只有管理员可以上传文件')
        } else if (code === STATUS_CODES.UNAUTHORIZED) {
          alert('请先登录')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('上传文件失败:', error)
    alert('上传文件失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const onReplace = async (name, e) => {
  const file = e.target.files?.[0]
  if (!file) return
  if (loading.value) return
  
  loading.value = true
  try {
    const { data } = await replaceFile(name, file)
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onSuccess: async () => {
        await load()
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.FORBIDDEN) {
          alert('权限不足，只有管理员可以替换文件')
        } else if (code === STATUS_CODES.NOT_FOUND) {
          alert('文件不存在')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('替换文件失败:', error)
    alert('替换文件失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const onDelete = async (name) => {
  if (loading.value) return
  
  loading.value = true
  try {
    const { data } = await deleteFile(name)
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onSuccess: async () => {
        await load()
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.FORBIDDEN) {
          alert('权限不足，只有管理员可以删除文件')
        } else if (code === STATUS_CODES.NOT_FOUND) {
          alert('文件不存在')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('删除文件失败:', error)
    alert('删除文件失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.box{max-width:900px;margin:24px auto}
.ops{margin-bottom:12px}
.tbl{width:100%;border-collapse:collapse}
th,td{border:1px solid #eee;padding:8px;text-align:left}
</style>


