package com.lyz.enums;

/**
 * 业务状态码枚举
 * 统一管理系统中常用的业务状态码
 */
public enum ResultCode {
    
    // 通用状态码
    SUCCESS(0, "操作成功"),
    FAIL(1, "操作失败"),
    
    // HTTP标准状态码
    BAD_REQUEST(400, "请求错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "冲突"),
    TOO_MANY_REQUESTS(429, "请求过多"),
    INTERNAL_SERVER_ERROR(500, "内部服务器错误"),
    
    // 用户相关状态码
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_ALREADY_TAKEN(1002, "用户名已被占用"),
    PASSWORD_ERROR(1003, "密码错误"),
    OLD_PASSWORD_ERROR(1004, "原密码不正确"),
    PASSWORD_MISMATCH(1005, "两次输入的密码不同"),
    PASSWORD_SAME_AS_OLD(1006, "新修改的密码不能与旧密码相同"),
    
    // 权限相关状态码
    TOKEN_EXPIRED(2001, "访问令牌已过期"),
    TOKEN_INVALID(2002, "访问令牌无效"),
    TOKEN_MISSING(2003, "缺少访问令牌"),
    
    // 参数相关状态码
    PARAM_MISSING(3001, "缺少必要参数"),
    PARAM_INVALID(3002, "参数不合法"),
    
    // 文件相关状态码
    FILE_NOT_FOUND(4001, "文件不存在"),
    FILE_UPLOAD_FAILED(4002, "文件上传失败"),
    
    // 下载相关状态码
    DOWNLOAD_QUOTA_EXCEEDED(5001, "今日下载次数已达上限"),
    
    // 数据相关状态码
    DATA_NOT_FOUND(6001, "数据不存在"),
    DATA_ALREADY_EXISTS(6002, "数据已存在");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * 根据状态码获取枚举
     */
    public static ResultCode getByCode(Integer code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return null;
    }
}
