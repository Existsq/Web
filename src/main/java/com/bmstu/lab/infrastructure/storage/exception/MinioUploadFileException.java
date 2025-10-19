package com.bmstu.lab.infrastructure.storage.exception;

public class MinioUploadFileException extends RuntimeException {
  public MinioUploadFileException(String message) {
    super(message);
  }
}
