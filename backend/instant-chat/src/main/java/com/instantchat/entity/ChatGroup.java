package com.instantchat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_groups")
public class ChatGroup extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("group_name")
    private String groupName;

    @TableField("group_avatar")
    private String groupAvatar;

    @TableField("owner_id")
    private Long ownerId;
}