# FoodX Architecture Discovery

**Author:** Senior Staff Software Engineer & Principal Backend Architect  
**Project:** FoodX Backend (`com.foodx:foodx-backend:1.0.0`)  
**Repository State:** Reverse Engineered Current State (As-Is Production Inspection)  
**Document Classification:** System Architecture Reverse-Engineering Report  

---

## 1. Executive Summary

### What is FoodX?
FoodX is a monolithic, RESTful food ordering and restaurant discovery backend platform built on Java 21 and Spring Boot 3.3.5. It provides domain capabilities enabling customer onboarding, authentication, restaurant discovery and management, menu structuring, active shopping cart management, order placement, order lifecycle state updates, and post-delivery review publishing. It also incorporates administrative capabilities for platform moderation.

### What Problem Does It Solve?
FoodX serves as the centralized backend commerce and operational orchestration layer connecting three primary user personas:
1. **Customers:** Discover dining establishments by name or city, explore categorized menus, assemble shopping carts with single-restaurant constraints, submit food orders, track order fulfillment states, and submit post-delivery ratings and reviews.
2. **Restaurant Owners:** Register restaurant profiles, manage operational status (Open/Closed), create menu categories, and manage menu items with pricing and availability flags.
3. **Platform Administrators:** Supervise platform health, inspect global accounts, view system-wide orders, suspend misbehaving user accounts, and disable non-compliant restaurant entities.

### High-Level Architecture
FoodX follows a traditional, layered (controller-service-repository) monolithic architecture pattern:
- **Client Tier:** Consumes RESTful JSON APIs secured via stateless HTTP Bearer tokens (JSON Web Tokens).
- **Presentation / API Tier:** Standard Spring Web MVC REST controllers mapped under `/api/*`, handling incoming request validation via Jakarta Bean Validation and returning uniform DTO records.
- **Security & Authorization Tier:** Spring Security 6 filter chain utilizing a custom `OncePerRequestFilter` (`JwtAuthenticationFilter`) that parses HS256 JWT access tokens, validates user identity via database queries on every authenticated request, and enforces method-level authorization (`@PreAuthorize`) based on user roles (`CUSTOMER`, `OWNER`, `ADMIN`).
- **Service / Business Tier:** Spring-managed transactional service beans (`@Transactional`) implementing business rules, state machines, and cache evictions.
- **Caching Tier:** Distributed caching via Redis 7 with a uniform 30-minute default time-to-live (TTL) and Jackson JSON value serialization.
- **Persistence Tier:** Spring Data JPA with Hibernate 6 acting as the Object-Relational Mapper (ORM), running against a PostgreSQL 16 relational database with automated schema generation (`hibernate.ddl-auto: update`).

```
                    +-------------------------------------------------+
                    |               HTTP Clients / SPAs               |
                    +-------------------------------------------------+
                                             |
                                  REST JSON / Bearer JWT
                                             v
+---------------------------------------------------------------------------------------+
|  FoodX Backend Service (Spring Boot 3.3.5 / Java 21)                                  |
|                                                                                       |
|  +---------------------------------------------------------------------------------+  |
|  | Spring Security Filter Chain                                                    |  |
|  | - CorsFilter (Default)                                                          |  |
|  | - CsrfFilter (Disabled)                                                         |  |
|  | - JwtAuthenticationFilter -> JwtService (HS256) -> CustomUserDetailsService     |  |
|  +---------------------------------------------------------------------------------+  |
|                                            |                                          |
|                                            v                                          |
|  +---------------------------------------------------------------------------------+  |
|  | REST Controllers (@RestController)                                              |  |
|  | Auth | Restaurant | Menu | Cart | Order | Review | Admin                       |  |
|  +---------------------------------------------------------------------------------+  |
|                                            |                                          |
|                                            v                                          |
|  +---------------------------------------------------------------------------------+  |
|  | Service Layer (@Service, @Transactional)                                        |  |
|  | AuthService | RestaurantService | MenuService | CartService | OrderService ...   |  |
|  +---------------------------------------------------------------------------------+  |
|                         |                                     |                       |
|                         v                                     v                       |
|  +-------------------------------------+   +---------------------------------------+  |
|  | Cache Abstraction (Spring Cache)   |   | Spring Data JPA Repositories          |  |
|  | RedisCacheManager                   |   | Hibernate 6 ORM                       |  |
|  +-------------------------------------+   +---------------------------------------+  |
+----------------------------|-----------------------------------|----------------------+
                             |                                   |
                             v                                   v
             +-------------------------------+   +---------------------------------+
             |   Redis 7 Cache (Port 6379)   |   | PostgreSQL 16 DB (Port 5432)   |
             +-------------------------------+   +---------------------------------+
```

### Technologies Used
- **Core Runtime:** Java 21 (Eclipse Temurin)
- **Framework:** Spring Boot 3.3.5
- **Data Persistence:** Spring Data JPA, Hibernate 6, PostgreSQL JDBC Driver
- **Security:** Spring Security 6.3, JJWT (io.jsonwebtoken 0.11.5)
- **Caching:** Spring Data Redis, Jedis/Lettuce client, Redis 7
- **API Documentation:** SpringDoc OpenAPI 2.6.0 (Swagger UI 3)
- **Containerization:** Multi-stage Dockerfile, Docker Compose v2
- **Build System:** Apache Maven 3.9.9, Lombok 1.18.36, MapStruct 1.6.3 (defined in build config)

### Overall Project Complexity
The project complexity is **Low to Moderate**. It is a clean, modern Spring Boot greenfield/early-stage monolithic codebase (~2,100 lines of Java code). It possesses a compact domain model (10 entity tables) and clean separation of concerns across packages. However, it lacks advanced enterprise concerns such as automated migrations (Flyway/Liquibase), asynchronous event brokers (Kafka/RabbitMQ), integrated test suites (zero test coverage), payment gateways, idempotency keys, and granular object-level authorization (IDOR protection).

---

## 2. Technology Stack

All dependencies, build plugins, runtimes, and external components configured in the project are itemized below with exact versions extracted directly from `pom.xml`, `Dockerfile`, `docker-compose.yml`, and source manifests:

| Component Category | Technology / Library | Version | Scope / Notes |
| :--- | :--- | :--- | :--- |
| **Java Platform** | OpenJDK / Eclipse Temurin | `21` | Targeted via `<java.version>21</java.version>` |
| **Core Framework** | Spring Boot | `3.3.5` | `spring-boot-starter-parent` |
| **Build Automation** | Apache Maven | `3.9.9` (Docker build) | Maven 4.0.0 project model |
| **Web Tier** | `spring-boot-starter-web` | Managed (3.3.5) | Embedded Apache Tomcat |
| **Security Framework** | `spring-boot-starter-security` | Managed (3.3.5) | Spring Security 6.3.x |
| **JWT Library** | `io.jsonwebtoken:jjwt-api` | `0.11.5` | JJWT API |
| | `io.jsonwebtoken:jjwt-impl` | `0.11.5` | JJWT Runtime implementation |
| | `io.jsonwebtoken:jjwt-jackson` | `0.11.5` | JJWT Jackson JSON deserializer |
| **Validation** | `spring-boot-starter-validation` | Managed (3.3.5) | Hibernate Validator (Jakarta EE Bean Validation 3.0) |
| **Persistence / ORM** | `spring-boot-starter-data-jpa` | Managed (3.3.5) | Spring Data JPA / Hibernate 6.5.x |
| **Database Driver** | `org.postgresql:postgresql` | Managed (42.7.4) | PostgreSQL JDBC Driver (runtime scope) |
| **Relational Database**| PostgreSQL Engine | `16` | Specified via Docker image `postgres:16` |
| **Caching Core** | `spring-boot-starter-cache` | Managed (3.3.5) | Spring Caching Abstraction |
| **Redis Client** | `spring-boot-starter-data-redis` | Managed (3.3.5) | Lettuce Driver connecting to Redis |
| **Cache Server** | Redis Engine | `7` | Specified via Docker image `redis:7` |
| **Metrics / Ops** | `spring-boot-starter-actuator` | Managed (3.3.5) | Actuator endpoints (health, info, metrics) |
| **API Documentation** | `springdoc-openapi-starter-webmvc-ui`| `2.6.0` | OpenAPI 3.0 specification & Swagger UI |
| **Code Generation** | `org.projectlombok:lombok` | `1.18.36` | Annotation processor (compile-time) |
| **Object Mapping** | `org.mapstruct:mapstruct` | `1.6.3` | **Declared in POM, but unused in application code** |
| | `mapstruct-processor` | `1.6.3` | Annotation processor defined in `maven-compiler-plugin` |
| **Config Processor** | `spring-boot-configuration-processor` | Managed (3.3.5) | Generates configuration metadata |
| **Testing Libraries** | `spring-boot-starter-test` | Managed (3.3.5) | JUnit Jupiter, Mockito, AssertJ (scope: `test`) |
| | `spring-security-test` | Managed (3.3.5) | Security testing utilities (scope: `test`) |
| **Build Plugins** | `spring-boot-maven-plugin` | Managed (3.3.5) | Executable fat JAR packaging |
| | `maven-compiler-plugin` | `3.13.0` | Java 21 compilation with annotation processors |
| **Database Migrations**| Flyway / Liquibase | **None** | *Absent.* Relies on `hibernate.ddl-auto: update` |
| **Message Brokers** | Kafka / RabbitMQ | **None** | *Absent.* System is strictly synchronous HTTP |
| **CI/CD** | GitHub Actions | **None** | *Absent.* No `.github/workflows` present in repo |
| **Container Engine** | Docker / Docker Compose | Compose Schema v3.8+ | 3 services: `postgres`, `redis`, `app` |

---

## 3. Project Folder Structure

The root directory contains configuration files, Docker specifications, Maven build files, and source code divided into standard Maven layouts.

### Filesystem Directory Tree
```
foodx-backend/
├── .gitignore
├── Dockerfile
├── README.md
├── docker-compose.yml
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── foodx/
    │   │           ├── FoodxApplication.java
    │   │           ├── admin/
    │   │           │   ├── AdminController.java
    │   │           │   ├── AdminDtos.java
    │   │           │   └── AdminService.java
    │   │           ├── auth/
    │   │           │   ├── AuthController.java
    │   │           │   ├── AuthDtos.java
    │   │           │   ├── AuthService.java
    │   │           │   ├── RefreshToken.java
    │   │           │   └── RefreshTokenRepository.java
    │   │           ├── cart/
    │   │           │   ├── Cart.java
    │   │           │   ├── CartController.java
    │   │           │   ├── CartDtos.java
    │   │           │   ├── CartItem.java
    │   │           │   ├── CartRepository.java
    │   │           │   └── CartService.java
    │   │           ├── common/
    │   │           │   ├── AccountStatus.java
    │   │           │   ├── BaseEntity.java
    │   │           │   ├── MenuItemStatus.java
    │   │           │   ├── OrderStatus.java
    │   │           │   ├── RestaurantStatus.java
    │   │           │   ├── ReviewStatus.java
    │   │           │   └── UserRole.java
    │   │           ├── config/
    │   │           │   ├── AppConfig.java
    │   │           │   ├── CacheNames.java
    │   │           │   ├── JwtProperties.java
    │   │           │   ├── OpenApiConfig.java
    │   │           │   └── RedisConfig.java
    │   │           ├── exception/
    │   │           │   ├── BadRequestException.java
    │   │           │   ├── GlobalExceptionHandler.java
    │   │           │   ├── ResourceNotFoundException.java
    │   │           │   └── UnauthorizedException.java
    │   │           ├── menu/
    │   │           │   ├── MenuCategory.java
    │   │           │   ├── MenuCategoryRepository.java
    │   │           │   ├── MenuController.java
    │   │           │   ├── MenuDtos.java
    │   │           │   ├── MenuItem.java
    │   │           │   ├── MenuItemRepository.java
    │   │           │   └── MenuService.java
    │   │           ├── order/
    │   │           │   ├── FoodOrder.java
    │   │           │   ├── FoodOrderItem.java
    │   │           │   ├── FoodOrderItemRepository.java
    │   │           │   ├── FoodOrderRepository.java
    │   │           │   ├── OrderController.java
    │   │           │   ├── OrderDtos.java
    │   │           │   └── OrderService.java
    │   │           ├── restaurant/
    │   │           │   ├── Restaurant.java
    │   │           │   ├── RestaurantController.java
    │   │           │   ├── RestaurantDtos.java
    │   │           │   ├── RestaurantRepository.java
    │   │           │   └── RestaurantService.java
    │   │           ├── review/
    │   │           │   ├── Review.java
    │   │           │   ├── ReviewController.java
    │   │           │   ├── ReviewDtos.java
    │   │           │   ├── ReviewRepository.java
    │   │           │   └── ReviewService.java
    │   │           ├── security/
    │   │           │   ├── CustomUserDetailsService.java
    │   │           │   ├── JwtAuthenticationFilter.java
    │   │           │   ├── JwtService.java
    │   │           │   └── SecurityConfig.java
    │   │           └── user/
    │   │               ├── User.java
    │   │               └── UserRepository.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/
            └── com/
                └── foodx/
                    └── (empty directory)
```

### Folder Responsibilities

