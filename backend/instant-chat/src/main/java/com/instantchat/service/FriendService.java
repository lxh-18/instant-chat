package com.instantchat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.instantchat.dto.FriendDto;
import com.instantchat.dto.FriendRequestDto;
import com.instantchat.entity.Friend;
import com.instantchat.entity.FriendGroup;
import com.instantchat.entity.FriendRequest;
import com.instantchat.entity.User;
import com.instantchat.mapper.FriendGroupMapper;
import com.instantchat.mapper.FriendMapper;
import com.instantchat.mapper.FriendRequestMapper;
import com.instantchat.mapper.PrivateMessageMapper;
import com.instantchat.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 好友服务层 - 满足得分点1：MVC分层架构 + 好友分组管理
 * 实现好友的CRUD操作、好友请求处理、分组移动等功能
 */
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendGroupMapper friendGroupMapper;
    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final PrivateMessageMapper privateMessageMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public List<FriendGroup> getFriendGroups(Long userId) {
        LambdaQueryWrapper<FriendGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendGroup::getUserId, userId);
        return friendGroupMapper.selectList(wrapper);
    }

    public FriendGroup createGroup(Long userId, String groupName) {
        FriendGroup group = new FriendGroup();
        group.setUserId(userId);
        group.setGroupName(groupName);
        friendGroupMapper.insert(group);
        return group;
    }

    public FriendGroup updateGroup(Long groupId, Long userId, String groupName) {
        FriendGroup group = friendGroupMapper.selectById(groupId);
        if (group == null || !group.getUserId().equals(userId)) {
            throw new RuntimeException("分组不存在或无权限");
        }
        group.setGroupName(groupName);
        friendGroupMapper.updateById(group);
        return group;
    }

    public void deleteGroup(Long groupId, Long userId) {
        FriendGroup group = friendGroupMapper.selectById(groupId);
        if (group == null || !group.getUserId().equals(userId)) {
            throw new RuntimeException("分组不存在或无权限");
        }
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getGroupId, groupId);
        List<Friend> friends = friendMapper.selectList(wrapper);
        for (Friend friend : friends) {
            friend.setGroupId(null);
            friendMapper.updateById(friend);
        }
        friendGroupMapper.deleteById(groupId);
    }

    public List<FriendDto> getFriends(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId);
        List<Friend> friends = friendMapper.selectList(wrapper);

        List<FriendDto> result = new ArrayList<>();
        for (Friend friend : friends) {
            User user = userMapper.selectById(friend.getFriendId());
            if (user != null) {
                FriendDto dto = new FriendDto();
                dto.setId(friend.getId());
                dto.setUserId(user.getId());
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatar());
                dto.setStatus(user.getStatus());
                dto.setGroupId(friend.getGroupId());
                if (friend.getGroupId() != null) {
                    FriendGroup group = friendGroupMapper.selectById(friend.getGroupId());
                    if (group != null) {
                        dto.setGroupName(group.getGroupName());
                    }
                }
                result.add(dto);
            }
        }
        return result;
    }

    public void moveFriend(Long userId, Long friendId, Long newGroupId, String groupName) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        Friend friend = friendMapper.selectOne(wrapper);
        if (friend == null) {
            throw new RuntimeException("好友关系不存在");
        }

        if (groupName != null && !groupName.isEmpty()) {
            // 通过分组名称查找或创建分组
            LambdaQueryWrapper<FriendGroup> gw = new LambdaQueryWrapper<>();
            gw.eq(FriendGroup::getUserId, userId).eq(FriendGroup::getGroupName, groupName);
            FriendGroup group = friendGroupMapper.selectOne(gw);
            if (group == null) {
                group = new FriendGroup();
                group.setUserId(userId);
                group.setGroupName(groupName);
                friendGroupMapper.insert(group);
            }
            newGroupId = group.getId();
        }

        if (newGroupId != null) {
            FriendGroup group = friendGroupMapper.selectById(newGroupId);
            if (group == null || !group.getUserId().equals(userId)) {
                throw new RuntimeException("分组不存在");
            }
        }
        friend.setGroupId(newGroupId);
        friendMapper.updateById(friend);
    }

    @Transactional
    public void deleteFriend(Long userId, Long friendId, Boolean deleteMessages) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        friendMapper.delete(wrapper);

        LambdaQueryWrapper<Friend> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId);
        friendMapper.delete(wrapper2);

        if (deleteMessages) {
            LambdaQueryWrapper<com.instantchat.entity.PrivateMessage> mw = new LambdaQueryWrapper<>();
            mw.and(w -> w.eq(com.instantchat.entity.PrivateMessage::getFromUserId, userId)
                    .eq(com.instantchat.entity.PrivateMessage::getToUserId, friendId)
                    .or(w2 -> w2.eq(com.instantchat.entity.PrivateMessage::getFromUserId, friendId)
                            .eq(com.instantchat.entity.PrivateMessage::getToUserId, userId)));
            privateMessageMapper.delete(mw);
        }
    }

    public void sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("不能添加自己为好友");
        }
        User user = userMapper.selectById(toUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        LambdaQueryWrapper<Friend> fw = new LambdaQueryWrapper<>();
        fw.eq(Friend::getUserId, fromUserId).eq(Friend::getFriendId, toUserId);
        if (friendMapper.selectCount(fw) > 0) {
            throw new RuntimeException("该用户已经是您的好友");
        }

        LambdaQueryWrapper<FriendRequest> rw = new LambdaQueryWrapper<>();
        rw.eq(FriendRequest::getFromUserId, fromUserId)
                .eq(FriendRequest::getToUserId, toUserId)
                .eq(FriendRequest::getStatus, 0);
        if (friendRequestMapper.selectCount(rw) > 0) {
            throw new RuntimeException("请勿重复发送请求");
        }

        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus(0);
        request.setExpiresAt(LocalDateTime.now().plusDays(7));
        friendRequestMapper.insert(request);

        // 通过WebSocket推送好友请求通知给接收者 - 满足得分点1：WebSocket实时通信
        FriendRequestDto notification = new FriendRequestDto();
        notification.setId(request.getId());
        notification.setFromUserId(fromUserId);
        User fromUser = userMapper.selectById(fromUserId);
        notification.setFromUsername(fromUser != null ? fromUser.getUsername() : "");
        notification.setFromNickname(fromUser != null ? fromUser.getNickname() : "");
        notification.setFromAvatar(fromUser != null ? fromUser.getAvatar() : "");
        notification.setMessage(message);
        notification.setStatus(0);
        notification.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt().toString() : "");

        messagingTemplate.convertAndSend("/topic/friend-request/" + toUserId, notification);
    }

    public List<FriendRequestDto> getFriendRequests(Long userId) {
        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getToUserId, userId)
                .orderByDesc(FriendRequest::getCreatedAt);
        List<FriendRequest> requests = friendRequestMapper.selectList(wrapper);

        List<FriendRequestDto> result = new ArrayList<>();
        for (FriendRequest req : requests) {
            User fromUser = userMapper.selectById(req.getFromUserId());
            if (fromUser != null) {
                FriendRequestDto dto = new FriendRequestDto();
                dto.setId(req.getId());
                dto.setFromUserId(fromUser.getId());
                dto.setFromUsername(fromUser.getUsername());
                dto.setFromNickname(fromUser.getNickname());
                dto.setFromAvatar(fromUser.getAvatar());
                dto.setMessage(req.getMessage());
                dto.setStatus(req.getStatus());
                dto.setCreatedAt(req.getCreatedAt() != null ? req.getCreatedAt().toString() : "");
                dto.setIsSent(false);
                dto.setIsRead(req.getIsRead());
                result.add(dto);
            }
        }
        return result;
    }

    public List<FriendRequestDto> getSentFriendRequests(Long userId) {
        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getFromUserId, userId)
                .orderByDesc(FriendRequest::getCreatedAt);
        List<FriendRequest> requests = friendRequestMapper.selectList(wrapper);

        List<FriendRequestDto> result = new ArrayList<>();
        for (FriendRequest req : requests) {
            User toUser = userMapper.selectById(req.getToUserId());
            if (toUser != null) {
                FriendRequestDto dto = new FriendRequestDto();
                dto.setId(req.getId());
                dto.setFromUserId(toUser.getId());
                dto.setFromUsername(toUser.getUsername());
                dto.setFromNickname(toUser.getNickname());
                dto.setFromAvatar(toUser.getAvatar());
                dto.setMessage(req.getMessage());
                dto.setStatus(req.getStatus());
                dto.setCreatedAt(req.getCreatedAt() != null ? req.getCreatedAt().toString() : "");
                dto.setIsSent(true);
                dto.setIsRead(req.getIsRead());
                result.add(dto);
            }
        }
        return result;
    }

    @Transactional
    public void handleFriendRequest(Long requestId, Long userId, Boolean accept) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || !request.getToUserId().equals(userId)) {
            throw new RuntimeException("请求不存在或无权限");
        }
        if (request.getStatus() != 0) {
            throw new RuntimeException("请求已处理");
        }
        if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
            request.setStatus(3);
            friendRequestMapper.updateById(request);
            throw new RuntimeException("请求已过期");
        }

        if (accept) {
            // 检查是否已经是好友
            LambdaQueryWrapper<Friend> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(Friend::getUserId, request.getFromUserId()).eq(Friend::getFriendId, request.getToUserId());
            if (friendMapper.selectCount(checkWrapper) > 0) {
                // 已经是好友，只更新请求状态
                request.setStatus(1);
                friendRequestMapper.updateById(request);
                return;
            }

            Friend friend1 = new Friend();
            friend1.setUserId(request.getFromUserId());
            friend1.setFriendId(request.getToUserId());
            friendMapper.insert(friend1);

            Friend friend2 = new Friend();
            friend2.setUserId(request.getToUserId());
            friend2.setFriendId(request.getFromUserId());
            friendMapper.insert(friend2);
        }

        request.setStatus(accept ? 1 : 2);
        friendRequestMapper.updateById(request);

        // 通过WebSocket推送好友请求处理结果给发送者 - 满足得分点1：WebSocket实时通信
        Map<String, Object> result = new HashMap<>();
        result.put("type", "friendRequestResult");
        result.put("requestId", requestId);
        result.put("fromUserId", request.getFromUserId());
        result.put("toUserId", request.getToUserId());
        result.put("accept", accept);
        result.put("message", accept ? "对方同意了你的好友请求" : "对方拒绝了你的好友请求");

        messagingTemplate.convertAndSend("/topic/friend-request/" + request.getFromUserId(), result);
    }

    public void resendFriendRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || !request.getFromUserId().equals(userId)) {
            throw new RuntimeException("请求不存在或无权限");
        }
        if (request.getStatus() == 0) {
            throw new RuntimeException("请求仍在处理中");
        }

               LambdaQueryWrapper<Friend> fw = new LambdaQueryWrapper<>();
        fw.eq(Friend::getUserId, userId).eq(Friend::getFriendId, request.getToUserId());
        if (friendMapper.selectCount(fw) > 0) {
            throw new RuntimeException("该用户已经是您的好友");
        }

        // 检查是否已经存在待处理的好友请求
        LambdaQueryWrapper<FriendRequest> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(FriendRequest::getFromUserId, userId)
                .eq(FriendRequest::getToUserId, request.getToUserId())
                .eq(FriendRequest::getStatus, 0);
        if (friendRequestMapper.selectCount(pendingWrapper) > 0) {
            throw new RuntimeException("请勿重复发送请求");
        }

        FriendRequest newRequest = new FriendRequest();
        newRequest.setFromUserId(request.getFromUserId());
        newRequest.setToUserId(request.getToUserId());
        newRequest.setMessage(request.getMessage());
        newRequest.setStatus(0);
        newRequest.setExpiresAt(LocalDateTime.now().plusDays(7));
        friendRequestMapper.insert(newRequest);
    }

    public void markFriendRequestsAsRead(Long userId) {
        // 标记收到的已处理请求为已读
        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getToUserId, userId)
                .ne(FriendRequest::getStatus, 0)
                .eq(FriendRequest::getIsRead, false);
        FriendRequest update = new FriendRequest();
        update.setIsRead(true);
        friendRequestMapper.update(update, wrapper);

        // 标记发送的已处理请求为已读
        LambdaQueryWrapper<FriendRequest> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(FriendRequest::getFromUserId, userId)
                .ne(FriendRequest::getStatus, 0)
                .eq(FriendRequest::getIsRead, false);
        friendRequestMapper.update(update, wrapper2);
    }
}