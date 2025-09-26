package com.lyz.pojo;

import lombok.Data;

@Data
public class Role {
    private Integer id;
    private Integer roleCode; // 0=管理员,1=用户,2=游客
    private String roleName;
    private String description;
}


