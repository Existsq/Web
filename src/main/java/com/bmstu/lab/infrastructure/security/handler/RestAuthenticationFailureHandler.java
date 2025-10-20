package com.bmstu.lab.infrastructure.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write("{\"error\": \"" + exception.getMessage() + "\"}");
  }
}
