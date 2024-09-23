package com.nulianov.twofactorauthentication.model;

public record EmailServiceRequest(String to, String subject, String body) {
}
