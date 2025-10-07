package com.bmstu.lab.exception;

import com.bmstu.lab.calculate.cpi.exception.DraftNotFoundException;
import com.bmstu.lab.calculate.cpi.exception.InvalidDraftException;
import com.bmstu.lab.calculate.cpi.exception.UnauthorizedDraftAccessException;
import com.bmstu.lab.category.exception.CategoryAlreadyExistException;
import com.bmstu.lab.category.exception.CategoryNotFoundException;
import com.bmstu.lab.user.exception.DuplicateUserException;
import com.bmstu.lab.user.exception.InvalidCredentialsException;
import com.bmstu.lab.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalAdviceController {

  @ExceptionHandler(UserNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleUserNotFound(UserNotFoundException e, HttpServletRequest request) {
    log.error("Пользователь не найден: {}", e.getMessage());

    return ErrorResponse.builder()
        .timestamp(Timestamp.valueOf(LocalDateTime.now()))
        .status(String.valueOf(HttpStatus.NOT_FOUND.value()))
        .error("Пользователь не найден")
        .message(e.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(CategoryAlreadyExistException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponse handleCategoryAlreadyExist(
      CategoryAlreadyExistException e, HttpServletRequest request) {
    log.error("Попытка добавления существующей категории: {}", e.getMessage());

    return ErrorResponse.builder()
        .timestamp(Timestamp.valueOf(LocalDateTime.now()))
        .status(String.valueOf(HttpStatus.CONFLICT.value()))
        .error("Категория уже существует")
        .message(e.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(CategoryNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleCategoryNotFound(
      CategoryNotFoundException e, HttpServletRequest request) {
    log.error("Категория не найдена: {}", e.getMessage());

    return ErrorResponse.builder()
        .timestamp(Timestamp.valueOf(LocalDateTime.now()))
        .status(String.valueOf(HttpStatus.CONFLICT.value()))
        .error("Категория не найдена")
        .message(e.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(DraftNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleDraftNotFound(DraftNotFoundException e, HttpServletRequest request) {
    log.error("Черновик не найден: {}", e.getMessage());

    return ErrorResponse.builder()
        .timestamp(Timestamp.valueOf(LocalDateTime.now()))
        .status(String.valueOf(HttpStatus.CONFLICT.value()))
        .error("Черновик не найден")
        .message(e.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(InvalidDraftException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleInvalidDraft(InvalidDraftException e, HttpServletRequest request) {
    log.error("Передано невалидное содержание: {}", e.getMessage());

    return ErrorResponse.builder()
        .timestamp(Timestamp.valueOf(LocalDateTime.now()))
        .status(String.valueOf(HttpStatus.BAD_REQUEST.value()))
        .error("Передано невалидное содержание")
        .message(e.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(UnauthorizedDraftAccessException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ErrorResponse handleUnauthorizedDraftAccess(
      UnauthorizedDraftAccessException e, HttpServletRequest request) {
    log.error("Попытка модификации заявки пользователем без права модерации: {}", e.getMessage());

    return ErrorResponse.builder()
        .timestamp(Timestamp.valueOf(LocalDateTime.now()))
        .status(String.valueOf(HttpStatus.UNAUTHORIZED.value()))
        .error("Попытка модификации заявки пользователем без права модерации")
        .message(e.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ErrorResponse handleInvalidCredentials(
      InvalidCredentialsException e, HttpServletRequest request) {

    log.warn("Попытка входа с неверными данными: {}", e.getMessage());

    return ErrorResponse.builder()
        .timestamp(Timestamp.valueOf(LocalDateTime.now()))
        .status(String.valueOf(HttpStatus.UNAUTHORIZED.value()))
        .error("Неверный логин или пароль")
        .message(e.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(DuplicateUserException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponse handleDuplicateUser(DuplicateUserException e, HttpServletRequest request) {

    log.warn(
        "Попытка регистрации с уже использованными регистрационными данными: {}", e.getMessage());

    return ErrorResponse.builder()
        .timestamp(Timestamp.valueOf(LocalDateTime.now()))
        .status(String.valueOf(HttpStatus.CONFLICT.value()))
        .error("Пользователь с такими данными уже существует")
        .message(e.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest request) {
    log.error("Необработанная ошибка: {}", ex.getMessage(), ex);

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(Timestamp.valueOf(LocalDateTime.now()))
            .status(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .error("Internal Server Error")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining(", "));

    log.warn("Ошибка валидации: {}", message);

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(Timestamp.valueOf(LocalDateTime.now()))
            .status(String.valueOf(HttpStatus.BAD_REQUEST.value()))
            .error("Validation Error")
            .message(message)
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.badRequest().body(error);
  }
}
