package com.bmstu.lab.infrastructure.security.handler;

import com.bmstu.lab.infrastructure.security.jwt.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtService jwtService;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_OK);

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserDetails userDetails)) {
      response.sendError(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid authentication principal");
      return;
    }

    String token = jwtService.generateToken(userDetails);

    Cookie jwtCookie = new Cookie("jwt_token", token);
    jwtCookie.setHttpOnly(true);
    jwtCookie.setSecure(false);
    jwtCookie.setPath("/");
    jwtCookie.setMaxAge(24 * 60 * 60);

    response.addCookie(jwtCookie);

    response.getWriter().write("{\"message\": \"Login successful\"}");
  }
}
