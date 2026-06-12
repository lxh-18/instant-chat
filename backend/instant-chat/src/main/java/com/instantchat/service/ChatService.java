package com.instantchat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.instantchat.dto.ChatMessageDto;
import com.instantchat.entity.*;
import com.instantchat.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final PrivateMessageMapper privateMessageMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final UserMapper userMapper;
    private final ChatGroupMapper chatGroupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public List<ChatMessageDto> getPrivateMessages(Long userId, Long friendId, int page, int size) {
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(PrivateMessage::getFromUserId, userId).eq(PrivateMessage::getToUserId, friendId)
                .or(w2 -> w2.eq(PrivateMessage::getFromUserId, friendId).eq(PrivateMessage::getToUserId, userId)))
                .orderByDesc(PrivateMessage::getCreatedAt)
                .last("LIMIT " + (page - 1) * size + ", " + size);

        List<PrivateMessage> messages = privateMessageMapper.selectList(wrapper);
        List<ChatMessageDto> result = new ArrayList<>();
        for (PrivateMessage msg : messages) {
            User fromUser = userMapper.selectById(msg.getFromUserId());
            ChatMessageDto dto = new ChatMessageDto();
            dto.setId(msg.getId());
            dto.setFromUserId(msg.getFromUserId());
            dto.setFromUsername(fromUser != null ? fromUser.getUsername() : "");
            dto.setFromNickname(fromUser != null ? fromUser.getNickname() : "");
            dto.setFromAvatar(fromUser != null ? fromUser.getAvatar() : "");
            dto.setToUserId(msg.getToUserId());
            dto.setContent(msg.getContent());
            dto.setMessageType(msg.getMessageType());
            dto.setFilePath(msg.getFilePath());
            dto.setCreatedAt(msg.getCreatedAt().toString());
            dto.setIsRead(msg.getIsRead());
            result.add(dto);
        }

        // 反转列表，让新消息在下面（按时间升序排列）
        Collections.reverse(result);

        return result;
    }

    public List<ChatMessageDto> getGroupMessages(Long groupId, int page, int size) {
        LambdaQueryWrapper<GroupMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessage::getGroupId, groupId)
                .orderByDesc(GroupMessage::getCreatedAt)
                .last("LIMIT " + (page - 1) * size + ", " + size);

        List<GroupMessage> messages = groupMessageMapper.selectList(wrapper);
        List<ChatMessageDto> result = new ArrayList<>();
        for (GroupMessage msg : messages) {
            User fromUser = userMapper.selectById(msg.getFromUserId());
            ChatMessageDto dto = new ChatMessageDto();
            dto.setId(msg.getId());
            dto.setGroupId(msg.getGroupId());
            dto.setFromUserId(msg.getFromUserId());
            dto.setFromUsername(fromUser != null ? fromUser.getUsername() : "");
            dto.setFromNickname(fromUser != null ? fromUser.getNickname() : "");
            dto.setFromAvatar(fromUser != null ? fromUser.getAvatar() : "");
            dto.setContent(msg.getContent());
            dto.setMessageType(msg.getMessageType());
            dto.setFilePath(msg.getFilePath());
            dto.setCreatedAt(msg.getCreatedAt().toString());
            result.add(dto);
        }

        // 反转列表，让新消息在下面（按时间升序排列）
        Collections.reverse(result);

        return result;
    }

    public List<ChatGroup> getUserGroups(Long userId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getUserId, userId);
        List<GroupMember> memberships = groupMemberMapper.selectList(wrapper);

        List<ChatGroup> groups = new ArrayList<>();
        for (GroupMember m : memberships) {
            ChatGroup g = chatGroupMapper.selectById(m.getGroupId());
            if (g != null) {
                groups.add(g);
            }
        }
        return groups;
    }

    public ChatGroup createGroup(Long userId, String groupName) {
        ChatGroup group = new ChatGroup();
        group.setGroupName(groupName);
        group.setOwnerId(userId);
        chatGroupMapper.insert(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        groupMemberMapper.insert(member);

        return group;
    }

    public void joinGroup(Long userId, Long groupId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        if (groupMemberMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("已在群中");
        }

        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群不存在");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        groupMemberMapper.insert(member);
    }

    public void inviteUserToGroup(Long userId, Long groupId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        if (groupMemberMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该用户已在群中");
        }

        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群不存在");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        groupMemberMapper.insert(member);

        // 通过WebSocket通知被邀请的用户
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "groupInvite");
        notification.put("groupId", groupId);
        notification.put("groupName", group.getGroupName());
        notification.put("message", "你被邀请加入了群：" + group.getGroupName());

        messagingTemplate.convertAndSend("/topic/user/" + userId + "/groups", notification);
    }

    public void leaveGroup(Long userId, Long groupId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group != null && group.getOwnerId().equals(userId)) {
            throw new RuntimeException("群主不能退出群聊");
        }

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        groupMemberMapper.delete(wrapper);
    }

    public List<Long> getGroupMemberIds(Long groupId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId);
        List<GroupMember> members = groupMemberMapper.selectList(wrapper);
        List<Long> ids = new ArrayList<>();
        for (GroupMember m : members) {
            ids.add(m.getUserId());
        }
        return ids;
    }

    public List<ChatGroup> searchGroups(String keyword) {
        LambdaQueryWrapper<ChatGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(ChatGroup::getGroupName, keyword);
        return chatGroupMapper.selectList(wrapper);
    }

    public List<ChatGroup> getAllGroups() {
        return chatGroupMapper.selectList(null);
    }

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public ChatGroup getGroupById(Long id) {
        return chatGroupMapper.selectById(id);
    }

    @Transactional
    public void deletePrivateMessages(Long userId, Long friendId) {
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getFromUserId, userId).eq(PrivateMessage::getToUserId, friendId);
        privateMessageMapper.delete(wrapper);

        LambdaQueryWrapper<PrivateMessage> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(PrivateMessage::getFromUserId, friendId).eq(PrivateMessage::getToUserId, userId);
        privateMessageMapper.delete(wrapper2);
    }

    @Transactional
    public PrivateMessage sendPrivateMessage(Long userId, Long friendId, String content, Integer messageType, String filePath) {
        PrivateMessage message = new PrivateMessage();
        message.setFromUserId(userId);
        message.setToUserId(friendId);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : 1);
        message.setFilePath(filePath);
        message.setIsRead(0);
        privateMessageMapper.insert(message);

        // 通过 WebSocket 广播消息给接收者和发送者
        User fromUser = userMapper.selectById(userId);
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setFromUserId(message.getFromUserId());
        dto.setFromUsername(fromUser != null ? fromUser.getUsername() : "");
        dto.setFromNickname(fromUser != null ? fromUser.getNickname() : "");
        dto.setFromAvatar(fromUser != null ? fromUser.getAvatar() : "");
        dto.setToUserId(message.getToUserId());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setFilePath(message.getFilePath());
        dto.setCreatedAt(message.getCreatedAt().toString());
        dto.setIsRead(0);

        messagingTemplate.convertAndSend("/topic/private/" + friendId, dto);
        messagingTemplate.convertAndSend("/topic/private/" + userId, dto);

        return message;
    }

    @Transactional
    public void sendGroupMessage(Long userId, Long groupId, String content, Integer messageType, String filePath) {
        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setFromUserId(userId);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : 1);
        message.setFilePath(filePath);
        groupMessageMapper.insert(message);

        // 通过 WebSocket 广播群消息
        User fromUser = userMapper.selectById(userId);
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setGroupId(message.getGroupId());
        dto.setFromUserId(message.getFromUserId());
        dto.setFromUsername(fromUser != null ? fromUser.getUsername() : "");
        dto.setFromNickname(fromUser != null ? fromUser.getNickname() : "");
        dto.setFromAvatar(fromUser != null ? fromUser.getAvatar() : "");
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setFilePath(message.getFilePath());
        dto.setCreatedAt(message.getCreatedAt().toString());

        messagingTemplate.convertAndSend("/topic/group/" + groupId, dto);
    }

    public List<ChatMessageDto> searchPrivateMessages(Long userId, Long friendId, String keyword) {
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(PrivateMessage::getFromUserId, userId).eq(PrivateMessage::getToUserId, friendId)
                .or(w2 -> w2.eq(PrivateMessage::getFromUserId, friendId).eq(PrivateMessage::getToUserId, userId)))
                .like(PrivateMessage::getContent, keyword)
                .orderByDesc(PrivateMessage::getCreatedAt)
                .last("LIMIT 100");

        List<PrivateMessage> messages = privateMessageMapper.selectList(wrapper);
        List<ChatMessageDto> result = new ArrayList<>();
        for (PrivateMessage msg : messages) {
            User fromUser = userMapper.selectById(msg.getFromUserId());
            ChatMessageDto dto = new ChatMessageDto();
            dto.setId(msg.getId());
            dto.setFromUserId(msg.getFromUserId());
            dto.setFromUsername(fromUser != null ? fromUser.getUsername() : "");
            dto.setFromNickname(fromUser != null ? fromUser.getNickname() : "");
            dto.setFromAvatar(fromUser != null ? fromUser.getAvatar() : "");
            dto.setToUserId(msg.getToUserId());
            dto.setContent(msg.getContent());
            dto.setMessageType(msg.getMessageType());
            dto.setFilePath(msg.getFilePath());
            dto.setCreatedAt(msg.getCreatedAt().toString());
            dto.setIsRead(msg.getIsRead());
            result.add(dto);
        }
        Collections.reverse(result);
        return result;
    }

    public List<ChatMessageDto> searchGroupMessages(Long groupId, String keyword) {
        LambdaQueryWrapper<GroupMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessage::getGroupId, groupId)
                .like(GroupMessage::getContent, keyword)
                .orderByDesc(GroupMessage::getCreatedAt)
                .last("LIMIT 100");

        List<GroupMessage> messages = groupMessageMapper.selectList(wrapper);
        List<ChatMessageDto> result = new ArrayList<>();
        for (GroupMessage msg : messages) {
            User fromUser = userMapper.selectById(msg.getFromUserId());
            ChatMessageDto dto = new ChatMessageDto();
            dto.setId(msg.getId());
            dto.setGroupId(msg.getGroupId());
            dto.setFromUserId(msg.getFromUserId());
            dto.setFromUsername(fromUser != null ? fromUser.getUsername() : "");
            dto.setFromNickname(fromUser != null ? fromUser.getNickname() : "");
            dto.setFromAvatar(fromUser != null ? fromUser.getAvatar() : "");
            dto.setContent(msg.getContent());
            dto.setMessageType(msg.getMessageType());
            dto.setFilePath(msg.getFilePath());
            dto.setCreatedAt(msg.getCreatedAt().toString());
            result.add(dto);
        }
        Collections.reverse(result);
        return result;
    }

    public int getPrivateUnreadCount(Long userId, Long friendId) {
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getFromUserId, friendId)
               .eq(PrivateMessage::getToUserId, userId)
               .eq(PrivateMessage::getIsRead, 0);
        return Math.toIntExact(privateMessageMapper.selectCount(wrapper));
    }

    public List<Map<String, Object>> getPrivateUnreadSummary(Long userId) {
        // Get all unread private messages sent to this user, grouped by sender
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getToUserId, userId)
               .eq(PrivateMessage::getIsRead, 0);
        List<PrivateMessage> unreadMessages = privateMessageMapper.selectList(wrapper);

        // Group by fromUserId and count
        java.util.Map<Long, Integer> unreadMap = new java.util.HashMap<>();
        for (PrivateMessage msg : unreadMessages) {
            unreadMap.merge(msg.getFromUserId(), 1, Integer::sum);
        }

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<Long, Integer> entry : unreadMap.entrySet()) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("friendId", entry.getKey());
            item.put("unreadCount", entry.getValue());
            result.add(item);
        }
        return result;
    }

    public int getGroupUnreadCount(Long userId, Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        if (groupMemberMapper.selectCount(memberWrapper) == 0) {
            return 0;
        }
        LambdaQueryWrapper<GroupMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessage::getGroupId, groupId)
               .eq(GroupMessage::getIsRead, 0);
        return Math.toIntExact(groupMessageMapper.selectCount(wrapper));
    }

    public void markPrivateMessagesRead(Long userId, Long friendId) {
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getFromUserId, friendId)
               .eq(PrivateMessage::getToUserId, userId)
               .eq(PrivateMessage::getIsRead, 0);
        PrivateMessage update = new PrivateMessage();
        update.setIsRead(1);
        privateMessageMapper.update(update, wrapper);
    }

    public void markGroupMessagesRead(Long userId, Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        if (groupMemberMapper.selectCount(memberWrapper) == 0) {
            return;
        }
        LambdaQueryWrapper<GroupMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessage::getGroupId, groupId)
               .eq(GroupMessage::getIsRead, 0);
        GroupMessage update = new GroupMessage();
        update.setIsRead(1);
        groupMessageMapper.update(update, wrapper);
    }
}