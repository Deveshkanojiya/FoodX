# FoodX Backend

Production-ready Spring Boot backend for FoodX.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Security
- JWT + Refresh Tokens
- PostgreSQL
- Redis
- Swagger / OpenAPI
- Docker / Docker Compose

## Run Locally

1. Start PostgreSQL and Redis:
   - `docker compose up -d postgres redis`
2. Run the application:
   - `mvn spring-boot:run`
3. Swagger UI:
   - `/swagger-ui.html`

## Features

- Authentication
- Restaurants
- Menu
- Cart
- Orders
- Reviews
- Admin APIs

## Notes

- JWT secret should be replaced in production.
- Database schema is created via JPA for development use.
- Use proper migrations for production.
