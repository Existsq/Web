package com.bmstu.lab.minio;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MinioTemplate implements MinioOperations {

  private final MinioClient minioClient;

  private final String bucketName;

  public MinioTemplate(MinioClient minioClient, @Value("${minio.bucket.name}") String bucketName) {
    this.minioClient = minioClient;
    this.bucketName = bucketName;
  }

  public String uploadFile(MultipartFile file) {
    String fileName = UUID.randomUUID().toString() + ".jpg";

    try (InputStream inputStream = file.getInputStream()) {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                  inputStream, file.getSize(), -1)
              .contentType(file.getContentType())
              .build());
    } catch (Exception e) {
      throw new RuntimeException("Failed to upload file to MinIO", e);
    }

    return fileName;
  }

  public void deleteFile(String fileName) {
    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete file from MinIO", e);
    }
  }
}