- **`com.foodx` (Root Package):** Contains the primary application entry point (`FoodxApplication.java`), which enables Spring Boot auto-configuration and Spring caching.
- **`config/`:** Infrastructure configuration classes managing Spring JPA Auditing, Task Scheduling, JWT properties binding, OpenAPI/Swagger specifications, and Redis Cache Manager serializations.
- **`common/`:** Cross-cutting base classes and core enumeration domain types. Holds the mapped superclass `BaseEntity` (providing automated timestamping) and system-wide enums (`UserRole`, `AccountStatus`, `OrderStatus`, etc.).
- **`exception/`:** Centralized error-handling infrastructure. Houses unchecked business runtime exceptions and the `@RestControllerAdvice` global error translator.
- **`security/`:** Low-level Spring Security infrastructure. Handles JWT generation and verification, security filter chains, CORS/CSRF definitions, password hashing, and Spring Security `UserDetailsService` adaptation.
- **`user/`:** Core user domain model and repository. Encapsulates identity details, hashed credentials, roles, and account statuses.
- **`auth/`:** Authentication and token lifecycle domain. Manages registration, credential login, cryptographic hashing of refresh tokens, token issuance, and token revocation.
- **`restaurant/`:** Restaurant management domain. Controls establishment profiles, ownership mappings, address details, operational status toggles (open/close/disable), and public search.
- **`menu/`:** Menu hierarchy domain. Structures dining offerings into categories and distinct food items with prices and availability flags.
- **`cart/`:** In-flight session/shopping cart domain. Enforces single-restaurant rules per cart, tracks line item quantities, and exposes clearing/modification operations.
- **`order/`:** Order lifecycle domain. Converts active carts into committed orders, snapshots unit prices and item names, tracks state changes from placement to delivery, and calculates total billings.
- **`review/`:** Customer feedback domain. Allows customers to leave ratings (1-5) and text reviews exclusively on delivered orders with a one-review-per-order constraint.
- **`admin/`:** Governance and platform moderation module. Allows system administrators to inspect accounts, disable restaurants, and view global orders.
- **`src/main/resources/`:** Contains environment and application configuration files (`application.yml`).
- **`src/test/java/`:** Test source tree. Currently an empty directory hierarchy with no test classes implemented.

---

## 4. Package-by-Package Analysis

### 1. `com.foodx`
- **Purpose:** Application bootstrapping.
- **Responsibilities:** Initializes the Spring ApplicationContext, triggers component scanning across sub-packages, and enables the declarative caching infrastructure (`@EnableCaching`).
- **Classes:** `FoodxApplication.java`
- **Dependencies:** `org.springframework.boot`, `org.springframework.cache.annotation`.
- **Relationship:** Scans all sub-packages (`com.foodx.*`).

