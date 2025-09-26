package com.lyz.pojo;

import lombok.Data;

@Data
public class Permission {
    private Integer id;
    private Integer roleId; // 外键：对应角色
    private String permCode; // 权限标识
    private String permName; // 权限名称
    private String description;
}


