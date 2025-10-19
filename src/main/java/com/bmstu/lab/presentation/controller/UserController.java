package com.bmstu.lab.presentation.controller;

import com.bmstu.lab.application.dto.UserCredentialsDTO;
import com.bmstu.lab.application.dto.UserDTO;
import com.bmstu.lab.application.service.UserService;
import com.bmstu.lab.infrastructure.persistence.entity.User;
import com.bmstu.lab.presentation.response.JwtToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

  @PostMapping("/register")
  public ResponseEntity<UserDTO> register(@RequestBody UserCredentialsDTO dto) {
    return ResponseEntity.ok(userService.register(dto));
  }

  @PostMapping("/login")
  public ResponseEntity<JwtToken> login(@RequestBody UserCredentialsDTO dto) {
    User user = userService.authenticate(dto.getUsername(), dto.getPassword());
    String token = "token";
    return ResponseEntity.ok(new JwtToken(token));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public ResponseEntity<UserDTO> getCurrentUser() {
    String username = "user";
    return ResponseEntity.ok(userService.getCurrentUser(username));
  }

  @PutMapping("/me")
  public ResponseEntity<UserDTO> updateUser(@RequestBody UserCredentialsDTO dto) {
    String username = "user";
    return ResponseEntity.ok(userService.updateUser(username, dto));
  }
}