### 2. `com.foodx.common`
- **Purpose:** Universal foundation models and status enumerations.
- **Responsibilities:** Provides base entity auditing properties (`createdAt`, `updatedAt`) and shared enumerated types.
- **Classes:**
  - `BaseEntity.java` (MappedSuperclass with `@PrePersist` and `@PreUpdate` hooks)
  - `UserRole.java` (`CUSTOMER`, `OWNER`, `ADMIN`)
  - `AccountStatus.java` (`PENDING_VERIFICATION`, `ACTIVE`, `SUSPENDED`, `DELETED`)
  - `RestaurantStatus.java` (`OPEN`, `CLOSED`, `DISABLED`)
  - `MenuItemStatus.java` (`ACTIVE`, `INACTIVE`, `DISABLED`)
  - `OrderStatus.java` (`PENDING`, `CONFIRMED`, `PREPARING`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`)
  - `ReviewStatus.java` (`PENDING`, `PUBLISHED`, `HIDDEN`)
- **Dependencies:** `jakarta.persistence`, `lombok`, `org.springframework.data.annotation`.
- **Relationship:** Imported by every domain entity and DTO package across the project.

### 3. `com.foodx.config`
- **Purpose:** Spring framework and third-party configuration.
- **Responsibilities:** Configures JPA Auditing, task scheduling, properties mapping (`jwt.*`), OpenAPI metadata with JWT Bearer scheme, and Redis Cache Manager with customized serializers.
- **Classes:**
  - `AppConfig.java`
  - `CacheNames.java`
  - `JwtProperties.java`
  - `OpenApiConfig.java`
  - `RedisConfig.java`
- **Dependencies:** Spring Context, Spring Data Redis, SpringDoc OpenAPI, Spring Boot Configuration.
- **Relationship:** Injects properties into `security`, interacts with Redis caches for `restaurant` and `menu`.

### 4. `com.foodx.exception`
- **Purpose:** Global exception translation and error response definitions.
- **Responsibilities:** Intercepts unhandled runtime exceptions and validation failures, transforming them into standardized HTTP response bodies.
- **Classes:**
  - `GlobalExceptionHandler.java` (contains `ApiError` record)
  - `BadRequestException.java` (400)
  - `ResourceNotFoundException.java` (404)
  - `UnauthorizedException.java` (401)
- **Dependencies:** `org.springframework.web`, `org.slf4j`.
- **Relationship:** Thrown by service layer components in all domain packages; captured by Spring Web MVC.

### 5. `com.foodx.security`
- **Purpose:** HTTP authentication and authorization mechanics.
- **Responsibilities:** Validates JWT access tokens on incoming requests, sets SecurityContext authentication, provides BCrypt password encoder, and defines access rule expressions.
- **Classes:**
  - `SecurityConfig.java`
  - `JwtAuthenticationFilter.java`
  - `JwtService.java`
  - `CustomUserDetailsService.java`
- **Dependencies:** `org.springframework.security`, `io.jsonwebtoken`, `com.foodx.user`, `com.foodx.config`.
- **Relationship:** Depends on `user` package to resolve user principals by email. Used across all controllers via `@PreAuthorize`.

### 6. `com.foodx.user`
- **Purpose:** User account entity and persistence.
- **Responsibilities:** Defines the `User` entity and provides persistence queries.
- **Classes:**
  - `User.java`
  - `UserRepository.java`
- **Dependencies:** `com.foodx.common`, `jakarta.persistence`, `org.springframework.data.jpa`.
- **Relationship:** Fundamental entity referenced by `auth`, `restaurant`, `cart`, `order`, `review`, and `admin`.

### 7. `com.foodx.auth`
- **Purpose:** Identity onboarding and session tokens.
- **Responsibilities:** Handles user registration, credentials verification, JWT token issuance, refresh token storage, SHA-256 token hashing, token refreshes, and revocation.
- **Classes:**
  - `AuthController.java`
  - `AuthService.java`
  - `AuthDtos.java`
  - `RefreshToken.java`
  - `RefreshTokenRepository.java`
- **Dependencies:** `com.foodx.user`, `com.foodx.security`, `com.foodx.common`, `com.foodx.exception`.
- **Relationship:** Creates `User` records; invokes `JwtService` and `AuthenticationManager`.

### 8. `com.foodx.restaurant`
- **Purpose:** Restaurant management and public discovery.
- **Responsibilities:** Creation and updates of dining establishments, city/name search, owner listing, status toggles (open/close/disable), and Redis caching.
- **Classes:**
  - `Restaurant.java`
  - `RestaurantRepository.java`
  - `RestaurantService.java`
  - `RestaurantController.java`
  - `RestaurantDtos.java`
- **Dependencies:** `com.foodx.user`, `com.foodx.common`, `com.foodx.exception`, `com.foodx.config`.
- **Relationship:** Linked to `User` (owner); referenced by `menu`, `cart`, `order`, `review`, and `admin`.

### 9. `com.foodx.menu`
- **Purpose:** Menu organization and item pricing.
- **Responsibilities:** Management of menu categories and items per restaurant, query filters, item status management, and caching.
- **Classes:**
  - `MenuCategory.java`
  - `MenuItem.java`
  - `MenuCategoryRepository.java`
  - `MenuItemRepository.java`
  - `MenuService.java`
  - `MenuController.java`
  - `MenuDtos.java`
- **Dependencies:** `com.foodx.restaurant`, `com.foodx.common`, `com.foodx.exception`.
- **Relationship:** Belongs to `Restaurant`; referenced by `cart` and `order`.

### 10. `com.foodx.cart`
- **Purpose:** Shopping cart assembly.
- **Responsibilities:** Manages active user cart, enforces single-restaurant item boundaries, modifies quantities, and purges line items.
- **Classes:**
  - `Cart.java`
  - `CartItem.java`
  - `CartRepository.java`
  - `CartService.java`
  - `CartController.java`
  - `CartDtos.java`
- **Dependencies:** `com.foodx.user`, `com.foodx.restaurant`, `com.foodx.menu`, `com.foodx.common`, `com.foodx.exception`.
- **Relationship:** Consumed by `order` when placing an order.

### 11. `com.foodx.order`
- **Purpose:** Order placement and status tracking.
- **Responsibilities:** Converts cart items into immutable order line items, calculates total price in minor currency units, assigns order tracking numbers, and transitions order statuses.
- **Classes:**
  - `FoodOrder.java`
  - `FoodOrderItem.java`
  - `FoodOrderRepository.java`
  - `FoodOrderItemRepository.java`
  - `OrderService.java`
  - `OrderController.java`
  - `OrderDtos.java`
- **Dependencies:** `com.foodx.cart`, `com.foodx.user`, `com.foodx.restaurant`, `com.foodx.menu`, `com.foodx.common`, `com.foodx.exception`.
- **Relationship:** Tied to `User`, `Restaurant`, and `Cart`; referenced by `review` and `admin`.

### 12. `com.foodx.review`
- **Purpose:** Customer feedback and rating system.
- **Responsibilities:** Captures ratings (1-5) and text reviews for delivered orders; restricts duplicate reviews per order.
- **Classes:**
  - `Review.java`
  - `ReviewRepository.java`
  - `ReviewService.java`
  - `ReviewController.java`
  - `ReviewDtos.java`
- **Dependencies:** `com.foodx.order`, `com.foodx.user`, `com.foodx.restaurant`, `com.foodx.common`, `com.foodx.exception`.
- **Relationship:** Validates against `FoodOrder` and associates feedback with `Restaurant`.

### 13. `com.foodx.admin`
- **Purpose:** System governance and administrative back-office operations.
- **Responsibilities:** Global inspection of users, restaurants, and orders; user account suspension; restaurant disabling.
- **Classes:**
  - `AdminController.java`
  - `AdminService.java`
  - `AdminDtos.java`
- **Dependencies:** `com.foodx.user`, `com.foodx.restaurant`, `com.foodx.order`, `com.foodx.common`.
- **Relationship:** Cross-cutting read/write management across `user`, `restaurant`, and `order`.

---

## 5. Module Analysis

The application is structured into eight cohesive business modules:

```
+----------------------------------------------------------------------------------------------------+
|                                         BUSINESS MODULES                                           |
+-------------------+--------------------+--------------------+--------------------+-----------------+
| Auth & Identity   | Restaurant Ops     | Menu Management    | Cart System        | Order Lifecycle |
| - AuthController  | - RestaurantCtrl   | - MenuController   | - CartController   | - OrderCtrl     |
| - AuthService     | - RestaurantServ   | - MenuService      | - CartService      | - OrderServ     |
| - RefreshToken    | - Restaurant       | - MenuCategory/Item| - Cart/CartItem    | - FoodOrder/Item|
+-------------------+--------------------+--------------------+--------------------+-----------------+
| Reviews & Ratings | Administrative     | Security & Infra                                          |
| - ReviewCtrl      | - AdminController  | - SecurityConfig, JwtService, CustomUserDetailsService     |
| - ReviewService   | - AdminService     | - RedisConfig, OpenApiConfig, GlobalExceptionHandler       |
| - Review entity   | - (Cross-domain)   | - BaseEntity, Enums                                        |
+-------------------+--------------------+-----------------------------------------------------------+
```

### Module Breakdown Matrix

| Module | Controllers | Services | Repositories | Entities | DTOs | Key Relationships |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Auth & Identity** | `AuthController` | `AuthService` | `UserRepository`, `RefreshTokenRepository` | `User`, `RefreshToken` | `RegisterRequest`, `LoginRequest`, `RefreshRequest`, `LogoutRequest`, `AuthResponse` | Creates `User`; issues JWTs. |
| **Restaurant Ops** | `RestaurantController`| `RestaurantService` | `RestaurantRepository` | `Restaurant` | `CreateRestaurantRequest`, `UpdateRestaurantRequest`, `RestaurantResponse` | Owned by `User`; holds `MenuCategory`, `MenuItem`. |
| **Menu Management**| `MenuController` | `MenuService` | `MenuCategoryRepository`, `MenuItemRepository` | `MenuCategory`, `MenuItem` | `CreateCategoryRequest`, `CategoryResponse`, `CreateItemRequest`, `UpdateItemRequest`, `ItemResponse` | Linked to `Restaurant`; items referenced in `CartItem`, `FoodOrderItem`. |
| **Cart System** | `CartController` | `CartService` | `CartRepository` | `Cart`, `CartItem` | `AddItemRequest`, `UpdateQuantityRequest`, `CartItemResponse`, `CartResponse` | Owned by `User`; binds to single `Restaurant`; contains `MenuItem`. |
| **Order Lifecycle**| `OrderController` | `OrderService` | `FoodOrderRepository`, `FoodOrderItemRepository` | `FoodOrder`, `FoodOrderItem` | `PlaceOrderRequest`, `UpdateStatusRequest`, `OrderItemResponse`, `OrderResponse` | Originates from `Cart`; binds to `User` and `Restaurant`. |
| **Reviews & Feedback**| `ReviewController`| `ReviewService` | `ReviewRepository` | `Review` | `CreateReviewRequest`, `ReviewResponse` | Bound to delivered `FoodOrder`, `User`, and `Restaurant`. |
| **Administration** | `AdminController` | `AdminService` | Directly uses `UserRepository`, `RestaurantRepository`, `FoodOrderRepository` | *None (operates on core entities)* | `UserView`, `RestaurantView`, `OrderView` | Manages `User`, `Restaurant`, `FoodOrder`. |
| **Security & Infra**| *None* | `JwtService`, `CustomUserDetailsService` | *None* | `BaseEntity` | `ApiError` | Protects controllers, parses JWTs, exposes OpenAPI & Actuator. |

---

## 6. Request Flow

The complete lifecycle of an incoming HTTP request through FoodX unfolds as follows:

```
[ HTTP Request: e.g., POST /api/cart/items with Bearer <JWT> ]
                             |
                             v
+-------------------------------------------------------------------------+
| Tomcat Web Server Connector (Port 8080)                                 |
+-------------------------------------------------------------------------+
                             |
                             v
+-------------------------------------------------------------------------+
| Filter Chain: CorsFilter -> CsrfFilter (no-op) ->                      |
| JwtAuthenticationFilter                                                 |
|  1. Extract "Authorization" header                                      |
|  2. Check for "Bearer " prefix                                          |
|  3. JwtService.extractUsername(token)                                   |
|  4. CustomUserDetailsService.loadUserByUsername(email) [SQL User Query]|
|  5. JwtService.isTokenValid(token, userDetails)                         |
|  6. SecurityContextHolder.getContext().setAuthentication(auth)          |
+-------------------------------------------------------------------------+
                             |
                             v
+-------------------------------------------------------------------------+
| DispatcherServlet                                                       |
|  - Match URL to HandlerMapping (e.g., CartController.add(...))         |
+-------------------------------------------------------------------------+
                             |
                             v
+-------------------------------------------------------------------------+
| MethodSecurityInterceptor                                               |
|  - Evaluate @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")             |
|  - If unauthorized -> throw AccessDeniedException (403)                 |
+-------------------------------------------------------------------------+
                             |
                             v
+-------------------------------------------------------------------------+
| HandlerAdapter & Argument Resolvers                                     |
|  - @RequestBody: Jackson deserialization                                |
|  - @Valid: Jakarta Validator evaluates Bean Constraints (@NotNull, etc.)|
|    - If invalid -> throw MethodArgumentNotValidException (400)         |
|  - @AuthenticationPrincipal: inject UserDetails                         |
+-------------------------------------------------------------------------+
                             |
                             v
+-------------------------------------------------------------------------+
| Controller Method Execution (e.g., CartController.add)                  |
|  - Resolve User via userRepository.findByEmail(principal.getUsername()) |
|  - Invoke Service Layer method                                          |
+-------------------------------------------------------------------------+
                             |
                             v
+-------------------------------------------------------------------------+
| Transactional Service Layer (@Transactional)                            |
|  - Open Hibernate Transaction / Acquire DB Connection                   |
|  - Check Redis Cache (if @Cacheable) or execute Domain Logic            |
|  - Call Spring Data JPA Repository methods                              |
|  - Commit Transaction / Flush Entities to DB                            |
+-------------------------------------------------------------------------+
                             |
                             v
+-------------------------------------------------------------------------+
| Controller transforms Entity -> DTO Record                              |
+-------------------------------------------------------------------------+
                             |
                             v
+-------------------------------------------------------------------------+
| DispatcherServlet & HttpMessageConverter (Jackson JSON serialization)   |
+-------------------------------------------------------------------------+
                             |
                             v
[ HTTP 200 OK / Response JSON ]
```

### Exception Path
If any layer throws an exception:
1. `ResourceNotFoundException` -> Captured by `GlobalExceptionHandler.handleNotFound` -> HTTP 404 with `ApiError`.
2. `BadRequestException` -> Captured by `GlobalExceptionHandler.handleBadRequest` -> HTTP 400 with `ApiError`.
3. `UnauthorizedException` -> Captured by `GlobalExceptionHandler.handleUnauthorized` -> HTTP 401 with `ApiError`.
4. `MethodArgumentNotValidException` -> Captured by `GlobalExceptionHandler.handleValidation` -> HTTP 400 with field-level error map in `ApiError`.
5. Any other uncaught exception -> Captured by `GlobalExceptionHandler.handleGeneric` -> Logged as error -> HTTP 500 with message `"Unexpected error occurred"`.

---

## 7. Authentication Flow

### 1. User Registration
```
Client                      AuthController              AuthService              UserRepository
  |                               |                          |                         |
  |-- POST /api/auth/register --->|                          |                         |
  |   (email, password, role)     |-- register(request) ---->|                         |
  |                               |                          |-- findByEmail(email) -->|
  |                               |                          |<-- [empty / exists] ----|
  |                               |                          |   (if exists -> 400)    |
  |                               |                          |                         |
  |                               |                          |-- BCrypt.encode(pass)   |
  |                               |                          |-- save(User) ---------->|
  |                               |                          |                         |
  |                               |                          |-- JwtService.generate   |
  |                               |                          |   AccessToken(user)     |
  |                               |                          |-- createRefreshToken(u) |
  |                               |                          |   (SHA-256 hash saved)  |
  |                               |<-- AuthResponse ---------|                         |
  |<-- 200 OK (Tokens + User) ----|                          |                         |
```

### 2. Login Flow
1. Client submits `POST /api/auth/login` with `email` and `password`.
2. `AuthService` delegates to Spring's `AuthenticationManager.authenticate(...)` via a `UsernamePasswordAuthenticationToken`.
3. `DaoAuthenticationProvider` retrieves user credentials via `CustomUserDetailsService.loadUserByUsername(...)` and verifies password match using `BCryptPasswordEncoder`.
4. If authentication passes, `AuthService` retrieves the `User` entity, verifies `user.getStatus() == AccountStatus.ACTIVE` (throws `UnauthorizedException` if not active).
5. A signed JWT Access Token (TTL: 15 minutes) is generated by `JwtService`.
6. An opaque Refresh Token is generated (`UUID:email:Instant.now()`), hashed using `SHA-256`, and persisted in the `refresh_tokens` table with an expiration timestamp (`now + 30 days`).
7. `AuthResponse` containing both tokens is returned to the client.

### 3. JWT Generation Details
- **Algorithm:** HMAC-SHA256 (`HS256`).
- **Signing Key:** Derived via `Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8))` from `jwt.secret`.
- **Subject:** `user.getEmail()`.
- **Claims:** Custom claim `"role"` storing `user.getRole().name()` (e.g., `"CUSTOMER"`).
- **IssuedAt:** Current UTC timestamp.
- **Expiration:** Current UTC timestamp + `jwt.access-token-ttl-minutes * 60` seconds.

### 4. JWT Validation & Security Filter Execution
- On every inbound request, `JwtAuthenticationFilter` intercepts the HTTP call.
- Checks `Authorization` header for `Bearer <token>`.
- Calls `jwtService.extractUsername(token)` which parses claims using the signing key.
- Loads `UserDetails` from database via `customUserDetailsService.loadUserByUsername(username)`.
- Validates that `username.equals(userDetails.getUsername())` and token expiration is strictly after current time.
- Instantiates `UsernamePasswordAuthenticationToken` with authorities `ROLE_<ROLE_NAME>` and sets it in `SecurityContextHolder.getContext()`.
- If parsing or validation fails, `SecurityContextHolder.clearContext()` is called and the filter chain continues as an anonymous request.

### 5. Refresh Token Mechanism
- Client submits `POST /api/auth/refresh` with `{ "refreshToken": "<raw_token_string>" }`.
- `AuthService` hashes the raw token with SHA-256 and searches `RefreshTokenRepository.findByTokenHash(...)`.
- Validates `!token.isRevoked()` and `token.getExpiresAt().isAfter(Instant.now())`.
- If valid, generates a new JWT access token for `token.getUser()`.
- Returns `AuthResponse` with the newly minted access token and the **same** refresh token (refresh token rotation is **not** implemented).

### 6. Logout Mechanism
- Client submits `POST /api/auth/logout` with `{ "refreshToken": "<raw_token_string>" }`.
- `AuthService` computes `SHA-256` hash of the incoming refresh token string.
- Finds matching record in `refresh_tokens` table.
- Sets `token.setRevoked(true)` and saves. Subsequent refresh requests with this token fail.
- Note: Access token blacklist does not exist; access tokens remain valid until expiration.

---

## 8. Entity Analysis

All domain entities extend `BaseEntity`, inheriting `createdAt` and `updatedAt` (`Instant`). All entities use UUID surrogate primary keys generated through `GenerationType.UUID`.

### 1. `User`
- **Table:** `users`
- **Purpose:** Represents individual identity, credentials, role, and operational account status.
- **Fields:**
  - `id` (UUID, PK)
  - `email` (String, max 255, unique, non-null)
  - `passwordHash` (String, max 255, non-null)
  - `fullName` (String, max 100, non-null)
  - `role` (`UserRole` Enum: `CUSTOMER`, `OWNER`, `ADMIN`, length 32, non-null)
  - `status` (`AccountStatus` Enum: `PENDING_VERIFICATION`, `ACTIVE`, `SUSPENDED`, `DELETED`, length 32, non-null, default `ACTIVE`)
  - `emailVerified` (boolean, non-null, default `true`)
  - `createdAt`, `updatedAt` (inherited from `BaseEntity`)
- **Indexes:** `idx_users_email` (unique), `idx_users_role`, `idx_users_status`.
- **Relationships:**
  - One-to-Many with `RefreshToken`
  - One-to-Many with `Restaurant` (as owner)
  - One-to-Many with `Cart`
  - One-to-Many with `FoodOrder`
  - One-to-Many with `Review`

### 2. `RefreshToken`
- **Table:** `refresh_tokens`
- **Purpose:** Stores cryptographically hashed refresh tokens tied to authenticated sessions.
- **Fields:**
  - `id` (UUID, PK)
  - `user_id` (UUID, FK referencing `users.id`, non-null)
  - `tokenHash` (String, max 128, unique, non-null) - SHA-256 hex string
  - `expiresAt` (Instant, non-null)
  - `revoked` (boolean, non-null, default `false`)
  - `createdAt`, `updatedAt` (inherited)
- **Indexes:** `idx_refresh_tokens_user_id`, `idx_refresh_tokens_token_hash` (unique), `idx_refresh_tokens_expires_at`.
- **Relationships:** Many-to-One with `User` (`FetchType.LAZY`).

### 3. `Restaurant`
- **Table:** `restaurants`
- **Purpose:** Dining establishment profile and business status.
- **Fields:**
  - `id` (UUID, PK)
  - `owner_id` (UUID, FK referencing `users.id`, non-null)
  - `name` (String, max 140, unique, non-null)
  - `slug` (String, max 140, non-null)
  - `description` (String, max 1000, nullable)
  - `city` (String, max 100, non-null)
  - `address` (String, max 100, non-null)
  - `status` (`RestaurantStatus` Enum: `OPEN`, `CLOSED`, `DISABLED`, length 32, non-null, default `CLOSED`)
  - `openNow` (boolean, non-null, default `false`)
  - `createdAt`, `updatedAt` (inherited)
- **Indexes:** `idx_restaurants_owner_id`, `idx_restaurants_status`, `idx_restaurants_city`.
- **Relationships:**
  - Many-to-One with `User` (`FetchType.LAZY`)
  - One-to-Many with `MenuCategory`, `MenuItem`, `FoodOrder`, `Review`.

### 4. `MenuCategory`
- **Table:** `menu_categories`
- **Purpose:** Grouping mechanism for restaurant dishes (e.g., Starters, Beverages).
- **Fields:**
  - `id` (UUID, PK)
  - `restaurant_id` (UUID, FK referencing `restaurants.id`, non-null)
  - `name` (String, max 140, non-null)
  - `slug` (String, max 140, non-null)
  - `active` (boolean, non-null, default `true`)
  - `createdAt`, `updatedAt` (inherited)
- **Indexes:** `idx_menu_categories_restaurant_id`.
- **Relationships:** Many-to-One with `Restaurant` (`FetchType.LAZY`).

### 5. `MenuItem`
- **Table:** `menu_items`
- **Purpose:** Individual dishes/products offered by a restaurant.
- **Fields:**
  - `id` (UUID, PK)
  - `restaurant_id` (UUID, FK referencing `restaurants.id`, non-null)
  - `category_id` (UUID, FK referencing `menu_categories.id`, nullable)
  - `name` (String, max 140, non-null)
  - `description` (String, max 2000, non-null)
  - `priceMinor` (long, non-null) - Price in cents/paise (avoids floating point errors)
  - `status` (`MenuItemStatus` Enum: `ACTIVE`, `INACTIVE`, `DISABLED`, length 32, non-null, default `ACTIVE`)
  - `createdAt`, `updatedAt` (inherited)
- **Indexes:** `idx_menu_items_restaurant_id`, `idx_menu_items_category_id`, `idx_menu_items_status`.
- **Relationships:**
  - Many-to-One with `Restaurant` (`FetchType.LAZY`)
  - Many-to-One with `MenuCategory` (`FetchType.LAZY`, nullable)

### 6. `Cart`
- **Table:** `carts`
- **Purpose:** Active shopping basket for a customer.
- **Fields:**
  - `id` (UUID, PK)
  - `user_id` (UUID, FK referencing `users.id`, non-null)
  - `restaurant_id` (UUID, FK referencing `restaurants.id`, nullable)
  - `active` (boolean, non-null, default `true`)
  - `createdAt`, `updatedAt` (inherited)
- **Indexes:** `idx_carts_user_id`, `idx_carts_restaurant_id`.
- **Relationships:**
  - Many-to-One with `User` (`FetchType.LAZY`)
  - Many-to-One with `Restaurant` (`FetchType.LAZY`, nullable)
  - One-to-Many with `CartItem` (`mappedBy = "cart"`, `orphanRemoval = true`)

### 7. `CartItem`
- **Table:** `cart_items`
- **Purpose:** Specific menu item entry and quantity within a cart.
- **Fields:**
  - `id` (UUID, PK)
  - `cart_id` (UUID, FK referencing `carts.id`, non-null)
  - `menu_item_id` (UUID, FK referencing `menu_items.id`, non-null)
  - `quantity` (int, non-null)
  - `createdAt`, `updatedAt` (inherited)
- **Indexes:** `idx_cart_items_cart_id`, `idx_cart_items_menu_item_id`.
- **Relationships:**
  - Many-to-One with `Cart` (`FetchType.LAZY`)
  - Many-to-One with `MenuItem` (`FetchType.LAZY`)

### 8. `FoodOrder`
- **Table:** `orders`
- **Purpose:** Placed order tracking financial total, restaurant, items, and state.
- **Fields:**
  - `id` (UUID, PK)
  - `orderNumber` (String, unique, non-null) - Generated as `"FX-" + System.currentTimeMillis()`
  - `user_id` (UUID, FK referencing `users.id`, non-null)
  - `restaurant_id` (UUID, FK referencing `restaurants.id`, non-null)
  - `cart_id` (UUID, FK referencing `carts.id`, nullable)
  - `status` (`OrderStatus` Enum: `PENDING`, `CONFIRMED`, `PREPARING`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`, length 32, non-null, default `PENDING`)
  - `totalMinor` (long, non-null) - Total price in minor units
  - `createdAt`, `updatedAt` (inherited)
