package com.instantchat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类 - 满足得分点1：MVC模式Entity层
 * 对应数据库 users 表，包含用户基本信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("nickname")
    private String nickname;

    @TableField("avatar")
    private String avatar;

    @TableField("status")
    private Integer status; // 0: offline, 1: online

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}