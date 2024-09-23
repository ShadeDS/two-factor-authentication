package com.nulianov.twofactorauthentication.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nulianov.twofactorauthentication.config.TwoFactorAuthProperties;
import com.nulianov.twofactorauthentication.model.CodeData;
import com.nulianov.twofactorauthentication.model.VerificationResult;
import com.nulianov.twofactorauthentication.model.VerificationStatus;
import com.nulianov.twofactorauthentication.service.CodeVerificationService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CodeVerificationServiceImplTest {

  private CodeVerificationService codeVerificationService;

  private final String code = "123456";

  @BeforeEach
  void setUp() {
    TwoFactorAuthProperties properties = mock(TwoFactorAuthProperties.class);
    when(properties.getCodeValidityDuration()).thenReturn(300);
    when(properties.getMaxAttempts()).thenReturn(3);
    codeVerificationService = new CodeVerificationServiceImpl(properties);
  }

  @Test
  void verify_ShouldReturnSuccess_WhenCodeIsCorrectAndNotExpired() {
    CodeData codeData = new CodeData(code, Instant.now().getEpochSecond(), 0);

    VerificationResult result = codeVerificationService.verify(codeData, code);

    assertEquals(VerificationStatus.SUCCESS, result.status());
  }

  @Test
  void verify_ShouldReturnCodeExpired_WhenCodeIsExpired() {
    long creationTime = Instant.now().minusSeconds(301).getEpochSecond(); // Code expired
    CodeData codeData = new CodeData(code, creationTime, 0);

    VerificationResult result = codeVerificationService.verify(codeData, code);

    assertEquals(VerificationStatus.CODE_EXPIRED, result.status());
  }

  @Test
  void verify_ShouldReturnMaxAttemptsExceeded_WhenAttemptsExceeded() {
    CodeData codeData = new CodeData(code, Instant.now().getEpochSecond(), 3);

    VerificationResult result = codeVerificationService.verify(codeData, code);

    assertEquals(VerificationStatus.MAX_ATTEMPTS_EXCEEDED, result.status());
  }

  @Test
  void verify_ShouldReturnInvalidCode_WhenCodeIsIncorrect() {
    CodeData codeData = new CodeData(code, Instant.now().getEpochSecond(), 0);

    VerificationResult result = codeVerificationService.verify(codeData, "654321");

    assertEquals(VerificationStatus.INVALID_CODE, result.status());
  }
}