package com.lyz.example;

import com.lyz.pojo.Result;
import com.lyz.exception.BusinessException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

/**
 * 状态码使用示例
 * 展示如何在Controller中使用Result类的各种状态码方法
 */
@RestController
@RequestMapping("/example")
public class StatusCodeExampleController {

    /**
     * 成功响应示例
     */
    @GetMapping("/success")
    public ResponseEntity<Result<String>> success() {
        return ResponseEntity.ok(Result.success("操作成功"));
    }

    /**
     * 400 请求错误示例
     */
    @GetMapping("/bad-request")
    public ResponseEntity<Result<Void>> badRequest() {
        return ResponseEntity.ok(Result.badRequest("请求参数有误"));
    }

    /**
     * 401 未授权示例
     */
    @GetMapping("/unauthorized")
    public ResponseEntity<Result<Void>> unauthorized() {
        return ResponseEntity.ok(Result.unauthorized("请先登录"));
    }

    /**
     * 403 禁止访问示例
     */
    @GetMapping("/forbidden")
    public ResponseEntity<Result<Void>> forbidden() {
        return ResponseEntity.ok(Result.forbidden("权限不足"));
    }

    /**
     * 404 资源不存在示例
     */
    @GetMapping("/not-found")
    public ResponseEntity<Result<Void>> notFound() {
        return ResponseEntity.ok(Result.notFound("资源不存在"));
    }

    /**
     * 409 冲突示例
     */
    @GetMapping("/conflict")
    public ResponseEntity<Result<Void>> conflict() {
        return ResponseEntity.ok(Result.conflict("数据冲突"));
    }

    /**
     * 429 请求过多示例
     */
    @GetMapping("/too-many-requests")
    public ResponseEntity<Result<Void>> tooManyRequests() {
        return ResponseEntity.ok(Result.tooManyRequests("请求过于频繁"));
    }

    /**
     * 500 内部服务器错误示例
     */
    @GetMapping("/internal-error")
    public ResponseEntity<Result<Void>> internalError() {
        return ResponseEntity.ok(Result.internalServerError("内部服务器错误"));
    }

    /**
     * 503 服务不可用示例
     */
    @GetMapping("/service-unavailable")
    public ResponseEntity<Result<Void>> serviceUnavailable() {
        return ResponseEntity.ok(Result.serviceUnavailable("服务暂时不可用"));
    }

    /**
     * 510 数据库错误示例
     */
    @GetMapping("/database-error")
    public ResponseEntity<Result<Void>> databaseError() {
        return ResponseEntity.ok(Result.databaseError("数据库连接错误"));
    }

    /**
     * 511 数据库操作失败示例
     */
    @GetMapping("/database-operation-failed")
    public ResponseEntity<Result<Void>> databaseOperationFailed() {
        return ResponseEntity.ok(Result.databaseOperationFailed("数据库操作失败"));
    }

    /**
     * 512 缓存服务错误示例
     */
    @GetMapping("/cache-error")
    public ResponseEntity<Result<Void>> cacheError() {
        return ResponseEntity.ok(Result.cacheError("缓存服务错误"));
    }

    /**
     * 513 文件系统错误示例
     */
    @GetMapping("/file-system-error")
    public ResponseEntity<Result<Void>> fileSystemError() {
        return ResponseEntity.ok(Result.fileSystemError("文件系统错误"));
    }

    /**
     * 514 网络连接错误示例
     */
    @GetMapping("/network-error")
    public ResponseEntity<Result<Void>> networkError() {
        return ResponseEntity.ok(Result.networkError("网络连接错误"));
    }

    /**
     * 515 第三方服务错误示例
     */
    @GetMapping("/third-party-service-error")
    public ResponseEntity<Result<Void>> thirdPartyServiceError() {
        return ResponseEntity.ok(Result.thirdPartyServiceError("第三方服务错误"));
    }

    /**
     * 516 业务逻辑错误示例
     */
    @GetMapping("/business-logic-error")
    public ResponseEntity<Result<Void>> businessLogicError() {
        return ResponseEntity.ok(Result.businessLogicError("业务逻辑错误"));
    }

