package com.nulianov.twofactorauthentication.model;

import lombok.Getter;

@Getter
public enum VerificationStatus {
  SUCCESS(null),
  CODE_EXPIRED("Verification code has expired."),
  MAX_ATTEMPTS_EXCEEDED("Maximum number of attempts exceeded."),
  INVALID_CODE("Invalid verification code.");

  private final String errorMessage;

  VerificationStatus(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
