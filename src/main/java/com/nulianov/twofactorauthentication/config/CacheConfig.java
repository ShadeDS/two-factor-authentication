package com.nulianov.twofactorauthentication.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nulianov.twofactorauthentication.model.CodeData;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

  private final TwoFactorAuthProperties properties;

  public CacheConfig(TwoFactorAuthProperties properties) {
    this.properties = properties;
  }

  @Bean
  public Cache<String, CodeData> codeCache() {
    return Caffeine.newBuilder()
        .expireAfterWrite(properties.getCodeValidityDuration(), TimeUnit.SECONDS)
        .build();
  }
}
