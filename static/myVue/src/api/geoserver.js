// GeoServer地图API配置
// 用于前端地图集成

const GEOSERVER_CONFIG = {
  // GeoServer基础配置 (当前使用模拟数据)
  baseUrl: 'http://localhost:8080/geoserver',
  workspace: 'cement',
  username: 'admin',
  password: 'geoserver',
  // 模拟模式标志
  mockMode: true,
  
  // WMS服务配置
  wms: {
    url: 'http://localhost:8080/geoserver/cement/wms',
    version: '1.3.0',
    format: 'image/png',
    transparent: true,
    crs: 'EPSG:4326'
  },
  
  // WFS服务配置
  wfs: {
    url: 'http://localhost:8080/geoserver/cement/wfs',
    version: '2.0.0',
    outputFormat: 'application/json',
    crs: 'EPSG:4326'
  },
  
  // 图层配置
  layers: {
    cementPlants: {
      name: 'cement:cement_plants',
      title: '水泥厂位置',
      style: 'cement_plants',
      visible: true,
      opacity: 1.0,
      legend: {
        title: '水泥厂',
        items: [
          { color: '#00ff00', label: '活跃' },
          { color: '#ff0000', label: '非活跃' },
          { color: '#ffff00', label: '建设中' }
        ]
      }
    },
    
    cementPlantIdentifications: {
      name: 'cement:cement_plant_identifications',
      title: '水泥厂识别记录',
      style: 'cement_plant_identifications',
      visible: true,
      opacity: 0.8,
      legend: {
        title: '识别记录',
        items: [
          { shape: 'square', color: '#0066cc', label: 'GF-2' },
          { shape: 'triangle', color: '#ff6600', label: 'Landsat-8' },
          { shape: 'star', color: '#9900cc', label: 'Sentinel-2' }
        ]
      }
    },
    
    geospatialFiles: {
      name: 'cement:geospatial_files',
      title: '地理数据文件',
      style: 'geospatial_files',
      visible: false,
      opacity: 0.5,
      legend: {
        title: 'TIF文件边界',
        items: [
          { color: '#00ff00', label: 'TIF文件' },
          { color: '#ffaa00', label: '其他格式' }
        ]
      }
    }
  },
  
  // 地图默认配置
  map: {
    center: [114.3, 30.6], // 武汉中心
    zoom: 8,
    projection: 'EPSG:4326',
    maxZoom: 18,
    minZoom: 3
  },
  
  // 底图配置
  baseMaps: {
    osm: {
      name: 'OpenStreetMap',
      url: 'https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      attribution: '© OpenStreetMap contributors'
    },
    satellite: {
      name: '卫星影像',
      url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
      attribution: '© Esri'
    },
    terrain: {
      name: '地形图',
      url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}',
      attribution: '© Esri'
    }
  }
};

// 获取WMS图层URL
export function getWmsLayerUrl(layerName, options = {}) {
  if (GEOSERVER_CONFIG.mockMode) {
    // 模拟模式：返回模拟的图层URL
    console.log(`模拟WMS图层URL: ${layerName}`)
    return `mock://wms/${layerName}`
  }
  
  const layer = GEOSERVER_CONFIG.layers[layerName];
  if (!layer) {
    throw new Error(`图层 ${layerName} 不存在`);
  }
  
  const params = {
    SERVICE: 'WMS',
    VERSION: GEOSERVER_CONFIG.wms.version,
    REQUEST: 'GetMap',
    LAYERS: layer.name,
    STYLES: layer.style || '',
    FORMAT: GEOSERVER_CONFIG.wms.format,
    TRANSPARENT: GEOSERVER_CONFIG.wms.transparent,
    CRS: GEOSERVER_CONFIG.wms.crs,
    ...options
  };
  
  const queryString = Object.keys(params)
    .map(key => `${key}=${encodeURIComponent(params[key])}`)
    .join('&');
    
  return `${GEOSERVER_CONFIG.wms.url}?${queryString}`;
}

// 获取WFS图层URL
export function getWfsLayerUrl(layerName, options = {}) {
  const layer = GEOSERVER_CONFIG.layers[layerName];
  if (!layer) {
    throw new Error(`图层 ${layerName} 不存在`);
  }
  
  const params = {
    SERVICE: 'WFS',
    VERSION: GEOSERVER_CONFIG.wfs.version,
    REQUEST: 'GetFeature',
    TYPENAME: layer.name,
    OUTPUTFORMAT: GEOSERVER_CONFIG.wfs.outputFormat,
    SRSNAME: GEOSERVER_CONFIG.wfs.crs,
    ...options
  };
  
  const queryString = Object.keys(params)
    .map(key => `${key}=${encodeURIComponent(params[key])}`)
    .join('&');
    
  return `${GEOSERVER_CONFIG.wfs.url}?${queryString}`;
}

// 获取图层信息
export function getLayerInfo(layerName) {
  return GEOSERVER_CONFIG.layers[layerName];
}

// 获取所有图层
export function getAllLayers() {
  return GEOSERVER_CONFIG.layers;
}

// 获取地图配置
export function getMapConfig() {
  return GEOSERVER_CONFIG.map;
}

// 获取底图配置
export function getBaseMaps() {
  return GEOSERVER_CONFIG.baseMaps;
}

export default GEOSERVER_CONFIG;
