<template>
  <div class="cement-plant-page">
    <!-- 查询条件 -->
    <div class="search-panel">
      <div class="search-form">
        <div class="form-group">
          <label>水泥厂名称</label>
          <input v-model="searchForm.name" placeholder="请输入水泥厂名称" />
        </div>
        <div class="form-group">
          <label>省份</label>
          <input v-model="searchForm.province" placeholder="请输入省份" />
        </div>
        <div class="form-group">
          <label>城市</label>
          <input v-model="searchForm.city" placeholder="请输入城市" />
        </div>
        <button @click="loadData(1)" class="btn btn-primary">查询</button>
        <button @click="resetSearch" class="btn btn-secondary">重置</button>
      </div>
      <div class="header-actions">
        <button @click="refreshData" class="btn btn-primary">刷新数据</button>
        <button @click="showUploadDialog = true" class="btn btn-success">上传数据</button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="data-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>水泥厂名称</th>
            <th>省份</th>
            <th>城市</th>
            <th>经度</th>
            <th>纬度</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in dataList" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.province }}</td>
            <td>{{ item.city }}</td>
            <td>{{ item.longitude }}</td>
            <td>{{ item.latitude }}</td>
            <td>
              <span :class="['status', item.status === 1 ? 'active' : 'inactive']">
                {{ item.status === 1 ? '活跃' : '非活跃' }}
              </span>
            </td>
            <td>
              <button @click="viewDetails(item)" class="btn btn-sm btn-info">查看</button>
              <button @click="editItem(item)" class="btn btn-sm btn-warning">编辑</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <button :disabled="currentPage <= 1" @click="loadData(currentPage - 1)" class="btn btn-secondary">上一页</button>
      <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
      <button :disabled="currentPage >= totalPages" @click="loadData(currentPage + 1)" class="btn btn-secondary">下一页</button>
    </div>

    <!-- 上传对话框 -->
    <div v-if="showUploadDialog" class="modal-overlay" @click="showUploadDialog = false">
      <div class="modal-content" @click.stop>
        <h3>上传水泥厂数据</h3>
        <div class="upload-form">
          <input type="file" @change="handleFileSelect" accept=".csv,.xlsx,.json" />
          <div class="upload-actions">
            <button @click="uploadFile" :disabled="!selectedFile" class="btn btn-primary">上传</button>
            <button @click="showUploadDialog = false" class="btn btn-secondary">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { handleResponse, STATUS_CODES } from '@/utils/statusCode'

const dataList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const showUploadDialog = ref(false)
const selectedFile = ref(null)

const searchForm = ref({
  name: '',
  province: '',
  city: ''
})

const loadData = async (page = 1) => {
  try {
    currentPage.value = page
    const params = {
      pageNum: page,
      pageSize: pageSize.value,
      ...searchForm.value
    }
    
    const { data } = await request.get('/cement-plant/page', { params })
    const result = handleResponse(data, {
      showError: true,
      onSuccess: (responseData) => {
        dataList.value = responseData.items || []
        totalPages.value = Math.ceil((responseData.total || 0) / pageSize.value)
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.UNAUTHORIZED) {
          alert('请先登录')
        } else if (code === STATUS_CODES.NOT_FOUND) {
          alert('没有找到相关数据')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('查询失败:', error)
    alert('网络错误，请稍后重试')
  }
}

const refreshData = () => {
  loadData(currentPage.value)
}

const resetSearch = () => {
  searchForm.value = { name: '', province: '', city: '' }
  loadData(1)
}

const viewDetails = (item) => {
  alert(`水泥厂详情：\n名称：${item.name}\n位置：${item.province} ${item.city}\n坐标：${item.longitude}, ${item.latitude}`)
}

const editItem = (item) => {
  alert('编辑功能开发中...')
}

const handleFileSelect = (event) => {
  selectedFile.value = event.target.files[0]
}

const uploadFile = async () => {
  if (!selectedFile.value) return
  
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    
    const { data } = await request.post('/files', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onSuccess: () => {
        showUploadDialog.value = false
        selectedFile.value = null
        loadData(1)
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
    console.error('上传失败:', error)
    alert('上传失败，请稍后重试')
  }
}

onMounted(() => {
  loadData(1)
})
</script>

<style scoped>
.cement-plant-page {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.search-panel {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #eee;
  background-color: #f8f9fa;
  display: flex;
  justify-content: space-between;
  align-items: end;
  flex-wrap: wrap;
  gap: 1rem;
}

.search-form {
  display: flex;
  gap: 1rem;
  align-items: end;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  min-width: 150px;
}

.form-group label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.5rem;
}

.form-group input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 0.9rem;
}

.data-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.status {
  padding: 0.25rem 0.5rem;
  border-radius: 3px;
  font-size: 0.8rem;
}

.status.active {
  background-color: #d4edda;
  color: #155724;
}

.status.inactive {
  background-color: #f8d7da;
  color: #721c24;
}

.pagination {
  padding: 1.5rem 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  border-top: 1px solid #eee;
}

.page-info {
  font-weight: 600;
  color: #666;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 10px;
  min-width: 400px;
}

.modal-content h3 {
  margin-bottom: 1rem;
  color: #333;
}

.upload-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.upload-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover {
  background-color: #1e7e34;
}

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-warning {
  background-color: #ffc107;
  color: #212529;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
