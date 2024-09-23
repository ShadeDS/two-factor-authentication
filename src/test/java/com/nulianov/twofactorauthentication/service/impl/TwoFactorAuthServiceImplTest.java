package com.nulianov.twofactorauthentication.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nulianov.twofactorauthentication.config.TwoFactorAuthProperties;
import com.nulianov.twofactorauthentication.model.CodeData;
import com.nulianov.twofactorauthentication.model.VerificationResult;
import com.nulianov.twofactorauthentication.model.VerificationStatus;
import com.nulianov.twofactorauthentication.repository.CodeRepository;
import com.nulianov.twofactorauthentication.service.CodeVerificationService;
import com.nulianov.twofactorauthentication.service.EmailService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TwoFactorAuthServiceImplTest {

  private EmailService emailService;

  private CodeVerificationService codeVerificationService;

  private CodeRepository codeRepository;

  private TwoFactorAuthProperties properties;

  private TwoFactorAuthServiceImpl twoFactorAuthService;

  private final String email = "test@example.com";
  private final String code = "123456";

  @BeforeEach
  void setUp() {
    emailService = mock(EmailService.class);
    codeVerificationService = mock(CodeVerificationService.class);
    codeRepository = mock(CodeRepository.class);
    properties = mock(TwoFactorAuthProperties.class);

    twoFactorAuthService = new TwoFactorAuthServiceImpl(
        properties,
        emailService,
        codeVerificationService,
        codeRepository
    );

    when(properties.getCodeValidityDuration()).thenReturn(300);
    when(properties.getMaxAttempts()).thenReturn(3);
    when(properties.getResendInterval()).thenReturn(300);
  }

  @Test
  void initiateTwoFactorAuth_ShouldGenerateAndSendCode() {
    twoFactorAuthService.initiateTwoFactorAuth(email);

    verify(codeRepository, times(1)).save(eq(email), any(CodeData.class));
    verify(emailService, times(1)).sendEmail(eq(email), anyString());
  }

  @Test
  void verifyCode_ShouldReturnSuccess_WhenCodeIsValid() {
    CodeData codeData = new CodeData(code, Instant.now().getEpochSecond());
    when(codeRepository.findByEmail(email)).thenReturn(codeData);
    when(codeVerificationService.verify(codeData, code)).thenReturn(new VerificationResult(VerificationStatus.SUCCESS));

    VerificationResult result = twoFactorAuthService.verifyCode(email, code);

    assertEquals(VerificationStatus.SUCCESS, result.status());
    verify(codeRepository, times(1)).deleteByEmail(email);
  }

  @Test
  void verifyCode_ShouldReturnInvalidCode_WhenNoCodeExists() {
    when(codeRepository.findByEmail(email)).thenReturn(null);

    VerificationResult result = twoFactorAuthService.verifyCode(email, code);

    assertEquals(VerificationStatus.INVALID_CODE, result.status());
    verify(codeRepository, never()).deleteByEmail(anyString());
  }

  @Test
  void verifyCode_ShouldHandleInvalidCodeAndIncrementAttempts() {
    CodeData codeData = new CodeData("654321", Instant.now().getEpochSecond());
    when(codeRepository.findByEmail(email)).thenReturn(codeData);
    when(codeVerificationService.verify(codeData, code)).thenReturn(
        new VerificationResult(VerificationStatus.INVALID_CODE));
    when(properties.getMaxAttempts()).thenReturn(3);

    VerificationResult result = twoFactorAuthService.verifyCode(email, code);

    assertEquals(VerificationStatus.INVALID_CODE, result.status());
    verify(codeRepository, times(1)).save(eq(email), any(CodeData.class));
    verify(codeRepository, never()).deleteByEmail(email);
  }

  @Test
  void verifyCode_ShouldReturnMaxAttemptsExceeded_WhenAttemptsExceeded() {
    CodeData codeData = new CodeData("654321", Instant.now().getEpochSecond(), 2);
    when(codeRepository.findByEmail(email)).thenReturn(codeData);
    when(codeVerificationService.verify(codeData, code)).thenReturn(
        new VerificationResult(VerificationStatus.INVALID_CODE));
    when(properties.getMaxAttempts()).thenReturn(3);

    VerificationResult result = twoFactorAuthService.verifyCode(email, code);

    assertEquals(VerificationStatus.MAX_ATTEMPTS_EXCEEDED, result.status());
    verify(codeRepository, times(1)).deleteByEmail(email);
  }

  @Test
  void resendCode_ShouldResendCode_WhenIntervalHasPassed() {
    when(codeRepository.findByEmail(email)).thenReturn(null);

    boolean result = twoFactorAuthService.resendCode(email);

    assertTrue(result);
    verify(emailService, times(1)).sendEmail(eq(email), anyString());
    verify(codeRepository, times(1)).save(eq(email), any(CodeData.class));
  }

  @Test
  void resendCode_ShouldNotResendCode_WhenIntervalHasNotPassed() {
    CodeData codeData = new CodeData(code, Instant.now().getEpochSecond());
    when(codeRepository.findByEmail(email)).thenReturn(codeData);

    boolean result = twoFactorAuthService.resendCode(email);

    assertFalse(result);
    verify(emailService, never()).sendEmail(anyString(), anyString());
    verify(codeRepository, never()).save(anyString(), any(CodeData.class));
  }
}