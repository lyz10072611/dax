package com.lyz.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//统一响应码
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result <T>{
    private Integer code;//业务状态码 0成功 1失败
    private String message;//提示信息
    private T data;//响应数据


    //快速返回操作成功响应结果（带响应数据）
    public static <E> Result<E> success (E data) {
        return new Result<>(0,"操作成功",data);
    }
    public static Result success () {return new Result(0,"操作成功",null);}
    public static <E> Result<E> error (String message) {
        return new Result<>(1, message, null);
    }
}