- **Indexes:** `idx_orders_user_id`, `idx_orders_restaurant_id`, `idx_orders_status`.
- **Relationships:**
  - Many-to-One with `User`, `Restaurant`, `Cart` (`FetchType.LAZY`)
  - One-to-Many with `FoodOrderItem` (`mappedBy = "order"`, `orphanRemoval = true`)
  - One-to-One / One-to-Many with `Review`

### 9. `FoodOrderItem`
- **Table:** `order_items`
- **Purpose:** Immutable snapshot of an ordered dish, capturing price at order time.
- **Fields:**
  - `id` (UUID, PK)
  - `order_id` (UUID, FK referencing `orders.id`, non-null)
  - `menu_item_id` (UUID, FK referencing `menu_items.id`, nullable)
  - `itemName` (String, non-null) - Copied from `menu_items.name`
  - `quantity` (int, non-null)
  - `unitPriceMinor` (long, non-null) - Unit price captured at purchase
  - `lineTotalMinor` (long, non-null) - `unitPriceMinor * quantity`
  - `createdAt`, `updatedAt` (inherited)
- **Indexes:** `idx_order_items_order_id`.
- **Relationships:**
  - Many-to-One with `FoodOrder` (`FetchType.LAZY`)
  - Many-to-One with `MenuItem` (`FetchType.LAZY`, nullable)

### 10. `Review`
- **Table:** `reviews`
- **Purpose:** Customer evaluation of a completed order and restaurant.
- **Fields:**
  - `id` (UUID, PK)
  - `order_id` (UUID, FK referencing `orders.id`, unique, non-null)
  - `user_id` (UUID, FK referencing `users.id`, non-null)
  - `restaurant_id` (UUID, FK referencing `restaurants.id`, non-null)
  - `rating` (int, non-null) - Validated 1 to 5 via DTO
  - `comment` (String, max 2000, nullable)
  - `status` (`ReviewStatus` Enum: `PENDING`, `PUBLISHED`, `HIDDEN`, length 32, non-null, default `PENDING`; set to `PUBLISHED` on creation)
  - `createdAt`, `updatedAt` (inherited)
- **Indexes:** `idx_reviews_order_id`, `idx_reviews_restaurant_id`, `idx_reviews_user_id`.
- **Relationships:**
  - Many-to-One with `FoodOrder` (`FetchType.LAZY`, unique join column)
  - Many-to-One with `User` (`FetchType.LAZY`)
  - Many-to-One with `Restaurant` (`FetchType.LAZY`)

### Textual ER Diagram

```
 +--------------------+             +------------------------+
 |       users        |1           *|     refresh_tokens     |
 +--------------------+-------------+------------------------+
 | PK  id             |             | PK  id                 |
 |     email (UQ)     |             | FK  user_id            |
 |     password_hash  |             |     token_hash (UQ)    |
 |     full_name      |             |     expires_at         |
 |     role           |             |     revoked            |
 |     status         |             |     created_at         |
 |     email_verified |             +------------------------+
 |     created_at     |
 +--------------------+
      | 1            | 1                       | 1
      |              |                         |
      | * (owner)    | *                       | *
 +--------------------+  |                  +--------------------+
 |    restaurants     |  |                  |       carts        |
 +--------------------+  |                  +--------------------+
 | PK  id             |  |                  | PK  id             |
 | FK  owner_id       |  |                  | FK  user_id        |
 |     name (UQ)      |  |                  | FK  restaurant_id  |
 |     slug           |  |                  |     active         |
 |     city           |  |                  |     created_at       |
 |     status         |  |                  +--------------------+
 |     open_now       |  |                            | 1
 +--------------------+  |                            |
      | 1          | 1   |                            | *
      |            |     |                  +--------------------+
      | *          | *   |                  |     cart_items     |
 +------------+ +------------+              +--------------------+
 |menu_categor| | menu_items |              | PK  id             |
 +------------+ +------------+              | FK  cart_id        |
 | PK  id     |1| PK  id     |              | FK  menu_item_id   |
 | FK  rest_id| | FK  rest_id|              |     quantity       |
 |     name   |-| FK  cat_id |              +--------------------+
 |     slug   |*|     name   |                       | *
 +------------+ | price_minor|                       |
                +------------+                       |
                      | 1                            |
                      | *                            | (placed into)
                +--------------------+               |
                |    order_items     |               v
                +--------------------+      +--------------------+
                | PK  id             |*    1|       orders       |
                | FK  order_id       |------+--------------------+
                | FK  menu_item_id   |      | PK  id             |
                |     item_name      |      |     order_number(UQ|
                |     unit_price_min |      | FK  user_id        |
                |     line_total_min |      | FK  restaurant_id  |
                +--------------------+      | FK  cart_id        |
                                            |     status         |
                                            |     total_minor    |
                                            +--------------------+
                                                      | 1
                                                      |
                                                      | 1 (unique)
                                            +--------------------+
                                            |      reviews       |
                                            +--------------------+
                                            | PK  id             |
                                            | FK  order_id (UQ)  |
                                            | FK  user_id        |
                                            | FK  restaurant_id  |
                                            |     rating         |
                                            |     comment        |
                                            |     status         |
                                            +--------------------+
```

---

## 9. Database Analysis

### Physical Table Catalog

#### 1. `users`
- **PK:** `id` (`UUID`)
- **Foreign Keys:** None
- **Unique Constraints:** `UK_users_email` (`email`)
- **Indexes:**
  - `idx_users_email` on (`email`) [UNIQUE]
  - `idx_users_role` on (`role`)
  - `idx_users_status` on (`status`)
- **Cascade Rules:** None configured at JPA level.

#### 2. `refresh_tokens`
- **PK:** `id` (`UUID`)
- **Foreign Keys:** `FK_refresh_tokens_user` (`user_id` -> `users.id`)
- **Unique Constraints:** `UK_refresh_tokens_token_hash` (`tokenHash`)
- **Indexes:**
  - `idx_refresh_tokens_token_hash` on (`tokenHash`) [UNIQUE]
  - `idx_refresh_tokens_user_id` on (`user_id`)
  - `idx_refresh_tokens_expires_at` on (`expiresAt`)
- **Cascade Rules:** Default JPA (RESTRICT/NO ACTION).

#### 3. `restaurants`
- **PK:** `id` (`UUID`)
- **Foreign Keys:** `FK_restaurants_owner` (`owner_id` -> `users.id`)
- **Unique Constraints:** `UK_restaurants_name` (`name`)
- **Indexes:**
  - `idx_restaurants_owner_id` on (`owner_id`)
  - `idx_restaurants_status` on (`status`)
  - `idx_restaurants_city` on (`city`)
- **Cascade Rules:** Default JPA.

#### 4. `menu_categories`
- **PK:** `id` (`UUID`)
- **Foreign Keys:** `FK_menu_categories_restaurant` (`restaurant_id` -> `restaurants.id`)
- **Unique Constraints:** None
- **Indexes:** `idx_menu_categories_restaurant_id` on (`restaurant_id`)
- **Cascade Rules:** Default JPA.

#### 5. `menu_items`
- **PK:** `id` (`UUID`)
- **Foreign Keys:**
  - `FK_menu_items_restaurant` (`restaurant_id` -> `restaurants.id`)
  - `FK_menu_items_category` (`category_id` -> `menu_categories.id`)
- **Unique Constraints:** None
- **Indexes:**
  - `idx_menu_items_restaurant_id` on (`restaurant_id`)
  - `idx_menu_items_category_id` on (`category_id`)
  - `idx_menu_items_status` on (`status`)
- **Cascade Rules:** Default JPA.

#### 6. `carts`
- **PK:** `id` (`UUID`)
- **Foreign Keys:**
  - `FK_carts_user` (`user_id` -> `users.id`)
  - `FK_carts_restaurant` (`restaurant_id` -> `restaurants.id`)
- **Unique Constraints:** None
- **Indexes:**
  - `idx_carts_user_id` on (`user_id`)
  - `idx_carts_restaurant_id` on (`restaurant_id`)
- **Cascade Rules:** `orphanRemoval = true` on `items` collection (`cart_items`).

#### 7. `cart_items`
- **PK:** `id` (`UUID`)
- **Foreign Keys:**
  - `FK_cart_items_cart` (`cart_id` -> `carts.id`)
  - `FK_cart_items_menu_item` (`menu_item_id` -> `menu_items.id`)
- **Unique Constraints:** None (Composite unique constraint on `cart_id + menu_item_id` is missing in database schema; uniqueness enforced via Java stream filtering in `CartService.addItem`).
- **Indexes:**
  - `idx_cart_items_cart_id` on (`cart_id`)
  - `idx_cart_items_menu_item_id` on (`menu_item_id`)
- **Cascade Rules:** Handled via orphan removal from parent `Cart`.

#### 8. `orders`
- **PK:** `id` (`UUID`)
- **Foreign Keys:**
  - `FK_orders_user` (`user_id` -> `users.id`)
  - `FK_orders_restaurant` (`restaurant_id` -> `restaurants.id`)
  - `FK_orders_cart` (`cart_id` -> `carts.id`)
- **Unique Constraints:** `UK_orders_order_number` (`orderNumber`)
- **Indexes:**
  - `idx_orders_user_id` on (`user_id`)
  - `idx_orders_restaurant_id` on (`restaurant_id`)
  - `idx_orders_status` on (`status`)
- **Cascade Rules:** `orphanRemoval = true` on `items` collection (`order_items`).

#### 9. `order_items`
- **PK:** `id` (`UUID`)
- **Foreign Keys:**
  - `FK_order_items_order` (`order_id` -> `orders.id`)
  - `FK_order_items_menu_item` (`menu_item_id` -> `menu_items.id`)
- **Unique Constraints:** None
- **Indexes:** `idx_order_items_order_id` on (`order_id`)
- **Cascade Rules:** Handled via parent `FoodOrder`.

#### 10. `reviews`
- **PK:** `id` (`UUID`)
- **Foreign Keys:**
  - `FK_reviews_order` (`order_id` -> `orders.id`) [UNIQUE]
  - `FK_reviews_user` (`user_id` -> `users.id`)
  - `FK_reviews_restaurant` (`restaurant_id` -> `restaurants.id`)
- **Unique Constraints:** Unique key on `order_id` column.
- **Indexes:**
  - `idx_reviews_order_id` on (`order_id`)
  - `idx_reviews_restaurant_id` on (`restaurant_id`)
  - `idx_reviews_user_id` on (`user_id`)
- **Cascade Rules:** Default JPA.

---

## 10. API Analysis

The system exposes 25 application REST endpoints across 7 controllers, plus Actuator and Swagger interfaces:

