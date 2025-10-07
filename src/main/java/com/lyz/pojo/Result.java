package com.lyz.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果类
 * 封装所有API的返回结果
 */
@NoArgsConstructor
@Data
public class Result<T> {
    private Integer code;    // HTTP状态码
    private String message;  // 提示信息
    private T data;          // 响应数据
    private Integer businessCode; // 业务状态码（可选，用于前端业务逻辑判断）
    
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public Result(Integer code, String message, T data, Integer businessCode) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.businessCode = businessCode;
    }

    // ==================== 成功响应 ====================

    /**
     * 成功响应 - 带数据
     */
    public static <E> Result<E> success(E data) {
        return new Result<>(200, "操作成功", data, 0);
    }

    /**
     * 成功响应 - 无数据
     */
    public static Result<Void> success() {
        return new Result<>(200, "操作成功", null, 0);
    }

    /**
     * 成功响应 - 自定义消息
     */
    public static <E> Result<E> success(E data, String message) {
        return new Result<>(200, message, data, 0);
    }

    // ==================== 客户端错误响应 ====================
    
    /**
     * 请求错误 - 400
     */
    public static <E> Result<E> badRequest(String message) {
        return new Result<>(400, message, null, 1);
    }

    /**
     * 未授权 - 401
     */
    public static <E> Result<E> unauthorized(String message) {
        return new Result<>(401, message, null, 1);
    }

    /**
     * 禁止访问 - 403
     */
    public static <E> Result<E> forbidden(String message) {
        return new Result<>(403, message, null, 1);
    }

    /**
     * 资源不存在 - 404
     */
    public static <E> Result<E> notFound(String message) {
        return new Result<>(404, message, null, 1);
    }

    /**
     * 冲突 - 409
     */
    public static <E> Result<E> conflict(String message) {
        return new Result<>(409, message, null, 1);
    }

    /**
     * 请求过多 - 429
     */
    public static <E> Result<E> tooManyRequests(String message) {
        return new Result<>(429, message, null, 1);
    }

    // ==================== 服务器错误响应 (500+) ====================
    
    /**
     * 内部服务器错误 - 500
     */
    public static <E> Result<E> internalServerError(String message) {
        return new Result<>(500, message, null, 1);
    }
    
    /**
     * 内部服务器错误 - 500 (默认消息)
     */
    public static <E> Result<E> internalServerError() {
        return new Result<>(500, "内部服务器错误", null, 1);
    }
    
    /**
     * 服务不可用 - 503
     */
    public static <E> Result<E> serviceUnavailable(String message) {
        return new Result<>(503, message, null, 1);
    }
    
    /**
     * 服务不可用 - 503 (默认消息)
     */
    public static <E> Result<E> serviceUnavailable() {
        return new Result<>(503, "服务暂时不可用，请稍后重试", null, 1);
    }
    
    /**
     * 网关超时 - 504
     */
    public static <E> Result<E> gatewayTimeout(String message) {
        return new Result<>(504, message, null, 1);
    }
    
    /**
     * 网关超时 - 504 (默认消息)
     */
    public static <E> Result<E> gatewayTimeout() {
        return new Result<>(504, "网关超时，请稍后重试", null, 1);
    }
    
    /**
     * HTTP版本不支持 - 505
     */
    public static <E> Result<E> httpVersionNotSupported(String message) {
        return new Result<>(505, message, null, 1);
    }
    
    /**
     * HTTP版本不支持 - 505 (默认消息)
     */
    public static <E> Result<E> httpVersionNotSupported() {
        return new Result<>(505, "HTTP版本不支持", null, 1);
    }
    
    /**
     * 数据库连接错误 - 510
     */
    public static <E> Result<E> databaseError(String message) {
        return new Result<>(510, message, null, 1);
    }
    
    /**
     * 数据库连接错误 - 510 (默认消息)
     */
    public static <E> Result<E> databaseError() {
        return new Result<>(510, "数据库连接错误，请稍后重试", null, 1);
    }
    
    /**
     * 数据库操作失败 - 511
     */
    public static <E> Result<E> databaseOperationFailed(String message) {
        return new Result<>(511, message, null, 1);
    }
    
    /**
     * 数据库操作失败 - 511 (默认消息)
     */
    public static <E> Result<E> databaseOperationFailed() {
        return new Result<>(511, "数据库操作失败", null, 1);
    }
    
    /**
     * 缓存服务错误 - 512
     */
    public static <E> Result<E> cacheError(String message) {
        return new Result<>(512, message, null, 1);
    }
    
    /**
     * 缓存服务错误 - 512 (默认消息)
     */
    public static <E> Result<E> cacheError() {
        return new Result<>(512, "缓存服务错误，请稍后重试", null, 1);
    }
    
    /**
     * 文件系统错误 - 513
     */
    public static <E> Result<E> fileSystemError(String message) {
        return new Result<>(513, message, null, 1);
    }
    
    /**
     * 文件系统错误 - 513 (默认消息)
     */
    public static <E> Result<E> fileSystemError() {
        return new Result<>(513, "文件系统错误，请稍后重试", null, 1);
    }
    
    /**
     * 网络连接错误 - 514
     */
    public static <E> Result<E> networkError(String message) {
        return new Result<>(514, message, null, 1);
    }
    
    /**
     * 网络连接错误 - 514 (默认消息)
     */
    public static <E> Result<E> networkError() {
        return new Result<>(514, "网络连接错误，请检查网络设置", null, 1);
    }
    
    /**
     * 第三方服务错误 - 515
     */
    public static <E> Result<E> thirdPartyServiceError(String message) {
        return new Result<>(515, message, null, 1);
    }
    
    /**
     * 第三方服务错误 - 515 (默认消息)
     */
    public static <E> Result<E> thirdPartyServiceError() {
        return new Result<>(515, "第三方服务暂时不可用，请稍后重试", null, 1);
    }
    
    /**
     * 业务逻辑错误 - 516
     */
    public static <E> Result<E> businessLogicError(String message) {
        return new Result<>(516, message, null, 1);
    }
    
    /**
     * 业务逻辑错误 - 516 (默认消息)
     */
    public static <E> Result<E> businessLogicError() {
        return new Result<>(516, "业务逻辑处理错误", null, 1);
    }
    
    /**
     * 数据验证错误 - 517
     */
    public static <E> Result<E> dataValidationError(String message) {
        return new Result<>(517, message, null, 1);
    }
    
    /**
     * 数据验证错误 - 517 (默认消息)
     */
    public static <E> Result<E> dataValidationError() {
        return new Result<>(517, "数据验证失败", null, 1);
    }
    
    /**
     * 权限验证错误 - 518
     */
    public static <E> Result<E> permissionError(String message) {
        return new Result<>(518, message, null, 1);
    }
    
    /**
     * 权限验证错误 - 518 (默认消息)
     */
    public static <E> Result<E> permissionError() {
        return new Result<>(518, "权限验证失败", null, 1);
    }
    
    /**
     * 系统配置错误 - 519
     */
    public static <E> Result<E> configurationError(String message) {
        return new Result<>(519, message, null, 1);
    }
    
    /**
     * 系统配置错误 - 519 (默认消息)
     */
    public static <E> Result<E> configurationError() {
        return new Result<>(519, "系统配置错误，请联系管理员", null, 1);
    }
    
    /**
     * 系统资源不足 - 520
     */
    public static <E> Result<E> resourceExhausted(String message) {
        return new Result<>(520, message, null, 1);
    }
    
    /**
     * 系统资源不足 - 520 (默认消息)
     */
    public static <E> Result<E> resourceExhausted() {
        return new Result<>(520, "系统资源不足，请稍后重试", null, 1);
    }
    
    /**
     * 系统维护中 - 521
     */
    public static <E> Result<E> systemMaintenance(String message) {
        return new Result<>(521, message, null, 1);
    }
    
    /**
     * 系统维护中 - 521 (默认消息)
     */
    public static <E> Result<E> systemMaintenance() {
        return new Result<>(521, "系统维护中，请稍后重试", null, 1);
    }
    
    /**
     * 未知错误 - 599
     */
    public static <E> Result<E> unknownError(String message) {
        return new Result<>(599, message, null, 1);
    }
    
    /**
     * 未知错误 - 599 (默认消息)
     */
    public static <E> Result<E> unknownError() {
        return new Result<>(599, "未知错误，请联系技术支持", null, 1);
    }
    
    // ==================== GeoServer相关错误 (530-549) ====================
    
    /**
     * GeoServer连接错误 - 530
     */
    public static <E> Result<E> geoserverConnectionError(String message) {
        return new Result<>(530, message, null, 1);
    }
    
    /**
     * GeoServer连接错误 - 530 (默认消息)
     */
    public static <E> Result<E> geoserverConnectionError() {
        return new Result<>(530, "GeoServer连接失败，请检查服务状态", null, 1);
    }
    
    /**
     * GeoServer服务不可用 - 531
     */
    public static <E> Result<E> geoserverServiceUnavailable(String message) {
        return new Result<>(531, message, null, 1);
    }
    
    /**
     * GeoServer服务不可用 - 531 (默认消息)
     */
    public static <E> Result<E> geoserverServiceUnavailable() {
        return new Result<>(531, "GeoServer服务暂时不可用，请稍后重试", null, 1);
    }
    
    /**
     * GeoServer工作空间错误 - 532
     */
    public static <E> Result<E> geoserverWorkspaceError(String message) {
        return new Result<>(532, message, null, 1);
    }
    
    /**
     * GeoServer工作空间错误 - 532 (默认消息)
     */
    public static <E> Result<E> geoserverWorkspaceError() {
        return new Result<>(532, "GeoServer工作空间配置错误", null, 1);
    }
    
    /**
     * GeoServer数据存储错误 - 533
     */
    public static <E> Result<E> geoserverDatastoreError(String message) {
        return new Result<>(533, message, null, 1);
    }
    
    /**
     * GeoServer数据存储错误 - 533 (默认消息)
     */
    public static <E> Result<E> geoserverDatastoreError() {
        return new Result<>(533, "GeoServer数据存储连接失败", null, 1);
    }
    
    /**
     * GeoServer图层错误 - 534
     */
    public static <E> Result<E> geoserverLayerError(String message) {
        return new Result<>(534, message, null, 1);
    }
    
    /**
     * GeoServer图层错误 - 534 (默认消息)
     */
    public static <E> Result<E> geoserverLayerError() {
        return new Result<>(534, "GeoServer图层访问失败", null, 1);
    }
    
    /**
     * GeoServer样式错误 - 535
     */
    public static <E> Result<E> geoserverStyleError(String message) {
        return new Result<>(535, message, null, 1);
    }
    
    /**
     * GeoServer样式错误 - 535 (默认消息)
     */
    public static <E> Result<E> geoserverStyleError() {
        return new Result<>(535, "GeoServer样式配置错误", null, 1);
    }
    
    /**
     * GeoServer WMS服务错误 - 536
     */
    public static <E> Result<E> geoserverWmsError(String message) {
        return new Result<>(536, message, null, 1);
    }
    
    /**
     * GeoServer WMS服务错误 - 536 (默认消息)
     */
    public static <E> Result<E> geoserverWmsError() {
        return new Result<>(536, "GeoServer WMS服务请求失败", null, 1);
    }
    
    /**
     * GeoServer WFS服务错误 - 537
     */
    public static <E> Result<E> geoserverWfsError(String message) {
        return new Result<>(537, message, null, 1);
    }
    
    /**
     * GeoServer WFS服务错误 - 537 (默认消息)
     */
    public static <E> Result<E> geoserverWfsError() {
        return new Result<>(537, "GeoServer WFS服务请求失败", null, 1);
    }
    
    /**
     * GeoServer WCS服务错误 - 538
     */
    public static <E> Result<E> geoserverWcsError(String message) {
        return new Result<>(538, message, null, 1);
    }
    
    /**
     * GeoServer WCS服务错误 - 538 (默认消息)
     */
    public static <E> Result<E> geoserverWcsError() {
        return new Result<>(538, "GeoServer WCS服务请求失败", null, 1);
    }
    
    /**
     * GeoServer认证错误 - 539
     */
    public static <E> Result<E> geoserverAuthError(String message) {
        return new Result<>(539, message, null, 1);
    }
    
    /**
     * GeoServer认证错误 - 539 (默认消息)
     */
    public static <E> Result<E> geoserverAuthError() {
        return new Result<>(539, "GeoServer认证失败，请检查用户名和密码", null, 1);
    }
    
    /**
     * GeoServer权限错误 - 540
     */
    public static <E> Result<E> geoserverPermissionError(String message) {
        return new Result<>(540, message, null, 1);
    }
    
    /**
     * GeoServer权限错误 - 540 (默认消息)
     */
    public static <E> Result<E> geoserverPermissionError() {
        return new Result<>(540, "GeoServer权限不足，无法访问该资源", null, 1);
    }
    
    /**
     * GeoServer数据格式错误 - 541
     */
    public static <E> Result<E> geoserverDataFormatError(String message) {
        return new Result<>(541, message, null, 1);
    }
    
    /**
     * GeoServer数据格式错误 - 541 (默认消息)
     */
    public static <E> Result<E> geoserverDataFormatError() {
        return new Result<>(541, "GeoServer不支持该数据格式", null, 1);
    }
    
    /**
     * GeoServer坐标系统错误 - 542
     */
    public static <E> Result<E> geoserverCrsError(String message) {
        return new Result<>(542, message, null, 1);
    }
    
    /**
     * GeoServer坐标系统错误 - 542 (默认消息)
     */
    public static <E> Result<E> geoserverCrsError() {
        return new Result<>(542, "GeoServer坐标系统配置错误", null, 1);
    }
    
    /**
     * GeoServer瓦片服务错误 - 543
     */
    public static <E> Result<E> geoserverTileError(String message) {
        return new Result<>(543, message, null, 1);
    }
    
    /**
     * GeoServer瓦片服务错误 - 543 (默认消息)
     */
    public static <E> Result<E> geoserverTileError() {
        return new Result<>(543, "GeoServer瓦片服务请求失败", null, 1);
    }
    
    /**
     * GeoServer数据发布错误 - 544
     */
    public static <E> Result<E> geoserverPublishError(String message) {
        return new Result<>(544, message, null, 1);
    }
    
    /**
     * GeoServer数据发布错误 - 544 (默认消息)
     */
    public static <E> Result<E> geoserverPublishError() {
        return new Result<>(544, "GeoServer数据发布失败", null, 1);
    }
    
    /**
     * GeoServer配置错误 - 545
     */
    public static <E> Result<E> geoserverConfigError(String message) {
        return new Result<>(545, message, null, 1);
    }
    
    /**
     * GeoServer配置错误 - 545 (默认消息)
     */
    public static <E> Result<E> geoserverConfigError() {
        return new Result<>(545, "GeoServer配置错误，请联系管理员", null, 1);
    }
    
    /**
     * GeoServer超时错误 - 546
     */
    public static <E> Result<E> geoserverTimeoutError(String message) {
        return new Result<>(546, message, null, 1);
    }
    
    /**
     * GeoServer超时错误 - 546 (默认消息)
     */
    public static <E> Result<E> geoserverTimeoutError() {
        return new Result<>(546, "GeoServer请求超时，请稍后重试", null, 1);
    }
    
    /**
     * GeoServer资源不存在 - 547
     */
    public static <E> Result<E> geoserverResourceNotFound(String message) {
        return new Result<>(547, message, null, 1);
    }
    
    /**
     * GeoServer资源不存在 - 547 (默认消息)
     */
    public static <E> Result<E> geoserverResourceNotFound() {
        return new Result<>(547, "GeoServer资源不存在", null, 1);
    }
    
    /**
     * GeoServer处理错误 - 548
     */
    public static <E> Result<E> geoserverProcessingError(String message) {
        return new Result<>(548, message, null, 1);
    }
    
    /**
     * GeoServer处理错误 - 548 (默认消息)
     */
    public static <E> Result<E> geoserverProcessingError() {
        return new Result<>(548, "GeoServer数据处理失败", null, 1);
    }
    
    /**
     * GeoServer未知错误 - 549
     */
    public static <E> Result<E> geoserverUnknownError(String message) {
        return new Result<>(549, message, null, 1);
    }
    
    /**
     * GeoServer未知错误 - 549 (默认消息)
     */
    public static <E> Result<E> geoserverUnknownError() {
        return new Result<>(549, "GeoServer未知错误，请联系技术支持", null, 1);
    }

    /**
     * 失败响应 - 自定义消息（默认500）
     */
    public static <E> Result<E> error(String message) {
        return new Result<>(500, message, null, 1);
    }

    /**
     * 失败响应 - 自定义状态码和消息
     */
    public static <E> Result<E> error(Integer code, String message) {
        return new Result<>(code, message, null, 1);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return Integer.valueOf(200).equals(this.code);
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
    
    /**
     * 判断业务是否成功（基于businessCode）
     */
    public boolean isBusinessSuccess() {
        return this.businessCode != null && this.businessCode == 0;
    }
}
