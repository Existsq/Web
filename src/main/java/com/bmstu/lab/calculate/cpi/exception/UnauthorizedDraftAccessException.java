package com.bmstu.lab.calculate.cpi.exception;

/** Исключение выбрасывается, когда пользователь пытается формировать чужой черновик. */
public class UnauthorizedDraftAccessException extends RuntimeException {
  public UnauthorizedDraftAccessException(String message) {
    super(message);
  }
}
