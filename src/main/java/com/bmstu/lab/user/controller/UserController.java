package com.bmstu.lab.user.controller;

import com.bmstu.lab.user.model.dto.AuthResponseDTO;
import com.bmstu.lab.user.model.dto.UserAuthenticationDTO;
import com.bmstu.lab.user.model.dto.UserDTO;
import com.bmstu.lab.user.model.dto.UserRegistrationDTO;
import com.bmstu.lab.user.model.entity.User;
import com.bmstu.lab.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
  public ResponseEntity<UserDTO> register(@RequestBody UserRegistrationDTO dto) {
    return ResponseEntity.ok(userService.register(dto));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponseDTO> login(@RequestBody UserAuthenticationDTO dto) {
    User user = userService.authenticate(dto.getUsername(), dto.getPassword());
    String token = "token";
    return ResponseEntity.ok(new AuthResponseDTO(token));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal String username) {
    return ResponseEntity.ok(userService.getCurrentUser(username));
  }

  @PutMapping("/me")
  public ResponseEntity<UserDTO> updateUser(
      @AuthenticationPrincipal String username, @RequestBody UserRegistrationDTO dto) {
    return ResponseEntity.ok(userService.updateUser(username, dto));
  }
}
