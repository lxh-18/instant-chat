package com.instantchat.controller;

import com.instantchat.dto.ApiResponse;
import com.instantchat.service.MessageExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageExportController {

    private final MessageExportService messageExportService;

    @GetMapping("/private/{friendId}/export/{format}")
    public ResponseEntity<byte[]> exportPrivateMessages(
            @PathVariable Long friendId,
            @PathVariable String format,
            Authentication authentication) throws Exception {
        Long userId = (Long) authentication.getPrincipal();

        String filename;
        if ("json".equalsIgnoreCase(format)) {
            filename = messageExportService.exportPrivateMessagesToJson(userId, friendId);
        } else if ("txt".equalsIgnoreCase(format)) {
            filename = messageExportService.exportPrivateMessagesToTxt(userId, friendId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        File file = new File("exports/" + filename);
        Path path = file.toPath();
        byte[] content = Files.readAllBytes(path);

        String contentType = "json".equalsIgnoreCase(format) ? "application/json" : "text/plain";
        String downloadFilename = "private_chat_" + friendId + "." + format.toLowerCase();

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"")
                .body(content);
    }

    @GetMapping("/group/{groupId}/export/{format}")
    public ResponseEntity<byte[]> exportGroupMessages(
            @PathVariable Long groupId,
            @PathVariable String format,
            Authentication authentication) throws Exception {

        String filename;
        if ("json".equalsIgnoreCase(format)) {
            filename = messageExportService.exportGroupMessagesToJson(groupId);
        } else if ("txt".equalsIgnoreCase(format)) {
            filename = messageExportService.exportGroupMessagesToTxt(groupId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        File file = new File("exports/" + filename);
        Path path = file.toPath();
        byte[] content = Files.readAllBytes(path);

        String contentType = "json".equalsIgnoreCase(format) ? "application/json" : "text/plain";
        String downloadFilename = "group_chat_" + groupId + "." + format.toLowerCase();

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"")
                .body(content);
    }
}