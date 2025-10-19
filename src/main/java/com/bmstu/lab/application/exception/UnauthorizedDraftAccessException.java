package com.bmstu.lab.application.exception;

public class UnauthorizedDraftAccessException extends RuntimeException {
  public UnauthorizedDraftAccessException(String message) {
    super(message);
  }
}
