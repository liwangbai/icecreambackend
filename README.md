# Icecream Backend

Spring Boot 后端项目，为 Icecream Android 应用提供 API 接口，管理社区数据和用户数据。

## 技术栈

- **Java 17**
- **Spring Boot 3.2.0**
- **PostgreSQL** - 主数据库
- **Redis** - 缓存
- **Spring Security** - 安全认证
- **JWT** - 令牌认证
- **Spring Data JPA** - 数据访问
- **SpringDoc OpenAPI** - API 文档
- **Docker** - 容器化

## 项目结构

```
icecreambackend/
├── src/main/java/com/icecream/backend/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器层
│   ├── service/         # 服务层
│   ├── repository/      # 数据访问层
│   ├── model/          # 实体模型
│   ├── dto/            # 数据传输对象
│   ├── exception/      # 异常处理
│   ├── security/       # 安全相关
│   ├── util/           # 工具类
│   └── aspect/         # 切面编程
├── src/main/resources/
│   ├── application.yml          # 主配置文件
│   ├── application-dev.yml      # 开发环境配置
│   ├── application-prod.yml     # 生产环境配置
│   └── application-test.yml     # 测试环境配置
├── src/test/           # 测试代码
├── Dockerfile          # Docker 构建文件
├── docker-compose.yml  # Docker Compose 编排
├── pom.xml            # Maven 配置
└── .env.example       # 环境变量示例
```

## 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.6+
- Docker 20.10+ (可选)
- PostgreSQL 15+ (可选，可使用 Docker)

### 2. 本地开发

#### 使用 Docker Compose（推荐）

```bash
# 复制环境变量文件
cp .env.example .env

# 编辑 .env 文件，设置 JWT_SECRET 等参数

# 启动所有服务
docker-compose up -d
```

#### 手动启动

1. 启动 PostgreSQL 和 Redis
2. 配置数据库连接参数
3. 运行应用：

```bash
mvn clean spring-boot:run
```

### 3. API 文档

应用启动后，访问以下地址查看 API 文档：

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### 4. 健康检查

- 应用健康状态: http://localhost:8080/actuator/health
- 应用信息: http://localhost:8080/actuator/info

## 配置说明

### 配置文件

项目使用多环境配置：

- `application.yml` - 主配置，包含通用设置
- `application-dev.yml` - 开发环境配置
- `application-prod.yml` - 生产环境配置
- `application-test.yml` - 测试环境配置

### 环境变量

重要配置通过环境变量注入，参见 `.env.example` 文件。

## 数据库

### 实体关系

主要实体包括：

1. **User** - 用户信息
2. **Community** - 社区信息
3. **Post** - 帖子信息
4. **Comment** - 评论信息

### 数据库迁移

项目使用 JPA 的 `ddl-auto: validate` 模式，需要手动创建数据库表结构。

## 安全特性

- JWT 令牌认证
- 基于角色的访问控制 (RBAC)
- 密码加密 (BCrypt)
- CORS 配置
- 请求速率限制

## 部署

### Docker 部署

```bash
# 构建镜像
docker build -t icecreambackend .

# 运行容器
docker run -p 8080:8080 --env-file .env icecreambackend
```

### Kubernetes 部署

参见 `kubernetes/` 目录下的部署配置文件（待补充）。

## 开发指南

### 代码规范

1. 使用 Lombok 减少样板代码
2. 使用 MapStruct 进行对象映射
3. 遵循 RESTful API 设计原则
4. 使用统一的异常处理
5. 所有 API 返回统一的响应格式

### 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 生成测试报告
mvn surefire-report:report
```

## 监控

应用集成了 Spring Boot Actuator，提供以下端点：

- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标
- `/actuator/loggers` - 日志配置

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

Apache License 2.0