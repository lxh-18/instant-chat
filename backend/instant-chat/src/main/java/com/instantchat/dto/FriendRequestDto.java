package com.instantchat.dto;

import lombok.Data;

@Data
public class FriendRequestDto {
    private Long id;
    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;
    private String fromAvatar;
    private String message;
    private Integer status;
    private String createdAt;
    private Boolean isSent; // true=我发送的请求, false=收到的请求
    private Boolean isRead;
}