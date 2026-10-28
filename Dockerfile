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

# Download the New Relic Java APM agent (set NR_AGENT_VERSION to pin, e.g. 8.21.0)
ARG NR_AGENT_VERSION=current
RUN wget -q -O /tmp/newrelic-java.zip \
    https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${NR_AGENT_VERSION}/newrelic-java.zip \
    && unzip -q /tmp/newrelic-java.zip -d /app \
    && rm /tmp/newrelic-java.zip

COPY --from=builder /build/target/*.jar /app/app.jar

ENV SERVER_PORT=5100 \
    NEW_RELIC_APP_NAME=doodle-weather-java \
    NEW_RELIC_LOG_FILE_NAME=STDOUT \
    NEW_RELIC_DISTRIBUTED_TRACING_ENABLED=true
EXPOSE 5100

ENTRYPOINT ["java", "-javaagent:/app/newrelic/newrelic.jar", "-jar", "/app/app.jar"]
