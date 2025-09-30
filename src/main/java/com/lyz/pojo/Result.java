package com.lyz.pojo;

import com.lyz.enums.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果类
 * 封装所有API的返回结果，使用业务状态码进行统一管理
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code;    // 业务状态码
    private String message;  // 提示信息
    private T data;          // 响应数据

    /**
     * 成功响应 - 带数据
     */
    public static <E> Result<E> success(E data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应 - 无数据
     */
    public static Result<Void> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应 - 自定义消息
     */
    public static <E> Result<E> success(E data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应 - 自定义消息
     */
    public static <E> Result<E> error(String message) {
        return new Result<>(ResultCode.FAIL.getCode(), message, null);
    }

    /**
     * 失败响应 - 使用状态码枚举
     */
    public static <E> Result<E> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败响应 - 使用状态码枚举，自定义消息
     */
    public static <E> Result<E> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    /**
     * 失败响应 - 自定义状态码和消息
     */
    public static <E> Result<E> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 根据状态码枚举创建响应
     */
    public static <E> Result<E> result(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 根据状态码枚举创建响应 - 带数据
     */
    public static <E> Result<E> result(ResultCode resultCode, E data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    /**
     * 根据状态码枚举创建响应 - 自定义消息
     */
    public static <E> Result<E> result(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    /**
     * 根据状态码枚举创建响应 - 自定义消息和数据
     */
    public static <E> Result<E> result(ResultCode resultCode, String message, E data) {
        return new Result<>(resultCode.getCode(), message, data);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}
