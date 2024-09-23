package com.nulianov.twofactorauthentication.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;


@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

  String status;
  String message;
  String errorCode;

  public ApiResponse(String message) {
    this.status = "success";
    this.message = message;
    this.errorCode = null;
  }

  public ApiResponse(String errorCode, String message) {
    this.status = "error";
    this.errorCode = errorCode;
    this.message = message;
  }
}
