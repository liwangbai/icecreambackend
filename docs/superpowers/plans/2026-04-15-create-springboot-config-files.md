# Spring Boot配置文件实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为icecreambackend项目创建完整的Spring Boot配置文件集，支持开发、测试和生产环境

**Architecture:** 采用分层配置架构，包括主配置文件(application.yml)和环境特定配置文件(application-dev.yml, application-prod.yml, application-test.yml)，支持多环境切换和敏感信息安全隔离

**Tech Stack:** Spring Boot 3.2.0, MySQL 8.1.0, Redis, MyBatis, Spring Security, SpringDoc OpenAPI

---

## 文件结构

### 创建的文件
- `src/main/resources/application.yml` - 主配置文件，包含通用设置
- `src/main/resources/application-dev.yml` - 开发环境配置，使用MySQL root/a123456789
- `src/main/resources/application-prod.yml` - 生产环境配置，使用环境变量注入
- `src/main/resources/application-test.yml` - 测试环境配置，使用H2内存数据库

### 修改的文件
- 无（全部为新建文件）

---

### Task 1: 创建主配置文件 application.yml

**Files:**
- Create: `src/main/resources/application.yml`

- [ ] **Step 1: 创建application.yml文件**

```yaml
# 应用程序基础配置
spring:
  application:
    name: icecreambackend
  
  # 配置文件激活规则
  profiles:
    active: dev
    group:
      dev: dev
      prod: prod
      test: test

  # Web服务器配置
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

# MyBatis配置
mybatis:
  # XML映射文件位置
  mapper-locations: classpath:mapper/**/*.xml
  # 实体类别名扫描包
  type-aliases-package: com.icecream.backend.model
  # 配置属性
  configuration:
    # 驼峰命名自动映射
    map-underscore-to-camel-case: true
    # 日志实现（开发环境可开启）
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  # 全局配置
  global-config:
    db-config:
      # 主键类型：自增
      id-type: auto

# PageHelper分页插件配置
pagehelper:
  helper-dialect: mysql
  reasonable: true
  support-methods-arguments: true
  params: count=countSql

# 应用服务配置
server:
  port: 8080
  servlet:
    context-path: /
    encoding:
      charset: UTF-8
      enabled: true

# Spring Security配置（基础）
security:
  basic:
    enabled: false
```

- [ ] **Step 2: 添加Actuator和SpringDoc配置**

```yaml
# Actuator监控端点配置
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

# SpringDoc OpenAPI配置
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha
  packages-to-scan: com.icecream.backend.controller
  default-consumes-media-type: application/json
  default-produces-media-type: application/json

# 日志配置
logging:
  level:
    com.icecream.backend: DEBUG
    org.springframework.web: INFO
    org.springframework.security: DEBUG
    org.mybatis: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log

# Redis缓存配置（基础模板）
spring:
  cache:
    type: redis
    redis:
      time-to-live: 60000
      cache-null-values: false
  data:
    redis:
      host: localhost
      port: 6379
      password:
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

- [ ] **Step 3: 验证文件语法**

运行: `mvn validate`
预期: BUILD SUCCESS

- [ ] **Step 4: 提交变更**

```bash
git add src/main/resources/application.yml
git commit -m "feat: add main application.yml configuration"
```

---

### Task 2: 创建开发环境配置文件 application-dev.yml

**Files:**
- Create: `src/main/resources/application-dev.yml`

- [ ] **Step 1: 创建application-dev.yml文件**

```yaml
# 开发环境配置
spring:
  # 数据源配置（MySQL 8.1.0）
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
  
  # JPA/Hibernate配置（用于实体映射验证）
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        use_sql_comments: true
    show-sql: true
  
  # Redis开发配置
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0

# MyBatis开发配置
mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    lazy-loading-enabled: true
    aggressive-lazy-loading: false

# 文件上传配置
file:
  upload:
    dir: ./uploads
    max-size: 10MB

# JWT配置（开发环境）
jwt:
  secret: your-256-bit-secret-key-change-this-in-production
  expiration: 86400000  # 24小时
  
