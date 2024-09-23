package com.nulianov.twofactorauthentication.service.impl;

import static com.nulianov.twofactorauthentication.model.VerificationStatus.CODE_EXPIRED;
import static com.nulianov.twofactorauthentication.model.VerificationStatus.INVALID_CODE;
import static com.nulianov.twofactorauthentication.model.VerificationStatus.MAX_ATTEMPTS_EXCEEDED;
import static com.nulianov.twofactorauthentication.model.VerificationStatus.SUCCESS;

import com.nulianov.twofactorauthentication.config.TwoFactorAuthProperties;
import com.nulianov.twofactorauthentication.model.CodeData;
import com.nulianov.twofactorauthentication.model.VerificationResult;
import com.nulianov.twofactorauthentication.service.CodeVerificationService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Default implementation of CodeVerificationService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CodeVerificationServiceImpl implements CodeVerificationService {

  private final TwoFactorAuthProperties properties;

  @Override
  public VerificationResult verify(CodeData codeData, String code) {
    log.debug("Starting code validation");
    long currentTime = Instant.now().getEpochSecond();

    if (currentTime - codeData.creationTime() > properties.getCodeValidityDuration()) {
      return new VerificationResult(CODE_EXPIRED);
    }

    if (codeData.attempts() >= properties.getMaxAttempts()) {
      return new VerificationResult(MAX_ATTEMPTS_EXCEEDED);
    }

    if (codeData.code().equals(code)) {
      return new VerificationResult(SUCCESS);
    } else {
      return new VerificationResult(INVALID_CODE);
    }
  }
}