    /**
     * 517 数据验证错误示例
     */
    @GetMapping("/data-validation-error")
    public ResponseEntity<Result<Void>> dataValidationError() {
        return ResponseEntity.ok(Result.dataValidationError("数据验证失败"));
    }

    /**
     * 518 权限验证错误示例
     */
    @GetMapping("/permission-error")
    public ResponseEntity<Result<Void>> permissionError() {
        return ResponseEntity.ok(Result.permissionError("权限验证失败"));
    }

    /**
     * 519 系统配置错误示例
     */
    @GetMapping("/configuration-error")
    public ResponseEntity<Result<Void>> configurationError() {
        return ResponseEntity.ok(Result.configurationError("系统配置错误"));
    }

    /**
     * 520 系统资源不足示例
     */
    @GetMapping("/resource-exhausted")
    public ResponseEntity<Result<Void>> resourceExhausted() {
        return ResponseEntity.ok(Result.resourceExhausted("系统资源不足"));
    }

    /**
     * 521 系统维护中示例
     */
    @GetMapping("/system-maintenance")
    public ResponseEntity<Result<Void>> systemMaintenance() {
        return ResponseEntity.ok(Result.systemMaintenance("系统维护中"));
    }

    /**
     * 599 未知错误示例
     */
    @GetMapping("/unknown-error")
    public ResponseEntity<Result<Void>> unknownError() {
        return ResponseEntity.ok(Result.unknownError("未知错误"));
    }

    // ==================== GeoServer相关状态码示例 ====================

    /**
     * 530 GeoServer连接错误示例
     */
    @GetMapping("/geoserver-connection-error")
    public ResponseEntity<Result<Void>> geoserverConnectionError() {
        return ResponseEntity.ok(Result.geoserverConnectionError("GeoServer连接失败"));
    }

    /**
     * 531 GeoServer服务不可用示例
     */
    @GetMapping("/geoserver-service-unavailable")
    public ResponseEntity<Result<Void>> geoserverServiceUnavailable() {
        return ResponseEntity.ok(Result.geoserverServiceUnavailable("GeoServer服务暂时不可用"));
    }

    /**
     * 532 GeoServer工作空间错误示例
     */
    @GetMapping("/geoserver-workspace-error")
    public ResponseEntity<Result<Void>> geoserverWorkspaceError() {
        return ResponseEntity.ok(Result.geoserverWorkspaceError("GeoServer工作空间配置错误"));
    }

    /**
     * 533 GeoServer数据存储错误示例
     */
    @GetMapping("/geoserver-datastore-error")
    public ResponseEntity<Result<Void>> geoserverDatastoreError() {
        return ResponseEntity.ok(Result.geoserverDatastoreError("GeoServer数据存储连接失败"));
    }

    /**
     * 534 GeoServer图层错误示例
     */
    @GetMapping("/geoserver-layer-error")
    public ResponseEntity<Result<Void>> geoserverLayerError() {
        return ResponseEntity.ok(Result.geoserverLayerError("GeoServer图层访问失败"));
    }

    /**
     * 535 GeoServer样式错误示例
     */
    @GetMapping("/geoserver-style-error")
    public ResponseEntity<Result<Void>> geoserverStyleError() {
        return ResponseEntity.ok(Result.geoserverStyleError("GeoServer样式配置错误"));
    }

    /**
     * 536 GeoServer WMS服务错误示例
     */
    @GetMapping("/geoserver-wms-error")
    public ResponseEntity<Result<Void>> geoserverWmsError() {
        return ResponseEntity.ok(Result.geoserverWmsError("GeoServer WMS服务请求失败"));
    }

    /**
     * 537 GeoServer WFS服务错误示例
     */
    @GetMapping("/geoserver-wfs-error")
    public ResponseEntity<Result<Void>> geoserverWfsError() {
        return ResponseEntity.ok(Result.geoserverWfsError("GeoServer WFS服务请求失败"));
    }

    /**
     * 538 GeoServer WCS服务错误示例
     */
    @GetMapping("/geoserver-wcs-error")
    public ResponseEntity<Result<Void>> geoserverWcsError() {
        return ResponseEntity.ok(Result.geoserverWcsError("GeoServer WCS服务请求失败"));
    }

