# Market & Mobility Backend
A Spring Boot backend service for managing products, ecommerce orders, and ride hailing with JWT authentication, search/pagination, integration tests, Swagger docs, Docker support, and CI.

---

## ✅ Development Checklist

| Feature | Status |
|---------|--------|
| Products CRUD | ❌ |
| Auth + JWT | ❌ |
| Orders | ❌ |
| Rides | ❌ |
| Search + Pagination | ❌ |
| Integration Tests | ❌ |
| Swagger | ❌ |
| Docker | ❌ |
| CI | ❌ |

---

## 🛠 Project Structure

- `auth/` — registration & login with JWT
- `security/` — JWT filter & Spring Security config
- `users/` — user entity & role
- `products/` — product CRUD + search
- `orders/` — ecommerce order handling
- `rides/` — ride requests & status
- `common/` — exceptions and helpers
- `config/` — Swagger / app configuration

---

## 🚀 Getting Started

### Requirements
- Java 17+ / 21
- PostgreSQL
- Docker & Docker Compose
- Maven

### Running Locally
1. Configure environment variables:
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/cymelle
   export DB_USER=postgres
   export DB_PASSWORD=password
   export JWT_SECRET=your_jwt_secret
Start PostgreSQL:

docker compose up -d


Run the app:

./mvnw spring-boot:run

Running with Docker
docker compose up --build

📘 API Documentation

Once the application is running:

Swagger UI: http://localhost:8080/swagger-ui.html

Explore endpoints for Auth, Products, Orders, and Rides

🧪 Tests

Run integration tests:

./mvnw clean test

📄 License
Apache