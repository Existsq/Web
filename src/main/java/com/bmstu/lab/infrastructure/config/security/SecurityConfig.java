package com.bmstu.lab.infrastructure.config.security;

import com.bmstu.lab.infrastructure.security.filter.JsonUsernamePasswordAuthFilter;
import com.bmstu.lab.infrastructure.security.filter.JwtAuthenticationFilter;
import com.bmstu.lab.infrastructure.security.filter.JwtBlacklistFilter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
                auth.requestMatchers(
                        "/api/users/login",
                        "/api/categories",
                        "/api/users/register",
                        "api/calculate-cpi/draft-info",
                        "/swagger-ui/**",
                        "v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories/*")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/calculate-cpi/*/async-data")
                    .permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/calculate-cpi/*/async-result")
                    .permitAll()
                    .requestMatchers("/error", "/")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .requestCache(RequestCacheConfigurer::disable)
        .addFilterBefore(jwtBlacklistFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(
            jsonUsernamePasswordAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        List.of("https://localhost:3000", "tauri://localhost", "https://tauri.localhost", "https://existsq.github.io"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
