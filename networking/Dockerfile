ARG SERVICE
FROM openjdk:21-jdk-slim AS builder
WORKDIR /build
RUN mkdir -p ru/mentee/power
COPY src/*.java ru/mentee/power/
RUN javac ru/mentee/power/*.java

FROM eclipse-temurin:21-jre-alpine
ARG SERVICE
ENV SERVICE=${SERVICE}
WORKDIR /app
COPY --from=builder /build/ru/ ./ru/
EXPOSE 8080
CMD ["sh", "-c", "java ru.mentee.power.${SERVICE}"]