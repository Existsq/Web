package com.bmstu.lab.application.exception;

public class InvalidDraftException extends RuntimeException {
  public InvalidDraftException(String message) {
    super(message);
  }
}
