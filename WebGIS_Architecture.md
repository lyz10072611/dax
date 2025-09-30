# WebGIS前端架构设计方案

## 技术栈
- **前端框架**: Vue 3 + TypeScript
- **地图引擎**: OpenLayers 8.x
- **地图服务**: GeoServer 2.22+
- **样式管理**: Tailwind CSS
- **状态管理**: Pinia
- **HTTP客户端**: Axios

## 项目结构
```
static/myVue/
├── src/
│   ├── components/
│   │   ├── map/
│   │   │   ├── MapContainer.vue          # 主地图容器
│   │   │   ├── LayerControl.vue          # 图层控制面板
│   │   │   ├── Toolbar.vue               # 地图工具栏
│   │   │   ├── InfoPanel.vue             # 信息面板
│   │   │   └── Legend.vue                # 图例组件
│   │   ├── cement/
│   │   │   ├── CementPlantList.vue       # 水泥厂列表
│   │   │   ├── CementPlantCard.vue       # 水泥厂卡片
│   │   │   ├── CementPlantForm.vue       # 水泥厂表单
│   │   │   └── DataUpload.vue            # 数据上传组件
│   │   └── common/
│   │       ├── LoadingSpinner.vue         # 加载动画
│   │       ├── ErrorMessage.vue          # 错误提示
│   │       └── ConfirmDialog.vue         # 确认对话框
│   ├── views/
│   │   ├── MapView.vue                   # 地图主页面
│   │   ├── CementPlantView.vue           # 水泥厂管理页面
│   │   └── DataManagementView.vue        # 数据管理页面
│   ├── stores/
│   │   ├── mapStore.ts                   # 地图状态管理
│   │   ├── cementStore.ts                # 水泥厂数据管理
│   │   └── layerStore.ts                 # 图层状态管理
│   ├── utils/
│   │   ├── mapUtils.ts                   # 地图工具函数
│   │   ├── geoUtils.ts                   # 地理计算工具
│   │   └── tileUtils.ts                  # 瓦片处理工具
│   ├── types/
│   │   ├── map.ts                        # 地图相关类型定义
│   │   ├── cement.ts                     # 水泥厂相关类型定义
│   │   └── geo.ts                        # 地理数据类型定义
│   └── api/
│       ├── mapApi.ts                     # 地图相关API
│       ├── cementApi.ts                  # 水泥厂API
│       └── geoApi.ts                      # 地理数据API
```

## 核心组件设计

### 1. MapContainer.vue - 主地图容器
```vue
<template>
  <div id="map" class="w-full h-full"></div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { Map, View } from 'ol'
import { OSM, TileWMS, ImageWMS } from 'ol/source'
import { Tile as TileLayer, Image as ImageLayer } from 'ol/layer'
import { useMapStore } from '@/stores/mapStore'

const mapStore = useMapStore()

onMounted(() => {
  // 初始化地图
  const map = new Map({
    target: 'map',
    view: new View({
      center: [114.3, 30.6], // 武汉中心
      zoom: 8
    }),
    layers: [
      // 底图
      new TileLayer({
        source: new OSM()
      }),
      // GeoServer WMS图层
      new TileLayer({
        source: new TileWMS({
          url: 'http://localhost:8080/geoserver/cement/wms',
          params: {
            'LAYERS': 'cement:cement_plants',
            'TILED': true
          }
        })
      })
    ]
  })
  
  mapStore.setMap(map)
})
</script>
```

### 2. LayerControl.vue - 图层控制面板
```vue
<template>
  <div class="layer-control bg-white rounded-lg shadow-lg p-4">
    <h3 class="text-lg font-semibold mb-4">图层控制</h3>
    
    <!-- 底图选择 -->
    <div class="mb-4">
      <label class="block text-sm font-medium mb-2">底图</label>
      <select v-model="selectedBaseLayer" @change="changeBaseLayer">
        <option value="osm">OpenStreetMap</option>
        <option value="satellite">卫星影像</option>
        <option value="terrain">地形图</option>
      </select>
    </div>
    
    <!-- 数据图层 -->
    <div class="mb-4">
      <label class="block text-sm font-medium mb-2">数据图层</label>
      <div v-for="layer in dataLayers" :key="layer.id" class="flex items-center mb-2">
        <input 
          type="checkbox" 
          :id="layer.id"
          v-model="layer.visible"
          @change="toggleLayer(layer)"
          class="mr-2"
        >
        <label :for="layer.id" class="text-sm">{{ layer.name }}</label>
      </div>
    </div>
    
    <!-- TIF数据图层 -->
    <div class="mb-4">
      <label class="block text-sm font-medium mb-2">遥感数据</label>
      <div v-for="tifLayer in tifLayers" :key="tifLayer.id" class="flex items-center mb-2">
        <input 
          type="checkbox" 
          :id="tifLayer.id"
          v-model="tifLayer.visible"
          @change="toggleTifLayer(tifLayer)"
          class="mr-2"
        >
        <label :for="tifLayer.id" class="text-sm">{{ tifLayer.name }}</label>
        <div class="ml-auto">
          <input 
            type="range" 
            :min="0" 
            :max="1" 
            :step="0.1"
            v-model="tifLayer.opacity"
            @input="updateTifOpacity(tifLayer)"
            class="w-16"
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useLayerStore } from '@/stores/layerStore'

const layerStore = useLayerStore()

const selectedBaseLayer = ref('osm')
const dataLayers = computed(() => layerStore.dataLayers)
const tifLayers = computed(() => layerStore.tifLayers)

const changeBaseLayer = (layer: string) => {
  layerStore.setBaseLayer(layer)
}

const toggleLayer = (layer: any) => {
  layerStore.toggleLayer(layer.id)
}

const toggleTifLayer = (layer: any) => {
  layerStore.toggleTifLayer(layer.id)
}

const updateTifOpacity = (layer: any) => {
  layerStore.updateTifOpacity(layer.id, layer.opacity)
}
</script>
```

