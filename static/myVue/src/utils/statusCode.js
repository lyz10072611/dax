/**
 * 状态码处理工具
 * 与后端Result类的状态码保持一致
 */

// 状态码常量
export const STATUS_CODES = {
  SUCCESS: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  TOO_MANY_REQUESTS: 429,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503,
  GATEWAY_TIMEOUT: 504,
  HTTP_VERSION_NOT_SUPPORTED: 505,
  DATABASE_ERROR: 510,
  DATABASE_OPERATION_FAILED: 511,
  CACHE_ERROR: 512,
  FILE_SYSTEM_ERROR: 513,
  NETWORK_ERROR: 514,
  THIRD_PARTY_SERVICE_ERROR: 515,
  BUSINESS_LOGIC_ERROR: 516,
  DATA_VALIDATION_ERROR: 517,
  PERMISSION_ERROR: 518,
  CONFIGURATION_ERROR: 519,
  RESOURCE_EXHAUSTED: 520,
  SYSTEM_MAINTENANCE: 521,
  // GeoServer相关错误 (530-549)
  GEOSERVER_CONNECTION_ERROR: 530,
  GEOSERVER_SERVICE_UNAVAILABLE: 531,
  GEOSERVER_WORKSPACE_ERROR: 532,
  GEOSERVER_DATASTORE_ERROR: 533,
  GEOSERVER_LAYER_ERROR: 534,
  GEOSERVER_STYLE_ERROR: 535,
  GEOSERVER_WMS_ERROR: 536,
  GEOSERVER_WFS_ERROR: 537,
  GEOSERVER_WCS_ERROR: 538,
  GEOSERVER_AUTH_ERROR: 539,
  GEOSERVER_PERMISSION_ERROR: 540,
  GEOSERVER_DATA_FORMAT_ERROR: 541,
  GEOSERVER_CRS_ERROR: 542,
  GEOSERVER_TILE_ERROR: 543,
  GEOSERVER_PUBLISH_ERROR: 544,
  GEOSERVER_CONFIG_ERROR: 545,
  GEOSERVER_TIMEOUT_ERROR: 546,
  GEOSERVER_RESOURCE_NOT_FOUND: 547,
  GEOSERVER_PROCESSING_ERROR: 548,
  GEOSERVER_UNKNOWN_ERROR: 549,
  UNKNOWN_ERROR: 599,
  DEFAULT_ERROR: 500 // For generic errors
}

