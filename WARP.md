# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Code Nest is a developer community and knowledge platform built with **Spring Boot 3.4.4** (backend) and **Vue 3 + Vite 5** (frontend). It's a multi-module Maven project with 20+ business modules aggregated through `xiaou-application`.

## Build & Run Commands

### Backend

```bash
# Full build (skip tests)
mvn clean package -DskipTests

# Run in dev mode
mvn -pl xiaou-application -am spring-boot:run

# Run packaged JAR
java -jar xiaou-application/target/xiaou-application-1.7.0.jar --spring.profiles.active=prod

# Run single test class
mvn -pl xiaou-application test -Dtest=CozeTest
```

### Frontend (Admin)

```bash
cd vue3-admin-front
npm install
npm run dev      # Port 3000, proxies /api to localhost:9999
npm run build
npm run lint
```

### Frontend (User)

```bash
cd vue3-user-front
npm install
npm run dev2     # Port 3001, proxies /api to localhost:9999 (note: script is "dev2")
npm run build
npm run lint
```

### Docker

```bash
docker build -t code-nest:1.7.0 -f docker/Dockerfile .
docker run -d -p 9999:9999 -e SPRING_PROFILES_ACTIVE=prod code-nest:1.7.0
```

## Architecture

### Backend Module Structure

```
xiaou-application   # Main entry point - aggregates all modules, Spring Boot starter
xiaou-common        # Shared utilities: Result<T>, PageResult, exceptions, annotations, Sa-Token utils
xiaou-system        # System management: roles, menus, dictionaries, audit logs
xiaou-user[-api]    # User authentication (xiaou-user-api for cross-module interfaces)
xiaou-interview     # Quiz/interview questions system
xiaou-community     # Posts, jobs, community features
xiaou-moment        # Social feed/timeline
xiaou-blog          # Blog/article system
xiaou-codepen       # Code playground
xiaou-resume        # Resume builder
xiaou-chat          # WebSocket IM
xiaou-notification  # Notifications system
xiaou-filestorage   # File upload/storage
xiaou-sensitive[-api] # Sensitive word filtering
xiaou-knowledge     # Knowledge graph
xiaou-points        # Points/gamification
xiaou-plan          # Daily check-in/plans
xiaou-mock-interview # AI mock interviews (Coze integration)
xiaou-team          # Team collaboration
xiaou-version       # Version tracking
xiaou-moyu          # Daily tasks ("fishing" features)
```

### Business Module Convention

Each `xiaou-*` module follows this structure:
```
com.xiaou.{module}/
├── controller/
│   ├── admin/    # Admin endpoints: /admin/{module}/**
│   └── pub/      # Public/user endpoints: /{module}/**
├── domain/       # Entity classes
├── dto/          # Request/response DTOs
├── mapper/       # MyBatis mappers (XML in src/main/resources/mapper/)
├── service/
│   └── impl/
└── utils/        # Module-specific utilities
```

### Key Patterns

**Unified Response**: All APIs return `Result<T>` from `xiaou-common`:
```java
return Result.success(data);
return Result.error("message");
return Result.error(ResultCode.PARAM_VALIDATE_ERROR.getCode(), "message");
```

**Authentication** (Sa-Token with dual account systems):
- Admin APIs: Use `@RequireAdmin` annotation, login via `StpAdminUtil`
- User APIs: Use `StpUserUtil`
- Get current user: `SaTokenUserUtil.getCurrentUserId()`, `SaTokenUserUtil.getCurrentAdminId()`

**Logging**: Use `@Log` annotation on controller methods:
```java
@Log(module = "面试题目", type = Log.OperationType.INSERT, description = "创建题目")
```

**Pagination**: Use `PageResult<T>` and PageHelper:
```java
public Result<PageResult<Entity>> list(@RequestBody QueryRequest request) {
    PageResult<Entity> result = service.getPage(request);
    return Result.success(result);
}
```

### Frontend Structure (Both apps)

```
src/
├── api/          # API service modules (axios calls)
├── components/   # Reusable Vue components
├── layout/       # App layout components
├── router/       # Vue Router configuration
├── stores/       # Pinia state stores
├── styles/       # Global SCSS/CSS
├── utils/        # Utility functions
└── views/        # Page components
```

Path alias: `@` → `src/`

## Configuration

- Main config: `xiaou-application/src/main/resources/application.yml`
- Environment profiles: `application-dev.yml`, `application-prod.yml`
- Secrets: `application-sec.yml` (gitignored, contains API keys)
- Backend port: 9999, context path: `/api`
- Redis: Database 3 for Redisson (business), Database 4 for Sa-Token sessions

## Database

- MySQL 8 with utf8mb4
- Schema: `sql/code_nest.sql` (full), incremental scripts in `sql/v{version}/`
- MyBatis mapper locations: `classpath*:mapper/**/*.xml` and `classpath*:com/xiaou/*/mapper/*.xml`

## Monitoring

- Prometheus metrics: `http://localhost:9999/api/actuator/prometheus`
- Health check: `http://localhost:9999/api/actuator/health`
- Swagger UI: `http://localhost:9999/api/swagger-ui.html`

## Important Notes

- The project uses Undertow instead of Tomcat for better performance
- P6Spy is enabled in dev for SQL logging (configured via `jdbc:p6spy:mysql://...`)
- WebSocket endpoints require Sa-Token authentication
- Sensitive config (API keys, passwords) goes in `application-sec.yml`
- Version is managed via `${revision}` property in parent pom.xml (currently 1.7.0)
