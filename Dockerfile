# Stage 1: Build application with Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application artifact
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Download OpenTelemetry Java agent for zero-code instrumentation
RUN wget -q -O /app/opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar || true

COPY --from=builder /build/target/*.jar /app/app.jar

ENV SERVER_PORT=5100
EXPOSE 5100

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
