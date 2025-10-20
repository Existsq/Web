package com.bmstu.lab.infrastructure.security.jwt;

import com.bmstu.lab.presentation.response.JwtToken;
import java.util.Collection;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

  private final JwtToken token;
  private final Object principal;

  public JwtAuthenticationToken(JwtToken token) {
    super(null);
    this.token = token;
    this.principal = null;
    setAuthenticated(false);
  }

  public JwtAuthenticationToken(
      UserDetails principal, JwtToken token, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.token = token;
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return token;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public void eraseCredentials() {
    super.eraseCredentials();
  }
}
