package com.nulianov.twofactorauthentication.service;

/**
 * Service interface for sending emails.
 */
public interface EmailService {

  /**
   * Sends an email containing a verification code to the specified recipient.
   *
   * @param email the recipient's email address
   * @param code  the verification code to be sent
   */
  void sendEmail(String email, String code);
}
