package com.lyz.pojo;

import jakarta.validation.constraints.Email;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;  // 导入 Jackson 的 JsonIgnore 注解
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
public class User {
    private Integer id; // 主键ID
    private String username; // 用户名

    @JsonIgnore  // 使用 Jackson 提供的 JsonIgnore
    private String password; // 密码

    private String nickname; // 昵称，可为空
    @Email
    private String email; // 邮箱，可为空
    @URL
    private String userPic; // 头像地址
    private Integer roleId; // 角色ID（外键）
    private Integer dailyDownload; // 每日下载数量
    private Integer sumDownload; // 总下载数量
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
