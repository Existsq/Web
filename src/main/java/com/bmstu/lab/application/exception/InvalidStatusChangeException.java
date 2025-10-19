package com.bmstu.lab.application.exception;

public class InvalidStatusChangeException extends RuntimeException {

  public InvalidStatusChangeException(String message) {
    super(message);
  }
}
