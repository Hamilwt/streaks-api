# Streaks API

## Live API URL

[https://streaks-api-ffig.onrender.com/](https://streaks-api-ffig.onrender.com/)

## Bonus Requirements

- [x] **Flyway database migrations** — versioned SQL in `src/main/resources/db/migration`, with Hibernate in `validate` mode.
- [x] **Dockerization** — multi-stage `Dockerfile` plus `docker-compose.yml` running the API alongside PostgreSQL 15.
- [x] **GitHub Actions CI** — `.github/workflows/test.yml` runs the full test suite on every push and pull request to `main`.

A Spring Boot REST API for building habits, checking in daily, and earning coins. Users sign up, authenticate with a JWT, create habits, check in once per day to earn 10 coins, and redeem those coins from their wallet.

Built with Java 17, Spring Boot 4, Spring Security (stateless JWT), Spring Data JPA/Hibernate, and H2 (development/tests) or PostgreSQL (production).

## Requirements

- Java 17 or newer
- No Maven installation needed — the Maven wrapper (`mvnw` / `mvnw.cmd`) is included

## Environment variables

| Variable | Required | Description |
| --- | --- | --- |
| `JWT_SECRET` | Always | Secret used to sign JWTs. Must be at least **32 bytes** (HS256 minimum). The application fails at startup if it is missing or too short. |
| `SPRING_DATASOURCE_URL` | PostgreSQL only | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/streaks` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL only | Database username |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL only | Database password |

**H2 (local development and tests):** no database configuration is required. Spring Boot starts an in-memory H2 database automatically, so no URL, username, or password is needed.

**PostgreSQL (production):** the PostgreSQL driver is already on the classpath. Point Spring at your database with the three `SPRING_DATASOURCE_*` variables above.

Generate a strong secret, for example:

```bash
openssl rand -base64 48
```

## Run locally

PowerShell (Windows):

```powershell
$env:JWT_SECRET = "dev-only-secret-at-least-32-bytes-long"
.\mvnw.cmd spring-boot:run
```

Bash (macOS/Linux):

```bash
export JWT_SECRET="dev-only-secret-at-least-32-bytes-long"
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

## Docker / docker-compose

`docker-compose.yml` starts PostgreSQL 15 and the API together. The API waits for the database healthcheck before starting, and Flyway applies the schema automatically on boot.

`JWT_SECRET` is passed through from your host environment, so export it first (at least 32 bytes):

macOS/Linux:

```bash
JWT_SECRET=my-32-character-secret-key docker-compose up --build
```

Windows PowerShell:

```powershell
$env:JWT_SECRET = "my-32-character-secret-key"
docker compose up --build
```

The API is then available at `http://localhost:8080`, with PostgreSQL on `localhost:5432`.

Stop the stack with `docker compose down`; add `-v` to also delete the database volume. If you are on Compose v2, `docker compose` is the equivalent of `docker-compose`.

The image is built in two stages: `eclipse-temurin:17-jdk` compiles and packages the jar, then only the jar is copied into a slim `eclipse-temurin:17-jre-alpine` runtime image.

## Endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/auth/signup` | Public | Create an account |
| `POST` | `/auth/login` | Public | Log in and receive a JWT |
| `GET` | `/habits` | Bearer token | List the current user's habits |
| `POST` | `/habits` | Bearer token | Create a habit |
| `POST` | `/habits/{id}/check-ins` | Bearer token | Check in to a habit for today (+10 coins) |
| `GET` | `/wallet` | Bearer token | Get the current coin balance |
| `POST` | `/wallet/redeem` | Bearer token | Redeem coins from the balance |

Errors are returned as JSON, for example:

```json
{"error": "INSUFFICIENT_BALANCE", "message": "Not enough coins"}
```

## curl examples

Set the base URL once:

```bash
BASE=http://localhost:8080
```

### Sign up

```bash
curl -i -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"secret123","name":"Alice"}'
```

Expected: `201 Created`.

### Log in and store the token

```bash
TOKEN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"secret123"}' | jq -r '.token')
```

### Create a habit

```bash
curl -i -X POST "$BASE/habits" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Morning run"}'
```

Copy the returned `id` into `HABIT_ID`:

```bash
HABIT_ID=<habit-id-from-response>
```

### List habits

```bash
curl -i "$BASE/habits" -H "Authorization: Bearer $TOKEN"
```

### Check in to a habit

```bash
curl -i -X POST "$BASE/habits/$HABIT_ID/check-ins" \
  -H "Authorization: Bearer $TOKEN"
```

Expected: `201 Created`. A second check-in on the same day returns `409` with `{"error":"ALREADY_CHECKED_IN",...}`.

### Check the wallet balance

```bash
curl -i "$BASE/wallet" -H "Authorization: Bearer $TOKEN"
```

### Redeem coins

```bash
curl -i -X POST "$BASE/wallet/redeem" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 10}'
```

Redeeming more than the current balance returns `422` with `{"error":"INSUFFICIENT_BALANCE","message":"Not enough coins"}`.

## Tests

`JWT_SECRET` must be set before running the suite, because the Spring context smoke test starts the full application:

```powershell
$env:JWT_SECRET = "dev-only-secret-at-least-32-bytes-long"
.\mvnw.cmd test
```

`StreaksServiceTest` covers the three assignment scenarios with Mockito:

1. Checking in twice on the same day → `409 Conflict`
2. Redeeming more than the balance → `422 Unprocessable Entity` (`INSUFFICIENT_BALANCE`)
3. Checking in to another user's habit → `404 Not Found`

## Assignment questions

### Q1: What happens if a user taps "Redeem" twice quickly?

**Answer:** This causes a race condition (Double-Spend Problem). Both threads might read the same balance before either updates it, allowing the user to redeem more coins than they have, potentially resulting in a negative balance. To prevent this, we must use pessimistic locking (@Lock(LockModeType.PESSIMISTIC_WRITE) on the UserRepository) or optimistic locking (@Version column) to ensure only one transaction modifies the balance at a time.

### Q2: Top 3-5 things to add for production?

**Answer:** 1) Database indexing on email and habit IDs. 2) A Redis rate-limiter to prevent brute-force login attacks and API abuse. 3) Database migrations using Flyway/Liquibase instead of Hibernate auto-ddl. 4) A proper validation layer (@Valid with @NotBlank, @Email) to reject malformed inputs before they hit the service layer.
