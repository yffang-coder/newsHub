# Stage 1: Build dependencies
FROM maven:3.9-eclipse-temurin-17-alpine AS dependency-builder
WORKDIR /app
COPY pom.xml .
# 使用 go-offline 下载所有传递性依赖，并将其缓存到 /root/.m2
RUN mvn dependency:go-offline -B

# Stage 2: Build application
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
# 从 dependency-builder 阶段拷贝缓存的 Maven 本地仓库
COPY --from=dependency-builder /root/.m2 /root/.m2
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 3: Final image
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
RUN apk add --no-cache python3 py3-pip build-base libxml2-dev libxslt-dev python3-dev && \
    ln -sf /usr/bin/python3 /usr/bin/python && \
    pip3 install --no-cache-dir feedparser requests beautifulsoup4 trafilatura kafka-python --break-system-packages
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
