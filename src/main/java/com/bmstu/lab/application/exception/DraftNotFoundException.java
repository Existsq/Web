package com.bmstu.lab.application.exception;

public class DraftNotFoundException extends RuntimeException {
  public DraftNotFoundException(String message) {
    super(message);
  }
}
