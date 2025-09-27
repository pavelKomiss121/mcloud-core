FROM openjdk:21-jdk-slim AS builder
WORKDIR /build
COPY src/Todo.java ./
RUN javac Todo.java

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/ ./
CMD ["java", "Todo"]