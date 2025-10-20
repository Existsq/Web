package com.bmstu.lab.infrastructure.config.security;

import com.bmstu.lab.infrastructure.security.filter.JsonUsernamePasswordAuthFilter;
import com.bmstu.lab.infrastructure.security.handler.RestAuthenticationFailureHandler;
import com.bmstu.lab.infrastructure.security.handler.RestAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
public class FilterConfig {

  @Bean
  public JsonUsernamePasswordAuthFilter jsonUsernamePasswordAuthFilter(
      AuthenticationManager authenticationManager,
      RestAuthenticationFailureHandler failureHandler,
      RestAuthenticationSuccessHandler successHandler) {
    var filter = new JsonUsernamePasswordAuthFilter(authenticationManager);
    filter.setAuthenticationFailureHandler(failureHandler);
    filter.setAuthenticationSuccessHandler(successHandler);

    return filter;
  }
}
