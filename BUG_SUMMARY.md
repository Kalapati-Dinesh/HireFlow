# HireFlow — Bug Fix Summary

> Full codebase scan — 7 issues found and fixed across 5 files.

---

## Bug 1 — Hardcoded JWT Secret ✅ FIXED
**File:** `src/main/java/com/hireflow/jwt/JwtUtil.java` (Lines 17–18)
**Severity:** Critical | **CWE:** CWE-798

**Problem:** The JWT signing secret was hardcoded as a string literal in source code.

**Fix:** Removed the hardcoded string. Injected the secret via `@Value("${jwt.secret}")` so it is read from the environment variable `JWT_SECRET` at runtime and never stored in source code.

---

## Bug 2 — Missing Charset in `String.getBytes()` ✅ FIXED
**File:** `src/main/java/com/hireflow/jwt/JwtUtil.java` (Lines 21–22)
**Severity:** Medium

**Problem:** `SECRET.getBytes()` used the JVM default encoding, which varies by platform and can cause inconsistent JWT key generation.

**Fix:** Changed to `secret.getBytes(StandardCharsets.UTF_8)` to guarantee consistent encoding on all platforms.

---

## Bug 3 — Hardcoded Database & Mail Credentials ✅ FIXED
**File:** `src/main/resources/application.properties` (Lines 4–5, 14–15)
**Severity:** Low | **CWE:** CWE-798

**Problem:** DB password, mail username, and mail password were hardcoded directly in the properties file.

**Fix:** Replaced all sensitive values with environment variable placeholders:
- `spring.datasource.password=${DB_PASSWORD}`
- `spring.mail.username=${MAIL_USERNAME}`
- `spring.mail.password=${MAIL_PASSWORD}`
- `jwt.secret=${JWT_SECRET}`

Set these as environment variables before running the application.

---

## Bug 4 — Log Injection ✅ FIXED
**File:** `src/main/java/com/hireflow/api/CandidateRestController.java` (Lines 48–49)
**Severity:** High | **CWE:** CWE-117, CWE-93

**Problem:** User-supplied email was logged without sanitization. An attacker could inject `\n` or `\r` characters to forge fake log entries.

**Fix:** Added `.replaceAll("[\r\n]", "")` to strip all newline characters from the email before it is passed to the logger.

---

## Bug 5 — CSRF Disabled Without Justification ✅ FIXED
**File:** `src/main/java/com/hireflow/config/SecurityConfig.java` (Lines 37–38)
**Severity:** High | **CWE:** CWE-352

**Problem:** CSRF protection was disabled with no explanation, which is a red flag during security reviews.

**Fix:** CSRF is legitimately not needed here because the app is fully stateless and uses JWT Bearer tokens (not cookies). Added a clear comment documenting this reasoning so it is not mistakenly re-enabled or flagged without context.

---

## Bug 6 — Missing Auth Check on POST Endpoint ✅ FIXED
**File:** `src/main/java/com/hireflow/controllers/CandidateController.java` (Lines 80–81)
**Severity:** High | **CWE:** CWE-352

**Problem:** The `saveCandidate` POST endpoint had no session or role check, unlike every other endpoint in the same controller. Any unauthenticated user could POST to `/saveCandidate` and insert records.

**Fix:** Added the same session + RECRUITER role guard used by all other endpoints in the controller.

---

## Bug 7 — Unsafe `Optional.get()` Without Null Check ✅ FIXED
**File:** `src/main/java/com/hireflow/services/CandidateService.java` (Lines 44–45)
**Severity:** Medium | **CWE:** CWE-476

**Problem:** `optional.get()` was called directly without checking `isPresent()`, causing a `NoSuchElementException` crash if a candidate ID does not exist.

**Fix:** Replaced with `.orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id))` for a safe, descriptive failure. Also removed the now-unused `Optional` import.

---

## Summary Table

| # | File | Severity | Issue | Status |
|---|------|----------|-------|--------|
| 1 | `JwtUtil.java` | Critical | Hardcoded JWT secret | ✅ Fixed |
| 2 | `JwtUtil.java` | Medium | `getBytes()` without charset | ✅ Fixed |
| 3 | `application.properties` | Low | Hardcoded DB & mail credentials | ✅ Fixed |
| 4 | `CandidateRestController.java` | High | Log injection via unsanitized input | ✅ Fixed |
| 5 | `SecurityConfig.java` | High | CSRF disabled without justification | ✅ Fixed |
| 6 | `CandidateController.java` | High | Missing auth check on POST endpoint | ✅ Fixed |
| 7 | `CandidateService.java` | Medium | Unsafe `Optional.get()` without null check | ✅ Fixed |

---

## Environment Variables Required to Run

Set these before starting the application:

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | JWT signing secret (min 32 chars) |
| `DB_PASSWORD` | MySQL database password |
| `MAIL_USERNAME` | Gmail address for sending emails |
| `MAIL_PASSWORD` | Gmail app password |
| `DB_URL` *(optional)* | Defaults to `jdbc:mysql://host.docker.internal:3306/hireflow` |
| `DB_USERNAME` *(optional)* | Defaults to `root` |
