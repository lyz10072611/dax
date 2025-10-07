<template>
  <div class="tif-management-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>TIF文件管理</h2>
      <div class="header-actions">
        <button @click="showUploadDialog = true" class="btn btn-primary" :disabled="!canUpload">
          <span class="icon">📁</span>
          上传TIF文件
        </button>
        <button @click="refreshFiles" class="btn btn-secondary">
          <span class="icon">🔄</span>
          刷新
        </button>
      </div>
    </div>

    <!-- 文件列表 -->
    <div class="file-list">
      <div class="list-header">
        <h3>TIF文件列表</h3>
        <div class="filter-controls">
          <select v-model="filterStatus" @change="loadFiles">
            <option value="">全部状态</option>
            <option value="processing">处理中</option>
            <option value="processed">已处理</option>
            <option value="active">活跃</option>
            <option value="error">错误</option>
          </select>
        </div>
      </div>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="files.length === 0" class="empty-state">
        <div class="empty-icon">📄</div>
        <p>暂无TIF文件</p>
        <button @click="showUploadDialog = true" class="btn btn-primary" :disabled="!canUpload">
          上传第一个文件
        </button>
      </div>

      <div v-else class="file-grid">
        <div v-for="file in files" :key="file.fileId" class="file-card">
          <div class="file-header">
            <h4>{{ file.fileName }}</h4>
            <div class="file-status" :class="file.status">
              {{ getStatusText(file.status) }}
            </div>
          </div>
          
          <div class="file-info">
            <div class="info-item">
              <span class="label">文件大小:</span>
              <span class="value">{{ formatFileSize(file.fileSize) }}</span>
            </div>
            <div class="info-item">
              <span class="label">坐标系:</span>
              <span class="value">{{ file.coordinateSystem }}</span>
            </div>
            <div class="info-item">
              <span class="label">波段数:</span>
              <span class="value">{{ file.bandsCount }}</span>
            </div>
            <div class="info-item">
              <span class="label">数据类型:</span>
              <span class="value">{{ file.dataType }}</span>
            </div>
            <div v-if="file.boundsWest" class="info-item">
              <span class="label">边界范围:</span>
              <span class="value">
                {{ file.boundsWest }}, {{ file.boundsSouth }} - 
                {{ file.boundsEast }}, {{ file.boundsNorth }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">上传时间:</span>
              <span class="value">{{ formatDate(file.uploadTime) }}</span>
            </div>
          </div>

          <div class="file-actions">
            <button @click="viewOnMap(file)" class="btn btn-sm btn-primary">
              <span class="icon">🗺️</span>
              在地图中查看
            </button>
            <button @click="downloadFile(file)" class="btn btn-sm btn-secondary" :disabled="!canDownload">
              <span class="icon">⬇️</span>
              下载
            </button>
            <button v-if="file.status === 'error'" @click="reprocessFile(file)" class="btn btn-sm btn-warning">
              <span class="icon">🔄</span>
              重新处理
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 上传对话框 -->
    <div v-if="showUploadDialog" class="modal-overlay" @click="closeUploadDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>上传TIF文件</h3>
          <button @click="closeUploadDialog" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="upload-form">
            <div class="form-group">
              <label>选择TIF文件:</label>
              <input 
                type="file" 
                @change="handleFileSelect" 
                accept=".tif,.tiff"
                ref="fileInput"
              >
              <div v-if="selectedFile" class="file-info">
                <p><strong>文件名:</strong> {{ selectedFile.name }}</p>
                <p><strong>大小:</strong> {{ formatFileSize(selectedFile.size) }}</p>
              </div>
            </div>
            
            <div class="form-group">
              <label>关联水泥厂 (可选):</label>
              <select v-model="uploadPlantId">
                <option value="">不关联水泥厂</option>
                <option v-for="plant in cementPlants" :key="plant.plantId" :value="plant.plantId">
                  {{ plant.plantName }}
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label>数据类型:</label>
              <select v-model="uploadDataType">
                <option value="NDVI">NDVI</option>
                <option value="RGB">RGB</option>
                <option value="NIR">近红外</option>
                <option value="THERMAL">热红外</option>
                <option value="OTHER">其他</option>
              </select>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeUploadDialog" class="btn btn-secondary">取消</button>
          <button @click="uploadFile" class="btn btn-primary" :disabled="!selectedFile || uploading">
            <span v-if="uploading" class="spinner-small"></span>
            {{ uploading ? '上传中...' : '上传' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 下载状态对话框 -->
    <div v-if="showDownloadDialog" class="modal-overlay" @click="closeDownloadDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>下载状态</h3>
          <button @click="closeDownloadDialog" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="download-info">
            <div class="info-item">
              <span class="label">今日已下载:</span>
              <span class="value">{{ downloadStatus.dailyCount || 0 }} 次</span>
            </div>
            <div class="info-item">
              <span class="label">每日限制:</span>
              <span class="value">{{ downloadStatus.dailyLimit || 0 }} 次</span>
            </div>
            <div class="info-item">
              <span class="label">剩余次数:</span>
              <span class="value">{{ (downloadStatus.dailyLimit || 0) - (downloadStatus.dailyCount || 0) }} 次</span>
            </div>
            <div class="info-item">
              <span class="label">用户角色:</span>
              <span class="value">{{ downloadStatus.roleCode === 0 ? '管理员' : '普通用户' }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeDownloadDialog" class="btn btn-primary">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

// 响应式数据
const files = ref([])
const loading = ref(false)
const uploading = ref(false)
const showUploadDialog = ref(false)
const showDownloadDialog = ref(false)
const selectedFile = ref(null)
const uploadPlantId = ref('')
const uploadDataType = ref('NDVI')
const filterStatus = ref('')
const cementPlants = ref([])
const downloadStatus = ref({})

// 计算属性
const canUpload = computed(() => {
  return auth.userInfo?.roleCode === 0
})

const canDownload = computed(() => {
  return downloadStatus.value.dailyCount < downloadStatus.value.dailyLimit
})

// 方法
const loadFiles = async () => {
  loading.value = true
  try {
    // 模拟加载数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    files.value = [
      {
        fileId: 1,
        fileName: 'sample_ndvi.tif',
        fileSize: 1024000,
        coordinateSystem: 'EPSG:4326',
        bandsCount: 1,
        dataType: 'FLOAT32',
        boundsWest: 114.3,
        boundsEast: 114.4,
        boundsSouth: 38.1,
        boundsNorth: 38.2,
        uploadTime: new Date().toISOString(),
        status: 'processed'
      }
    ]
  } catch (error) {
    console.error('加载TIF文件失败:', error)
    alert('加载TIF文件失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const loadCementPlants = async () => {
  try {
    // 模拟加载水泥厂数据
    cementPlants.value = [
      { plantId: 1, plantName: '测试水泥厂1' },
      { plantId: 2, plantName: '测试水泥厂2' }
    ]
  } catch (error) {
    console.error('加载水泥厂列表失败:', error)
  }
}

const loadDownloadStatus = async () => {
  try {
    // 模拟下载状态
    downloadStatus.value = {
      dailyCount: 0,
      dailyLimit: 10,
      roleCode: auth.userInfo?.roleCode || 1
    }
  } catch (error) {
    console.error('加载下载状态失败:', error)
  }
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    if (!file.name.toLowerCase().endsWith('.tif') && !file.name.toLowerCase().endsWith('.tiff')) {
      alert('请选择TIF格式的文件')
      return
    }
    selectedFile.value = file
  }
}

const uploadFile = async () => {
  if (!selectedFile.value) return
  
  uploading.value = true
  try {
    // 模拟上传
    await new Promise(resolve => setTimeout(resolve, 2000))
    alert('文件上传成功！')
    closeUploadDialog()
    loadFiles()
  } catch (error) {
    console.error('上传失败:', error)
    alert('上传失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

const downloadFile = async (file) => {
  if (!canDownload.value) {
    showDownloadDialog.value = true
    return
  }
  
  try {
    alert(`开始下载文件: ${file.fileName}`)
    // 模拟下载
    downloadStatus.value.dailyCount++
  } catch (error) {
    console.error('下载失败:', error)
    alert('下载失败，请稍后重试')
  }
}

const viewOnMap = (file) => {
  // 跳转到地图页面并传递文件ID
  router.push({
    path: '/map',
    query: { fileId: file.fileId }
  })
}

const reprocessFile = async (file) => {
  try {
    alert(`重新处理文件: ${file.fileName}`)
    loadFiles()
  } catch (error) {
    console.error('重新处理失败:', error)
    alert('重新处理失败，请稍后重试')
  }
}

const refreshFiles = () => {
  loadFiles()
  loadDownloadStatus()
}

const closeUploadDialog = () => {
  showUploadDialog.value = false
  selectedFile.value = null
  uploadPlantId.value = ''
  uploadDataType.value = 'NDVI'
}

const closeDownloadDialog = () => {
  showDownloadDialog.value = false
}

const getStatusText = (status) => {
  const statusMap = {
    'processing': '处理中',
    'processed': '已处理',
    'active': '活跃',
    'error': '错误'
  }
  return statusMap[status] || status
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString()
}

// 生命周期
onMounted(() => {
  loadFiles()
  loadCementPlants()
  loadDownloadStatus()
})
</script>

<style scoped>
.tif-management-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #eee;
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 28px;
}

.header-actions {
  display: flex;
  gap: 15px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #545b62;
}

.btn-warning {
  background: #ffc107;
  color: #212529;
}

.btn-warning:hover:not(:disabled) {
  background: #e0a800;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.icon {
  font-size: 16px;
}

.file-list {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.list-header h3 {
  margin: 0;
  color: #333;
}

.filter-controls select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

.spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #666;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  padding: 20px;
}

.file-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  background: white;
  transition: box-shadow 0.2s;
}

.file-card:hover {
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.file-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.file-header h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.file-status.processing {
  background: #fff3cd;
  color: #856404;
}

.file-status.processed {
  background: #d4edda;
  color: #155724;
}

.file-status.active {
  background: #d1ecf1;
  color: #0c5460;
}

.file-status.error {
  background: #f8d7da;
  color: #721c24;
}

.file-info {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #666;
  font-weight: 500;
}

.info-item .value {
  color: #333;
  text-align: right;
  max-width: 60%;
  word-break: break-all;
}

.file-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
}

.upload-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #333;
}

.form-group input,
.form-group select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.file-info {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 14px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.download-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .file-grid {
    grid-template-columns: 1fr;
    padding: 15px;
  }
  
  .file-actions {
    justify-content: center;
  }
  
  .modal-content {
    width: 95%;
    margin: 10px;
  }
}
</style>