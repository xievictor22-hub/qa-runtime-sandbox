# Backend context (Spring Boot 3 / Java 17)

## Auth endpoints (AuthController)
- `POST /auth/login` -> returns `{ token, refresh_token, uuid }`
- `POST /auth/logout` -> deletes Redis session by uuid extracted from access token
- `POST /auth/refresh` -> accepts `{ refreshToken }`, returns **string access token**

> Issue: `refresh` is annotated with `@PreAuthorize(...)` which may prevent refresh when access token is expired.
> Recommended: remove or change to refresh-token-only validation.

## TokenService
- Creates UUID session id (uuid) stored in Redis: `login_tokens:{uuid} -> LoginUser`
- Also stores username->latest uuid mapping: `user_last_token_key:{username} -> uuid`
- Issues two JWTs:
  - access: key `token`
  - refresh: key `refresh_token`
- Both JWTs include claim: `login_user_key = uuid`

### Critical bug
`TokenService` defines `long now = System.currentTimeMillis();` as a field and uses it in `createJwt()`.
This makes `iat/exp` timestamps wrong after the server has been running.
Fix: compute `now` inside `createJwt()` per call.

## JWT filter
- `JwtAuthenticationTokenFilter` parses JWT from `Authorization` header.
- On ExpiredJwtException: returns 401 JSON `{code:401, message:...}`
