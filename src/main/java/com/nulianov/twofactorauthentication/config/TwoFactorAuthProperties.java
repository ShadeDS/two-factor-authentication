package com.nulianov.twofactorauthentication.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "two-factor")
public class TwoFactorAuthProperties {

  private int codeValidityDuration;
  private int resendInterval;
  private int maxAttempts;
}
