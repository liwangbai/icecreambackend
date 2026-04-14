---
name: MySQL + MyBatis Migration Design
description: 从PostgreSQL + Spring Data JPA迁移到MySQL + MyBatis的完整设计规范
type: design
date: 2026-04-15
---

# MySQL + MyBatis 迁移设计规范

## 概述
本设计文档描述了将Ice Cream后端项目从PostgreSQL + Spring Data JPA架构迁移到MySQL + MyBatis架构的完整方案。迁移后，其他组件（Spring Boot、Spring Security、Redis、JWT等）保持不变。

## 设计决策摘要
- **数据库**: PostgreSQL → MySQL 8.0
- **数据访问层**: Spring Data JPA → MyBatis (XML映射方式)
- **项目结构**: 创建完整的MyBatis标准目录结构
- **配置文件**: 全面更新以适应MySQL和MyBatis
- **容器化**: 更新Docker相关文件使用MySQL

## 详细设计

### 1. 依赖修改
#### 1.1 移除的依赖
- `spring-boot-starter-data-jpa` (Spring Data JPA)
- `postgresql` (PostgreSQL驱动)

#### 1.2 新增的依赖
- `mybatis-spring-boot-starter` (版本: 3.0.3)
- `mysql-connector-java` (版本: 8.0.33)
- 可选: `pagehelper-spring-boot-starter` (版本: 1.4.7) - 分页插件

### 2. 配置文件更新
#### 2.1 主配置文件 (application.yml)
- 数据库URL从PostgreSQL格式改为MySQL格式
- 移除JPA配置部分 (`spring.jpa`)
- 新增MyBatis配置部分 (`mybatis`)
- 启用下划线到驼峰命名自动映射

#### 2.2 开发环境配置 (application-dev.yml)
- 数据库连接使用本地MySQL，用户名/密码: `root`/`root`
- 数据库名称: `icecream_dev`
- MyBatis日志级别设置为DEBUG
- 移除H2控制台配置

#### 2.3 生产环境配置 (application-prod.yml)
- 数据库连接使用环境变量
- 启用MyBatis缓存
- 禁用延迟加载以提高性能

#### 2.4 环境变量模板 (.env.example)
- 更新数据库相关环境变量为MySQL格式
- 保持其他配置不变

### 3. 项目结构
#### 3.1 新增目录结构
```
src/main/java/com/icecream/backend/
├── model/                  # 实体类目录（空）
├── mapper/                 # MyBatis Mapper接口目录（空）
├── service/                # Service层目录（空）
└── controller/             # Controller层目录（空）

src/main/resources/
└── mapper/                 # MyBatis XML映射文件目录（空）
```

#### 3.2 示例文件
1. **ExampleMapper.java** - 示例Mapper接口
2. **ExampleMapper.xml** - 示例XML映射文件
3. **README.txt** - 目录结构说明

### 4. Docker配置更新
#### 4.1 docker-compose.yml
- 将`postgres`服务替换为`mysql:8.0`服务
- 更新数据库连接配置
- 更新健康检查命令为MySQL格式
- 更新环境变量映射

#### 4.2 数据卷
- `postgres_data` → `mysql_data`
- 其他卷保持不变

#### 4.3 初始化脚本
- 可选创建`init.sql`用于数据库初始化

### 5. CLAUDE.md文档更新
#### 5.1 技术栈说明
- 数据库: PostgreSQL → MySQL
- 数据访问: Spring Data JPA → MyBatis

#### 5.2 架构概述
- 更新项目结构说明
- 说明MyBatis XML映射的使用方式

#### 5.3 配置说明
- 更新环境变量格式
- 添加MyBatis配置说明

#### 5.4 数据库管理
- 移除JPA `ddl-auto`相关说明
- 说明手动SQL管理方式

#### 5.5 Docker部署
- 更新为MySQL容器配置
- 更新健康检查说明

### 6. 迁移策略
#### 6.1 阶段一：依赖和配置更新
- 更新pom.xml依赖
- 更新所有配置文件
- 创建目录结构

#### 6.2 阶段二：Docker文件更新
- 更新docker-compose.yml
- 更新环境变量配置

#### 6.3 阶段三：文档更新
- 更新CLAUDE.md
- 更新README.md（如果需要）

#### 6.4 阶段四：验证
- 确保应用能够启动
- 确保数据库连接正常
- 确保MyBatis配置正确

## 技术细节

### 数据库连接配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/icecream_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### MyBatis配置
```yaml
mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.icecream.backend.model
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
```

### 开发环境特定配置
```yaml
# application-dev.yml
spring:
  mybatis:
    configuration:
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

logging:
  level:
    com.icecream.backend: DEBUG
    org.apache.ibatis: DEBUG
```

## 成功标准
1. 应用能够成功启动并连接到MySQL数据库
2. MyBatis配置正确，能够解析XML映射文件
3. 所有配置文件语法正确，无错误
4. Docker容器能够正常启动并连接
5. 文档更新完整准确

## 风险与缓解
1. **数据库兼容性问题**: 验证SQL语法兼容性
2. **MyBatis学习曲线**: 提供示例文件作为参考
3. **配置错误**: 分阶段验证，先验证配置再验证功能
4. **环境变量冲突**: 更新所有相关配置文件

## 后续建议
1. 考虑添加数据库迁移工具（如Flyway）
2. 考虑添加MyBatis Generator自动生成代码
3. 添加MyBatis相关测试示例
4. 监控MyBatis SQL执行性能