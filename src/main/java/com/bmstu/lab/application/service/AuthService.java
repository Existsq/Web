package com.bmstu.lab.application.service;

import com.bmstu.lab.application.dto.UserCredentialsDTO;
import com.bmstu.lab.application.exception.UserAlreadyExistsException;
import com.bmstu.lab.infrastructure.persistence.entity.User;
import com.bmstu.lab.infrastructure.persistence.repository.UserRepository;
import com.bmstu.lab.infrastructure.security.jwt.JwtService;
import com.bmstu.lab.presentation.response.JwtToken;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RedisTemplate<String, String> redisTemplate;

  @Value("${jwt.blacklist.ttl-hours:2}")
  private long blacklistTtl;

  public JwtToken register(UserCredentialsDTO request) {
    userRepository
        .findByUsername(request.getUsername())
        .ifPresent(
            u -> {
              throw new UserAlreadyExistsException("Пользователь с таким никнеймом уже существует");
            });

    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    userRepository.save(user);

    return new JwtToken(jwtService.generateToken(user));
  }

  public void logout(JwtToken jwtToken) {
    if (jwtToken == null || jwtToken.value() == null) {
      throw new IllegalArgumentException("JWT токен отсутствует в запросе");
    }

    String key = "blacklist:" + jwtToken.value();
    redisTemplate.opsForValue().set(key, "true", Duration.ofHours(blacklistTtl));
  }
}
