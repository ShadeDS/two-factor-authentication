package com.nulianov.twofactorauthentication.client;

import com.nulianov.twofactorauthentication.model.EmailServiceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Feign client for interacting with the external email service.
 */
@FeignClient(name = "emailServiceClient", url = "${email.service.url}")
public interface EmailServiceClient {

  @PostMapping("/send")
  void sendEmail(EmailServiceRequest emailRequest);
}
