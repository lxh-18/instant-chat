package com.instantchat.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private Long id;
    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;
    private String fromAvatar;
    private Long toUserId;
    private Long groupId;
    private String content;
    private Integer messageType;
    private String filePath;
    private String createdAt;
    private Integer isRead;
}