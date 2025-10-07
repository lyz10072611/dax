<template>
  <div class="map-container">
    <!-- 地图容器 -->
    <div id="map" class="map"></div>
    
    <!-- 图层控制面板 -->
    <div class="layer-control-panel">
      <div class="panel-header">
        <h3>图层控制</h3>
        <button @click="togglePanel" class="toggle-btn">
          {{ panelVisible ? '隐藏' : '显示' }}
        </button>
      </div>
      
      <div v-show="panelVisible" class="panel-content">
        <!-- 底图选择 -->
        <div class="control-group">
          <label>底图:</label>
          <select v-model="selectedBaseMap" @change="changeBaseMap">
            <option v-for="(baseMap, key) in baseMaps" :key="key" :value="key">
              {{ baseMap.name }}
            </option>
          </select>
        </div>
        
        <!-- TIF文件图层 -->
        <div v-if="fileId" class="control-group">
          <label>TIF文件图层:</label>
          <div class="layer-item">
            <div class="layer-control">
              <input 
                type="checkbox" 
                id="tif-layer"
                v-model="tifLayerVisible"
                @change="toggleTifLayer"
              >
              <label for="tif-layer">TIF影像 (ID: {{ fileId }})</label>
            </div>
            
            <div v-if="tifLayerVisible" class="layer-options">
              <div class="opacity-control">
                <label>透明度:</label>
                <input 
                  type="range" 
                  :min="0" 
                  :max="1" 
                  :step="0.1"
                  v-model="tifLayerOpacity"
                  @input="updateTifLayerOpacity"
                >
                <span>{{ Math.round(tifLayerOpacity * 100) }}%</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 图例 -->
        <div class="legend">
          <h4>图例</h4>
          <div v-if="fileId" class="legend-group">
            <h5>TIF影像</h5>
            <div class="legend-items">
              <div class="legend-item">
                <div class="legend-symbol" style="background: linear-gradient(45deg, #00ff00, #0066cc);"></div>
                <span>遥感影像</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 信息面板 -->
    <div v-if="selectedFeature" class="info-panel">
      <div class="info-header">
        <h4>要素信息</h4>
        <button @click="closeInfoPanel" class="close-btn">×</button>
      </div>
      <div class="info-content">
        <div v-for="(value, key) in selectedFeature.properties" :key="key" class="info-item">
          <strong>{{ key }}:</strong> {{ value }}
        </div>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>加载地图中...</p>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { getBaseMaps, getMapConfig } from '@/api/geoserver'

export default {
  name: 'MapContainer',
  props: {
    fileId: {
      type: [String, Number],
      default: null
    }
  },
  setup(props) {
    const map = ref(null)
    const loading = ref(true)
    const panelVisible = ref(true)
    const selectedBaseMap = ref('osm')
    const selectedFeature = ref(null)
    const tifLayerVisible = ref(true)
    const tifLayerOpacity = ref(0.8)
    
    // 获取配置
    const mapConfig = getMapConfig()
    const baseMaps = getBaseMaps()
    
    // 初始化地图
    const initMap = () => {
      try {
        // 创建简单的地图容器
        const mapElement = document.getElementById('map')
        if (mapElement) {
          mapElement.innerHTML = `
            <div style="width: 100%; height: 100%; background: #f0f0f0; display: flex; align-items: center; justify-content: center; flex-direction: column;">
              <div style="font-size: 24px; color: #666; margin-bottom: 20px;">🗺️ 地图视图</div>
              <div style="font-size: 16px; color: #999; text-align: center;">
                <p>坐标系统: ${mapConfig.projection}</p>
                <p>中心点: [${mapConfig.center[0]}, ${mapConfig.center[1]}]</p>
                <p>缩放级别: ${mapConfig.zoom}</p>
                ${props.fileId ? `<p>TIF文件ID: ${props.fileId}</p>` : ''}
              </div>
              <div style="margin-top: 20px; padding: 10px; background: #e8f4fd; border-radius: 5px; color: #0066cc;">
                <p>💡 提示: 这是一个简化的地图视图</p>
                <p>要显示完整的TIF影像，需要配置GeoServer服务</p>
              </div>
            </div>
          `
        }
        
        loading.value = false
        console.log('简化地图初始化完成')
      } catch (error) {
        console.error('地图初始化失败:', error)
        loading.value = false
      }
    }
    
    // 切换面板显示
    const togglePanel = () => {
      panelVisible.value = !panelVisible.value
    }
    
    // 关闭信息面板
    const closeInfoPanel = () => {
      selectedFeature.value = null
    }
    
    // 切换底图
    const changeBaseMap = () => {
      console.log('切换底图:', selectedBaseMap.value)
    }
    
    // 切换TIF图层
    const toggleTifLayer = () => {
      console.log('切换TIF图层:', tifLayerVisible.value)
    }
    
    // 更新TIF图层透明度
    const updateTifLayerOpacity = () => {
      console.log('更新TIF图层透明度:', tifLayerOpacity.value)
    }
    
    // 监听fileId变化
    watch(() => props.fileId, (newFileId, oldFileId) => {
      console.log('TIF文件ID变化:', oldFileId, '->', newFileId)
      if (newFileId) {
        tifLayerVisible.value = true
      }
    })
    
    // 组件挂载时初始化地图
    onMounted(() => {
      initMap()
    })
    
    // 组件卸载时清理
    onUnmounted(() => {
      console.log('地图组件卸载')
    })
    
    return {
      map,
      loading,
      panelVisible,
      selectedBaseMap,
      selectedFeature,
      tifLayerVisible,
      tifLayerOpacity,
      baseMaps,
      togglePanel,
      closeInfoPanel,
      changeBaseMap,
      toggleTifLayer,
      updateTifLayerOpacity
    }
  }
}
</script>

<style scoped>
.map-container {
  position: relative;
  width: 100%;
  height: 100vh;
}

.map {
  width: 100%;
  height: 100%;
}

.layer-control-panel {
  position: absolute;
  top: 10px;
  left: 10px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  min-width: 250px;
  z-index: 1000;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #eee;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.toggle-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.panel-content {
  padding: 15px;
}

.control-group {
  margin-bottom: 15px;
}

.control-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  font-size: 14px;
}

.control-group select {
  width: 100%;
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.layer-item {
  margin-bottom: 10px;
  padding: 8px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.layer-control {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.layer-control input[type="checkbox"] {
  margin-right: 8px;
}

.layer-control label {
  margin: 0;
  font-weight: normal;
  cursor: pointer;
}

.layer-options {
  margin-left: 20px;
}

.opacity-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.opacity-control input[type="range"] {
  flex: 1;
}

.legend {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.legend h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
}

.legend-group {
  margin-bottom: 10px;
}

.legend-group h5 {
  margin: 0 0 5px 0;
  font-size: 12px;
  color: #666;
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
}

.legend-symbol {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  border: 1px solid #000;
}

.info-panel {
  position: absolute;
  top: 10px;
  right: 10px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  min-width: 250px;
  max-width: 400px;
  z-index: 1000;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #eee;
}

.info-header h4 {
  margin: 0;
  font-size: 16px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.info-content {
  padding: 15px;
  max-height: 300px;
  overflow-y: auto;
}

.info-item {
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item strong {
  color: #333;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>