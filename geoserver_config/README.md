# GeoServer配置说明

## 概述
本目录包含水泥厂管理系统的GeoServer配置文件和样式定义。

## 文件说明

### 核心配置文件
- `workspace.xml` - GeoServer工作空间配置
- `datastore.xml` - PostgreSQL数据存储配置

### 图层配置
- `cement_plants_layer.xml` - 水泥厂数据图层配置
- `cement_plant_identifications_layer.xml` - 水泥厂识别记录图层配置
- `geospatial_files_layer.xml` - 地理空间文件图层配置

### 样式文件
- `cement_plants.sld` - 水泥厂数据点样式
- `cement_plant_identifications.sld` - 水泥厂识别记录样式（按置信度分级显示）
- `geospatial_files.sld` - 地理空间文件样式（按状态分级显示）

## 部署步骤

### 1. 创建GeoServer工作空间
```bash
# 使用REST API创建工作空间
curl -u admin:geoserver -XPOST -H "Content-type: text/xml" \
  -d @workspace.xml \
  http://localhost:8080/geoserver/rest/workspaces
```

### 2. 创建数据存储
```bash
# 创建PostGIS数据存储
curl -u admin:geoserver -XPOST -H "Content-type: text/xml" \
  -d @datastore.xml \
  http://localhost:8080/geoserver/rest/workspaces/cement_workspace/datastores
```

### 3. 发布图层
```bash
# 发布水泥厂图层
curl -u admin:geoserver -XPOST -H "Content-type: text/xml" \
  -d @cement_plants_layer.xml \
  http://localhost:8080/geoserver/rest/workspaces/cement_workspace/datastores/cement_datastore/featuretypes

# 发布识别记录图层
curl -u admin:geoserver -XPOST -H "Content-type: text/xml" \
  -d @cement_plant_identifications_layer.xml \
  http://localhost:8080/geoserver/rest/workspaces/cement_workspace/datastores/cement_datastore/featuretypes

# 发布地理空间文件图层
curl -u admin:geoserver -XPOST -H "Content-type: text/xml" \
  -d @geospatial_files_layer.xml \
  http://localhost:8080/geoserver/rest/workspaces/cement_workspace/datastores/cement_datastore/featuretypes
```

### 4. 应用样式
```bash
# 应用水泥厂样式
curl -u admin:geoserver -XPUT -H "Content-type: application/vnd.ogc.sld+xml" \
  -d @cement_plants.sld \
  http://localhost:8080/geoserver/rest/styles/cement_plants

# 应用识别记录样式
curl -u admin:geoserver -XPUT -H "Content-type: application/vnd.ogc.sld+xml" \
  -d @cement_plant_identifications.sld \
  http://localhost:8080/geoserver/rest/styles/cement_plant_identifications

# 应用地理空间文件样式
curl -u admin:geoserver -XPUT -H "Content-type: application/vnd.ogc.sld+xml" \
  -d @geospatial_files.sld \
  http://localhost:8080/geoserver/rest/styles/geospatial_files
```

## 自动化部署脚本

### Windows (PowerShell)
```powershell
# setup_geoserver.ps1
$baseUrl = "http://localhost:8080/geoserver/rest"
$auth = "admin:geoserver"

# 创建工作空间
Invoke-RestMethod -Uri "$baseUrl/workspaces" -Method POST -Headers @{"Content-Type"="text/xml"} -Body (Get-Content "workspace.xml") -Credential $auth

# 创建数据存储
Invoke-RestMethod -Uri "$baseUrl/workspaces/cement_workspace/datastores" -Method POST -Headers @{"Content-Type"="text/xml"} -Body (Get-Content "datastore.xml") -Credential $auth

# 发布图层
Invoke-RestMethod -Uri "$baseUrl/workspaces/cement_workspace/datastores/cement_datastore/featuretypes" -Method POST -Headers @{"Content-Type"="text/xml"} -Body (Get-Content "cement_plants_layer.xml") -Credential $auth
```

### Linux/Mac (Bash)
```bash
#!/bin/bash
# setup_geoserver.sh
BASE_URL="http://localhost:8080/geoserver/rest"
AUTH="admin:geoserver"

# 创建工作空间
curl -u $AUTH -XPOST -H "Content-type: text/xml" -d @workspace.xml $BASE_URL/workspaces

# 创建数据存储
curl -u $AUTH -XPOST -H "Content-type: text/xml" -d @datastore.xml $BASE_URL/workspaces/cement_workspace/datastores

# 发布图层
curl -u $AUTH -XPOST -H "Content-type: text/xml" -d @cement_plants_layer.xml $BASE_URL/workspaces/cement_workspace/datastores/cement_datastore/featuretypes
```

## 访问地址
配置完成后，可以通过以下地址访问：
- WMS服务: `http://localhost:8080/geoserver/cement_workspace/wms?service=WMS&version=1.1.0&request=GetMap&layers=cement_plants&styles=&bbox=70,15,140,55&width=800&height=600&srs=EPSG:4326&format=image/png`
- WFS服务: `http://localhost:8080/geoserver/cement_workspace/wfs?service=WFS&version=1.0.0&request=GetFeature&typeName=cement_plants&outputFormat=application/json`

## 注意事项
1. 确保PostgreSQL数据库已安装PostGIS扩展
2. 确保GeoServer已安装PostGIS数据存储插件
3. 根据实际环境修改数据库连接参数
4. 样式文件支持动态样式，可根据数据属性自动调整显示效果
