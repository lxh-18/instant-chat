package com.instantchat.dto;

import lombok.Data;

@Data
public class GroupInvitationDto {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long inviterUserId;
    private String inviterUsername;
    private String inviterNickname;
    private String inviterAvatar;
    private Long inviteeUserId;
    private String message;
    private Integer status;
    private String createdAt;
}