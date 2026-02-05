# NewsHub Backend

This is the Spring Boot backend for the NewsHub application.

## Tech Stack
- **Java**: 17
- **Framework**: Spring Boot 3.2.3
- **Database**: MySQL 8.0
- **ORM**: MyBatis
- **Security**: Spring Security + JWT
- **Cache**: Redis
- **Messaging**: Kafka (for crawler)
- **Crawler**: Rome (RSS) + Jsoup
- **Documentation**: OpenAPI / Swagger UI

## Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven

## Setup & Run

### Option 1: Docker (Recommended)
Requires Docker Desktop installed and running.

1. **Start Infrastructure (MySQL, Redis, Kafka)**
   ```bash
   docker-compose up -d
   ```

2. **Run Backend**
   ```bash
   mvn spring-boot:run
   ```

### Option 2: Local Mode (No Docker required)
Runs with in-memory database (H2) and mock services. Data is lost on restart.

1. **Run Backend**
   ```bash
   ./run-local.bat
   # OR
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```
   
   H2 Console: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:newshubdb`
   - User: `sa`
   - Password: (empty)

## Features implemented
- **Authentication**: User registration and login with JWT.
- **News Crawler**: Fetches RSS feeds from BBC/CNN every hour and pushes to Kafka.
- **Data Processing**: Consumes news from Kafka, saves to MySQL (with deduplication).
- **Caching**: Hot news caching in Redis (5 mins TTL).
- **Data Retention**: Daily cleanup of news older than 3 days.
- **API**: Endpoints for latest news, news details, and category-based news.

## Configuration
See `src/main/resources/application.yml` for configuration details.
- Database credentials
- JWT secret
- Crawler interval

## Frontend Integration
The frontend (Vite) is configured to proxy `/api` requests to `http://localhost:8080`.
Ensure both backend and frontend are running.
