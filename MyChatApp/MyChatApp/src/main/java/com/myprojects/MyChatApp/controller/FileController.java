package com.myprojects.MyChatApp.controller;

import com.myprojects.MyChatApp.model.FileInfo;
import com.myprojects.MyChatApp.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@RestController
@CrossOrigin
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam("uploader") String uploader,
                                               @RequestParam("receiver") String receiver) {
        try {
            FileInfo fileInfo = fileStorageService.storeFile(file, uploader, receiver);
            return ResponseEntity.ok(fileInfo);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long fileId, @RequestParam("receiver") String receiver) {
        FileInfo fileInfo = fileStorageService.getFile(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id " + fileId));

        if (!fileInfo.getReceiver().equals(receiver)) {
            return ResponseEntity.status(403).build(); // Forbidden if the receiver doesn't match
        }

        try {
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(Paths.get(fileInfo.getFilePath())));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileInfo.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getFileName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
}
