<template>
  <div class="box">
    <h2>污染物数据</h2>
    <div class="filters">
      <label>dataFormat <input v-model.number="q.dataFormat" type="number" min="1" max="5"/></label>
      <label>pollutantType <input v-model="q.pollutantType" placeholder="PM25/PM10/O3/NO2"/></label>
      <label>year <input v-model.number="q.year" type="number"/></label>
      <label>month <input v-model.number="q.month" type="number"/></label>
      <label>day <input v-model.number="q.day" type="number"/></label>
      <label>hour <input v-model.number="q.hour" type="number"/></label>
      <button @click="load(1)">查询</button>
    </div>
    <div class="ops">
      <button @click="onDownload">同步下载(选中)</button>
      <button @click="onAsync">异步下载(批量)</button>
    </div>
    <table class="tbl">
      <thead><tr><th><input type="checkbox" @change="onToggleAll"></th><th>ID</th><th>类型</th><th>格式</th><th>生产时间</th></tr></thead>
      <tbody>
        <tr v-for="it in items" :key="it.id">
          <td><input type="checkbox" :value="it.id" v-model="selected"></td>
          <td>{{ it.id }}</td>
          <td>{{ it.pollutantType }}</td>
          <td>{{ it.dataFormat }}</td>
          <td>{{ it.produceTime }}</td>
        </tr>
      </tbody>
    </table>
    <div class="pager">
      <button :disabled="pageNum<=1" @click="load(pageNum-1)">上一页</button>
      <span>{{ pageNum }} / {{ pages }}</span>
      <button :disabled="pageNum>=pages" @click="load(pageNum+1)">下一页</button>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { pagePollution, download } from '@/api/pollution'

const q = ref({ dataFormat: undefined, pollutantType: '', year: undefined, month: undefined, day: undefined, hour: undefined })
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(1)
const items = ref([])
const selected = ref([])

const load = async (p = 1) => {
  pageNum.value = p
  const { data } = await pagePollution({ pageNum: pageNum.value, pageSize: pageSize.value, ...q.value })
  if (data.code === 0) {
    total.value = data.data.total
    items.value = data.data.items
    pages.value = Math.max(1, Math.ceil(total.value / pageSize.value))
  }
}
onMounted(() => load(1))

const onToggleAll = (e) => {
  if (e.target.checked) selected.value = items.value.map(i => i.id)
  else selected.value = []
}

const blobSave = (blob, filename) => { const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = filename; a.click(); URL.revokeObjectURL(a.href) }
const onDownload = async () => {
  if (!selected.value.length) return alert('请选择数据')
  try {
    const blob = await download(selected.value)
    blobSave(blob, selected.value.length === 1 ? 'data.bin' : 'pollution-data.zip')
  } catch (e) { alert(e.message || '下载失败') }
}
const onAsync = async () => { window.location.href = '/pollution/async' }
</script>
<style scoped>
.box{max-width:1000px;margin:24px auto}
.filters label{margin-right:8px}
.tbl{width:100%;border-collapse:collapse;margin-top:12px}
th,td{border:1px solid #eee;padding:8px;text-align:left}
.pager{margin-top:12px}
</style>


