package com.instantchat.controller;

import com.instantchat.dto.*;
import com.instantchat.entity.ChatGroup;
import com.instantchat.entity.PrivateMessage;
import com.instantchat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/messages/private/{friendId}")
    public ApiResponse<List<ChatMessageDto>> getPrivateMessages(
            @PathVariable Long friendId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(chatService.getPrivateMessages(userId, friendId, page, size));
    }

    @GetMapping("/messages/private/{friendId}/search")
    public ApiResponse<List<ChatMessageDto>> searchPrivateMessages(
            @PathVariable Long friendId,
            @RequestParam String keyword,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(chatService.searchPrivateMessages(userId, friendId, keyword));
    }

    @PostMapping("/messages/private/{friendId}")
    public ApiResponse<PrivateMessage> sendPrivateMessage(
            @PathVariable Long friendId,
            @RequestBody ChatMessageDto request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        PrivateMessage message = chatService.sendPrivateMessage(userId, friendId, request.getContent(), request.getMessageType(), request.getFilePath());
        return ApiResponse.success(message);
    }

    @GetMapping("/messages/group/{groupId}")
    public ApiResponse<List<ChatMessageDto>> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        return ApiResponse.success(chatService.getGroupMessages(groupId, page, size));
    }

    @GetMapping("/messages/group/{groupId}/search")
    public ApiResponse<List<ChatMessageDto>> searchGroupMessages(
            @PathVariable Long groupId,
            @RequestParam String keyword,
            Authentication authentication) {
        return ApiResponse.success(chatService.searchGroupMessages(groupId, keyword));
    }

    @PostMapping("/messages/group/{groupId}")
    public ApiResponse<Void> sendGroupMessage(
            @PathVariable Long groupId,
            @RequestBody ChatMessageDto request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        chatService.sendGroupMessage(userId, groupId, request.getContent(), request.getMessageType(), request.getFilePath());
        return ApiResponse.success("发送成功", null);
    }

    @GetMapping("/groups")
    public ApiResponse<List<ChatGroup>> getUserGroups(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(chatService.getUserGroups(userId));
    }

    @PostMapping("/groups")
    public ApiResponse<ChatGroup> createGroup(@RequestBody CreateChatGroupRequest request,
                                               Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(chatService.createGroup(userId, request.getGroupName()));
    }

    @PostMapping("/groups/{id}/join")
    public ApiResponse<Void> joinGroup(@PathVariable Long id,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        chatService.joinGroup(userId, id);
        return ApiResponse.success("加入成功", null);
    }

    @PostMapping("/groups/{id}/invite")
    public ApiResponse<Void> inviteUserToGroup(@PathVariable Long id,
                                               @RequestBody Map<String, Long> request,
                                               Authentication authentication) {
        Long inviterId = (Long) authentication.getPrincipal();
        Long userId = request.get("userId");
        if (userId == null) {
            return ApiResponse.error("缺少用户ID");
        }
        chatService.inviteUserToGroup(userId, id);
        return ApiResponse.success("邀请成功", null);
    }

    @GetMapping("/groups/{id}/members")
    public ApiResponse<List<Long>> getGroupMembers(@PathVariable Long id) {
        return ApiResponse.success(chatService.getGroupMemberIds(id));
    }

    @GetMapping("/groups/search")
    public ApiResponse<List<ChatGroup>> searchGroups(@RequestParam String keyword) {
        return ApiResponse.success(chatService.searchGroups(keyword));
    }

    @GetMapping("/groups/all")
    public ApiResponse<List<ChatGroup>> getAllGroups() {
        return ApiResponse.success(chatService.getAllGroups());
    }

    @GetMapping("/groups/{id}")
    public ApiResponse<ChatGroup> getGroupById(@PathVariable Long id) {
        ChatGroup group = chatService.getGroupById(id);
        if (group == null) {
            return ApiResponse.error("群聊不存在");
        }
        return ApiResponse.success(group);
    }

    @DeleteMapping("/groups/{id}/leave")
    public ApiResponse<Void> leaveGroup(@PathVariable Long id,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        chatService.leaveGroup(userId, id);
        return ApiResponse.success("退出成功", null);
    }

    @GetMapping("/messages/unread-summary")
    public ApiResponse<Map<String, Object>> getUnreadSummary(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<ChatGroup> groups = chatService.getUserGroups(userId);
        List<Map<String, Object>> friendUnreads = chatService.getPrivateUnreadSummary(userId);
        List<Map<String, Object>> groupUnreads = new java.util.ArrayList<>();
        for (ChatGroup g : groups) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("groupId", g.getId());
            item.put("unreadCount", chatService.getGroupUnreadCount(userId, g.getId()));
            groupUnreads.add(item);
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("friendUnreads", friendUnreads);
        result.put("groupUnreads", groupUnreads);
        return ApiResponse.success(result);
    }

    @PostMapping("/messages/private/{friendId}/mark-read")
    public ApiResponse<Void> markPrivateRead(@PathVariable Long friendId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        chatService.markPrivateMessagesRead(userId, friendId);
        return ApiResponse.success("已标记已读", null);
    }

    @PostMapping("/messages/group/{groupId}/mark-read")
    public ApiResponse<Void> markGroupRead(@PathVariable Long groupId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        chatService.markGroupMessagesRead(userId, groupId);
        return ApiResponse.success("已标记已读", null);
    }
}