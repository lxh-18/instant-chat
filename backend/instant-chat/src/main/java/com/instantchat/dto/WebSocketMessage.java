package com.instantchat.dto;

import lombok.Data;

@Data
public class WebSocketMessage {
    private String fromUserId;
    private Integer type; // 1: private, 2: group
    private Long toUserId;
    private Long toGroupId;
    private String content;
    private Integer messageType; // 1: text, 2: image, 3: audio
    private String filePath;
}