# E-commerce Backend

A modular-monolith e-commerce REST API built with **Java 21**, **Spring Boot 3.3.2**, **Spring Security**, **JWT**, and **MongoDB**. The project is organized by feature and uses layered controllers, services, repositories, DTOs, and domain models.

## Technology stack

- Java 21
- Spring Boot 3.3.2
- Spring Web / REST
- Spring Data MongoDB
- Spring Security with stateless JWT authentication
- JWT (JJWT 0.12.6)
- Jakarta Bean Validation
- Lombok
- Maven
- Springdoc OpenAPI + Swagger UI

Kafka and the React frontend are planned roadmap items; they are not part of the current implementation.

## Architecture

The backend is a **modular monolith**, not a microservices system. Each feature owns its API and business logic while sharing common infrastructure.

```text
src/main/java/com/sarthak/ecommerce/
├── EcommerceBackendApplication.java
├── auth/
│   ├── controller/
│   ├── dto/
│   ├── security/
│   └── service/
├── cart/
│   ├── controller/
│   ├── dto/
│   ├── exception/
│   ├── model/
│   ├── repository/
│   └── service/
├── common/
│   ├── dto/
│   ├── exception/
│   └── response/
├── order/
│   ├── controller/
│   ├── dto/
│   ├── model/
│   ├── repository/
│   └── service/
├── product/
│   ├── controller/
│   ├── dto/
│   ├── exception/
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── util/
└── user/
    ├── controller/
    ├── dto/
    ├── exception/
    ├── model/
    ├── repository/
    ├── service/
    └── util/
```

- **Controller:** HTTP routing and request/response handling.
- **Service:** business rules and orchestration.
- **Repository:** MongoDB persistence.
- **DTO:** API request and response contracts.
- **Model:** MongoDB documents and domain enums.
- **Common:** shared response and exception handling.
- **Security:** JWT filter and authorization configuration.

## Main features

- Product creation, update, deletion, browsing, search, filtering, pagination, and sorting.
- User registration and profile management.
- Login with a JWT stored in an HttpOnly cookie.
- Role-based access for `USER`, `SELLER`, and `ADMIN`.
- Cart management with product/stock validation.
- Order placement from the cart, order history, order details, and cancellation.
- Product reviews and rating-related data.
- Centralized API exception handling and validation.

## API reference

Base URL: `http://localhost:8080`

All endpoints below are prefixed with the base URL. Unless stated otherwise, protected endpoints require a valid login cookie named `accessToken`.

### Authentication — `/api/auth`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/api/auth/register` | Register a user | Public |
| POST | `/api/auth/login` | Log in and receive the JWT cookie | Public |

Registration accepts `name`, `email`, `password`, `role`, and optional `storeName`. Roles are `USER`, `SELLER`, and `ADMIN`.

### Products — `/api/products`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/api/products` | Browse products (pageable) | Public |
| GET | `/api/products/search?name={name}` | Search by product name | Public |
| GET | `/api/products/filter?minPrice={min}&maxPrice={max}` | Filter by price range | Public |
| GET | `/api/products/{id}` | Get product details | Public |
| POST | `/api/products` | Create a product | SELLER, ADMIN |
| PUT | `/api/products/{id}` | Update a product | SELLER, ADMIN |
| DELETE | `/api/products/{id}` | Delete a product | SELLER, ADMIN |

Product listing, search, and price filtering support Spring Data pagination and sorting parameters such as `page`, `size`, and `sort`.

### Reviews — `/api/reviews`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/api/reviews` | Add a product review | Authenticated |
| GET | `/api/reviews/product/{productId}` | Get reviews for a product (pageable) | Authenticated |

A review contains `productId`, `userId`, `rating` (1–5), and `comment`.

### Users — `/api/users`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/api/users` | Create a user | Authenticated |
| GET | `/api/users` | List users | ADMIN |
| GET | `/api/users/{id}` | Get a user | Authenticated; ownership enforced by service |
| PUT | `/api/users/{id}` | Update a user | Authenticated; ownership enforced by service |
| DELETE | `/api/users/{id}` | Delete a user | Authenticated; ownership enforced by service |

### Cart — `/api/cart`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/api/cart` | Get the current user's cart | USER, SELLER |
| POST | `/api/cart/items/{productId}` | Add a product (body: `quantity`) | USER, SELLER |
| PUT | `/api/cart/items/{productId}` | Update item quantity | USER, SELLER |
| DELETE | `/api/cart/items/{productId}` | Remove an item | USER, SELLER |
| DELETE | `/api/cart` | Clear the cart | USER, SELLER |

### Orders — `/api/orders`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/api/orders` | Place an order from the current cart | Authenticated |
| GET | `/api/orders` | Get the current user's order history | Authenticated |
| GET | `/api/orders/{orderId}` | Get order details (ownership checked) | Authenticated |
| POST | `/api/orders/{orderId}/cancel` | Cancel an eligible order (ownership checked) | Authenticated |

Order statuses: `PLACED`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`.

Order items preserve the product name and price at checkout. Checkout validates and reserves stock, creates the order, and clears the cart. Cancellation restores stock for eligible orders. The current implementation uses compensating updates; it is **not a multi-document MongoDB transaction**. For stronger production consistency, use a MongoDB replica set and transactions, and make cancellation/stock restoration concurrency-safe.

## Authentication and authorization

- The application is stateless; it does not use an HTTP session.
- Successful login sets an HttpOnly cookie named `accessToken`.
- The JWT subject identifies the authenticated user; role-based rules use `USER`, `SELLER`, and `ADMIN`.
- Product browsing and authentication routes are public.
- Product writes are restricted to sellers and admins.
- Cart routes are restricted to users and sellers.
- Ownership checks are performed in the service layer for user/order resources.

For local development, the login cookie can be retained automatically by Postman when cookie handling is enabled. Do not expose JWTs or passwords in logs or commit secrets to the repository.

## Run locally

Prerequisites:
- JDK 21
- Maven (or the included Maven wrapper)
- MongoDB running locally or in Docker

Run the application:

```bash
./mvnw spring-boot:run
```

On Windows:

```bat
mvnw.cmd spring-boot:run
```

Configure the MongoDB connection using your local Spring Boot configuration/environment. Keep credentials and other secrets out of version control.

## OpenAPI and Swagger UI

The OpenAPI contract is maintained at [`src/main/resources/static/openapi.yaml`](src/main/resources/static/openapi.yaml). Add the following to your local (git-ignored) `src/main/resources/application.properties` so Swagger UI loads this maintained contract instead of the generated default document:

```properties
springdoc.swagger-ui.url=/openapi.yaml
```

After adding the Springdoc dependency, restart the application and open:

`http://localhost:8080/swagger-ui/index.html`

The static specification can also be viewed at:

`http://localhost:8080/openapi.yaml`

## Current roadmap

- [x] Product API
- [x] User API
- [x] JWT authentication and authorization
- [x] Cart API
- [x] Order API
- [ ] Kafka event-driven integration
- [ ] React frontend
- [ ] Final API documentation, image management, and polishing

## License

No license has been specified yet.
