# Backend (Spring Boot)

## Requirements
- Java 21+ (Java 17/21 recommended)

## Run (dev)
1. Start MySQL: `docker compose -f ../db/docker-compose.yml up -d`
2. Optional: `copy .env.example .env` and edit values
3. Run API:
   - Windows: `mvnw.cmd spring-boot:run`
   - macOS/Linux: `./mvnw spring-boot:run`

API runs on `http://localhost:8080`.

## Auth endpoints
- `POST /api/auth/register` -> `{ "email": "...", "password": "..." }`
- `POST /api/auth/login` -> `{ "email": "...", "password": "..." }`

Both return `{ "token": "..." }`.

## Quick auth check
1. Register/login to get a token
2. Call `GET /api/ping` with header: `Authorization: Bearer <token>`
