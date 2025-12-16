package com.bmstu.lab.presentation.controller;

import com.bmstu.lab.application.dto.UserCredentialsDTO;
import com.bmstu.lab.application.dto.UserDTO;
import com.bmstu.lab.application.service.AuthService;
import com.bmstu.lab.application.service.UserService;
import com.bmstu.lab.infrastructure.persistence.entity.User;
import com.bmstu.lab.infrastructure.security.jwt.JwtAuthenticationToken;
import com.bmstu.lab.presentation.response.JwtToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для регистрации, получения и обновления пользователя")
public class UserController {

  private final UserService userService;
  private final AuthService authService;

  @Operation(
      summary = "Регистрация нового пользователя",
      description = "Регистрирует пользователя и возвращает JWT токен в httpOnly cookie")
  @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован")
  @ApiResponse(responseCode = "400", description = "Ошибка регистрации")
  @PostMapping("/register")
  public ResponseEntity<JwtToken> register(@RequestBody UserCredentialsDTO dto) {
    JwtToken token = authService.register(dto);

    ResponseCookie cookie =
        ResponseCookie.from("jwt_token", token.value())
            .httpOnly(false)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofHours(2))
            .sameSite("None")
            .build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(token);
  }

  @Operation(
      summary = "Вход пользователя",
      description = "Устанавливает JWT токен в cookie, если данные для входа валидны")
  @ApiResponse(responseCode = "200", description = "Пользователь успешно вошел")
  @ApiResponse(responseCode = "401", description = "Указаны неверные данные для входа")
  @PostMapping("/login")
  public void login(@RequestBody UserCredentialsDTO credentialsDTO) {}

  @Operation(
      summary = "Выход пользователя",
      description = "Удаляет JWT токен из cookie, завершает сессию пользователя")
  @ApiResponse(responseCode = "200", description = "Пользователь успешно вышел")
  @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(Authentication authentication, HttpServletResponse response) {
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      JwtToken token = jwtAuth.getToken();

      authService.logout(token);

      ResponseCookie deleteCookie =
          ResponseCookie.from("jwt_token", "")
              .httpOnly(false)
              .secure(true)
              .path("/")
              .maxAge(0)
              .sameSite("None")
              .build();

      response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

      return ResponseEntity.ok().build();
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @Operation(
      summary = "Получить текущего пользователя",
      description = "Возвращает информацию о текущем аутентифицированном пользователе")
  @ApiResponse(responseCode = "200", description = "Информация успешно получена")
  @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
  @GetMapping("/me")
  public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
  }

  @Operation(
      summary = "Обновить текущего пользователя",
      description = "Позволяет пользователю обновить свои данные или модератору изменить данные")
  @ApiResponse(responseCode = "200", description = "Данные пользователя успешно обновлены")
  @ApiResponse(responseCode = "401", description = "Нет прав для обновления пользователя")
  @PutMapping("/me")
  public ResponseEntity<UserDTO> updateUser(
      @AuthenticationPrincipal UserDetails userDetails, @RequestBody UserCredentialsDTO dto) {
    return ResponseEntity.ok(userService.updateUser(userDetails.getUsername(), dto));
  }
  @Operation(summary = "Обновить пароль пользователя", description = "Позвояет установить новый пароль пользователю")
  @ApiResponse(responseCode = "200", description = "Пароль успешно изменен")
  @PostMapping("/change-password")
  public ResponseEntity<String> changePassword(String newPassword, @AuthenticationPrincipal(errorOnInvalidType = true) User user)
      throws UserPrincipalNotFoundException {
      userService.changePassword(newPassword, user);
      return ResponseEntity.ok().body("New password set");
  }
}
