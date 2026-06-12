package com.instantchat.websocket;

import com.instantchat.dto.ChatMessageDto;
import com.instantchat.dto.WebSocketMessage;
import com.instantchat.entity.PrivateMessage;
import com.instantchat.entity.GroupMessage;
import com.instantchat.mapper.GroupMessageMapper;
import com.instantchat.mapper.PrivateMessageMapper;
import com.instantchat.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final PrivateMessageMapper privateMessageMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final UserMapper userMapper;

    @MessageMapping("/chat/private")
    public void handlePrivateMessage(WebSocketMessage message) {
        PrivateMessage pm = new PrivateMessage();
        pm.setFromUserId(Long.parseLong(message.getFromUserId()));
        pm.setToUserId(message.getToUserId());
        pm.setContent(message.getContent());
        pm.setMessageType(message.getMessageType());
        pm.setFilePath(message.getFilePath());
        pm.setIsRead(0);
        privateMessageMapper.insert(pm);

        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(pm.getId());
        dto.setFromUserId(pm.getFromUserId());
        dto.setToUserId(pm.getToUserId());
        dto.setContent(pm.getContent());
        dto.setMessageType(pm.getMessageType());
        dto.setFilePath(pm.getFilePath());
        dto.setCreatedAt(pm.getCreatedAt().toString());
        dto.setIsRead(0);

        messagingTemplate.convertAndSend("/topic/private/" + message.getToUserId(), dto);
        messagingTemplate.convertAndSend("/topic/private/" + pm.getFromUserId(), dto);
    }

    @MessageMapping("/chat/group")
    public void handleGroupMessage(WebSocketMessage message) {
        GroupMessage gm = new GroupMessage();
        gm.setGroupId(message.getToGroupId());
        gm.setFromUserId(Long.parseLong(message.getFromUserId()));
        gm.setContent(message.getContent());
        gm.setMessageType(message.getMessageType());
        gm.setFilePath(message.getFilePath());
        groupMessageMapper.insert(gm);

        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(gm.getId());
        dto.setGroupId(gm.getGroupId());
        dto.setFromUserId(gm.getFromUserId());
        dto.setContent(gm.getContent());
        dto.setMessageType(gm.getMessageType());
        dto.setFilePath(gm.getFilePath());
        dto.setCreatedAt(gm.getCreatedAt().toString());

        messagingTemplate.convertAndSend("/topic/group/" + message.getToGroupId(), dto);
    }
}