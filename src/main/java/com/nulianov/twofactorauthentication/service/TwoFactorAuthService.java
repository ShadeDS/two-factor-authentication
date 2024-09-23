package com.nulianov.twofactorauthentication.service;

import com.nulianov.twofactorauthentication.model.VerificationResult;

/**
 * Interface for two-factor authentication service.
 */
public interface TwoFactorAuthService {

  /**
   * Initiates the two-factor authentication process by generating a code and sending it to the user's email.
   *
   * @param email The user's email address.
   */
  void initiateTwoFactorAuth(String email);

  /**
   * Verifies the provided code for two-factor authentication.
   *
   * @param email the user's email address
   * @param code  the verification code entered by the user
   * @return a {@link VerificationResult} indicating the outcome of the verification attempt
   */
  VerificationResult verifyCode(String email, String code);

  /**
   * Allows the user to request a new verification code.
   *
   * @param email The user's email address.
   * @return True if a new code is sent; false if the user must wait longer.
   */
  boolean resendCode(String email);
}
