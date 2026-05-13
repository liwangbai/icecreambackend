# 构建阶段
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安装wget用于健康检查，安装tini用于进程管理
RUN apk add --no-cache wget tini

# 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring

# 创建必要目录
RUN mkdir -p /app/uploads /app/logs && chown -R spring:spring /app

COPY --from=build /app/target/*.jar app.jar

USER spring:spring

# 健康检查（使用 /manage 路径匹配生产环境 actuator base-path）
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/manage/health || exit 1

EXPOSE 8080

# tini 作为 init 进程，优雅处理 SIGTERM
ENTRYPOINT ["/sbin/tini", "--"]
CMD java \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=prod \
    -jar app.jar
