FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY build.gradle .
COPY src/ ./src/
RUN gradle build -x test


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN mkdir -p /app/config
# Открываем порт
EXPOSE 8080

# Устанавливаем переменные окружения по умолчанию
ENV SERVER_PORT=8080
ENV DATABASE_HOST=postgres-service
ENV DATABASE_PORT=5432
ENV DATABASE_NAME=calculator_db
ENV DATABASE_USER=calculator_user
ENV LOG_LEVEL=INFO
ENV CACHE_ENABLED=false
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

CMD ["java", "-jar", "app.jar"]