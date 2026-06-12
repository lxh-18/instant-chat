package com.instantchat.dto;

import lombok.Data;

@Data
public class SendFriendRequestDto {
    private Long toUserId;
    private String message;
}