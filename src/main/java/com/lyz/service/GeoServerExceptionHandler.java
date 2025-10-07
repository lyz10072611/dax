package com.lyz.service;

import com.lyz.pojo.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpStatus;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.URISyntaxException;
import java.io.IOException;

/**
 * GeoServer异常处理工具类
 * 专门处理GeoServer相关的异常情况
 */
@Service
public class GeoServerExceptionHandler {
    
    /**
     * 处理GeoServer连接异常
     */
    public Result<Void> handleGeoServerException(Exception e) {
        if (e instanceof ConnectException) {
            return Result.geoserverConnectionError("无法连接到GeoServer服务");
        } else if (e instanceof SocketTimeoutException) {
            return Result.geoserverTimeoutError("GeoServer连接超时");
        } else if (e instanceof UnknownHostException) {
            return Result.geoserverConnectionError("GeoServer主机不可达");
        } else if (e instanceof URISyntaxException) {
            return Result.geoserverConfigError("GeoServer URL格式错误");
        } else if (e instanceof IOException) {
            return Result.geoserverServiceUnavailable("GeoServer服务不可用");
        } else if (e instanceof RestClientException) {
            return Result.geoserverServiceUnavailable("GeoServer REST服务异常");
        } else {
            return Result.geoserverUnknownError("GeoServer未知错误: " + e.getMessage());
        }
    }
    
    /**
     * 处理GeoServer HTTP响应错误
     */
    public Result<Void> handleGeoServerHttpError(HttpStatus status, String message) {
        switch (status) {
            case UNAUTHORIZED:
                return Result.geoserverAuthError(message != null ? message : "GeoServer认证失败");
            case FORBIDDEN:
                return Result.geoserverPermissionError(message != null ? message : "GeoServer权限不足");
            case NOT_FOUND:
                return Result.geoserverResourceNotFound(message != null ? message : "GeoServer资源不存在");
            case BAD_REQUEST:
                return Result.geoserverConfigError(message != null ? message : "GeoServer请求参数错误");
            case INTERNAL_SERVER_ERROR:
                return Result.geoserverProcessingError(message != null ? message : "GeoServer内部错误");
            case SERVICE_UNAVAILABLE:
                return Result.geoserverServiceUnavailable(message != null ? message : "GeoServer服务不可用");
            case GATEWAY_TIMEOUT:
                return Result.geoserverTimeoutError(message != null ? message : "GeoServer网关超时");
            default:
                return Result.geoserverUnknownError(message != null ? message : "GeoServer未知HTTP错误");
        }
    }
    
    /**
     * 处理GeoServer WMS服务异常
     */
    public Result<Void> handleWmsException(Exception e) {
        if (e instanceof ConnectException) {
            return Result.geoserverWmsError("WMS服务连接失败");
        } else if (e instanceof SocketTimeoutException) {
            return Result.geoserverWmsError("WMS服务请求超时");
        } else {
            return Result.geoserverWmsError("WMS服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理GeoServer WFS服务异常
     */
    public Result<Void> handleWfsException(Exception e) {
        if (e instanceof ConnectException) {
            return Result.geoserverWfsError("WFS服务连接失败");
        } else if (e instanceof SocketTimeoutException) {
            return Result.geoserverWfsError("WFS服务请求超时");
        } else {
            return Result.geoserverWfsError("WFS服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理GeoServer WCS服务异常
     */
    public Result<Void> handleWcsException(Exception e) {
        if (e instanceof ConnectException) {
            return Result.geoserverWcsError("WCS服务连接失败");
        } else if (e instanceof SocketTimeoutException) {
            return Result.geoserverWcsError("WCS服务请求超时");
        } else {
            return Result.geoserverWcsError("WCS服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理GeoServer瓦片服务异常
     */
    public Result<Void> handleTileException(Exception e) {
        if (e instanceof ConnectException) {
            return Result.geoserverTileError("瓦片服务连接失败");
        } else if (e instanceof SocketTimeoutException) {
            return Result.geoserverTileError("瓦片服务请求超时");
        } else {
            return Result.geoserverTileError("瓦片服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理GeoServer数据发布异常
     */
    public Result<Void> handlePublishException(Exception e) {
        if (e instanceof ConnectException) {
            return Result.geoserverPublishError("数据发布服务连接失败");
        } else if (e instanceof SocketTimeoutException) {
            return Result.geoserverPublishError("数据发布服务请求超时");
        } else {
            return Result.geoserverPublishError("数据发布异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理GeoServer图层异常
     */
    public Result<Void> handleLayerException(Exception e) {
        if (e instanceof ConnectException) {
            return Result.geoserverLayerError("图层服务连接失败");
        } else if (e instanceof SocketTimeoutException) {
            return Result.geoserverLayerError("图层服务请求超时");
        } else {
            return Result.geoserverLayerError("图层服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理GeoServer数据存储异常
     */
    public Result<Void> handleDatastoreException(Exception e) {
        if (e instanceof ConnectException) {
            return Result.geoserverDatastoreError("数据存储连接失败");
        } else if (e instanceof SocketTimeoutException) {
            return Result.geoserverDatastoreError("数据存储请求超时");
        } else {
            return Result.geoserverDatastoreError("数据存储异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理GeoServer工作空间异常
     */
    public Result<Void> handleWorkspaceException(Exception e) {
        if (e instanceof ConnectException) {
            return Result.geoserverWorkspaceError("工作空间服务连接失败");
        } else if (e instanceof SocketTimeoutException) {
            return Result.geoserverWorkspaceError("工作空间服务请求超时");
        } else {
            return Result.geoserverWorkspaceError("工作空间服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理GeoServer样式异常
     */
    public Result<Void> handleStyleException(Exception e) {
        if (e instanceof ConnectException) {
            return Result.geoserverStyleError("样式服务连接失败");
        } else if (e instanceof SocketTimeoutException) {
            return Result.geoserverStyleError("样式服务请求超时");
        } else {
            return Result.geoserverStyleError("样式服务异常: " + e.getMessage());
        }
    }
}
