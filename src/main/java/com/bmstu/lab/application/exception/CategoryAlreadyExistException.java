package com.bmstu.lab.application.exception;

public class CategoryAlreadyExistException extends RuntimeException {

  public CategoryAlreadyExistException(String message) {
    super(message);
  }
}