# CORS配置（开发环境）
cors:
  allowed-origins: http://localhost:3000,http://localhost:8080
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  max-age: 3600
```

- [ ] **Step 2: 添加开发环境日志配置**

```yaml
# 日志级别（开发环境详细）
logging:
  level:
    com.icecream.backend: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.mybatis: DEBUG
    org.springframework.jdbc.core.JdbcTemplate: DEBUG
    org.springframework.jdbc.core.StatementCreatorUtils: TRACE
```

- [ ] **Step 3: 验证文件语法**

运行: `mvn validate`
预期: BUILD SUCCESS

- [ ] **Step 4: 提交变更**

```bash
git add src/main/resources/application-dev.yml
git commit -m "feat: add development environment configuration"
```

---

### Task 3: 创建生产环境配置文件 application-prod.yml

**Files:**
- Create: `src/main/resources/application-prod.yml`

- [ ] **Step 1: 创建application-prod.yml文件**

```yaml
# 生产环境配置
spring:
  # 生产环境数据源（通过环境变量配置）
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
  
  # JPA/Hibernate生产配置
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
  
  # Redis生产配置
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

# MyBatis生产配置
mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
    cache-enabled: true

# JWT生产配置（必须通过环境变量设置）
jwt:
  secret: ${JWT_SECRET:}
  expiration: ${JWT_EXPIRATION:86400000}

# CORS生产配置
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:}
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  max-age: 3600

# 文件上传生产配置
file:
  upload:
    dir: ${FILE_UPLOAD_DIR:./uploads}
    max-size: 10MB
```

- [ ] **Step 2: 添加生产环境日志和Actuator配置**

```yaml
# 日志生产配置
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
  logback:
    rollingpolicy:
      max-file-size: 10MB

# Actuator生产配置（安全限制）
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

- [ ] **Step 3: 验证文件语法**

运行: `mvn validate`
预期: BUILD SUCCESS

- [ ] **Step 4: 提交变更**

```bash
git add src/main/resources/application-prod.yml
git commit -m "feat: add production environment configuration"
```

---

### Task 4: 创建测试环境配置文件 application-test.yml

**Files:**
- Create: `src/main/resources/application-test.yml`

- [ ] **Step 1: 创建application-test.yml文件**

```yaml
# 测试环境配置
spring:
  # H2内存数据库用于测试
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:icecream_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
    username: sa
    password: 
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1
  
  # H2数据库Web控制台（仅测试环境）
  h2:
    console:
      enabled: true
      path: /h2-console
  
  # JPA测试配置
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
    show-sql: true
  
  # Redis测试配置（使用嵌入式或禁用）
  data:
    redis:
      host: localhost
      port: 6379
      # 或使用测试专用的Redis配置
  
  # 测试环境禁用某些功能
  main:
    allow-bean-definition-overriding: true

# MyBatis测试配置
mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# JWT测试配置
jwt:
  secret: test-jwt-secret-for-testing-only
  expiration: 3600000  # 1小时

# CORS测试配置
cors:
  allowed-origins: "*"
  allowed-methods: "*"
  allowed-headers: "*"
  allow-credentials: true
```

- [ ] **Step 2: 添加测试环境日志配置**

```yaml
# 日志测试配置
logging:
  level:
    com.icecream.backend: DEBUG
    org.springframework.test: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

# 测试专用配置
test:
  database:
    init-script: classpath:test-data.sql
```

- [ ] **Step 3: 验证文件语法**

运行: `mvn validate`
预期: BUILD SUCCESS

- [ ] **Step 4: 提交变更**

```bash
git add src/main/resources/application-test.yml
git commit -m "feat: add test environment configuration"
```

---

### Task 5: 验证配置文件集成

**Files:**
- All: `src/main/resources/application.yml`
- All: `src/main/resources/application-dev.yml`
- All: `src/main/resources/application-prod.yml`
- All: `src/main/resources/application-test.yml`

- [ ] **Step 1: 验证所有配置文件语法**

运行: `mvn validate`
预期: BUILD SUCCESS

