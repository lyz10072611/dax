<template>
  <div class="map-page">
    <!-- 地图容器 -->
    <div class="map-wrapper">
      <MapContainer :fileId="fileId" />
    </div>
    
    <!-- 状态栏 -->
    <footer class="status-bar">
      <div class="status-info">
        <span>坐标系统: EPSG:4326</span>
        <span>服务状态: {{ serviceStatus }}</span>
        <span>图层数量: {{ layerCount }}</span>
      </div>
      <div class="status-actions">
        <button @click="showAbout" class="btn btn-link">关于</button>
      </div>
    </footer>
    
    <!-- 关于对话框 -->
    <div v-if="showAboutDialog" class="modal-overlay" @click="closeAbout">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>关于水泥厂WebGIS系统</h3>
          <button @click="closeAbout" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="about-section">
            <h4>系统信息</h4>
            <ul>
              <li><strong>版本:</strong> 1.0.0</li>
              <li><strong>技术栈:</strong> Vue 3 + OpenLayers + GeoServer</li>
              <li><strong>数据库:</strong> PostgreSQL + PostGIS</li>
              <li><strong>地图服务:</strong> GeoServer 2.22+</li>
            </ul>
          </div>
          
          <div class="about-section">
            <h4>功能特性</h4>
            <ul>
              <li>水泥厂位置展示</li>
              <li>遥感识别记录管理</li>
              <li>TIF文件边界显示</li>
              <li>多底图切换</li>
              <li>图层控制</li>
              <li>要素信息查询</li>
            </ul>
          </div>
          
          <div class="about-section">
            <h4>服务地址</h4>
            <ul>
              <li><strong>WMS服务:</strong> http://localhost:8080/geoserver/cement/wms</li>
              <li><strong>WFS服务:</strong> http://localhost:8080/geoserver/cement/wfs</li>
              <li><strong>管理界面:</strong> http://localhost:8080/geoserver/web</li>
            </ul>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeAbout" class="btn btn-primary">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MapContainer from '@/components/MapContainer.vue'
import { getAllLayers } from '@/api/geoserver'

export default {
  name: 'MapView',
  components: {
    MapContainer
  },
  setup() {
    const route = useRoute()
    const showAboutDialog = ref(false)
    const serviceStatus = ref('正常')
    const fileId = ref(route.query.fileId || null)
    
    const layerCount = computed(() => {
      const layers = getAllLayers()
      return Object.keys(layers).length
    })
    
    // 刷新地图
    const refreshMap = () => {
      window.location.reload()
    }
    
    // 切换全屏
    const toggleFullscreen = () => {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen()
      } else {
        document.exitFullscreen()
      }
    }
    
    // 显示关于对话框
    const showAbout = () => {
      showAboutDialog.value = true
    }
    
    // 关闭关于对话框
    const closeAbout = () => {
      showAboutDialog.value = false
    }
    
    // 检查服务状态
    const checkServiceStatus = async () => {
      try {
        // 模拟服务状态检查，避免CORS问题
        await new Promise(resolve => setTimeout(resolve, 1000))
        serviceStatus.value = '正常 (模拟)'
        console.log('服务状态检查完成 (使用模拟数据)')
      } catch (error) {
        serviceStatus.value = '离线'
        console.error('服务状态检查失败:', error)
      }
    }
    
    // 组件挂载时检查服务状态
    onMounted(() => {
      checkServiceStatus()
    })
    
    return {
      showAboutDialog,
      serviceStatus,
      layerCount,
      fileId,
      refreshMap,
      toggleFullscreen,
      showAbout,
      closeAbout
    }
  }
}
</script>

<style scoped>
.map-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}


.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.btn-link {
  background: none;
  color: #007bff;
  text-decoration: underline;
}

.btn-link:hover {
  color: #0056b3;
}

.map-wrapper {
  flex: 1;
  position: relative;
}

.status-bar {
  background: white;
  border-top: 1px solid #ddd;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.status-info {
  display: flex;
  gap: 20px;
}

.status-actions {
  display: flex;
  gap: 10px;
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
  z-index: 3000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 600px;
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
  font-size: 18px;
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

.about-section {
  margin-bottom: 20px;
}

.about-section h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.about-section ul {
  margin: 0;
  padding-left: 20px;
}

.about-section li {
  margin-bottom: 5px;
  font-size: 14px;
  color: #666;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
}

.icon-refresh::before {
  content: "🔄";
}

.icon-fullscreen::before {
  content: "⛶";
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: center;
  }
  
  .status-info {
    flex-direction: column;
    gap: 5px;
  }
  
  .modal-content {
    width: 95%;
    margin: 10px;
  }
}
</style>
