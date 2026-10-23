# FoodX

> **Production-Style Food Ordering Backend built with Spring Boot**

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-Cache-red?style=for-the-badge&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger-85EA2D?style=for-the-badge)

---

# Overview

FoodX is a production-inspired backend application that simulates the core workflow of a modern food delivery platform. The project focuses on backend engineering rather than frontend design, implementing secure authentication, restaurant management, menu management, cart handling, order processing, Redis caching, PostgreSQL persistence, and containerized deployment using Docker Compose.

The primary objective of the project is to demonstrate how a scalable Spring Boot backend is structured using clean layered architecture and modular domain separation.

---

# Key Engineering Highlights

- JWT Authentication with Refresh Token support
- Spring Security based stateless authentication
- Role-based authorization for Admin and Users
- Modular feature-based architecture
- PostgreSQL persistence using Spring Data JPA
- Redis caching for frequently accessed resources
- Docker Compose based multi-container deployment
- Swagger/OpenAPI documentation
- Global exception handling
- DTO-based request and response mapping

---

# Technology Stack

| Category         | Technologies               |
| ---------------- | -------------------------- |
| Language         | Java 21                    |
| Framework        | Spring Boot 3              |
| Security         | Spring Security, JWT       |
| Database         | PostgreSQL                 |
| Cache            | Redis                      |
| ORM              | Spring Data JPA, Hibernate |
| Documentation    | Swagger / OpenAPI          |
| Containerization | Docker, Docker Compose     |
| Build Tool       | Maven                      |

---

# High Level Architecture

```text
                Client
                   │
        HTTP / JSON REST APIs
                   │
                   ▼
            Spring Boot Backend
                   │
     ┌─────────────┴─────────────┐
     ▼                           ▼
 Redis Cache              PostgreSQL Database
```

---

# Order Lifecycle

```text
User Login
     │
     ▼
JWT Token Issued
     │
     ▼
Browse Restaurants
     │
     ▼
Browse Menu
     │
     ▼
Add Items to Cart
     │
     ▼
Place Order
     │
     ▼
Persist Order
     │
     ▼
Update Order Status
```

---

# Features

## Authentication

- User Registration
- Login
- JWT Access Tokens
- Refresh Tokens
- BCrypt Password Encoding
- Role-Based Authorization

## Restaurant

- Restaurant CRUD
- Search Restaurants
- Enable / Disable Restaurant

## Menu

- Category Management
- Menu Item Management
- Availability Controls

## Cart & Orders

- Cart Management
- Order Placement
- Order History
- Order Status Tracking

## Performance

- Redis Caching
- Cache Eviction Strategy
- Reduced Database Reads

---

# System Architecture

FoodX follows a modular layered architecture where each layer has a single responsibility. This separation keeps the codebase maintainable, testable, and scalable.

```text
                    Client Applications
         (Web / Mobile / Postman / Swagger)
                           │
                    HTTP REST Requests
                           │
                           ▼
                   Spring Boot Controllers
                           │
                           ▼
                    Service Layer
          Business Rules & Validations
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
      Redis Cache                Repository Layer
                                         │
                                         ▼
                                  PostgreSQL Database
```

---

# Request Lifecycle

Every request follows the same pipeline throughout the application.

```text
Client
   │
   ▼
Spring Security Filter
   │
JWT Authentication
   │
Role Authorization
   │
Controller
   │
Service
   │
Repository
   │
Database
   │
Response DTO
   │
Client
```

This architecture keeps authentication, business logic and persistence completely separated.

---

# JWT Authentication Flow

FoodX implements stateless authentication using Spring Security and JSON Web Tokens.

```text
User Login
      │
      ▼
Username + Password
      │
      ▼
Authentication Manager
      │
      ▼
Generate JWT
      │
      ▼
Return Access Token
      │
      ▼
Client stores Token
      │
      ▼
Future Requests
      │
Authorization Header
      │
Bearer Token
      ▼
JWT Filter
      │
Token Validation
      │
      ▼
Protected Endpoint
```

### Authentication Features

- Stateless Authentication
- JWT Access Tokens
- Refresh Tokens
- BCrypt Password Encryption
- Role Based Authorization
- Spring Security Filter Chain

---

# Redis Caching Strategy

Redis is used to reduce unnecessary database queries for frequently accessed resources.

