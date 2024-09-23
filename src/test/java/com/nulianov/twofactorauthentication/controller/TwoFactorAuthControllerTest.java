package com.nulianov.twofactorauthentication.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nulianov.twofactorauthentication.exception.EmailSendingException;
import com.nulianov.twofactorauthentication.model.TwoFactorAuthRequest;
import com.nulianov.twofactorauthentication.model.VerificationResult;
import com.nulianov.twofactorauthentication.model.VerificationStatus;
import com.nulianov.twofactorauthentication.service.TwoFactorAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TwoFactorAuthController.class)
class TwoFactorAuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private TwoFactorAuthService twoFactorAuthService;

  @Autowired
  private ObjectMapper objectMapper;

  private final String email = "test@example.com";
  private final String code = "123456";

  @Test
  void initiateTwoFactorAuth_ShouldReturnOk() throws Exception {
    doNothing().when(twoFactorAuthService).initiateTwoFactorAuth(email);

    mockMvc.perform(post("/api/auth/2fa/initiate")
            .param("email", email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.message").value("A verification code has been sent to your email."));
  }

  @Test
  void initiateTwoFactorAuth_ShouldHandleEmailSendingException() throws Exception {
    doThrow(new EmailSendingException("Failed to send email.")).when(twoFactorAuthService).initiateTwoFactorAuth(email);

    mockMvc.perform(post("/api/auth/2fa/initiate")
            .param("email", email))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.errorCode").value("EMAIL_SENDING_FAILED"))
        .andExpect(jsonPath("$.message").value("Failed to send email."));
  }

  @Test
  void verifyCode_ShouldReturnOk_WhenVerificationIsSuccessful() throws Exception {
    when(twoFactorAuthService.verifyCode(eq(email), eq(code)))
        .thenReturn(new VerificationResult(VerificationStatus.SUCCESS));

    TwoFactorAuthRequest request = new TwoFactorAuthRequest(email, code);

    mockMvc.perform(post("/api/auth/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.message").value("Two-factor authentication successful."));
  }

  @Test
  void verifyCode_ShouldReturnAppropriateError_WhenVerificationFails() throws Exception {
    when(twoFactorAuthService.verifyCode(eq(email), eq(code)))
        .thenReturn(new VerificationResult(VerificationStatus.INVALID_CODE));

    TwoFactorAuthRequest request = new TwoFactorAuthRequest(email, code);

    mockMvc.perform(post("/api/auth/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.errorCode").value("INVALID_CODE"))
        .andExpect(jsonPath("$.message").value("Invalid verification code."));
  }

  @Test
  void resendCode_ShouldReturnOk_WhenCodeResentSuccessfully() throws Exception {
    when(twoFactorAuthService.resendCode(email)).thenReturn(true);

    mockMvc.perform(post("/api/auth/2fa/resend")
            .param("email", email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.message").value("A new verification code has been sent to your email."));
  }

  @Test
  void resendCode_ShouldReturnTooManyRequests_WhenResendIntervalNotElapsed() throws Exception {
    when(twoFactorAuthService.resendCode(email)).thenReturn(false);

    mockMvc.perform(post("/api/auth/2fa/resend")
            .param("email", email))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.errorCode").value("RESEND_NOT_ALLOWED"))
        .andExpect(jsonPath("$.message").value("You cannot request a new code at the moment."));
  }

  @Test
  void resendCode_ShouldHandleEmailSendingException() throws Exception {
    doThrow(new EmailSendingException("Failed to send email.")).when(twoFactorAuthService).resendCode(email);

    mockMvc.perform(post("/api/auth/2fa/resend")
            .param("email", email))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.errorCode").value("EMAIL_SENDING_FAILED"))
        .andExpect(jsonPath("$.message").value("Failed to send email."));
  }
}