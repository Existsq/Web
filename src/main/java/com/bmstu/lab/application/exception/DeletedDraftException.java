package com.bmstu.lab.application.exception;

public class DeletedDraftException extends RuntimeException {
  public DeletedDraftException(String message) {
    super(message);
  }
}