```text
Client Request
      │
      ▼
Check Redis
      │
 ┌────┴─────┐
 │          │
Hit        Miss
 │          │
 ▼          ▼
Return    PostgreSQL
Cached       │
Data         ▼
         Store in Redis
              │
              ▼
         Return Response
```

### Cache Benefits

- Faster API response times
- Reduced PostgreSQL load
- Better scalability
- Automatic cache eviction after updates

---

# Docker Architecture

FoodX is designed to run using Docker Compose.

```text
                 Docker Compose
                       │
      ┌────────────────┼────────────────┐
      ▼                ▼                ▼
 Spring Boot      PostgreSQL         Redis
 Application       Database          Cache
```

Running the project requires only one command:

```bash
docker compose up -d
```

Docker automatically starts all required services.

---

# Package Responsibilities

The project is organized into feature-based modules instead of placing everything into a single package.

| Package    | Responsibility                |
| ---------- | ----------------------------- |
| admin      | Administrative operations     |
| auth       | Login, Registration, JWT      |
| cart       | Shopping cart management      |
| restaurant | Restaurant management         |
| menu       | Menu and categories           |
| order      | Order processing              |
| review     | Ratings and reviews           |
| user       | User profile management       |
| security   | Spring Security configuration |
| config     | Application configuration     |
| common     | Shared utilities              |
| exception  | Global exception handling     |

This modular approach makes each feature independent and easier to maintain.

---

# Project Structure

```text
FoodX
│
├── admin
│
├── auth
│   ├── controller
│   ├── service
│   ├── entity
│   ├── repository
│   └── dto
│
├── restaurant
│
├── menu
│
├── cart
│
├── order
│
├── review
│
├── security
│
├── config
│
├── common
│
└── exception
```

---

# Backend Engineering Concepts

FoodX demonstrates several important backend engineering concepts:

- Layered Architecture
- DTO Pattern
- Repository Pattern
- Dependency Injection
- Stateless Authentication
- JWT Authorization
- Spring Security
- Global Exception Handling
- REST API Design
- Redis Caching
- Docker Containerization
- Modular Code Organization
- PostgreSQL Integration
- Spring Data JPA
- Hibernate ORM

---

# Security

Authentication and authorization are implemented using Spring Security.

Implemented security features include:

- Password hashing using BCrypt
- JWT based authentication
- Stateless session management
- Role-based endpoint protection
- Unauthorized request handling
- Authentication filters

---

# Database

FoodX uses PostgreSQL as the primary database.

The database stores:

- Users
- Restaurants
- Menu Categories
- Menu Items
- Shopping Cart
- Orders
- Reviews

Spring Data JPA handles persistence while Hibernate manages ORM mapping.

---

---

# API Reference

FoodX exposes RESTful APIs for authentication, restaurant management, menu management, cart operations, order processing, and reviews.

| Method | Endpoint                  | Description               |
| ------ | ------------------------- | ------------------------- |
| POST   | `/auth/register`          | Register a new user       |
| POST   | `/auth/login`             | Authenticate user         |
| POST   | `/auth/refresh-token`     | Generate new access token |
| GET    | `/restaurants`            | Get all restaurants       |
| GET    | `/restaurants/{id}`       | Get restaurant details    |
| POST   | `/restaurants`            | Create restaurant         |
| PUT    | `/restaurants/{id}`       | Update restaurant         |
| DELETE | `/restaurants/{id}`       | Delete restaurant         |
| GET    | `/menu`                   | View menu items           |
| POST   | `/menu`                   | Create menu item          |
| PUT    | `/menu/{id}`              | Update menu item          |
| DELETE | `/menu/{id}`              | Delete menu item          |
| GET    | `/cart`                   | View user cart            |
| POST   | `/cart/add`               | Add item to cart          |
| PUT    | `/cart/update`            | Update cart item          |
| DELETE | `/cart/remove`            | Remove cart item          |
| POST   | `/orders`                 | Place new order           |
| GET    | `/orders`                 | View order history        |
| PUT    | `/orders/{id}`            | Update order status       |
| POST   | `/reviews`                | Add review                |
| GET    | `/reviews/{restaurantId}` | Restaurant reviews        |

> The actual endpoint names may vary slightly depending on the controller implementation.

---

# Local Development Setup

## Prerequisites

