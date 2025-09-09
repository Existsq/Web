package com.bmstu.lab.exception;

public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }
}
