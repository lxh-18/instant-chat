package com.instantchat.controller;

import com.instantchat.dto.ApiResponse;
import com.instantchat.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/audio")
    public ApiResponse<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileService.uploadAudio(file);
            return ApiResponse.success("上传成功", filePath);
        } catch (IOException e) {
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/image")
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileService.uploadImage(file);
            return ApiResponse.success("上传成功", filePath);
        } catch (IOException e) {
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/{type}/{filename}")
    public ResponseEntity<byte[]> getFile(@PathVariable String type, @PathVariable String filename) {
        try {
            String filePath = type + "/" + filename;
            byte[] content = fileService.getFile(filePath);

            String contentType = "audio".equals(type) ? "audio/mpeg" : "image/jpeg";
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}