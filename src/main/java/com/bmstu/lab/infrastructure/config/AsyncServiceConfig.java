package com.bmstu.lab.infrastructure.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AsyncServiceConfig {
  @Value("${async.service.url}")
  private String url;

  @Value("${async.service.token}")
  private String token;
}

