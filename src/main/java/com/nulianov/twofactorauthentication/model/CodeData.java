package com.nulianov.twofactorauthentication.model;

public record CodeData(String code, long creationTime, int attempts) {

  public CodeData(String code, long creationTime) {
    this(code, creationTime, 0);
  }

  public CodeData incrementAttempts() {
    return new CodeData(code, creationTime, attempts + 1);
  }
}
