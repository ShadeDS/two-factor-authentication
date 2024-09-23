package com.nulianov.twofactorauthentication.model;

public record TwoFactorAuthRequest(String email, String code) {
}
