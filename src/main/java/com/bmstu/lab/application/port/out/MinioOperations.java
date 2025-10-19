package com.bmstu.lab.application.port.out;

import org.springframework.web.multipart.MultipartFile;

public interface MinioOperations {

  String uploadFile(MultipartFile file);

  void deleteFile(String fileName);
}
