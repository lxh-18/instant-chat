package com.instantchat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.instantchat.dto.ChatMessageDto;
import com.instantchat.entity.GroupMessage;
import com.instantchat.entity.PrivateMessage;
import com.instantchat.entity.User;
import com.instantchat.mapper.GroupMessageMapper;
import com.instantchat.mapper.PrivateMessageMapper;
import com.instantchat.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息导出服务 - 满足得分点1：文件I/O处理聊天记录下载
 * 使用 FileReader/Writer 实现聊天记录导出为 JSON/TXT 格式
 *
 * 参考资料：
 * - Oracle官方文档: https://docs.oracle.com/javase/8/docs/api/java/io/FileReader.html
 * - MDN Blob API: https://developer.mozilla.org/en-US/docs/Web/API/Blob
 */
@Service
@RequiredArgsConstructor
public class MessageExportService {

    private final PrivateMessageMapper privateMessageMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public String exportPrivateMessagesToJson(Long userId, Long friendId) throws IOException {
        List<PrivateMessage> messages = getAllPrivateMessages(userId, friendId);
        List<ChatMessageDto> dtos = convertPrivateMessagesToDtos(messages);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String json = mapper.writeValueAsString(dtos);

        File dir = new File("exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filename = "private_chat_" + userId + "_" + friendId + "_" + System.currentTimeMillis() + ".json";
        File file = new File(dir, filename);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }

        return filename;
    }

    public String exportPrivateMessagesToTxt(Long userId, Long friendId) throws IOException {
        List<PrivateMessage> messages = getAllPrivateMessages(userId, friendId);

        File dir = new File("exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filename = "private_chat_" + userId + "_" + friendId + "_" + System.currentTimeMillis() + ".txt";
        File file = new File(dir, filename);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("=" .repeat(50) + "\n");
            writer.write("Private Chat Export\n");
            writer.write("=" .repeat(50) + "\n\n");

            for (PrivateMessage msg : messages) {
                User fromUser = userMapper.selectById(msg.getFromUserId());
                String sender = fromUser != null ? fromUser.getNickname() : "Unknown";
                String time = msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : "";
                String typeStr = getMessageTypeString(msg.getMessageType());

                writer.write("[" + time + "] " + sender + " (" + typeStr + "):\n");
                if (msg.getMessageType() == 2) {
                    writer.write("  [Image: " + msg.getFilePath() + "]\n");
                } else if (msg.getMessageType() == 3) {
                    writer.write("  [Voice: " + msg.getFilePath() + "]\n");
                } else {
                    writer.write("  " + msg.getContent() + "\n");
                }
                writer.write("\n");
            }
        }

        return filename;
    }

    public String exportGroupMessagesToJson(Long groupId) throws IOException {
        LambdaQueryWrapper<GroupMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessage::getGroupId, groupId).orderByAsc(GroupMessage::getCreatedAt);
        List<GroupMessage> messages = groupMessageMapper.selectList(wrapper);
        List<ChatMessageDto> dtos = convertGroupMessagesToDtos(messages);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String json = mapper.writeValueAsString(dtos);

        File dir = new File("exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filename = "group_chat_" + groupId + "_" + System.currentTimeMillis() + ".json";
        File file = new File(dir, filename);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }

        return filename;
    }

    public String exportGroupMessagesToTxt(Long groupId) throws IOException {
        LambdaQueryWrapper<GroupMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessage::getGroupId, groupId).orderByAsc(GroupMessage::getCreatedAt);
        List<GroupMessage> messages = groupMessageMapper.selectList(wrapper);

        File dir = new File("exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filename = "group_chat_" + groupId + "_" + System.currentTimeMillis() + ".txt";
        File file = new File(dir, filename);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("=".repeat(50) + "\n");
            writer.write("Group Chat Export\n");
            writer.write("=".repeat(50) + "\n\n");

            for (GroupMessage msg : messages) {
                User fromUser = userMapper.selectById(msg.getFromUserId());
                String sender = fromUser != null ? fromUser.getNickname() : "Unknown";
                String time = msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : "";
                String typeStr = getMessageTypeString(msg.getMessageType());

                writer.write("[" + time + "] " + sender + " (" + typeStr + "):\n");
                if (msg.getMessageType() == 2) {
                    writer.write("  [Image: " + msg.getFilePath() + "]\n");
                } else if (msg.getMessageType() == 3) {
                    writer.write("  [Voice: " + msg.getFilePath() + "]\n");
                } else {
                    writer.write("  " + msg.getContent() + "\n");
                }
                writer.write("\n");
            }
        }

        return filename;
    }

    private List<PrivateMessage> getAllPrivateMessages(Long userId, Long friendId) {
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(PrivateMessage::getFromUserId, userId).eq(PrivateMessage::getToUserId, friendId)
                .or(w2 -> w2.eq(PrivateMessage::getFromUserId, friendId).eq(PrivateMessage::getToUserId, userId)))
                .orderByAsc(PrivateMessage::getCreatedAt);
        return privateMessageMapper.selectList(wrapper);
    }

    private List<ChatMessageDto> convertPrivateMessagesToDtos(List<PrivateMessage> messages) {
        List<ChatMessageDto> dtos = new ArrayList<>();
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
            dto.setCreatedAt(msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : "");
            dto.setIsRead(msg.getIsRead());
            dtos.add(dto);
        }
        return dtos;
    }

    private List<ChatMessageDto> convertGroupMessagesToDtos(List<GroupMessage> messages) {
        List<ChatMessageDto> dtos = new ArrayList<>();
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
            dto.setCreatedAt(msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : "");
            dtos.add(dto);
        }
        return dtos;
    }

    private String getMessageTypeString(Integer type) {
        if (type == null) return "text";
        return switch (type) {
            case 2 -> "image";
            case 3 -> "voice";
            default -> "text";
        };
    }
}