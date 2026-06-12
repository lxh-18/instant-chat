package com.instantchat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("friend_requests")
public class FriendRequest extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("from_user_id")
    private Long fromUserId;

    @TableField("to_user_id")
    private Long toUserId;

    @TableField("message")
    private String message;

    @TableField("status")
    private Integer status; // 0: pending, 1: accepted, 2: rejected, 3: expired

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("is_read")
    private Boolean isRead = false;
}