// 状态码对应的默认消息
export const STATUS_MESSAGES = {
  [STATUS_CODES.SUCCESS]: '操作成功',
  [STATUS_CODES.BAD_REQUEST]: '请求错误',
  [STATUS_CODES.UNAUTHORIZED]: '未授权',
  [STATUS_CODES.FORBIDDEN]: '禁止访问',
  [STATUS_CODES.NOT_FOUND]: '资源不存在',
  [STATUS_CODES.CONFLICT]: '冲突',
  [STATUS_CODES.TOO_MANY_REQUESTS]: '请求过多',
  [STATUS_CODES.INTERNAL_SERVER_ERROR]: '内部服务器错误',
  [STATUS_CODES.SERVICE_UNAVAILABLE]: '服务不可用',
  [STATUS_CODES.GATEWAY_TIMEOUT]: '网关超时',
  [STATUS_CODES.HTTP_VERSION_NOT_SUPPORTED]: 'HTTP版本不支持',
  [STATUS_CODES.DATABASE_ERROR]: '数据库错误',
  [STATUS_CODES.DATABASE_OPERATION_FAILED]: '数据库操作失败',
  [STATUS_CODES.CACHE_ERROR]: '缓存服务错误',
  [STATUS_CODES.FILE_SYSTEM_ERROR]: '文件系统错误',
  [STATUS_CODES.NETWORK_ERROR]: '网络连接错误',
  [STATUS_CODES.THIRD_PARTY_SERVICE_ERROR]: '第三方服务错误',
  [STATUS_CODES.BUSINESS_LOGIC_ERROR]: '业务逻辑错误',
  [STATUS_CODES.DATA_VALIDATION_ERROR]: '数据验证错误',
  [STATUS_CODES.PERMISSION_ERROR]: '权限验证错误',
  [STATUS_CODES.CONFIGURATION_ERROR]: '系统配置错误',
  [STATUS_CODES.RESOURCE_EXHAUSTED]: '系统资源不足',
  [STATUS_CODES.SYSTEM_MAINTENANCE]: '系统维护中',
  [STATUS_CODES.UNKNOWN_ERROR]: '未知错误',
  // GeoServer相关错误消息
  [STATUS_CODES.GEOSERVER_CONNECTION_ERROR]: 'GeoServer连接错误',
  [STATUS_CODES.GEOSERVER_SERVICE_UNAVAILABLE]: 'GeoServer服务不可用',
  [STATUS_CODES.GEOSERVER_WORKSPACE_ERROR]: 'GeoServer工作空间错误',
  [STATUS_CODES.GEOSERVER_DATASTORE_ERROR]: 'GeoServer数据存储错误',
  [STATUS_CODES.GEOSERVER_LAYER_ERROR]: 'GeoServer图层错误',
  [STATUS_CODES.GEOSERVER_STYLE_ERROR]: 'GeoServer样式错误',
  [STATUS_CODES.GEOSERVER_WMS_ERROR]: 'GeoServer WMS服务错误',
  [STATUS_CODES.GEOSERVER_WFS_ERROR]: 'GeoServer WFS服务错误',
  [STATUS_CODES.GEOSERVER_WCS_ERROR]: 'GeoServer WCS服务错误',
  [STATUS_CODES.GEOSERVER_AUTH_ERROR]: 'GeoServer认证错误',
  [STATUS_CODES.GEOSERVER_PERMISSION_ERROR]: 'GeoServer权限错误',
  [STATUS_CODES.GEOSERVER_DATA_FORMAT_ERROR]: 'GeoServer数据格式错误',
  [STATUS_CODES.GEOSERVER_CRS_ERROR]: 'GeoServer坐标系统错误',
  [STATUS_CODES.GEOSERVER_TILE_ERROR]: 'GeoServer瓦片服务错误',
  [STATUS_CODES.GEOSERVER_PUBLISH_ERROR]: 'GeoServer数据发布错误',
  [STATUS_CODES.GEOSERVER_CONFIG_ERROR]: 'GeoServer配置错误',
  [STATUS_CODES.GEOSERVER_TIMEOUT_ERROR]: 'GeoServer超时错误',
  [STATUS_CODES.GEOSERVER_RESOURCE_NOT_FOUND]: 'GeoServer资源不存在',
  [STATUS_CODES.GEOSERVER_PROCESSING_ERROR]: 'GeoServer处理错误',
  [STATUS_CODES.GEOSERVER_UNKNOWN_ERROR]: 'GeoServer未知错误'
}

