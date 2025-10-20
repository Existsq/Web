package com.bmstu.lab.infrastructure.security.filter;

import com.bmstu.lab.infrastructure.security.jwt.CookieExtractor;
import com.bmstu.lab.infrastructure.security.jwt.JwtAuthenticationToken;
import com.bmstu.lab.presentation.response.JwtToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final AuthenticationManager authenticationManager;

  private final String cookieKey;

  public JwtAuthenticationFilter(
      AuthenticationManager authenticationManager, @Value("${jwt.cookie.key}") String cookieKey) {
    this.authenticationManager = authenticationManager;
    this.cookieKey = cookieKey;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    log.trace("Сработал jwt аутентифицирующий фильтр");

    String extractedToken = CookieExtractor.extractValueFromCookies(request, cookieKey);
    log.info("Полученный токен из куков {}", extractedToken);
    if (extractedToken == null) {
      filterChain.doFilter(request, response);
      return;
    }

    JwtToken token = new JwtToken(extractedToken);
    JwtAuthenticationToken unauthenticatedToken = new JwtAuthenticationToken(token);

    Authentication authentication = authenticationManager.authenticate(unauthenticatedToken);
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    filterChain.doFilter(request, response);
  }
}
