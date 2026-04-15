---
title: "Spring Boot配置文件设计方案"
date: 2026-04-15
author: Claude Code
project: icecreambackend
type: design-spec
---

# Spring Boot配置文件设计方案

## 项目概述
为icecreambackend项目创建完整的Spring Boot配置文件集，支持多环境开发部署。项目使用MySQL 8.1.0作为主数据库，Redis作为缓存，MyBatis作为数据访问层。

## 设计目标
1. 提供清晰的环境隔离（开发、测试、生产）
2. 确保敏感信息安全（生产环境使用环境变量）
3. 优化性能配置（连接池、缓存策略）
4. 遵循Spring Boot最佳实践
5. 支持Docker容器化部署

## 配置文件架构

### 文件结构
```
src/main/resources/
├── application.yml          # 主配置文件（通用设置）
├── application-dev.yml      # 开发环境配置
├── application-prod.yml     # 生产环境配置
└── application-test.yml     # 测试环境配置
```

### 配置优先级
1. 环境变量（最高优先级）
2. 环境特定配置文件（application-{env}.yml）
3. 主配置文件（application.yml）
4. Spring Boot默认配置

## 配置文件详细设计

### 1. application.yml（主配置文件）

#### 基础配置
```yaml
# 应用程序基础配置
spring:
  application:
    name: icecreambackend
  profiles:
    active: dev
    group:
      dev: dev
      prod: prod
      test: test
```

#### MyBatis配置
```yaml
mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.icecream.backend.model
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
```

#### PageHelper分页插件
```yaml
pagehelper:
  helper-dialect: mysql
  reasonable: true
  support-methods-arguments: true
  params: count=countSql
```

#### Web服务器配置
```yaml
server:
  port: 8080
  servlet:
    context-path: /
    encoding:
      charset: UTF-8
      enabled: true
```

#### Actuator监控
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
```

#### SpringDoc OpenAPI
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha
  packages-to-scan: com.icecream.backend.controller
```

### 2. application-dev.yml（开发环境配置）

#### MySQL数据库配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/icecream_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: a123456789
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      pool-name: IcecreamHikariPool
```

#### JPA验证配置
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        use_sql_comments: true
    show-sql: true
```

#### Redis开发配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
```

#### JWT配置（开发环境）
```yaml
jwt:
  secret: your-256-bit-secret-key-change-this-in-production
  expiration: 86400000  # 24小时
```

#### CORS配置
```yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:8080
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  max-age: 3600
```

#### 文件上传配置
```yaml
file:
  upload:
    dir: ./uploads
    max-size: 10MB
```

#### 日志配置（详细）
```yaml
logging:
  level:
    com.icecream.backend: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.mybatis: DEBUG
    org.springframework.jdbc.core.JdbcTemplate: DEBUG
```

### 3. application-prod.yml（生产环境配置）

#### 生产环境数据库（环境变量注入）
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${DATABASE_URL:jdbc:mysql://localhost:3306/icecream_prod?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true}
    username: ${DATABASE_USERNAME:root}
    password: ${DATABASE_PASSWORD:}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      validation-timeout: 5000
      leak-detection-threshold: 60000
```

#### JPA生产配置
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
        jdbc.batch_versioned_data: true
    show-sql: false
```

#### Redis生产配置
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: -1ms
```

#### JWT生产配置（必须环境变量）
```yaml
jwt:
  secret: ${JWT_SECRET:}
  expiration: ${JWT_EXPIRATION:86400000}
```

#### CORS生产配置
```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:}
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  max-age: 3600
```

#### 生产日志配置
```yaml
logging:
  level:
    com.icecream.backend: INFO
    org.springframework: INFO
    org.mybatis: WARN
  file:
    name: logs/application-prod.log
    max-size: 10MB
    max-history: 30
    total-size-cap: 1GB
```

#### Actuator安全配置
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
      base-path: /manage
  endpoint:
    health:
      show-details: never
    info:
      enabled: true
```

### 4. application-test.yml（测试环境配置）

#### H2内存数据库
```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:icecream_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
    username: sa
    password: 
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1
```

#### H2控制台
```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

#### JPA测试配置
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
    show-sql: true
```

#### JWT测试配置
```yaml
jwt:
  secret: test-jwt-secret-for-testing-only
  expiration: 3600000  # 1小时
```

#### CORS测试配置
```yaml
cors:
  allowed-origins: "*"
  allowed-methods: "*"
  allowed-headers: "*"
  allow-credentials: true
```

#### 测试日志配置
```yaml
logging:
  level:
    com.icecream.backend: DEBUG
    org.springframework.test: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

## 设计原则

### 1. 安全性原则
- 生产环境敏感信息完全通过环境变量注入
- JWT密钥必须在生产环境设置256位密钥
- 生产环境API文档和安全端点有限访问

### 2. 性能优化原则
- 开发环境：连接池较小，日志详细
- 生产环境：连接池较大，批量操作优化，日志精简
- 缓存策略：开发环境短TTL，生产环境长TTL

### 3. 可维护性原则
- 配置按功能模块分组
- 提供安全的默认值
- 环境变量与.env.example模板保持一致

### 4. 兼容性原则
- MySQL 8.1.0兼容性配置（时区、SSL、公钥检索）
- 支持Docker容器化部署
- 多环境无缝切换

## 环境变量映射

| 环境变量 | 默认值 | 描述 |
|---------|--------|------|
| DATABASE_URL | jdbc:mysql://localhost:3306/icecream_prod... | 生产数据库URL |
| DATABASE_USERNAME | root | 数据库用户名 |
| DATABASE_PASSWORD | (无) | 数据库密码 |
| REDIS_HOST | localhost | Redis主机 |
| REDIS_PORT | 6379 | Redis端口 |
| REDIS_PASSWORD | (空) | Redis密码 |
| JWT_SECRET | (必须设置) | JWT签名密钥 |
| JWT_EXPIRATION | 86400000 | JWT过期时间(ms) |
| CORS_ALLOWED_ORIGINS | (空) | CORS允许的源 |
| FILE_UPLOAD_DIR | ./uploads | 文件上传目录 |

## 验证清单

### 配置文件创建后验证
1. [ ] `application.yml` - 通用配置完整
2. [ ] `application-dev.yml` - 开发配置正确（MySQL连接、用户名root、密码a123456789）
3. [ ] `application-prod.yml` - 生产配置模板正确
4. [ ] `application-test.yml` - 测试配置正确
5. [ ] 所有配置文件语法正确（YAML格式）
6. [ ] 配置文件路径正确（src/main/resources/）
7. [ ] 与现有项目结构兼容
8. [ ] 支持Maven构建和Spring Boot运行

### 功能验证
1. [ ] 应用能正常启动（开发环境）
2. [ ] 数据库连接成功（MySQL 8.1.0）
3. [ ] MyBatis映射正常工作
4. [ ] 分页插件配置生效
5. [ ] CORS配置正确
6. [ ] 文件上传功能正常
7. [ ] 环境切换正常（dev/prod/test）

## 后续步骤

1. **立即执行**：创建4个配置文件到`src/main/resources/`
2. **测试验证**：使用开发环境配置运行应用
3. **Docker集成**：创建/更新docker-compose.yml（如果需要）
4. **文档更新**：更新README.md中的配置说明
5. **团队同步**：确保团队了解配置变更

## 变更记录

| 日期 | 版本 | 变更描述 | 作者 |
|------|------|----------|------|
| 2026-04-15 | 1.0 | 初始设计 | Claude Code |

---
*设计文档完成时间：2026-04-15*