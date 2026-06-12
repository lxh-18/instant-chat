package com.instantchat.controller;

import com.instantchat.dto.*;
import com.instantchat.entity.FriendGroup;
import com.instantchat.entity.FriendRequest;
import com.instantchat.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/friends/groups")
    public ApiResponse<List<FriendGroup>> getFriendGroups(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(friendService.getFriendGroups(userId));
    }

    @PostMapping("/friends/groups")
    public ApiResponse<FriendGroup> createGroup(@RequestBody CreateGroupRequest request,
                                                 Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(friendService.createGroup(userId, request.getGroupName()));
    }

    @PutMapping("/friends/groups/{id}")
    public ApiResponse<FriendGroup> updateGroup(@PathVariable Long id,
                                                 @RequestBody UpdateGroupRequest request,
                                                 Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(friendService.updateGroup(id, userId, request.getGroupName()));
    }

    @DeleteMapping("/friends/groups/{id}")
    public ApiResponse<Void> deleteGroup(@PathVariable Long id,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendService.deleteGroup(id, userId);
        return ApiResponse.success("分组已删除", null);
    }

    @GetMapping("/friends")
    public ApiResponse<List<FriendDto>> getFriends(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(friendService.getFriends(userId));
    }

    @PostMapping("/friends/move")
    public ApiResponse<Void> moveFriend(@RequestBody MoveFriendRequest request,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendService.moveFriend(userId, request.getFriendId(), request.getNewGroupId(), request.getGroupName());
        return ApiResponse.success("移动成功", null);
    }

    @DeleteMapping("/friends/{friendId}")
    public ApiResponse<Void> deleteFriend(@PathVariable Long friendId,
                                           @RequestParam(required = false, defaultValue = "false") Boolean deleteMessages,
                                           Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendService.deleteFriend(userId, friendId, deleteMessages);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/friend-requests")
    public ApiResponse<Void> sendFriendRequest(@RequestBody SendFriendRequestDto request,
                                                Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendService.sendFriendRequest(userId, request.getToUserId(), request.getMessage());
        return ApiResponse.success("请求已发送", null);
    }

    @GetMapping("/friend-requests")
    public ApiResponse<List<FriendRequestDto>> getFriendRequests(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(friendService.getFriendRequests(userId));
    }

    @GetMapping("/friend-requests/sent")
    public ApiResponse<List<FriendRequestDto>> getSentFriendRequests(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(friendService.getSentFriendRequests(userId));
    }

    @PutMapping("/friend-requests/{id}")
    public ApiResponse<Void> handleFriendRequest(@PathVariable Long id,
                                                  @RequestBody HandleFriendRequestDto request,
                                                  Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendService.handleFriendRequest(id, userId, request.getAccept());
        return ApiResponse.success(request.getAccept() ? "已同意" : "已拒绝", null);
    }

    @PostMapping("/friend-requests/{id}/resend")
    public ApiResponse<Void> resendFriendRequest(@PathVariable Long id,
                                                  Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendService.resendFriendRequest(id, userId);
        return ApiResponse.success("请求已重新发送", null);
    }

    @PostMapping("/friend-requests/mark-read")
    public ApiResponse<Void> markFriendRequestsAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendService.markFriendRequestsAsRead(userId);
        return ApiResponse.success("已标记已读", null);
    }
}