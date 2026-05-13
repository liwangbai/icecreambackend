# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Commands

### Build and Run
```bash
# Build the project
mvn clean compile

# Run the application locally (development profile)
mvn spring-boot:run

# Build executable JAR
mvn clean package

# Run with specific profile
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

### Testing
```bash
# Run all tests
mvn test

# Run integration tests
mvn verify

# Generate test report
mvn surefire-report:report
```

### Docker
```bash
# Start all services (MySQL, Redis, backend)
docker-compose up -d

# Stop services
docker-compose down

# Rebuild and start
docker-compose up -d --build

# View logs
docker-compose logs -f backend
```

### Development Utilities
```bash
# Check dependencies
mvn dependency:tree

# Format code (if spotless or other formatter configured)
# mvn spotless:apply
```

## Architecture Overview

This is a Spring Boot 3.2.0 backend for an Android Ice Cream community application. Key architectural components:

### Technology Stack
- **Java 17** with Spring Boot 3.2.0
- **MySQL 8.1.0** primary database with MyBatis
- **Redis** for caching (Spring Cache abstraction)
- **Spring Security** with JWT authentication
- **SpringDoc OpenAPI** for API documentation
- **Lombok** for reducing boilerplate code
- **MapStruct** for object mapping between entities and DTOs
- **MyBatis** with XML mapping for data access
- **PageHelper** for pagination support

### Application Structure
The project follows standard Spring Boot layered architecture:
- `controller/` - REST endpoints (not yet created)
- `service/` - Business logic layer (not yet created)
- `mapper/` - MyBatis Mapper接口层 (已创建示例)
- `model/` - 实体类目录 (空)
- `dto/` - Data transfer objects (only ApiResponse exists)
- `config/` - Configuration classes (SecurityConfig, SwaggerConfig)
- `exception/` - Global exception handling
- `security/` - Security-related components (not yet created)
- `util/` - Utility classes and constants
- `aspect/` - AOP aspects (not yet created)

### Security
- JWT-based stateless authentication
- Password encryption with BCrypt
- Role-based access control (RBAC)
- CORS configured for frontend origins
- Request rate limiting planned

### Data Persistence
- MySQL with Hikari connection pool
- MyBatis with XML mapping files (stored in `src/main/resources/mapper/`)
- Redis for caching frequent queries and sessions
- Automatic underscore to camelCase mapping enabled

## Configuration

### Environment Profiles
- `dev` - Default profile for local development (MySQL: root/a123456789)
- `prod` - Production configuration (uses environment variables)
- `test` - Test environment

### Key Configuration Files
- `src/main/resources/application.yml` - Base configuration
- `src/main/resources/application-{profile}.yml` - Profile-specific overrides
- `.env.example` - Template for environment variables
- `docker-compose.yml` - Docker容器编排配置

### Essential Environment Variables
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` - MySQL connection (开发环境默认root/root)
- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` - 生产环境MySQL连接
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` - Redis connection
- `JWT_SECRET` - Minimum 256-bit secret for JWT signing
- `CORS_ALLOWED_ORIGINS` - Comma-separated origins for CORS
- `FILE_UPLOAD_DIR` - Directory for uploaded files

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

API documentation is automatically generated from Spring controllers using SpringDoc OpenAPI.

## Development Patterns

### Code Generation
- Use Lombok annotations (`@Data`, `@Getter`, `@Setter`) for entities
- Use MapStruct mappers for conversion between entities and DTOs
- MyBatis XML mapping files for SQL statements
- Annotation processors are configured in `pom.xml`

### Error Handling
- Global exception handler (`GlobalExceptionHandler`) provides consistent error responses
- Custom exceptions: `ResourceNotFoundException`, `BadRequestException`
- All API responses wrapped in `ApiResponse<T>` wrapper

### File Upload
- Maximum file size: 10MB
- Upload directory configurable via `FILE_UPLOAD_DIR`
- Files stored locally with path in `./uploads/`

## Docker Deployment

### Container Architecture
- `mysql:8.1.0` - MySQL database
- `redis:7-alpine` - Redis cache
- Custom Spring Boot application container

### Volumes
- `mysql_data` - MySQL persistent data
- `redis_data` - Redis persistent data
- `uploads` - Uploaded files
- `logs` - Application logs

### Health Checks
- MySQL: `mysqladmin ping` command
- Backend: Spring Actuator health endpoint

## Monitoring

Spring Boot Actuator provides:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Performance metrics
- `/actuator/loggers` - Log level configuration

## Database Management

### Schema Creation
使用MyBatis需要手动管理数据库Schema：
1. 创建数据库 `icecream_db` (开发环境) 或 `icecream_prod` (生产环境)
2. 执行SQL脚本创建表结构 (`docker/init.sql` 包含示例)
3. MyBatis XML映射文件中的SQL语句需要与数据库Schema匹配

### MyBatis配置
- XML映射文件位置: `src/main/resources/mapper/**/*.xml`
- Mapper接口位置: `com.icecream.backend.mapper`
- 启用驼峰命名自动映射: `map-underscore-to-camel-case: true`
- 开发环境启用SQL日志: `log-impl: org.apache.ibatis.logging.stdout.StdOutImpl`

### 分页支持
项目已集成PageHelper分页插件，在Service层可方便实现分页查询。

## Notes for Future Development

1. Controllers, services, and entities need to be implemented
2. Consider adding Flyway or Liquibase for schema versioning
3. Add integration tests with Testcontainers
4. Configure CI/CD pipeline
5. Implement rate limiting with Redis
6. Add monitoring with Micrometer and Prometheus
7. 本项目生成的代码，复杂或者难以理解的方法均需要加上中文注释
8. 本项目生成的文档，均使用简体中文
9. 接口更改需同步更改swagger文档
