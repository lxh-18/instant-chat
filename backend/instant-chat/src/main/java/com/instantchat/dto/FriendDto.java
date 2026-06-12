package com.instantchat.dto;

import lombok.Data;

@Data
public class FriendDto {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
    private Long groupId;
    private String groupName;
}