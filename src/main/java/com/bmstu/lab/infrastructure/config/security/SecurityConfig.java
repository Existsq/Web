package com.bmstu.lab.infrastructure.config.security;

import com.bmstu.lab.infrastructure.security.filter.JsonUsernamePasswordAuthFilter;
import com.bmstu.lab.infrastructure.security.filter.JwtAuthenticationFilter;
import com.bmstu.lab.infrastructure.security.filter.JwtBlacklistFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JsonUsernamePasswordAuthFilter jsonUsernamePasswordAuthFilter,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtBlacklistFilter jwtBlacklistFilter)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/users/login", "/api/users/register")
                    .permitAll()
                    .requestMatchers("/error", "/")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .requestCache(RequestCacheConfigurer::disable)
        .addFilterBefore(jwtBlacklistFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(
            jsonUsernamePasswordAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
