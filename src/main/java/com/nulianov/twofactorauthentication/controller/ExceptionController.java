package com.nulianov.twofactorauthentication.controller;

import com.nulianov.twofactorauthentication.exception.EmailSendingException;
import com.nulianov.twofactorauthentication.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

  @ExceptionHandler(EmailSendingException.class)
  public ResponseEntity<ApiResponse> handleEmailSendingException(EmailSendingException ex) {
    log.error("EmailSendingException: {}", ex.getMessage());
    ApiResponse response = new ApiResponse("EMAIL_SENDING_FAILED", ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
    log.error("Unhandled exception: {}", ex.getMessage(), ex);
    ApiResponse response = new ApiResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred.");
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