// 状态码对应的用户友好消息
export const USER_FRIENDLY_MESSAGES = {
  [STATUS_CODES.SUCCESS]: '操作成功',
  [STATUS_CODES.BAD_REQUEST]: '请求参数有误，请检查输入',
  [STATUS_CODES.UNAUTHORIZED]: '请先登录',
  [STATUS_CODES.FORBIDDEN]: '权限不足，无法执行此操作',
  [STATUS_CODES.NOT_FOUND]: '请求的资源不存在',
  [STATUS_CODES.CONFLICT]: '数据冲突，请检查输入',
  [STATUS_CODES.TOO_MANY_REQUESTS]: '操作过于频繁，请稍后再试',
  [STATUS_CODES.INTERNAL_SERVER_ERROR]: '服务器错误，请稍后重试',
  [STATUS_CODES.SERVICE_UNAVAILABLE]: '服务暂时不可用，请稍后重试',
  [STATUS_CODES.GATEWAY_TIMEOUT]: '网关超时，请稍后重试',
  [STATUS_CODES.HTTP_VERSION_NOT_SUPPORTED]: 'HTTP版本不支持',
  [STATUS_CODES.DATABASE_ERROR]: '数据库连接错误，请稍后重试',
  [STATUS_CODES.DATABASE_OPERATION_FAILED]: '数据库操作失败',
  [STATUS_CODES.CACHE_ERROR]: '缓存服务错误，请稍后重试',
  [STATUS_CODES.FILE_SYSTEM_ERROR]: '文件系统错误，请稍后重试',
  [STATUS_CODES.NETWORK_ERROR]: '网络连接错误，请检查网络设置',
  [STATUS_CODES.THIRD_PARTY_SERVICE_ERROR]: '第三方服务暂时不可用，请稍后重试',
  [STATUS_CODES.BUSINESS_LOGIC_ERROR]: '业务逻辑处理错误',
  [STATUS_CODES.DATA_VALIDATION_ERROR]: '数据验证失败',
  [STATUS_CODES.PERMISSION_ERROR]: '权限验证失败',
  [STATUS_CODES.CONFIGURATION_ERROR]: '系统配置错误，请联系管理员',
  [STATUS_CODES.RESOURCE_EXHAUSTED]: '系统资源不足，请稍后重试',
  [STATUS_CODES.SYSTEM_MAINTENANCE]: '系统维护中，请稍后重试',
  [STATUS_CODES.UNKNOWN_ERROR]: '未知错误，请联系技术支持',
  // GeoServer相关用户友好消息
  [STATUS_CODES.GEOSERVER_CONNECTION_ERROR]: '地图服务连接失败，请检查网络连接',
  [STATUS_CODES.GEOSERVER_SERVICE_UNAVAILABLE]: '地图服务暂时不可用，请稍后重试',
  [STATUS_CODES.GEOSERVER_WORKSPACE_ERROR]: '地图工作空间配置错误，请联系管理员',
  [STATUS_CODES.GEOSERVER_DATASTORE_ERROR]: '地图数据存储连接失败，请稍后重试',
  [STATUS_CODES.GEOSERVER_LAYER_ERROR]: '地图图层加载失败，请刷新页面重试',
  [STATUS_CODES.GEOSERVER_STYLE_ERROR]: '地图样式配置错误，请联系管理员',
  [STATUS_CODES.GEOSERVER_WMS_ERROR]: '地图服务请求失败，请稍后重试',
  [STATUS_CODES.GEOSERVER_WFS_ERROR]: '地图要素服务请求失败，请稍后重试',
  [STATUS_CODES.GEOSERVER_WCS_ERROR]: '地图覆盖服务请求失败，请稍后重试',
  [STATUS_CODES.GEOSERVER_AUTH_ERROR]: '地图服务认证失败，请检查登录状态',
  [STATUS_CODES.GEOSERVER_PERMISSION_ERROR]: '地图服务权限不足，请联系管理员',
  [STATUS_CODES.GEOSERVER_DATA_FORMAT_ERROR]: '地图数据格式不支持，请检查数据格式',
  [STATUS_CODES.GEOSERVER_CRS_ERROR]: '地图坐标系统配置错误，请联系管理员',
  [STATUS_CODES.GEOSERVER_TILE_ERROR]: '地图瓦片加载失败，请刷新页面重试',
  [STATUS_CODES.GEOSERVER_PUBLISH_ERROR]: '地图数据发布失败，请稍后重试',
  [STATUS_CODES.GEOSERVER_CONFIG_ERROR]: '地图服务配置错误，请联系管理员',
  [STATUS_CODES.GEOSERVER_TIMEOUT_ERROR]: '地图服务请求超时，请稍后重试',
  [STATUS_CODES.GEOSERVER_RESOURCE_NOT_FOUND]: '地图资源不存在，请检查资源路径',
  [STATUS_CODES.GEOSERVER_PROCESSING_ERROR]: '地图数据处理失败，请稍后重试',
  [STATUS_CODES.GEOSERVER_UNKNOWN_ERROR]: '地图服务未知错误，请联系技术支持'
}

/**
 * 检查响应是否成功
 * @param {Object} response - API响应对象
 * @returns {boolean} 是否成功
 */
