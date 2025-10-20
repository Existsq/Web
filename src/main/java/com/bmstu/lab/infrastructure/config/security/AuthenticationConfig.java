package com.bmstu.lab.infrastructure.config.security;

import com.bmstu.lab.infrastructure.security.jwt.JwtService;
import com.bmstu.lab.infrastructure.security.provider.JwtAuthenticationProvider;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      PasswordEncoder passwordEncoder,
      UserDetailsService userDetailsService,
      JwtService jwtService) {

    DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider(userDetailsService);
    daoProvider.setPasswordEncoder(passwordEncoder);

    JwtAuthenticationProvider jwtProvider = new JwtAuthenticationProvider(jwtService);

    return new ProviderManager(List.of(daoProvider, jwtProvider));
  }
}
