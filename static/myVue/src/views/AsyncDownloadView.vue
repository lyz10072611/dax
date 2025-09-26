<template>
  <div class="box">
    <h2>异步下载</h2>
    <p>输入要下载的ID（用逗号分隔），或从上一页选择后跳转</p>
    <input v-model="idText" placeholder="如 1,2,3"/>
    <button @click="onEnqueue">提交任务</button>
    <div v-if="taskId">
      <p>任务ID：{{ taskId }}</p>
      <p>状态：{{ status }}</p>
      <button :disabled="status!=='done'" @click="onDownload">下载结果</button>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { enqueue, taskStatus, taskResult } from '@/api/pollution'

const idText = ref('')
const taskId = ref('')
const status = ref('')
let timer = null

const poll = async () => {
  if (!taskId.value) return
  const { data } = await taskStatus(taskId.value)
  if (data.code === 0) {
    status.value = data.data.status
    if (status.value === 'done' || status.value === 'error') clearInterval(timer)
  }
}

const onEnqueue = async () => {
  const ids = idText.value.split(',').map(s => Number(s.trim())).filter(n => !isNaN(n))
  if (!ids.length) return alert('请输入有效ID')
  const { data } = await enqueue(ids)
  if (data.code === 0) {
    taskId.value = data.data
    status.value = 'queued'
    timer = setInterval(poll, 1500)
  } else alert(data.message)
}

const blobSave = (blob, filename) => { const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = filename; a.click(); URL.revokeObjectURL(a.href) }
const onDownload = async () => {
  const blob = await taskResult(taskId.value)
  blobSave(blob, 'async-pollution-data.zip')
}
</script>
<style scoped>
.box{max-width:720px;margin:24px auto}
input{width:100%;padding:8px}
button{margin-top:8px}
</style>