Install the following software before running the project.

- Java 21
- Maven
- Docker Desktop
- PostgreSQL
- Redis
- Git

---

## Clone Repository

```bash
git clone https://github.com/Deveshkanojiya/FoodX.git

cd FoodX
```

---

## Build Project

```bash
mvn clean install
```

---

## Run Using Maven

```bash
mvn spring-boot:run
```

---

## Run Using Docker Compose

```bash
docker compose up -d
```

Verify running containers

```bash
docker ps
```

Stop containers

```bash
docker compose down
```

---

# Environment Configuration

Application configuration is stored inside

```text
application.properties
```

Typical configuration includes

```properties
spring.datasource.url=

spring.datasource.username=

spring.datasource.password=

spring.data.redis.host=

spring.data.redis.port=

jwt.secret=

jwt.expiration=
```

Sensitive values should never be committed to source control.

---

# Swagger Documentation

Interactive API documentation is available after starting the application.

```
http://localhost:8080/swagger-ui/index.html
```

Swagger allows developers to

- Test APIs
- Inspect request bodies
- View response models
- Understand endpoint contracts

without writing additional client code.

---

# Error Handling

FoodX implements centralized exception handling using Spring Boot.

Handled scenarios include

- Invalid Requests
- Authentication Failures
- Authorization Failures
- Resource Not Found
- Validation Errors
- Internal Server Errors

Returning standardized API responses makes client-side integration easier.

---

# Performance Optimizations

The project includes several backend optimizations.

### Redis Caching

Frequently requested resources are cached to reduce database load.

### Stateless Authentication

JWT eliminates server-side session storage.

### Layered Architecture

Business logic remains independent from controllers and repositories.

### DTO Mapping

Only required information is transferred between client and server.

---

# Production Considerations

FoodX is designed as a learning project while following production-inspired backend practices.

A real-world deployment would typically include:

- HTTPS Everywhere
- Environment-based Configuration
- Secret Management
- Flyway Database Migrations
- Unit Testing
- Integration Testing
- CI/CD Pipeline
- Load Balancing
- API Gateway
- Monitoring using Prometheus & Grafana
- Centralized Logging
- Email Notifications
- Distributed Rate Limiting
- Cloud Deployment (AWS)

---

# Future Improvements

Some features planned for future versions include

- Payment Gateway Integration
- Real-Time Order Tracking
- WebSocket Notifications
- Kafka Event Streaming
- ElasticSearch Based Search
- Image Upload using AWS S3
- Recommendation Engine
- Restaurant Analytics Dashboard
- Admin Dashboard
- GitHub Actions CI/CD
- Kubernetes Deployment
- Microservices Migration

---

# Troubleshooting

### Application fails to start

Ensure PostgreSQL and Redis are running.

---

### Docker containers fail

Run

```bash
docker compose down

docker compose up --build
```

---

### JWT authentication fails

Verify

- JWT Secret
- Token Expiration
- Authorization Header

```
Authorization: Bearer <token>
```

---

### Database connection error

Verify

- PostgreSQL service
- Username
- Password
- Database name
- Port number

---

### Redis connection error

Check Redis container

```bash
docker ps
```

and verify the configured host and port.

---

# Learning Outcomes

This project demonstrates practical experience with

- Java 21
- Spring Boot
- Spring Security
- JWT Authentication
- Refresh Tokens
- REST API Design
- PostgreSQL
- Redis
- Docker Compose
- Spring Data JPA
- Hibernate
- Layered Architecture
- Feature Based Package Structure
- Exception Handling
- API Documentation
- Backend System Design

---

# Contributing

Contributions are welcome.

If you find bugs or have suggestions for improvements, feel free to create an issue or submit a pull request.

---

# License

This project is licensed under the **MIT License**.

---

# Acknowledgements

FoodX was developed as a backend engineering project to practice modern Spring Boot development principles, scalable REST API design, authentication, caching, and containerized deployment.

The project is intended for learning, experimentation, and portfolio purposes.

---

# Author

## Devesh Kanojiya

Backend Developer | Java • Spring Boot • PostgreSQL • Redis • Docker

**GitHub**

https://github.com/Deveshkanojiya

**LinkedIn**

https://www.linkedin.com/in/deveshkanojiya

If you found this project useful, consider giving it a ⭐ on GitHub.

---