    /**
     * 539 GeoServer认证错误示例
     */
    @GetMapping("/geoserver-auth-error")
    public ResponseEntity<Result<Void>> geoserverAuthError() {
        return ResponseEntity.ok(Result.geoserverAuthError("GeoServer认证失败"));
    }

    /**
     * 540 GeoServer权限错误示例
     */
    @GetMapping("/geoserver-permission-error")
    public ResponseEntity<Result<Void>> geoserverPermissionError() {
        return ResponseEntity.ok(Result.geoserverPermissionError("GeoServer权限不足"));
    }

    /**
     * 541 GeoServer数据格式错误示例
     */
    @GetMapping("/geoserver-data-format-error")
    public ResponseEntity<Result<Void>> geoserverDataFormatError() {
        return ResponseEntity.ok(Result.geoserverDataFormatError("GeoServer不支持该数据格式"));
    }

    /**
     * 542 GeoServer坐标系统错误示例
     */
    @GetMapping("/geoserver-crs-error")
    public ResponseEntity<Result<Void>> geoserverCrsError() {
        return ResponseEntity.ok(Result.geoserverCrsError("GeoServer坐标系统配置错误"));
    }

    /**
     * 543 GeoServer瓦片服务错误示例
     */
    @GetMapping("/geoserver-tile-error")
    public ResponseEntity<Result<Void>> geoserverTileError() {
        return ResponseEntity.ok(Result.geoserverTileError("GeoServer瓦片服务请求失败"));
    }

    /**
     * 544 GeoServer数据发布错误示例
     */
    @GetMapping("/geoserver-publish-error")
    public ResponseEntity<Result<Void>> geoserverPublishError() {
        return ResponseEntity.ok(Result.geoserverPublishError("GeoServer数据发布失败"));
    }

    /**
     * 545 GeoServer配置错误示例
     */
    @GetMapping("/geoserver-config-error")
    public ResponseEntity<Result<Void>> geoserverConfigError() {
        return ResponseEntity.ok(Result.geoserverConfigError("GeoServer配置错误"));
    }

    /**
     * 546 GeoServer超时错误示例
     */
    @GetMapping("/geoserver-timeout-error")
    public ResponseEntity<Result<Void>> geoserverTimeoutError() {
        return ResponseEntity.ok(Result.geoserverTimeoutError("GeoServer请求超时"));
    }

    /**
     * 547 GeoServer资源不存在示例
     */
    @GetMapping("/geoserver-resource-not-found")
    public ResponseEntity<Result<Void>> geoserverResourceNotFound() {
        return ResponseEntity.ok(Result.geoserverResourceNotFound("GeoServer资源不存在"));
    }

    /**
     * 548 GeoServer处理错误示例
     */
    @GetMapping("/geoserver-processing-error")
    public ResponseEntity<Result<Void>> geoserverProcessingError() {
        return ResponseEntity.ok(Result.geoserverProcessingError("GeoServer数据处理失败"));
    }

    /**
     * 549 GeoServer未知错误示例
     */
    @GetMapping("/geoserver-unknown-error")
    public ResponseEntity<Result<Void>> geoserverUnknownError() {
        return ResponseEntity.ok(Result.geoserverUnknownError("GeoServer未知错误"));
    }

    /**
     * 使用异常抛出状态码示例
     */
    @GetMapping("/exception-example")
    public ResponseEntity<Result<Void>> exceptionExample() {
        // 抛出业务异常，会被全局异常处理器捕获
        throw new BusinessException(516, "业务逻辑处理错误");
    }

    /**
     * 模拟数据库异常示例
     */
    @GetMapping("/simulate-database-exception")
    public ResponseEntity<Result<Void>> simulateDatabaseException() {
        // 模拟数据库异常
        try {
            throw new java.sql.SQLException("数据库连接失败");
        } catch (java.sql.SQLException e) {
            // 这里会被全局异常处理器捕获并返回相应的状态码
            throw new RuntimeException("数据库操作异常", e);
        }
    }
}
