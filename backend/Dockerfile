FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
RUN apk add --no-cache python3 py3-pip build-base libxml2-dev libxslt-dev python3-dev && \
    ln -sf /usr/bin/python3 /usr/bin/python && \
    pip3 install feedparser requests beautifulsoup4 trafilatura kafka-python --break-system-packages
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
