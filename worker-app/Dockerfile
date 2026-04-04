FROM gradle:8.10.2-jdk17 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .

COPY common-core common-core
COPY file-domain file-domain
COPY infra-rabbit infra-rabbit
COPY infra-redis infra-redis
COPY infra-s3 infra-s3
COPY worker-app worker-app

RUN chmod +x gradlew && ./gradlew :worker-app:bootJar -x test

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /app/worker-app/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]