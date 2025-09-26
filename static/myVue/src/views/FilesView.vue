<template>
  <div class="box">
    <h2>文件管理</h2>
    <div class="ops">
      <input type="file" @change="onPick">
      <button @click="onUpload">上传(管理员)</button>
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
            <button @click="onDelete(f.name)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { listFiles, uploadFile, replaceFile, deleteFile } from '@/api/files'

const files = ref([])
const picked = ref(null)

const load = async () => {
  const { data } = await listFiles()
  if (data.code === 0) files.value = data.data
}
onMounted(load)

const onPick = (e) => { picked.value = e.target.files?.[0] || null }
const onUpload = async () => {
  if (!picked.value) return alert('请选择文件')
  const { data } = await uploadFile(picked.value)
  alert(data.code === 0 ? '上传成功' : data.message)
  await load()
}
const onReplace = async (name, e) => {
  const file = e.target.files?.[0]
  if (!file) return
  const { data } = await replaceFile(name, file)
  alert(data.code === 0 ? '已替换' : data.message)
  await load()
}
const onDelete = async (name) => {
  const { data } = await deleteFile(name)
  alert(data.code === 0 ? '已删除' : data.message)
  await load()
}
</script>
<style scoped>
.box{max-width:900px;margin:24px auto}
.ops{margin-bottom:12px}
.tbl{width:100%;border-collapse:collapse}
th,td{border:1px solid #eee;padding:8px;text-align:left}
</style>


