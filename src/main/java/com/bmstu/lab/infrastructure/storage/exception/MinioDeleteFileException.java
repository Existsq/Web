package com.bmstu.lab.infrastructure.storage.exception;

public class MinioDeleteFileException extends RuntimeException {

  public MinioDeleteFileException(String message) {
    super(message);
  }
}
