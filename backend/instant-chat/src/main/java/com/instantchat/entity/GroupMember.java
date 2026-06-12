package com.instantchat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("group_members")
public class GroupMember {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("group_id")
    private Long groupId;

    @TableField("user_id")
    private Long userId;

    @TableField("joined_at")
    private java.time.LocalDateTime joinedAt;
}