package com.instantchat.dto;

import lombok.Data;

@Data
public class MoveFriendRequest {
    private Long friendId;
    private Long newGroupId;
    private String groupName;  // 支持通过分组名称移动
}