| Controller | Method | URL | Authentication Required | Roles Permitted | Request DTO | Response DTO | Expected Status Codes | Validation Rules |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **AuthController** | `POST` | `/api/auth/register` | No | Anonymous | `RegisterRequest` | `AuthResponse` | 200, 400 | `@Email`, `@NotBlank` email/pwd/name, `@NotNull` role |
| | `POST` | `/api/auth/login` | No | Anonymous | `LoginRequest` | `AuthResponse` | 200, 400, 401 | `@Email`, `@NotBlank` email/pwd |
| | `POST` | `/api/auth/refresh` | No | Anonymous | `RefreshRequest` | `AuthResponse` | 200, 400, 401 | `@NotBlank` refreshToken |
| | `POST` | `/api/auth/logout` | No | Anonymous | `LogoutRequest` | *Empty / Void* | 200, 400, 401 | `@NotBlank` refreshToken |
| **RestaurantController** | `POST` | `/api/restaurants` | Yes | `OWNER`, `ADMIN` | `CreateRestaurantRequest` | `RestaurantResponse` | 200, 400, 401, 403, 404 | `@NotBlank`, `@Size(max=140)` name/slug, `@Size(max=1000)` desc |
| | `GET` | `/api/restaurants/{id}` | No | Public | *None* | `RestaurantResponse` | 200, 404 | UUID in path |
| | `GET` | `/api/restaurants` | No | Public | *None* (Query: `q`) | `List<RestaurantResponse>` | 200 | Optional query parameter |
| | `GET` | `/api/restaurants/owner/mine` | Yes | `OWNER`, `ADMIN` | *None* | `List<RestaurantResponse>` | 200, 401, 403 | Principal extracted from JWT |
| | `PUT` | `/api/restaurants/{id}` | Yes | `OWNER`, `ADMIN` | `UpdateRestaurantRequest` | `RestaurantResponse` | 200, 400, 401, 403, 404 | Validation identical to Create |
| | `POST` | `/api/restaurants/{id}/open` | Yes | `OWNER`, `ADMIN` | *None* | `RestaurantResponse` | 200, 401, 403, 404 | UUID in path |
| | `POST` | `/api/restaurants/{id}/close`| Yes | `OWNER`, `ADMIN` | *None* | `RestaurantResponse` | 200, 401, 403, 404 | UUID in path |
| **MenuController** | `POST` | `/api/restaurants/{restaurantId}/menu/categories` | Yes | `OWNER`, `ADMIN` | `CreateCategoryRequest` | `CategoryResponse` | 200, 400, 401, 403, 404 | `@NotBlank`, `@Size(max=140)` name/slug |
| | `POST` | `/api/restaurants/{restaurantId}/menu/items` | Yes | `OWNER`, `ADMIN` | `CreateItemRequest` | `ItemResponse` | 200, 400, 401, 403, 404 | `@NotNull` categoryId, `@Min(0)` priceMinor, `@Size(max=2000)` desc |
| | `PUT` | `/api/restaurants/{restaurantId}/menu/items/{itemId}` | Yes | `OWNER`, `ADMIN` | `UpdateItemRequest` | `ItemResponse` | 200, 400, 401, 403, 404 | CreateItem constraints + `@NotNull` status |
| | `POST` | `/api/restaurants/{restaurantId}/menu/items/{itemId}/disable`| Yes | `OWNER`, `ADMIN` | *None* | `ItemResponse` | 200, 401, 403, 404 | UUIDs in path |
| | `GET` | `/api/restaurants/{restaurantId}/menu/categories` | No | Public | *None* | `List<CategoryResponse>` | 200, 404 | Restaurant UUID in path |
| | `GET` | `/api/restaurants/{restaurantId}/menu/items` | No | Public | *None* (Query: `q`) | `List<ItemResponse>` | 200, 404 | Optional query parameter |
| **CartController** | `GET` | `/api/cart` | Yes | `CUSTOMER`, `ADMIN` | *None* | `CartResponse` | 200, 401, 403 | Principal from JWT |
| | `POST` | `/api/cart/items` | Yes | `CUSTOMER`, `ADMIN` | `AddItemRequest` | `CartResponse` | 200, 400, 401, 403, 404 | `@NotNull` restaurantId/menuItemId, `@Min(1)` quantity |
| | `PUT` | `/api/cart/items/{cartItemId}`| Yes | `CUSTOMER`, `ADMIN` | `UpdateQuantityRequest` | `CartResponse` | 200, 400, 401, 403, 404 | `@Min(1)` quantity |
| | `DELETE`| `/api/cart/items/{cartItemId}`| Yes | `CUSTOMER`, `ADMIN` | *None* | `CartResponse` | 200, 401, 403, 404 | UUID in path |
| | `DELETE`| `/api/cart` | Yes | `CUSTOMER`, `ADMIN` | *None* | `CartResponse` | 200, 401, 403 | Principal from JWT |
| **OrderController** | `POST` | `/api/orders` | Yes | `CUSTOMER`, `ADMIN` | `PlaceOrderRequest` | `OrderResponse` | 200, 400, 401, 403, 404 | `@NotNull` cartId |
| | `GET` | `/api/orders` | Yes | `CUSTOMER`, `ADMIN` | *None* | `List<OrderResponse>` | 200, 401, 403 | History sorted by `createdAt DESC` |
| | `POST` | `/api/orders/{orderId}/cancel` | Yes | `CUSTOMER`, `ADMIN` | *None* | `OrderResponse` | 200, 400, 401, 403, 404 | Checks final state (`DELIVERED`/`CANCELLED`) |
| | `PUT` | `/api/orders/{orderId}/status` | Yes | `OWNER`, `ADMIN` | `UpdateStatusRequest` | `OrderResponse` | 200, 400, 401, 403, 404 | `@NotNull` status |
| **ReviewController** | `POST` | `/api/reviews` | Yes | `CUSTOMER`, `ADMIN` | `CreateReviewRequest` | `ReviewResponse` | 200, 400, 401, 403, 404 | `@NotNull` orderId, `@Min(1) @Max(5)` rating, `@NotBlank` comment |
| | `GET` | `/api/reviews/restaurant/{restaurantId}` | Yes *(Config mismatch: permitted in controller, blocked in SecurityConfig)* | Authenticated | *None* | `List<ReviewResponse>` | 200, 401, 404 | Restaurant UUID in path |
| | `GET` | `/api/reviews/me` | Yes | `CUSTOMER`, `ADMIN` | *None* | `List<ReviewResponse>` | 200, 401, 403 | Principal from JWT |
| **AdminController** | `GET` | `/api/admin/users` | Yes | `ADMIN` | *None* | `List<UserView>` | 200, 401, 403 | Loads all users (unpaged) |
| | `POST` | `/api/admin/users/{userId}/suspend` | Yes | `ADMIN` | *None* | *Empty / Void* | 200, 401, 403 | User UUID in path |
| | `GET` | `/api/admin/restaurants` | Yes | `ADMIN` | *None* | `List<RestaurantView>` | 200, 401, 403 | Loads all restaurants (unpaged) |
| | `POST` | `/api/admin/restaurants/{restaurantId}/disable` | Yes | `ADMIN` | *None* | *Empty / Void* | 200, 401, 403 | Restaurant UUID in path |
| | `GET` | `/api/admin/orders` | Yes | `ADMIN` | *None* | `List<OrderView>` | 200, 401, 403 | Loads all orders (unpaged) |

---

## 11. Service Layer Analysis

All service classes are annotated with class-level `@Transactional`. Read-only queries override this with `@Transactional(readOnly = true)`.

### 1. `AuthService`
- **Dependencies:** `UserRepository`, `RefreshTokenRepository`, `AuthenticationManager`, `PasswordEncoder`, `JwtService`.
- **Business Logic:**
  - `register`: Checks email uniqueness. Stores lowercased email. Encodes password using BCrypt. Generates access JWT and SHA-256 hashed refresh token. Sets `status = ACTIVE` and `emailVerified = true`.
  - `login`: Calls `authenticationManager.authenticate`. Validates account status (`AccountStatus.ACTIVE`). Issues fresh access and refresh tokens.
  - `refresh`: Computes SHA-256 hash of incoming refresh token, queries `RefreshTokenRepository`. Verifies `!isRevoked()` and expiration date. Issues new access token without rotating refresh token.
  - `logout`: Locates token by hash and sets `revoked = true`.
- **Complexity:** Low-Medium (Cryptographic hashing, token expiration checks).

### 2. `RestaurantService`
- **Dependencies:** `RestaurantRepository`, `UserRepository`.
- **Business Logic:**
  - `create`: Resolves owner entity, creates restaurant in `CLOSED` status with `openNow = false`.
  - `get`: Cached via `@Cacheable("restaurantDetails", key = "#id")`.
  - `search`: Cached via `@Cacheable("restaurantList", key = "#search")`. If query string is blank, invokes `findAll()`; otherwise runs case-insensitive substring search on name and city.
  - `listByOwner`: Returns establishments belonging to specified owner.
  - `update`: Mutates name, slug, description, city, address.
  - `open`, `close`, `disable`: Mutates `status` and `openNow` flag. Clears all entries in `restaurantDetails`, `restaurantList`, and `popularRestaurants` caches via `@CacheEvict`.
- **Complexity:** Low (CRUD operations with cache integration).

### 3. `MenuService`
- **Dependencies:** `MenuCategoryRepository`, `MenuItemRepository`, `RestaurantRepository`.
- **Business Logic:**
  - `createCategory`: Binds new `MenuCategory` to restaurant.
  - `createItem`: Binds dish to restaurant and category; initializes in `ACTIVE` status.
  - `updateItem`: Mutates name, description, priceMinor, category, and status.
  - `disableItem`: Sets status to `INACTIVE`.
  - `listCategories`: Cached under `menuByRestaurant` keyed by `#restaurantId`.
  - `listItems`: Cached under `menuItemsByRestaurant` keyed by `#restaurantId + ':' + #q`. Performs **in-memory filtering** if `q` parameter is present.
  - `evictRestaurantMenuCache`: Empty method annotated with `@CacheEvict` (never called internally by mutation methods).
- **Complexity:** Low.

### 4. `CartService`
- **Dependencies:** `CartRepository`, `UserRepository`, `RestaurantRepository`, `MenuItemRepository`.
- **Business Logic:**
  - `getCart`: Resolves active cart for user, or instantiates an empty one if none exists.
  - `addItem`: Enforces single-restaurant cart constraint (`cart.getRestaurant().getId().equals(restaurant.getId())`). If an item already exists in cart, increments quantity; otherwise appends new `CartItem`.
  - `updateQuantity`: Updates quantity for an existing cart item.
  - `removeItem`: Removes specific line item by ID from collection.
  - `clear`: Purges all line items from cart.
- **Complexity:** Medium (Collection management, constraint validation).

### 5. `OrderService`
- **Dependencies:** `FoodOrderRepository`, `FoodOrderItemRepository`, `CartRepository`, `UserRepository`.
- **Business Logic:**
  - `placeOrder`: Verifies cart ownership and non-empty status. Instantiates `FoodOrder` with order number `"FX-" + System.currentTimeMillis()`. Computes `totalMinor` by summing item totals (`priceMinor * quantity`). Copies cart items into `FoodOrderItem` records, clearing the user's active cart.
  - `history`: Returns user's orders sorted by `createdAt DESC`.
  - `restaurantOrders`: Returns restaurant's orders sorted by `createdAt DESC` (orphaned; not invoked by any controller).
  - `updateStatus`: Transitions order status. Blocks modification if current status is `CANCELLED` or `DELIVERED`.
  - `cancel`: Convenience method transitioning status to `CANCELLED`.
- **Complexity:** Medium-High (State management, line-item price freezing, multi-repository coordination).

### 6. `ReviewService`
- **Dependencies:** `ReviewRepository`, `FoodOrderRepository`, `UserRepository`, `RestaurantRepository`.
- **Business Logic:**
  - `create`: Enforces that the order belongs to the calling user, order status is strictly `DELIVERED`, and no existing review exists for the order. Sets review status directly to `PUBLISHED`.
  - `restaurantReviews`: Lists reviews for a restaurant.
  - `myReviews`: Lists reviews left by a user.
- **Complexity:** Low-Medium (Validation rules).

### 7. `AdminService`
- **Dependencies:** `UserRepository`, `RestaurantRepository`, `FoodOrderRepository`.
- **Business Logic:**
  - `users`: Unpaged `findAll()` mapped to `UserView`.
  - `suspendUser`: Sets user status to `SUSPENDED`.
  - `restaurants`: Unpaged `findAll()` mapped to `RestaurantView`.
  - `disableRestaurant`: Sets restaurant status to `DISABLED` and `openNow = false`. (Directly saves via `RestaurantRepository`, skipping Redis cache eviction).
  - `orders`: Unpaged `findAll()` mapped to `OrderView`.
- **Complexity:** Low (Administrative CRUD).

---

## 12. Repository Analysis

All repositories inherit from `org.springframework.data.jpa.repository.JpaRepository`.

| Repository Interface | Entity Managed | Key Type | Declared Query Methods | Query Type |
| :--- | :--- | :--- | :--- | :--- |
| **`UserRepository`** | `User` | `UUID` | `findByEmail(String email)` | Derived Method |
| | | | `findByRole(UserRole role)` | Derived Method |
| | | | `findByStatus(AccountStatus status)` | Derived Method |
| **`RefreshTokenRepository`** | `RefreshToken` | `UUID` | `findByTokenHash(String tokenHash)` | Derived Method |
| | | | `findAllByUserAndRevokedFalse(User user)` | Derived Method |
| **`RestaurantRepository`** | `Restaurant` | `UUID` | `findByOwner(User owner)` | Derived Method |
| | | | `findByStatus(RestaurantStatus status)` | Derived Method |
| | | | `findBySlug(String slug)` | Derived Method |
| | | | `findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(String name, String city)` | Derived Method |
| **`MenuCategoryRepository`**| `MenuCategory` | `UUID` | `findByRestaurant(Restaurant restaurant)` | Derived Method |
| **`MenuItemRepository`** | `MenuItem` | `UUID` | `findByRestaurant(Restaurant restaurant)` | Derived Method |
| | | | `findByRestaurantAndStatus(Restaurant restaurant, MenuItemStatus status)` | Derived Method |
| | | | `findByNameContainingIgnoreCase(String name)` | Derived Method |
| **`CartRepository`** | `Cart` | `UUID` | `findByUserAndActiveTrue(User user)` | Derived Method |
| | | | `findByUserAndRestaurantAndActiveTrue(User user, Restaurant restaurant)` | Derived Method |
| **`FoodOrderRepository`** | `FoodOrder` | `UUID` | `findByUserOrderByCreatedAtDesc(User user)` | Derived Method (Order By) |
| | | | `findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId)` | Derived Method (Order By) |
| | | | `findByOrderNumber(String orderNumber)` | Derived Method |
| | | | `findByStatus(OrderStatus status)` | Derived Method |
| **`FoodOrderItemRepository`**| `FoodOrderItem` | `UUID` | *Inherits standard CRUD only* | Built-in |
| **`ReviewRepository`** | `Review` | `UUID` | `findByOrder(FoodOrder order)` | Derived Method |
| | | | `findByRestaurant(Restaurant restaurant)` | Derived Method |
| | | | `findByUser(User user)` | Derived Method |

### Key Observations
- **Custom JPQL / Native Queries:** None. Zero `@Query` annotations exist in the codebase.
- **Specifications / QueryDSL:** None. No dynamic criteria queries are implemented.
- **Pagination & Sorting:** None of the repository methods accept `Pageable` or `Sort` parameters. All lists return full collections into memory (`List<T>`).

---

## 13. DTO Analysis

All DTOs are declared as immutable Java `record` structures defined inside final container utility classes.

