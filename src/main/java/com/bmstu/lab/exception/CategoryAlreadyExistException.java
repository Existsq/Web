package com.bmstu.lab.exception;

public class CategoryAlreadyExistException extends RuntimeException {

  public CategoryAlreadyExistException(String message) {
    super(message);
  }
}
