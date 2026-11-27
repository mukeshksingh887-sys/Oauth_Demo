package com.OAuth.demo.service;

import com.OAuth.demo.config.S3Config;
import com.OAuth.demo.entity.FileMetadata;
import com.OAuth.demo.repository.FileMetadataRepository;
import org.apache.catalina.Store;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private  final S3Client s3Client;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("aws.s3.bucket-name")
    private  String bucketName;

    public S3Service(S3Client s3Client, FileMetadataRepository fileMetadataRepository) {
        this.s3Client = s3Client;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    // Upload file to S3 and save metadata in RDS
    public String uploadFile(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();

        String key = UUID.randomUUID() + "_" + originalFileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(
                        file.getInputStream(),
                        file.getSize()
                )
        );

        // sava in a DB
        FileMetadata metadata = new FileMetadata();

        metadata.setFileName(originalFileName);
        metadata.setS3Key(key);
        metadata.setContentType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setBucketName(bucketName);


        fileMetadataRepository.save(metadata);

        return key;
    }

    // Download file
    public byte[] downloadFile(String key) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> stream =
                     s3Client.getObject(request)) {

            return stream.readAllBytes();

        } catch (IOException e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }

    // Delete file from S3 and RDS
    // Delete file from S3 and RDS
    public void deleteFile(Long id) {

        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(metadata.getS3Key())
                .build();

        s3Client.deleteObject(request);

         fileMetadataRepository.delete(metadata);
    }

    // Fetch file metadata from RDS
    public FileMetadata getMetadata(Long id) {

        return  fileMetadataRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));
    }
}
