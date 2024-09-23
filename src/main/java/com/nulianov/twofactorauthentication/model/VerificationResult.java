package com.nulianov.twofactorauthentication.model;


public record VerificationResult(VerificationStatus status) {

  public String getErrorMessage() {
    return status.getErrorMessage();
  }
}
