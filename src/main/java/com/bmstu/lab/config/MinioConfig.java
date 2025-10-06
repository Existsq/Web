package com.bmstu.lab.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

  @Value("${minio.key.access}")
  private String accessKey;

  @Value("${minio.key.secret}")
  private String secretKey;

  @Value("${minio.endpoint}")
  private String endpoint;

  @Value("${minio.port}")
  private int port;

  @Value("${minio.secure}")
  private boolean isSecure;

  @Bean
  public MinioClient minioClient() {
    return MinioClient.builder()
        .endpoint(endpoint, port, isSecure)
        .credentials(accessKey, secretKey)
        .build();
  }
}
