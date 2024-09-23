# Two-Factor Authentication Service
This is a simple two-factor authentication (2FA) service implemented using Spring Boot. It allows users to initiate 2FA, verify codes, and resend codes via email.

## Features
- Initiate two-factor authentication by sending a verification code to the user's email.
- Verify the code provided by the user.
- Resend verification codes with a configurable interval.

## API Endpoints

### Initiate Two-Factor Authentication

**Request**

```http
POST /api/auth/2fa/initiate?email={email}
```

**Response**

- `200 OK` on success.

### Verify Code

**Request**

```http
POST /api/auth/2fa/verify
Content-Type: application/json

{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response**

- `200 OK` if verification is successful.
- Appropriate error status codes and messages if verification fails.

### Resend Code

**Request**

```http
POST /api/auth/2fa/resend?email={email}
```

**Response**

- `200 OK` if code is resent successfully.
- `400 BAD REQUEST` if resend interval has not passed.

## Notes

- The application uses an in-memory cache for storing verification codes.
- The email sending functionality is mocked for local testing. Ensure that the `email.service.url` points to a valid email service endpoint.
