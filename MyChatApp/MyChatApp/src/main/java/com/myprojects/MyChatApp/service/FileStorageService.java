package com.myprojects.MyChatApp.service;

import com.myprojects.MyChatApp.model.FileInfo;
import com.myprojects.MyChatApp.repository.FileInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private FileInfoRepository fileInfoRepository;

    public FileInfo storeFile(MultipartFile file, String uploader, String receiver) throws IOException {

        // Ensure the upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Create a unique file path
        String filePath = uploadPath.toString() + File.separator + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Save the file to the specified directory
        Files.write(Paths.get(filePath), file.getBytes());

        // Save file metadata to the database
        FileInfo fileInfo = new FileInfo();

        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileType(file.getContentType());
        fileInfo.setUploadTime(LocalDateTime.now());
        fileInfo.setUploader(uploader);
        fileInfo.setReceiver(receiver);
        fileInfo.setFilePath(filePath);

        return fileInfoRepository.save(fileInfo);
    }

    public Optional<FileInfo> getFile(Long fileId) {
        return fileInfoRepository.findById(fileId);
    }
}
