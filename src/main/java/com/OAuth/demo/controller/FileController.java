package com.OAuth.demo.controller;


import com.OAuth.demo.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {


    private final S3Service s3Service;


    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    // Upload
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        String key = s3Service.uploadFile(file);

        return ResponseEntity.ok(
                "File uploaded successfully. S3 Key: " + key
        );
    }

    // Download
    @GetMapping("/download/{key}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String key) {

        byte[] file = s3Service.downloadFile(key);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(
            @PathVariable Long id) {

        s3Service.deleteFile(id);

        return ResponseEntity.ok(
                "File deleted successfully");
    }
}