### Request DTOs
1. **`AuthDtos.RegisterRequest`:** `(String email, String password, String fullName, UserRole role)`
2. **`AuthDtos.LoginRequest`:** `(String email, String password)`
3. **`AuthDtos.RefreshRequest`:** `(String refreshToken)`
4. **`AuthDtos.LogoutRequest`:** `(String refreshToken)`
5. **`RestaurantDtos.CreateRestaurantRequest`:** `(String name, String slug, String description, String city, String address)`
6. **`RestaurantDtos.UpdateRestaurantRequest`:** `(String name, String slug, String description, String city, String address)`
7. **`MenuDtos.CreateCategoryRequest`:** `(String name, String slug)`
8. **`MenuDtos.CreateItemRequest`:** `(UUID categoryId, String name, String description, long priceMinor)`
9. **`MenuDtos.UpdateItemRequest`:** `(UUID categoryId, String name, String description, long priceMinor, MenuItemStatus status)`
10. **`CartDtos.AddItemRequest`:** `(UUID restaurantId, UUID menuItemId, int quantity)`
11. **`CartDtos.UpdateQuantityRequest`:** `(int quantity)`
12. **`OrderDtos.PlaceOrderRequest`:** `(UUID cartId)`
13. **`OrderDtos.UpdateStatusRequest`:** `(OrderStatus status)`
14. **`ReviewDtos.CreateReviewRequest`:** `(UUID orderId, int rating, String comment)`

### Response DTOs
1. **`AuthDtos.AuthResponse`:** `(UUID userId, String email, String fullName, String role, String accessToken, String refreshToken)`
2. **`RestaurantDtos.RestaurantResponse`:** `(UUID id, UUID ownerId, String name, String slug, String description, String city, String address, RestaurantStatus status, boolean openNow)`
3. **`MenuDtos.CategoryResponse`:** `(UUID id, UUID restaurantId, String name, String slug, boolean active)`
4. **`MenuDtos.ItemResponse`:** `(UUID id, UUID restaurantId, UUID categoryId, String name, String description, long priceMinor, MenuItemStatus status)`
5. **`CartDtos.CartItemResponse`:** `(UUID id, UUID menuItemId, String menuItemName, int quantity)`
6. **`CartDtos.CartResponse`:** `(UUID id, UUID restaurantId, UUID userId, List<CartItemResponse> items)`
7. **`OrderDtos.OrderItemResponse`:** `(UUID id, UUID menuItemId, String itemName, int quantity, long unitPriceMinor, long lineTotalMinor)`
8. **`OrderDtos.OrderResponse`:** `(UUID id, String orderNumber, UUID userId, UUID restaurantId, OrderStatus status, long totalMinor, List<OrderItemResponse> items)`
9. **`ReviewDtos.ReviewResponse`:** `(UUID id, UUID orderId, UUID userId, UUID restaurantId, int rating, String comment, ReviewStatus status)`
10. **`AdminDtos.UserView`:** `(UUID id, String email, String fullName, String role, AccountStatus status)`
11. **`AdminDtos.RestaurantView`:** `(UUID id, String name, String city, RestaurantStatus status)`
12. **`AdminDtos.OrderView`:** `(UUID id, String orderNumber, OrderStatus status, long totalMinor)`
13. **`GlobalExceptionHandler.ApiError`:** `(Instant timestamp, int status, String message, Map<String, String> errors)`

### Mapping Strategy
Although `pom.xml` imports MapStruct (`org.mapstruct:mapstruct:1.6.3`) and configures `mapstruct-processor` in the Maven compiler plugin, **MapStruct is not used anywhere in the codebase**. There are zero `@Mapper` interfaces. All entity-to-DTO transformations are written manually as private helper methods inside service classes (`toResponse`, `toCategoryResponse`, `toItemResponse`) or as lambda expressions within Java Stream `.map()` pipelines.

---

## 14. Security Analysis

### Current Security Architecture
- **Stateless HTTP:** Session policy is configured as `SessionCreationPolicy.STATELESS`.
- **CSRF:** Explicitly disabled via `csrf -> csrf.disable()`.
- **CORS:** Configured via `.cors(Customizer.withDefaults())`. No `CorsConfigurationSource` bean is declared in `SecurityConfig.java`, meaning default Spring MVC CORS rules apply (external origins are blocked by default unless configured externally).
- **Password Hashing:** Uses `BCryptPasswordEncoder` with default strength (10 rounds).
- **Security Context:** Propagated per-request by `JwtAuthenticationFilter` via `UsernamePasswordAuthenticationToken`.

### Role Hierarchy
There is **no** `RoleHierarchy` bean defined. Roles are flat strings: `ROLE_CUSTOMER`, `ROLE_OWNER`, `ROLE_ADMIN`. Because there is no hierarchy, controllers must explicitly list multiple roles in annotations, e.g., `@PreAuthorize("hasAnyRole('OWNER','ADMIN')")` or `@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")`.

### Security Endpoint Rules Summary

```
PUBLIC (PermitAll via SecurityConfig):
  - /api/auth/**
  - /swagger-ui/**, /swagger-ui.html
  - /api-docs/**, /v3/api-docs/**
  - /actuator/health
  - GET /api/restaurants/**
  - GET /api/menu/** (Dead pattern; actual menu path is /api/restaurants/{id}/menu/**)

AUTHENTICATED (anyRequest().authenticated()):
  - /api/cart/**
  - /api/orders/**
  - /api/reviews/**
  - /api/admin/**
  - POST/PUT on /api/restaurants/**
  - /actuator/info, /actuator/metrics
```

### Identified Security Vulnerabilities & Implementation Quirks

1. **Broken Object Level Authorization (BOLA / IDOR) on Restaurants:**
   - In `RestaurantController.update`, `open`, and `close`, the endpoint is protected by `@PreAuthorize("hasAnyRole('OWNER','ADMIN')")`.
   - Neither the controller nor `RestaurantService` verifies that the authenticated user (`principal`) is the actual `owner` of the target restaurant (`restaurant.getOwner()`). Any user with role `OWNER` can modify, open, or close any other owner's restaurant.

2. **BOLA / IDOR on Menu Management:**
   - In `MenuController`, methods `createCategory`, `createItem`, `updateItem`, and `disableItem` check only `@PreAuthorize("hasAnyRole('OWNER','ADMIN')")`.
   - The code does not verify that the target `restaurantId` belongs to the authenticated user. Any restaurant owner can inject menu items into competing restaurants.

3. **BOLA / IDOR on Order Cancellation:**
   - In `OrderController.cancel` (`POST /api/orders/{orderId}/cancel`), the endpoint requires `@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")`.
   - `OrderService.cancel` modifies the status of `orderId` without validating that `order.getUser().getId()` matches the authenticated customer's ID. Any customer can cancel any other customer's order by providing the UUID.

4. **BOLA / IDOR on Order Status Updates:**
   - In `OrderController.updateStatus` (`PUT /api/orders/{orderId}/status`), the endpoint allows any `OWNER` to transition the status of any order across the entire platform, without checking if the order belongs to their restaurant.

5. **Security Configuration Discrepancy on Public Reviews:**
   - In `ReviewController`, `GET /api/reviews/restaurant/{restaurantId}` has `@PreAuthorize("permitAll()")`.
   - However, `SecurityConfig` specifies `.anyRequest().authenticated()`, and does **not** permit `/api/reviews/**`.
   - As a result, the HTTP filter chain rejects unauthenticated requests with a 401/403 before they ever reach the controller method.

6. **Redundant Database Queries Per Request:**
   - For every protected request, `JwtAuthenticationFilter` executes `CustomUserDetailsService.loadUserByUsername(...)` (triggering `userRepository.findByEmail`).
   - Subsequently, in controller helper methods (`resolveUser(principal)`), `userRepository.findByEmail` is called a second time to obtain the `User` entity UUID. This results in two identical DB queries per authenticated HTTP request.

7. **Default Secret in Configuration:**
   - `application.yml` contains a fallback default secret: `"CHANGE_ME_TO_A_LONG_RANDOM_SECRET_CHANGE_ME_TO_A_LONG_RANDOM_SECRET"`. If deployed without setting the `JWT_SECRET` environment variable, the system uses this known key.

---

## 15. Configuration Analysis

### Configuration Classes

#### 1. `AppConfig`
- Annotations: `@Configuration`, `@EnableScheduling`, `@EnableJpaAuditing`, `@EnableConfigurationProperties(JwtProperties.class)`.
- Purpose: Activates Spring background task scheduling, enables JPA entity auditing (`AuditingEntityListener` for `@CreatedDate`/`@LastModifiedDate`), and registers the `JwtProperties` record as a configuration bean.

#### 2. `JwtProperties`
- Definition: `@ConfigurationProperties(prefix = "jwt") public record JwtProperties(String secret, long accessTokenTtlMinutes, long refreshTokenTtlDays)`.
- Purpose: Strongly-typed mapping of `jwt.*` configuration keys from `application.yml`.

#### 3. `CacheNames`
- Definition: `public final class CacheNames` with private constructor.
- Constants defined:
  - `RESTAURANT_DETAILS = "restaurantDetails"`
  - `RESTAURANT_LIST = "restaurantList"`
  - `POPULAR_RESTAURANTS = "popularRestaurants"`
  - `MENU_BY_RESTAURANT = "menuByRestaurant"`
  - `MENU_ITEMS_BY_RESTAURANT = "menuItemsByRestaurant"`

#### 4. `OpenApiConfig`
- Annotations: `@Configuration`.
- Purpose: Configures OpenAPI 3.0 specification metadata (Title: `"FoodX API"`, Version: `"1.0.0"`). Defines a security scheme named `"bearerAuth"` of type `HTTP`, scheme `"bearer"`, bearer format `"JWT"`, and applies it globally to all endpoints.

#### 5. `RedisConfig`
- Annotations: `@Configuration`, `@EnableCaching`.
- Purpose: Instantiates a `RedisCacheManager` bean. Configures:
  - Cache TTL: Fixed at 30 minutes (`Duration.ofMinutes(30)`).
  - Key Serialization: `StringRedisSerializer`.
  - Value Serialization: `GenericJackson2JsonRedisSerializer`.

#### 6. `SecurityConfig`
- Annotations: `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity`.
- Purpose: Defines security filter chains, password encoders, authentication managers, and public path authorizations.

### Application Properties (`application.yml`)

```yaml
spring:
  application:
    name: foodx-backend
  datasource:
    url: jdbc:postgresql://localhost:5432/foodx
    username: foodx
    password: ${SPRING_DATASOURCE_PASSWORD:}
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
    properties:
      hibernate:
        "[format_sql]": true
        jdbc:
          "[time_zone]": UTC
    show-sql: false
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    type: redis
  jackson:
    serialization:
      write-dates-as-timestamps: false

server:
  port: 8080

jwt:
  secret: "CHANGE_ME_TO_A_LONG_RANDOM_SECRET_CHANGE_ME_TO_A_LONG_RANDOM_SECRET"
  access-token-ttl-minutes: 15
  refresh-token-ttl-days: 30

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### Environment Variables Recognized

| Environment Variable | Source / Target Property | Default / Fallback |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_PASSWORD` | `spring.datasource.password` | Empty string `""` |
| `SPRING_DATASOURCE_URL` | Defined in `docker-compose.yml` | `jdbc:postgresql://postgres:5432/foodx` |
| `SPRING_DATASOURCE_USERNAME` | Defined in `docker-compose.yml` | `foodx` |
| `SPRING_DATA_REDIS_HOST` | Defined in `docker-compose.yml` | `redis` |
| `SPRING_DATA_REDIS_PORT` | Defined in `docker-compose.yml` | `6379` |
| `JWT_SECRET` | Defined in `docker-compose.yml` | `TO_BE_REPLACED_IN_PRODUCTION` |

---

## 16. Docker Analysis

### Multi-Stage `Dockerfile`
The Docker build utilizes a 2-stage build pipeline:

