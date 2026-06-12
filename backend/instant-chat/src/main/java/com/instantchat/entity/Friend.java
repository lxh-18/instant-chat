package com.instantchat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("friends")
public class Friend extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("friend_id")
    private Long friendId;

    @TableField("group_id")
    private Long groupId;
}