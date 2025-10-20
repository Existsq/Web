package com.bmstu.lab.infrastructure.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieExtractor {

  public static String extractValueFromCookies(HttpServletRequest request, String cookieKey) {
    if (request.getCookies() == null) return null;

    for (Cookie cookie : request.getCookies()) {
      if (cookieKey.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
