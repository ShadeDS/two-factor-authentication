package com.nulianov.twofactorauthentication.service.impl;

import com.nulianov.twofactorauthentication.config.TwoFactorAuthProperties;
import com.nulianov.twofactorauthentication.model.CodeData;
import com.nulianov.twofactorauthentication.model.VerificationResult;
import com.nulianov.twofactorauthentication.model.VerificationStatus;
import com.nulianov.twofactorauthentication.repository.CodeRepository;
import com.nulianov.twofactorauthentication.service.CodeVerificationService;
import com.nulianov.twofactorauthentication.service.EmailService;
import com.nulianov.twofactorauthentication.service.TwoFactorAuthService;
import java.time.Instant;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Default implementation of TwoFactorAuthService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

  private final TwoFactorAuthProperties properties;
  private final EmailService emailService;
  private final CodeVerificationService codeVerificationService;
  private final CodeRepository codeRepository;

  @Override
  public void initiateTwoFactorAuth(String email) {
    log.info("Initiating two-factor authentication for email: {}", email);
    String code = generateSixDigitCode();
    log.debug("Generated verification code for email: {}", email);

    CodeData codeData = new CodeData(code, Instant.now().getEpochSecond());

    codeRepository.save(email, codeData);
    emailService.sendEmail(email, code);
    log.info("Sent verification email to {}", email);
  }

  @Override
  public VerificationResult verifyCode(String email, String code) {
    log.info("Verifying code for email: {}", email);
    CodeData codeData = codeRepository.findByEmail(email);

    if (codeData == null) {
      log.debug("No code data found for email: {}", email);
      return new VerificationResult(VerificationStatus.INVALID_CODE);
    }

    VerificationResult result = codeVerificationService.verify(codeData, code);
    log.debug("Verification result for email {}: {}", email, result.status());

    switch (result.status()) {
      case SUCCESS, CODE_EXPIRED, MAX_ATTEMPTS_EXCEEDED:
        codeRepository.deleteByEmail(email);
        break;

      case INVALID_CODE:
        CodeData updatedCodeData = codeData.incrementAttempts();

        if (updatedCodeData.attempts() >= properties.getMaxAttempts()) {
          log.warn("Maximum verification attempts exceeded for email: {}", email);
          codeRepository.deleteByEmail(email);
          result = new VerificationResult(VerificationStatus.MAX_ATTEMPTS_EXCEEDED);
        } else {
          codeRepository.save(email, updatedCodeData);
        }
        break;

      default:
        break;
    }

    return result;
  }

  @Override
  public boolean resendCode(String email) {
    log.info("Resend code requested for email: {}", email);
    CodeData codeData = codeRepository.findByEmail(email);

    long currentTime = Instant.now().getEpochSecond();

    if (codeData == null || currentTime - codeData.creationTime() >= properties.getResendInterval()) {
      String newCode = generateSixDigitCode();
      CodeData newCodeData = new CodeData(newCode, currentTime);

      codeRepository.save(email, newCodeData);
      emailService.sendEmail(email, newCode);
      log.debug("Generated and sent new verification code to {}", email);
      return true;
    } else {
      log.warn("Resend interval not elapsed for email: {}", email);
      return false;
    }
  }

  private String generateSixDigitCode() {
    Random random = new Random();
    int code = 100000 + random.nextInt(900000);
    return String.valueOf(code);
  }
}
