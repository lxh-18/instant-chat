package com.instantchat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_invitations")
public class GroupInvitation extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("group_id")
    private Long groupId;

    @TableField("inviter_user_id")
    private Long inviterUserId;

    @TableField("invitee_user_id")
    private Long inviteeUserId;

    @TableField("message")
    private String message;

    @TableField("status")
    private Integer status; // 0: pending, 1: accepted, 2: rejected
}