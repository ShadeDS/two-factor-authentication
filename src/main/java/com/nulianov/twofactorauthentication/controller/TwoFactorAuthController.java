package com.nulianov.twofactorauthentication.controller;

import com.nulianov.twofactorauthentication.model.ApiResponse;
import com.nulianov.twofactorauthentication.model.TwoFactorAuthRequest;
import com.nulianov.twofactorauthentication.model.VerificationResult;
import com.nulianov.twofactorauthentication.model.VerificationStatus;
import com.nulianov.twofactorauthentication.service.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling two-factor authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TwoFactorAuthController {

  private final TwoFactorAuthService twoFactorAuthService;

  /**
   * Initiates the two-factor authentication process by sending a code to the user's email.
   *
   * @param email The user's email address.
   * @return A response indicating that the code has been sent.
   */
  @PostMapping("/2fa/initiate")
  public ResponseEntity<ApiResponse> initiateTwoFactorAuth(@RequestParam String email) {
    log.info("Initiating two-factor authentication for email: {}", email);
    twoFactorAuthService.initiateTwoFactorAuth(email);
    return ResponseEntity.ok(new ApiResponse("A verification code has been sent to your email."));
  }

  /**
   * Verifies the code entered by the user.
   *
   * @param request Contains the user's email and the code they entered.
   * @return A response indicating success or failure of code verification.
   */
  @PostMapping("/2fa/verify")
  public ResponseEntity<ApiResponse> verifyCode(@RequestBody TwoFactorAuthRequest request) {
    log.info("Verification attempt for email: {}", request.email());
    VerificationResult result = twoFactorAuthService.verifyCode(request.email(), request.code());

    return switch (result.status()) {
      case SUCCESS -> ResponseEntity.ok(new ApiResponse("Two-factor authentication successful."));
      case CODE_EXPIRED, MAX_ATTEMPTS_EXCEEDED, INVALID_CODE ->
          ResponseEntity.status(getHttpStatusForStatus(result.status()))
              .body(new ApiResponse(result.status().name(), result.getErrorMessage()));
    };
  }

  /**
   * Allows the user to request a new verification code.
   *
   * @param email The user's email address.
   * @return A response indicating that a new code has been sent.
   */
  @PostMapping("/2fa/resend")
  public ResponseEntity<ApiResponse> resendCode(@RequestParam String email) {
    log.info("Resending the code for email: {}", email);
    boolean isResent = twoFactorAuthService.resendCode(email);
    if (isResent) {
      return ResponseEntity.ok(new ApiResponse("A new verification code has been sent to your email."));
    } else {
      return ResponseEntity
          .badRequest()
          .body(new ApiResponse("RESEND_NOT_ALLOWED", "You cannot request a new code at the moment."));
    }
  }

  private HttpStatus getHttpStatusForStatus(VerificationStatus status) {
    return switch (status) {
      case CODE_EXPIRED -> HttpStatus.GONE;
      case MAX_ATTEMPTS_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
      case INVALID_CODE -> HttpStatus.BAD_REQUEST;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }
}
