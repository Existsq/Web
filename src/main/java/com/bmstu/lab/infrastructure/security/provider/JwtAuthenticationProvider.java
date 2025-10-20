package com.bmstu.lab.infrastructure.security.provider;

import com.bmstu.lab.infrastructure.security.jwt.JwtAuthenticationToken;
import com.bmstu.lab.infrastructure.security.jwt.JwtService;
import com.bmstu.lab.presentation.response.JwtToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
public record JwtAuthenticationProvider(JwtService jwtService) implements AuthenticationProvider {

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    log.trace("Starting JWT token validation");

    JwtToken token = (JwtToken) authentication.getCredentials();
    if (!jwtService.isTokenValid(token)) {
      log.warn("JWT token validation failed: invalid token");
      throw new BadCredentialsException("Invalid token");
    }

    UserDetails user = jwtService.extractUserDetails(token);
    log.info("JWT token valid, authenticated user: {}", user.getUsername());

    JwtAuthenticationToken jwtAuthentication =
        new JwtAuthenticationToken(user, token, user.getAuthorities());
    log.debug("JwtAuthenticationToken created for user: {}", user.getUsername());

    log.debug(jwtAuthentication.getAuthorities().toString());

    return jwtAuthentication;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return JwtAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
