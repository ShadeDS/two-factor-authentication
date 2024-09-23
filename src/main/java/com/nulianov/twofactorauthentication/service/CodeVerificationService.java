package com.nulianov.twofactorauthentication.service;

import com.nulianov.twofactorauthentication.model.CodeData;
import com.nulianov.twofactorauthentication.model.VerificationResult;

/**
 * Service interface for verifying codes during two-factor authentication.
 */
public interface CodeVerificationService {

  /**
   * Verifies the provided code against the stored code data.
   *
   * @param codeData the stored code data
   * @param code     the code entered by the user
   * @return the result of the verification attempt
   */
  VerificationResult verify(CodeData codeData, String code);
}
