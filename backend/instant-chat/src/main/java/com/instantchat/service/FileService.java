package com.instantchat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    public String uploadAudio(MultipartFile file) throws IOException {
        return uploadFile(file, "audio");
    }

    public String uploadImage(MultipartFile file) throws IOException {
        return uploadFile(file, "image");
    }

    private String uploadFile(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + extension;
        String dirPath = uploadPath + "/" + subDir;

        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Path filePath = Paths.get(dirPath, filename);
        Files.write(filePath, file.getBytes());

        return subDir + "/" + filename;
    }

    public byte[] getFile(String filePath) throws IOException {
        Path path = Paths.get(uploadPath, filePath);
        return Files.readAllBytes(path);
    }
}