export function isSuccess(response) {
  return response && response.code === STATUS_CODES.SUCCESS
}

/**
 * 检查响应是否失败
 * @param {Object} response - API响应对象
 * @returns {boolean} 是否失败
 */
export function isError(response) {
  return !isSuccess(response)
}

/**
 * 获取用户友好的错误消息
 * @param {Object} response - API响应对象
 * @returns {string} 用户友好的错误消息
 */
export function getUserFriendlyMessage(response) {
  if (!response) return '网络错误，请稍后重试'
  
  const code = response.code
  const message = response.message
  
  // 如果有自定义消息，优先使用自定义消息
  if (message && message !== STATUS_MESSAGES[code]) {
    return message
  }
  
  // 否则使用用户友好的默认消息
  return USER_FRIENDLY_MESSAGES[code] || '未知错误'
}

/**
 * 处理API响应
 * @param {Object} response - API响应对象
 * @param {Object} options - 处理选项
 * @param {boolean} options.showSuccess - 是否显示成功消息
 * @param {boolean} options.showError - 是否显示错误消息
 * @param {Function} options.onSuccess - 成功回调
 * @param {Function} options.onError - 错误回调
 * @returns {Object} 处理结果
 */
export function handleResponse(response, options = {}) {
  const {
    showSuccess = false,
    showError = true,
    onSuccess = null,
    onError = null
  } = options
  
  const result = {
    success: isSuccess(response),
    error: isError(response),
    data: response?.data || null,
    message: response?.message || '',
    code: response?.code || STATUS_CODES.INTERNAL_SERVER_ERROR
  }
  
  if (result.success) {
    if (showSuccess) {
      alert(result.message || '操作成功')
    }
    if (onSuccess) {
      onSuccess(result.data, result.message)
    }
  } else {
    const userMessage = getUserFriendlyMessage(response)
    if (showError) {
      alert(userMessage)
    }
    if (onError) {
      onError(result.code, userMessage)
    }
  }
  
  return result
}

/**
 * 处理特定状态码的逻辑
 * @param {number} code - 状态码
 * @param {Object} response - API响应对象
 * @param {Object} options - 处理选项
 */
export function handleStatusCode(code, response, options = {}) {
  const { onUnauthorized, onForbidden, onNotFound, onConflict, onTooManyRequests } = options
  
  switch (code) {
    case STATUS_CODES.UNAUTHORIZED:
      if (onUnauthorized) {
        onUnauthorized(response)
      } else {
        // 默认处理：跳转到登录页
        window.location.href = '/myVue/index.html#/login'
      }
      break
      
    case STATUS_CODES.FORBIDDEN:
      if (onForbidden) {
        onForbidden(response)
      } else {
        alert('权限不足，无法执行此操作')
      }
      break
      
    case STATUS_CODES.NOT_FOUND:
      if (onNotFound) {
        onNotFound(response)
      } else {
        alert('请求的资源不存在')
      }
      break
      
    case STATUS_CODES.CONFLICT:
      if (onConflict) {
        onConflict(response)
      } else {
        alert('数据冲突，请检查输入')
      }
      break
      
    case STATUS_CODES.TOO_MANY_REQUESTS:
      if (onTooManyRequests) {
        onTooManyRequests(response)
      } else {
        alert('操作过于频繁，请稍后再试')
      }
      break
      
    default:
      alert(getUserFriendlyMessage(response))
      break
  }
}

/**
 * 创建API调用包装器
 * @param {Function} apiCall - API调用函数
 * @param {Object} options - 处理选项
 * @returns {Function} 包装后的API调用函数
 */
export function createApiWrapper(apiCall, options = {}) {
  return async (...args) => {
    try {
      const response = await apiCall(...args)
      return handleResponse(response, options)
    } catch (error) {
      console.error('API调用失败:', error)
      
      const errorResponse = {
        code: STATUS_CODES.INTERNAL_SERVER_ERROR,
        message: '网络错误，请稍后重试',
        data: null
      }
      
      return handleResponse(errorResponse, options)
    }
  }
}