### 3. 地图状态管理 (mapStore.ts)
```typescript
import { defineStore } from 'pinia'
import { Map } from 'ol'

export const useMapStore = defineStore('map', {
  state: () => ({
    map: null as Map | null,
    currentZoom: 8,
    currentCenter: [114.3, 30.6],
    selectedFeature: null,
    mapInteractions: {
      draw: false,
      measure: false,
      identify: false
    }
  }),
  
  actions: {
    setMap(map: Map) {
      this.map = map
      
      // 监听地图事件
      map.on('moveend', () => {
        const view = map.getView()
        this.currentCenter = view.getCenter() as [number, number]
        this.currentZoom = view.getZoom() as number
      })
      
      map.on('click', (event) => {
        this.handleMapClick(event)
      })
    },
    
    handleMapClick(event: any) {
      // 处理地图点击事件
      const features = map.getFeaturesAtPixel(event.pixel)
      if (features.length > 0) {
        this.selectedFeature = features[0]
      }
    },
    
    setInteraction(type: string, enabled: boolean) {
      this.mapInteractions[type] = enabled
    }
  }
})
```

## GeoServer配置建议

### 1. 数据源配置
```xml
<!-- cement_plants 数据源 -->
<dataStore>
  <id>cement_plants</id>
  <name>cement_plants</name>
  <description>水泥厂数据</description>
  <type>PostGIS</type>
  <enabled>true</enabled>
  <connectionParameters>
    <entry key="host">localhost</entry>
    <entry key="port">5432</entry>
    <entry key="database">cement</entry>
    <entry key="user">postgre</entry>
    <entry key="passwd">postgre</entry>
    <entry key="dbtype">postgis</entry>
    <entry key="schema">public</entry>
    <entry key="table">cement_plants</entry>
  </connectionParameters>
</dataStore>
```

### 2. 样式配置 (SLD)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld">
  <NamedLayer>
    <Name>cement_plants</Name>
    <UserStyle>
      <Title>水泥厂样式</Title>
      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#ff0000</CssParameter>
                </Fill>
                <Stroke>
                  <CssParameter name="stroke">#000000</CssParameter>
                  <CssParameter name="stroke-width">2</CssParameter>
                </Stroke>
              </Mark>
              <Size>12</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
```

## API接口设计

### 1. 地理数据API (geoApi.ts)
```typescript
import axios from 'axios'

const API_BASE = 'http://localhost:8083'

export const geoApi = {
  // 上传TIF文件
  uploadTifFile: (file: File, metadata: any) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('metadata', JSON.stringify(metadata))
    
    return axios.post(`${API_BASE}/geo/upload-tif`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  // 获取瓦片数据
  getTile: (fileId: number, z: number, x: number, y: number) => {
    return axios.get(`${API_BASE}/geo/tile/${fileId}/${z}/${x}/${y}`, {
      responseType: 'blob'
    })
  },
  
  // 获取TIF文件信息
  getTifInfo: (fileId: number) => {
    return axios.get(`${API_BASE}/geo/tif-info/${fileId}`)
  },
  
  // 处理TIF文件
  processTif: (fileId: number) => {
    return axios.post(`${API_BASE}/geo/process-tif/${fileId}`)
  }
}
```

## 部署建议

### 1. Docker Compose配置
```yaml
version: '3.8'
services:
  postgres:
    image: postgis/postgis:15-3.3
    environment:
      POSTGRES_DB: cement
      POSTGRES_USER: postgre
      POSTGRES_PASSWORD: postgre
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./src/cement.sql:/docker-entrypoint-initdb.d/cement.sql
      - ./src/geospatial_extension.sql:/docker-entrypoint-initdb.d/geospatial_extension.sql
    ports:
      - "5432:5432"
  
  geoserver:
    image: kartoza/geoserver:2.22.2
    environment:
      GEOSERVER_ADMIN_PASSWORD: admin
      GEOSERVER_ADMIN_USER: admin
    volumes:
      - geoserver_data:/opt/geoserver/data_dir
    ports:
      - "8080:8080"
    depends_on:
      - postgres
  
  app:
    build: .
    ports:
      - "8083:8083"
    depends_on:
      - postgres
    volumes:
      - ./tif-files:/app/tif-files

volumes:
  postgres_data:
  geoserver_data:
```

这个方案提供了完整的WebGIS系统架构，包括：
1. **数据库设计**：支持TIF文件存储和空间索引
2. **后端服务**：处理TIF文件上传、处理和瓦片生成
3. **前端架构**：Vue3 + OpenLayers的现代化WebGIS界面
4. **地图服务**：GeoServer提供WMS/WMTS服务
5. **部署方案**：Docker容器化部署

您可以根据具体需求调整和扩展这个方案。
