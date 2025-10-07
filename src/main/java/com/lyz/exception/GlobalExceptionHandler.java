package com.lyz.exception;

import com.lyz.pojo.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.dao.NonTransientDataAccessException;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.HttpRetryException;
import java.net.URISyntaxException;
import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;

/**
 * 全局异常处理器
 * 统一处理各种异常，返回合适的HTTP状态码
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        Integer code = e.getCode();
        HttpStatus httpStatus = getHttpStatus(code);
        return ResponseEntity.status(httpStatus).body(Result.error(code, e.getMessage()));
    }
    
    /**
     * 处理参数验证异常 - @Valid注解验证失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(Result.badRequest(message));
    }
    
    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(Result.badRequest(message));
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数 '%s' 类型错误，期望类型: %s", 
                e.getName(), e.getRequiredType().getSimpleName());
        return ResponseEntity.badRequest().body(Result.badRequest(message));
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Result.badRequest(e.getMessage()));
    }
    
    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<Void>> handleDataAccessException(DataAccessException e) {
        e.printStackTrace();
        String message = "数据库访问错误";
        if (e instanceof DataIntegrityViolationException) {
            message = "数据完整性约束违反";
        } else if (e instanceof TransientDataAccessException) {
            message = "数据库临时错误，请稍后重试";
        } else if (e instanceof NonTransientDataAccessException) {
            message = "数据库永久性错误";
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.databaseError(message));
    }
    
    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Result<Void>> handleSQLException(SQLException e) {
        e.printStackTrace();
        String message = "数据库操作失败: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.databaseOperationFailed(message));
    }
    
    /**
     * 处理IO异常
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Result<Void>> handleIOException(IOException e) {
        e.printStackTrace();
        String message = "文件操作失败: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fileSystemError(message));
    }
    
    /**
     * 处理网络连接异常
     */
    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<Result<Void>> handleConnectException(ConnectException e) {
        e.printStackTrace();
        String message = "网络连接失败: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.networkError(message));
    }
    
    /**
     * 处理Socket超时异常 - GeoServer连接超时
     */
    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<Result<Void>> handleSocketTimeoutException(SocketTimeoutException e) {
        e.printStackTrace();
        String message = "连接超时: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.geoserverTimeoutError(message));
    }
    
    /**
     * 处理未知主机异常 - GeoServer主机不可达
     */
    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<Result<Void>> handleUnknownHostException(UnknownHostException e) {
        e.printStackTrace();
        String message = "主机不可达: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.geoserverConnectionError(message));
    }
    
    /**
     * 处理HTTP重试异常 - GeoServer服务重试失败
     */
    @ExceptionHandler(HttpRetryException.class)
    public ResponseEntity<Result<Void>> handleHttpRetryException(HttpRetryException e) {
        e.printStackTrace();
        String message = "HTTP请求重试失败: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.geoserverServiceUnavailable(message));
    }
    
    /**
     * 处理URI语法异常 - GeoServer URL格式错误
     */
    @ExceptionHandler(URISyntaxException.class)
    public ResponseEntity<Result<Void>> handleURISyntaxException(URISyntaxException e) {
        e.printStackTrace();
        String message = "URL格式错误: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.geoserverConfigError(message));
    }
    
    /**
     * 处理文件不存在异常 - GeoServer资源文件缺失
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Result<Void>> handleFileNotFoundException(FileNotFoundException e) {
        e.printStackTrace();
        String message = "文件不存在: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.geoserverResourceNotFound(message));
    }
    
    /**
     * 处理访问拒绝异常 - GeoServer权限不足
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(AccessDeniedException e) {
        e.printStackTrace();
        String message = "访问被拒绝: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.geoserverPermissionError(message));
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<Void>> handleNullPointerException(NullPointerException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.internalServerError("系统内部错误"));
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        String message = StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "运行时错误";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.businessLogicError(message));
    }
    
    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        e.printStackTrace();
        String message = StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "系统内部错误";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.internalServerError(message));
    }
    
    /**
     * 根据业务状态码获取对应的HTTP状态码
     */
    private HttpStatus getHttpStatus(Integer code) {
        switch (code) {
            case 400:
                return HttpStatus.BAD_REQUEST;
            case 401:
                return HttpStatus.UNAUTHORIZED;
            case 403:
                return HttpStatus.FORBIDDEN;
            case 404:
                return HttpStatus.NOT_FOUND;
            case 409:
                return HttpStatus.CONFLICT;
            case 429:
                return HttpStatus.TOO_MANY_REQUESTS;
            case 500:
            case 510:
            case 511:
            case 512:
            case 513:
            case 514:
            case 515:
            case 516:
            case 517:
            case 518:
            case 519:
            case 520:
            case 521:
            case 530: // GeoServer连接错误
            case 531: // GeoServer服务不可用
            case 532: // GeoServer工作空间错误
            case 533: // GeoServer数据存储错误
            case 534: // GeoServer图层错误
            case 535: // GeoServer样式错误
            case 536: // GeoServer WMS服务错误
            case 537: // GeoServer WFS服务错误
            case 538: // GeoServer WCS服务错误
            case 539: // GeoServer认证错误
            case 540: // GeoServer权限错误
            case 541: // GeoServer数据格式错误
            case 542: // GeoServer坐标系统错误
            case 543: // GeoServer瓦片服务错误
            case 544: // GeoServer数据发布错误
            case 545: // GeoServer配置错误
            case 546: // GeoServer超时错误
            case 547: // GeoServer资源不存在
            case 548: // GeoServer处理错误
            case 549: // GeoServer未知错误
            case 599:
                return HttpStatus.INTERNAL_SERVER_ERROR;
            case 503:
                return HttpStatus.SERVICE_UNAVAILABLE;
            case 504:
                return HttpStatus.GATEWAY_TIMEOUT;
            case 505:
                return HttpStatus.HTTP_VERSION_NOT_SUPPORTED;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}