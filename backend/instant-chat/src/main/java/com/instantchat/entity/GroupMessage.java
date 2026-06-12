package com.instantchat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_messages")
public class GroupMessage extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("group_id")
    private Long groupId;

    @TableField("from_user_id")
    private Long fromUserId;

    @TableField("content")
    private String content;

    @TableField("message_type")
    private Integer messageType; // 1: text, 2: image, 3: audio

    @TableField("file_path")
    private String filePath;

    @TableField("is_read")
    private Integer isRead = 0; // 0: unread, 1: read
}