```dockerfile
# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

#### Build Process Evaluation
- **Base Builder Image:** `maven:3.9.9-eclipse-temurin-21` (official Maven with Java 21 JDK).
- **Compilation:** `mvn -q -DskipTests package` builds the Spring Boot executable fat JAR into `/app/target`. Note: Tests are skipped (`-DskipTests`), though `src/test` is currently empty.
- **Runtime Base Image:** Lightweight `eclipse-temurin:21-jre` (contains JRE only, without compiler tools, reducing the final container image footprint).
- **Execution Entrypoint:** Runs container in foreground via `java -jar /app/app.jar`.
- **Exposed Port:** `8080`.

### Docker Compose Architecture (`docker-compose.yml`)

The environment orchestrates three local containers:

```
+-----------------------------------------------------------------------------+
|                             Docker Compose Network                          |
|                                                                             |
|   +-------------------+    +-------------------+    +-------------------+   |
|   |  foodx-postgres   |    |    foodx-redis    |    |     foodx-app     |   |
|   |   (postgres:16)   |    |     (redis:7)     |    |   (Dockerfile)    |   |
|   |                   |    |                   |    |                   |   |
|   |  Port: 5432:5432  |    |  Port: 6379:6379  |    |  Port: 8080:8080  |   |
|   +-------------------+    +-------------------+    +-------------------+   |
|             |                        |                        |             |
|             v                        v                        |             |
|       postgres_data              redis_data                   |             |
|      (Docker Volume)          (Docker Volume)                 |             |
|             ^                        ^                        |             |
|             |                        |                        |             |
|             +------------------------+------------------------+             |
|                    depends_on: postgres, redis                              |
+-----------------------------------------------------------------------------+
```

#### Service Definitions
1. **`postgres`:**
   - Image: `postgres:16`
   - Container Name: `foodx-postgres`
   - Environment: `POSTGRES_DB=foodx`, `POSTGRES_USER=foodx`, `POSTGRES_PASSWORD=foodx`
   - Port Mapping: `5432:5432`
   - Volume: `postgres_data:/var/lib/postgresql/data` (persistent data)
2. **`redis`:**
   - Image: `redis:7`
   - Container Name: `foodx-redis`
   - Port Mapping: `6379:6379`
   - Volume: `redis_data:/data` (persistent cache)
3. **`app`:**
   - Build Context: `.` (builds root `Dockerfile`)
   - Container Name: `foodx-app`
   - Dependencies: `depends_on` lists `postgres` and `redis`.
   - Environment Overrides:
     - `SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/foodx`
     - `SPRING_DATASOURCE_USERNAME: foodx`
     - `SPRING_DATASOURCE_PASSWORD: foodx`
     - `SPRING_DATA_REDIS_HOST: redis`
     - `SPRING_DATA_REDIS_PORT: 6379`
     - `JWT_SECRET: TO_BE_REPLACED_IN_PRODUCTION`
   - Port Mapping: `8080:8080`

---

## 17. GitHub Actions Analysis

### Current Status
- **Workflows:** **None**.
- **Jobs:** None.
- **Build / Test / Deployment Automation:** None.
- **Triggers:** None.
- **Artifacts:** None.

**Inspection Finding:** There is no `.github` directory or CI/CD workflow configuration anywhere in this repository. Builds and tests must be executed manually using Maven (`mvn package`) and local Docker Compose commands.

---

## 18. Logging Analysis

### Logging Framework
- **Engine:** SLF4J (Simple Logging Facade for Java) backed by Logback (provided through `spring-boot-starter`).
- **Configuration:** Relies on default Spring Boot console logging patterns. No custom `logback-spring.xml` or `log4j2.xml` file is present.

### Log Levels & Outputs
- Default level: `INFO`.
- Configured in `application.yml`:
  - `spring.jpa.show-sql: false` (Hibernate raw SQL output is silenced in console).
  - `spring.jpa.properties.hibernate.format_sql: true` (Prepared if SQL logging is enabled).

### Log Declarations & Usage Across Classes
Loggers are declared via static SLF4J factories (`private static final Logger log = LoggerFactory.getLogger(...)`):
1. **`GlobalExceptionHandler`:**
   - `log.error("Unhandled error", ex);` logs full stack traces for uncaught exceptions handled by the generic fallback.
2. **`RestaurantService`:**
   - `log.info("Restaurant created: {}", saved.getId());` on restaurant creation.
3. **`OrderService`:**
   - `log.info("Order placed: {}", saved.getOrderNumber());`
   - `log.info("Order cancelled: {}", order.getOrderNumber());`
   - `log.info("Order updated: {} -> {}", order.getOrderNumber(), request.status());`

### Logging Gaps
- **Request/Response Logging:** Absent. There is no filter or interceptor logging inbound HTTP request methods, URIs, client IPs, or response statuses.
- **MDC / Distributed Tracing:** Absent. No correlation IDs or tracing headers (e.g., Micrometer Tracing / OpenTelemetry) are attached to log outputs.
- **Audit Logging:** Security events such as failed logins, token refreshes, and administrative suspensions are not logged.

---

## 19. Exception Handling

Centralized exception handling is implemented in `GlobalExceptionHandler` using `@RestControllerAdvice`.

### Custom Exception Hierarchy
All custom exceptions are unchecked (`RuntimeException` subclasses):
- `ResourceNotFoundException`: Thrown when entities (User, Restaurant, Order, CartItem, etc.) are absent. Translated to HTTP 404 NOT FOUND.
- `BadRequestException`: Thrown when business logic constraints are violated (e.g., email already registered, multi-restaurant cart attempt, reviewing non-delivered order). Translated to HTTP 400 BAD REQUEST.
- `UnauthorizedException`: Thrown when credentials fail, account is inactive, or refresh token is expired/revoked. Translated to HTTP 401 UNAUTHORIZED.

### Standard Error Response Structure (`ApiError`)
Declared as a Java record inside `GlobalExceptionHandler`:

```json
{
  "timestamp": "2026-10-22T06:30:00.123456Z",
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "must be a well-formed email address",
    "password": "must not be blank"
  }
}
```

- When handled via `handleNotFound`, `handleBadRequest`, or `handleUnauthorized`, `errors` is set to `null`.
- When handled via `handleValidation` (`MethodArgumentNotValidException`), `errors` is populated with a `Map<String, String>` mapping invalid field names to validation constraint messages.
- When handled via `handleGeneric` (`Exception.class`), returns HTTP 500 with message `"Unexpected error occurred"` and `errors = null`.

### Exception Handling Limitations
- Spring Security's `BadCredentialsException` thrown during `AuthService.login` is not explicitly intercepted by `GlobalExceptionHandler`. It bubbles up to `@ExceptionHandler(Exception.class)`, producing an HTTP 500 instead of HTTP 401.
- Spring Security's `AccessDeniedException` is not handled in `GlobalExceptionHandler`. It relies on Spring Security's default response handling.

---

## 20. Validation

### Bean Validation
Input validation relies on `jakarta.validation` annotations processed by Hibernate Validator via Spring's `@Valid` annotation on controller `@RequestBody` parameters.

#### Applied Constraints in DTOs
- **Emails:** `@Email`, `@NotBlank` (`RegisterRequest.email`, `LoginRequest.email`).
- **Strings:** `@NotBlank`, `@Size(max = 140)`, `@Size(max = 1000)`, `@Size(max = 2000)` (applied across restaurant, menu item, and review requests).
- **Numbers:**
  - `@Min(0)` on `priceMinor` (prevents negative dish pricing).
  - `@Min(1)` on cart item `quantity`.
  - `@Min(1) @Max(5)` on review `rating`.
- **References:** `@NotNull` on UUID references (`categoryId`, `restaurantId`, `menuItemId`, `cartId`, `orderId`) and enum fields (`role`, `status`).

### Custom Validation
There are **no custom Bean Validation annotations** (no custom `@ConstraintValidator` implementations). Complex validation is implemented imperatively in service methods:
- Email duplicate checks: `userRepository.findByEmail(...).isPresent()`.
- Single restaurant cart rule: `cart.getRestaurant().getId().equals(restaurant.getId())`.
- Order state transitions: checking if order is already `DELIVERED` or `CANCELLED`.
- Review eligibility: verifying `order.getStatus() == OrderStatus.DELIVERED` and checking `reviewRepository.findByOrder(...)`.

---

## 21. Dependency Graph

The following ASCII diagram illustrates the compile-time and runtime dependency relationships among packages:

```
                          +------------------------+
                          |   FoodxApplication     |
                          +------------------------+
                                       |
                   +-------------------+-------------------+
                   |                                       |
                   v                                       v
         +-------------------+                   +-------------------+
         |      config       |                   |      common       |
         +-------------------+                   +-------------------+
                   ^                                       ^
                   |                                       |
         +-------------------+                             |
         |     security      |                             |
         +-------------------+                             |
                   ^                                       |
                   |                                       |
         +-------------------+                             |
         |       user        |<----------------------------+
         +-------------------+                             |
          ^       ^       ^                                |
          |       |       +-----------------+              |
          |       |                         |              |
+---------+--+  +-+--------------+   +------+------+       |
|    auth    |  |   restaurant   |   |    cart     |       |
+------------+  +----------------+   +-------------+       |
                  ^            ^            ^              |
                  |            |            |              |
        +---------+---+        |     +------+------+       |
        |    menu     |        |     |    order    |       |
        +-------------+        |     +-------------+       |
                               |            ^              |
                               |            |              |
                               +----+   +---+              |
                                    |   |                  |
                               +------------+              |
                               |   review   |              |
                               +------------+              |
                                                           |
+----------------------------------------------------------+
|  admin  (depends directly on user, restaurant, order)   |
+----------------------------------------------------------+
|  exception (independent leaf referenced by all services) |
+----------------------------------------------------------+
```

---

## 22. Sequence Diagrams

### 1. User Login Sequence
```
Client             AuthController        AuthService       AuthManager    CustomUserDetailsService  JwtService
  |                      |                    |                 |                    |                 |
  |-- POST /api/auth/ -->|                    |                 |                    |                 |
  |   login              |-- login(req) ----->|                 |                    |                 |
  |                      |                    |-- authenticate->|                    |                 |
  |                      |                    |                 |-- loadUserBy       |                 |
  |                      |                    |                 |   Username(email)->|                 |
  |                      |                    |                 |<-- UserDetails ----|                 |
  |                      |                    |                 |    (verify pass)   |                 |
  |                      |                    |<-- Auth OK -----|                    |                 |
  |                      |                    |                                                        |
  |                      |                    |-- verify status == ACTIVE                              |
  |                      |                    |-- generateAccessToken(user) -------------------------->|
  |                      |                    |<-- JWT String -----------------------------------------|
  |                      |                    |                                                        |
  |                      |                    |-- createRefreshToken(user)                             |
  |                      |                    |   (hash SHA-256 & save to DB)                          |
  |                      |<-- AuthResponse ---|                                                        |
  |<-- 200 (JWT + Refresh)|                    |                                                        |
```

### 2. Add Item to Cart Sequence
```
Client            CartController       CartService        CartRepository    MenuItemRepo    RestaurantRepo
  |                     |                   |                   |                 |                |
  |-- POST /api/cart/ ->|                   |                   |                 |                |
  |   items (Bearer JWT)|-- addItem(uid,req)|                   |                 |                |
  |                     |                   |-- findUser(uid)   |                 |                |
  |                     |                   |-- findRestaurant->|                 |                |---> [DB]
  |                     |                   |-- findMenuItem -------------------->|---> [DB]       |
  |                     |                   |-- findByUserAnd   |                                  |
  |                     |                   |   ActiveTrue() -->|---> [DB]                         |
  |                     |                   |                                                      |
  |                     |                   |-- [Enforce single restaurant constraint]             |
  |                     |                   |-- [Find existing item or create new CartItem]        |
  |                     |                   |-- update quantity                                    |
  |                     |                   |-- save(cart) ---->|---> [DB]                         |
  |                     |<-- CartResponse --|                                                      |
  |<-- 200 OK (Cart) ---|                   |                                                      |
```

### 3. Place Order Sequence
```
Client           OrderController       OrderService        CartRepo        OrderRepo       OrderItemRepo
  |                     |                   |                  |               |                 |
  |-- POST /api/orders >|                   |                  |               |                 |
  |   (cartId)          |-- placeOrder() -->|                  |               |                 |
  |                     |                   |-- findCart() --->|---> [DB]      |                 |
  |                     |                   |   (verify user & |               |                 |
  |                     |                   |    items > 0)    |               |                 |
  |                     |                   |                                  |                 |
  |                     |                   |-- new FoodOrder  |               |                 |
  |                     |                   |   calc totalMinor|               |                 |
  |                     |                   |   orderNumber    |               |                 |
  |                     |                   |-- save(order) ------------------>|---> [DB]        |
  |                     |                   |                                  |                 |
  |                     |                   |-- loop cart items:               |                 |
  |                     |                   |   new FoodOrderItem              |                 |
  |                     |                   |   freeze unitPriceMinor          |                 |
  |                     |                   |-- save(orderItem) -------------------------------->|--> [DB]
  |                     |                   |                                                    |
  |                     |                   |-- cart.getItems().clear()        |                 |
  |                     |                   |-- save(cart) --->|---> [DB]      |                 |
  |                     |<-- OrderResponse -|                                  |                 |
  |<-- 200 OK (Order) --|                   |                                  |                 |
```

### 4. Authentication & Authorization Filter Sequence
```
Client           JwtAuthenticationFilter          JwtService       CustomUserDetailsService    Controller Method
  |                         |                          |                       |                       |
  |-- HTTP Request -------->|                          |                       |                       |
  |   Header: Bearer <JWT>  |-- extractUsername(token)>|                       |                       |
  |                         |<-- email ----------------|                       |                       |
  |                         |                                                  |                       |
  |                         |-- loadUserByUsername(email) -------------------->|                       |
  |                         |<-- UserDetails (Authorities: ROLE_*) ------------|                       |
  |                         |                                                  |                       |
  |                         |-- isTokenValid(token, userDetails) ------------->|                       |
  |                         |<-- true -----------------------------------------|                       |
  |                         |                                                                          |
  |                         |-- SecurityContextHolder.getContext().setAuthentication(...)              |
  |                         |                                                                          |
  |                         |-- filterChain.doFilter(...) -------------------------------------------->|
  |                         |                                                      [Evaluate @PreAuthorize]
  |                         |                                                      [Execute Handler Method]
  |<-- HTTP Response -------|<-------------------------------------------------------------------------|
```

---

## 23. Deployment Architecture

### Current Deployment Setup
The project runs as a containerized stack managed via Docker Compose:

```
                                  +-----------------------+
                                  |   External Traffic    |
                                  +-----------------------+
                                              |
                       +----------------------+----------------------+
                       | Port 8080            | Port 5432            | Port 6379
                       v                      v                      v
             +-------------------+  +-------------------+  +-------------------+
             | foodx-app         |  | foodx-postgres    |  | foodx-redis       |
             | Spring Boot 3.3.5 |  | PostgreSQL 16     |  | Redis 7           |
             +-------------------+  +-------------------+  +-------------------+
                       |                      |                      |
                       |                      v                      v
                       |             +-----------------+    +-----------------+
                       |             |  postgres_data  |    |   redis_data    |
                       |             |  (Named Volume) |    |  (Named Volume) |
                       |             +-----------------+    +-----------------+
                       +----------------------+----------------------+
                                  Docker Default Bridge Network
