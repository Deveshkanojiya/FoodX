# FoodX - Food Delivery Backend

FoodX is a backend application inspired by food delivery platforms like Zomato and Swiggy. The project focuses on scalable backend architecture, authentication, caching, database design, and containerized deployment using modern Java backend technologies.

## Features

### Authentication & Authorization

* User Registration & Login
* JWT Authentication
* Refresh Token Mechanism
* Role-Based Access Control (Customer, Restaurant Owner, Admin)
* Secure Password Hashing using BCrypt

### Restaurant Management

* Create Restaurant
* Update Restaurant Details
* Open / Close Restaurant
* Disable Restaurant
* Search Restaurants

### Menu Management

* Create Menu Categories
* Add Menu Items
* Update Menu Items
* Enable / Disable Menu Items

### Cart Management

* Add Items to Cart
* Update Quantity
* Remove Items
* Clear Cart

### Order Management

* Place Orders
* View Order History
* Track Order Status
* Order Status Updates

### Reviews & Ratings

* Create Reviews
* View Restaurant Reviews
* Review Management

### Caching

* Redis Integration
* Restaurant Details Caching
* Search Result Caching
* Cache Eviction on Updates

### API Documentation

* Swagger / OpenAPI Integration

### Containerization

* Dockerized Application
* Docker Compose Setup
* PostgreSQL Container
* Redis Container

---

## Tech Stack

### Backend

* Java 21
* Spring Boot 3

### Security

* Spring Security
* JWT Authentication

### Database

* PostgreSQL

### Cache

* Redis

### Documentation

* Swagger / OpenAPI

### Containerization

* Docker
* Docker Compose

### Build Tool

* Maven

---

## Key Backend Concepts Implemented

- JWT Access Tokens
- Refresh Token Strategy
- Stateless Authentication
- Role-Based Authorization
- Redis Caching
- Global Exception Handling
- DTO Pattern
- Layered Architecture
- Dockerized Deployment
- OpenAPI Documentation

---

## Architecture

```text
Client
   |
   v
Spring Boot REST API
   |
   +------> Redis Cache
   |
   +------> PostgreSQL Database
```

### Application Layers

```text
Controller
     |
     v
Service
     |
     v
Repository
     |
     v
Database
```

---

## Project Structure

```text
com.foodx
│
├── admin
├── auth
├── cart
├── common
├── config
├── exception
├── menu
├── order
├── restaurant
├── review
├── security
├── user
```

---

## Screenshots

### Swagger UI

![Swagger UI](images/swagger-home.png)

### Authentication

![Authentication](images/login-api.png)

### Restaurant APIs

![Restaurant APIs](images/restaurant-api.png)

### Docker Containers

![Docker Containers](images/docker-containers.png)


---

## Running with Docker

### Clone Repository

```bash
git clone https://github.com/Deveshkanojiya/FoodX.git
cd FoodX
```

### Start Application

```bash
docker-compose up -d
```

### Check Running Containers

```bash
docker ps
```

### View Logs

```bash
docker logs foodx-app
```

### Stop Application

```bash
docker-compose down
```

---

## API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI Docs:

```text
http://localhost:8080/api-docs
```

---

## Learning Outcomes

This project helped me gain practical experience with:

* Spring Boot Backend Development
* REST API Design
* Spring Security
* JWT Authentication
* Refresh Token Flow
* Redis Caching
* PostgreSQL Integration
* Docker & Docker Compose
* API Documentation using Swagger
* Layered Architecture Design

---

## Future Improvements

* Unit Testing (JUnit + Mockito)
* Flyway Database Migrations
* Pagination & Sorting
* Rate Limiting using Redis
* Email Notifications
* CI/CD Pipeline
* Cloud Deployment (AWS)

---

## Author

Devesh Kanojiya

GitHub: https://github.com/Deveshkanojiya