- [ ] **Step 2: 测试开发环境配置**

运行: `mvn spring-boot:test-run -Dspring.profiles.active=dev`
预期: 应用能够正常编译，Spring Boot启动不报配置错误

- [ ] **Step 3: 测试生产环境配置（无环境变量）**

运行: `mvn spring-boot:test-run -Dspring.profiles.active=prod`
预期: 应用能够正常编译，使用默认值启动

- [ ] **Step 4: 测试测试环境配置**

运行: `mvn spring-boot:test-run -Dspring.profiles.active=test`
预期: 应用能够正常编译，使用H2数据库启动

- [ ] **Step 5: 提交最终验证**

```bash
git status
git diff --staged
git commit -m "feat: complete Spring Boot configuration files setup"
```

---

### Task 6: 创建配置文件验证测试

**Files:**
- Create: `src/test/java/com/icecream/backend/config/ConfigurationValidationTest.java`

- [ ] **Step 1: 创建配置验证测试类**

```java
package com.icecream.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConfigurationValidationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @ActiveProfiles("dev")
    void contextLoadsWithDevProfile() {
        // 验证开发环境配置能正常加载
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @ActiveProfiles("test")
    void contextLoadsWithTestProfile() {
        // 验证测试环境配置能正常加载
        assertThat(applicationContext).isNotNull();
    }
}
```

- [ ] **Step 2: 运行测试验证配置**

运行: `mvn test -Dtest=ConfigurationValidationTest`
预期: 测试通过，两个测试方法都成功

- [ ] **Step 3: 提交测试文件**

```bash
git add src/test/java/com/icecream/backend/config/ConfigurationValidationTest.java
git commit -m "test: add configuration validation tests"
```

---

### Task 7: 更新项目文档

**Files:**
- Modify: `README.md`

- [ ] **Step 1: 更新README.md中的配置说明**

在README.md文件中找到"配置说明"部分，更新为：

```markdown
## 配置说明

### 配置文件

项目使用多环境配置：

- `application.yml` - 主配置，包含通用设置（MyBatis、PageHelper、SpringDoc等）
- `application-dev.yml` - 开发环境配置（MySQL: root/a123456789, Redis: localhost:6379）
- `application-prod.yml` - 生产环境配置（通过环境变量注入敏感信息）
- `application-test.yml` - 测试环境配置（H2内存数据库）

### 环境变量

重要配置通过环境变量注入，参见 `.env.example` 文件。

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

### 激活环境

```bash
# 开发环境
mvn spring-boot:run -Dspring.profiles.active=dev

# 生产环境
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run

# 测试环境
mvn test -Dspring.profiles.active=test
```
```

- [ ] **Step 2: 验证README.md格式**

运行: `markdownlint README.md` 或检查Markdown语法
预期: 无严重格式错误

- [ ] **Step 3: 提交文档更新**

```bash
git add README.md
git commit -m "docs: update README with configuration details"
```

---

## 计划自检清单

### Spec覆盖检查
- [ ] 主配置文件(application.yml) - Task 1
- [ ] 开发环境配置(application-dev.yml) - Task 2
- [ ] 生产环境配置(application-prod.yml) - Task 3
- [ ] 测试环境配置(application-test.yml) - Task 4
- [ ] 配置文件集成验证 - Task 5
- [ ] 配置验证测试 - Task 6
- [ ] 文档更新 - Task 7

### 占位符扫描
- [ ] 无TBD、TODO占位符
- [ ] 所有步骤都有完整的代码示例
- [ ] 所有命令都有预期输出

### 类型一致性检查
- [ ] 配置文件路径一致
- [ ] 配置项名称一致
- [ ] 环境变量名称一致

## 执行选项

**计划已完成并保存到 `docs/superpowers/plans/2026-04-15-create-springboot-config-files.md`。两个执行选项：**

**1. Subagent-Driven (推荐)** - 我为每个任务分发一个全新的子代理，在任务之间进行审查，快速迭代

**2. Inline Execution** - 在此会话中使用executing-plans执行任务，批量执行并设置检查点

**请选择哪种方法？**