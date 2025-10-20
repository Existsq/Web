package com.bmstu.lab.application.exception;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super("Пользователь не найден");
  }
}