```

- **Hosting & Infrastructure:** Single Docker host running Docker Engine and Docker Compose.
- **Port Exposure:**
  - `8080`: Exposes the Spring Boot HTTP server.
  - `5432`: Exposes PostgreSQL to the host network (intended for development/debugging; poses a security risk if deployed directly to production).
  - `6379`: Exposes Redis to the host network without password authentication.
- **Networking:** All containers reside on the default Compose bridge network. The application container references peer services by DNS name (`postgres` and `redis`).
- **Data Persistence:** Backed by two Docker named volumes: `postgres_data` and `redis_data`.

---

## 24. External Integrations

| Integration Type | Target Service / Vendor | Status in Codebase | Notes |
| :--- | :--- | :--- | :--- |
| **Email / SMTP** | None | **Absent** | User registration initializes `emailVerified = true` by default; no verification emails or password reset workflows exist. |
| **Payment Gateway** | None (Stripe, Razorpay, etc.) | **Absent** | Orders are placed directly into `PENDING` status without payment authorization, webhooks, or ledger recording. |
| **Cloud Storage** | None (AWS S3, Cloudinary) | **Absent** | No media uploads. Restaurant and menu item models store only text metadata, descriptions, and addresses. |
| **Maps / Geolocation** | None (Google Maps, Mapbox) | **Absent** | Restaurant addresses and cities are plain strings. No latitude/longitude coordinates or spatial search queries exist. |
| **Third-Party OAuth** | None (Google, Apple, GitHub) | **Absent** | Relies strictly on native username/password authentication via BCrypt. |
| **Push Notifications** | None (Firebase, APNs, Twilio) | **Absent** | No push notifications or SMS messages are dispatched upon order state changes. |

---

## 25. Code Statistics

Exact code metrics calculated across the codebase:

### High-Level Metrics
- **Total Packages:** 13 (Root package + 12 functional sub-packages)
- **Total Source Files (Java):** 61
- **Total Lines of Java Code:** 2,095 lines
- **Total Configuration & Build Files:** 5 (`pom.xml`, `application.yml`, `Dockerfile`, `docker-compose.yml`, `README.md`)
- **Total Unit / Integration Tests:** 0

### Component Breakdown
- **REST Controllers:** 7 (`AdminController`, `AuthController`, `CartController`, `MenuController`, `OrderController`, `RestaurantController`, `ReviewController`)
- **Business Services:** 7 (`AdminService`, `AuthService`, `CartService`, `MenuService`, `OrderService`, `RestaurantService`, `ReviewService`)
- **Security & User Services:** 2 (`CustomUserDetailsService`, `JwtService`)
- **Spring Data JPA Repositories:** 9 (`UserRepository`, `RefreshTokenRepository`, `RestaurantRepository`, `MenuCategoryRepository`, `MenuItemRepository`, `CartRepository`, `FoodOrderRepository`, `FoodOrderItemRepository`, `ReviewRepository`)
- **Persistent Domain Entities:** 10 (`User`, `RefreshToken`, `Restaurant`, `MenuCategory`, `MenuItem`, `Cart`, `CartItem`, `FoodOrder`, `FoodOrderItem`, `Review`)
- **Mapped Superclasses:** 1 (`BaseEntity`)
- **Domain Enumerations:** 6 (`UserRole`, `AccountStatus`, `RestaurantStatus`, `MenuItemStatus`, `OrderStatus`, `ReviewStatus`)
- **DTO Records:** 26 (declared within 7 DTO holder classes) + 1 error record (`ApiError`) = 27
- **Configuration Classes:** 5 (`AppConfig`, `JwtProperties`, `CacheNames`, `OpenApiConfig`, `RedisConfig`) + `SecurityConfig`
- **Global Exception Handlers:** 1 (`GlobalExceptionHandler`)
- **Custom Exceptions:** 3 (`BadRequestException`, `ResourceNotFoundException`, `UnauthorizedException`)

---

## 26. Architecture Strengths

1. **Modern Runtime & Framework Baseline:** Built on Java 21 and Spring Boot 3.3.5, taking advantage of modern language capabilities (Java Records, pattern matching, switch expressions) and Spring Security 6.
2. **Clear Domain Partitioning:** High cohesion within packages (`restaurant`, `menu`, `cart`, `order`, `review`, `auth`). Each package encapsulates its controller, service, repository, entity, and DTOs.
3. **Immutability via Java Records:** All Request and Response DTOs are declared as Java records, eliminating boilerplate and ensuring thread-safe data transfer across layers.
4. **Integer Currency Arithmetic:** Monies are strictly handled as `long priceMinor`, `long unitPriceMinor`, `long lineTotalMinor`, and `long totalMinor` (cents/paise), preventing floating-point precision errors.
5. **Separation of Read/Write Database Transactions:** Services consistently apply `@Transactional` at the class level and annotate read methods with `@Transactional(readOnly = true)`, allowing database drivers to optimize query routing and dirty checking.
6. **Hashed Refresh Token Persistence:** Refresh tokens are never stored in plaintext within PostgreSQL; they are hashed via SHA-256 before persistence, mitigating database credential dump risks.
7. **Stateless JWT Security Architecture:** Enables horizontal scaling without session replication bottlenecks.
8. **Automated Entity Auditing:** `BaseEntity` with `@EntityListeners(AuditingEntityListener.class)` and `@CreatedDate`/`@LastModifiedDate` ensures consistent audit timestamps across all database records.
9. **Containerized Multi-Stage Build:** The Dockerfile employs a two-stage pattern separating the Maven build environment from the minimal JRE runtime image.
10. **OpenAPI / Swagger Integration:** API documentation is automatically generated from controller signatures and annotated with the JWT Bearer security scheme.

---

## 27. Architecture Weaknesses

1. **Broken Object-Level Authorization (BOLA / IDOR):**
   - Owners can update, open, or close restaurants they do not own.
   - Owners can inject, modify, or disable menu items in other restaurants.
   - Customers can cancel other customers' orders by UUID.
   - Owners can modify order statuses across the platform.
2. **Double User Queries on Every Protected Request:**
   - Every request triggers `customUserDetailsService.loadUserByUsername` in the JWT filter, followed by `userRepository.findByEmail` in controller helper methods (`resolveUser`).
3. **No Database Migration Tooling:**
   - Relies on `hibernate.ddl-auto: update`. There are no versioned migration scripts (Flyway or Liquibase), making automated CI/CD schema changes and rollbacks unreliable.
4. **Collision-Prone Order Number Generation:**
   - `orderNumber` is generated as `"FX-" + System.currentTimeMillis()`. Concurrent orders placed within the same millisecond trigger a database unique constraint violation on `orders.order_number`.
5. **No Test Coverage:**
   - `src/test/java` is empty. Zero unit, integration, or slice tests exist.
6. **Memory-Bound Unpaged Queries:**
   - Admin APIs (`users()`, `restaurants()`, `orders()`) execute unpaged `findAll()` calls, creating out-of-memory risks as tables grow.
7. **In-Memory Menu Item Filtering:**
   - `MenuService.listItems` pulls all menu items for a restaurant into memory and filters them using Java streams (`contains()`), bypassing database indexing and query optimization.
8. **Broken Cache Eviction in Admin Module:**
   - `AdminService.disableRestaurant` updates the repository directly, bypassing `RestaurantService.disable` and leaving stale open restaurants in Redis cache.
9. **Menu Cache Staleness:**
   - `MenuService.createCategory`, `createItem`, `updateItem`, and `disableItem` do not invoke `@CacheEvict`. Cached menu listings remain stale for up to 30 minutes.
10. **Security Configuration / Method Security Discrepancy:**
    - `GET /api/reviews/restaurant/{id}` is marked `permitAll()` in the controller, but blocked by `anyRequest().authenticated()` in `SecurityConfig`.
11. **Unused Dependencies:**
    - MapStruct 1.6.3 is declared in `pom.xml` and configured in the compiler plugin, but is completely unused.
12. **Absence of Refresh Token Rotation:**
    - Calling `/api/auth/refresh` returns the same refresh token, increasing exposure window if a refresh token is intercepted.
13. **Exposed Database Ports in Docker Compose:**
    - Ports `5432` and `6379` are bound directly to the host network. Redis runs without password authentication.

---

## 28. Missing Components

1. **Automated Database Migrations:** No Flyway or Liquibase integration.
2. **Automated Test Suite:** Zero unit tests, mock tests, or `@SpringBootTest` integration tests.
3. **CI/CD Pipeline:** No `.github/workflows` or other CI automation configurations.
4. **Payment Processing:** No payment gateways (Stripe, PayPal, Razorpay) or payment transaction entities.
5. **Object-Level Authorization Guards:** No authorization logic verifying ownership of restaurants, menus, or orders.
6. **Pagination & Slicing:** No `Pageable` parameters on list endpoints.
7. **Media & Image Storage:** No cloud file storage (S3, Cloudinary) for restaurant banners or menu item images.
8. **Event-Driven Messaging:** No message queues (RabbitMQ, Kafka) for asynchronous workflows (order notifications, delivery tracking).
9. **Observability & Distributed Tracing:** No correlation ID filter, Micrometer Tracing, or OpenTelemetry integration.
10. **Email & Notification Engine:** No SMTP mail client or push notification service.
11. **Address & Geolocation Engine:** No spatial data types, address validation, or distance calculations.
12. **Access Token Revocation / Blacklisting:** No Redis-backed token denylist for revoked JWTs.
13. **CORS Configuration Source Bean:** No explicit `CorsConfigurationSource` allowing cross-origin requests from frontend hosts.
14. **Custom Role Hierarchy:** No `RoleHierarchy` configuration establishing role inheritance (e.g., `ADMIN > OWNER > CUSTOMER`).

---

## 29. Glossary

- **`BaseEntity`:** An abstract mapped superclass containing common audit timestamps (`createdAt`, `updatedAt`) managed by JPA lifecycle hooks and `AuditingEntityListener`.
- **`BCryptPasswordEncoder`:** A password-hashing implementation using the OpenBSD Blowfish password-hashing algorithm with embedded salt generation.
- **BOLA / IDOR:** Broken Object Level Authorization / Insecure Direct Object Reference; a vulnerability where an endpoint allows an actor to access or mutate an object without verifying ownership.
- **`CartItem`:** A line item entity mapping a chosen `MenuItem` and quantity to a customer's `Cart`.
- **`CustomUserDetailsService`:** Spring Security service implementing `UserDetailsService` that resolves user credentials and authorities from the `UserRepository`.
- **DDL (`hibernate.ddl-auto`):** Data Definition Language mode; configured as `update`, instructing Hibernate to alter database tables dynamically on application startup.
- **DTO (Data Transfer Object):** Strongly typed record structures decoupling internal JPA entities from external JSON representations.
- **`FoodOrder`:** Persistent entity representing a completed purchase, tracking financial totals, fulfillment status, and line items.
- **`FoodOrderItem`:** Immutable line item associated with an order, freezing the menu item's name and price at purchase time.
- **HS256:** HMAC using SHA-256; a symmetric cryptographic algorithm where the same secret key is used to both sign and verify JWT tokens.
- **`JwtAuthenticationFilter`:** A custom `OncePerRequestFilter` that intercepts incoming HTTP requests, extracts the Bearer token, validates claims, and sets the authenticated principal in the security context.
- **`JwtProperties`:** A configuration record bound to the `jwt.*` prefix in `application.yml` storing secret keys and token TTLs.
- **`JwtService`:** Service encapsulating JJWT operations, responsible for token generation, signature validation, and claims extraction.
- **MapStruct:** A compile-time annotation processor for generating object mappers; declared in `pom.xml` but unused in the codebase.
- **Minor Units (`priceMinor`):** Storing currency values in the smallest unit (cents/paise) as integers/longs to prevent floating-point rounding errors.
- **`MenuItem`:** Persistent entity representing a dish or beverage tied to a restaurant and optional category.
- **`MenuCategory`:** Persistent entity grouping dishes under a restaurant.
- **`RefreshToken`:** Persistent entity storing a SHA-256 hashed session token, expiration timestamp, and revocation flag.
- **`Restaurant`:** Persistent entity representing a dining establishment, its physical location, ownership, and operational status.
- **`Review`:** Persistent entity capturing customer rating (1-5) and feedback tied to a delivered order.
- **`UserRole`:** Domain enumeration defining platform roles: `CUSTOMER`, `OWNER`, and `ADMIN`.

---

## 30. Final Summary

### Current Architecture
FoodX is a clean, layered Spring Boot 3.3.5 monolithic backend built on Java 21, Spring Data JPA, and PostgreSQL 16, with Redis 7 serving as a query cache. The application utilizes a stateless, Bearer token JWT architecture and organizes code into distinct domain packages (`auth`, `user`, `restaurant`, `menu`, `cart`, `order`, `review`, `admin`). Business models are defined via JPA entities inheriting auditing timestamps from `BaseEntity`, while client interactions rely on immutable Java records.

### Major Modules
1. **Authentication & Identity:** User onboarding, password hashing, JWT generation, SHA-256 hashed refresh token persistence.
2. **Restaurant Operations:** Establishment profile management, status transitions, city/name search, owner lookups.
3. **Menu Management:** Structured menu categories and menu items with minor-unit pricing.
4. **Cart System:** Active shopping cart management enforcing single-restaurant constraints.
5. **Order Lifecycle:** Cart-to-order transformation, price freezing, order tracking, and state progression.
6. **Reviews & Ratings:** Post-delivery feedback and rating system.
7. **Administration:** System-wide visibility, account suspension, and restaurant disabling.

### Current Maturity Level
The system is at an **Early Stage / Functional Prototype** maturity level. The domain boundaries, entity models, and API interfaces are well-defined and follow clean code conventions. However, critical operational foundations—automated tests, database migrations, CI/CD pipelines, object-level authorization checks, payment integrations, and pagination—are not yet implemented.

### Production Readiness Estimate: 3 / 10
While the core business logic functions as a proof of concept, the application is not production-ready in its current state due to the following gaps:
- **0/10 for Automated Testing:** Complete lack of test coverage across unit, slice, and integration levels.
- **BOLA/IDOR Security Gaps:** Absence of object-level authorization checks on restaurant updates, menu modifications, order status transitions, and order cancellations.
- **Database Management:** Reliance on `hibernate.ddl-auto: update` without migration tooling (Flyway/Liquibase).
- **Concurrency Risks:** Potential order number collisions under high concurrency (`System.currentTimeMillis()`).
- **Memory Scaling Risks:** Unpaged administrative queries that will degrade under production data volumes.
- **Incomplete Workflows:** Missing payment gateway integration and notification mechanisms.
