FROM openjdk:21-jdk-slim AS builder
WORKDIR /build
RUN mkdir -p ru/mentee/power
COPY src/main/java/ru/mentee/power/SimpleWebServer.java ru/mentee/power/
RUN javac ru/mentee/power/SimpleWebServer.java

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/ru/ ./ru/
EXPOSE 8080
CMD ["java", "ru.mentee.power.SimpleWebServer"]