# 1) Vue 빌드
FROM node:20-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install
COPY frontend/ .
RUN npm run build

# 2) Spring 빌드 (Vue dist를 static에 포함)
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
COPY --from=frontend /app/dist ./src/main/resources/static
RUN mvn package -DskipTests -B

# 3) 실행
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache netcat-openbsd bash

COPY --from=builder /build/target/vuln-board-*.jar app.jar

ENV MYSQL_HOST=mysql
ENV MYSQL_PORT=3306
ENV MYSQL_DATABASE=vulndb
ENV MYSQL_USER=root
ENV MYSQL_PASSWORD=root

EXPOSE 8080

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN sed -i 's/\r$//' /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]
