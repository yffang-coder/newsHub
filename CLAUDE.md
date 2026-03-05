# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NewsHub is a full-stack news aggregation platform with a microservices-inspired architecture consisting of three main components:
- **Frontend**: Vue 3 SPA with TypeScript, Tailwind CSS, and Element Plus
- **Backend**: Spring Boot REST API with MySQL, Redis, and Kafka integration
- **Crawler System**: Python-based news aggregation with RSS and web scraping

## Architecture Notes

### Frontend Structure
- **Entry point**: `src/main.ts` - Vue 3 app initialization with auto-imports for Element Plus components
- **Routing**: `src/router/index.ts` - Vue Router configuration with authentication guards
- **State management**: `src/stores/user.ts` - Pinia store for user authentication state
- **API layer**: `src/api/` - Axios wrappers organized by domain (auth.ts, news.ts, admin.ts, etc.)
- **Component organization**:
  - `src/components/` - Reusable components (WeatherWidget.vue, HoroscopeWidget.vue, etc.)
  - `src/views/` - Page components organized by feature (admin/, auth/, user/, etc.)

### Backend Structure
- **Package**: `com.newshub.backend` - Java package structure
- **Architecture layers**:
  - `domain/` - Entity models
  - `application/` - Business logic and services
  - `infrastructure/` - Persistence, configuration, and utilities
  - `interfaces/` - REST controllers and DTOs
- **Key services**:
  - `CrawlerService.java` - Orchestrates Python crawler execution
  - `IpLocationService.java` - Resolves IP addresses to cities using ip-api.com
  - `CacheService.java` - Redis caching wrapper
  - `WeatherController.java` - Weather API with IP-based city detection

### Crawler System
- **Main crawler**: `crawler-python/main.py` - News RSS feed aggregation
- **Weather crawler**: `crawler-python/weather_crawler.py` - Fetches weather from wttr.in API
- **Integration**: Python scripts are executed by Java backend via ProcessBuilder
- **Data flow**: Crawlers → Kafka or direct database writes → Redis cache → Frontend

## Development Commands

### Frontend Development
```bash
# Install dependencies
npm install

# Start development server with proxy to backend (port 5173)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Type checking
npm run type-check
```

### Backend Development
```bash
# Run with local profile (requires MySQL/Redis running)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Build JAR
mvn clean package

# Run tests
mvn test
```

### Docker Deployment
```bash
# Start all services (frontend, backend, MySQL, Redis, Kafka)
cd deployment
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f
```

### Database
```bash
# Initialize database (run once)
mysql -u root -p < backend/src/main/resources/schema.sql

# Database connection details:
# Host: localhost:3307 (Docker) or localhost:3306 (local)
# Database: newshub
# Username: root
# Password: password (configured in application.yml)
```

## Configuration

### Key Configuration Files
- `backend/src/main/resources/application.yml` - Main Spring Boot configuration
- `backend/src/main/resources/application-local.yml` - Local development overrides
- `backend/src/main/resources/schema.sql` - Database schema and initial data
- `vite.config.ts` - Frontend build configuration with proxy setup
- `tailwind.config.js` - Tailwind CSS theme configuration

### Environment Variables
- `VITE_API_BASE_URL` - Frontend API base URL (default: proxy to localhost:8080)
- `SPRING_PROFILES_ACTIVE` - Spring profile (local, prod)
- `SPRING_DATA_REDIS_HOST` - Redis host (default: redis)
- `SPRING_DATA_REDIS_PORT` - Redis port (default: 6379)
- `BACKEND_API_URL` - Python crawler backend target (default: http://127.0.0.1:8080)

## Common Issues and Solutions

### Weather API Returns Empty
1. **Check Redis connection**: Weather data is cached in Redis with key `weather:data:{city}`
2. **Verify IP location service**: `IpLocationService` uses ip-api.com; local IPs default to "上海"
3. **Test Python crawler**: Run `python weather_crawler.py 上海` to verify wttr.in API access
4. **Network issues**: wttr.in may be blocked; consider adding fallback APIs

### Crawler Not Executing
1. **Check configuration**: `app.crawler.enabled` must be `true` in application.yml
2. **Verify Python environment**: Ensure `python3` or configured command is available
3. **Check script paths**: `app.crawler.script-path` should point to crawler-python directory
4. **Review logs**: Crawler output is logged by `CrawlerService`

### Database Connection Issues
1. **MySQL port**: Docker uses 3307, local installation may use 3306
2. **Schema initialization**: Ensure `schema.sql` has been executed
3. **Connection pool**: Check MySQL is running and credentials match application.yml

### Frontend-Backend Communication
1. **Proxy setup**: Vite dev server proxies `/api/*` to `http://localhost:8080`
2. **CORS**: Backend controllers use `@CrossOrigin` annotation
3. **Authentication**: JWT tokens are required for protected endpoints; stored in Pinia store

## Testing Patterns

### Frontend Testing
- Components use Composition API with TypeScript
- API calls use Axios interceptors for auth token injection
- State management via Pinia stores
- Route guards for authentication/authorization

### Backend Testing
- Spring Boot integration tests
- MyBatis for database operations
- Redis caching with TTL configuration
- Scheduled tasks for crawler execution

### Integration Points
- Python crawlers communicate with backend via HTTP POST to `/api/public/weather/update`
- Weather data flow: wttr.in API → Python crawler → Backend cache → Frontend display
- News data flow: RSS feeds → Python crawler → Kafka → Backend processing → Database

## Deployment Notes

### Docker Configuration
- Multi-stage builds for both frontend (Node.js → nginx) and backend (Maven → JDK)
- Service dependencies: backend requires MySQL and Redis
- Volume persistence: MySQL data stored in Docker volume
- Health checks: Redis health monitoring configured in docker-compose.yml

### Production Considerations
- Environment-specific configuration via Spring profiles
- JWT secret should be changed from default in production
- Consider rate limiting for public APIs
- Implement monitoring for crawler execution and API health

## Code Conventions

### Frontend
- Vue 3 Composition API with `<script setup>`
- TypeScript for type safety
- Tailwind CSS utility classes for styling
- Element Plus components for UI consistency
- Pinia stores for state management

### Backend
- Spring Boot 3.x with Java 17
- Lombok for boilerplate reduction
- MyBatis annotations for database operations
- JWT-based authentication with Spring Security
- Redis caching for performance optimization

### Python Crawlers
- Requests library for HTTP calls
- Structured logging with timestamps
- Error handling with multiple fallback strategies
- Integration with Java backend via HTTP APIs