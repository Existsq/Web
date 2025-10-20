package com.bmstu.lab.presentation.controller;

import com.bmstu.lab.application.dto.UserCredentialsDTO;
import com.bmstu.lab.application.dto.UserDTO;
import com.bmstu.lab.application.service.AuthService;
import com.bmstu.lab.application.service.UserService;
import com.bmstu.lab.infrastructure.security.jwt.JwtAuthenticationToken;
import com.bmstu.lab.presentation.response.JwtToken;
import jakarta.servlet.http.HttpServletResponse;
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
public class UserController {

  private final UserService userService;
  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<JwtToken> register(@RequestBody UserCredentialsDTO dto) {
    JwtToken token = authService.register(dto);

    ResponseCookie cookie =
        ResponseCookie.from("jwt_token", token.value())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofHours(2))
            .sameSite("Strict")
            .build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(token);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(Authentication authentication, HttpServletResponse response) {
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      JwtToken token = jwtAuth.getToken();

      authService.logout(token);

      ResponseCookie deleteCookie =
          ResponseCookie.from("jwt_token", "")
              .httpOnly(true)
              .secure(true)
              .path("/")
              .maxAge(0)
              .sameSite("Strict")
              .build();

      response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

      return ResponseEntity.ok().build();
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @GetMapping("/me")
  public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
  }

  @PutMapping("/me")
  public ResponseEntity<UserDTO> updateUser(
      @AuthenticationPrincipal UserDetails userDetails, @RequestBody UserCredentialsDTO dto) {
    return ResponseEntity.ok(userService.updateUser(userDetails.getUsername(), dto));
  }
}
