package com.nulianov.twofactorauthentication.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nulianov.twofactorauthentication.client.EmailServiceClient;
import com.nulianov.twofactorauthentication.exception.EmailSendingException;
import com.nulianov.twofactorauthentication.model.EmailServiceRequest;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExternalEmailServiceTest {

  private EmailServiceClient emailServiceClient;

  private ExternalEmailService externalEmailService;

  private final String emailSubject = "Your verification code";
  private final String emailBodyTemplate = "Your code is: {code}";

  private final String email = "test@example.com";
  private final String code = "123456";

  @BeforeEach
  void setUp() {
    emailServiceClient = mock(EmailServiceClient.class);
    externalEmailService = new ExternalEmailService(emailServiceClient, emailSubject, emailBodyTemplate);
  }

  @Test
  void sendEmail_ShouldSendEmailUsingEmailServiceClient() {
    String expectedBody = emailBodyTemplate.replace("{code}", code);

    externalEmailService.sendEmail(email, code);

    EmailServiceRequest expectedRequest = new EmailServiceRequest(email, emailSubject, expectedBody);
    verify(emailServiceClient, times(1)).sendEmail(expectedRequest);
  }

  @Test
  void sendEmail_ShouldHandleFeignExceptionAndThrowEmailSendingException() {
    doThrow(FeignException.class).when(emailServiceClient).sendEmail(any(EmailServiceRequest.class));

    EmailSendingException exception = assertThrows(
        EmailSendingException.class,
        () -> externalEmailService.sendEmail(email, code)
    );
    assertEquals("Failed to send verification email.", exception.getMessage());
  }

  @Test
  void sendEmail_ShouldHandleGenericExceptionAndThrowEmailSendingException() {
    doThrow(RuntimeException.class).when(emailServiceClient).sendEmail(any(EmailServiceRequest.class));

    EmailSendingException exception = assertThrows(
        EmailSendingException.class,
        () -> externalEmailService.sendEmail(email, code)
    );
    assertEquals("An unexpected error occurred while sending verification email.", exception.getMessage());
  }
}