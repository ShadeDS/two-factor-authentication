package com.nulianov.twofactorauthentication.service.impl;

import com.nulianov.twofactorauthentication.client.EmailServiceClient;
import com.nulianov.twofactorauthentication.exception.EmailSendingException;
import com.nulianov.twofactorauthentication.model.EmailServiceRequest;
import com.nulianov.twofactorauthentication.service.EmailService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService using Feign client to send emails.
 */
@Service
@Slf4j
public class ExternalEmailService implements EmailService {

  private final EmailServiceClient emailServiceClient;
  private final String emailSubject;
  private final String emailBodyTemplate;

  @Autowired
  public ExternalEmailService(
      EmailServiceClient emailServiceClient,
      @Value("${email.subject}") String emailSubject,
      @Value("${email.body.template}") String emailBodyTemplate
  ) {
    this.emailServiceClient = emailServiceClient;
    this.emailSubject = emailSubject;
    this.emailBodyTemplate = emailBodyTemplate;
  }

  @Override
  public void sendEmail(String email, String code) {
    log.info("Attempting to send email to {}", email);
    String emailBody = emailBodyTemplate.replace("{code}", code);

    EmailServiceRequest emailRequest = new EmailServiceRequest(
        email,
        emailSubject,
        emailBody
    );

    try {
      emailServiceClient.sendEmail(emailRequest);
      log.info("Email sent successfully to {}", email);
    } catch (FeignException fe) {
      log.error("Failed to send email to {}: {}", email, fe.getMessage());
      throw new EmailSendingException("Failed to send verification email.");
    } catch (Exception ex) {
      log.error("An unexpected error occurred while sending email to {}: {}", email, ex.getMessage());
      throw new EmailSendingException("An unexpected error occurred while sending verification email.");
    }
  }
}
