package com.bmstu.lab.infrastructure.security.filter;

import com.bmstu.lab.infrastructure.security.jwt.CookieExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class JwtBlacklistFilter extends OncePerRequestFilter {

  private final String cookieKey;

  private final RedisTemplate<String, String> redisTemplate;

  public JwtBlacklistFilter(
      @Value("${jwt.cookie.key}") String cookieKey, RedisTemplate<String, String> redisTemplate) {
    this.cookieKey = cookieKey;
    this.redisTemplate = redisTemplate;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    log.trace("Сработал блеклист фильтр");
    String tokenValue = CookieExtractor.extractValueFromCookies(request, cookieKey);

    if (tokenValue != null && redisTemplate.hasKey("blacklist:" + tokenValue)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Ваша сессия недействительная");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
