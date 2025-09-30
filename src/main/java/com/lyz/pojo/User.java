package com.lyz.pojo;

import jakarta.validation.constraints.Email;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 用户实体类 - 适配水泥厂业务系统
 */
@Data
public class User {
    private Integer id; // 主键ID
    private String username; // 用户名

    @JsonIgnore  // 使用 Jackson 提供的 JsonIgnore
    private String password; // 密码

    private String nickname; // 昵称，可为空
    @Email
    private String email; // 邮箱，可为空
    private String userPic; // 头像地址
    private Integer roleId; // 角色ID（外键）
    private Integer sumDownload; // 总下载数量
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime; // 最后登录时间
    
    private Integer status; // 账户状态：0-禁用，1-启用
    
    // 验证分组接口
    public interface Add {}
    public interface Update {}
}
