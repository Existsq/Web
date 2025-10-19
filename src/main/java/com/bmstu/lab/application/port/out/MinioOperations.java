package com.bmstu.lab.application.port.out;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MinioOperations {

  UUID uploadFile(MultipartFile file);

  void deleteFile(String fileName);
}
