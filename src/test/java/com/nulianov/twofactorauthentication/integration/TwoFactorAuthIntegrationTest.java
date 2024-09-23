package com.nulianov.twofactorauthentication.integration;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nulianov.twofactorauthentication.TwoFactorAuthenticationApplication;
import com.nulianov.twofactorauthentication.model.TwoFactorAuthRequest;
import com.nulianov.twofactorauthentication.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TwoFactorAuthenticationApplication.class)
@AutoConfigureMockMvc
public class TwoFactorAuthIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private EmailService emailService;

  @Autowired
  private ObjectMapper objectMapper;

  private final String email = "test@example.com";

  @Test
  void initiateTwoFactorAuth_ShouldSendEmailAndReturnSuccess() throws Exception {
    mockMvc.perform(post("/api/auth/2fa/initiate")
            .param("email", email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.message").value("A verification code has been sent to your email."));

    verify(emailService, times(1)).sendEmail(Mockito.eq(email), Mockito.anyString());
  }

  @Test
  void verifyCode_SuccessfulVerification() throws Exception {
    mockMvc.perform(post("/api/auth/2fa/initiate")
            .param("email", email))
        .andExpect(status().isOk());

    ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendEmail(Mockito.eq(email), codeCaptor.capture());
    String sentCode = codeCaptor.getValue();

    TwoFactorAuthRequest request = new TwoFactorAuthRequest(email, sentCode);
    mockMvc.perform(post("/api/auth/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.message").value("Two-factor authentication successful."));
  }

  @Test
  void verifyCode_InvalidCode_ShouldReturnInvalidCodeError() throws Exception {
    mockMvc.perform(post("/api/auth/2fa/initiate")
            .param("email", email))
        .andExpect(status().isOk());

    String incorrectCode = "000000";

    TwoFactorAuthRequest request = new TwoFactorAuthRequest(email, incorrectCode);
    mockMvc.perform(post("/api/auth/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.errorCode").value("INVALID_CODE"))
        .andExpect(jsonPath("$.message").value("Invalid verification code."));
  }

  @Test
  void resendCode_ShouldResendCodeSuccessfully() throws Exception {
    mockMvc.perform(post("/api/auth/2fa/resend")
            .param("email", email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.message").value("A new verification code has been sent to your email."));

    verify(emailService, times(1)).sendEmail(Mockito.eq(email), Mockito.anyString());
  }

  @Test
  void verifyCode_ExpiredCode_ShouldReturnInvalidCode() throws Exception {
    mockMvc.perform(post("/api/auth/2fa/initiate")
            .param("email", email))
        .andExpect(status().isOk());

    ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendEmail(Mockito.eq(email), codeCaptor.capture());
    String sentCode = codeCaptor.getValue();

    Thread.sleep(1001);

    TwoFactorAuthRequest request = new TwoFactorAuthRequest(email, sentCode);
    mockMvc.perform(post("/api/auth/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.errorCode").value("INVALID_CODE"))
        .andExpect(jsonPath("$.message").value("Invalid verification code."));
  }
}
